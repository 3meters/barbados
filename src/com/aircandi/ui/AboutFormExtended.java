package com.aircandi.ui;

import android.os.Bundle;
import android.widget.TextView;

import com.aircandi.R;

public class AboutFormExtended extends AboutForm {

	private TextView	mAppName;

	@Override
	public void initialize(Bundle savedInstanceState) {
		super.initialize(savedInstanceState);
		mAppName = (TextView) findViewById(R.id.app_name);
		mAppName.setText("Total Takover!!");
	}
}