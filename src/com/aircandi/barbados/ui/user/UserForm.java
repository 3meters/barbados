package com.aircandi.barbados.ui.user;

import android.os.Bundle;

import com.aircandi.R;
import com.aircandi.barbados.ui.components.UserStats;
import com.aircandi.ui.user.UserFragment;

@SuppressWarnings("ucd")
public class UserForm extends com.aircandi.ui.user.UserForm {

	protected UserFragment	mUserFragment;

	@Override
	public void initialize(Bundle savedInstanceState) {
		super.initialize(savedInstanceState);

		mUserFragment = new UserFragment();
		mUserFragment
				.setDrawStats(new UserStats())
				.setSelfBindingEnabled(true)
				.setEntityId(mEntityId)
				.setTitleResId(R.string.label_profile_title);

		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_holder, mUserFragment).commit();
	}
}