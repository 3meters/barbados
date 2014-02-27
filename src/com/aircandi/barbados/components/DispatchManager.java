package com.aircandi.barbados.components;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.aircandi.Aircandi;
import com.aircandi.Constants;
import com.aircandi.barbados.ui.AircandiForm;
import com.aircandi.barbados.ui.SplashForm;
import com.aircandi.components.IntentBuilder;
import com.aircandi.objects.Entity;
import com.aircandi.objects.Route;
import com.aircandi.objects.Shortcut;
import com.aircandi.objects.TransitionType;
import com.aircandi.ui.base.BaseActivity;

public class DispatchManager extends com.aircandi.components.DispatchManager {

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void route(final Activity activity, Integer route, Entity entity, Shortcut shortcut, Bundle extras) {

		String schema = null;
		if (extras != null) {
			schema = extras.getString(Constants.EXTRA_ENTITY_SCHEMA);
		}

		if (schema == null && entity != null) {
			schema = entity.schema;
		}

		if (route == Route.HOME) {

			final IntentBuilder intentBuilder = new IntentBuilder(activity, AircandiForm.class);
			Intent intent = intentBuilder.create();
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

			activity.startActivity(intent);
			Aircandi.getInstance().getAnimationManager().doOverridePendingTransition(activity, TransitionType.PAGE_TO_HELP);
			return;
		}
		
		else if (route == Route.SPLASH) {

			final IntentBuilder intentBuilder = new IntentBuilder(activity, SplashForm.class);
			final Intent intent = intentBuilder.create();
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			if (Constants.SUPPORTS_HONEYCOMB) {
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			}
			if (activity instanceof BaseActivity) {
				((BaseActivity) activity).setResultCode(Activity.RESULT_CANCELED);
			}
			activity.startActivity(intent);
			activity.finish();
			Aircandi.getInstance().getAnimationManager().doOverridePendingTransition(activity, TransitionType.FORM_TO_PAGE);
			return;
		}

		else {
			super.route(activity, route, entity, shortcut, extras);
		}

		return;
	}
}
