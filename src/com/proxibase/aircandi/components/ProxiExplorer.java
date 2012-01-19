package com.proxibase.aircandi.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;

import com.proxibase.aircandi.Aircandi;
import com.proxibase.aircandi.Preferences;
import com.proxibase.aircandi.CandiRadar.EventBus;
import com.proxibase.aircandi.components.NetworkManager.ResponseCode;
import com.proxibase.aircandi.components.NetworkManager.ServiceResponse;
import com.proxibase.sdk.android.proxi.consumer.Beacon;
import com.proxibase.sdk.android.proxi.consumer.Entity;
import com.proxibase.sdk.android.proxi.consumer.Beacon.BeaconState;
import com.proxibase.sdk.android.proxi.consumer.Entity.EntityState;
import com.proxibase.sdk.android.proxi.service.ProxibaseService;
import com.proxibase.sdk.android.proxi.service.ServiceRequest;
import com.proxibase.sdk.android.proxi.service.ProxibaseService.GsonType;
import com.proxibase.sdk.android.proxi.service.ProxibaseService.ProxibaseException;
import com.proxibase.sdk.android.proxi.service.ProxibaseService.RequestType;
import com.proxibase.sdk.android.proxi.service.ProxibaseService.ResponseFormat;
import com.proxibase.sdk.android.util.ProxiConstants;

public class ProxiExplorer {

	private static ProxiExplorer	singletonObject;
	private Context					mContext;

	private List<Beacon>			mBeacons				= new ArrayList<Beacon>();
	public List<Entity>				mEntities				= new ArrayList<Entity>();
	public HashMap					mEntitiesUpdated		= new HashMap();
	public HashMap					mEntitiesInserted		= new HashMap();
	public HashMap					mEntitiesDeleted		= new HashMap();

	private Boolean					mScanRequestActive		= false;
	private Boolean					mScanRequestProcessing	= false;

	public List<WifiScanResult>		mWifiList				= new ArrayList<WifiScanResult>();
	private WifiManager				mWifiManager;
	private WifiLock				mWifiLock;
	private boolean					mUsingEmulator			= false;
	private final static int		demoLevel				= -20;
	private final static String		demoBssid				= "48:5b:39:e6:d3:55";
	private final static String		demoSsid				= "demo_dolly";
	private final static int		globalLevel				= -20;
	private final static String		globalBssid				= "00:00:00:00:00:00";
	private final static String		globalSsid				= "candi_feed";

	public static synchronized ProxiExplorer getInstance() {
		if (singletonObject == null) {
			singletonObject = new ProxiExplorer();
		}
		return singletonObject;
	}

	/**
	 * Designed as a singleton. The private Constructor prevents any other class from instantiating.
	 */
	private ProxiExplorer() {}

