/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.android.utils;

import com.amazon.utils.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Locale;

/**
 * A collection of utility methods, all static.
 */
public class Helpers {

    private static final String TAG = Helpers.class.getName();
    public static final boolean DEBUG = false;

    /**
     * Default charset to be used in app.
     */
    private static final String DEFAULT_CHARSET_TEXT = "UTF-8";

    /**
     * Making sure public utility methods remain static.
     */
    private Helpers() {

    }

    /**
     * Returns the screen/display size.
     *
     * @param context The context.
     * @return The display size.
     */
    public static Point getDisplaySize(Context context) {

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    /**
     * Shows a (long) toast.
     *
     * @param context The context.
     * @param msg     The message to display
     */
    public static void showToast(Context context, String msg) {

        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Shows a (long) toast.
     *
     * @param context    The context.
     * @param resourceId The resource id.
     */
    public static void showToast(Context context, int resourceId) {

        Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_LONG).show();
    }

    /**
     * This method converts the dp to pixels.
     *
     * @param ctx The application context.
     * @param dp  The pixel value to convert to dp.
     * @return The rounded pixel result.
     */
    public static int convertDpToPixel(Context ctx, int dp) {

        float density = 0;
        try {
            density = ctx.getResources().getDisplayMetrics().density;
        }
        catch (Resources.NotFoundException exception) {
            Log.e(TAG, "Resources not found", exception);
        }
        return Math.round((float) dp * density);
    }

    /**
     * Converts a pixel value to dp value.
     *
     * @param ctx The application context.
     * @param px  The pixel value to convert to dp.
     * @return The rounded dp result.
     */
    public static int convertPixelToDp(Context ctx, int px) {

        float density = 0;
        try {
            density = ctx.getResources().getDisplayMetrics().density;
        }
        catch (Resources.NotFoundException exception) {
            Log.e(TAG, "Resources not found", exception);
        }
        return Math.round(px * density);
    }

    /**
     * Sleep for the given time in milliseconds
     *
     * @param milliseconds The time to sleep.
     */
    public static void sleep(int milliseconds) {

        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e) {
            Log.e(TAG, "Thread sleep exception", e);
        }
    }

    /**
     * This method converts Unix time to Date.
     *
     * @param intDate Input integer value.
     * @return The date.
     */
    public static Date covertIntegerToDate(Integer intDate) {

        return new Date((long) intDate * 1000);
    }

    /**
     * A debug helper class to listen for errors when loading image resources via Glide.
     *
     * @param <T> The type of the input source.
     * @param <R> The type of the resource that will be transcoded from the loaded resource.
     */
    public static class LoggingListener<T, R> implements RequestListener<T, R> {

        @Override
        public boolean onException(Exception e, Object model, Target target, boolean
                isFirstResource) {

            Log.e("GLIDE", String.format(Locale.ROOT,
                                         "onException(%s, %s, %s, %s)", e, model, target,
                                         isFirstResource), e);
            return false;
        }

        @Override
        public boolean onResourceReady(Object resource, Object model, Target target, boolean
                isFromMemoryCache, boolean isFirstResource) {

            return false;
        }
    }

    /**
     * Get the contents of the file and return as a Spanned text object.
     *
     * @param context  Application context that allows access to the assets folder.
     * @param filename Filename of the file in the assets folder.
     * @return The contents of the file as a Spanned text object.
     */
    public static String getContentFromFile(Context context, String filename) {

        StringBuilder stringBuilder = new StringBuilder();

        try {
            stringBuilder = new StringBuilder();
            InputStream inputStream = context.getResources().getAssets().open(filename);
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, Helpers
                    .getDefaultAppCharset()));
            String text;
            while ((text = in.readLine()) != null) {
                stringBuilder.append(text);
            }

