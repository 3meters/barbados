package com.aircandi.components;

import android.app.Activity;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.aircandi.R;

public class MenuManager {

	public static boolean onCreateOptionsMenu(Activity activity, Menu menu) {

		/* Browsing */

		String activityName = activity.getClass().getSimpleName();
		final SherlockFragmentActivity sherlock = (SherlockFragmentActivity) activity;
		MenuInflater menuInflater = sherlock.getSupportMenuInflater();

		/* Editing */

		if (activityName.equals("CommentEdit")
				|| activityName.equals("ReportEdit")
				|| activityName.equals("FeedbackEdit")
				|| activityName.equals("InviteEdit")) {
			menuInflater.inflate(R.menu.menu_send, menu);
		}
		else if (activityName.equals("ApplinkListEdit")) {
			menuInflater.inflate(R.menu.menu_edit_list, menu);
		}
		else if (activityName.equals("ApplinkEdit")) {
			menuInflater.inflate(R.menu.menu_edit_applink, menu);
		}
		else if (activityName.equals("UserEdit")
				|| activityName.equals("PasswordEdit")
				|| activityName.equals("TuningEdit")
				|| activityName.contains("SignInEdit")) {
			menuInflater.inflate(R.menu.menu_edit_builder, menu);
		}
		else if (activityName.contains("CandigramWizard")) {
			menuInflater.inflate(R.menu.menu_cancel, menu);
		}
		else if (activityName.contains("Edit")) {
			menuInflater.inflate(R.menu.menu_edit_entity, menu);
		}
		else if (activityName.contains("Builder")) {
			menuInflater.inflate(R.menu.menu_edit_builder, menu);
		}
		else if (activityName.contains("Picker")) {
			menuInflater.inflate(R.menu.menu_cancel, menu);
		}
		else {

			/* BROWSE */

			if (activityName.equals("HelpForm") 
					|| activityName.equals("AboutForm")) {
				menuInflater.inflate(R.menu.menu_cancel, menu);
			}
			else if (activityName.equals("HelpForm")) {
				menuInflater.inflate(R.menu.menu_cancel, menu);
			}
			else {
				menuInflater.inflate(R.menu.menu_base, menu);

				if (!activityName.equals("AircandiForm")) {
					menuInflater.inflate(R.menu.menu_home, menu);
				}

				if (activityName.equals("PlaceForm")) {
					menuInflater.inflate(R.menu.menu_browse_entity, menu);
					menuInflater.inflate(R.menu.menu_help, menu);
				}
				else if (activityName.equals("PictureForm")) {
					menuInflater.inflate(R.menu.menu_browse_entity, menu);
				}
				else if (activityName.equals("PhotoForm")) {
					menuInflater.inflate(R.menu.menu_browse_photo, menu);
				}
				else if (activityName.equals("CandigramForm")) {
					menuInflater.inflate(R.menu.menu_browse_entity, menu);
					menuInflater.inflate(R.menu.menu_help, menu);
				}
				else if (activityName.equals("ProfileForm")) {
					menuInflater.inflate(R.menu.menu_browse_profile, menu);
				}
				else if (activityName.equals("UserForm")) {
					menuInflater.inflate(R.menu.menu_browse_user, menu);
				}
				else if (activityName.equals("EntityList")
						|| activityName.equals("EntityGrid")) {
					menuInflater.inflate(R.menu.menu_browse_entity_list, menu);
				}
			}
		}

		return true;
	}

	public static boolean onCreateOptionsMenu(Fragment fragment, Menu menu, MenuInflater inflater) {

		/* Fragments */

		if (fragment != null) {

			String fragmentName = fragment.getClass().getSimpleName();
			MenuInflater menuInflater = inflater;

			if (fragmentName.equals("RadarFragment")) {
				menuInflater.inflate(R.menu.menu_browse_radar, menu);
				menuInflater.inflate(R.menu.menu_help, menu);
			}
			else if (fragmentName.equals("CreatedFragment")) {
				menuInflater.inflate(R.menu.menu_browse_created, menu);
			}
			else if (fragmentName.equals("WatchingFragment")) {
				menuInflater.inflate(R.menu.menu_browse_watching, menu);
			}
			else if (fragmentName.equals("ActivityFragment")) {
				menuInflater.inflate(R.menu.menu_browse_activity_list, menu);
			}
		}

		return true;
	}
}
