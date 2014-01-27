package com.aircandi.barbados.controllers;

import java.util.ArrayList;
import java.util.List;

import com.aircandi.barbados.Constants;
import com.aircandi.barbados.R;
import com.aircandi.barbados.ui.PlaceForm;
import com.aircandi.components.AirApplication;
import com.aircandi.components.StringManager;

public class Places extends com.aircandi.controllers.Places {
	
	public Places() {
		super();
		mBrowseClass = PlaceForm.class;
	}

	@Override
	public List<Object> getApplications(String themeTone) {

		final List<Object> listData = new ArrayList<Object>();
		
		listData.add(new AirApplication(themeTone.equals("light") ? R.drawable.ic_logo_holo_light : R.drawable.ic_logo_holo_dark
				, StringManager.getString(R.string.dialog_application_candigram_new), null, Constants.SCHEMA_ENTITY_CANDIGRAM));
		
		return listData;
	}
}
