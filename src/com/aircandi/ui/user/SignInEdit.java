package com.aircandi.ui.user;

import java.util.Locale;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.aircandi.Aircandi;
import com.aircandi.Constants;
import com.aircandi.beta.R;
import com.aircandi.components.EntityManager;
import com.aircandi.components.Logger;
import com.aircandi.components.NetworkManager.ResponseCode;
import com.aircandi.components.ProximityManager.ModelResult;
import com.aircandi.components.Tracker;
import com.aircandi.service.HttpService;
import com.aircandi.service.HttpService.ObjectType;
import com.aircandi.service.HttpService.ServiceDataWrapper;
import com.aircandi.service.objects.ServiceData;
import com.aircandi.service.objects.User;
import com.aircandi.ui.base.BaseActivity;
import com.aircandi.utilities.Animate;
import com.aircandi.utilities.Animate.TransitionType;
import com.aircandi.utilities.Dialogs;
import com.aircandi.utilities.Routing;
import com.aircandi.utilities.UI;
import com.aircandi.utilities.Utilities;

public class SignInEdit extends BaseActivity {

	private EditText	mTextEmail;
	private EditText	mTextPassword;
	private TextView	mTextMessage;

	private String		mMessage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			initialize();
			draw();
		}
	}

	@Override
	protected void unpackIntent() {

		final Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mMessage = extras.getString(Constants.EXTRA_MESSAGE);
		}
	}

	private void initialize() {
		mTextEmail = (EditText) findViewById(R.id.email);
		mTextPassword = (EditText) findViewById(R.id.password);
		mTextMessage = (TextView) findViewById(R.id.message);

		mTextPassword.setImeOptions(EditorInfo.IME_ACTION_GO);
		mTextPassword.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					doSignIn();
					return true;
				}
				return false;
			}
		});
	}

	private void draw() {
		if (mMessage != null) {
			mTextMessage.setText(mMessage);
		}
		final String email = Aircandi.settings.getString(Constants.SETTING_LAST_EMAIL, null);
		if (email != null) {
			mTextEmail.setText(email);
			mTextPassword.requestFocus();
		}
	}

	// --------------------------------------------------------------------------------------------
	// Event routines
	// --------------------------------------------------------------------------------------------

	@SuppressWarnings("ucd")
	public void onSendPasswordButtonClick(View view) {
		Dialogs.showAlertDialog(R.drawable.ic_launcher
				, getResources().getString(R.string.alert_send_password_title)
				, getResources().getString(R.string.alert_send_password_message)
				, null
				, SignInEdit.this, android.R.string.ok, null, null, null, null);
		Tracker.sendEvent("ui_action", "recover_password", null, 0, Aircandi.getInstance().getUser());
	}

	@SuppressWarnings("ucd")
	public void onSignInButtonClick(View view) {
		doSignIn();
	}

	private void doSignIn() {
		if (validate()) {

			final String email = mTextEmail.getText().toString().toLowerCase(Locale.US);
			final String password = mTextPassword.getText().toString();

			new AsyncTask() {

				@Override
				protected void onPreExecute() {
					mBusyManager.showBusy(R.string.progress_signing_in);
				}

				@Override
				protected Object doInBackground(Object... params) {
					Thread.currentThread().setName("SignIn");
					final ModelResult result = EntityManager.getInstance().signin(email, password);
					return result;
				}

				@Override
				protected void onPostExecute(Object response) {

					final ModelResult result = (ModelResult) response;
					mBusyManager.hideBusy();
					if (result.serviceResponse.responseCode == ResponseCode.Success) {

						final String jsonResponse = (String) result.serviceResponse.data;
						final ServiceData serviceData = (ServiceData) HttpService.jsonToObject(jsonResponse, ObjectType.None, ServiceDataWrapper.True);
						final User user = serviceData.user;
						user.session = serviceData.session;
						Logger.i(this, "User signed in: " + user.name + " (" + user.id + ")");

						Aircandi.getInstance().setUser(user);

						Tracker.startNewSession(Aircandi.getInstance().getUser());
						Tracker.sendEvent("ui_action", "signin_user", null, 0, Aircandi.getInstance().getUser());

						UI.showToastNotification(getResources().getString(R.string.alert_signed_in)
								+ " " + Aircandi.getInstance().getUser().name, Toast.LENGTH_SHORT);

						final String jsonUser = HttpService.objectToJson(user);
						final String jsonSession = HttpService.objectToJson(user.session);

						Aircandi.settingsEditor.putString(Constants.SETTING_USER, jsonUser);
						Aircandi.settingsEditor.putString(Constants.SETTING_USER_SESSION, jsonSession);
						Aircandi.settingsEditor.putString(Constants.SETTING_LAST_EMAIL, user.email);
						Aircandi.settingsEditor.commit();

						setResult(Constants.RESULT_USER_SIGNED_IN);
						finish();
						Animate.doOverridePendingTransition(SignInEdit.this, TransitionType.FormToPage);
					}
					else {
						Routing.serviceError(SignInEdit.this, result.serviceResponse);
					}
				}
			}.execute();
		}
	}

	private boolean validate() {
		if (mTextPassword.getText().length() < 6) {
			Dialogs.showAlertDialog(android.R.drawable.ic_dialog_alert
					, null
					, getResources().getString(R.string.error_missing_password)
					, null
					, this
					, android.R.string.ok
					, null, null, null, null);
			return false;
		}
		if (!Utilities.validEmail(mTextEmail.getText().toString())) {
			Dialogs.showAlertDialog(android.R.drawable.ic_dialog_alert
					, null
					, getResources().getString(R.string.error_invalid_email)
					, null
					, this
					, android.R.string.ok
					, null, null, null, null);
			return false;
		}
		return true;
	}

	//--------------------------------------------------------------------------------------------
	// Misc routines
	// --------------------------------------------------------------------------------------------

	@Override
	protected int getLayoutId() {
		return R.layout.signin_edit;
	}
}