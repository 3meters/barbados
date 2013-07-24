package com.aircandi.ui.helpers;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.aircandi.Constants;
import com.aircandi.beta.R;
import com.aircandi.components.AirApplication;
import com.aircandi.components.AndroidManager;
import com.aircandi.service.HttpService;
import com.aircandi.service.HttpService.ObjectType;
import com.aircandi.service.objects.Applink;
import com.aircandi.service.objects.Entity;
import com.aircandi.service.objects.Link.Direction;
import com.aircandi.service.objects.Place;
import com.aircandi.ui.base.BaseBrowse;

public class PhotoSourcePicker extends BaseBrowse implements OnItemClickListener {

	private TextView	mName;
	private ListView	mListView;
	private ListAdapter	mListAdapter;
	private Entity		mEntity;
	
	@Override
	protected void unpackIntent() {
		super.unpackIntent();
		
		final Bundle extras = getIntent().getExtras();
		if (extras != null) {
			final String jsonEntity = extras.getString(Constants.EXTRA_ENTITY);
			if (jsonEntity != null) {
				mEntity = (Entity) HttpService.jsonToObject(jsonEntity, ObjectType.Entity);
			}
		}
	}

	@Override
	protected void initialize(Bundle savedInstanceState) {
		super.initialize(savedInstanceState);

		mName = (TextView) findViewById(R.id.name);
		mListView = (ListView) findViewById(R.id.form_list);
	}

	@Override
	protected void databind(Boolean refresh) {

		/* Shown as a dialog so doesn't have an action bar */
		final List<Object> listData = new ArrayList<Object>();

		/* Everyone gets these options */
		listData.add(new AirApplication(mThemeTone.equals("light") ? R.drawable.ic_action_search_light : R.drawable.ic_action_search_dark
				, getString(R.string.dialog_picture_source_search), null, Constants.PHOTO_SOURCE_SEARCH));

		listData.add(new AirApplication(mThemeTone.equals("light") ? R.drawable.ic_action_tiles_large_light : R.drawable.ic_action_tiles_large_dark
				, getString(R.string.dialog_picture_source_gallery), null, Constants.PHOTO_SOURCE_GALLERY));

		/* Only show the camera choice if there is one and there is a place to store the image */
		if (AndroidManager.isIntentAvailable(this, MediaStore.ACTION_IMAGE_CAPTURE)) {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				listData.add(new AirApplication(mThemeTone.equals("light") ? R.drawable.ic_action_camera_light : R.drawable.ic_action_camera_dark
						, getString(R.string.dialog_picture_source_camera), null, Constants.PHOTO_SOURCE_CAMERA));
			}
		}

		/* Add place photo option if this is a place entity */
		if (mEntity.schema.equals(Constants.SCHEMA_ENTITY_PLACE)) {
			Place place = (Place) mEntity;
			if (place.getProvider().type != null && place.getProvider().type.equals(Constants.TYPE_PROVIDER_FOURSQUARE)) {
				listData.add(new AirApplication(mThemeTone.equals("light") ? R.drawable.ic_action_location_light : R.drawable.ic_action_location_dark
						, getString(R.string.dialog_picture_source_place), null, Constants.PHOTO_SOURCE_PLACE));
			}
			else {
				
				List<String> schemas = new ArrayList<String>();
				schemas.add(Constants.SCHEMA_ENTITY_POST);
				List<String> linkTypes = new ArrayList<String>();
				linkTypes.add(Constants.TYPE_LINK_POST);
				
				List<Entity> entities = (List<Entity>) mEntity.getLinkedEntitiesByLinkTypes(linkTypes
						, schemas
						, Direction.in
						, false);
				
				for (Entity post : entities) {
					if (post.photo != null) {
						listData.add(new AirApplication(mThemeTone.equals("light") ? R.drawable.ic_action_location_light
								: R.drawable.ic_action_location_dark
								, getString(R.string.dialog_picture_source_place), null, Constants.PHOTO_SOURCE_PLACE));
						break;
					}
				}
			}
		}
		else if (mEntity.schema.equals(Constants.SCHEMA_ENTITY_APPLINK)) {
			Applink applink = (Applink) mEntity;
			if (applink.appId != null) {
				if (applink.type.equals(Constants.TYPE_APP_FACEBOOK)) {
					listData.add(new AirApplication(mThemeTone.equals("light") ? R.drawable.ic_action_facebook_light : R.drawable.ic_action_facebook_dark
							, getString(R.string.dialog_picture_source_facebook), null, Constants.PHOTO_SOURCE_FACEBOOK));
				}
				else if (applink.type.equals(Constants.TYPE_APP_TWITTER)) {
					listData.add(new AirApplication(mThemeTone.equals("light") ? R.drawable.ic_action_twitter_light : R.drawable.ic_action_twitter_dark
							, getString(R.string.dialog_picture_source_twitter), null, Constants.PHOTO_SOURCE_TWITTER));
				}
			}
		}

		/* Everyone gets the default option */
		listData.add(new AirApplication(mThemeTone.equals("light") ? R.drawable.ic_action_picture_light : R.drawable.ic_action_picture_dark
				, getString(R.string.dialog_picture_source_default), null, Constants.PHOTO_SOURCE_DEFAULT));

		mName.setText(R.string.dialog_picture_source_title);

		mListAdapter = new ListAdapter(this, listData);
		mListView.setAdapter(mListAdapter);
		mListView.setOnItemClickListener(this);
	}

	// --------------------------------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------------------------------

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final AirApplication choice = (AirApplication) view.getTag();
		final Intent intent = new Intent();
		intent.putExtra(Constants.EXTRA_PICTURE_SOURCE, choice.schema);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	// --------------------------------------------------------------------------------------------
	// Inner classes
	// --------------------------------------------------------------------------------------------

	private class ListAdapter extends ArrayAdapter<Object> {
		
		private final List<Object>	items;

		private ListAdapter(Context context, List<Object> items) {
			super(context, 0, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			final AirApplication itemData = (AirApplication) items.get(position);

			if (view == null) {
				view = LayoutInflater.from(PhotoSourcePicker.this).inflate(R.layout.temp_listitem_photo_source, null);
			}

			if (itemData != null) {
				((ImageView) view.findViewById(R.id.photo)).setImageResource(itemData.iconResId);
				((TextView) view.findViewById(R.id.name)).setText(itemData.title);
				view.setTag(itemData);
			}
			return view;
		}
	}

	// --------------------------------------------------------------------------------------------
	// Misc
	// --------------------------------------------------------------------------------------------

	@Override
	protected Boolean isDialog() {
		return true;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.picker_picture_source;
	}

}