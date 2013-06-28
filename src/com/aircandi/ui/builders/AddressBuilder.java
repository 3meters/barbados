package com.aircandi.ui.builders;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.MenuItem;
import com.aircandi.Constants;
import com.aircandi.beta.R;
import com.aircandi.components.FontManager;
import com.aircandi.service.HttpService;
import com.aircandi.service.HttpService.ServiceDataType;
import com.aircandi.service.objects.Place;
import com.aircandi.ui.base.FormActivity;
import com.aircandi.utilities.MiscUtils;

public class AddressBuilder extends FormActivity {

	private Place	mPlace;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!isFinishing()) {
			initialize();
			draw();
		}
	}

	private void initialize() {
		final Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			final String jsonAddress = extras.getString(Constants.EXTRA_PLACE);
			if (jsonAddress != null) {
				mPlace = (Place) HttpService.convertJsonToObjectInternalSmart(jsonAddress, ServiceDataType.Place);
			}
		}

		if (mPlace == null) {
			mPlace = new Place();
		}

		mCommon.mActionBar.setDisplayHomeAsUpEnabled(true);
		mCommon.mActionBar.setTitle(R.string.dialog_address_builder_title);

		((EditText) findViewById(R.id.phone)).setImeOptions(EditorInfo.IME_ACTION_DONE);
		((EditText) findViewById(R.id.phone)).setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					gather();
					doSave();
					return true;
				}
				return false;
			}
		});

		FontManager.getInstance().setTypefaceDefault((TextView) findViewById(R.id.title));
		FontManager.getInstance().setTypefaceDefault((EditText) findViewById(R.id.phone));
		FontManager.getInstance().setTypefaceDefault((EditText) findViewById(R.id.address));
		FontManager.getInstance().setTypefaceDefault((EditText) findViewById(R.id.city));
		FontManager.getInstance().setTypefaceDefault((EditText) findViewById(R.id.state));
		FontManager.getInstance().setTypefaceDefault((EditText) findViewById(R.id.zip_code));
	}

	private void draw() {
		/* Author */
		if (mPlace.address != null) {
			((EditText) findViewById(R.id.address)).setText(mPlace.address);
		}
		if (mPlace.city != null) {
			((EditText) findViewById(R.id.city)).setText(mPlace.city);
		}
		if (mPlace.region != null) {
			((EditText) findViewById(R.id.state)).setText(mPlace.region);
		}
		if (mPlace.postalCode != null) {
			((EditText) findViewById(R.id.zip_code)).setText(mPlace.postalCode);
		}
		if (mPlace.phone != null) {
			((EditText) findViewById(R.id.phone)).setText(mPlace.phone);
		}
	}

	// --------------------------------------------------------------------------------------------
	// Event routines
	// --------------------------------------------------------------------------------------------

	private void gather() {
		mPlace.phone = MiscUtils.emptyAsNull(((EditText) findViewById(R.id.phone)).getEditableText().toString());
		mPlace.address = MiscUtils.emptyAsNull(((EditText) findViewById(R.id.address)).getEditableText().toString());
		mPlace.city = MiscUtils.emptyAsNull(((EditText) findViewById(R.id.city)).getEditableText().toString());
		mPlace.region = MiscUtils.emptyAsNull(((EditText) findViewById(R.id.state)).getEditableText().toString());
		mPlace.postalCode = MiscUtils.emptyAsNull(((EditText) findViewById(R.id.zip_code)).getEditableText().toString());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == Constants.ACTIVITY_SIGNIN) {
			if (resultCode == Activity.RESULT_CANCELED) {
				setResult(resultCode);
				finish();
			}
			else {
				initialize();
				draw();
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, intent);
		}
	}

	// --------------------------------------------------------------------------------------------
	// Service routines
	// --------------------------------------------------------------------------------------------

	private void doSave() {
		final Intent intent = new Intent();
		if (mPlace != null) {
			final String jsonAddress = HttpService.convertObjectToJsonSmart(mPlace, false, true);
			intent.putExtra(Constants.EXTRA_PLACE, jsonAddress);
		}
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	// --------------------------------------------------------------------------------------------
	// Application menu routines (settings)
	// --------------------------------------------------------------------------------------------

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.accept) {
			gather();
			doSave();
			return true;
		}

		/* In case we add general menu items later */
		mCommon.doOptionsItemSelected(item);
		return true;
	}

	// --------------------------------------------------------------------------------------------
	// Misc routines
	// --------------------------------------------------------------------------------------------

	@Override
	protected Boolean isDialog() {
		return false;
	}

	@Override
	protected int getLayoutId() {
		return R.layout.builder_address;
	}
}