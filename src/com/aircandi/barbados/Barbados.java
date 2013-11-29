package com.aircandi.barbados;

import com.aircandi.Aircandi;
import com.aircandi.barbados.components.MenuManager;
import com.aircandi.barbados.objects.Links;
import com.aircandi.components.EntityManager;


public class Barbados extends Aircandi {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void initializeInstance() {
		super.initializeInstance();
		
		/* Must have this so activity rerouting works. */
		Aircandi.applicationContext = getApplicationContext(); 
		
		/* Configure */
		EntityManager.getInstance().setLinks(new Links());
		mMenuManager = new MenuManager();
	}
}
