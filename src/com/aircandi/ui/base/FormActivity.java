package com.aircandi.ui.base;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockActivity;
import com.aircandi.Aircandi;
import com.aircandi.CandiConstants;
import com.aircandi.R;
import com.aircandi.components.AircandiCommon;
import com.aircandi.components.CommandType;
import com.aircandi.components.IntentBuilder;
import com.aircandi.components.Logger;
import com.aircandi.components.NetworkManager.ResponseCode;
import com.aircandi.components.NetworkManager.ServiceResponse;
import com.aircandi.components.Tracker;
import com.aircandi.components.images.BitmapManager;
import com.aircandi.components.images.BitmapRequest;
import com.aircandi.components.images.BitmapRequest.ImageResponse;
import com.aircandi.components.images.BitmapRequestBuilder;
import com.aircandi.service.ProxibaseService.RequestListener;
import com.aircandi.service.objects.User;
import com.aircandi.ui.Preferences;
import com.aircandi.ui.builders.PicturePicker;
import com.aircandi.ui.widgets.BuilderButton;
import com.aircandi.ui.widgets.WebImageView;
import com.aircandi.utilities.AnimUtils;
import com.aircandi.utilities.AnimUtils.TransitionType;
import com.aircandi.utilities.ImageUtils;

public abstract class FormActivity extends SherlockActivity {

