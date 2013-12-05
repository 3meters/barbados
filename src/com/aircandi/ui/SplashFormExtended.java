package com.aircandi.ui;

import android.content.Intent;
import android.os.Bundle;

import com.aircandi.Constants;
import com.aircandi.service.objects.Candigram;
import com.aircandi.service.objects.Entity;
import com.aircandi.utilities.Routing;
import com.aircandi.utilities.Routing.Route;

public class SplashFormExtended extends SplashForm {

	@Override
	protected void initialize() {
		mEnableCategoryEditing = false;
		super.initialize();
	}

	@Override
	protected void intentRouting() {
		/*
		 * Need to determine which activity to jump to. We screen
		 * the intent heavily to prevent bad/malicious injection.
		 */
		Intent intent = getIntent();
		if (intent == null || intent.getAction() == null) {
			finish();
		}
		else {
			if (intent.getAction().equals("com.aircandi.action.CANDIGRAM_VIEW")) {

				final Bundle extras = intent.getExtras();
				if (extras != null) {
					final String entityId = extras.getString(Constants.EXTRA_ENTITY_ID);
					final String entitySchema = extras.getString(Constants.EXTRA_LIST_LINK_SCHEMA);

					if (entityId != null && entitySchema != null && entitySchema.equals(Constants.SCHEMA_ENTITY_CANDIGRAM)) {
						Entity entity = new Candigram();
						entity.id = entityId;
						entity.schema = entitySchema;
						Routing.route(this, Route.BROWSE, entity, null, null);
					}
				}
				finish();
			}
			else if (intent.getAction().equals("com.aircandi.action.CANDIGRAM_INSERT")) {

				final Bundle extras = intent.getExtras();
				if (extras != null) {
					if (Routing.route(this, Route.NEW, null, Constants.SCHEMA_ENTITY_CANDIGRAM, extras)) {
						finish();
					}
				}
			}
			else {
				super.intentRouting();
			}
		}
	}
}