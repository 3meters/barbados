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
import com.aircandi.R;
import com.aircandi.components.EntityManager;
import com.aircandi.components.Logger;
import com.aircandi.components.MessagingManager;
import com.aircandi.components.NetworkManager.ResponseCode;
import com.aircandi.components.ProximityManager.ModelResult;
import com.aircandi.service.objects.User;
import com.aircandi.ui.base.BaseEntityEdit;
import com.aircandi.utilities.Animate;
import com.aircandi.utilities.Animate.TransitionType;
import com.aircandi.utilities.Dialogs;
import com.aircandi.utilities.Errors;
import com.aircandi.utilities.Routing;
import com.aircandi.utilities.Routing.Route;
import com.aircandi.utilities.UI;
import com.aircandi.utilities.Utilities;

public class RegisterEdit extends BaseEntityEdit {

	private EditText	mEmail;
	private EditText	mPassword;
	private EditText	mPasswordConfirm;

	@Override
	public void initialize(Bundle savedInstanceState) {
		super.initialize(savedInstanceState);

		mEntitySchema = Constants.SCHEMA_ENTITY_USER;
		mEmail = (EditText) findViewById(R.id.email);
		mPassword = (EditText) findViewById(R.id.password);
		mPasswordConfirm = (EditText) findViewById(R.id.password_confirm);

		mPasswordConfirm.setImeOptions(EditorInfo.IME_ACTION_GO);
		mPasswordConfirm.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO) {
					update();
					return true;
				}
				return false;
			}
		});
	}

	// --------------------------------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------------------------------

	@SuppressWarnings("ucd")
	public void onViewTermsButtonClick(View view) {
		Routing.route(this, Route.TERMS);
	}

	@SuppressWarnings("ucd")
	public void onRegisterButtonClick(View view) {
		onAccept();
	}

	@Override
	public void onAccept() {
		if (validate()) {
			gather();
			update();
		}
	}

	// --------------------------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------------------------

	@Override
	protected String getLinkType() {
		return null;
	}

	@Override
	protected void gather() {
		super.gather();

		User user = (User) mEntity;
		user.email = mEmail.getText().toString().trim().toLowerCase(Locale.US);
		user.password = mPassword.getText().toString().trim();
	}

	// --------------------------------------------------------------------------------------------
	// Services
	// --------------------------------------------------------------------------------------------

	@Override
	protected boolean validate() {
		if (mName.getText().length() == 0) {
			Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
					, null
					, getResources().getString(R.string.error_missing_fullname)
					, null
					, this
					, android.R.string.ok
					, null, null, null, null);
			return false;
		}
		if (mEmail.getText().length() == 0) {
			Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
					, null
					, getResources().getString(R.string.error_missing_email)
					, null
					, this
					, android.R.string.ok
					, null, null, null, null);
			return false;
		}
		if (mPassword.getText().length() < 6) {
			Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
					, null
					, getResources().getString(R.string.error_missing_password)
					, null
					, this
					, android.R.string.ok
					, null, null, null, null);
			return false;
		}
		if (mPasswordConfirm.getText().length() < 6) {
			Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
					, null
					, getResources().getString(R.string.error_missing_password_confirmation)
					, null
					, this
					, android.R.string.ok
					, null, null, null, null);
			return false;
		}
		if (!Utilities.validEmail(mEmail.getText().toString())) {
			Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
					, null
					, getResources().getString(R.string.error_invalid_email)
					, null
					, this
					, android.R.string.ok
					, null, null, null, null);
			return false;
		}
		if (!mPassword.getText().toString().equals(mPasswordConfirm.getText().toString())) {
			Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
					, getResources().getString(R.string.error_signup_missmatched_passwords_title)
					, getResources().getString(R.string.error_signup_missmatched_passwords_message)
					, null
					, this
					, android.R.string.ok
					, null, null, null, null);
			mPasswordConfirm.setText("");
			return false;
		}
		return true;
	}

	@Override
	protected void update() {

		Logger.d(this, "Inserting user: " + mEntity.name);

		new AsyncTask() {

			@Override
			protected void onPreExecute() {
				mBusyManager.showBusy(R.string.progress_signing_up);
			}

			@Override
			protected Object doInBackground(Object... params) {
				Thread.currentThread().setName("InsertUser");

				ModelResult result = EntityManager.getInstance().registerUser((User) mEntity
						, mEntity.photo != null ? mEntity.photo.getBitmap() : null);

				if (result.serviceResponse.responseCode == ResponseCode.SUCCESS) {
					/*
					 * We automatically consider the user signed in.
					 */
					final User user = (User) result.data;
					Aircandi.getInstance().setCurrentUser(user);
					result = EntityManager.getInstance().activateCurrentUser();
					if (result.serviceResponse.responseCode == ResponseCode.SUCCESS) {
						result = MessagingManager.getInstance().registerInstallWithAircandi();
					}
				}
				return result;
			}

			@Override
			protected void onPostExecute(Object response) {
				final ModelResult result = (ModelResult) response;

				if (result.serviceResponse.responseCode == ResponseCode.SUCCESS) {

					Logger.i(RegisterEdit.this, "Inserted new user: " + mEntity.name + " (" + mEntity.id + ")");
					UI.showToastNotification(getString(R.string.alert_signed_in) + " " + Aircandi.getInstance().getCurrentUser().name, Toast.LENGTH_SHORT);
					setResultCode(Constants.RESULT_USER_SIGNED_IN);
					finish();
					Animate.doOverridePendingTransition(RegisterEdit.this, TransitionType.FORM_TO_PAGE);
				}
				else {
					Errors.handleError(RegisterEdit.this, result.serviceResponse);
				}
				hideBusy();
			}
		}.execute();
	}

	// --------------------------------------------------------------------------------------------
	// Misc
	// --------------------------------------------------------------------------------------------

	@Override
	protected int getLayoutId() {
		return R.layout.register_edit;
	}
}