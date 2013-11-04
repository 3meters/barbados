package com.aircandi.ui.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.aircandi.Aircandi;
import com.aircandi.Constants;
import com.aircandi.R;
import com.aircandi.service.objects.Applink;
import com.aircandi.ui.base.BaseEntityEdit;
import com.aircandi.utilities.Dialogs;
import com.aircandi.utilities.Routing;
import com.aircandi.utilities.Type;
import com.aircandi.utilities.UI;
import com.aircandi.utilities.Utilities;

public class ApplinkEdit extends BaseEntityEdit {

	private Spinner			mTypePicker;
	private TextView		mAppId;
	private TextView		mAppUrl;

	private Integer			mSpinnerItemResId;
	private Integer			mMissingResId;

	private List<String>	mApplinkSearchStrings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	}

	@Override
	public void initialize(Bundle savedInstanceState) {
		super.initialize(savedInstanceState);

		mTypePicker = (Spinner) findViewById(R.id.type_picker);
		mAppId = (EditText) findViewById(R.id.app_id);
		mAppUrl = (EditText) findViewById(R.id.app_url);

		mSpinnerItemResId = getThemeTone().equals("dark") ? R.layout.spinner_item_dark : R.layout.spinner_item_light;

		/* Turn on test button if user has something to test */

		if (mAppId != null) {
			mAppId.addTextChangedListener(new SimpleTextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
					if (((Applink) mEntity).appId == null || !s.toString().equals(((Applink) mEntity).appId)) {
						if (!mFirstDraw) {
							mDirty = true;
						}
					}
				}
			});
		}

		if (mAppUrl != null) {
			mAppUrl.addTextChangedListener(new SimpleTextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
					if (((Applink) mEntity).appUrl == null || !s.toString().equals(((Applink) mEntity).appUrl)) {
						if (!mFirstDraw) {
							mDirty = true;
						}
					}
				}
			});
		}
		bind(BindingMode.AUTO);
	}

	@Override
	public void draw() {

		final Applink applink = (Applink) mEntity;

		/* Spinners */
		if (mEditing) {
			UI.setVisibility(findViewById(R.id.type), View.VISIBLE);
			UI.setVisibility(findViewById(R.id.type_picker), View.GONE);

			((TextView) findViewById(R.id.type)).setText("link type: " + applink.type);
		}
		else {
			UI.setVisibility(findViewById(R.id.type), View.GONE);
			UI.setVisibility(findViewById(R.id.type_picker), View.VISIBLE);

			if (mTypePicker.getAdapter() == null) {
				mApplinkSearchStrings = new ArrayList<String>();
				mApplinkSearchStrings.add("website");
				mApplinkSearchStrings.add("facebook");
				mApplinkSearchStrings.add("twitter");
				mApplinkSearchStrings.add("email");
				mApplinkSearchStrings.add(getString(R.string.form_applink_type_hint));
				initializeTypeSpinner(mApplinkSearchStrings);
			}
		}
		if (applink.type != null) {

			UI.setVisibility(findViewById(R.id.app_id_holder), View.GONE);
			UI.setVisibility(findViewById(R.id.app_url_holder), View.GONE);

			if (applink.type.equals(Constants.TYPE_APP_WEBSITE)) {
				UI.setVisibility(findViewById(R.id.app_url_holder), View.VISIBLE);
				mMissingResId = R.string.error_missing_applink_url_website;
			}
			else if (applink.type.equals(Constants.TYPE_APP_EMAIL)) {
				UI.setVisibility(findViewById(R.id.app_id_holder), View.VISIBLE);
				mMissingResId = R.string.error_missing_applink_id_email;
			}
			else if (applink.type.equals(Constants.TYPE_APP_FACEBOOK)) {
				UI.setVisibility(findViewById(R.id.app_id_holder), View.VISIBLE);
				mMissingResId = R.string.error_missing_applink_id_facebook;
			}
			else if (applink.type.equals(Constants.TYPE_APP_TWITTER)) {
				UI.setVisibility(findViewById(R.id.app_id_holder), View.VISIBLE);
				mMissingResId = R.string.error_missing_applink_id_twitter;
			}
			if (applink.appId != null && !applink.appId.equals("")) {
				if (mAppId != null) {
					mAppId.setText(applink.appId);
				}
			}
			if (applink.appUrl != null && !applink.appUrl.equals("")) {
				if (mAppUrl != null) {
					mAppUrl.setText(applink.appUrl);
				}
			}

			drawPhoto();
		}
		super.draw();
	}

	private void initializeTypeSpinner(final List<String> items) {

		final ArrayAdapter adapter = new ArrayAdapter(this, mSpinnerItemResId, items) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				final View view = super.getView(position, convertView, parent);

				final TextView text = (TextView) view.findViewById(R.id.spinner_name);
				if (getThemeTone().equals("dark")) {
					if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
						text.setTextColor(Aircandi.getInstance().getResources().getColor(R.color.text_dark));
					}
				}

				if (position == getCount()) {
					((TextView) view.findViewById(R.id.spinner_name)).setText("");
					((TextView) view.findViewById(R.id.spinner_name)).setHint(items.get(getCount())); //"Hint to be displayed"
				}

				return view;
			}

			@Override
			public int getCount() {
				return super.getCount() - 1; // dont display last item. It is used as hint.
			}
		};

		if (getThemeTone().equals("dark")) {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				adapter.setDropDownViewResource(R.layout.spinner_item_light);
			}
		}

		mTypePicker.setAdapter(adapter);

		if (!mEditing) {
			mTypePicker.setSelection(adapter.getCount());
		}

		mTypePicker.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				if (getThemeTone().equals("dark")) {
					if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
						((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.text_light));
					}
				}

				/* Do nothing when the hint item is selected */
				if (position != parent.getCount()) {
					if (position < mApplinkSearchStrings.size()) {
						final String sourceType = mApplinkSearchStrings.get(position);
						setEntityType(sourceType);
						draw();
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}

		});
	}

	@Override
	protected void setEntityType(String type) {
		super.setEntityType(type);
		if (mEntity.data == null) {
			mEntity.data = new HashMap<String, Object>();
		}
		mEntity.data.put("origin", "user");
		((Applink) mEntity).appId = null;
		((Applink) mEntity).appUrl = null;
	}

	// --------------------------------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------------------------------

	@SuppressWarnings("ucd")
	public void onTestButtonClick() {
		if (validate()) {
			gather();
			Routing.shortcut(this, mEntity.getShortcut(), null, null);
		}
	}

	// --------------------------------------------------------------------------------------------
	// Methods
	// --------------------------------------------------------------------------------------------

	@Override
	protected void gather() {
		super.gather();

		Applink applink = (Applink) mEntity;

		if (mAppId != null) {
			applink.appId = Type.emptyAsNull(mAppId.getText().toString().trim());
		}

		if (mAppUrl != null) {
			applink.appUrl = Type.emptyAsNull(mAppUrl.getText().toString().trim());
			if (applink.appUrl != null && applink.type.equals(Constants.TYPE_APP_WEBSITE)) {
				String appUrl = applink.appUrl;
				if (!appUrl.startsWith("http://") && !appUrl.startsWith("https://")) {
					applink.appUrl = "http://" + appUrl;
				}
			}
		}
	}

	@Override
	protected String getLinkType() {
		return Constants.TYPE_LINK_CONTENT;
	};

	@Override
	protected boolean validate() {
		if (!super.validate()) {
			return false;
		}
		if (mEntity.type == null) {
			Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
					, null
					, getResources().getString(R.string.error_missing_applink_type)
					, null
					, this
					, android.R.string.ok
					, null, null, null, null);
			return false;
		}

		//		if (mName != null && mName.getText().length() == 0) {
		//			Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
		//					, null
		//					, getResources().getString(R.string.error_missing_entity_label)
		//					, null
		//					, this
		//					, android.R.string.ok
		//					, null, null, null, null);
		//			return false;
		//		}

		if (!mEditing && mAppId != null && mAppId.getText().length() == 0 && findViewById(R.id.app_id_holder).getVisibility() == View.VISIBLE) {
			Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
					, null
					, getResources().getString(mMissingResId)
					, null
					, this
					, android.R.string.ok
					, null, null, null, null);
			return false;
		}

		if (!mEditing && mAppUrl != null && findViewById(R.id.app_url_holder).getVisibility() == View.VISIBLE) {
			final String url = mAppUrl.getEditableText().toString();
			if (url == null || url.length() == 0) {
				Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
						, null
						, getResources().getString(mMissingResId)
						, null
						, this
						, android.R.string.ok
						, null, null, null, null);
				return false;
			}
			else if (url != null && url.length() > 0 && !Utilities.validWebUri(url)) {
				Dialogs.alertDialog(android.R.drawable.ic_dialog_alert
						, null
						, getResources().getString(R.string.error_weburi_invalid)
						, null
						, this
						, android.R.string.ok
						, null, null, null, null);
				return false;
			}
		}
		return true;
	}

	// --------------------------------------------------------------------------------------------
	// Misc
	// --------------------------------------------------------------------------------------------

	@Override
	protected int getLayoutId() {
		return R.layout.applink_edit;
	}
}