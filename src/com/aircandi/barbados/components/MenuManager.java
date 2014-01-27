package com.aircandi.barbados.components;

import android.app.Activity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.aircandi.Aircandi;
import com.aircandi.barbados.Constants;
import com.aircandi.barbados.R;
import com.aircandi.barbados.objects.Candigram;
import com.aircandi.barbados.objects.Route;
import com.aircandi.components.StringManager;
import com.aircandi.objects.Entity;
import com.aircandi.utilities.Type;

public class MenuManager extends com.aircandi.components.MenuManager {

	@Override
	public boolean onCreateOptionsMenu(Activity activity, Menu menu) {

		String activityName = activity.getClass().getSimpleName();
		final SherlockFragmentActivity sherlock = (SherlockFragmentActivity) activity;
		MenuInflater menuInflater = sherlock.getSupportMenuInflater();

		if (activityName.equals("PlaceForm")) {
			menuInflater.inflate(R.menu.menu_refresh, menu);
			menuInflater.inflate(R.menu.menu_add, menu);
			menuInflater.inflate(R.menu.menu_base, menu);

			if (Type.isTrue(Aircandi.getInstance().getCurrentUser().developer)
					&& Aircandi.settings.getBoolean(StringManager.getString(R.string.pref_enable_dev), false)) {
				menuInflater.inflate(R.menu.menu_save_location, menu);
			}

			menuInflater.inflate(R.menu.menu_help, menu);
			return true;			
		}
		else if (activityName.equals("CandigramForm")) {
			menuInflater.inflate(R.menu.menu_refresh, menu);
			menuInflater.inflate(R.menu.menu_add, menu);
			menuInflater.inflate(R.menu.menu_edit, menu);
			menuInflater.inflate(R.menu.menu_base, menu);
			menuInflater.inflate(R.menu.menu_help, menu);
			return true;
		}
		else if (activityName.contains("CandigramWizard")) {
			menuInflater.inflate(R.menu.menu_cancel, menu);
			return true;
		}
		else {
			return (super.onCreateOptionsMenu(activity, menu));
		}
	}
	
	@Override
	public  Boolean showAction(Integer route, Entity entity) {
		if (route == Route.ADD) {
			if (entity != null && (entity.schema.equals(Constants.SCHEMA_ENTITY_CANDIGRAM))) 
				return true;
		}
		else if (route == Route.KICK) 
			return canUserKick(entity);

		return super.showAction(route, entity);
	}	
	
	
	public  Boolean canUserKick(Entity entity) {
		if (entity == null || !(entity instanceof Candigram)) return false;
	
		/* Current user is owner */
		if (entity.isOwnedByCurrentUser() || entity.isOwnedBySystem()) return true;
	
		/* Nearby */
		Entity parent = entity.getParent(Constants.TYPE_LINK_CONTENT, Constants.SCHEMA_ENTITY_PLACE);
		if (parent != null && parent.hasActiveProximity()) return true;
	
		/* Not owner and nearby */
		return false;
	}
}