	/**
	 * Returns a {@link List} collection of {@link Entity} objects currently associated with
	 * wifi beacons visible to the host device.
	 * The bssid's for any discovered beacons are sent to the proxibase service to lookup
	 * any entity proxies that are currently associated with the beacon keyed on the bssid.
	 * <p>
	 * For data traffic efficiency, beacons are tracked once they are discovered and only new beacons are processed
	 * during subsequent calls. The fullUpdate param is used to clear the tracked beacons forcing a complete refresh.
	 * Using fullUpdate is recommended to ensure that beacons with new or updated Entity objects are current.
	 * <p>
	 * To force a beacon to be refreshed without forcing a full update, use the setInvalid method on the beacon in the
	 * Beacons collection.
	 * 
	 * @param fullUpdate if true then the existing beacon collection is cleared.
	 * @param listener the callback used to return the Entity collection.
	 */
	public void scanForBeacons(Options options) {

		mWifiList.clear();

		if (!mUsingEmulator) {
			if (mContext == null) {
				throw new IllegalStateException("Context must be set before calling scanForBeaconsAsync");
			}

			mScanRequestActive = true;

			mContext.registerReceiver(new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {

					/* We only process scan results we have requested */
					if (mScanRequestActive && !mScanRequestProcessing) {

						mScanRequestActive = false;
						wifiReleaseLock();

						/* Get the latest scan results */
						mWifiList.clear();
						boolean demoBeaconFound = false;
						for (ScanResult scanResult : mWifiManager.getScanResults()) {
							mWifiList.add(new WifiScanResult(scanResult));
							if (scanResult.BSSID.equals(demoBssid))
								demoBeaconFound = true;
						}

						/* Insert wifi results if in demo mode */
						if (Aircandi.settings.getBoolean(Preferences.PREF_DEMO_MODE, false) && !demoBeaconFound) {
							mWifiList.add(new WifiScanResult(demoBssid, demoSsid, demoLevel));
						}

						if (Aircandi.settings.getBoolean(Preferences.PREF_GLOBAL_BEACONS, true)) {
							mWifiList.add(new WifiScanResult(globalBssid, globalSsid, globalLevel));
						}

						Logger.d(this, "Received wifi scan results");
						EventBus.onWifiScanReceived(null);
					}
				}
			}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

			/*
			 * WIFI_MODE_FULL: Keeps the wifi radio active and behaving normally per user settings.
			 * 
			 * WIFI_MODE_SCAN_ONLY: Wi-Fi will be kept active, but the only operation that will be supported is
			 * initiation of scans, and the subsequent reporting of scan results. This would work fine
			 * but auto-refresh will not keep the device awake and when it sleeps, any auto-refresh
			 * that results in new beacons and service requests will find the wifi connection dead.
			 * 
			 * Acquiring a wifilock requires WakeLock permission.
			 */
			if (Aircandi.settings.getBoolean(Preferences.PREF_AUTOSCAN, false)) {
				wifiLockAcquire(WifiManager.WIFI_MODE_FULL);
			}

			for (ScanResult scanResult : mWifiManager.getScanResults()) {
				mWifiList.add(new WifiScanResult(scanResult));
			}

			Logger.d(this, "Requesting system wifi scan");

			/* Beacon and entity processing start when we recieve the scan results */
			mWifiManager.startScan();
		}
		else {
			Logger.d(this, "Emulator enabled so using dummy scan results");
			mWifiList.add(new WifiScanResult(demoBssid, demoSsid, demoLevel));
			EventBus.wifiScanReceived.onEvent(null);
		}
	}

	/**
	 * Refreshes only beacons and entities that have been flagged as dirty.
	 * 
	 * @return Updated master entity list which includes all entities refreshed or not.
	 * @throws ProxibaseException
	 */

	public ServiceResponse refreshDirtyEntities() {
		/*
		 * For beacons:
		 * - first entity or addtional entities could be added.
		 * - existing entities could be modified or gone.
		 */
		ServiceResponse serviceResponse = new ServiceResponse();
		for (Beacon beacon : mBeacons) {

			if (beacon.dirty) {
				beacon.dirty = false;
				beacon.entities.clear();

				if (Aircandi.settings.getBoolean(Preferences.PREF_AUTOSCAN, false)) {
					wifiLockAcquire(WifiManager.WIFI_MODE_FULL);
				}

				Bundle parameters = new Bundle();
				parameters.putString("beaconBssid", beacon.id);
				parameters.putInt("userId", Integer.parseInt(Aircandi.getInstance().getUser().id));

				ServiceRequest serviceRequest = new ServiceRequest();
				serviceRequest.setUri(ProxiConstants.URL_PROXIBASE_SERVICE + "GetEntitiesForBeacon");
				serviceRequest.setRequestType(RequestType.Method);
				serviceRequest.setParameters(parameters);
				serviceRequest.setResponseFormat(ResponseFormat.Json);

				serviceResponse = NetworkManager.getInstance().request(serviceRequest);

				wifiReleaseLock();

				if (serviceResponse.responseCode == ResponseCode.Success) {

					String jsonResponse = (String) serviceResponse.data;
					List<Object> freshEntities = ProxibaseService.convertJsonToObjects(jsonResponse, Entity.class, GsonType.ProxibaseService);

					if (freshEntities != null && freshEntities.size() > 0) {

						for (Object obj : freshEntities) {
							Entity freshEntity = (Entity) obj;

							freshEntity.state = EntityState.Refreshed;
							freshEntity.beacon = beacon;
							setEntityVisibility(freshEntity, beacon);

							for (Entity childEntity : freshEntity.children) {
								childEntity.beacon = beacon;
								childEntity.state = EntityState.Refreshed;
								setEntityVisibility(childEntity, beacon);
							}
							beacon.entities.add(freshEntity);
						}
					}
				}
			}
			else {
				for (int i = beacon.entities.size() - 1; i >= 0; i--) {
					if (serviceResponse.responseCode != ResponseCode.Success) {
						break;
					}
					Entity entity = beacon.entities.get(i);
					if (entity.dirty) {
						/*
						 * Includes call to rebuildEntityList()
						 */
						serviceResponse = doRefreshEntity(entity.id);
						if (serviceResponse.responseCode != ResponseCode.Success) {
							break;
						}
					}
					else {
						for (int j = entity.children.size() - 1; j >= 0; j--) {
							Entity childEntity = entity.children.get(j);
							if (childEntity.dirty) {
								serviceResponse = doRefreshEntity(childEntity.id);
								if (serviceResponse.responseCode != ResponseCode.Success) {
									break;
								}
							}
						}
					}
				}
			}
		}

		if (serviceResponse.responseCode == ResponseCode.Success) {
			rebuildEntityList();
			serviceResponse.data = mEntities;
		}

		return serviceResponse;
	}

