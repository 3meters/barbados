package com.aircandi.barbados.components;

import com.aircandi.R;
import com.aircandi.barbados.Barbados;

public class MediaManager extends com.aircandi.components.MediaManager {

	public static Integer		SOUND_CANDIGRAM_EXIT;

	@Override
	public MediaManager initSoundPool() {
		super.initSoundPool();
		SOUND_CANDIGRAM_EXIT = soundPool.load(Barbados.applicationContext, R.raw.candigram_exit, 1);
		return this;
	}
}