	protected Boolean			mBeaconUnregistered;
	protected String			mImageUriOriginal;
	protected AircandiCommon	mCommon;
	protected RequestListener	mImageRequestListener;
	protected WebImageView		mImageRequestWebImageView;
	protected String			mImagePath;
	protected String			mImageName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!Aircandi.getInstance().wasLaunchedNormally()) {
			/* Try to detect case where this is being created after a crash and bail out. */
			super.onCreate(savedInstanceState);
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
		else {
			/*
			 * Theme has to be set before any UI is constructed. We also have to do it for each activity so they pickup
			 * our custom style attributes.
			 */
			mCommon = new AircandiCommon(this, savedInstanceState);
			mCommon.unpackIntent();
			mCommon.setTheme(null, isDialog());
			super.onCreate(savedInstanceState);
			super.setContentView(this.getLayoutID());
			mCommon.initialize();
			mImagePath = Environment.getExternalStorageDirectory() + CandiConstants.IMAGE_CAPTURE_PATH;
		}
	}

	// --------------------------------------------------------------------------------------------
	// Events
	// --------------------------------------------------------------------------------------------

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		AnimUtils.doOverridePendingTransition(this, TransitionType.FormToCandiPage);
	}

	public void onCancelButtonClick(View view) {
		finish();
		AnimUtils.doOverridePendingTransition(this, TransitionType.FormToCandiPage);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		mCommon.doAttachedToWindow();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Logger.d(this, "Configuration changed");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		mCommon.doSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		/*
		 * Called before onResume. If we are returning from the market app, we get a zero result code whether the user
		 * decided to start an install or not.
		 */
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == CandiConstants.ACTIVITY_PICTURE_SEARCH) {

				Tracker.trackEvent("Entity", "PictureSearch", "None", 0);

				if (intent != null && intent.getExtras() != null) {
					Bundle extras = intent.getExtras();
					final String imageUri = extras.getString(CandiConstants.EXTRA_URI);
					final String imageTitle = extras.getString(CandiConstants.EXTRA_URI_TITLE);
					final String imageDescription = extras.getString(CandiConstants.EXTRA_URI_DESCRIPTION);

					BitmapRequestBuilder builder = new BitmapRequestBuilder(mImageRequestWebImageView)
							.setFromUris(imageUri, null)
							.setRequestListener(new RequestListener() {

								@Override
								public void onComplete(Object response) {

									final ServiceResponse serviceResponse = (ServiceResponse) response;
									if (serviceResponse.responseCode == ResponseCode.Success) {
										runOnUiThread(new Runnable() {

											@Override
											public void run() {
												if (mImageRequestListener != null) {
													ImageResponse imageResponse = (ImageResponse) serviceResponse.data;
													mImageRequestListener.onComplete(serviceResponse
															, imageResponse.imageUri
															, null
															, imageResponse.bitmap
															, imageTitle
															, imageDescription);
												}
											}
										});
									}
								}
							});

					if (imageTitle != null && !imageTitle.equals("")) {
						EditText title = (EditText) findViewById(R.id.text_title);
						if (title != null && title.getText().toString().equals("")) {
							title.setText(imageTitle);
						}
					}

					if (imageDescription != null && !imageDescription.equals("")) {
						EditText description = (EditText) findViewById(R.id.description);
						if (description != null && description.getText().toString().equals("")) {
							description.setText(imageDescription);
						}
					}

					BitmapRequest imageRequest = builder.create();
					mImageRequestWebImageView.setBitmapRequest(imageRequest, false);
				}
			}
			else if (requestCode == CandiConstants.ACTIVITY_PICTURE_PICK_PLACE) {

				Tracker.trackEvent("Entity", "PicturePlace", "None", 0);

				if (intent != null && intent.getExtras() != null) {
					Bundle extras = intent.getExtras();
					final String imageUri = extras.getString(CandiConstants.EXTRA_URI);

					BitmapRequestBuilder builder = new BitmapRequestBuilder(mImageRequestWebImageView)
							.setFromUris(imageUri, null)
							.setRequestListener(new RequestListener() {

								@Override
								public void onComplete(Object response) {

									final ServiceResponse serviceResponse = (ServiceResponse) response;
									if (serviceResponse.responseCode == ResponseCode.Success) {
										runOnUiThread(new Runnable() {

											@Override
											public void run() {
												if (mImageRequestListener != null) {
													ImageResponse imageResponse = (ImageResponse) serviceResponse.data;
													mImageRequestListener.onComplete(serviceResponse
															, imageResponse.imageUri
															, null
															, imageResponse.bitmap
															, null
															, null);
												}
											}
										});
									}
								}
							});

					BitmapRequest imageRequest = builder.create();
					mImageRequestWebImageView.setBitmapRequest(imageRequest, false);
				}
			}
			else if (requestCode == CandiConstants.ACTIVITY_PICTURE_PICK_DEVICE) {

				Tracker.trackEvent("Entity", "PickPicture", "None", 0);
				Uri imageUri = intent.getData();
				Bitmap bitmap = null;

				/* Bitmap size is trimmed if necessary to fit our max in memory image size. */
				bitmap = BitmapManager.getInstance().loadBitmapFromDevice(imageUri, "original");

				if (bitmap != null && mImageRequestListener != null) {
					mImageRequestWebImageView.getImageView().setImageBitmap(null);
					ImageUtils.showImageInImageView(bitmap, mImageRequestWebImageView.getImageView(), true, AnimUtils.fadeInMedium());
					mImageRequestListener.onComplete(new ServiceResponse(), null, null, bitmap, null, null);
				}
			}
			else if (requestCode == CandiConstants.ACTIVITY_PICTURE_MAKE) {

				Tracker.trackEvent("Entity", "TakePicture", "None", 0);
				try {
					/* Get bitmap */
					Bitmap bitmap = Media.getBitmap(getContentResolver(), Uri.fromFile(mCommon.getTempFile(this, "image_capture.tmp")));

					/* Adjust rotation using file Exif information */
					ExifInterface exif = new ExifInterface(mCommon.getTempFile(this, "image_capture.tmp").getAbsolutePath());
					int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
					int rotation = 0;
					switch (orientation) {
						case ExifInterface.ORIENTATION_ROTATE_270:
							rotation = 270;
							break;
						case ExifInterface.ORIENTATION_ROTATE_180:
							rotation = 180;
							break;
						case ExifInterface.ORIENTATION_ROTATE_90:
							rotation = 90;
							break;
					}
					/*
					 * Camera images can be huge. For example a 2560x1920 image takes up 20MBs of memory.
					 * To prevent OM errors, we need to make sure the image size is managed.
					 */
					Boolean scalingNeeded = (bitmap.getWidth() > CandiConstants.IMAGE_WIDTH_MAXIMUM || bitmap.getHeight() > CandiConstants.IMAGE_WIDTH_MAXIMUM);
					if (scalingNeeded || rotation != 0) {

						Matrix matrix = new Matrix();

						/* Resize the bitmap */
						if (scalingNeeded) {
							float scalingRatio = (float) CandiConstants.IMAGE_WIDTH_MAXIMUM / (float) bitmap.getWidth();
							matrix.postScale(scalingRatio, scalingRatio);
						}

						if (rotation != 0) {
							matrix.postRotate(rotation);
						}
						/*
						 * Create a new bitmap from the original using the matrix to transform the result.
						 * Potential for OM condition because if the garbage collector is behind, we could
						 * have several large bitmaps in memory at the same time.
						 */
						bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
					}

					mImageRequestWebImageView.getImageView().setImageBitmap(null);
					if (mImageRequestListener != null) {
						ImageUtils.showImageInImageView(bitmap, mImageRequestWebImageView.getImageView(), true, AnimUtils.fadeInMedium());
						mImageRequestListener.onComplete(new ServiceResponse(), null, null, bitmap, null, null);
					}
				}
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else if (requestCode == CandiConstants.ACTIVITY_WEBSITE_EDIT) {
				Tracker.trackEvent("Entity", "PickWebsite", "None", 0);
				if (intent != null && intent.getExtras() != null) {
					Bundle extras = intent.getExtras();
					String linkUri = extras.getString(CandiConstants.EXTRA_URI);
					if (!linkUri.startsWith("http://") && !linkUri.startsWith("https://")) {
						linkUri = "http://" + linkUri;
					}

					((BuilderButton) findViewById(R.id.website)).setText(linkUri);
				}
			}
			else if (requestCode == CandiConstants.ACTIVITY_FACEBOOK_PICK) {
				Tracker.trackEvent("Entity", "PickFacebook", "None", 0);
				if (intent != null && intent.getExtras() != null) {
					Bundle extras = intent.getExtras();
					String linkUri = extras.getString(CandiConstants.EXTRA_URI);
					if (!linkUri.startsWith("http://") && !linkUri.startsWith("https://")) {
						linkUri = "http://" + linkUri;
					}

					((BuilderButton) findViewById(R.id.facebook)).setText(linkUri);
				}
			}
			else if (requestCode == CandiConstants.ACTIVITY_LINK_PICK) {

				Tracker.trackEvent("Entity", "PickBookmark", "None", 0);
				if (intent != null && intent.getExtras() != null) {
					Bundle extras = intent.getExtras();

					String linkUriPre = extras.getString(CandiConstants.EXTRA_URI);
					if (!linkUriPre.startsWith("http://") && !linkUriPre.startsWith("https://")) {
						linkUriPre = "http://" + linkUriPre;
					}

					final String linkUri = linkUriPre;
					final String linkTitle = extras.getString(CandiConstants.EXTRA_URI_TITLE);
					final String linkDescription = extras.getString(CandiConstants.EXTRA_URI_DESCRIPTION);

					if (mImageRequestWebImageView != null) {
						if (linkUri != null && !linkUri.equals("")) {

							BitmapRequestBuilder builder = new BitmapRequestBuilder(mImageRequestWebImageView);
							builder.setFromUris(null, linkUri);
							builder.setRequestListener(new RequestListener() {

								@Override
								public void onComplete(Object response) {

									final ServiceResponse serviceResponse = (ServiceResponse) response;
									if (serviceResponse.responseCode == ResponseCode.Success) {
										runOnUiThread(new Runnable() {

											@Override
											public void run() {
												if (mImageRequestListener != null) {
													ImageResponse imageResponse = (ImageResponse) serviceResponse.data;
													mImageRequestListener.onComplete(serviceResponse, null, linkUri, imageResponse.bitmap, linkTitle,
															linkDescription);
												}
											}
										});
									}
								}
							});

							BitmapRequest imageRequest = builder.create();
							mImageRequestWebImageView.setBitmapRequest(imageRequest, false);
						}
					}
					else {
						if (mImageRequestListener != null) {
							mImageRequestListener.onComplete(new ServiceResponse(), null, linkUri, null, linkTitle, linkDescription);
						}
					}
				}
			}
		}
	}

	// --------------------------------------------------------------------------------------------
	// Picker routines
	// --------------------------------------------------------------------------------------------

	protected void pictureFromGallery() {
		Intent picturePickerIntent = new Intent(Intent.ACTION_PICK);
		picturePickerIntent.setType("image/*");
		startActivityForResult(picturePickerIntent, CandiConstants.ACTIVITY_PICTURE_PICK_DEVICE);
		AnimUtils.doOverridePendingTransition(this, TransitionType.CandiPageToForm);
	}

	protected void pictureFromCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCommon.getTempFile(this, "image_capture.tmp")));
		startActivityForResult(intent, CandiConstants.ACTIVITY_PICTURE_MAKE);
		AnimUtils.doOverridePendingTransition(this, TransitionType.CandiPageToForm);
	}

	protected void pictureSearch(String defaultSearch) {
		Intent intent = new Intent(this, PicturePicker.class);
		intent.putExtra(CandiConstants.EXTRA_SEARCH_PHRASE, defaultSearch);
		startActivityForResult(intent, CandiConstants.ACTIVITY_PICTURE_SEARCH);
		AnimUtils.doOverridePendingTransition(this, TransitionType.CandiPageToForm);
	}

	protected void pictureFromPlace(String entityId) {
		IntentBuilder intentBuilder = new IntentBuilder(this, PicturePicker.class);
		intentBuilder.setCommandType(CommandType.View)
				.setEntityId(entityId);
		Intent intent = intentBuilder.create();
		startActivityForResult(intent, CandiConstants.ACTIVITY_PICTURE_PICK_PLACE);
		AnimUtils.doOverridePendingTransition(this, TransitionType.CandiPageToForm);
	}

	protected void useFacebook() {
		/*
		 * Only used for user pictures
		 */
		final User user = Aircandi.getInstance().getUser();
		user.getPhoto().setImageUri("https://graph.facebook.com/" + user.facebookId + "/picture?type=large");
		user.getPhoto().setSourceName("external");

		BitmapRequestBuilder builder = new BitmapRequestBuilder(mImageRequestWebImageView);
		builder.setFromUris(user.getPhoto().getImageUri(), null);
		builder.setRequestListener(new RequestListener() {

			@Override
			public void onComplete(Object response) {

				/* Used to pass back the bitmap and imageUri (sometimes) for the entity */
				if (mImageRequestListener != null) {
					mImageRequestListener.onComplete(new ServiceResponse(), user.getImageUri(), null, null, null, null);
				}
			}
		});

		BitmapRequest imageRequest = builder.create();
		mImageRequestWebImageView.setBitmapRequest(imageRequest);
	}

	// --------------------------------------------------------------------------------------------
	// UI routines
	// --------------------------------------------------------------------------------------------


	protected void usePictureDefault(String defaultUri) {
		/* Tag has the uri to use for the placeholder */
		String imageUri = "resource:placeholder_logo";
		if (mCommon.mEntityType != null && mCommon.mEntityType.equals(CandiConstants.TYPE_CANDI_FOLDER)) {
			imageUri = "resource:ic_collection_250";
		}
		if (defaultUri != null) {
			imageUri = defaultUri;
		}
		BitmapRequestBuilder builder = new BitmapRequestBuilder(mImageRequestWebImageView);
		builder.setFromUris(imageUri, null);

		BitmapRequest imageRequest = builder.create();

		mImageRequestWebImageView.setBitmapRequest(imageRequest);

		if (mImageRequestListener != null) {
			mImageRequestListener.onComplete(new ServiceResponse(), imageUri, null, null, null, null);
		}

		Tracker.trackEvent("Entity", "DefaultPicture", "None", 0);
	}

	protected Boolean isDialog() {
		return false;
	}

	protected static void setVisibility(View view, Integer visibility) {
		if (view != null) {
			view.setVisibility(visibility);
		}
	}

	// --------------------------------------------------------------------------------------------
	// Lifecycle routines
	// --------------------------------------------------------------------------------------------

	@Override
	protected void onResume() {
		super.onResume();
		mCommon.doResume();
		if (!mCommon.mPrefTheme.equals(Aircandi.settings.getString(Preferences.PREF_THEME, CandiConstants.THEME_DEFAULT))) {
			mCommon.mPrefTheme = Aircandi.settings.getString(Preferences.PREF_THEME, CandiConstants.THEME_DEFAULT);
			mCommon.reload();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mCommon.doPause();
	}

	protected void onDestroy() {
		/* This activity gets destroyed everytime we leave using back or finish(). */
		Logger.d(this, "onDestroy called");
		try {
			if (mCommon != null) {
				mCommon.recycleImageViewDrawable(R.id.image_picture);
				mCommon.recycleImageViewDrawable(R.id.image_user_picture);
				mCommon.doDestroy();
			}
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
		finally {
			super.onDestroy();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mCommon.doStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mCommon.doStart();
	}

	protected int getLayoutID() {
		return 0;
	}

	// --------------------------------------------------------------------------------------------
	// Inner classes and enums
	// --------------------------------------------------------------------------------------------

	public class SimpleTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
	}
}