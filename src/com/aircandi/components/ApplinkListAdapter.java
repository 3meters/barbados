package com.aircandi.components;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filterable;
import android.widget.TextView;

import com.aircandi.beta.R;
import com.aircandi.components.bitmaps.BitmapRequest;
import com.aircandi.components.bitmaps.BitmapRequestBuilder;
import com.aircandi.service.objects.Applink;
import com.aircandi.service.objects.Entity;
import com.aircandi.ui.widgets.WebImageView;

public class ApplinkListAdapter extends ArrayAdapter<Entity> implements Filterable {

	private final LayoutInflater	mInflater;
	private Integer					mItemLayoutId	= R.layout.temp_listitem_sources_builder;
	private final List<Entity>		mListItems;

	public ApplinkListAdapter(Context context, List<Entity> applinks, Integer itemLayoutId) {
		super(context, 0, applinks);

		mListItems = applinks;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (itemLayoutId != null) {
			mItemLayoutId = itemLayoutId;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final ViewHolder holder;
		final Entity itemData = mListItems.get(position);

		if (view == null) {
			view = mInflater.inflate(mItemLayoutId, null);
			holder = new ViewHolder();
			holder.photo = (WebImageView) view.findViewById(R.id.photo);
			holder.name = (TextView) view.findViewById(R.id.name);
			holder.id = (TextView) view.findViewById(R.id.id);
			holder.url = (TextView) view.findViewById(R.id.url);
			holder.checked = (CheckBox) view.findViewById(R.id.checked);
			if (holder.checked != null) {
				holder.checked.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View view) {
						final CheckBox checkBox = (CheckBox) view;
						final Entity applink = (Entity) checkBox.getTag();
						applink.checked = checkBox.isChecked();
					}
				});
			}
			view.setTag(holder);

			FontManager.getInstance().setTypefaceDefault(holder.name);
			FontManager.getInstance().setTypefaceDefault(holder.id);
			FontManager.getInstance().setTypefaceDefault(holder.url);
		}
		else {
			holder = (ViewHolder) view.getTag();
		}

		if (itemData != null) {
			final Applink applink = (Applink) itemData;

			setVisibility(holder.checked, View.GONE);
			if (holder.checked != null && applink.checked != null) {
				holder.checked.setChecked(applink.checked);
				holder.checked.setTag(applink);
				setVisibility(holder.checked, View.VISIBLE);
			}

			setVisibility(holder.name, View.GONE);
			if (holder.name != null && applink.name != null && applink.name.length() > 0) {
				holder.name.setText(applink.name);
				setVisibility(holder.name, View.VISIBLE);
			}

			setVisibility(holder.id, View.GONE);
			if (holder.id != null) {
				if (applink.id != null && applink.id.length() > 0) {
					holder.id.setText(applink.id);
					setVisibility(holder.id, View.VISIBLE);
				}
			}

			setVisibility(holder.url, View.GONE);
			if (holder.url != null) {
				if (applink.appUrl != null && applink.appUrl.length() > 0) {
					holder.url.setText(applink.appUrl);
					setVisibility(holder.url, View.VISIBLE);
				}
			}
			
			if (holder.photo != null) {
				holder.photo.setTag(applink);
				/*
				 * The WebImageView sets the current bitmap ref being held
				 * by the internal image view to null before doing the work
				 * to satisfy the new request.
				 */
				final String imageUri = applink.getPhotoUri();

				/* Don't do anything if the image is already set to the one we want */
				if (holder.photo.getImageUri() == null || !holder.photo.getImageUri().equals(imageUri)) {

					final BitmapRequestBuilder builder = new BitmapRequestBuilder(holder.photo).setImageUri(imageUri);
					final BitmapRequest imageRequest = builder.create();
					holder.photo.setBitmapRequest(imageRequest);
				}
			}
		}
		return view;
	}

	@Override
	public Entity getItem(int position) {
		return mListItems.get(position);
	}

	@Override
	public int getCount() {
		return mListItems.size();
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	private static void setVisibility(View view, Integer visibility) {
		if (view != null) {
			view.setVisibility(visibility);
		}
	}

	private static class ViewHolder {
		private WebImageView	photo;
		private TextView		name;
		private TextView		id;
		private TextView		url;
		private CheckBox		checked;
	}
}
