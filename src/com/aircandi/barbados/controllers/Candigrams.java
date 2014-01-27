package com.aircandi.barbados.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

import com.aircandi.barbados.Barbados;
import com.aircandi.barbados.Constants;
import com.aircandi.barbados.R;
import com.aircandi.barbados.objects.Candigram;
import com.aircandi.barbados.objects.LinkProfile;
import com.aircandi.barbados.ui.CandigramForm;
import com.aircandi.barbados.ui.edit.CandigramEdit;
import com.aircandi.barbados.ui.edit.CandigramWizard;
import com.aircandi.components.AirApplication;
import com.aircandi.components.SpinnerData;
import com.aircandi.components.StringManager;
import com.aircandi.controllers.EntityControllerBase;
import com.aircandi.objects.Entity;
import com.aircandi.objects.NotificationType;
import com.aircandi.utilities.Colors;
import com.aircandi.utilities.DateTime;
import com.aircandi.utilities.Integers;

public class Candigrams extends EntityControllerBase {

	public Candigrams() {
		mColorPrimary = R.color.brand_pink_lighter;
		mSchema = Constants.SCHEMA_ENTITY_CANDIGRAM;
		mBrowseClass = CandigramForm.class;
		mEditClass = CandigramEdit.class;
		mNewClass = CandigramWizard.class;
		mPageSize = Integers.getInteger(R.integer.page_size_candigrams);
		mListLayoutResId = R.layout.entity_list_fragment;		
		mListItemResId = R.layout.temp_listitem_candigram;
		mListLoadingResId = R.layout.temp_list_item_loading;
	}

	@Override
	public Entity makeNew() {
		Entity entity = new Candigram();
		entity.schema = mSchema;
		entity.id = "temp:" + DateTime.nowString(DateTime.DATE_NOW_FORMAT_FILENAME); // Temporary
		entity.signalFence = -100.0f;
		return entity;
	}

	@Override
	public Integer getLinkProfile() {
		return LinkProfile.LINKS_FOR_CANDIGRAM;
	}

	@Override
	public Drawable getIcon() {
		Drawable icon = Barbados.applicationContext.getResources().getDrawable(R.drawable.img_candigram_temp);
		icon.setColorFilter(Colors.getColor(mColorPrimary), PorterDuff.Mode.SRC_ATOP);
		return icon;
	}

	@Override
	public String getType(Entity entity, Boolean verbose) {
		if (entity.type.equals(Constants.TYPE_APP_TOUR)) {
			return StringManager.getString(R.string.label_candigram_type_tour_verbose);
		}
		else if (entity.type.equals(Constants.TYPE_APP_BOUNCE)) {
			return StringManager.getString(R.string.label_candigram_type_bounce_verbose);
		}
		return null;
	}
	
	@Override
	public Integer getNotificationType(Entity entity) {
		if (entity.getPhoto().getUri() != null) {
			return NotificationType.BIG_PICTURE;
		}
		return NotificationType.NORMAL;
	}
	
	@Override
	public List<Object> getApplications(String themeTone) {

		final List<Object> listData = new ArrayList<Object>();

		listData.add(new AirApplication(themeTone.equals("light") ? R.drawable.ic_action_picture_light : R.drawable.ic_action_picture_dark
				, StringManager.getString(R.string.dialog_application_picture_new), null, Constants.SCHEMA_ENTITY_PICTURE));

		listData.add(new AirApplication(themeTone.equals("light") ? R.drawable.ic_action_monolog_light : R.drawable.ic_action_monolog_dark
				, StringManager.getString(R.string.dialog_application_comment_new), null, Constants.SCHEMA_ENTITY_COMMENT));

		return listData;
	}

	@Override
	public Entity makeFromMap(Map<String, Object> map, Boolean nameMapping) {
		return Candigram.setPropertiesFromMap(new Candigram(), map, nameMapping);
	}

	public static int	RANGE_DEFAULT_POSITION		= 1;
	public static int	DURATION_DEFAULT_POSITION	= 1;
	public static int	HOPS_DEFAULT_POSITION		= 1;
	public static int	ICON_COLOR					= R.color.brand_pink_lighter;
	
	/*
	 * Property support
	 */

	public static SpinnerData getSpinnerData(Context context, PropertyType propertyType) {

		Integer entriesResId = null;
		Integer valuesResId = null;
		Integer descriptionsResId = null;

		if (propertyType == PropertyType.TYPE) {
			entriesResId = R.array.candigram_type_entries;
			valuesResId = R.array.candigram_type_values;
			descriptionsResId = R.array.candigram_type_descriptions;
		}
		else if (propertyType == PropertyType.RANGE) {
			entriesResId = R.array.candigram_range_entries;
			valuesResId = R.array.candigram_range_values;
			descriptionsResId = R.array.candigram_range_descriptions;
		}
		else if (propertyType == PropertyType.DURATION) {
			entriesResId = R.array.candigram_duration_entries;
			valuesResId = R.array.candigram_duration_values;
			descriptionsResId = R.array.candigram_duration_descriptions;
		}
		else if (propertyType == PropertyType.HOPS) {
			entriesResId = R.array.candigram_hops_entries;
			valuesResId = R.array.candigram_hops_values;
			descriptionsResId = R.array.candigram_hops_descriptions;
		}

		SpinnerData data = new SpinnerData(context);
		data.setEntries(entriesResId);
		data.setEntryValues(valuesResId);
		data.setDescriptions(descriptionsResId);

		return data;
	}

	public static List<PropertyValue> getPropertyValues(Context context, PropertyType propertyType) {

		List<PropertyValue> propertyValues = new ArrayList<PropertyValue>();

		Integer entriesResId = null;
		Integer valuesResId = null;
		Integer descriptionsResId = null;

		if (propertyType == PropertyType.TYPE) {
			entriesResId = R.array.candigram_type_entries;
			valuesResId = R.array.candigram_type_values;
			descriptionsResId = R.array.candigram_type_descriptions;
		}

		String[] entries = context.getResources().getStringArray(entriesResId);
		String[] values = context.getResources().getStringArray(valuesResId);
		String[] description = context.getResources().getStringArray(descriptionsResId);

		for (int i = 0; i < entries.length; i++) {
			propertyValues.add(new PropertyValue(entries[i], values[i], description[i]));
		}

		return propertyValues;
	}

	// --------------------------------------------------------------------------------------------
	// Classes
	// --------------------------------------------------------------------------------------------

	public static class PropertyValue {

		public String	name;
		public Object	value;
		public String	description;

		public PropertyValue(String name, Object value, String description) {
			this.name = name;
			this.value = value;
			this.description = description;
		}
	}

	public enum PropertyType {
		TYPE,
		RANGE,
		DURATION,
		LOCKED,
		HOPS
	}
}
