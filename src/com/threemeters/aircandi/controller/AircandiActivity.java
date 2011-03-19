package com.threemeters.aircandi.controller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.BaseRequestListener;
import com.threemeters.aircandi.model.FriendsFb;
import com.threemeters.aircandi.model.Post;
import com.threemeters.sdk.android.core.Entity;
import com.threemeters.sdk.android.core.RippleService;
import com.threemeters.sdk.android.core.Stream;
import com.threemeters.sdk.android.core.UserFb;
import com.threemeters.sdk.android.core.RippleService.GsonType;

public abstract class AircandiActivity extends Activity
{
	protected ImageView	mProgressIndicator;
	protected ImageView	mButtonRefresh;
	protected Stream	mStream;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		configure();
	}

	@Override
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		Window window = getWindow();
		window.setFormat(PixelFormat.RGBA_8888);
	}

	private void configure()
	{
		// Get view references
		mProgressIndicator = (ImageView) findViewById(R.id.Application_ProgressIndicator);
		if (mProgressIndicator != null)
			mProgressIndicator.setVisibility(View.INVISIBLE);
		mButtonRefresh = (ImageView) findViewById(R.id.Application_Button_Refresh);
		if (mButtonRefresh != null)
			mButtonRefresh.setVisibility(View.VISIBLE);

		// If mStream wasn't set by a sub class then check to see if there is something
		// we can do to create it.
		if (mStream == null)
		{
			if (getIntent() != null && getIntent().getExtras() != null)
			{
				String jsonStream = getIntent().getExtras().getString("stream");
				if (jsonStream != null && !jsonStream.equals(""))
					mStream = RippleService.getGson(GsonType.Internal).fromJson(getIntent().getExtras().getString("stream"), Stream.class);
			}
		}

		configureAppHeader();

		if (mStream != null)
		{
			// Configure the activity header
			configureHeader(mStream);

			// Configure the activity footer
			configureFooter(mStream);
		}
	}

	private void configureAppHeader()
	{
		TableRow row = (TableRow) findViewById(R.id.Texture_Row);
		if (row != null)
		{
//			Drawable bgDrawable = row.getBackground().mutate();
//			if (bgDrawable != null)
//				bgDrawable.setAlpha(64);
//
//			if (bgDrawable != null)
//			{
//				bgDrawable.setColorFilter(null);
//				int colorFilter = getResources().getColor(R.color.point_ripple_filter);
//				bgDrawable.setColorFilter(colorFilter, PorterDuff.Mode.MULTIPLY);
//			}

			
		}
	}

	private void configureHeader(Stream stream)
	{
		if (stream.showHeader)
		{
			// Title
			TextView title = (TextView) findViewById(R.id.Activity_Title);
			if (title != null)
				title.setText(stream.headerTitle);

			// Icon
			ImageView icon = (ImageView) findViewById(R.id.Activity_Icon);
			if (icon != null)
				if (!stream.headerIconResource.equals("none"))
					icon.setBackgroundResource(this.getResources().getIdentifier(stream.headerIconResource, "drawable", this.getPackageName()));
				else
					icon.setVisibility(View.GONE);
		}
		else
		{
			TableLayout activityHeader = (TableLayout) findViewById(R.id.Activity_Header);
			if (activityHeader != null)
				activityHeader.setVisibility(View.GONE);
		}
	}

	private void configureFooter(Stream stream)
	{
		if (stream.showFooter)
		{
		}
		else
		{
			// Layout might have a default footer in it or it might not
			TableLayout activityFooter = (TableLayout) findViewById(R.id.Activity_Footer);
			if (activityFooter != null)
				activityFooter.setVisibility(View.GONE);
		}
	}

	/*
	 * Routines required by the titlebar
	 */

	public void onHomeClick(View view)
	{
		Intent intent = new Intent(this, Dashboard.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	// Titlebar refresh
	public void onRefreshClick(View view)
	{
		return;
	}

	// Titlebar search
	public void onSearchClick(View view)
	{
		AircandiUI.showToastNotification(this, "Unimplemented...", Toast.LENGTH_SHORT);
		return;
	}

	/*
	 * These need to be overridden by activity sub classes
	 */

	protected void onActivityButtonClick(View view)
	{
		AircandiUI.showToastNotification(this, "Unimplemented...", Toast.LENGTH_SHORT);
		return;
	}

	protected void onItemButtonClick(View view)
	{
		AircandiUI.showToastNotification(this, "Unimplemented...", Toast.LENGTH_SHORT);
		return;
	}

	protected void onItemClick(View view)
	{
		AircandiUI.showToastNotification(this, "Unimplemented...", Toast.LENGTH_SHORT);
		return;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		// Hide the sign out option if we don't have a current session
		if (!FacebookService.facebookRunner.isSessionValid())
		{
			MenuItem item = menu.findItem(R.id.signout);
			item.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		// Hide the sign out option if we don't have a current session
		if (!FacebookService.facebookRunner.isSessionValid())
		{
			MenuItem item = menu.findItem(R.id.signout);
			item.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.settings:
				startActivity(new Intent(this, Preferences.class));
				return (true);
			case R.id.signout:
				signOut();
				return (true);
			default:
				return (super.onOptionsItemSelected(item));
		}
	}

	private void signOut()
	{
		// User clicked OK so sign out
		FacebookService.facebookRunner.logout(new LogoutRequestListener());
	}

	private final class LogoutRequestListener extends BaseRequestListener
	{
		public void onComplete(String response)
		{
			AircandiActivity.this.runOnUiThread(new Runnable() {
				public void run()
				{
					// Clear out the facebook objects
					setCurrentUser(null);
					setCurrentFriends(null);

					// Feedback
					AircandiUI.showToastNotification(AircandiActivity.this, "Signed out", Toast.LENGTH_SHORT);

					// Jump to the sign in activity
					Intent intent = new Intent(getApplicationContext(), AircandiLogin.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					startActivity(intent);
				}
			});
		}
	}

	protected void startProgress()
	{
		if (mProgressIndicator != null)
		{
			mProgressIndicator.setVisibility(View.VISIBLE);
			mButtonRefresh.setVisibility(View.INVISIBLE);
			mProgressIndicator.bringToFront();
			AnimationDrawable rippleAnimation = (AnimationDrawable) mProgressIndicator.getBackground();
			rippleAnimation.start();
			mProgressIndicator.invalidate();
		}
	}

	protected void stopProgress()
	{
		if (mProgressIndicator != null)
		{
			mProgressIndicator.setAnimation(null);
			mButtonRefresh.setVisibility(View.VISIBLE);
			mButtonRefresh.bringToFront();
			mProgressIndicator.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}
	
	protected void setCurrentLocation(Location currentLocation)
	{
		((Aircandi) getApplicationContext()).currentLocation = currentLocation;
	}

	protected Location getCurrentLocation()
	{
		return ((Aircandi) getApplicationContext()).currentLocation;
	}

	protected void setCurrentFriends(FriendsFb currentFriends)
	{
		((Aircandi) getApplicationContext()).currentFriendsX = currentFriends;
	}

	protected FriendsFb getCurrentFriends()
	{
		return ((Aircandi) getApplicationContext()).currentFriendsX;
	}

	protected void setCurrentPost(Post post)
	{
		((Aircandi) getApplicationContext()).currentPostX = post;
	}

	protected Post getCurrentPost()
	{
		return ((Aircandi) getApplicationContext()).currentPostX;
	}

	protected void setCurrentUser(UserFb currentUser)
	{
		((Aircandi) getApplicationContext()).currentUserX = currentUser;
	}

	protected UserFb getCurrentUser()
	{
		return ((Aircandi) getApplicationContext()).currentUserX;
	}

	protected void setCurrentEntity(Entity currentEntity)
	{
		((Aircandi) getApplicationContext()).currentEntityX = currentEntity;
	}

	protected Entity getCurrentEntity()
	{
		return ((Aircandi) getApplicationContext()).currentEntityX;
	}
	
	public static class ImageHolder
	{
		public ImageView	imageView;
		public String		imageUrl;
		public String		imageId;
		public String 		imageShape = "native";
		public Bitmap		image;
		public boolean		showReflection = false;
		public Object		data;
		public Object		containerView;
	}

	public static class ViewHolder
	{
		public ImageView	itemIcon;
		public TextView		itemTitle;
		public TextView		itemBody;
		public Button		itemButton;
		public LinearLayout	itemSidebarContainer;
		public TextView		itemSidebarText;
		public String		itemIconUrl;
		public Object		data;
		public String		userId;
		public String		imageFormat	= "large";
	}

	public static class ViewHolderEgg
	{
		public TextView		itemTitle;
		
		public Button		itemButton;
		public String		itemButtonImageUrl;
		public ImageView	itemFoundImage;
		public String		itemFoundImageUrl;
		public TextView		itemFoundText;
		public LinearLayout	itemFoundRow;
		
		public Object		data;
		public String		foundUserId;
		public String		imageFormat	= "large";
	}
}