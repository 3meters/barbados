package com.proxibase.aircandi.components;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.proxibase.aircandi.R;
import com.proxibase.aircandi.widgets.AuthorBlock;
import com.proxibase.aircandi.widgets.TextViewEllipsizing;
import com.proxibase.aircandi.widgets.WebImageView;
import com.proxibase.service.objects.Entity;

public class CandiListAdapter extends ArrayAdapter<Entity> {

	private LayoutInflater	mInflater;
	private Integer			mItemLayoutId	= R.layout.temp_listitem_candi;
	private List<Entity>	mEntities;

	public CandiListAdapter(Context context, List<Entity> entities, Integer itemLayoutId) {
		super(context, 0, entities);
		mEntities = entities;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (itemLayoutId != null) {
			mItemLayoutId = itemLayoutId;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		final CandiListViewHolder holder;
		Entity itemData = (Entity) mEntities.get(position);

		if (view == null) {
			view = mInflater.inflate(mItemLayoutId, null);
			holder = new CandiListViewHolder();
			holder.itemImage = (WebImageView) view.findViewById(R.id.item_image);
			holder.itemTitle = (TextView) view.findViewById(R.id.item_title);
			holder.itemSubtitle = (TextView) view.findViewById(R.id.item_subtitle);
			holder.itemDescription = (TextViewEllipsizing) view.findViewById(R.id.item_description);
			holder.itemAuthor = (AuthorBlock) view.findViewById(R.id.item_block_author);
			holder.itemComments = (Button) view.findViewById(R.id.item_comments);
			view.setTag(holder);
		}
		else {
			holder = (CandiListViewHolder) view.getTag();
		}

		if (itemData != null) {
			Entity entity = itemData;
			holder.data = itemData;
			if (holder.itemTitle != null) {
				if (entity.title != null && entity.title.length() > 0) {
					holder.itemTitle.setText(entity.title);
					holder.itemTitle.setVisibility(View.VISIBLE);
				}
				else {
					holder.itemTitle.setVisibility(View.GONE);
				}
			}

			if (holder.itemSubtitle != null) {
				if (entity.subtitle != null && entity.subtitle.length() > 0) {
					holder.itemSubtitle.setText(entity.subtitle);
					holder.itemSubtitle.setVisibility(View.VISIBLE);
				}
				else {
					holder.itemSubtitle.setVisibility(View.GONE);
				}
			}

			if (holder.itemDescription != null) {
				holder.itemDescription.setMaxLines(5);
				if (entity.description != null && entity.description.length() > 0) {
					holder.itemDescription.setText(entity.description);
					holder.itemDescription.setVisibility(View.VISIBLE);
				}
				else {
					holder.itemDescription.setVisibility(View.GONE);
				}
			}

			/* Comments */
			if (holder.itemComments != null) {
				if (entity.commentCount != null && entity.commentCount > 0) {
					holder.itemComments.setText(String.valueOf(entity.commentCount) + (entity.commentCount == 1 ? " Comment" : " Comments"));
					holder.itemComments.setTag(entity);
					holder.itemComments.setVisibility(View.VISIBLE);
				}
				else {
					holder.itemComments.setVisibility(View.GONE);
				}
			}

			if (holder.itemAuthor != null) {
				if (entity.creator != null) {
					holder.itemAuthor.bindToAuthor(entity.creator, entity.modifiedDate.longValue(), entity.locked);
					holder.itemAuthor.setVisibility(View.VISIBLE);
				}
				else {
					holder.itemAuthor.setVisibility(View.GONE);
				}
			}

			if (holder.itemImage != null) {
				/*
				 * The WebImageView sets the current bitmap ref being held
				 * by the internal image view to null before doing the work
				 * to satisfy the new request.
				 */
				String imageUri = entity.getMasterImageUri();
				if (holder.itemImage.getImageView().getTag() == null || !imageUri.equals((String) holder.itemImage.getImageView().getTag())) {

					BitmapDrawable bitmapDrawable = (BitmapDrawable) holder.itemImage.getImageView().getDrawable();
					if (bitmapDrawable != null && bitmapDrawable.getBitmap() != null && !bitmapDrawable.getBitmap().isRecycled()) {
						bitmapDrawable.getBitmap().recycle();
					}
					ImageRequestBuilder builder = new ImageRequestBuilder(holder.itemImage);
					builder.setImageUri(imageUri);
					builder.setImageFormat(entity.getMasterImageFormat());
					builder.setLinkZoom(entity.linkZoom);
					builder.setLinkJavascriptEnabled(entity.linkJavascriptEnabled);
					ImageRequest imageRequest = builder.create();
					holder.itemImage.setImageRequest(imageRequest);

				}
			}
		}
		return view;
	}

	@Override
	public Entity getItem(int position) {
		return mEntities.get(position);
	}

	public boolean areAllItemsEnabled() {
		return false;
	}

	public boolean isEnabled(int position) {
		return false;
	}

	public static class CandiListViewHolder {

		public WebImageView			itemImage;
		public TextView				itemTitle;
		public TextView				itemSubtitle;
		public TextViewEllipsizing	itemDescription;
		public AuthorBlock			itemAuthor;
		public Button				itemComments;
		public View					itemActionButton;
		public Object				data;
	}
}
