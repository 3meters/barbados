package com.aircandi.barbados.ui;

import android.content.res.Configuration;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.aircandi.barbados.Constants;
import com.aircandi.barbados.R;
import com.aircandi.components.StringManager;
import com.aircandi.events.MessageEvent;
import com.aircandi.ui.ActivityFragment;
import com.aircandi.ui.RadarFragment;
import com.squareup.otto.Subscribe;

public class AircandiForm extends com.aircandi.ui.AircandiForm {

	// --------------------------------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------------------------------

	@Override
	@Subscribe
	@SuppressWarnings("ucd")
	public void onMessage(final MessageEvent event) {
		super.onMessage(event);
	}

	@Override
	public void onAdd(Bundle extras) {
		extras.putString(Constants.EXTRA_ENTITY_SCHEMA, Constants.SCHEMA_ENTITY_CANDIGRAM);
		super.onAdd(extras);
	}

	// --------------------------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------------------------

	@Override
	protected void addTabs(Configuration config) {
		mActionBar.removeAllTabs();

		addCustomTab(StringManager.getString(R.string.tab_item_radar), Constants.FRAGMENT_TYPE_RADAR, RadarFragment.class, null, false);
		addCustomTab(StringManager.getString(R.string.tab_item_watch), Constants.FRAGMENT_TYPE_WATCH, ShortcutFragment.class, null, false);
		addCustomTab(StringManager.getString(R.string.tab_item_created), Constants.FRAGMENT_TYPE_CREATE, ShortcutFragment.class, null, false);
		addCustomTab(StringManager.getString(R.string.tab_item_activity), Constants.FRAGMENT_TYPE_ACTIVITY, ActivityFragment.class, null, true);

		if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		}
		else {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		}
	}

	// --------------------------------------------------------------------------------------------
	// Menus
	// --------------------------------------------------------------------------------------------	

	// --------------------------------------------------------------------------------------------
	// Lifecycle
	// --------------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------
	// Misc
	// --------------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------
	// Classes
	// --------------------------------------------------------------------------------------------

}