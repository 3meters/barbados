package com.aircandi.barbados.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.aircandi.barbados.R;

public class AboutForm extends com.aircandi.ui.AboutForm {

	private TextView	mAppName;

	@Override
	public void initialize(Bundle savedInstanceState) {
		super.initialize(savedInstanceState);
		mAppName = (TextView) findViewById(R.id.app_name);
		mAppName.setText("Total Takover!!");
	}
}