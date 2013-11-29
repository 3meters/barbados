package com.aircandi.barbados.ui.widgets;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.aircandi.Constants;
import com.aircandi.barbados.R;
import com.aircandi.service.objects.Entity;
import com.aircandi.service.objects.Link.Direction;
import com.aircandi.service.objects.Shortcut;
import com.aircandi.service.objects.ShortcutSettings;
import com.aircandi.ui.widgets.AirImageView;
import com.aircandi.utilities.Integers;
import com.aircandi.utilities.UI;

@SuppressWarnings("ucd")
public class CandiView extends com.aircandi.ui.widgets.CandiView {

	public static final int	HORIZONTAL	= 0;
	public static final int	VERTICAL	= 1;

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
		setVisibility(mShortcuts, View.GONE);
		if (mShortcuts != null) {

			mShortcuts.removeAllViews();

			ShortcutSettings settings = new ShortcutSettings(Constants.TYPE_LINK_CONTENT, Constants.SCHEMA_ENTITY_CANDIGRAM, Direction.in, null, false, false);
			List<Shortcut> shortcuts = (List<Shortcut>) entity.getShortcuts(settings, null, new Shortcut.SortByPositionSortDate());
			if (shortcuts.size() > 0) {

				final LayoutInflater inflater = LayoutInflater.from(this.getContext());
				final int sizePixels = UI.getRawPixelsForDisplayPixels(this.getContext(), 40);
				final int marginPixels = UI.getRawPixelsForDisplayPixels(this.getContext(), 5);

				/* We only show the first five */
				int shortcutCount = 0;
				for (Shortcut shortcut : shortcuts) {
					if (shortcutCount < Integers.getInteger(R.integer.limit_indicators_radar)) {
						View view = inflater.inflate(R.layout.temp_indicator_candigram, null);
						AirImageView photoView = (AirImageView) view.findViewById(R.id.photo);
						photoView.setSizeHint(sizePixels);

						UI.drawPhoto(photoView, shortcut.getPhoto());

						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePixels, sizePixels);
						params.setMargins(marginPixels
								, marginPixels
								, marginPixels
								, marginPixels);
						view.setLayoutParams(params);
						mShortcuts.addView(view);
					}
					shortcutCount++;
				}
				setVisibility(mShortcuts, View.VISIBLE);
			}
		}
	}
}
