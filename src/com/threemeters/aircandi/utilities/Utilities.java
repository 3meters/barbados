package com.threemeters.aircandi.utilities;

import com.threemeters.aircandi.controller.Aircandi;

import android.util.Log;

public class Utilities {

	public static void Log(String tag, String task, String message) {

		if (Aircandi.MODE_DEBUG)
			Log.d(tag, task + ": " + message);
	}
}