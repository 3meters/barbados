package com.aircandi.ui;

import java.util.ArrayList;
import java.util.List;

import com.aircandi.Aircandi;
import com.aircandi.Constants;
import com.aircandi.components.EntityManager;
import com.aircandi.components.ProximityManager.ModelResult;
import com.aircandi.events.MessageEvent;
import com.aircandi.service.objects.Action.EventType;
import com.aircandi.service.objects.Cursor;
import com.aircandi.utilities.Maps;
import com.squareup.otto.Subscribe;

public class ActivityFragmentExtended extends ActivityFragment {

	// --------------------------------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------------------------------
	
	@Override
	@Subscribe
	@SuppressWarnings("ucd")
	public void onMessage(final MessageEvent event) {
		/*
		 * Must pass this through because event bus doesn't
		 * find the super class.
		 */
		super.onMessage(event);
	}
	
	// --------------------------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------------------------

	@Override
	protected ModelResult loadActivities(Integer skip) {
		/*
		 * Called on a background thread.
		 * 
		 * Sorting is applied to links not the entities on the service side.
		 */
		List<String> schemas = new ArrayList<String>();
		schemas.add(Constants.SCHEMA_ENTITY_PLACE);
		schemas.add(Constants.SCHEMA_ENTITY_CANDIGRAM);
		schemas.add(Constants.SCHEMA_ENTITY_PICTURE);
		schemas.add(Constants.SCHEMA_ENTITY_USER);

		List<String> linkTypes = new ArrayList<String>();
		linkTypes.add(Constants.TYPE_LINK_CREATE);
		linkTypes.add(Constants.TYPE_LINK_WATCH);

		mCursor = new Cursor()
				.setLimit(PAGE_SIZE_DEFAULT)
				.setSort(Maps.asMap("modifiedDate", -1))
				.setSkip(skip)
				.setSchemas(schemas)
				.setLinkTypes(linkTypes);

		List<String> events = new ArrayList<String>();
		events.add(EventType.INSERT_PLACE);
		events.add(EventType.INSERT_CANDIGRAM_TO_PLACE);
		events.add(EventType.INSERT_PICTURE_TO_CANDIGRAM);
		events.add(EventType.INSERT_COMMENT_TO_CANDIGRAM);
		events.add(EventType.MOVE_CANDIGRAM);
		events.add(EventType.FORWARD_CANDIGRAM);
		events.add(EventType.RESTART_CANDIGRAM);

		ModelResult result = EntityManager.getInstance().loadActivities(Aircandi.getInstance().getCurrentUser().id, mCursor, events);

		return result;
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