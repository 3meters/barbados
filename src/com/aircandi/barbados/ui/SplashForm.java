package com.aircandi.barbados.ui;

import com.aircandi.Aircandi;
import com.aircandi.barbados.Constants;
import com.aircandi.barbados.components.ActivityDecorator;
import com.aircandi.barbados.components.DispatchManager;
import com.aircandi.barbados.components.EntityManager;
import com.aircandi.barbados.components.MediaManager;
import com.aircandi.barbados.components.MenuManager;
import com.aircandi.barbados.components.ShortcutManager;
import com.aircandi.barbados.controllers.Candigrams;
import com.aircandi.barbados.controllers.Places;
import com.aircandi.barbados.controllers.Users;
import com.aircandi.barbados.objects.Links;
import com.aircandi.components.Logger;
import com.aircandi.controllers.Applinks;
import com.aircandi.controllers.Beacons;
import com.aircandi.controllers.Comments;
import com.aircandi.controllers.Maps;
import com.aircandi.controllers.Pictures;

public class SplashForm extends com.aircandi.ui.SplashForm {


	@Override
	protected void configure() {
		
		Aircandi.getInstance()
				.setMenuManager(new MenuManager())
				.setActivityDecorator(new ActivityDecorator())
				.setShortcutManager(new ShortcutManager())
				.setEntityManager(new EntityManager().setLinks(new Links()))
				.setMediaManager(new MediaManager().initSoundPool());

		Aircandi.dispatch = new DispatchManager();

		Aircandi.controllerMap.put(Constants.SCHEMA_ENTITY_APPLINK, new Applinks());
		Aircandi.controllerMap.put(Constants.SCHEMA_ENTITY_BEACON, new Beacons());
		Aircandi.controllerMap.put(Constants.SCHEMA_ENTITY_COMMENT, new Comments());
		Aircandi.controllerMap.put(Constants.SCHEMA_ENTITY_PICTURE, new Pictures());
		Aircandi.controllerMap.put(Constants.SCHEMA_ENTITY_PLACE, new Places());
		Aircandi.controllerMap.put(Constants.SCHEMA_ENTITY_USER, new Users());
		Aircandi.controllerMap.put(Constants.TYPE_APP_MAP, new Maps());
		Aircandi.controllerMap.put(Constants.SCHEMA_ENTITY_CANDIGRAM, new Candigrams());		
		
		/* Start out with anonymous user then upgrade to signed in user if possible */
		Aircandi.getInstance().initializeUser();
		
		Logger.i(this, "First run configuration completed");
	}

}