	// --------------------------------------------------------------------------------------------
	// Entities
	// --------------------------------------------------------------------------------------------

	public void initialize() {
		if (!mUsingEmulator) {
			mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		}
	}

	@SuppressWarnings("unused")
	private boolean scanResultsContain(String bsid) {
		for (WifiScanResult wifiScanResult : mWifiList) {
			if (wifiScanResult.BSSID.equals(bsid)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unused")
	private void updateOrInsertDemoBeacon(Beacon beacon, String bssid, String label) {

		/* Keep method until we are sure we won't be using it. */
		if (beacon == null) {
			Beacon beaconNew = new Beacon(bssid, label, label, demoLevel, DateUtils.nowDate());
			beaconNew.detectedLastPass = true;
			beaconNew.state = BeaconState.New;
			beaconNew.addScanPass(demoLevel);
			mBeacons.add(beaconNew);
		}
		else {
			beacon.detectedLastPass = true;
			beacon.state = BeaconState.Normal;
			beacon.addScanPass(demoLevel);
		}
	}

	public void processBeaconsFromScan(boolean refreshAllBeacons) {
		/*
		 * Full update: Queries service for entities for every discovered beacon.
		 * Partial update: Queries service for entities for beacons that were not discovered on previous scans.
		 * Entities from previous scans will still be updated for local changes in visibility.
		 */
		Logger.d(this, "Processing beacons from scan");
		ServiceResponse serviceResponse = new ServiceResponse();
		mScanRequestProcessing = true;

		/* Clear beacon collection for a complete rebuild */
		if (refreshAllBeacons)
			mBeacons.clear();

		/* Reset detection flag */
		for (Beacon beacon : mBeacons)
			beacon.detectedLastPass = false;

		/* Walk all the latest wifi scan hits */
		for (int i = 0; i < mWifiList.size(); i++) {

			final WifiScanResult scanResult = mWifiList.get(i);

			/* See if we are already tracking the beacon */
			Beacon beaconMatch = findBeaconById(scanResult.BSSID);

			/* Add it if we aren't */
			if (beaconMatch == null) {
				Beacon beaconNew = new Beacon(scanResult.BSSID, scanResult.SSID, scanResult.SSID, scanResult.level, DateUtils.nowDate());
				beaconNew.detectedLastPass = true;
				beaconNew.state = BeaconState.New;
				beaconNew.addScanPass(scanResult.level);
				mBeacons.add(beaconNew);
			}
			else {
				beaconMatch.detectedLastPass = true;
				beaconMatch.state = BeaconState.Normal;
				beaconMatch.addScanPass(scanResult.level);
			}
		}

		/* Remove beacons that have too many scan misses */
		for (int i = mBeacons.size() - 1; i >= 0; i--) {
			Beacon beacon = mBeacons.get(i);
			if (!beacon.detectedLastPass) {
				beacon.scanMisses++;
				if (beacon.scanMisses >= 3) {
					mBeacons.remove(i);
				}
			}
		}

		/*
		 * Call the proxi service to see if the new beacons have been tagged with any entities.
		 * If call comes back null then there was a network or service problem.
		 * The user got a toast notification from the service. We are making synchronous calls inside
		 * an asynchronous thread.
		 */

		/* Set state of all pre-existing entities to Normal */
		for (Beacon beacon : mBeacons) {
			for (Entity entity : beacon.entities) {
				entity.state = EntityState.Normal;
				for (Entity childEntity : entity.children) {
					childEntity.state = EntityState.Normal;
				}
			}
		}

		/* Construct string array of the beacon ids */
		ArrayList<String> refreshBeaconIds = new ArrayList<String>();
		for (Beacon beacon : mBeacons) {
			if (beacon.state == BeaconState.New) {
				beacon.state = BeaconState.Normal;
				refreshBeaconIds.add(beacon.id);
			}
		}

		if (refreshBeaconIds.size() > 0) {

			if (Aircandi.settings.getBoolean(Preferences.PREF_AUTOSCAN, false)) {
				wifiLockAcquire(WifiManager.WIFI_MODE_FULL);
			}

			Bundle parameters = new Bundle();
			parameters.putStringArrayList("beaconBssids", refreshBeaconIds);
			parameters.putInt("userId", Integer.parseInt(Aircandi.getInstance().getUser().id));

			ServiceRequest serviceRequest = new ServiceRequest();
			serviceRequest.setUri(ProxiConstants.URL_PROXIBASE_SERVICE + "GetEntitiesForBeacons");
			serviceRequest.setRequestType(RequestType.Method);
			serviceRequest.setParameters(parameters);
			serviceRequest.setResponseFormat(ResponseFormat.Json);

			serviceResponse = NetworkManager.getInstance().request(serviceRequest);

			wifiReleaseLock();

			if (serviceResponse.responseCode == ResponseCode.Success) {

				String jsonResponse = (String) serviceResponse.data;
				List<Object> freshEntities = ProxibaseService.convertJsonToObjects(jsonResponse, Entity.class, GsonType.ProxibaseService);

				/* Match returned entities back to beacons */
				if (freshEntities != null && freshEntities.size() > 0) {
					for (Object obj : freshEntities) {
						Entity freshEntity = (Entity) obj;

						for (Beacon beacon : mBeacons) {
							if (beacon.id.equals(freshEntity.beaconId)) {

								beacon.entities.add(freshEntity);
								freshEntity.state = EntityState.New;
								freshEntity.beacon = beacon;
								for (Entity childEntity : freshEntity.children) {
									childEntity.beacon = beacon;
									childEntity.state = EntityState.New;
								}
							}
						}
					}
				}
			}
		}

		/* Rebuild the top level entity list */
		mEntities.clear();
		for (Beacon beacon : mBeacons) {
			for (Entity entity : beacon.entities) {
				mEntities.add(entity);
			}
		}

		manageEntityVisibility();
		EventBus.onEntitiesLoaded(serviceResponse);
		mScanRequestProcessing = false;
		return;
	}

	private Beacon findBeaconById(String beaconId) {
		for (Beacon beacon : mBeacons) {
			if (beacon.id.equals(beaconId)) {
				return beacon;
			}
		}
		return null;
	}

	private ServiceResponse doRefreshEntity(int entityId) {

		ServiceResponse serviceResponse = new ServiceResponse();
		Entity targetEntity = getEntityById(entityId);

		for (Beacon beacon : mBeacons) {
			if (beacon.entities.contains(targetEntity)) {

				/* We are doing a replace not a merge */
				Entity entity = beacon.entities.get(beacon.entities.indexOf(targetEntity));

				if (Aircandi.settings.getBoolean(Preferences.PREF_AUTOSCAN, false)) {
					wifiLockAcquire(WifiManager.WIFI_MODE_FULL);
				}

				Bundle parameters = new Bundle();
				parameters.putInt("entityId", entity.id);
				parameters.putBoolean("includeChildren", true);

				ServiceRequest serviceRequest = new ServiceRequest();
				serviceRequest.setUri(ProxiConstants.URL_PROXIBASE_SERVICE + "GetEntity");
				serviceRequest.setRequestType(RequestType.Method);
				serviceRequest.setParameters(parameters);
				serviceRequest.setResponseFormat(ResponseFormat.Json);

				serviceResponse = NetworkManager.getInstance().request(serviceRequest);

				wifiReleaseLock();

				if (serviceResponse.responseCode == ResponseCode.Success) {

					String jsonResponse = (String) serviceResponse.data;
					List<Object> freshEntities = ProxibaseService.convertJsonToObjects(jsonResponse, Entity.class, GsonType.ProxibaseService);

					if (freshEntities != null && freshEntities.size() > 0) {

						for (Object obj : freshEntities) {
							Entity freshEntity = (Entity) obj;

							freshEntity.beacon = beacon;
							freshEntity.state = EntityState.Refreshed;
							setEntityVisibility(freshEntity, beacon);
							for (Entity childEntity : freshEntity.children) {
								childEntity.beacon = beacon;
								childEntity.state = EntityState.Refreshed;
								setEntityVisibility(childEntity, beacon);
							}
							beacon.entities.set(beacon.entities.indexOf(entity), freshEntity);
							break;
						}
					}
					else {
						beacon.entities.remove(beacon.entities.indexOf(entity));
						break;
					}
				}
				return serviceResponse;
			}
			else {
				for (Entity entity : beacon.entities) {
					if (entity.children.contains(targetEntity)) {

						Entity childEntity = entity.children.get(entity.children.indexOf(targetEntity));

						if (Aircandi.settings.getBoolean(Preferences.PREF_AUTOSCAN, false)) {
							wifiLockAcquire(WifiManager.WIFI_MODE_FULL);
						}

						Bundle parameters = new Bundle();
						parameters.putInt("entityId", childEntity.id);
						parameters.putBoolean("includeChildren", false);

						ServiceRequest serviceRequest = new ServiceRequest();
						serviceRequest.setUri(ProxiConstants.URL_PROXIBASE_SERVICE + "GetEntity");
						serviceRequest.setRequestType(RequestType.Method);
						serviceRequest.setParameters(parameters);
						serviceRequest.setResponseFormat(ResponseFormat.Json);

						serviceResponse = NetworkManager.getInstance().request(serviceRequest);

						wifiReleaseLock();

						if (serviceResponse.responseCode == ResponseCode.Success) {

							String jsonResponse = (String) serviceResponse.data;
							List<Object> freshEntities = ProxibaseService.convertJsonToObjects(jsonResponse, Entity.class,
									GsonType.ProxibaseService);

							if (freshEntities != null && freshEntities.size() > 0) {

								for (Object obj : freshEntities) {
									Entity freshEntity = (Entity) obj;
									freshEntity.beacon = beacon;
									freshEntity.state = EntityState.Refreshed;
									setEntityVisibility(freshEntity, beacon);
									entity.children.set(entity.children.indexOf(childEntity), freshEntity);
									break;
								}
							}
							else {
								entity.children.remove(entity.children.indexOf(childEntity));
								break;
							}
						}
						return serviceResponse;
					}
				}
			}
		}
		return serviceResponse;
	}

	public ServiceResponse getEntityFromService(int entityId, boolean includeChildren) {
		final ServiceRequest serviceRequest = new ServiceRequest();
		final Bundle parameters = new Bundle();

		if (Aircandi.settings.getBoolean(Preferences.PREF_AUTOSCAN, false)) {
			wifiLockAcquire(WifiManager.WIFI_MODE_FULL);
		}

		parameters.putInt("entityId", entityId);
		parameters.putBoolean("includeChildren", includeChildren);
		serviceRequest.setUri(ProxiConstants.URL_PROXIBASE_SERVICE + "GetEntity");

		serviceRequest.setRequestType(RequestType.Method);
		serviceRequest.setParameters(parameters);
		serviceRequest.setResponseFormat(ResponseFormat.Json);

		ServiceResponse serviceResponse = NetworkManager.getInstance().request(serviceRequest);

		wifiReleaseLock();

		if (serviceResponse.responseCode == ResponseCode.Success) {
			String jsonResponse = (String) serviceResponse.data;
			List<Entity> entities = (List<Entity>) (List<?>) ProxibaseService.convertJsonToObjects(jsonResponse, Entity.class,
								GsonType.ProxibaseService);
			if (entities.size() > 0) {
				Entity entity = entities.get(0);
				serviceResponse.data = entity;
			}
		}
		return serviceResponse;
	}

	private void rebuildEntityList() {

		/* Rebuild the top level entity list when there could have been a top level change */
		mEntities.clear();
		for (Beacon beaconTemp : mBeacons) {
			for (Entity entityTemp : beaconTemp.entities) {
				mEntities.add(entityTemp);
			}
		}
	}

	private void manageEntityVisibility() {

		/* Visibility status effects all entities regardless of whether this is a full or partial update. */
		Logger.d(this, "Managing entity visibility");

		for (Beacon beacon : mBeacons) {
			for (Entity entity : beacon.entities) {
				setEntityVisibility(entity, beacon);
			}
		}

		/* Push hidden setting down to children */
		for (Beacon beacon : mBeacons) {
			for (Entity entity : beacon.entities) {
				for (Entity childEntity : entity.children) {
					childEntity.hidden = entity.hidden;

					/* If child is going to inherit visibility then perform its own personal visibility check. */
					if (!childEntity.hidden) {
						setEntityVisibility(childEntity, beacon);
					}
				}
			}
		}
	}

	private void setEntityVisibility(Entity entity, Beacon beacon) {
		boolean oldIsHidden = entity.hidden;
		entity.hidden = false;
		/*
		 * Make it harder to fade out than it is to fade in.
		 * Entities are only New for the first scan that discovers them.
		 */
		float signalThresholdFluid = entity.signalFence;
		if (oldIsHidden == false && entity.beacon.state != BeaconState.New)
			signalThresholdFluid = entity.signalFence - 5;

		/* Hide entities that are not within entity declared virtual range */
		if (Aircandi.settings.getBoolean(Preferences.PREF_ENTITY_FENCING, true) && beacon.getAvgBeaconLevel() < signalThresholdFluid) {
			entity.hidden = true;
			return;
		}
	}

	// --------------------------------------------------------------------------------------------
	// WIFI routines
	// --------------------------------------------------------------------------------------------

	private void wifiLockAcquire(int lockType) {
		if (mWifiLock != null && mWifiLock.isHeld()) {
			mWifiLock.release();
		}
		mWifiLock = mWifiManager.createWifiLock(lockType, "Proxi");
		mWifiLock.setReferenceCounted(false);
		if (!mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	private void wifiReleaseLock() {
		if (mWifiLock != null && mWifiLock.isHeld()) {
			mWifiLock.release();
		}
	}

	// --------------------------------------------------------------------------------------------
	// MISC routines
	// --------------------------------------------------------------------------------------------

	/**
	 * Call this method when an activity holding a reference to ProxiExplorer is being
	 * paused by the system. This ensures that any ongoing tag scan is cleaned
	 * up properly.
	 */
	public void onPause() {
		try {
			/* Start blocking the processing of any scan messages even if we have an active request */
			mScanRequestActive = false;
		}
		catch (Exception exception) {
			/*
			 * Jayma: For some insane reason, unregisterReceiver always throws an exception
			 * so we catch it and move on.
			 */
		}
	}

	/**
	 * Call this method when an activity holding a reference to ProxiExplorer is being
	 * destroyed by the system. This ensures that any ongoing tag scan is cleaned
	 * up properly and locks are released.
	 */
	public void onDestroy() {
		try {
			/*
			 * We are aggressive about hold our wifi lock so we need to be sure it gets
			 * released when we are destroyed.
			 */
			wifiReleaseLock();
		}
		catch (Exception exception) {
		}
	}

	/**
	 * Call this method when an activity holding a reference to ProxiExplorer is being
	 * resumed by the system. This ensures that the component is re-initialized properly.
	 */
	public void onResume() {
	/*
	 * This is a placeholder for any future work that should be done
	 * when a parent activity is resumed.
	 */
	}

	public Context getContext() {
		return this.mContext;
	}

	public void setContext(Context context) {
		this.mContext = context;
	}

	public List<Beacon> getBeacons() {
		return this.mBeacons;
	}

	public List<Entity> getEntitiesFlat() {
		List<Entity> entitiesFlat = new ArrayList<Entity>();
		for (Beacon proxiBeacon : mBeacons)
			for (Entity entity : proxiBeacon.entities) {
				entitiesFlat.add(entity);
				for (Entity childEntity : entity.children)
					entitiesFlat.add(childEntity);
			}
		return entitiesFlat;
	}

	public Beacon getStrongestBeacon() {
		int strongestLevelDb = -1000;
		Beacon strongestBeacon = null;
		Beacon demoBeacon = null;
		for (Beacon beacon : mBeacons) {
			if (beacon.id.equals(demoBssid)) {
				demoBeacon = beacon;
			}
			else {
				if (!beacon.id.equals(globalBssid) && beacon.levelDb > strongestLevelDb) {
					strongestBeacon = beacon;
					strongestLevelDb = beacon.levelDb;
				}
			}
		}
		if (strongestBeacon == null && demoBeacon != null) {
			strongestBeacon = demoBeacon;
		}

		return strongestBeacon;
	}

	public Beacon getBeaconById(String beaconId) {
		for (Beacon beacon : mBeacons) {
			if (beacon.id.equals(beaconId)) {
				return beacon;
			}
		}
		return null;
	}

	public Entity getEntityById(Integer entityId) {
		if (entityId != null) {
			for (Beacon beacon : mBeacons) {
				for (Entity entity : beacon.entities) {
					if (entity.id.equals(entityId)) {
						return entity;
					}
					for (Entity childEntity : entity.children) {
						if (childEntity.id.equals(entityId)) {
							return childEntity;
						}
					}
				}
			}
		}
		return null;
	}

	public void setPrefDemoMode(boolean prefDemoMode) {

		/* Make sure we clear any demo beacons */
		if (!Aircandi.settings.getBoolean(Preferences.PREF_DEMO_MODE, false)) {
			for (int i = mBeacons.size() - 1; i >= 0; i--) {
				if (mBeacons.get(i).label.equals("demo_dolly")) {
					mBeacons.remove(i);
				}
			}
		}
	}

	public void setPrefGlobalBeacons(boolean prefGlobalBeacons) {

		/* Make sure we clear any global beacons */
		if (!Aircandi.settings.getBoolean(Preferences.PREF_GLOBAL_BEACONS, true)) {
			for (int i = mBeacons.size() - 1; i >= 0; i--) {
				if (mBeacons.get(i).label.equals("candi_feed")) {
					mBeacons.remove(i);
				}
			}
		}
	}

	public void setUsingEmulator(boolean usingEmulator) {
		mUsingEmulator = usingEmulator;
	}

	public boolean isUsingEmulator() {
		return mUsingEmulator;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static class Options {

		public boolean	refreshDirty		= false;
		public boolean	refreshAllBeacons	= true;

		public Options(boolean refreshAllBeacons, boolean refreshDirty) {
			this.refreshDirty = refreshDirty;
			this.refreshAllBeacons = refreshAllBeacons;
		}
	}

	public class WifiScanResult {

		public String	BSSID;
		public String	SSID;
		public int		level	= 0;

		public WifiScanResult(String bssid, String ssid, int level) {
			this.BSSID = bssid;
			this.SSID = ssid;
			this.level = level;
		}

		public WifiScanResult(ScanResult scanResult) {
			this.BSSID = scanResult.BSSID;
			this.SSID = scanResult.SSID;
			this.level = scanResult.level;
		}
	}

	/**
	 * Callback interface for ProxiExplorer beacon scan requests.
	 */
	public static interface IEntityProcessListener {

		/**
		 * Called when a request completes with the given response.
		 * Executed by a background thread: do not update the UI in this method.
		 */
		public void onComplete(List<Entity> proxiEntities);

		/**
		 * Called when the server-side Proxibase method fails.
		 * Executed by a background thread: do not update the UI in this method.
		 */
		public void onProxibaseException(ProxibaseException exception);
	}
}