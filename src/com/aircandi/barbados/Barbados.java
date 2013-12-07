package com.aircandi.barbados;

import com.aircandi.Aircandi;
import com.aircandi.R;
import com.aircandi.barbados.components.MenuManager;
import com.aircandi.barbados.objects.Links;
import com.aircandi.components.EntityManager;
import com.aircandi.utilities.Strings;
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
		openContainer(Strings.getString(R.string.id_container), RefreshMode.STANDARD);
	}

	@Override
	protected void configure() {
		EntityManager.getInstance().setLinks(new Links());
		mMenuManager = new MenuManager();
	}
}
