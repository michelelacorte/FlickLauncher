/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.hardware.fingerprint.FingerprintManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.TtsSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.compat.UserHandleCompat;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.config.ProviderConfig;
import com.android.launcher3.fingerprint.FingerprintActivity;
import com.android.launcher3.graphics.ShadowGenerator;
import com.android.launcher3.util.IconNormalizer;
import com.android.launcher3.util.StringFilter;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {

    private static final String TAG = "Launcher.Utilities";

    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();

    private static final Pattern sTrimPattern =
            Pattern.compile("^[\\s|\\p{javaSpaceChar}]*(.*)[\\s|\\p{javaSpaceChar}]*$");

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;

    private static final int[] sLoc0 = new int[2];
    private static final int[] sLoc1 = new int[2];

    public static boolean isNycMR1OrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
    }

    public static boolean isNycOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static final boolean ATLEAST_MARSHMALLOW =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

    public static final boolean ATLEAST_LOLLIPOP_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;

    public static final boolean ATLEAST_LOLLIPOP =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;

    public static final boolean ATLEAST_KITKAT =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    public static final boolean ATLEAST_JB_MR1 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    public static final boolean ATLEAST_JB_MR2 =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;

    // An intent extra to indicate the horizontal scroll of the wallpaper.
    public static final String EXTRA_WALLPAPER_OFFSET = "com.android.launcher3.WALLPAPER_OFFSET";

    // These values are same as that in {@link AsyncTask}.
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;
    /**
     * An {@link Executor} to be used with async task with no limit on the queue size.
     */
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public static final String ALLOW_ROTATION_PREFERENCE_KEY = "pref_allowRotation";

    public static final String ALLOW_CIRCULAR_ICON_PREFERENCE_KEY = "pref_allowCircularIcon";

    public static final String GRID_SIZE_COLUMN = "pref_gridSizeColumn";

    public static final String GRID_SIZE_ROW = "pref_gridSizeRow";

    public static final String GRID_SIZE = "pref_gridSize";

    public static final String ALL_APPS_SIZE = "pref_allAppsSize";

    public static final String ALL_APPS_SIZE_ITEM = "pref_allAppsSizeItem";

    public static final String DOCK_SIZE = "pref_dockSize";

    public static final String DOCK_SIZE_ITEM = "pref_dockSizeItem";

    public static final String DEFAULT_LAUNCHER = "pref_askDefaultLauncher";

    public static final String RESTART_LAUNCHER = "pref_askRestartLauncher";

    // Gesture Preferences
    public static final String DOUBLE_TAP_TO_SLEEP = "pref_allowSleep";

    public static final String CHOOSE_DOUBLE_TAP = "pref_chooseDoubleTap";
    public static final String CHOOSE_DOUBLE_TAP_PACKAGE = "pref_chooseDoubleTapPackage";
    public static final String CHOOSE_DOUBLE_TAP_CLASS = "pref_chooseDoubleTapClass";

    public static final String CHOOSE_SWIPE_UP = "pref_chooseUpSwipe";
    public static final String CHOOSE_SWIPE_UP_PACKAGE = "pref_chooseUpSwipePackage";
    public static final String CHOOSE_SWIPE_UP_CLASS = "pref_chooseUpSwipeClass";

    public static final String CHOOSE_SWIPE_BOTTOM = "pref_chooseBottomSwipe";
    public static final String CHOOSE_SWIPE_BOTTOM_PACKAGE = "pref_chooseBottomSwipePackage";
    public static final String CHOOSE_SWIPE_BOTTOM_CLASS = "pref_chooseBottomSwipeClass";

    public static final String CHOOSE_SWIPE_UP_TWO_FINGERS = "pref_chooseUpSwipeTwoFingers";
    public static final String CHOOSE_SWIPE_UP_TWO_FINGERS_PACKAGE = "pref_chooseUpSwipeTwoFingersPackage";
    public static final String CHOOSE_SWIPE_UP_TWO_FINGERS_CLASS = "pref_chooseUpSwipeTwoFingersClass";

    public static final String CHOOSE_SWIPE_BOTTOM_TWO_FINGERS = "pref_chooseBottomSwipeTwoFingers";
    public static final String CHOOSE_SWIPE_BOTTOM_TWO_FINGERS_PACKAGE = "pref_chooseBottomSwipeTwoFingersPackage";
    public static final String CHOOSE_SWIPE_BOTTOM_TWO_FINGERS_CLASS = "pref_chooseBottomSwipeTwoFingersClass";


    // Info Preferences
    public static final String INFORMATION = "pref_info";
    public static final String DONATION = "pref_donation";
    public static final String LICENSE = "pref_license";


    // Folder Preferences
    public static final String FOLDER_BACKGROUND = "pref_folderBackground";
    public static final String FOLDER_TRANSPARENT = "pref_folderPreviewTransparent";
    public static final String FOLDER_PREVIEW_BACKGROUND = "pref_folderPreviewBackground";
    public static final String FOLDER_PREVIEW_CIRCLE = "pref_folderPreviewCircleColor";

    // Custom gesture
    public static final String TORCH = "TORCH";
    public static final String BLUETOOTH = "BLUETOOTH";
    public static final String SETTINGS = "SETTINGS";
    public static final String WIFI = "WIFI";
    public static final String SCREENSHOT = "SCREENSHOT";
    public static final String SLEEP = "SLEEP";
    public static final String MODE_SILENT = "MODESILENT";
    public static final String MODE_VIBRATE = "MODEVIBRATE";
    public static final String MODE_NORMAL = "MODENORMAL";

    // Fingerptint Preference
    public static final String FINGERPRINT = "pref_fingerprint";
    public static final String FINGERPRINT_PACKAGE = "pref_fingerprintPackage";
    public static final String FINGERPRINT_CLASS = "pref_fingerprintClass";
    public static final String FINGERPRINT_POS = "pref_fingerprintPos";

    private static boolean isFlashLightOn = false;
    private static boolean isBluetoothOn = false;
    private static boolean isWifiOn = false;
    private static int mobileModeSilentPrevious = -1;
    private static boolean isModeSilent = false;
    private static int mobileModeVibratePrevious = -1;
    private static boolean isModeVibrate = false;
    private static int mobileModeNormalPrevious = -1;
    private static boolean isModeNormal = false;

    private static Camera cam = null;


    public static void setFingerprintAppsValue(Context context, CharSequence name, String packageName, String className, int pos) {
        getPrefs(context).edit().putString(FINGERPRINT+String.valueOf(pos), name.toString()).apply();
        getPrefs(context).edit().putString(FINGERPRINT_PACKAGE+String.valueOf(pos), packageName).apply();
        getPrefs(context).edit().putString(FINGERPRINT_CLASS+String.valueOf(pos), className).apply();
        getPrefs(context).edit().putInt(FINGERPRINT_POS+String.valueOf(pos), pos).apply();
    }

    public static void setFingerprintNullAppsValue(Context context, int pos) {
        getPrefs(context).edit().putString(FINGERPRINT+String.valueOf(pos), null).apply();
        getPrefs(context).edit().putString(FINGERPRINT_PACKAGE+String.valueOf(pos), null).apply();
        getPrefs(context).edit().putString(FINGERPRINT_CLASS+String.valueOf(pos), null).apply();
        getPrefs(context).edit().putInt(FINGERPRINT_POS+String.valueOf(pos), -1).apply();
    }

    public static String getFingerprintPrefEnabled(Context context, int pos) {
        return getPrefs(context).getString(FINGERPRINT+String.valueOf(pos),
                null);
    }

    public static String getFingerprintClassPrefEnabled(Context context, int pos) {
        return getPrefs(context).getString(FINGERPRINT_CLASS+String.valueOf(pos),
                null);
    }

    public static String getFingerprintPackagePrefEnabled(Context context, int pos) {
        return getPrefs(context).getString(FINGERPRINT_PACKAGE+String.valueOf(pos),
                null);
    }

    public static int getFingerprintPosPrefEnabled(Context context, int pos) {
        return getPrefs(context).getInt(FINGERPRINT_POS+String.valueOf(pos),
                -1);
    }


    public static void setFolderPreviewCircleValue(Context context, int color) {
        getPrefs(context).edit().putInt(FOLDER_PREVIEW_CIRCLE, color).apply();
    }

    public static int getFolderPreviewCirclePrefEnabled(Context context) {
        return getPrefs(context).getInt(FOLDER_PREVIEW_CIRCLE,
                -1);
    }

    public static void setFolderPreviewBackgroundValue(Context context, int color) {
        getPrefs(context).edit().putInt(FOLDER_PREVIEW_BACKGROUND, color).apply();
    }

    public static int getFolderPreviewBackgroundPrefEnabled(Context context) {
        return getPrefs(context).getInt(FOLDER_PREVIEW_BACKGROUND,
                -1);
    }

    public static boolean isAllowFolderTransparentPrefEnabled(Context context) {
        return getPrefs(context).getBoolean(FOLDER_TRANSPARENT,
                getAllowFolderTransparentDefaultValue());
    }

    public static boolean getAllowFolderTransparentDefaultValue() {
        return false;
    }


    public static void setFolderBackgroundValue(Context context, int color) {
        getPrefs(context).edit().putInt(FOLDER_BACKGROUND, color).apply();
    }

    public static int getFolderBackgroundPrefEnabled(Context context) {
        return getPrefs(context).getInt(FOLDER_BACKGROUND,
                -1);
    }


    public static boolean isPropertyEnabled(String propertyName) {
        return Log.isLoggable(propertyName, Log.VERBOSE);
    }

    public static boolean isAllowRotationPrefEnabled(Context context) {
        return getPrefs(context).getBoolean(ALLOW_ROTATION_PREFERENCE_KEY,
                getAllowRotationDefaultValue(context));
    }

    public static boolean getAllowRotationDefaultValue(Context context) {
        if (isNycOrAbove()) {
            // If the device was scaled, used the original dimensions to determine if rotation
            // is allowed of not.
            Resources res = context.getResources();
            int originalSmallestWidth = res.getConfiguration().smallestScreenWidthDp
                    * res.getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEVICE_STABLE;
            return originalSmallestWidth >= 600;
        }
        return false;
    }

    public static boolean getAllowDoubleTapToSleepDefaultValue() {
        return false;
    }

    public static boolean isAllowDoubleTapToSleepPrefEnabled(Context context) {
        return getPrefs(context).getBoolean(DOUBLE_TAP_TO_SLEEP,
                getAllowDoubleTapToSleepDefaultValue());
    }

    public static boolean getAllowCircularIconDefaultValue() {
        return true;
    }

    public static boolean isAllowCircularIconPrefEnabled(Context context) {
        return getPrefs(context).getBoolean(ALLOW_CIRCULAR_ICON_PREFERENCE_KEY,
                getAllowCircularIconDefaultValue());
    }

    public static void setAppDoubleTapValue(Context context, String appName, String packageName, String className) {
        getPrefs(context).edit().putString(CHOOSE_DOUBLE_TAP, appName).apply();
        getPrefs(context).edit().putString(CHOOSE_DOUBLE_TAP_PACKAGE, packageName).apply();
        getPrefs(context).edit().putString(CHOOSE_DOUBLE_TAP_CLASS, className).apply();
    }

    public static String getAppDoubleTapPrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_DOUBLE_TAP,
                null);
    }


    public static void setAppSwipeUpValue(Context context, String appName, String packageName, String className) {
        getPrefs(context).edit().putString(CHOOSE_SWIPE_UP, appName).apply();
        getPrefs(context).edit().putString(CHOOSE_SWIPE_UP_PACKAGE, packageName).apply();
        getPrefs(context).edit().putString(CHOOSE_SWIPE_UP_CLASS, className).apply();
    }

    public static String getAppSwipeUpPrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_UP,
                null);
    }


    public static void setAppSwipeBottomValue(Context context, String appName, String packageName, String className) {
        getPrefs(context).edit().putString(CHOOSE_SWIPE_BOTTOM, appName).apply();
        getPrefs(context).edit().putString(CHOOSE_SWIPE_BOTTOM_PACKAGE, packageName).apply();
        getPrefs(context).edit().putString(CHOOSE_SWIPE_BOTTOM_CLASS, className).apply();
    }

    public static String getAppSwipeBottomPrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_BOTTOM,
                null);
    }


    public static String getAppDoubleTapPackageNamePrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_DOUBLE_TAP_PACKAGE,
                null);
    }

    public static String getAppDoubleTapClassNamePrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_DOUBLE_TAP_CLASS,
                null);
    }


    public static String getAppSwipeUpPackageNamePrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_UP_PACKAGE,
                null);
    }

    public static String getAppSwipeUpClassNamePrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_UP_CLASS,
                null);
    }

    public static String getAppSwipeBottomPackageNamePrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_BOTTOM_PACKAGE,
                null);
    }

    public static String getAppSwipeBottomClassNamePrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_BOTTOM_CLASS,
                null);
    }

    public static void setAppSwipeBottomTwoFingersValue(Context context, String appName, String packageName, String className) {
        getPrefs(context).edit().putString(CHOOSE_SWIPE_BOTTOM_TWO_FINGERS, appName).apply();
        getPrefs(context).edit().putString(CHOOSE_SWIPE_BOTTOM_TWO_FINGERS_PACKAGE, packageName).apply();
        getPrefs(context).edit().putString(CHOOSE_SWIPE_BOTTOM_TWO_FINGERS_CLASS, className).apply();
    }

    public static String getAppSwipeBottomTwoFingersPrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_BOTTOM_TWO_FINGERS,
                null);
    }


    public static String getAppSwipeBottomTwoFingersPackageNamePrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_BOTTOM_TWO_FINGERS_PACKAGE,
                null);
    }

    public static String getAppSwipeBottomTwoFingersClassNamePrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_BOTTOM_TWO_FINGERS_CLASS,
                null);
    }


    public static void setAppSwipeUpTwoFingersValue(Context context, String appName, String packageName, String className) {
        getPrefs(context).edit().putString(CHOOSE_SWIPE_UP_TWO_FINGERS, appName).apply();
        getPrefs(context).edit().putString(CHOOSE_SWIPE_UP_TWO_FINGERS_PACKAGE, packageName).apply();
        getPrefs(context).edit().putString(CHOOSE_SWIPE_UP_TWO_FINGERS_CLASS, className).apply();
    }

    public static String getAppSwipeUpTwoFingersPrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_UP_TWO_FINGERS,
                null);
    }


    public static String getAppSwipeUpTwoFingersPackageNamePrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_UP_TWO_FINGERS_PACKAGE,
                null);
    }

    public static String getAppSwipeUpTwoFingersClassNamePrefEnabled(Context context) {
        return getPrefs(context).getString(CHOOSE_SWIPE_UP_TWO_FINGERS_CLASS,
                null);
    }


    public static void setGridSizeColumnDefaultValue(Context context, int columnCount) {
        getPrefs(context).edit().putInt(GRID_SIZE_COLUMN, columnCount).apply();
    }

    public static void setGridSizeRowDefaultValue(Context context, int rowCount) {
        getPrefs(context).edit().putInt(GRID_SIZE_ROW, rowCount).apply();
    }

    public static void setDockSizeDefaultValue(Context context, int num) {
        getPrefs(context).edit().putInt(DOCK_SIZE_ITEM, num).apply();
    }

    public static int getGridSizeColumnDefaultValue(Context context) {
        return getPrefs(context).getInt(GRID_SIZE_COLUMN, 5);
    }

    public static int getGridSizeRowDefaultValue(Context context) {
        return  getPrefs(context).getInt(GRID_SIZE_ROW, 6);
    }

    public static int getDockSizeDefaultValue(Context context) {
        return  getPrefs(context).getInt(DOCK_SIZE_ITEM, 5);
    }

    public static void setAllAppsSizeDefaultValue(Context context, int num) {
        getPrefs(context).edit().putInt(ALL_APPS_SIZE_ITEM, num).apply();
    }

    public static int getAllAppsSizeDefaultValue(Context context) {
        return  getPrefs(context).getInt(ALL_APPS_SIZE_ITEM, 5);
    }

    public static Bitmap createIconBitmap(Cursor c, int iconIndex, Context context) {
        byte[] data = c.getBlob(iconIndex);
        try {
            return createIconBitmap(BitmapFactory.decodeByteArray(data, 0, data.length), context);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a bitmap suitable for the all apps view. If the package or the resource do not
     * exist, it returns null.
     */
    public static Bitmap createIconBitmap(String packageName, String resourceName,
            Context context) {
        PackageManager packageManager = context.getPackageManager();
        // the resource
        try {
            Resources resources = packageManager.getResourcesForApplication(packageName);
            if (resources != null) {
                final int id = resources.getIdentifier(resourceName, null, null);
                return createIconBitmap(
                        resources.getDrawableForDensity(id, LauncherAppState.getInstance()
                                .getInvariantDeviceProfile().fillResIconDpi), context);
            }
        } catch (Exception e) {
            // Icon not found.
        }
        return null;
    }

    private static int getIconBitmapSize() {
        return LauncherAppState.getInstance().getInvariantDeviceProfile().iconBitmapSize;
    }

    /**
     * Returns a bitmap which is of the appropriate size to be displayed as an icon
     */
    public static Bitmap createIconBitmap(Bitmap icon, Context context) {
        final int iconBitmapSize = getIconBitmapSize();
        if (iconBitmapSize == icon.getWidth() && iconBitmapSize == icon.getHeight()) {
            return icon;
        }
        return createIconBitmap(new BitmapDrawable(context.getResources(), icon), context);
    }

    /**
     * Returns a bitmap suitable for the all apps view. The icon is badged for {@param user}.
     * The bitmap is also visually normalized with other icons.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Bitmap createBadgedIconBitmap(
            Drawable icon, UserHandleCompat user, Context context) {
        float scale = FeatureFlags.LAUNCHER3_DISABLE_ICON_NORMALIZATION ?
                1 : IconNormalizer.getInstance().getScale(icon, null);
        Bitmap bitmap = createIconBitmap(icon, context, scale);
        return badgeIconForUser(bitmap, user, context);
    }

    /**
     * Badges the provided icon with the user badge if required.
     */
    public static Bitmap badgeIconForUser(Bitmap icon,  UserHandleCompat user, Context context) {
        if (Utilities.ATLEAST_LOLLIPOP && user != null
                && !UserHandleCompat.myUserHandle().equals(user)) {
            BitmapDrawable drawable = new FixedSizeBitmapDrawable(icon);
            Drawable badged = context.getPackageManager().getUserBadgedIcon(
                    drawable, user.getUser());
            if (badged instanceof BitmapDrawable) {
                return ((BitmapDrawable) badged).getBitmap();
            } else {
                return createIconBitmap(badged, context);
            }
        } else {
            return icon;
        }
    }

    /**
     * Creates a normalized bitmap suitable for the all apps view. The bitmap is also visually
     * normalized with other icons and has enough spacing to add shadow.
     */
    public static Bitmap createScaledBitmapWithoutShadow(Drawable icon, Context context) {
        RectF iconBounds = new RectF();
        float scale = FeatureFlags.LAUNCHER3_DISABLE_ICON_NORMALIZATION ?
                1 : IconNormalizer.getInstance().getScale(icon, iconBounds);
        scale = Math.min(scale, ShadowGenerator.getScaleForBounds(iconBounds));
        return createIconBitmap(icon, context, scale);
    }

    /**
     * Adds a shadow to the provided icon. It assumes that the icon has already been scaled using
     * {@link #createScaledBitmapWithoutShadow(Drawable, Context)}
     */
    public static Bitmap addShadowToIcon(Bitmap icon) {
        return ShadowGenerator.getInstance().recreateIcon(icon);
    }

    /**
     * Adds the {@param badge} on top of {@param srcTgt} using the badge dimensions.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Bitmap badgeWithBitmap(Bitmap srcTgt, Bitmap badge, Context context) {
        int badgeSize = context.getResources().getDimensionPixelSize(R.dimen.profile_badge_size);
        synchronized (sCanvas) {
            sCanvas.setBitmap(srcTgt);
            sCanvas.drawBitmap(badge, new Rect(0, 0, badge.getWidth(), badge.getHeight()),
                    new Rect(srcTgt.getWidth() - badgeSize,
                            srcTgt.getHeight() - badgeSize, srcTgt.getWidth(), srcTgt.getHeight()),
                    new Paint(Paint.FILTER_BITMAP_FLAG));
            sCanvas.setBitmap(null);
        }
        return srcTgt;
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context) {
        return createIconBitmap(icon, context, 1.0f /* scale */);
    }

    /**
     * @param scale the scale to apply before drawing {@param icon} on the canvas
     */
    public static Bitmap createIconBitmap(Drawable icon, Context context, float scale) {
        synchronized (sCanvas) {
            final int iconBitmapSize = getIconBitmapSize();

            int width = iconBitmapSize;
            int height = iconBitmapSize;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap != null && bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = iconBitmapSize;
            int textureHeight = iconBitmapSize;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.scale(scale, scale, textureWidth / 2, textureHeight / 2);
            icon.draw(canvas);
            canvas.restore();
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
    }

    /**
     * Given a coordinate relative to the descendant, find the coordinate in a parent view's
     * coordinates.
     *
     * @param descendant The descendant to which the passed coordinate is relative.
     * @param ancestor The root view to make the coordinates relative to.
     * @param coord The coordinate that we want mapped.
     * @param includeRootScroll Whether or not to account for the scroll of the descendant:
     *          sometimes this is relevant as in a child's coordinates within the descendant.
     * @return The factor by which this descendant is scaled relative to this DragLayer. Caution
     *         this scale factor is assumed to be equal in X and Y, and so if at any point this
     *         assumption fails, we will need to return a pair of scale factors.
     */
    public static float getDescendantCoordRelativeToAncestor(
            View descendant, View ancestor, int[] coord, boolean includeRootScroll) {
        float[] pt = {coord[0], coord[1]};
        float scale = 1.0f;
        View v = descendant;
        while(v != ancestor && v != null) {
            // For TextViews, scroll has a meaning which relates to the text position
            // which is very strange... ignore the scroll.
            if (v != descendant || includeRootScroll) {
                pt[0] -= v.getScrollX();
                pt[1] -= v.getScrollY();
            }

            v.getMatrix().mapPoints(pt);
            pt[0] += v.getLeft();
            pt[1] += v.getTop();
            scale *= v.getScaleX();

            v = (View) v.getParent();
        }

        coord[0] = Math.round(pt[0]);
        coord[1] = Math.round(pt[1]);
        return scale;
    }

    /**
     * Inverse of {@link #getDescendantCoordRelativeToAncestor(View, View, int[], boolean)}.
     */
    public static float mapCoordInSelfToDescendent(View descendant, View root,
                                                   int[] coord) {
        ArrayList<View> ancestorChain = new ArrayList<View>();

        float[] pt = {coord[0], coord[1]};

        View v = descendant;
        while(v != root) {
            ancestorChain.add(v);
            v = (View) v.getParent();
        }
        ancestorChain.add(root);

        float scale = 1.0f;
        Matrix inverse = new Matrix();
        int count = ancestorChain.size();
        for (int i = count - 1; i >= 0; i--) {
            View ancestor = ancestorChain.get(i);
            View next = i > 0 ? ancestorChain.get(i-1) : null;

            pt[0] += ancestor.getScrollX();
            pt[1] += ancestor.getScrollY();

            if (next != null) {
                pt[0] -= next.getLeft();
                pt[1] -= next.getTop();
                next.getMatrix().invert(inverse);
                inverse.mapPoints(pt);
                scale *= next.getScaleX();
            }
        }

        coord[0] = (int) Math.round(pt[0]);
        coord[1] = (int) Math.round(pt[1]);
        return scale;
    }

    /**
     * Utility method to determine whether the given point, in local coordinates,
     * is inside the view, where the area of the view is expanded by the slop factor.
     * This method is called while processing touch-move events to determine if the event
     * is still within the view.
     */
    public static boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < (v.getWidth() + slop) &&
                localY < (v.getHeight() + slop);
    }

    /** Translates MotionEvents from src's coordinate system to dst's. */
    public static void translateEventCoordinates(View src, View dst, MotionEvent dstEvent) {
        toGlobalMotionEvent(src, dstEvent);
        toLocalMotionEvent(dst, dstEvent);
    }

    /**
     * Emulates View.toGlobalMotionEvent(). This implementation does not handle transformations
     * (scaleX, scaleY, etc).
     */
    private static void toGlobalMotionEvent(View view, MotionEvent event) {
        view.getLocationOnScreen(sLoc0);
        event.offsetLocation(sLoc0[0], sLoc0[1]);
    }

    /**
     * Emulates View.toLocalMotionEvent(). This implementation does not handle transformations
     * (scaleX, scaleY, etc).
     */
    private static void toLocalMotionEvent(View view, MotionEvent event) {
        view.getLocationOnScreen(sLoc0);
        event.offsetLocation(-sLoc0[0], -sLoc0[1]);
    }

    public static int[] getCenterDeltaInScreenSpace(View v0, View v1, int[] delta) {
        v0.getLocationInWindow(sLoc0);
        v1.getLocationInWindow(sLoc1);

        sLoc0[0] += (v0.getMeasuredWidth() * v0.getScaleX()) / 2;
        sLoc0[1] += (v0.getMeasuredHeight() * v0.getScaleY()) / 2;
        sLoc1[0] += (v1.getMeasuredWidth() * v1.getScaleX()) / 2;
        sLoc1[1] += (v1.getMeasuredHeight() * v1.getScaleY()) / 2;

        if (delta == null) {
            delta = new int[2];
        }

        delta[0] = sLoc1[0] - sLoc0[0];
        delta[1] = sLoc1[1] - sLoc0[1];

        return delta;
    }

    public static void scaleRectAboutCenter(Rect r, float scale) {
        if (scale != 1.0f) {
            int cx = r.centerX();
            int cy = r.centerY();
            r.offset(-cx, -cy);

            r.left = (int) (r.left * scale + 0.5f);
            r.top = (int) (r.top * scale + 0.5f);
            r.right = (int) (r.right * scale + 0.5f);
            r.bottom = (int) (r.bottom * scale + 0.5f);

            r.offset(cx, cy);
        }
    }

    public static void startActivityForResultSafely(
            Activity activity, Intent intent, int requestCode) {
        try {
            activity.startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(activity, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    static boolean isSystemApp(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = intent.getComponent();
        String packageName = null;
        if (cn == null) {
            ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if ((info != null) && (info.activityInfo != null)) {
                packageName = info.activityInfo.packageName;
            }
        } else {
            packageName = cn.getPackageName();
        }
        if (packageName != null) {
            try {
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                return (info != null) && (info.applicationInfo != null) &&
                        ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
            } catch (NameNotFoundException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * This picks a dominant color, looking for high-saturation, high-value, repeated hues.
     * @param bitmap The bitmap to scan
     * @param samples The approximate max number of samples to use.
     */
    static int findDominantColorByHue(Bitmap bitmap, int samples) {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        int sampleStride = (int) Math.sqrt((height * width) / samples);
        if (sampleStride < 1) {
            sampleStride = 1;
        }

        // This is an out-param, for getting the hsv values for an rgb
        float[] hsv = new float[3];

        // First get the best hue, by creating a histogram over 360 hue buckets,
        // where each pixel contributes a score weighted by saturation, value, and alpha.
        float[] hueScoreHistogram = new float[360];
        float highScore = -1;
        int bestHue = -1;

        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int argb = bitmap.getPixel(x, y);
                int alpha = 0xFF & (argb >> 24);
                if (alpha < 0x80) {
                    // Drop mostly-transparent pixels.
                    continue;
                }
                // Remove the alpha channel.
                int rgb = argb | 0xFF000000;
                Color.colorToHSV(rgb, hsv);
                // Bucket colors by the 360 integer hues.
                int hue = (int) hsv[0];
                if (hue < 0 || hue >= hueScoreHistogram.length) {
                    // Defensively avoid array bounds violations.
                    continue;
                }
                float score = hsv[1] * hsv[2];
                hueScoreHistogram[hue] += score;
                if (hueScoreHistogram[hue] > highScore) {
                    highScore = hueScoreHistogram[hue];
                    bestHue = hue;
                }
            }
        }

        SparseArray<Float> rgbScores = new SparseArray<Float>();
        int bestColor = 0xff000000;
        highScore = -1;
        // Go back over the RGB colors that match the winning hue,
        // creating a histogram of weighted s*v scores, for up to 100*100 [s,v] buckets.
        // The highest-scoring RGB color wins.
        for (int y = 0; y < height; y += sampleStride) {
            for (int x = 0; x < width; x += sampleStride) {
                int rgb = bitmap.getPixel(x, y) | 0xff000000;
                Color.colorToHSV(rgb, hsv);
                int hue = (int) hsv[0];
                if (hue == bestHue) {
                    float s = hsv[1];
                    float v = hsv[2];
                    int bucket = (int) (s * 100) + (int) (v * 10000);
                    // Score by cumulative saturation * value.
                    float score = s * v;
                    Float oldTotal = rgbScores.get(bucket);
                    float newTotal = oldTotal == null ? score : oldTotal + score;
                    rgbScores.put(bucket, newTotal);
                    if (newTotal > highScore) {
                        highScore = newTotal;
                        // All the colors in the winning bucket are very similar. Last in wins.
                        bestColor = rgb;
                    }
                }
            }
        }
        return bestColor;
    }

    /*
     * Finds a system apk which had a broadcast receiver listening to a particular action.
     * @param action intent action used to find the apk
     * @return a pair of apk package name and the resources.
     */
    static Pair<String, Resources> findSystemApk(String action, PackageManager pm) {
        final Intent intent = new Intent(action);
        for (ResolveInfo info : pm.queryBroadcastReceivers(intent, 0)) {
            if (info.activityInfo != null &&
                    (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                final String packageName = info.activityInfo.packageName;
                try {
                    final Resources res = pm.getResourcesForApplication(packageName);
                    return Pair.create(packageName, res);
                } catch (NameNotFoundException e) {
                    Log.w(TAG, "Failed to find resources for " + packageName);
                }
            }
        }
        return null;
    }

    /**
     * Compresses the bitmap to a byte array for serialization.
     */
    public static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w(TAG, "Could not write bitmap");
            return null;
        }
    }

    /**
     * Trims the string, removing all whitespace at the beginning and end of the string.
     * Non-breaking whitespaces are also removed.
     */
    public static String trim(CharSequence s) {
        if (s == null) {
            return null;
        }

        // Just strip any sequence of whitespace or java space characters from the beginning and end
        Matcher m = sTrimPattern.matcher(s);
        return m.replaceAll("$1");
    }

    /**
     * Calculates the height of a given string at a specific text size.
     */
    public static int calculateTextHeight(float textSizePx) {
        Paint p = new Paint();
        p.setTextSize(textSizePx);
        Paint.FontMetrics fm = p.getFontMetrics();
        return (int) Math.ceil(fm.bottom - fm.top);
    }

    /**
     * Convenience println with multiple args.
     */
    public static void println(String key, Object... args) {
        StringBuilder b = new StringBuilder();
        b.append(key);
        b.append(": ");
        boolean isFirstArgument = true;
        for (Object arg : args) {
            if (isFirstArgument) {
                isFirstArgument = false;
            } else {
                b.append(", ");
            }
            b.append(arg);
        }
        System.out.println(b.toString());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(Resources res) {
        return ATLEAST_JB_MR1 &&
                (res.getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    /**
     * Returns true if the intent is a valid launch intent for a launcher activity of an app.
     * This is used to identify shortcuts which are different from the ones exposed by the
     * applications' manifest file.
     *
     * @param launchIntent The intent that will be launched when the shortcut is clicked.
     */
    public static boolean isLauncherAppTarget(Intent launchIntent) {
        if (launchIntent != null
                && Intent.ACTION_MAIN.equals(launchIntent.getAction())
                && launchIntent.getComponent() != null
                && launchIntent.getCategories() != null
                && launchIntent.getCategories().size() == 1
                && launchIntent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && TextUtils.isEmpty(launchIntent.getDataString())) {
            // An app target can either have no extra or have ItemInfo.EXTRA_PROFILE.
            Bundle extras = launchIntent.getExtras();
            if (extras == null) {
                return true;
            } else {
                Set<String> keys = extras.keySet();
                return keys.size() == 1 && keys.contains(ItemInfo.EXTRA_PROFILE);
            }
        };
        return false;
    }

    public static float dpiFromPx(int size, DisplayMetrics metrics){
        float densityRatio = (float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return (size / densityRatio);
    }
    public static int pxFromDp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                size, metrics));
    }
    public static int pxFromSp(float size, DisplayMetrics metrics) {
        return (int) Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                size, metrics));
    }

    public static String createDbSelectionQuery(String columnName, Iterable<?> values) {
        return String.format(Locale.ENGLISH, "%s IN (%s)", columnName, TextUtils.join(", ", values));
    }

    public static boolean isBootCompleted() {
        return "1".equals(getSystemProperty("sys.boot_completed", "1"));
    }

    public static String getSystemProperty(String property, String defaultValue) {
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, property);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to read system properties");
        }
        return defaultValue;
    }

    /**
     * Ensures that a value is within given bounds. Specifically:
     * If value is less than lowerBound, return lowerBound; else if value is greater than upperBound,
     * return upperBound; else return value unchanged.
     */
    public static int boundToRange(int value, int lowerBound, int upperBound) {
        return Math.max(lowerBound, Math.min(value, upperBound));
    }

    /**
     * @see #boundToRange(int, int, int).
     */
    public static float boundToRange(float value, float lowerBound, float upperBound) {
        return Math.max(lowerBound, Math.min(value, upperBound));
    }

    /**
     * Wraps a message with a TTS span, so that a different message is spoken than
     * what is getting displayed.
     * @param msg original message
     * @param ttsMsg message to be spoken
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static CharSequence wrapForTts(CharSequence msg, String ttsMsg) {
        if (Utilities.ATLEAST_LOLLIPOP) {
            SpannableString spanned = new SpannableString(msg);
            spanned.setSpan(new TtsSpan.TextBuilder(ttsMsg).build(),
                    0, spanned.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            return spanned;
        } else {
            return msg;
        }
    }

    /**
     * Replacement for Long.compare() which was added in API level 19.
     */
    public static int longCompare(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(
                LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isPowerSaverOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return ATLEAST_LOLLIPOP && powerManager.isPowerSaveMode();
    }

    public static boolean isWallapaperAllowed(Context context) {
        if (isNycOrAbove()) {
            try {
                WallpaperManager wm = context.getSystemService(WallpaperManager.class);
                return (Boolean) wm.getClass().getDeclaredMethod("isSetWallpaperAllowed")
                        .invoke(wm);
            } catch (Exception e) { }
        }
        return true;
    }

    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                if (ProviderConfig.IS_DOGFOOD_BUILD) {
                    Log.d(TAG, "Error closing", e);
                }
            }
        }
    }

    /**
     * Returns true if {@param original} contains all entries defined in {@param updates} and
     * have the same value.
     * The comparison uses {@link Object#equals(Object)} to compare the values.
     */
    public static boolean containsAll(Bundle original, Bundle updates) {
        for (String key : updates.keySet()) {
            Object value1 = updates.get(key);
            Object value2 = original.get(key);
            if (value1 == null) {
                if (value2 != null) {
                    return false;
                }
            } else if (!value1.equals(value2)) {
                return false;
            }
        }
        return true;
    }

    /** Returns whether the collection is null or empty. */
    public static boolean isEmpty(Collection c) {
        return c == null || c.isEmpty();
    }

    /**
     * An extension of {@link BitmapDrawable} which returns the bitmap pixel size as intrinsic size.
     * This allows the badging to be done based on the action bitmap size rather than
     * the scaled bitmap size.
     */
    private static class FixedSizeBitmapDrawable extends BitmapDrawable {

        public FixedSizeBitmapDrawable(Bitmap bitmap) {
            super(null, bitmap);
        }

        @Override
        public int getIntrinsicHeight() {
            return getBitmap().getWidth();
        }

        @Override
        public int getIntrinsicWidth() {
            return getBitmap().getWidth();
        }
    }

    public static int getColorAccent(Context context) {
        TypedArray ta = context.obtainStyledAttributes(new int[]{android.R.attr.colorAccent});
        int colorAccent = ta.getColor(0, 0);
        ta.recycle();
        return colorAccent;
    }

    public static void sendCustomAccessibilityEvent(View target, int type, String text) {
        AccessibilityManager accessibilityManager = (AccessibilityManager)
                target.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain(type);
            target.onInitializeAccessibilityEvent(event);
            event.getText().add(text);
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }

    private static void changeDefaultLauncher(Context context){
        context.startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addCategory(Intent.CATEGORY_HOME));
    }

    public static void answerToChangeDefaultLauncher(final Context context){
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));


        alert.setTitle(context.getResources().getString(R.string.app_name));
        alert.setMessage(context.getResources().getString(R.string.ask_default));


        alert.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                changeDefaultLauncher(context);
            }
        });

        alert.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.setIcon(R.mipmap.ic_launcher_home);
        alert.show();
    }

    public static void restart(Context context, int delay) {
        if (delay == 0) {
            delay = 1;
        }
        Intent restartIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName() );
        restartIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        PendingIntent intent = PendingIntent.getActivity(
                context, 0,
                restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.set(AlarmManager.RTC, java.lang.System.currentTimeMillis() + delay, intent);
        java.lang.System.exit(2);
    }

    public static void answerToRestartLauncher(final Context contextRestart, Context context, final int delay){
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));


        alert.setTitle(context.getResources().getString(R.string.app_name));
        alert.setMessage(context.getResources().getString(R.string.ask_restart));


        alert.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                restart(contextRestart, delay);
            }
        });

        alert.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.setIcon(R.mipmap.ic_launcher_home);
        alert.show();
    }

    public static void aboutAlertDialog(Context context)
    {
        AlertDialog builder =
                new AlertDialog.Builder(context, R.style.AlertDialogCustom).setTitle(context.getResources().getString(R.string.app_name))
                        .setCancelable(false)
                        .setIcon(R.mipmap.ic_launcher_home)
                        .setMessage(R.string.disclaimer_dialog_message)
                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).create();
        builder.show();
        ((TextView)builder.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)builder.findViewById(android.R.id.message)).setGravity(Gravity.CENTER_VERTICAL);
        builder.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public static void licenseAlertDialog(Context context)
    {
        AlertDialog builder =
                new AlertDialog.Builder(context, R.style.AlertDialogCustom).setTitle(context.getResources().getString(R.string.app_name))
                        .setCancelable(false)
                        .setIcon(R.mipmap.ic_launcher_home)
                        .setMessage(R.string.license_dialog_message)
                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }).create();
        builder.show();
        ((TextView)builder.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView)builder.findViewById(android.R.id.message)).setGravity(Gravity.CENTER_VERTICAL);
        builder.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    public static void turnOnFlashLight(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camManager = (CameraManager) context.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null;
            try {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, true);
                isFlashLightOn = true;
            } catch (CameraAccessException e) {
                isFlashLightOn = false;
                e.printStackTrace();
            }
        }else {
            try {
                if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    Camera cam = Camera.open();
                    Camera.Parameters p = cam.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    cam.setParameters(p);
                    cam.startPreview();
                    isFlashLightOn = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                isFlashLightOn = false;
            }
        }
    }

    public static void turnOffFlashLight(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camManager = (CameraManager) context.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null;
            try {
                cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, false);
                isFlashLightOn = false;
            } catch (CameraAccessException e) {
                isFlashLightOn = true;
                e.printStackTrace();
            }
        }else {
            try {
                if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    cam.stopPreview();
                    cam.release();
                    cam = null;
                    isFlashLightOn = false;
                }
            } catch (Exception e) {
                isFlashLightOn = true;
                e.printStackTrace();
            }
        }
    }

    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = bluetoothAdapter.isEnabled();
        if (enable && !isEnabled) {
            isBluetoothOn = true;
            return bluetoothAdapter.enable();
        }
        else if(!enable && isEnabled) {
            isBluetoothOn = false;
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    public static void openSettings(Activity activity){
        activity.startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
    }


    public static void turnOnWifi(Activity activity){
        WifiManager wifimanager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifimanager.setWifiEnabled(true);
        isWifiOn = true;
    }

    public static void turnOffWifi(Activity activity){
        WifiManager wifimanager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifimanager.setWifiEnabled(false);
        isWifiOn = false;
    }

    private static Bitmap takeShot(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;


        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();


        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }

    private static void savePic(Bitmap b, String strFileName, Activity activity) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(strFileName);
            b.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
            Toast.makeText(activity.getApplicationContext(), "Screenshot Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("TAG", e.toString());
            e.printStackTrace();
        }
    }


    public static void takeScreenshot(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            checkPermission(activity);
        }
        try {

            File cacheDir = new File(
                    android.os.Environment.getExternalStorageDirectory(),
                    "FlickLauncher");

            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }

            String path = new File(
                    android.os.Environment.getExternalStorageDirectory(),
                    "FlickLauncher") + "/Screenshot-" + System.currentTimeMillis() + ".jpg";

            savePic(takeShot(activity), path, activity);

        } catch (NullPointerException ignored) {
            ignored.printStackTrace();
        }
    }

    /**
     * Check if user had permission
     * @param activity Activity
     */
    private static void checkPermission(Activity activity) {
        int result = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED) {
            requestPermission(activity);
        }
    }

    /**
     * Make request permission
     * @param activity Activity
     */
    private static void requestPermission(Activity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d(TAG, "Write External Storage permission allows us to do store shortcuts data. Please allow this permission in App Settings.");
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
            Log.d(TAG, "Write External Storage permission allows us to do store shortcuts data.");
        }
    }

    public static void modeSilent(Activity activity){
        checkPermissionForRingtone(activity);
        AudioManager mobilemode = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        if(isModeSilent){
            mobilemode.setRingerMode(mobileModeSilentPrevious);
            isModeSilent = false;
        }else {
            isModeSilent = true;
            mobileModeSilentPrevious = mobilemode.getRingerMode();
            mobilemode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

    public static void modeVibrate(Activity activity){
        checkPermissionForRingtone(activity);
        AudioManager mobilemode = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        if(isModeVibrate){
            mobilemode.setRingerMode(mobileModeVibratePrevious);
            isModeVibrate = false;
        }else {
            isModeVibrate = true;
            mobileModeVibratePrevious = mobilemode.getRingerMode();
            mobilemode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
    }

    public static void modeNormal(Activity activity){
        checkPermissionForRingtone(activity);
        AudioManager mobilemode = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        if(isModeNormal){
            mobilemode.setRingerMode(mobileModeNormalPrevious);
            isModeNormal = false;
        }else {
            isModeNormal = true;
            mobileModeNormalPrevious = mobilemode.getRingerMode();
            mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }

    private static void checkPermissionForRingtone(Activity activity){
        NotificationManager notificationManager =
                (NotificationManager) activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !notificationManager.isNotificationPolicyAccessGranted()) {

            Intent intent = new Intent(
                    android.provider.Settings
                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);

            activity.startActivity(intent);
        }
    }

    public static ArrayList<String> getAppHasFingerprint(Context context){
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < AllAppsList.data.size(); i++) {
            if(Utilities.getFingerprintPrefEnabled(context, i) != null){
                result.add(Utilities.getFingerprintPrefEnabled(context, i));
            }
        }
        return  result;
    }

    @TargetApi(23)
    public static boolean checkFingerprintHardwareAndPermission(Context context, FingerprintManager fingerprintManager){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
                if (fingerprintManager.isHardwareDetected()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isFlashLightOn() {
        return isFlashLightOn;
    }

    public static boolean isBluetoothOn() {
        return isBluetoothOn;
    }

    public static boolean isWifiOn() {
        return isWifiOn;
    }


}
