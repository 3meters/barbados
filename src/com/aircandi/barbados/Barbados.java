package com.aircandi.barbados;

import com.aircandi.Aircandi;
import com.aircandi.barbados.components.DispatchManager;
import com.aircandi.components.StringManager;
import com.google.tagmanager.TagManager.RefreshMode;

public class Barbados extends Aircandi {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void initializeInstance() {
		/* Must have this so activity rerouting works. */
		Aircandi.applicationContext = getApplicationContext();
		super.initializeInstance();

		/* Inject configuration */
		openContainer(StringManager.getString(R.string.id_container), RefreshMode.STANDARD);
		
		/* Inject dispatch manager */
		Aircandi.dispatch = new DispatchManager();
		
	}
}
