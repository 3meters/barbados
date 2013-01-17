package com.aircandi.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.aircandi.Aircandi;
import com.aircandi.CandiConstants;
import com.aircandi.PlacesConstants;
import com.aircandi.R;
import com.aircandi.components.AircandiCommon.ServiceOperation;
import com.aircandi.components.CommandType;
import com.aircandi.components.Events;
import com.aircandi.components.Events.EventHandler;
import com.aircandi.components.Exceptions;
import com.aircandi.components.IntentBuilder;
import com.aircandi.components.LocationManager;
import com.aircandi.components.Logger;
import com.aircandi.components.NetworkManager;
import com.aircandi.components.NetworkManager.ResponseCode;
import com.aircandi.components.NetworkManager.ServiceResponse;
import com.aircandi.components.ProxiExplorer;
import com.aircandi.components.ProxiExplorer.EntityModel;
import com.aircandi.components.ProxiExplorer.ModelResult;
import com.aircandi.components.RadarListAdapter;
import com.aircandi.components.Tracker;
import com.aircandi.components.bitmaps.BitmapManager;
import com.aircandi.service.ProxibaseService.RequestListener;
import com.aircandi.service.objects.Entity;
import com.aircandi.service.objects.Observation;
import com.aircandi.ui.base.CandiActivity;
import com.aircandi.ui.widgets.BounceListView;
import com.aircandi.utilities.AnimUtils;
import com.aircandi.utilities.AnimUtils.TransitionType;

/*
 * Library Notes
 * 
 * - AWS: We are using the minimum libraries: core and S3. We could do the work to call AWS without their
 * libraries which should give us the biggest savings.
 */

/*
 * Threading Notes
 * 
 * - AsyncTasks: AsyncTask uses a static internal work queue with a hard-coded limit of 10 elements.
 * Once we have 10 tasks going concurrently, task 11 causes a RejectedExecutionException. ThreadPoolExecutor is a way to
 * get more control over thread pooling but it requires Android version 11/3.0 (we currently target 7/2.1 and higher).
 * AsyncTasks are hard-coded with a low priority and continue their work even if the activity is paused.
 */

/*
 * Bitmap Management
 * 
 * gc calls are evil but necessary sometimes. It forces code exection to stop while
 * the gc makes an explicit garbage pass. Behavior may be a bit different with
 * the introduction of concurrent gc in Gingerbread (v2.3)
 * 
 * Explicit gc calls to free bitmap memory:
 * 
 * - EntityForm: onDestroy.
 * - PictureSearch: onDestroy.
 * - ProfileForm: onDestroy.
 * - SignUpForm: onDestroy.
 * 
 * Explicit bitmap recycling
 * 
 * - Anyplace where a new bitmap has been processed from another bitmap.
 * - Releasing bitmaps when forms are destroyed.
 * - Releasing bitmaps when list items are reused.
 */

/*
 * Lifecycle event sequences from Radar
 * 
 * First Launch: onCreate->onStart->onResume
 * Home: Pause->Stop->||Restart->Start->Resume
 * Back: Pause->Stop->Destroyed
 * Other Candi Activity: Pause->Stop||Restart->Start->Resume
 * 
 * Alert Dialog: None
 * Dialog Activity: Pause||Resume
 * Overflow menu: None
 * ProgressIndicator: None
 * 
 * Preferences: Pause->Stop->||Restart->Start->Resume
 * Profile: Pause->Stop->||Restart->Start->Resume
 * 
 * Power off with Aircandi in foreground: Pause->Stop
 * Power on with Aircandi in foreground: Nothing
 * Unlock screen with Aircandi in foreground: Restart->Start->Resume
 */

