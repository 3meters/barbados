package com.aircandi.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.aircandi.Aircandi;
import com.aircandi.Constants;
import com.aircandi.applications.Comments;
import com.aircandi.service.objects.AirNotification;
import com.aircandi.ui.AircandiForm;
import com.aircandi.ui.NewsFragment;
import com.aircandi.ui.base.BaseEntityForm;
import com.aircandi.ui.base.BaseFragment;
import com.aircandi.utilities.Json;
import com.aircandi.utilities.Notifications;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	public GCMIntentService() {
		super(Constants.SENDER_ID);
	}

	/*
	 * Note: The methods below run in the intent service's thread and hence are free to make network calls without the
	 * risk of blocking the UI thread.
	 */

	@Override
	protected void onRegistered(Context context, String registrationId) {
		/*
		 * Called after a registration intent is received, passes the registration ID assigned by GCM to that
		 * device/application pair as parameter. Typically, you should send the regid to your server so it can use it to
		 * send messages to this device.
		 */
		Logger.i(this, "GCM: device registration id is: " + registrationId);
	}

	@Override
	protected void onMessage(Context context, Intent messageIntent) {
		/*
		 * Called when our server sends a message to GCM, and GCM delivers it to the device. If the message has a
		 * payload, its contents are available as extras in the intent.
		 */
		String jsonNotification = messageIntent.getStringExtra("notification");
		AirNotification notification = (AirNotification) Json.jsonToObject(jsonNotification, Json.ObjectType.AIR_NOTIFICATION);

		/* We don't self notify unless dev settings are on and self notify is enabled */
		if (!Aircandi.settings.getBoolean(Constants.PREF_ENABLE_DEV, Constants.PREF_ENABLE_DEV_DEFAULT)
				|| !Aircandi.settings.getBoolean(Constants.PREF_TESTING_SELF_NOTIFY, Constants.PREF_TESTING_SELF_NOTIFY_DEFAULT)) {
			if (notification.user != null
					&& Aircandi.getInstance().getCurrentUser() != null
					&& notification.user.id.equals(Aircandi.getInstance().getCurrentUser().id)) {
				return;
			}
		}

		/* Build intent that can be used in association with the notification */
		if (notification.entity != null) {
			if (notification.entity.schema.equals(Constants.SCHEMA_ENTITY_COMMENT)) {
				notification.intent = Comments.viewForGetIntent(context, notification.toEntity.id, Constants.TYPE_LINK_CONTENT, null, null);
			}
			else {
				Class<?> clazz = BaseEntityForm.viewFormBySchema(notification.entity.schema);
				IntentBuilder intentBuilder = new IntentBuilder(context, clazz)
						.setEntityId(notification.entity.id)
						.setEntitySchema(notification.entity.schema)
						.setForceRefresh(true);
				notification.intent = intentBuilder.create();
			}
		}

		/* Customize title and subtitle before storing and broadcasting */
		Notifications.decorate(notification);

		/* Stash in our local database */
		NotificationManager.getInstance().storeNotification(notification, jsonNotification);

		/* Trigger event so subscribers can decide if they should refresh */
		NotificationManager.getInstance().broadcastNotification(notification);

		/* Display if user is not currently using the notifications activity */
		Activity currentActivity = Aircandi.getInstance().getCurrentActivity();
		if (currentActivity != null && currentActivity.getClass().equals(AircandiForm.class)) {
			BaseFragment fragment = ((AircandiForm) currentActivity).getCurrentFragment();
			if (fragment.getClass().equals(NewsFragment.class)) {
				return;
			}
		}

		NotificationManager.getInstance().showNotification(notification, context);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		/*
		 * Called after the device has been unregistered from GCM. Typically, you should send the regid to the server so
		 * it unregisters the device.
		 */
		Logger.i(this, "GCM: Unregistered");
	}

	@Override
	protected void onError(Context context, String errorId) {
		/*
		 * Called when the device tries to register or unregister, but GCM returned an error. Typically, there is
		 * nothing to be done other than evaluating the error (returned by errorId) and trying to fix the problem.
		 */
		Logger.i(this, "GCM: ERROR: " + errorId);
	}
}
