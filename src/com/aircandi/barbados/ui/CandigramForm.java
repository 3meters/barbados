package com.aircandi.barbados.ui;

import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aircandi.Aircandi;
import com.aircandi.ServiceConstants;
import com.aircandi.barbados.Constants;
import com.aircandi.barbados.R;
import com.aircandi.barbados.components.AnimationManager;
import com.aircandi.barbados.components.EntityManager;
import com.aircandi.barbados.components.MediaManager;
import com.aircandi.barbados.objects.Candigram;
import com.aircandi.barbados.objects.LinkProfile;
import com.aircandi.barbados.objects.Route;
import com.aircandi.barbados.objects.TransitionType;
import com.aircandi.components.NetworkManager.ResponseCode;
import com.aircandi.components.ProximityManager.ModelResult;
import com.aircandi.components.StringManager;
import com.aircandi.controllers.IEntityController;
import com.aircandi.events.MessageEvent;
import com.aircandi.objects.Applink;
import com.aircandi.objects.Count;
import com.aircandi.objects.Entity;
import com.aircandi.objects.Link;
import com.aircandi.objects.Link.Direction;
import com.aircandi.objects.Photo;
import com.aircandi.objects.Place;
import com.aircandi.objects.ServiceBase;
import com.aircandi.objects.Shortcut;
import com.aircandi.objects.ShortcutSettings;
import com.aircandi.objects.User;
import com.aircandi.ui.base.BaseEntityForm;
import com.aircandi.ui.widgets.AirImageView;
import com.aircandi.ui.widgets.CandiView;
import com.aircandi.ui.widgets.CandiView.IndicatorOptions;
import com.aircandi.ui.widgets.EntityView;
import com.aircandi.ui.widgets.UserView;
import com.aircandi.utilities.DateTime;
import com.aircandi.utilities.DateTime.IntervalContext;
import com.aircandi.utilities.Dialogs;
import com.aircandi.utilities.Errors;
import com.aircandi.utilities.Type;
import com.aircandi.utilities.UI;
import com.squareup.otto.Subscribe;

public class CandigramForm extends BaseEntityForm {

	private Runnable	mTimer;
	private TextView	mActionInfo;

	@Override
	public void initialize(Bundle savedInstanceState) {
		super.initialize(savedInstanceState);

		mLinkProfile = LinkProfile.LINKS_FOR_CANDIGRAM;
		mActionInfo = (TextView) findViewById(R.id.action_info);
	}

