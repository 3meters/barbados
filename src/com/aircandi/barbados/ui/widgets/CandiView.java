package com.aircandi.barbados.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.aircandi.barbados.Constants;
import com.aircandi.barbados.R;
import com.aircandi.objects.Entity;
import com.aircandi.objects.Link.Direction;
import com.aircandi.objects.Photo;
import com.aircandi.objects.Shortcut;
import com.aircandi.objects.ShortcutSettings;
import com.aircandi.ui.widgets.AirImageView;
import com.aircandi.utilities.Integers;
import com.aircandi.utilities.UI;

@SuppressWarnings("ucd")
public class CandiView extends com.aircandi.ui.widgets.CandiView {

	public static final int	HORIZONTAL	= 0;
	public static final int	VERTICAL	= 1;

	List<Shortcut>			mShortcuts	= new ArrayList<Shortcut>();

	public CandiView(Context context) {
		this(context, null);
	}

	public CandiView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CandiView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void databind(Entity entity, IndicatorOptions options) {
		options.forceUpdate = true;
		super.databind(entity, options);
	}

	@Override
	public void showIndicators(Entity entity, IndicatorOptions options) {

		/* Indicators */
		setVisibility(mHolderShortcuts, View.GONE);
		if (mHolderShortcuts != null) {

			ShortcutSettings settings = new ShortcutSettings(Constants.TYPE_LINK_CONTENT, Constants.SCHEMA_ENTITY_CANDIGRAM, Direction.in, null, false, false);
			List<Shortcut> shortcuts = (List<Shortcut>) entity.getShortcuts(settings, null, new Shortcut.SortByPositionSortDate());

			Boolean dirty = (mShortcuts.size() != shortcuts.size());
			if (!dirty) {
				Integer i = 0;
				for (Shortcut shortcut : mShortcuts) {
					if (!Photo.same(shortcut.getPhoto(), shortcuts.get(i).getPhoto())) {
						dirty = true;
						break;
					}
					i++;
				}
			}

			if (dirty) {
				mHolderShortcuts.removeAllViews();

				final LayoutInflater inflater = LayoutInflater.from(this.getContext());
				final int sizePixels = UI.getRawPixelsForDisplayPixels(this.getContext(), 40);
				final int marginPixels = UI.getRawPixelsForDisplayPixels(this.getContext(), 5);

				/* We only show the first five */
				int shortcutCount = 0;
				for (Shortcut shortcut : shortcuts) {
					if (shortcutCount < Integers.getInteger(R.integer.limit_indicators_radar)) {
						View view = inflater.inflate(R.layout.temp_indicator_candigram, null);
						AirImageView photoView = (AirImageView) view.findViewById(R.id.entity_photo);
						photoView.setSizeHint(sizePixels);

						UI.drawPhoto(photoView, shortcut.getPhoto());

						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePixels, sizePixels);
						params.setMargins(marginPixels
								, marginPixels
								, marginPixels
								, marginPixels);
						view.setLayoutParams(params);
						mHolderShortcuts.addView(view);
					}
					shortcutCount++;
				}
				mShortcuts = shortcuts;
			}
			if (mShortcuts.size() > 0) {
				setVisibility(mHolderShortcuts, View.VISIBLE);
			}
		}
	}
}
