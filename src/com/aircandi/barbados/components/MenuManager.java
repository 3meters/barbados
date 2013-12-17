package com.aircandi.barbados.components;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.aircandi.Aircandi;
import com.aircandi.barbados.R;
import com.aircandi.utilities.Strings;
import com.aircandi.utilities.Type;

public class MenuManager extends com.aircandi.components.MenuManager {

	@Override
	public boolean onCreateOptionsMenu(Activity activity, Menu menu) {

		String activityName = activity.getClass().getSimpleName();
		final SherlockFragmentActivity sherlock = (SherlockFragmentActivity) activity;
		MenuInflater menuInflater = sherlock.getSupportMenuInflater();

		if (activityName.equals("PlaceFormExtended")) {
			menuInflater.inflate(R.menu.menu_base, menu);
			menuInflater.inflate(R.menu.menu_browse_place, menu);
			if (Type.isTrue(Aircandi.getInstance().getCurrentUser().developer)
					&& Aircandi.settings.getBoolean(Strings.getString(R.string.pref_enable_dev), false)) {
				menuInflater.inflate(R.menu.menu_browse_entity_dev, menu);
			}
			menuInflater.inflate(R.menu.menu_help, menu);
			return true;
		}
		else if (activityName.equals("ProfileFormExtended")) {
			menuInflater.inflate(R.menu.menu_base, menu);
			menuInflater.inflate(R.menu.menu_browse_profile, menu);
			return true;
		}
		else if (activityName.equals("UserFormExtended")) {
			menuInflater.inflate(R.menu.menu_base, menu);
			menuInflater.inflate(R.menu.menu_browse_user, menu);
			return true;
		}
		else {
			return (super.onCreateOptionsMenu(activity, menu));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Fragment fragment, Menu menu, MenuInflater inflater) {

		if (fragment != null) {

			String fragmentName = fragment.getClass().getSimpleName();
			MenuInflater menuInflater = inflater;

			if (fragmentName.equals("RadarFragment")) {
				menuInflater.inflate(R.menu.menu_browse_radar, menu);
				menuInflater.inflate(R.menu.menu_help, menu);
				return true;
			}
			else if (fragmentName.equals("ActivityFragmentExtended")) {
				menuInflater.inflate(R.menu.menu_browse_activity_list, menu);
				return true;
			}
			else {
				return (super.onCreateOptionsMenu(fragment, menu, inflater));
			}
		}
		return false;
	}
}
