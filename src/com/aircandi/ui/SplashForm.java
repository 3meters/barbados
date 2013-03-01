package com.aircandi.ui;

import java.io.InputStream;
import java.util.Properties;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.aircandi.Aircandi;
import com.aircandi.CandiConstants;
import com.aircandi.R;
import com.aircandi.components.CommandType;
import com.aircandi.components.FontManager;
import com.aircandi.components.IntentBuilder;
import com.aircandi.components.Logger;
import com.aircandi.components.NetworkManager;
import com.aircandi.components.ProxiManager;
import com.aircandi.components.ProxiManager.ModelResult;
import com.aircandi.components.Tracker;
import com.aircandi.components.bitmaps.BitmapManager;
import com.aircandi.service.ProxibaseService;
import com.aircandi.service.ProxibaseService.ServiceDataType;
import com.aircandi.service.objects.Session;
import com.aircandi.service.objects.User;
import com.aircandi.ui.user.RegisterForm;
import com.aircandi.ui.user.SignInForm;
import com.aircandi.utilities.AnimUtils;
import com.aircandi.utilities.AnimUtils.TransitionType;
import com.amazonaws.auth.BasicAWSCredentials;
import com.google.analytics.tracking.android.GoogleAnalytics;

public class SplashForm extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Aircandi.stopwatch3.start("sherlockActivity onCreate start");
		super.onCreate(savedInstanceState);
		Aircandi.stopwatch3.stop("sherlockActivity onCreate stop");
		/*
		 * Used by other activities to determine if they were launched normally or auto launched after a crash
		 */
		Aircandi.getInstance().setLaunchedNormally(true);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		if (Aircandi.firstStartApp) {
			initializeApp();
		}

		signinAuto();
		if (!isFinishing()) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.splash);
			initialize();
		}
	}

	private void initializeApp() {

		Aircandi.stopwatch3.start("initializeApp function start");
		if (Build.PRODUCT.contains("sdk")) {
			Aircandi.usingEmulator = true;
		}
		/* Tickle the bitmap manager to get it initialized */
		BitmapManager.getInstance();

		/* AWS Credentials */
		startGetAWSCredentials();
		
		/* Google analytics tracking */
		GoogleAnalytics.getInstance(this).setDebug(false);

		/* Connectivity monitoring */
		NetworkManager.getInstance().setContext(getApplicationContext());
		NetworkManager.getInstance().initialize();

		/* Proxibase sdk components */
		ProxiManager.getInstance().setContext(getApplicationContext());
		Aircandi.getInstance().setUsingEmulator(Aircandi.usingEmulator);
		ProxiManager.getInstance().initialize();

		/* Cache categories - we delay until after the initial rush for data */
//		Aircandi.mainThreadHandler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				loadCategories();
//			}
//		}, 60000);

		Aircandi.firstStartApp = false;
		Aircandi.stopwatch3.stop("initializeApp function stop");
	}

	private void initialize() {
		((ImageView) findViewById(R.id.image_background)).setBackgroundResource(R.drawable.img_splash_v);

		FontManager.getInstance().setTypefaceDefault((TextView) findViewById(R.id.splash_title));
		FontManager.getInstance().setTypefaceDefault((TextView) findViewById(R.id.splash_message));
		FontManager.getInstance().setTypefaceDefault((TextView) findViewById(R.id.button_signup));
		FontManager.getInstance().setTypefaceDefault((TextView) findViewById(R.id.button_signin));
	}

	private void signinAuto() {

		final String jsonUser = Aircandi.settings.getString(CandiConstants.SETTING_USER, null);
		final String jsonSession = Aircandi.settings.getString(CandiConstants.SETTING_USER_SESSION, null);

		if (jsonUser != null && jsonSession != null) {
			Logger.i(this, "Auto sign in...");
			final User user = (User) ProxibaseService.convertJsonToObjectInternalSmart(jsonUser, ServiceDataType.User);
			user.session = (Session) ProxibaseService.convertJsonToObjectInternalSmart(jsonSession, ServiceDataType.Session);
			Tracker.startNewSession();
			Tracker.sendEvent("action", "signin_auto", null, 0);
			Aircandi.getInstance().setUser(user);
			startMainApp();
		}
	}

	private void startMainApp() {
		final Intent intent = new Intent(this, CandiRadar.class);
		startActivity(intent);
		finish();
	}

	@SuppressWarnings("unused")
	private void loadCategories() {
		new AsyncTask() {

			@Override
			protected Object doInBackground(Object... params) {
				Thread.currentThread().setName("LoadCategories");
				ModelResult result = new ModelResult();
				if (ProxiManager.getInstance().getEntityModel().getCategories().size() == 0) {
					result = ProxiManager.getInstance().getEntityModel().loadCategories();
				}
				return result;
			}
		}.execute();
	}

	private void startGetAWSCredentials() {
		final Thread t = new Thread() {

			@Override
			public void run() {
				Thread.currentThread().setName("GetAwsCredentials");
				try {
					final Properties properties = new Properties();
					final InputStream inputStream = getClass().getResourceAsStream("/com/aircandi/aws.properties");
					properties.load(inputStream);

					final String accessKeyId = properties.getProperty("accessKey");
					final String secretKey = properties.getProperty("secretKey");

					if ((accessKeyId == null) || (accessKeyId.equals(""))
							|| (accessKeyId.equals("CHANGEME"))
							|| (secretKey == null)
							|| (secretKey.equals(""))
							|| (secretKey.equals("CHANGEME"))) {
						Logger.e(SplashForm.this, "Aws Credentials not configured correctly.");
					}
					else {
						Aircandi.awsCredentials = new BasicAWSCredentials(properties.getProperty("accessKey"), properties.getProperty("secretKey"));
					}
				}
				catch (Exception exception) {
					Logger.e(SplashForm.this, exception.getMessage(), exception);
				}
			}
		};
		t.start();
	}

	// --------------------------------------------------------------------------------------------
	// Event routines
	// --------------------------------------------------------------------------------------------

	@SuppressWarnings("ucd")
	public void onSigninButtonClick(View view) {
		final IntentBuilder intentBuilder = new IntentBuilder(this, SignInForm.class);
		final Intent intent = intentBuilder.create();
		startActivityForResult(intent, CandiConstants.ACTIVITY_SIGNIN);
		AnimUtils.doOverridePendingTransition(this, TransitionType.PageToForm);
	}

	@SuppressWarnings("ucd")
	public void onSignupButtonClick(View view) {
		final IntentBuilder intentBuilder = new IntentBuilder(this, RegisterForm.class);
		intentBuilder.setCommandType(CommandType.New);
		final Intent intent = intentBuilder.create();
		startActivityForResult(intent, CandiConstants.ACTIVITY_SIGNIN);
		AnimUtils.doOverridePendingTransition(this, TransitionType.PageToForm);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == CandiConstants.ACTIVITY_SIGNIN) {
			if (resultCode == CandiConstants.RESULT_USER_SIGNED_IN) {
				startMainApp();
			}
		}
	}
}