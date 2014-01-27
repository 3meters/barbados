package com.aircandi.barbados.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.aircandi.barbados.Constants;
import com.aircandi.barbados.R;
import com.aircandi.barbados.controllers.Candigrams;
import com.aircandi.barbados.objects.Candigram;
import com.aircandi.components.StringManager;
import com.aircandi.events.MessageEvent;
import com.aircandi.objects.Link.Direction;
import com.aircandi.objects.Shortcut;
import com.aircandi.objects.ShortcutSettings;
import com.aircandi.utilities.Colors;
import com.aircandi.utilities.UI;
import com.squareup.otto.Subscribe;

public class PlaceForm extends com.aircandi.ui.PlaceForm {

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
		extras.putString(Constants.EXTRA_ENTITY_PARENT_ID, mEntityId);
		extras.putString(Constants.EXTRA_ENTITY_SCHEMA, Constants.SCHEMA_ENTITY_CANDIGRAM);
		super.onAdd(extras);
	}

	@Override
	public void onShortcutClick(View view) {
		final Shortcut shortcut = (Shortcut) view.getTag();
		if (shortcut.action != null
				&& shortcut.action.equals(Constants.ACTION_INSERT)
				&& shortcut.app.equals(Constants.TYPE_APP_CANDIGRAM)) {
			onAdd(new Bundle());
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
		((ViewGroup) findViewById(R.id.holder_shortcuts)).removeAllViews();

		List<Shortcut> shortcuts = new ArrayList<Shortcut>();

		/* Make shortcut for making new candigrams */
		Shortcut shortcutNew = Shortcut.builder(mEntity
				, Constants.SCHEMA_ENTITY_CANDIGRAM
				, Constants.TYPE_APP_CANDIGRAM
				, Constants.ACTION_INSERT
				, StringManager.getString(R.string.label_type_candigrams)
				, "img_candigram_temp"
				, 10
				, false
				, true);

		shortcutNew.photo.colorize = true;
		shortcutNew.photo.color = Colors.getColor(Candigrams.ICON_COLOR);
		shortcutNew.name = "new candigram";

		/* Candigram shortcuts */
		ShortcutSettings settings = new ShortcutSettings(Constants.TYPE_LINK_CONTENT, Constants.SCHEMA_ENTITY_CANDIGRAM, Direction.in, null, false, false);
		settings.appClass = Candigram.class;
		shortcuts = (List<Shortcut>) mEntity.getShortcuts(settings, null, new Shortcut.SortByPositionSortDate());

		if (shortcuts.size() > 0) {
			Collections.sort(shortcuts, new Shortcut.SortByPositionSortDate());
		}

		shortcuts.add(0, shortcutNew);

		if (shortcuts.size() > 0) {
			prepareShortcuts(shortcuts
					, settings
					, R.string.label_link_candigrams
					, R.string.label_link_links_more
					, mResources.getInteger(R.integer.limit_shortcuts_flow)
					, R.id.holder_shortcuts
					, R.layout.widget_shortcut);
		}
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