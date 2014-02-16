package com.aircandi.barbados.queries;

import java.util.ArrayList;
import java.util.List;

import com.aircandi.Aircandi;
import com.aircandi.barbados.objects.EventType;
import com.aircandi.components.ProximityManager.ModelResult;
import com.aircandi.objects.Cursor;
import com.aircandi.objects.ServiceData;
import com.aircandi.queries.IQuery;
import com.aircandi.utilities.Maps;

public class ActivityByUserQuery implements IQuery {

	protected Boolean	mMore	= false;
	protected Integer	mPageSize = 30;
	protected String	mEntityId;

	@Override
	public ModelResult execute(Integer skip) {
		/*
		 * - Should be called on a background thread.
		 * - Sorting is applied to links not the entities on the service side.
		 */
		
		Cursor cursor = new Cursor()
				.setLimit(mPageSize)
				.setSort(Maps.asMap("modifiedDate", -1))
				.setSkip(skip);

		List<String> events = new ArrayList<String>();
		events.add(EventType.INSERT_PLACE);
		events.add(EventType.INSERT_CANDIGRAM_TO_PLACE);
		events.add(EventType.INSERT_PICTURE_TO_CANDIGRAM);
		events.add(EventType.INSERT_COMMENT_TO_CANDIGRAM);
		events.add(EventType.MOVE_CANDIGRAM);
		events.add(EventType.RESTART_CANDIGRAM);

		ModelResult result = Aircandi.getInstance().getEntityManager().loadActivities(mEntityId, cursor, events);
		
		if (result.data != null) {
			mMore = ((ServiceData) result.serviceResponse.data).more;
		}
		return result;
	}

	@Override
	public Boolean isMore() {
		return mMore;
	}

	public ActivityByUserQuery setPageSize(Integer pageSize) {
		mPageSize = pageSize;
		return this;
	}

	public ActivityByUserQuery setEntityId(String entityId) {
		mEntityId = entityId;
		return this;
	}
}
