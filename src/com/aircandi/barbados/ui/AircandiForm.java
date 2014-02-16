package com.aircandi.barbados.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.aircandi.Aircandi;
import com.aircandi.barbados.Barbados;
import com.aircandi.barbados.Constants;
import com.aircandi.barbados.R;
import com.aircandi.barbados.queries.ActivityByAffinityQuery;
import com.aircandi.barbados.queries.ActivityByUserQuery;
import com.aircandi.components.FontManager;
import com.aircandi.components.StringManager;
import com.aircandi.events.MessageEvent;
import com.aircandi.monitors.CurrentUserMonitor;
import com.aircandi.objects.Route;
import com.aircandi.queries.ShortcutsQuery;
import com.aircandi.ui.ActivityFragment;
import com.aircandi.ui.RadarFragment;
import com.aircandi.ui.base.BaseFragment;
import com.aircandi.utilities.Integers;
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
		if (!extras.containsKey(Constants.EXTRA_ENTITY_SCHEMA)) {
			extras.putString(Constants.EXTRA_ENTITY_SCHEMA, Constants.SCHEMA_ENTITY_CANDIGRAM);
		}
		Barbados.dispatch.route(this, Route.NEW, null, null, extras);
	}

	// --------------------------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------------------------

	@Override
	public void setCurrentFragment(String fragmentType, View view) {
		/*
		 * Fragment menu items are in addition to any menu items added by the parent activity.
		 */
		BaseFragment fragment = null;

		FontManager.getInstance().setTypefaceLight((TextView) findViewById(R.id.item_radar).findViewById(R.id.name));
		FontManager.getInstance().setTypefaceLight((TextView) findViewById(R.id.item_activity).findViewById(R.id.name));
		FontManager.getInstance().setTypefaceLight((TextView) findViewById(R.id.item_my_activity).findViewById(R.id.name));
		FontManager.getInstance().setTypefaceLight((TextView) findViewById(R.id.item_watch).findViewById(R.id.name));
		FontManager.getInstance().setTypefaceLight((TextView) findViewById(R.id.item_create).findViewById(R.id.name));
		FontManager.getInstance().setTypefaceMedium((TextView) view.findViewById(R.id.name));

		if (mFragments.containsKey(fragmentType)) {
			fragment = mFragments.get(fragmentType);
		}
		else {

			if (fragmentType.equals(Constants.FRAGMENT_TYPE_RADAR)) {

				fragment = new RadarFragment()
						.setTitleResId(R.string.label_radar_title);

				((BaseFragment) fragment).getMenuResIds().add(R.menu.menu_beacons);
				((BaseFragment) fragment).getMenuResIds().add(R.menu.menu_refresh_special);
				((BaseFragment) fragment).getMenuResIds().add(R.menu.menu_new_place);
				((BaseFragment) fragment).getMenuResIds().add(R.menu.menu_help);
			}

			else if (fragmentType.equals(Constants.FRAGMENT_TYPE_ACTIVITY)) {

				fragment = new ActivityFragment();
				CurrentUserMonitor monitor = new CurrentUserMonitor();
				ActivityByAffinityQuery query = new ActivityByAffinityQuery()
						.setEntityId(Aircandi.getInstance().getCurrentUser().id)
						.setPageSize(Integers.getInteger(R.integer.page_size_activities));

				((ActivityFragment) fragment)
						.setMonitor(monitor)
						.setQuery(query)
						.setActivityStream(true)
						.setSelfBindingEnabled(true)
						.setTitleResId(R.string.label_activity_title);

				((BaseFragment) fragment).getMenuResIds().add(R.menu.menu_refresh);
			}

			else if (fragmentType.equals(Constants.FRAGMENT_TYPE_MY_ACTIVITY)) {

				fragment = new ActivityFragment();
				CurrentUserMonitor monitor = new CurrentUserMonitor();
				ActivityByUserQuery query = new ActivityByUserQuery()
						.setEntityId(Aircandi.getInstance().getCurrentUser().id)
						.setPageSize(Integers.getInteger(R.integer.page_size_activities));

				((ActivityFragment) fragment)
						.setMonitor(monitor)
						.setQuery(query)
						.setActivityStream(true)
						.setSelfBindingEnabled(true)
						.setTitleResId(R.string.label_my_activity_title);

				((BaseFragment) fragment).getMenuResIds().add(R.menu.menu_refresh);
			}

			else if (fragmentType.equals(Constants.FRAGMENT_TYPE_WATCH)) {

				fragment = new ShortcutFragment();
				CurrentUserMonitor monitor = new CurrentUserMonitor();
				ShortcutsQuery query = new ShortcutsQuery().setEntityId(Aircandi.getInstance().getCurrentUser().id);

				((ShortcutFragment) fragment)
						.setQuery(query)
						.setMonitor(monitor)
						.setShortcutType(Constants.TYPE_LINK_WATCH)
						.setEmptyMessageResId(R.string.label_watching_empty)
						.setSelfBindingEnabled(true)
						.setTitleResId(R.string.label_watch_title);

				((BaseFragment) fragment).getMenuResIds().add(R.menu.menu_refresh);
			}

			else if (fragmentType.equals(Constants.FRAGMENT_TYPE_CREATE)) {

				fragment = new ShortcutFragment();
				CurrentUserMonitor monitor = new CurrentUserMonitor();
				ShortcutsQuery query = new ShortcutsQuery().setEntityId(Aircandi.getInstance().getCurrentUser().id);

				((ShortcutFragment) fragment)
						.setQuery(query)
						.setMonitor(monitor)
						.setShortcutType(Constants.TYPE_LINK_CREATE)
						.setEmptyMessageResId(R.string.label_created_empty)
						.setSelfBindingEnabled(true)
						.setTitleResId(R.string.label_create_title);

				((BaseFragment) fragment).getMenuResIds().add(R.menu.menu_refresh);
			}

			mFragments.put(fragmentType, fragment);
		}

		mDrawerTitle = StringManager.getString(fragment.getTitleResId());
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.fragment_holder, fragment);
		ft.commit();
		mCurrentFragment = (BaseFragment) fragment;
		mCurrentFragmentTag = fragmentType;
	}

	// --------------------------------------------------------------------------------------------
	// Menus
	// --------------------------------------------------------------------------------------------
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		if (mDrawerLayout != null) {
			Boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawer);

			MenuItem menuItemAdd = menu.findItem(R.id.add);
			if (menuItemAdd != null) {
				menuItemAdd.setVisible(!(drawerOpen));
			}
		}

		return true;
	}

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