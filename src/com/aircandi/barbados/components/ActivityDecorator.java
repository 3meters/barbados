package com.aircandi.barbados.components;

import android.text.TextUtils;

import com.aircandi.barbados.Constants;
import com.aircandi.objects.Action.EventCategory;
import com.aircandi.objects.ActivityBase;
import com.aircandi.objects.ActivityBase.TriggerType;
import com.aircandi.objects.Photo;

public class ActivityDecorator extends com.aircandi.components.ActivityDecorator {

	/*
	 * Own
	 * 
	 * [George Snelling] commented on your [candigram] at [Taco Del Mar].
	 * [George Snelling] commented on your [picture] at [Taco Del Mar].
	 * [George Snelling] commented on your [place] [Taco Del Mar].
	 * 
	 * [George Snelling] added a [picture] to your [place] [Taco Del Mar].
	 * [George Snelling] added a [picture] to your [candigram] at [Taco Del Mar].
	 * [George Snelling] added a [candigram] to your [place] [Taco Del Mar].
	 * 
	 * [George Snelling] kicked a [candigram] to your [place] [Taco Del Mar].
	 * 
	 * Watching
	 * 
	 * [George Snelling] commented on a [candigram] you are watching.
	 * [George Snelling] commented on a [picture] you are watching.
	 * [George Snelling] commented on a [place] you are watching.
	 * 
	 * [George Snelling] added a [picture] to a [place] you are watching.
	 * [George Snelling] added a [picture] to a [candigram] you are watching.
	 * [George Snelling] added a [candigram] to a [place] you are watching.
	 * 
	 * [George Snelling] kicked a [candigram] to a [place] you are watching.
	 * 
	 * Nearby
	 * 
	 * [George Snelling] commented on a [candigram] nearby.
	 * [George Snelling] commented on a [picture] nearby.
	 * [George Snelling] commented on a [place] nearby.
	 * 
	 * [George Snelling] added a [picture] to a [place] nearby.
	 * [George Snelling] added a [picture] to a [candigram] nearby.
	 * [George Snelling] added a [candigram] to a [place] nearby.
	 * 
	 * [George Snelling] kicked a [candigram] to a [place] nearby.
	 * 
	 * Move
	 * 
	 * A candigram has traveled to a place nearby
	 * A candigram has traveled to your place Taco Del Mar.
	 * A candigram has traveled to a place you are watching.
	 */

	@Override
	public String subtitle(ActivityBase activity) {

		if (activity.action.getEventCategory().equals(EventCategory.MOVE)) {
			if (activity.action.entity.schema.equals(Constants.SCHEMA_ENTITY_CANDIGRAM)) {

				if (activity.action.entity.type.equals(Constants.TYPE_APP_BOUNCE)) {

					if (activity.trigger.equals(TriggerType.NEARBY))
						return "kicked a candigram to a place nearby.";
					else if (activity.trigger.equals(TriggerType.WATCH))
						return "kicked a candigram you\'re watching to a new place.";
					else if (activity.trigger.equals(TriggerType.WATCH_TO))
						return "kicked a candigram to a place you\'re watching.";
					else if (activity.trigger.equals(TriggerType.WATCH_USER)
							|| activity.trigger.equals(TriggerType.NONE))
						return "kicked a candigram to a new place.";
					else if (activity.trigger.equals(TriggerType.OWN))
						return "kicked a candigram you started to a new place.";
					else if (activity.trigger.equals(TriggerType.OWN_TO)) return "kicked a candigram to a place of yours.";
				}
				else if (activity.action.entity.type.equals(Constants.TYPE_APP_TOUR)) {

					if (activity.trigger.equals(TriggerType.NEARBY))
						return "A candigram has traveled to a place nearby";
					else if (activity.trigger.equals(TriggerType.WATCH))
						return "A candigram you\'re watching has traveled to a new place";
					else if (activity.trigger.equals(TriggerType.WATCH_TO))
						return "A candigram has traveled to a place you\'re watching";
					else if (activity.trigger.equals(TriggerType.OWN))
						return "A candigram of yours has traveled to a new place.";
					else if (activity.trigger.equals(TriggerType.OWN_TO)) return "A candigram has traveled to a place of yours";
				}
			}
		}
		else if (activity.action.getEventCategory().equals(EventCategory.INSERT)) {

			if (activity.action.entity.schema.equals(Constants.SCHEMA_ENTITY_CANDIGRAM)) {

				if (activity.trigger.equals(TriggerType.NEARBY))
					return String.format("sent a candigram to a place nearby.", activity.action.entity.getSchemaMapped(),
							activity.action.toEntity.getSchemaMapped());
				else if (activity.trigger.equals(TriggerType.WATCH_TO))
					return String.format("sent a candigram to a place you are watching.", activity.action.entity.getSchemaMapped(),
							activity.action.toEntity.getSchemaMapped());
				else if (activity.trigger.equals(TriggerType.WATCH_USER)
						|| activity.trigger.equals(TriggerType.NONE))
					return String.format("sent a candigram.", activity.action.entity.getSchemaMapped(), activity.action.toEntity.getSchemaMapped());
				else if (activity.trigger.equals(TriggerType.OWN_TO)) return String
						.format("sent a candigram to a place of yours.", activity.action.entity.getSchemaMapped(), activity.action.toEntity.getSchemaMapped());
			}
		}
		return super.subtitle(activity);
	}

	@Override
	public String title(ActivityBase activity) {
		if (activity.action.entity.schema.equals(Constants.SCHEMA_ENTITY_CANDIGRAM)
				&& activity.action.entity.type.equals(Constants.TYPE_APP_TOUR)
				&& activity.action.getEventCategory().equals(EventCategory.MOVE)) {
			return activity.action.entity.name;
		}
		
		return super.title(activity);
	}

	@Override
	public Photo photoBy(ActivityBase activity) {
		Photo photo = null;
		if (activity.action.entity.schema.equals(Constants.SCHEMA_ENTITY_CANDIGRAM)
				&& activity.action.entity.type.equals(Constants.TYPE_APP_TOUR)
				&& activity.action.getEventCategory().equals(EventCategory.MOVE)) {
			photo = activity.action.entity.getPhoto();
			photo.name = activity.action.entity.name;
			photo.shortcut = activity.action.entity.getShortcut();
			return photo;
		}
		else {
			return super.photoBy(activity);
		}
	}

	@Override
	public Photo photoOne(ActivityBase activity) {
		Photo photo = null;

		if (activity.action.entity.schema.equals(Constants.SCHEMA_ENTITY_CANDIGRAM)
				&& activity.action.entity.type.equals(Constants.TYPE_APP_TOUR)
				&& activity.action.getEventCategory().equals(EventCategory.MOVE)) {
			photo = activity.action.toEntity.getPhoto();
			photo.name = TextUtils.isEmpty(activity.action.toEntity.name)
					? activity.action.toEntity.getSchemaMapped()
					: activity.action.toEntity.name;
			photo.shortcut = activity.action.toEntity.getShortcut();
			return photo;
		}
		else {
			return super.photoOne(activity);
		}
	}
}
