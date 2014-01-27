package com.aircandi.barbados.components;

import com.aircandi.Aircandi;
import com.aircandi.barbados.Constants;
import com.aircandi.barbados.R;
import com.aircandi.components.StringManager;
import com.aircandi.utilities.Booleans;

public class PreferenceManager extends com.aircandi.components.PreferenceManager {

	@Override
	public Boolean notificationEnabled(String triggerCategory, String entitySchema) {

		if (entitySchema.equals(Constants.SCHEMA_ENTITY_CANDIGRAM)) {

			if (!Aircandi.settings.getBoolean(StringManager.getString(R.string.pref_messages_candigrams),
					Booleans.getBoolean(R.bool.pref_notifications_candigrams_default)))
				return false;
		}
		return super.notificationEnabled(triggerCategory, entitySchema);
	}
}
