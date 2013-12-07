package com.aircandi.barbados.ui.components;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.aircandi.Constants;
import com.aircandi.R;
import com.aircandi.components.RenderDelegate;
import com.aircandi.service.objects.Action.EventType;
import com.aircandi.service.objects.Count;
import com.aircandi.service.objects.Link.Direction;
import com.aircandi.service.objects.User;
import com.aircandi.ui.base.BaseEntityForm;
import com.aircandi.utilities.UI;

public class UserStats implements RenderDelegate {

	//@SuppressWarnings("unused")
	@Override
	public void draw(BaseEntityForm activity) {

		User user = (User) activity.mEntity;
		final TextView stats = (TextView) activity.findViewById(R.id.stats);

		UI.setVisibility(stats, View.GONE);
		final StringBuilder statString = new StringBuilder(500);

		/* Like and watch stats */

		Count count = user.getCount(Constants.TYPE_LINK_LIKE, null, false, Direction.in);
		if (count == null) count = new Count(Constants.TYPE_LINK_LIKE, Constants.SCHEMA_ENTITY_USER, 0);
		statString.append("Liked by: " + String.valueOf(count.count.intValue()) + "<br/>");

		count = user.getCount(Constants.TYPE_LINK_WATCH, null, false, Direction.in);
		if (count == null) count = new Count(Constants.TYPE_LINK_WATCH, Constants.SCHEMA_ENTITY_USER, 0);
		statString.append("Watchers: " + String.valueOf(count.count.intValue()) + "<br/>");

		/* Other stats */

		if (stats != null && user.stats != null && user.stats.size() > 0) {

			int insertPictureCount = 0;
			int insertCommentCount = 0;
			int insertCandigramCount = 0;
			int updateCandigramCount = 0;
			int bounceCount = 0;
			int expandCount = 0;

			for (Count stat : user.stats) {
								
				/* Insert */
				if (stat.type.equals(EventType.INSERT_CANDIGRAM)) {
					insertCandigramCount += stat.count.intValue();
				}
				else if (stat.type.equals(EventType.INSERT_PICTURE_TO_CANDIGRAM)) {
					insertPictureCount += stat.count.intValue();
				}
				else if (stat.type.equals(EventType.INSERT_COMMENT_TO_CANDIGRAM)) {
					insertCommentCount += stat.count.intValue();
				}
				
				/* Update */
				else if (stat.type.equals(EventType.UPDATE_CANDIGRAM)) {
					updateCandigramCount += stat.count.intValue();
				}
				
				/* Move and expand */
				else if (stat.type.equals(EventType.MOVE_CANDIGRAM)) {
					bounceCount += stat.count.intValue();
				}
				else if (stat.type.equals(EventType.EXPAND_CANDIGRAM)) {
					expandCount += stat.count.intValue();
				}

			}

			if (insertCandigramCount > 0) {
				statString.append("Candigrams: " + String.valueOf(insertCandigramCount) + "<br/>");
			}

			if (insertPictureCount > 0) {
				statString.append("Pictures: " + String.valueOf(insertPictureCount) + "<br/>");
			}

			if (insertCommentCount > 0) {
				statString.append("Comments: " + String.valueOf(insertCommentCount) + "<br/>");
			}

			if (updateCandigramCount > 0) {
				statString.append("Candigrams edited: " + String.valueOf(updateCandigramCount) + "<br/>");
			}

			if (bounceCount > 0) {
				statString.append("Candigrams kicks: " + String.valueOf(bounceCount) + "<br/>");
			}

			if (expandCount > 0) {
				statString.append("Candigrams repeats: " + String.valueOf(expandCount) + "<br/>");
			}

			stats.setText(Html.fromHtml(statString.toString()));
			UI.setVisibility(stats, View.VISIBLE);
		}
	}
}
