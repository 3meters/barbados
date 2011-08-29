package com.proxibase.aircandi.candi.views;

import java.util.Observable;
import java.util.Observer;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.opengl.buffer.BufferObjectManager;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.util.GLHelper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;

import com.proxibase.aircandi.candi.models.BaseModel;
import com.proxibase.aircandi.candi.models.IModel;
import com.proxibase.aircandi.candi.presenters.CandiPatchPresenter;
import com.proxibase.aircandi.candi.sprites.CandiSprite;
import com.proxibase.aircandi.candi.utils.CandiConstants;
import com.proxibase.aircandi.utils.BitmapTextureSource;
import com.proxibase.aircandi.utils.BitmapTextureSource.IBitmapAdapter;

public abstract class BaseView extends Entity implements Observer, IView {

	protected CandiPatchPresenter	mCandiPatchPresenter;
	protected BaseModel				mBaseModel;

	protected Texture				mTitleTexture;
	private TextureRegion			mTitleTextureRegion;
	protected CandiSprite			mTitleSprite;
	protected String				mTitleText;

	public BaseView(BaseModel baseModel, CandiPatchPresenter candiPatchPresenter) {
		super();

		mBaseModel = baseModel;
		mCandiPatchPresenter = candiPatchPresenter;
	}

	public void initialize() {
		loadTextureSources();
		construct();
	}

	@Override
	public void update(Observable observable, Object data) {
		/*
		 * Any requested display extras are added to the title text by
		 * the getTitleText() method.
		 */
		updateTitleSprite(mBaseModel.getTitleText());
	}

	private void construct() {
		mTitleSprite = new CandiSprite(0,
				CandiConstants.CANDI_VIEW_TITLE_HEIGHT - (mTitleTextureRegion.getHeight() + CandiConstants.CANDI_VIEW_TITLE_SPACER_HEIGHT),
				mTitleTextureRegion);
		mTitleSprite.setBlendFunction(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
		mTitleSprite.setAlpha(0);
		mTitleSprite.setZIndex(0);
		attachChild(mTitleSprite);
	}

	protected boolean isVisibleToCamera(final Camera camera) {
		return (mTitleSprite != null && mTitleSprite.isVisibleToCamera(camera));
	}

	@Override
	public void setAlpha(float alpha) {
		super.setAlpha(alpha);

		for (int i = 0; i < getChildCount(); i++) {
			getChild(i).setAlpha(alpha);
		}
	}

	protected void updateTitleSprite(String titleText) {
		if (titleText != mTitleText) {
			mTitleText = titleText;
			mTitleTexture.clearTextureSources();
			Bitmap titleBitmap = makeTextBitmap(CandiConstants.CANDI_VIEW_WIDTH, CandiConstants.CANDI_VIEW_TITLE_HEIGHT, mBaseModel.getTitleText());
			mTitleTextureRegion = TextureRegionFactory.createFromSource(mTitleTexture, new BitmapTextureSource(titleBitmap, new IBitmapAdapter() {

				@Override
				public Bitmap reloadBitmap() {
					Bitmap titleBitmap = makeTextBitmap(CandiConstants.CANDI_VIEW_WIDTH, CandiConstants.CANDI_VIEW_TITLE_HEIGHT, mBaseModel
							.getTitleText());
					return titleBitmap;
				}
			}), 0, 0);

			mTitleSprite.setPosition(0,
					CandiConstants.CANDI_VIEW_TITLE_HEIGHT - (titleBitmap.getHeight() + CandiConstants.CANDI_VIEW_TITLE_SPACER_HEIGHT));
		}
	}

	public IModel getModel() {
		return mBaseModel;
	}

	private Bitmap makeTextBitmap(int width, int height, CharSequence text) {
		final TextPaint tp = new TextPaint();
		tp.setTextSize(CandiConstants.CANDI_VIEW_FONT_SIZE);
		tp.setColor(Color.WHITE);
		tp.setTypeface(Typeface.SANS_SERIF);
		tp.setAntiAlias(true);

		DynamicLayout textLayout = new DynamicLayout(text, text, tp, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false,
				TextUtils.TruncateAt.END, CandiConstants.CANDI_VIEW_WIDTH);
		int cappedHeight = textLayout.getHeight() > CandiConstants.CANDI_VIEW_TITLE_HEIGHT ? CandiConstants.CANDI_VIEW_TITLE_HEIGHT : textLayout
				.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, cappedHeight, CandiConstants.IMAGE_CONFIG_DEFAULT);
		Canvas canvas = new Canvas(bitmap);
		textLayout.draw(canvas);
		canvas = null;

		return bitmap;
	}

