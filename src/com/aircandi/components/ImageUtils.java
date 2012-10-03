package com.aircandi.components;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.aircandi.Aircandi;
import com.aircandi.components.ImageRequest.ImageShape;
import com.aircandi.core.CandiConstants;

public class ImageUtils {

	private static Paint			mPaint	= new Paint();
	private static LinearGradient	mShader	= new LinearGradient(0, 0, 0, CandiConstants.CANDI_VIEW_REFLECTION_HEIGHT, 0x80ffffff, 0x00ffffff,
													TileMode.CLAMP);

	public static Bitmap bitmapFromView(View view, int width, int height) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, CandiConstants.IMAGE_CONFIG_DEFAULT);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);
		canvas = null;
		return bitmap;
	}
	
	public static void showToastNotification(final String message, final int duration) {
		Aircandi.applicationHandler.post(new Runnable() {

			@Override
			public void run() {
				CharSequence text = message;
				Toast toast = Toast.makeText(Aircandi.applicationContext, text, duration);
				toast.show();
			}
		});
	}

	public static void showToastNotification(final int messageId, final int duration) {
		Aircandi.applicationHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast toast = Toast.makeText(Aircandi.applicationContext, messageId, duration);
				toast.show();
			}
		});
	}

	public static int getRawPixelsForDisplayPixels(int displayPixels) {
		if (ImageManager.getInstance().getDisplayMetrics() != null) {
			final float scale = ImageManager.getInstance().getDisplayMetrics().density;
			return (int) (displayPixels * scale + 0.5f);
		}
		else {
			return (int) (displayPixels * 1.5f + 0.5f);
		}
	}
	
	public static int getColorBySignalLevel(int signalLevel, int alpha) {
		/*
		 * hue: 0 = red, 120 = green
		 */
		float levelMax = 100;
		float levelMin = 50;
		float signalLevelAbs = (float) Math.abs(signalLevel);
		if (signalLevelAbs > levelMax) {
			signalLevelAbs = levelMax;
		}
		else if (signalLevelAbs < levelMin) {
			signalLevelAbs = levelMin;
		}
		
		float signalLevelPcnt = (signalLevelAbs - levelMin) / (levelMax - levelMin);
		
		float[] hsv = new float[]{(120 - (120 * signalLevelPcnt)), 1.0f, 0.85f};
		return Color.HSVToColor(hsv);
	}

	public static int getImageMemorySize(int height, int width, boolean hasAlpha) {
		return height * width * (hasAlpha ? 4 : 3);
	}
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
				.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = pixels;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	public static Bitmap cropToSquare(Bitmap bitmap) {
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();
		int xStart = 0;
		int yStart = 0;
		int xEnd = width;
		int yEnd = height;

		if (height > width) {
			int diff = height - width;
			yStart = yStart + (diff / 2);
			yEnd = width;
		}
		else if (width > height) {
			int diff = width - height;
			xStart = xStart + (diff / 2);
			xEnd = height;
		}

		Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, xStart, yStart, xEnd, yEnd, null, true);
		return croppedBitmap;
	}

	public static Bitmap scaleAndCropBitmap(Bitmap bitmap, ImageRequest imageRequest) {
		return scaleAndCropBitmap(bitmap, imageRequest.getScaleToWidth(), imageRequest.getImageShape());
	}

	public static Bitmap scaleAndCropBitmap(Bitmap bitmap, int scaleToWidth, ImageShape imageShape) {
		
//		/* Create a matrix for the manipulation */
//		Matrix matrix = new Matrix();
//
//		/* Resize the bitmap */
//		matrix.postScale(scaleBy, scaleBy);
//		if (rotation != 0) {
//			matrix.postRotate(rotation);
//		}
//
//		Bitmap bitmapSampledAndScaled = Bitmap.createBitmap(bitmapSampled, 0, 0, bitmapSampled.getWidth(), bitmapSampled.getHeight(), matrix,
//				true);
		
		/* Scale if needed */
		Bitmap bitmapScaled = bitmap;
		if (scaleToWidth > 0) {
			boolean portrait = bitmap.getHeight() > bitmap.getWidth();
			if (portrait) {
				if (bitmap.getWidth() != scaleToWidth) {
					float scalingRatio = (float) scaleToWidth / (float) bitmap.getWidth();
					float newHeight = (float) bitmap.getHeight() * scalingRatio;
					bitmapScaled = Bitmap.createScaledBitmap(bitmap, scaleToWidth, (int) (newHeight), true);
					if (!bitmapScaled.equals(bitmap)) {
						bitmap.recycle();
					}
				}
			}
			else {
				if (bitmap.getHeight() != scaleToWidth) {
					float scalingRatio = (float) scaleToWidth / (float) bitmap.getHeight();
					float newWidth = (float) bitmap.getWidth() * scalingRatio;
					bitmapScaled = Bitmap.createScaledBitmap(bitmap, (int) (newWidth), scaleToWidth, true);
					if (!bitmapScaled.equals(bitmap)) {
						bitmap.recycle();
					}
				}
			}
		}

		/* Crop if requested */
		Bitmap bitmapScaledAndCropped = bitmapScaled;
		if (imageShape == ImageShape.Square) {
			bitmapScaledAndCropped = ImageUtils.cropToSquare(bitmapScaled);
			if (!bitmapScaledAndCropped.equals(bitmapScaled)) {
				bitmapScaled.recycle();
			}
		}

		/* Make sure the bitmap format is right */
		Bitmap bitmapFinal = bitmapScaledAndCropped;
		if (!bitmapScaledAndCropped.getConfig().name().equals(CandiConstants.IMAGE_CONFIG_DEFAULT.toString())) {
			bitmapFinal = bitmapScaledAndCropped.copy(CandiConstants.IMAGE_CONFIG_DEFAULT, false);
			if (!bitmapFinal.equals(bitmapScaledAndCropped)) {
				bitmapScaledAndCropped.recycle();
			}
		}

		if (bitmapFinal.isRecycled()) {
			throw new IllegalArgumentException("bitmapFinal has been recycled");
		}

		return bitmapFinal;
	}

	public static Bitmap makeReflection(Bitmap originalBitmap, boolean applyReflectionGradient) {

		int width = originalBitmap.getWidth();
		int height = originalBitmap.getHeight();

		/* This will not scale but will flip on the Y axis */
		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		/*
		 * Create a Bitmap with the flip matrix applied to it.
		 * We only want the bottom half of the image
		 */
		Bitmap reflectionImage = Bitmap.createBitmap(originalBitmap, 0, height / 2, width, height / 2, matrix, false);

		/* Create a new bitmap with same width but taller to fit reflection */
		Bitmap reflectionBitmap = Bitmap.createBitmap(width, (height / 2), CandiConstants.IMAGE_CONFIG_DEFAULT);

		Canvas canvas = new Canvas();

		canvas.setBitmap(reflectionBitmap);

		/* Draw in the reflection */
		canvas.drawBitmap(reflectionImage, 0, 0, null);

		/* Apply reflection gradient */
		if (applyReflectionGradient) {
			applyReflectionGradient(reflectionBitmap, Mode.DST_IN);
		}

		/* Stash the image with reflection */
		return reflectionBitmap;
	}

	public static void applyReflectionGradient(Bitmap reflectionBitmap, Mode mode) {

		Canvas canvas = new Canvas();

		canvas.setBitmap(reflectionBitmap);

		/* Set the paint to use this shader (linear gradient) */
		mPaint.setShader(mShader);

		/* Set the Transfer mode to be porter duff and destination in */
		mPaint.setXfermode(new PorterDuffXfermode(mode));

		/* Draw a rectangle using the paint with our linear gradient */
		canvas.drawRect(0, 0, reflectionBitmap.getWidth(), reflectionBitmap.getHeight(), mPaint);
	}

	public static int hexToColor(String hex) {
		int color = Color.parseColor(hex);
		return color;
	}

	public static String colorToHex(int color) {
		String r = Integer.toHexString(Color.red(color));
		String g = Integer.toHexString(Color.green(color));
		String b = Integer.toHexString(Color.blue(color));

		String hexColor = "#" + r + g + b;
		return hexColor;
	}

	public static void showImageInImageView(Bitmap bitmap, ImageView imageView, boolean animate, Animation animation) {
		imageView.setImageBitmap(bitmap);
		if (animate) {
			animation.setFillEnabled(true);
			animation.setFillAfter(true);
			imageView.startAnimation(animation);
		}
	}

	public static void clearImageInImageView(ImageView imageView, boolean animate, Animation animation) {
		if (animate) {
			animation.setFillEnabled(true);
			animation.setFillAfter(true);
			imageView.startAnimation(animation);
		}
		else {
			imageView.setAnimation(null);
			imageView.setImageBitmap(null);
		}
	}

	public static void showDrawableInImageView(Drawable drawable, ImageView imageView, boolean animate, Animation animation) {
		imageView.setImageDrawable(drawable);
		if (animate) {
			animation.setFillEnabled(true);
			animation.setFillAfter(true);
			imageView.startAnimation(animation);
		}
	}
}