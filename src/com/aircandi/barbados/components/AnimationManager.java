package com.aircandi.barbados.components;

import android.app.Activity;

import com.aircandi.R;
import com.aircandi.barbados.objects.TransitionType;

public class AnimationManager extends com.aircandi.components.AnimationManager {

	@Override
	@SuppressWarnings("ucd")
	public void doOverridePendingTransition(Activity activity, Integer transitionType) {
		/*
		 * Default android animations are used unless overridden here.
		 */
		if (transitionType == TransitionType.CANDIGRAM_OUT) {
			activity.overridePendingTransition(R.anim.fade_in_medium, R.anim.slide_out_right);
		}
		else {
			super.doOverridePendingTransition(activity, transitionType);
		}

	}
}