	@SuppressWarnings("unused")
	private Bitmap makeTextBitmapStatic(int width, int height, CharSequence text) {
		final TextPaint tp = new TextPaint();
		tp.setTextSize(CandiConstants.CANDI_VIEW_FONT_SIZE);
		tp.setColor(Color.WHITE);
		tp.setTypeface(Typeface.SANS_SERIF);
		tp.setAntiAlias(true);

		StaticLayout sl = new StaticLayout(text, tp, width, Layout.Alignment.ALIGN_NORMAL, 0.95f, 0.0f, false);
		int cappedHeight = sl.getHeight() > CandiConstants.CANDI_VIEW_TITLE_HEIGHT ? CandiConstants.CANDI_VIEW_TITLE_HEIGHT : sl.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, cappedHeight, CandiConstants.IMAGE_CONFIG_DEFAULT);
		Canvas canvas = new Canvas(bitmap);
		sl.draw(canvas);
		canvas = null;

		return bitmap;
	}

	public void unloadResources() {
		/*
		 * Completely remove all resources associated with this sprite.
		 */
		if (mTitleSprite != null)
			mTitleSprite.removeResources();

		if (mTitleTextureRegion != null)
			BufferObjectManager.getActiveInstance().unloadBufferObject(mTitleTextureRegion.getTextureBuffer());

		if (mTitleTexture != null)
			mCandiPatchPresenter.getEngine().getTextureManager().unloadTexture(mTitleTexture);
	}

	public void resetTextures() {
		mTitleTexture.clearTextureSources();
		loadTextureSources();
	}

	public void loadTextures() {
		mTitleTexture = new Texture(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mCandiPatchPresenter.getEngine().getTextureManager().loadTextures(mTitleTexture);
	}

	protected void loadTextureSources() {
		Bitmap titleBitmap = makeTextBitmap(CandiConstants.CANDI_VIEW_WIDTH, CandiConstants.CANDI_VIEW_TITLE_HEIGHT, mBaseModel.getTitleText());
		mTitleTextureRegion = TextureRegionFactory.createFromSource(mTitleTexture, new BitmapTextureSource(titleBitmap, new IBitmapAdapter() {

			@Override
			public Bitmap reloadBitmap() {
				Bitmap titleBitmap = makeTextBitmap(CandiConstants.CANDI_VIEW_WIDTH, CandiConstants.CANDI_VIEW_TITLE_HEIGHT, mBaseModel
						.getTitleText());
				return titleBitmap;
			}
		}), 0, 0);
	}

	@Override
	protected void applyRotation(final GL10 pGL) {

		// Disable culling so we can see the backside of this sprite.
		GLHelper.disableCulling(pGL);

		final float rotation = mRotation;

		if (rotation != 0) {
			final float rotationCenterX = mRotationCenterX;
			final float rotationCenterY = mRotationCenterY;

			pGL.glTranslatef(rotationCenterX, rotationCenterY, 0);
			// Note we are applying rotation around the y-axis and not the z-axis anymore!
			pGL.glRotatef(rotation, 0, 1, 0);
			pGL.glTranslatef(-rotationCenterX, -rotationCenterY, 0);
		}
	}

	public interface OnViewTexturesLoadedListener {

		void onTexturesLoaded(IView candiView);
	}
}