/*
 * Scan management
 * 
 * There are three cases that trigger scans:
 * 
 * - First run scan: When application is first started, we load the entity model with a full scan. The
 * entity model lives on even if the radar activity is killed.
 * 
 * - User requested scan: (doRefresh) This can be either full or standard.
 * 
 * - Autoscan: Causes another scan to be scheduled as soon as a scan is finished. We also need
 * to handle suspending autoscan when the activity is paused and restarting when resumed.
 * ---Starting: BeaconScanWatcher, onWindowFocusChange
 * ---Stopping: onStop, scanForBeacons
 * 
 * - Fixup scan: These are done because a settings change requires that the UI is rebuilt.
 */

public class CandiRadar extends CandiActivity {

	private Handler				mHandler		= new Handler();

	private Number				mEntityModelRefreshDate;
	private Number				mEntityModelActivityDate;
	private Location			mActiveLocation	= null;

	private BounceListView		mList;

	private SoundPool			mSoundPool;
	private int					mNewCandiSoundId;
	private Boolean				mInitialized	= false;
	private EventHandler		mEventWifiScanReceived;
	private EventHandler		mEventBeaconsLocked;
	private EventHandler		mEventLocationChanged;
	private EventHandler		mEventBeaconEntitiesLoaded;
	private EventHandler		mEventLocationEntitiesLoaded;
	private EventHandler		mEventSyntheticsLoaded;

	private List<Entity>		mEntities		= new ArrayList<Entity>();
	private RadarListAdapter	mRadarAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			/*
			 * Get setup for location snapshots. Initialize will populate location
			 * with the best of any cached location fixes. A single update will
			 * be launched if the best cached location fix doesn't meet our freshness
			 * and accuracy requirements.
			 */
			LocationManager.getInstance().initialize(getApplicationContext());

			if (!LocationManager.getInstance().isLocationAccessEnabled()) {
				/* We won't continue if location services are disabled */
				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				AnimUtils.doOverridePendingTransition(CandiRadar.this, TransitionType.CandiPageToForm);
				finish();
			}