            in.close();
        }
        catch (Resources.NotFoundException exception) {
            Log.e(TAG, "Resources not found", exception);
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to load content from file " + filename, e);
        }

        return stringBuilder.toString();
    }

    /**
     * Checks for network connectivity.
     *
     * @param context The context to use to get hold of connection related data.
     * @return True if connected; false otherwise.
     */
    public static boolean isConnectedToNetwork(Context context) {

        final NetworkInfo networkInfo = ((ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        // Only care for a case where there is no network info at all or no network connectivity
        // detected
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Rounds the corners of an image.
     *
     * @param activity The activity.
     * @param raw      The raw bitmap image to round.
     * @param round    The radius for the round corners.
     * @return The rounded image.
     */
    public static Bitmap roundCornerImage(Activity activity, Bitmap raw, float round) {

        int width = raw.getWidth();
        int height = raw.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawARGB(0, 0, 0, 0);

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(ContextCompat.getColor(activity, android.R.color.black));

        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);

        canvas.drawRoundRect(rectF, round, round, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(raw, rect, rect, paint);

        return result;
    }

    /**
     * This method updates the opacity of the bitmap.
     *
     * @param bitmap  The bitmap.
     * @param opacity The value of alpha.
     * @return The bitmap after adjusting the opacity.
     */
    public static Bitmap adjustOpacity(Bitmap bitmap, int opacity) {

        Bitmap mutableBitmap = bitmap.isMutable() ? bitmap : bitmap.copy(Bitmap.Config.ARGB_8888,
                                                                         true);
        Canvas canvas = new Canvas(mutableBitmap);
        int color = (opacity & 0xFF) << 24;
        canvas.drawColor(color, PorterDuff.Mode.DST_IN);
        return mutableBitmap;
    }

    /**
     * Loads an image using Glide from a URL into an image view and crossfades it with the image
     * view's current image.
     *
     * @param activity          The activity.
     * @param imageView         The image view to load the image into to.
     * @param url               The URL that points to the image to load.
     * @param crossFadeDuration The duration of the cross-fade in milliseconds.
     */
    public static void loadImageWithCrossFadeTransition(Activity activity, ImageView imageView,
                                                        String url, final int crossFadeDuration) {
        /*
         * With the Glide image managing framework, cross fade animations only take place if the
         * image is not already downloaded in cache. In order to have the cross fade animation
         * when the image is in cache, we need to make the following two calls.
         */
        Glide.with(activity)
             .load(url)
             .listener(new LoggingListener<>())
             .fitCenter()
             .error(R.drawable.browse_bg_color)
             .placeholder(imageView.getDrawable())
             .crossFade()
             .into(imageView);

        // Adding this second Glide call enables cross-fade transition even if the image is cached.
        Glide.with(activity)
             .load(url)
             .fitCenter()
             .error(R.drawable.browse_bg_color)
                .placeholder(imageView.getDrawable())
                        // Here we override the onResourceReady of the RequestListener to force
                        // the cross fade animation.
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {

                        Log.d("GLIDE", String.format(Locale.ROOT,
                                                     "onException(%s, %s, %s, %s)", e, model,
                                                     target, isFirstResource), e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource,
                                                   String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {

                        ImageViewTarget<GlideDrawable> imageTarget
                                = (ImageViewTarget<GlideDrawable>) target;
                        Drawable current = imageTarget.getCurrentDrawable();
                        if (current != null) {
                            TransitionDrawable transitionDrawable
                                    = new TransitionDrawable(new Drawable[]{current, resource});
                            transitionDrawable.setCrossFadeEnabled(true);
                            transitionDrawable.startTransition(crossFadeDuration);
                            imageTarget.setDrawable(transitionDrawable);
                            return true;
                        }
                        else
                            return false;
                    }
                }).crossFade()
                .into(imageView);
    }

    /**
     * Handles the activity's enter fade transition.
     *
     * @param activity     The activity.
     * @param fadeDuration The fade duration in milliseconds.
     */
    public static void handleActivityEnterFadeTransition(Activity activity, int fadeDuration) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition changeTransform = TransitionInflater.from(activity).
                    inflateTransition(R.transition.change_image_transform);
            Transition fadeTransform = TransitionInflater.from(activity).
                    inflateTransition(android.R.transition.fade);
            fadeTransform.setStartDelay(0);
            fadeTransform.setDuration(fadeDuration);
            activity.getWindow().setSharedElementEnterTransition(changeTransform);
            activity.getWindow().setEnterTransition(fadeTransform);
        }
    }

    /**
     * Returns the charset to be used throughout the app. If the charset is invalid, default
     * charset from the system is returned.
     *
     * @return The charset to be used throughout the app.
     */
    public static Charset getDefaultAppCharset() {

        try {
            return Charset.forName(DEFAULT_CHARSET_TEXT);
        }
        catch (Exception e) {
            Log.e(TAG, "Illegal charset " + DEFAULT_CHARSET_TEXT + " given ", e);
            return Charset.defaultCharset();
        }
    }
}