	@Override
	public void draw() {
		/*
		 * For now, we assume that the candi form isn't recycled.
		 * 
		 * We leave most of the views visible by default so they are visible in the layout editor.
		 * 
		 * - WebImageView primary image is visible by default
		 * - WebImageView child views are gone by default
		 * - Header views are visible by default
		 */
		mFirstDraw = false;
		if (mActionInfo != null) {
			if (mEntity.type.equals(Constants.TYPE_APP_TOUR) && !((Candigram) mEntity).stopped && mTimer == null) {
				mTimer = new Runnable() {

					@Override
					public void run() {
						setActionText();
						mHandler.postDelayed(mTimer, 1000);
					}
				};
			}
			else {
				setActionText();
			}
		}

		setActivityTitle(mEntity.name);

		final CandiView candiView = (CandiView) findViewById(R.id.candi_view);
		final AirImageView photoView = (AirImageView) findViewById(R.id.entity_photo);
		final TextView name = (TextView) findViewById(R.id.name);
		final TextView subtitle = (TextView) findViewById(R.id.subtitle);

		final TextView description = (TextView) findViewById(R.id.description);
		final UserView user_one = (UserView) findViewById(R.id.user_one);
		final UserView user_two = (UserView) findViewById(R.id.user_two);

		if (candiView != null) {
			/*
			 * This is a place entity with a fancy image widget
			 */
			candiView.databind(mEntity, new IndicatorOptions());
		}
		else {
			UI.setVisibility(photoView, View.GONE);
			if (photoView != null) {

				int screenWidthDp = (int) UI.getScreenWidthDisplayPixels(this);
				int widgetWidthDp = 122;
				if (screenWidthDp - widgetWidthDp <= 264) {
					int photoViewWidth = UI.getRawPixelsForDisplayPixels(this, screenWidthDp - widgetWidthDp);
					RelativeLayout.LayoutParams paramsImage = new RelativeLayout.LayoutParams(photoViewWidth, photoViewWidth);
					photoView.setLayoutParams(paramsImage);
				}

				if (!UI.photosEqual(photoView.getPhoto(), mEntity.getPhoto())) {
					Photo photo = mEntity.getPhoto();
					UI.drawPhoto(photoView, photo);
					if (Type.isFalse(photo.usingDefault)) {
						photoView.setClickable(true);
					}
				}
				UI.setVisibility(photoView, View.VISIBLE);
			}

			name.setText(null);
			subtitle.setText(null);

			UI.setVisibility(name, View.GONE);
			if (name != null && mEntity.name != null && !mEntity.name.equals("")) {
				name.setText(Html.fromHtml(mEntity.name));
				UI.setVisibility(name, View.VISIBLE);
			}

			UI.setVisibility(subtitle, View.GONE);
			if (subtitle != null && mEntity.subtitle != null && !mEntity.subtitle.equals("")) {
				subtitle.setText(Html.fromHtml(mEntity.subtitle));
				UI.setVisibility(subtitle, View.VISIBLE);
			}
		}

		/* Set header */
		Candigram candigram = (Candigram) mEntity;
		UI.setVisibility(mActionInfo, View.GONE);
		if (candigram.type.equals(Constants.TYPE_APP_TOUR)) {
			/* Start updating the countdown in action info */
			mHandler.post(mTimer);
			((TextView) findViewById(R.id.header)).setText(StringManager.getString(R.string.label_candigram_type_tour_verbose) + " "
					+ StringManager.getString(R.string.form_title_candigram));
			UI.setVisibility(mActionInfo, View.VISIBLE);
		}
		else if (candigram.type.equals(Constants.TYPE_APP_BOUNCE)) {
			((TextView) findViewById(R.id.header)).setText(StringManager.getString(R.string.label_candigram_type_bounce_verbose) + " "
					+ StringManager.getString(R.string.form_title_candigram));
			if (candigram.stopped) {
				UI.setVisibility(mActionInfo, View.VISIBLE);
			}
		}

		/* Primary candi image */

		description.setText(null);

		UI.setVisibility(findViewById(R.id.section_description), View.GONE);
		if (description != null && mEntity.description != null && !mEntity.description.equals("")) {
			description.setText(Html.fromHtml(mEntity.description));
			UI.setVisibility(findViewById(R.id.section_description), View.VISIBLE);
		}

		/* Place context */
		EntityView placeView = (EntityView) findViewById(R.id.place);
		UI.setVisibility(placeView, View.GONE);
		if (placeView != null) {
			Link link = candigram.getParentLink(Constants.TYPE_LINK_CONTENT, Constants.SCHEMA_ENTITY_PLACE);
			if (link != null && link.shortcut != null) {
				Entity place = link.shortcut.getAsEntity();
				placeView.setLabel("currently at");
				placeView.databind(place);
				UI.setVisibility(placeView, View.VISIBLE);
			}
		}

		/* Stats */

		drawStats();

		/* Shortcuts */

		/* Clear shortcut holder */
		((ViewGroup) findViewById(R.id.holder_shortcuts)).removeAllViews();

		/* Synthetic applink shortcuts */
		ShortcutSettings settings = new ShortcutSettings(Constants.TYPE_LINK_CONTENT, Constants.SCHEMA_ENTITY_APPLINK, Direction.in, null, true, true);
		settings.appClass = Applink.class;
		List<Shortcut> shortcuts = mEntity.getShortcuts(settings, null, new Shortcut.SortByPositionSortDate());
		if (shortcuts.size() > 0) {
			Collections.sort(shortcuts, new Shortcut.SortByPositionSortDate());
			Boolean canAdd = Aircandi.getInstance().getMenuManager().canUserAdd(mEntity);
			prepareShortcuts(shortcuts
					, settings
					, canAdd ? R.string.section_candigram_shortcuts_applinks_can_add : R.string.label_section_candigram_applinks
					, R.string.label_link_links_more
					, mResources.getInteger(R.integer.limit_shortcuts_flow)
					, R.id.holder_shortcuts
					, R.layout.widget_shortcut);
		}

		/* service applink shortcuts */
		settings = new ShortcutSettings(Constants.TYPE_LINK_CONTENT, Constants.SCHEMA_ENTITY_APPLINK, Direction.in, null, false, true);
		settings.appClass = Applink.class;
		shortcuts = mEntity.getShortcuts(settings, null, new Shortcut.SortByPositionSortDate());
		if (shortcuts.size() > 0) {
			Collections.sort(shortcuts, new Shortcut.SortByPositionSortDate());
			prepareShortcuts(shortcuts
					, settings
					, null
					, R.string.label_link_links_more
					, mResources.getInteger(R.integer.limit_shortcuts_flow)
					, R.id.holder_shortcuts
					, R.layout.widget_shortcut);
		}

		/* Shortcuts for places linked to this candigram */
		settings = new ShortcutSettings(Constants.TYPE_LINK_CONTENT, Constants.SCHEMA_ENTITY_PLACE, Direction.out, null, false, false);
		settings.appClass = Place.class;
		shortcuts = mEntity.getShortcuts(settings, new ServiceBase.SortByPositionSortDate(), new Shortcut.SortByPositionSortDate());
		if (shortcuts.size() > 0) {
			Collections.sort(shortcuts, new Shortcut.SortByPositionSortDate());
			prepareShortcuts(shortcuts
					, settings
					, R.string.label_section_places
					, R.string.label_link_links_more
					, mResources.getInteger(R.integer.limit_shortcuts_flow)
					, R.id.holder_shortcuts
					, R.layout.widget_shortcut);
		}

		/* Creator block */

		UI.setVisibility(user_one, View.GONE);
		UI.setVisibility(user_two, View.GONE);
		UserView user = user_one;

		if (user != null
				&& mEntity.creator != null
				&& !mEntity.creator.id.equals(ServiceConstants.ADMIN_USER_ID)
				&& !mEntity.creator.id.equals(ServiceConstants.ANONYMOUS_USER_ID)) {

			if (mEntity.schema.equals(Constants.SCHEMA_ENTITY_PLACE)) {
				if (((Place) mEntity).getProvider().type.equals("aircandi")) {
					user.setLabel(StringManager.getString(R.string.label_created_by));
					user.databind(mEntity.creator, mEntity.createdDate.longValue(), mEntity.locked);
					UI.setVisibility(user, View.VISIBLE);
					user = user_two;
				}
			}
			else {
				if (mEntity.schema.equals(Constants.SCHEMA_ENTITY_PICTURE)) {
					user.setLabel(StringManager.getString(R.string.label_added_by));
				}
				else {
					user.setLabel(StringManager.getString(R.string.label_created_by));
				}
				user.databind(mEntity.creator, mEntity.createdDate.longValue(), mEntity.locked);
				UI.setVisibility(user_one, View.VISIBLE);
				user = user_two;
			}
		}

		/* Editor block */

		if (user != null && mEntity.modifier != null
				&& !mEntity.modifier.id.equals(ServiceConstants.ADMIN_USER_ID)
				&& !mEntity.modifier.id.equals(ServiceConstants.ANONYMOUS_USER_ID)) {
			if (mEntity.createdDate.longValue() != mEntity.modifiedDate.longValue()) {
				user.setLabel(StringManager.getString(R.string.label_edited_by));
				user.databind(mEntity.modifier, mEntity.modifiedDate.longValue(), null);
				UI.setVisibility(user, View.VISIBLE);
			}
		}

		/* Buttons */
		drawButtons();

		/* Visibility */
		if (mScrollView != null) {
			mScrollView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void drawStats() {
		super.drawStats();

		Count count = mEntity.getCount(Constants.TYPE_LINK_LIKE, null, false, Direction.in);
		if (count == null) {
			count = new Count(Constants.TYPE_LINK_LIKE, Constants.SCHEMA_ENTITY_CANDIGRAM, 0);
		}
		((TextView) findViewById(R.id.like_stats)).setText(String.valueOf(count.count));

		count = mEntity.getCount(Constants.TYPE_LINK_WATCH, null, false, Direction.in);
		if (count == null) {
			count = new Count(Constants.TYPE_LINK_WATCH, Constants.SCHEMA_ENTITY_CANDIGRAM, 0);
		}
		((TextView) findViewById(R.id.watching_stats)).setText(String.valueOf(count.count));
	}

	@Override
	protected void drawButtons() {
		super.drawButtons();

		Candigram candigram = (Candigram) mEntity;

		UI.setVisibility(findViewById(R.id.button_bounce), View.GONE);

		if (!candigram.stopped) {
			if (candigram.type.equals(Constants.TYPE_APP_BOUNCE)) {
				UI.setVisibility(findViewById(R.id.button_bounce), View.VISIBLE);
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------------------------------

	@Subscribe
	@SuppressWarnings("ucd")
	public void onMessage(final MessageEvent event) {
		/*
		 * Refresh the form because something new has been added to it
		 * like a comment or post.
		 */
		if (event.message.action.toEntity != null && mEntityId.equals(event.message.action.toEntity.id)) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					onRefresh();
				}
			});
		}
		else if (event.message.action.entity != null && mEntityId.equals(event.message.action.entity.id)) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					/*
					 * Candigram has moved or repeated and we want to show something.
					 * 
					 * - We don't get this message if current user triggered the move.
					 * - Refresh will update stats, show new place.
					 */
					String message = null;
					if (event.message.action.entity.type.equals(Constants.TYPE_APP_BOUNCE)) {
						message = StringManager.getString(R.string.alert_candigram_bounced);
					}
					else if (event.message.action.entity.type.equals(Constants.TYPE_APP_TOUR)) {
						message = StringManager.getString(R.string.alert_candigram_toured);
					}
					if (!TextUtils.isEmpty(event.message.action.toEntity.name)) {
						message += ": " + event.message.action.toEntity.name;
					}
					UI.showToastNotification(message, Toast.LENGTH_SHORT);
					onRefresh();
				}
			});
		}
	}

	@SuppressWarnings("ucd")
	public void onBounceButtonClick(View view) {

		if (Aircandi.getInstance().getCurrentUser().isAnonymous()) {
			Dialogs.signin(this, R.string.alert_signin_message_candigram_bounce);
			return;
		}

		new AsyncTask() {

			@Override
			protected void onPreExecute() {
				mBusy.showBusy();
			}

			@Override
			protected Object doInBackground(Object... params) {
				Thread.currentThread().setName("AsyncKickEntity");
				/*
				 * Call service routine to get a move candidate.
				 */
				final ModelResult result = ((EntityManager) Aircandi.getInstance().getEntityManager()).moveCandigram(mEntity, false, true, null);
				return result;
			}

			@Override
			protected void onPostExecute(Object response) {
				ModelResult result = (ModelResult) response;
				setSupportProgressBarIndeterminateVisibility(false);
				mBusy.hideBusy();
				if (result.serviceResponse.responseCode == ResponseCode.SUCCESS) {
					if (result.data != null) {
						List<Entity> entities = (List<Entity>) result.data;
						Entity place = entities.get(0);
						kickCandidate(place, Aircandi.getInstance().getCurrentUser());
					}
				}
				else {
					Errors.handleError(CandigramForm.this, result.serviceResponse);
				}
			}
		}.execute();

	}

	@Override
	public void onAdd(Bundle extras) {
		Aircandi.dispatch.route(this, Route.NEW_PICKER, mEntity, null, null);
	}

	@Override
	public void onHelp() {
		Bundle extras = new Bundle();
		Integer helpResId = null;
		if (mEntity.type.equals(Constants.TYPE_APP_BOUNCE)) {
			helpResId = R.layout.candigram_bouncing_help;
		}
		else if (mEntity.type.equals(Constants.TYPE_APP_TOUR)) {
			helpResId = R.layout.candigram_touring_help;
		}
		extras.putInt(Constants.EXTRA_HELP_ID, helpResId);
		Aircandi.dispatch.route(this, Route.HELP, null, null, extras);
	}

	// --------------------------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------------------------

	public void kickCandidate(final Entity place, final User user) {

		ViewGroup customView = getPlaceView((Place) place, place.name, place.getPhoto());
		final TextView message = (TextView) customView.findViewById(R.id.message);

		message.setText(StringManager.getString(R.string.alert_message_candigram_bounce));

		String dialogTitle = StringManager.getString(R.string.alert_candigram_bounce);
		if (mEntity.name != null && !mEntity.name.equals("")) {
			dialogTitle = mEntity.name;
		}

		final AlertDialog dialog = Dialogs.alertDialog(null
				, dialogTitle
				, null
				, customView
				, this
				, R.string.alert_button_bounce
				, android.R.string.cancel
				, R.string.alert_button_bounce_follow
				, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							kickWrapup(place, user, false);
						}
						else if (which == DialogInterface.BUTTON_NEUTRAL) {
							kickWrapup(place, user, true);
						}
						else if (which == DialogInterface.BUTTON_NEGATIVE) {
							dialog.dismiss();
						}
					}
				}
				, null);
		dialog.setCanceledOnTouchOutside(false);
	}

	public void kickWrapup(final Entity entity, User user, final Boolean follow) {

		new AsyncTask() {

			@Override
			protected void onPreExecute() {
				mBusy.showBusy();
			}

			@Override
			protected Object doInBackground(Object... params) {
				Thread.currentThread().setName("AsyncKickEntity");
				final ModelResult result = ((EntityManager) Aircandi.getInstance().getEntityManager()).moveCandigram(mEntity, false, false, entity.id);
				return result;
			}

			@Override
			protected void onPostExecute(Object response) {
				ModelResult result = (ModelResult) response;
				setSupportProgressBarIndeterminateVisibility(false);
				mBusy.hideBusy();
				if (result.serviceResponse.responseCode == ResponseCode.SUCCESS) {
					if (follow) {
						IEntityController controller = Aircandi.getInstance().getControllerForSchema(entity.schema);
						controller.view(CandigramForm.this, entity, null, null, null, null, true);
						MediaManager.playSound(MediaManager.SOUND_CANDIGRAM_EXIT, 1.0f);
						finish();
						AnimationManager.getInstance().doOverridePendingTransition(CandigramForm.this, TransitionType.CANDIGRAM_OUT);
					}
					else {
						MediaManager.playSound(MediaManager.SOUND_CANDIGRAM_EXIT, 1.0f);
						finish();
						AnimationManager.getInstance().doOverridePendingTransition(CandigramForm.this, TransitionType.CANDIGRAM_OUT);
					}
				}
				else {
					Errors.handleError(CandigramForm.this, result.serviceResponse);
				}
			}
		}.execute();
	}

	public void repeatCandidate(final Entity place, final User user) {

		ViewGroup customView = getPlaceView((Place) place, place.name, place.getPhoto());
		final TextView message = (TextView) customView.findViewById(R.id.message);

		message.setText(StringManager.getString(R.string.alert_message_candigram_expand));

		String dialogTitle = StringManager.getString(R.string.alert_promoted_candigram);
		if (mEntity.name != null && !mEntity.name.equals("")) {
			dialogTitle = mEntity.name;
		}

		final AlertDialog dialog = Dialogs.alertDialog(null
				, dialogTitle
				, null
				, customView
				, this
				, R.string.alert_button_expand
				, android.R.string.cancel
				, R.string.alert_button_expand_follow
				, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == DialogInterface.BUTTON_POSITIVE) {
							repeatWrapup(place, user, false);
						}
						else if (which == DialogInterface.BUTTON_NEUTRAL) {
							repeatWrapup(place, user, true);
						}
						else if (which == DialogInterface.BUTTON_NEGATIVE) {
							dialog.dismiss();
						}
					}
				}
				, null);
		dialog.setCanceledOnTouchOutside(false);
	}

	public void repeatWrapup(final Entity entity, User user, final Boolean follow) {

		new AsyncTask() {

			@Override
			protected void onPreExecute() {
				mBusy.showBusy();
			}

			@Override
			protected Object doInBackground(Object... params) {
				Thread.currentThread().setName("AsyncPromoteEntity");
				final ModelResult result = ((EntityManager) Aircandi.getInstance().getEntityManager()).moveCandigram(mEntity, true, false, entity.id);
				return result;
			}

			@Override
			protected void onPostExecute(Object response) {
				ModelResult result = (ModelResult) response;
				setSupportProgressBarIndeterminateVisibility(false);
				mBusy.hideBusy();
				if (result.serviceResponse.responseCode == ResponseCode.SUCCESS) {
					if (follow) {
						IEntityController controller = Aircandi.getInstance().getControllerForSchema(entity.schema);
						controller.view(CandigramForm.this, entity, null, null, null, null, true);
						MediaManager.playSound(MediaManager.SOUND_CANDIGRAM_EXIT, 1.0f);
						finish();
						AnimationManager.getInstance().doOverridePendingTransition(CandigramForm.this, TransitionType.CANDIGRAM_OUT);
					}
					else {
						MediaManager.playSound(MediaManager.SOUND_CANDIGRAM_EXIT, 1.0f);
						onRefresh();
					}
				}
				else {
					Errors.handleError(CandigramForm.this, result.serviceResponse);
				}
			}
		}.execute();
	}

	public ViewGroup getPlaceView(Place place, String placeName, Photo placePhoto) {

		final LayoutInflater inflater = LayoutInflater.from(this);
		final ViewGroup customView = (ViewGroup) inflater.inflate(R.layout.dialog_candigram, null);
		final TextView name = (TextView) customView.findViewById(R.id.name);
		final TextView address = (TextView) customView.findViewById(R.id.address);
		final AirImageView photoView = (AirImageView) customView.findViewById(R.id.entity_photo);

		UI.setVisibility(name, View.GONE);
		UI.setVisibility(address, View.GONE);
		if (placeName != null) {
			name.setText(placeName);
			UI.setVisibility(name, View.VISIBLE);
		}

		if (place != null) {
			String addressBlock = "";
			if (place.city != null && place.region != null && !place.city.equals("") && !place.region.equals("")) {
				addressBlock += place.city + ", " + place.region;
			}
			else if (place.city != null && !place.city.equals("")) {
				addressBlock += place.city;
			}
			else if (place.region != null && !place.region.equals("")) {
				addressBlock += place.region;
			}
			if (!addressBlock.equals("")) {
				address.setText(addressBlock);
				UI.setVisibility(address, View.VISIBLE);
			}
		}

		photoView.setTag(placePhoto);
		UI.drawPhoto(photoView, placePhoto);
		return customView;
	}

	private void setActionText() {
		Candigram candigram = (Candigram) mEntity;
		String action = "ready to leave";

		if (Type.isTrue(candigram.stopped)) {
			action = "parked";
			if (candigram.hopCount != null && candigram.hopsMax != null && candigram.hopCount.intValue() >= candigram.hopsMax.intValue()) {
				action = "finished traveling and back with sender";
			}
		}
		else {
			if (candigram.hopNextDate != null) {
				Long now = DateTime.nowDate().getTime();
				Long next = candigram.hopNextDate.longValue();
				String timeTill = DateTime.interval(now, next, IntervalContext.FUTURE);
				if (!timeTill.equals("now")) {
					action = "leaving in" + "\n" + timeTill;
				}
				else {
					action = "waiting to leave";
				}
			}
		}
		mActionInfo.setText(action);
	}

	// --------------------------------------------------------------------------------------------
	// Menus
	// --------------------------------------------------------------------------------------------

	// --------------------------------------------------------------------------------------------
	// Lifecycle
	// --------------------------------------------------------------------------------------------

	@Override
	protected void onPause() {
		mHandler.removeCallbacks(mTimer);
		super.onPause();
	}

	// --------------------------------------------------------------------------------------------
	// Misc
	// --------------------------------------------------------------------------------------------

	@Override
	protected int getLayoutId() {
		return R.layout.candigram_form;
	}

}