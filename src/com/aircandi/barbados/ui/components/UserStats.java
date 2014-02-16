package com.aircandi.barbados.ui.components;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.aircandi.Constants;
import com.aircandi.R;
import com.aircandi.barbados.objects.EventType;
import com.aircandi.components.RenderDelegate;
import com.aircandi.objects.Count;
import com.aircandi.objects.Entity;
import com.aircandi.objects.Link.Direction;
import com.aircandi.objects.User;
import com.aircandi.utilities.UI;

public class UserStats implements RenderDelegate {

	//@SuppressWarnings("unused")
	@Override
	public void draw(Entity entity, View view) {

		User user = (User) entity;
		final TextView stats = (TextView) view.findViewById(R.id.stats);

		UI.setVisibility(stats, View.GONE);
		final StringBuilder statString = new StringBuilder(500);

		/* Like and watch stats */

		TextView likeStats = (TextView) view.findViewById(R.id.like_stats);
		if (likeStats != null) {
			Count count = user.getCount(Constants.TYPE_LINK_LIKE, null, false, Direction.in);
			if (count == null) {
				count = new Count(Constants.TYPE_LINK_LIKE, Constants.SCHEMA_ENTITY_PICTURE, 0);
			}
			likeStats.setText(String.valueOf(count.count));
		}

		TextView watchingStats = (TextView) view.findViewById(R.id.watching_stats);
		if (watchingStats != null) {
			Count count = user.getCount(Constants.TYPE_LINK_WATCH, null, false, Direction.in);
			if (count == null) {
				count = new Count(Constants.TYPE_LINK_WATCH, Constants.SCHEMA_ENTITY_PICTURE, 0);
			}
			watchingStats.setText(String.valueOf(count.count));
		}

		/* Other stats */

		if (stats != null && user.stats != null && user.stats.size() > 0) {

			int insertPictureCount = 0;
			int insertCommentCount = 0;
			int insertCandigramCount = 0;
			int updateCandigramCount = 0;
			int bounceCount = 0;
			int forwardCount = 0;

			for (Count stat : user.stats) {
								
				/* Insert */
				if (stat.type.equals(EventType.INSERT_CANDIGRAM_TO_PLACE)) {
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
				
				/* Move and forward */
				else if (stat.type.equals(EventType.MOVE_CANDIGRAM)) {
					bounceCount += stat.count.intValue();
				}
				else if (stat.type.equals(EventType.FORWARD_CANDIGRAM)) {
					forwardCount += stat.count.intValue();
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

			if (forwardCount > 0) {
				statString.append("Candigrams forwarded: " + String.valueOf(forwardCount) + "<br/>");
			}

			stats.setText(Html.fromHtml(statString.toString()));
			UI.setVisibility(stats, View.VISIBLE);
		}
	}
}
