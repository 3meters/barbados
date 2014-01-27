package com.aircandi.barbados.ui.user;

import android.os.Bundle;

public class UserForm extends com.aircandi.ui.user.UserForm {

	@Override
	public void initialize(Bundle savedInstanceState) {
		super.initialize(savedInstanceState);
		mDrawStats = new com.aircandi.barbados.ui.components.UserStats();
	}
}