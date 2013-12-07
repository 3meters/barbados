package com.aircandi.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.aircandi.Constants;
import com.aircandi.applications.Candigrams;
import com.aircandi.barbados.R;
import com.aircandi.components.EntityManager;
import com.aircandi.service.objects.Link.Direction;
import com.aircandi.service.objects.Shortcut;
import com.aircandi.service.objects.ShortcutSettings;
import com.aircandi.utilities.Colors;
import com.aircandi.utilities.Dialogs;
import com.aircandi.utilities.Routing;
import com.aircandi.utilities.Routing.Route;
import com.aircandi.utilities.Strings;
import com.aircandi.utilities.UI;

public class PlaceFormExtended extends PlaceForm {

	// --------------------------------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------------------------------

	@Override
	public void onAdd() {
		if (EntityManager.canUserAdd(mEntity)) {
			Bundle extras = new Bundle();
			extras.putString(Constants.EXTRA_ENTITY_PARENT_ID, mEntityId);
			Routing.route(this, Route.NEW, null, Constants.SCHEMA_ENTITY_CANDIGRAM, extras);
			return;
		}

		if (mEntity.locked) {
			Dialogs.locked(this, mEntity);
		}
	}

	@Override
	public void onShortcutClick(View view) {
		final Shortcut shortcut = (Shortcut) view.getTag();
		if (shortcut.action != null && shortcut.action.equals(Constants.ACTION_INSERT) && shortcut.app.equals(Constants.TYPE_APP_CANDIGRAM)) {
			onAdd();
		}
		else {
			super.onShortcutClick(view);
		}
	}

	// --------------------------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------------------------

	@Override
	protected void drawBody() {
		UI.setVisibility(findViewById(R.id.section_description), View.GONE);
		UI.setVisibility(findViewById(R.id.candi_form_address), View.GONE);
	}

	@Override
	protected void drawShortcuts() {

		/* Clear shortcut holder */
		((ViewGroup) findViewById(R.id.shortcut_holder)).removeAllViews();

		List<Shortcut> shortcuts = new ArrayList<Shortcut>();

		/* Make shortcut for making new candigrams */
		Shortcut shortcutNew = Shortcut.builder(mEntity
				, Constants.SCHEMA_ENTITY_CANDIGRAM
				, Constants.TYPE_APP_CANDIGRAM
				, Constants.ACTION_INSERT
				, Strings.getString(R.string.applink_name_candigrams)
				, "img_candigram_temp"
				, 10
				, false
				, true);

		shortcutNew.photo.colorize = true;
		shortcutNew.photo.color = Colors.getColor(Candigrams.ICON_COLOR);
		shortcutNew.name = "new candigram";

		/* Candigram shortcuts */
		ShortcutSettings settings = new ShortcutSettings(Constants.TYPE_LINK_CONTENT, Constants.SCHEMA_ENTITY_CANDIGRAM, Direction.in, null, false, false);
		settings.appClass = Candigrams.class;
		shortcuts = (List<Shortcut>) mEntity.getShortcuts(settings, null, new Shortcut.SortByPositionSortDate());
		if (shortcuts.size() > 0) {
			Collections.sort(shortcuts, new Shortcut.SortByPositionSortDate());
		}

		shortcuts.add(0, shortcutNew);

		if (shortcuts.size() > 0) {
			prepareShortcuts(shortcuts
					, settings
					, R.string.section_place_shortcuts_candigrams
					, R.string.section_links_more
					, mResources.getInteger(R.integer.limit_shortcuts_flow)
					, R.id.shortcut_holder
					, R.layout.temp_place_switchboard_item);
		}
	}

	@Override
	protected void drawUsers() {
		UI.setVisibility(findViewById(R.id.user_one), View.GONE);
		UI.setVisibility(findViewById(R.id.user_two), View.GONE);
	}

	// --------------------------------------------------------------------------------------------
	// Lifecycle
	// --------------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------
	// Menus
	// --------------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------
	// Misc
	// --------------------------------------------------------------------------------------------

}