package com.aircandi.ui.user;

import android.os.Bundle;

public class ProfileFormExtended extends ProfileForm {
	@Override
	public void initialize(Bundle savedInstanceState) {
		super.initialize(savedInstanceState);
		mDrawStats = new com.aircandi.barbados.ui.components.UserStats();
	}
}