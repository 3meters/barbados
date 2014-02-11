package com.aircandi.barbados.ui;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;

import com.aircandi.Aircandi;
import com.aircandi.barbados.Constants;
import com.aircandi.barbados.R;
import com.aircandi.barbados.controllers.Candigrams;
import com.aircandi.barbados.objects.Candigram;
import com.aircandi.components.Logger;
import com.aircandi.objects.Link.Direction;
import com.aircandi.objects.Place;
import com.aircandi.objects.ServiceBase;
import com.aircandi.objects.Shortcut;
import com.aircandi.objects.ShortcutSettings;
import com.aircandi.objects.User;

@SuppressWarnings("ucd")
public class ShortcutFragment extends com.aircandi.ui.ShortcutFragment {

	// --------------------------------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------------------------

	@Override
	public void draw() {

		Logger.d(this, "Fragment drawing");
		Aircandi.stopwatch3.segmentTime("Fragment draw start");

		if (getView() == null) return;

		if (mEntity == null) return;

		/* Clear shortcut holder */
		((ViewGroup) getView().findViewById(R.id.holder_shortcuts)).removeAllViews();
		mShortcutCount = 0;
		mFlowLayouts.clear();

		if (mShortcutType.equals(Constants.TYPE_LINK_WATCH)) {

			/* Watching places */

			ShortcutSettings settings = new ShortcutSettings(Constants.TYPE_LINK_WATCH, Constants.SCHEMA_ENTITY_PLACE, Direction.out, null, false, false);
			settings.appClass = Place.class;
			List<Shortcut> shortcuts = mEntity.getShortcuts(settings, new ServiceBase.SortByPositionSortDate(), null);
			if (shortcuts.size() > 0) {
				mShortcutCount += shortcuts.size();
				prepareShortcuts(shortcuts
						, settings
						, R.string.label_section_places_watching
						, R.string.label_link_places_more
						, mResources.getInteger(R.integer.limit_shortcuts_flow_watch)
						, R.id.holder_shortcuts
						, R.layout.widget_shortcut);
			}

			/* Watching candigrams */

			settings = new ShortcutSettings(Constants.TYPE_LINK_WATCH, Constants.SCHEMA_ENTITY_CANDIGRAM, Direction.out, null, false, false);
			settings.appClass = Candigram.class;
			shortcuts = mEntity.getShortcuts(settings, new ServiceBase.SortByPositionSortDate(), null);
			if (shortcuts.size() > 0) {
				mShortcutCount += shortcuts.size();
				prepareShortcuts(shortcuts
						, settings
						, R.string.label_section_candigrams_watching
						, R.string.label_link_candigrams_more
						, mResources.getInteger(R.integer.limit_shortcuts_flow_watch)
						, R.id.holder_shortcuts
						, R.layout.widget_shortcut);
			}

			/* Watching users */

			settings = new ShortcutSettings(Constants.TYPE_LINK_WATCH, Constants.SCHEMA_ENTITY_USER, Direction.out, null, false, false);
			settings.appClass = User.class;
			shortcuts = mEntity.getShortcuts(settings, new ServiceBase.SortByPositionSortDate(), null);
			if (shortcuts.size() > 0) {
				mShortcutCount += shortcuts.size();
				prepareShortcuts(shortcuts
						, settings
						, R.string.label_section_users_watching
						, R.string.label_link_users_more
						, mResources.getInteger(R.integer.limit_shortcuts_flow_watch)
						, R.id.holder_shortcuts
						, R.layout.widget_shortcut);
			}
		}
		else if (mShortcutType.equals(Constants.TYPE_LINK_CREATE)) {

			/* Shortcuts for candigram entities created by user */
			
			ShortcutSettings settings = new ShortcutSettings(Constants.TYPE_LINK_CREATE, Constants.SCHEMA_ENTITY_CANDIGRAM, Direction.out, null, false, false);
			settings.appClass = Candigrams.class;
			List<Shortcut> shortcuts = mEntity.getShortcuts(settings, new ServiceBase.SortByPositionSortDate(), null);
			if (shortcuts.size() > 0) {
				mShortcutCount += shortcuts.size();
				prepareShortcuts(shortcuts
						, settings
						, R.string.label_section_candigrams_created
						, R.string.label_link_candigrams_more
						, mResources.getInteger(R.integer.limit_shortcuts_flow_create)
						, R.id.holder_shortcuts
						, R.layout.widget_shortcut);
			}
		}

		Aircandi.stopwatch3.segmentTime("Fragment draw finished");

		if (mScrollView != null) {
			mScrollView.setVisibility(View.VISIBLE);
			mScrollView.post(new Runnable() {
				@Override
				public void run() {
					mScrollView.scrollTo(mScrollX, mScrollY);
				}
			});
		}
	}
}