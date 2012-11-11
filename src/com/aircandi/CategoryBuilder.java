package com.aircandi;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.aircandi.components.ImageRequest;
import com.aircandi.components.ImageRequestBuilder;
import com.aircandi.components.NetworkManager.ResponseCode;
import com.aircandi.components.ProxiExplorer;
import com.aircandi.components.ProxiExplorer.ModelResult;
import com.aircandi.service.ProxibaseService;
import com.aircandi.service.ProxibaseService.ServiceDataType;
import com.aircandi.service.objects.Category;
import com.aircandi.service.objects.Entity.ImageFormat;
import com.aircandi.widgets.WebImageView;

public class CategoryBuilder extends FormActivity {

	private Category		mOriginalCategory;

	private WebImageView	mImage;
	private Spinner			mSpinnerCategory;
	private Spinner			mSpinnerSubcategory;

	private Category		mCategory;
	private Integer			mCategoryIndex;
	private Category		mSubcategory;
	private Integer			mSubcategoryIndex;

	private List<Category>	mCategories;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize();
	}

	private void initialize() {
		Bundle extras = this.getIntent().getExtras();
		if (extras != null) {
			String jsonCategory = extras.getString(getString(R.string.EXTRA_CATEGORY));
			if (jsonCategory != null) {
				mOriginalCategory = (Category) ProxibaseService.convertJsonToObjectInternalSmart(jsonCategory, ServiceDataType.Category);
			}
		}
		mImage = (WebImageView) findViewById(R.id.image);
		mSpinnerCategory = (Spinner) findViewById(R.id.category);
		mSpinnerSubcategory = (Spinner) findViewById(R.id.sub_category);

		if (ProxiExplorer.getInstance().getEntityModel().getCategories().size() == 0) {
			loadCategories();
		}
		else {
			mCategories = ProxiExplorer.getInstance().getEntityModel().getCategories();
			if (mCategories != null) {
				if (mOriginalCategory != null) {
					setCategoryIndexes();
				}
				initializeSpinners();
			}
		}
	}

	public void loadCategories() {
		new AsyncTask<Object, Object, Object>() {

			@Override
			protected Object doInBackground(Object... params) {
				ModelResult result = ProxiExplorer.getInstance().getEntityModel().loadCategories();
				return result;
			}

			@Override
			protected void onPostExecute(Object response) {
				ModelResult result = (ModelResult) response;
				if (result.serviceResponse.responseCode == ResponseCode.Success) {
					mCategories = ProxiExplorer.getInstance().getEntityModel().getCategories();
					if (mCategories != null) {
						if (mOriginalCategory != null) {
							setCategoryIndexes();
						}
						initializeSpinners();
					}
				}
			}

		}.execute();
	}

	// --------------------------------------------------------------------------------------------
	// Event routines
	// --------------------------------------------------------------------------------------------

	public void onSaveButtonClick(View view) {
		gather();
		doSave();
	}

	private void gather() {}

	@Override
	protected Boolean isDialog() {
		return true;
	}

	@Override
	protected Integer getThemeResId() {
		return R.style.aircandi_theme_dialog_light;
	}

	// --------------------------------------------------------------------------------------------
	// Service routines
	// --------------------------------------------------------------------------------------------

	private void doSave() {
		Intent intent = new Intent();
		if (mSubcategory != null) {
			String jsonCategory = ProxibaseService.convertObjectToJsonSmart(mSubcategory, false, true);
			intent.putExtra(getString(R.string.EXTRA_CATEGORY), jsonCategory);
		}
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	// --------------------------------------------------------------------------------------------
	// Misc routines
	// --------------------------------------------------------------------------------------------

	private void setCategoryIndexes() {
		int categoryIndex = 0;
		for (Category category : mCategories) {
			int subcategoryIndex = 0;
			for (Category subcategory : category.categories) {
				if (subcategory.name.equals(mOriginalCategory.name)) {
					mCategoryIndex = categoryIndex;
					mSubcategoryIndex = subcategoryIndex;
					return;
				}
				subcategoryIndex++;
			}
			categoryIndex++;
		}
	}

	private void initializeSpinners() {
		final List<String> categories = ProxiExplorer.getInstance().getEntityModel().getCategoriesAsStrings(mCategories);
		categories.add(getString(R.string.form_place_category_hint));

		ArrayAdapter adapter = new ArrayAdapter(this, R.layout.sherlock_spinner_item, categories) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				View view = super.getView(position, convertView, parent);

				if (position == getCount()) {
					((TextView) view.findViewById(android.R.id.text1)).setText("");
					((TextView) view.findViewById(android.R.id.text1)).setHint(categories.get(getCount())); //"Hint to be displayed"
				}

				return view;
			}

			@Override
			public int getCount() {
				return super.getCount() - 1; // you dont display last item. It is used as hint.
			}
		};
		adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		mSpinnerCategory.setAdapter(adapter);
		if (mOriginalCategory == null) {
			mSpinnerCategory.setSelection(adapter.getCount());
		}
		mSpinnerCategory.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				/* Do nothing when the hint item is selected */
				if (position < mCategories.size()) {

					mCategory = (Category) mCategories.get(position);
					if (mCategory.iconUri() != null) {
						updateCustomImage(mCategory.iconUri());
					}
					final List<String> categories = ProxiExplorer.getInstance().getEntityModel().getCategoriesAsStrings(mCategory.categories);
					categories.add(getString(R.string.form_place_sub_category_hint));

					ArrayAdapter adapter = new ArrayAdapter(CategoryBuilder.this, R.layout.sherlock_spinner_item, categories) {
						@Override
						public View getView(int position, View convertView, ViewGroup parent) {

							View view = super.getView(position, convertView, parent);

							if (position == getCount()) {
								((TextView) view.findViewById(android.R.id.text1)).setText("");
								((TextView) view.findViewById(android.R.id.text1)).setHint(categories.get(getCount())); //"Hint to be displayed"
							}

							return view;
						}

						@Override
						public int getCount() {
							return super.getCount() - 1; // you dont display last item. It is used as hint.
						}
					};

					adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

					mSpinnerSubcategory.setVisibility(View.VISIBLE);
					mSpinnerSubcategory.setAdapter(adapter);

					if (mOriginalCategory == null) {
						mSpinnerSubcategory.setSelection(adapter.getCount());
					}
					mSpinnerSubcategory.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
							/* Do nothing when the hint item is selected */
							if (position < mCategory.categories.size()) {
								mSubcategory = (Category) mCategory.categories.get(position);
								if (mSubcategory.iconUri() != null) {
									updateCustomImage(mSubcategory.iconUri());
								}
							}
						}

						@Override
						public void onNothingSelected(AdapterView<?> parent) {}
					});
					if (mOriginalCategory != null) {
						mSpinnerSubcategory.setSelection(mSubcategoryIndex);
						mOriginalCategory = null;
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});

		if (mOriginalCategory != null) {
			mSpinnerCategory.setSelection(mCategoryIndex);
		}
	}

	public void updateCustomImage(String uri) {
		ImageRequestBuilder builder = new ImageRequestBuilder(mImage)
				.setImageUri(uri)
				.setImageFormat(ImageFormat.Binary);

		final ImageRequest imageRequest = builder.create();
		mImage.setImageRequest(imageRequest);
	}

	@Override
	protected int getLayoutID() {
		return R.layout.category_builder;
	}
}