			/* We alert that wifi isn't enabled */
			if (NetworkManager.getInstance().isWifiTethered()
					|| (!NetworkManager.getInstance().isWifiEnabled() && !Aircandi.usingEmulator)) {

				showWifiAlertDialog(NetworkManager.getInstance().isWifiTethered()
						? R.string.alert_wifi_tethered
						: R.string.alert_wifi_disabled
						, new RequestListener() {

							@Override
							public void onComplete() {
								initialize();
							}
						});
			}
			else {
				initialize();
			}
		}
	}

	private void initialize() {
		/*
		 * Here we initialize activity level state. Only called from
		 * onCreate.
		 */

		/* Save that we've been run once. */
		Aircandi.settingsEditor.putBoolean(PlacesConstants.SP_KEY_RUN_ONCE, true);
		Aircandi.settingsEditor.commit();

		/* Always reset the entity cache */
		ProxiExplorer.getInstance().getEntityModel().removeAllEntities();

		/* Initialize preferences */
		updatePreferences(true);

		/* Location support */

		mEventWifiScanReceived = new EventHandler() {

			@Override
			public void onEvent(Object data) {
				synchronized (Events.EventBus.wifiScanReceived) {
					Events.EventBus.wifiScanReceived.remove(this);
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						ProxiExplorer.getInstance().lockBeacons();
					}
				});
			}
		};

		mEventBeaconsLocked = new EventHandler() {

			@Override
			public void onEvent(Object data) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						new AsyncTask() {

							@Override
							protected void onPreExecute() {
								mCommon.showBusy();
							}

							@Override
							protected Object doInBackground(Object... params) {
								Thread.currentThread().setName("GetEntitiesForBeacons");
								ProxiExplorer.getInstance().getEntitiesForBeacons();
								return null;
							}

							@Override
							protected void onPostExecute(Object result) {
								mCommon.hideBusy();
							}

						}.execute();
					}
				});
			}
		};

		mEventLocationChanged = new EventHandler() {

			@Override
			public void onEvent(final Object data) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						Location location = (Location) data;
						if (location == null) {
							mCommon.showBusy();
						}
						else {
							Boolean hasMoved = LocationManager.hasMoved(location, mActiveLocation);
							if (mActiveLocation == null || hasMoved) {

								Logger.d(CandiRadar.this, "Location change: updating synthetics");
								mActiveLocation = location;

								final Observation observation = LocationManager.getInstance().getObservation();
								if (observation != null) {
									new AsyncTask() {

										@Override
										protected void onPreExecute() {
											mCommon.showBusy();
										}

										@Override
										protected Object doInBackground(Object... params) {
											Thread.currentThread().setName("GetEntitiesForLocation");
											ProxiExplorer.getInstance().getEntitiesForLocation();
											return null;
										}

										@Override
										protected void onPostExecute(Object result) {
											mCommon.hideBusy();
										}
									}.execute();
								}
							}
						}
					}
				});
			}
		};

		mEventBeaconEntitiesLoaded = new EventHandler() {

			@Override
			public void onEvent(final Object data) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						ServiceResponse serviceResponse = (ServiceResponse) data;
						if (serviceResponse.responseCode == ResponseCode.Success) {
							/* Start looking for entities by location now that beacon entities are finished */
							LocationManager.getInstance().lockLocationBurst();

							mEntityModelRefreshDate = ProxiExplorer.getInstance().getEntityModel().getLastRefreshDate();
							mEntityModelActivityDate = ProxiExplorer.getInstance().getEntityModel().getLastActivityDate();
							Entity firstLast = null;
							if (mRadarAdapter.getCount() > 0) {
								firstLast = mRadarAdapter.getItem(0);
							}
							mRadarAdapter.clear();
							List<Entity> entities = ProxiExplorer.getInstance().getEntityModel().getPlaces();
							mRadarAdapter.addAll(entities);
							mRadarAdapter.notifyDataSetChanged();
							/* Check for rookies and add some sparkle */
							Entity firstNext = null;
							if (mRadarAdapter.getCount() > 0) {
								firstNext = mRadarAdapter.getItem(0);
							}
							if ((firstLast == null && firstNext != null) || (firstLast != null && firstNext != null && !firstLast.id.equals(firstNext.id))) {
								scrollToTop();
								if (mPrefSoundEffects) {
									mSoundPool.play(mNewCandiSoundId, 0.2f, 0.2f, 1, 0, 1f);
								}
							}
							if (entities.size() > 0) {
								mCommon.hideBusy();
							}
						}
						else {
							mCommon.handleServiceError(serviceResponse, ServiceOperation.BeaconScan, CandiRadar.this);
						}
					}
				});
			}
		};

		mEventLocationEntitiesLoaded = new EventHandler() {

			@Override
			public void onEvent(final Object data) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						ServiceResponse serviceResponse = (ServiceResponse) data;
						if (serviceResponse.responseCode == ResponseCode.Success) {

							final Observation observation = LocationManager.getInstance().getObservation();
							if (observation != null) {
								new AsyncTask() {

									@Override
									protected Object doInBackground(Object... params) {
										Thread.currentThread().setName("GetPlacesNearLocation");
										ProxiExplorer.getInstance().getPlacesNearLocation(observation);
										return null;
									}

									@Override
									protected void onPostExecute(Object result) {
										mCommon.hideBusy();
									}

								}.execute();
							}

							mEntityModelRefreshDate = ProxiExplorer.getInstance().getEntityModel().getLastRefreshDate();
							mEntityModelActivityDate = ProxiExplorer.getInstance().getEntityModel().getLastActivityDate();
							mRadarAdapter.clear();
							List<Entity> entities = ProxiExplorer.getInstance().getEntityModel().getPlaces();
							mRadarAdapter.addAll(entities);
							mRadarAdapter.notifyDataSetChanged();
							if (entities.size() > 0) {
								mCommon.hideBusy();
							}
						}
						else {
							mCommon.handleServiceError(serviceResponse, ServiceOperation.BeaconScan, CandiRadar.this);
						}
					}
				});
			}
		};

		mEventSyntheticsLoaded = new EventHandler() {

			@Override
			public void onEvent(final Object data) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						ServiceResponse serviceResponse = (ServiceResponse) data;
						if (serviceResponse.responseCode == ResponseCode.Success) {
							mRadarAdapter.clear();
							List<Entity> entities = ProxiExplorer.getInstance().getEntityModel().getPlaces();
							mRadarAdapter.addAll(entities);
							mRadarAdapter.notifyDataSetChanged();
							if (entities.size() > 0) {
								mCommon.hideBusy();
							}
						}
						else {
							mCommon.handleServiceError(serviceResponse, ServiceOperation.BeaconScan, CandiRadar.this);
						}
					}
				});
			}
		};

		/* Other UI references */
		mList = (BounceListView) findViewById(R.id.radar_list);
		mList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Entity entity = mEntities.get(position);
				showCandiForm(entity, entity.synthetic);
			}
		});
		mRadarAdapter = new RadarListAdapter(this, mEntities);
		mList.setAdapter(mRadarAdapter);

		/* Store sounds */
		mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		mNewCandiSoundId = mSoundPool.load(this, R.raw.notification_candi_discovered, 1);

		mInitialized = true;
	}

	// --------------------------------------------------------------------------------------------
	// Entity routines
	// --------------------------------------------------------------------------------------------

	public void doRefresh() {
		Logger.d(this, "Starting refresh");
		Tracker.trackEvent("Radar", "Refresh", "Full", 0);
		searchForPlaces();
	}

	private void searchForPlaces() {
		/* We won't perform a search if location access is disabled */
		if (!LocationManager.getInstance().isLocationAccessEnabled()) {
			startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			AnimUtils.doOverridePendingTransition(CandiRadar.this, TransitionType.CandiPageToForm);
		}
		else {
			/* We remove this when we get the scan results */
			synchronized (Events.EventBus.wifiScanReceived) {
				Events.EventBus.wifiScanReceived.add(mEventWifiScanReceived);
			}
			//ProxiExplorer.getInstance().getEntityModel().removeAllEntities();
			mActiveLocation = null;
			ProxiExplorer.getInstance().scanForWifi();
		}
	}

	// --------------------------------------------------------------------------------------------
	// UI routines
	// --------------------------------------------------------------------------------------------

	private void showCandiForm(Entity entity, Boolean upsize) {

		IntentBuilder intentBuilder = new IntentBuilder(this, CandiForm.class)
				.setCommandType(CommandType.View)
				.setEntityId(entity.id)
				.setParentEntityId(entity.parentId)
				.setEntityType(entity.type);

		if (entity.parentId != null) {
			intentBuilder.setCollectionId(entity.getParent().id);
		}

		Intent intent = intentBuilder.create();
		if (upsize) {
			intent.putExtra(CandiConstants.EXTRA_UPSIZE_SYNTHETIC, true);
		}

		startActivity(intent);
		AnimUtils.doOverridePendingTransition(this, TransitionType.CandiRadarToCandiForm);
	}

	private void scrollToTop() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mList.setSelection(0);
			}
		});
	}

	private Boolean handleUpdateChecks(final RequestListener listener) {

		/* Update check */
		Boolean updateCheckNeeded = ProxiExplorer.getInstance().updateCheckNeeded();
		if (updateCheckNeeded) {

			new AsyncTask() {

				@Override
				protected Object doInBackground(Object... params) {
					Thread.currentThread().setName("CheckForUpdate");
					ModelResult result = ProxiExplorer.getInstance().checkForUpdate();
					return result;
				}

				@Override
				protected void onPostExecute(Object response) {
					ModelResult result = (ModelResult) response;
					if (result.serviceResponse.responseCode == ResponseCode.Success) {
						if (Aircandi.applicationUpdateNeeded) {
							invalidateOptionsMenu();
							showUpdateAlert(listener);
						}
						else {
							listener.onComplete(false);
						}
					}
					else {
						mCommon.handleServiceError(result.serviceResponse, ServiceOperation.CheckUpdate, CandiRadar.this);
					}
				}
			}.execute();
		}
		return updateCheckNeeded;
	}

	// --------------------------------------------------------------------------------------------
	// Location routines
	// --------------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------
	// System callbacks
	// --------------------------------------------------------------------------------------------

	@Override
	protected void onStart() {
		/*
		 * Called everytime the activity is started or restarted.
		 */
		Logger.d(this, "CandiRadarActivity starting");
		super.onStart();
		if (!mInitialized) return;

		/* Check for location service */
		if (!LocationManager.getInstance().isLocationAccessEnabled()) {
			/* We won't continue if location services are disabled */
			startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
			AnimUtils.doOverridePendingTransition(CandiRadar.this, TransitionType.CandiPageToForm);
			finish();
		}

		if (CandiConstants.DEBUG_TRACE) {
			Debug.startMethodTracing("aircandi", 100000000);
		}

		/* Make sure the right tab is active */
		mCommon.setActiveTab(0);

		/* Start listening for events */
		enableEvents();
	}

	@Override
	protected void onStop() {
		/*
		 * Fired when starting another activity and we lose our window.
		 */
		/* Start listening for events */
		disableEvents();

		Logger.d(this, "CandiRadarActivity stopped");
		if (CandiConstants.DEBUG_TRACE) {
			Debug.stopMethodTracing();
		}

		super.onStop();
	}

	@Override
	protected void onPause() {
		/*
		 * - Fires when we lose focus and have been moved into the background. This will
		 * be followed by onStop if we are not visible. Does not fire if the activity window
		 * loses focus but the activity is still active.
		 */

		Logger.d(this, "CandiRadarActivity paused");

		mCommon.stopScanService();
		super.onPause();
	}

	@Override
	protected void onResume() {
		/*
		 * Lifecycle ordering: (onCreate/onRestart)->onStart->onResume->onAttachedToWindow->onWindowFocusChanged
		 * 
		 * OnResume gets called after OnCreate (always) and whenever the activity is being brought back to the
		 * foreground. Not guaranteed but is usually called just before the activity receives focus.
		 */
		super.onResume();
		if (!mInitialized) return;

		mCommon.startScanService(CandiConstants.INTERVAL_SCAN_RADAR);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		if (!mInitialized) return;

		if (hasFocus) {
			if (Aircandi.applicationUpdateRequired) {
				showUpdateAlert(null);
			}
			else {
				/* Check for update */
				Boolean updateCheckNeeded = handleUpdateChecks(new RequestListener() {
					@Override
					public void onComplete(Object dialogDisplayed) {
						/*
						 * We don't do anything right now because window focus returning
						 * when dismissing the update dialog will restart the logic
						 * to trigger data updates.
						 */
						if (!(Boolean) dialogDisplayed) {
							manageData();
						}
					}
				});

				if (!updateCheckNeeded) {
					manageData();
				}
			}
		}
	}

	public void manageData() {

		EntityModel entityModel = ProxiExplorer.getInstance().getEntityModel();
		if (mEntityModelRefreshDate == null) {
			Logger.d(this, "Start first place search");
			searchForPlaces();
		}
		else if (mPrefChangeRefreshNeeded) {
			Logger.d(this, "Start place search because of preference change");
			mPrefChangeRefreshNeeded = false;
			searchForPlaces();
		}
		else if ((entityModel.getLastRefreshDate() != null
				&& entityModel.getLastRefreshDate().longValue() > mEntityModelRefreshDate.longValue())
				|| (entityModel.getLastActivityDate() != null
				&& entityModel.getLastActivityDate().longValue() > mEntityModelActivityDate.longValue())) {
			/*
			 * Everytime we show details for a place, we fetch place details from the service
			 * when in turn get pushed into the cache and activityDate gets tickled.
			 */
			Logger.d(this, "Update radar ui because of detected entity model change");
			mHandler.postDelayed(new Runnable() {

				@Override
				public void run() {
					mCommon.showBusy();
					invalidateOptionsMenu();
					mRadarAdapter.clear();
					mRadarAdapter.addAll(ProxiExplorer.getInstance().getEntityModel().getPlaces());
					mRadarAdapter.notifyDataSetChanged();
					mCommon.hideBusy();
				}
			}, 100);
		}
	}

	@Override
	protected void onDestroy() {
		/*
		 * The activity is getting destroyed but the application level state
		 * like singletons, statics, etc will continue as long as the application
		 * is running.
		 */
		Logger.d(this, "CandiRadarActivity destroyed");
		super.onDestroy();

		/* This is the only place we manually stop the analytics session. */
		Tracker.stopSession();

		/* Don't count on this always getting called when this activity is killed */
		try {
			BitmapManager.getInstance().stopBitmapLoaderThread();
		}
		catch (Exception exception) {
			Exceptions.Handle(exception);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	// --------------------------------------------------------------------------------------------
	// Application menu routines (settings)
	// --------------------------------------------------------------------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mCommon.doCreateOptionsMenu(menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		mCommon.doPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		mCommon.doOptionsItemSelected(menuItem);
		return true;
	}

	// --------------------------------------------------------------------------------------------
	// Misc routines
	// --------------------------------------------------------------------------------------------

	public void enableEvents() {
		synchronized (Events.EventBus.beaconsLocked) {
			Events.EventBus.beaconsLocked.add(mEventBeaconsLocked);
		}
		synchronized (Events.EventBus.locationChanged) {
			Events.EventBus.locationChanged.add(mEventLocationChanged);
		}
		synchronized (Events.EventBus.beaconEntitiesLoaded) {
			Events.EventBus.beaconEntitiesLoaded.add(mEventBeaconEntitiesLoaded);
		}
		synchronized (Events.EventBus.locationEntitiesLoaded) {
			Events.EventBus.locationEntitiesLoaded.add(mEventLocationEntitiesLoaded);
		}
		synchronized (Events.EventBus.syntheticsLoaded) {
			Events.EventBus.syntheticsLoaded.add(mEventSyntheticsLoaded);
		}
	}

	public void disableEvents() {
		synchronized (Events.EventBus.beaconsLocked) {
			Events.EventBus.beaconsLocked.remove(mEventBeaconsLocked);
		}
		synchronized (Events.EventBus.locationChanged) {
			Events.EventBus.locationChanged.remove(mEventLocationChanged);
		}
		synchronized (Events.EventBus.beaconEntitiesLoaded) {
			Events.EventBus.beaconEntitiesLoaded.remove(mEventBeaconEntitiesLoaded);
		}
		synchronized (Events.EventBus.locationEntitiesLoaded) {
			Events.EventBus.locationEntitiesLoaded.remove(mEventLocationEntitiesLoaded);
		}
		synchronized (Events.EventBus.syntheticsLoaded) {
			Events.EventBus.syntheticsLoaded.remove(mEventSyntheticsLoaded);
		}
	}

	@SuppressWarnings("unused")
	private String getGoogleAnalyticsId() {
		Properties properties = new Properties();

		try {
			properties.load(getClass().getResourceAsStream("/com/aircandi/google_analytics.properties"));
			String analyticsId = properties.getProperty("analyticsId");
			return analyticsId;
		}
		catch (IOException exception) {
			throw new IllegalStateException("Unable to retrieve google analytics id");
		}
	}

	@Override
	protected int getLayoutId() {
		return R.layout.candi_radar;
	}

	public enum PlaceType {
		Tuned, Synthetic
	}
}