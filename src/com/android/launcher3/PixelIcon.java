package com.android.launcher3.util.pixel;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.android.launcher3.AllAppsList;
import com.android.launcher3.AppInfo;
import com.android.launcher3.ItemInfo;

import org.xmlpull.v1.XmlPullParser;
import java.util.Calendar;
import java.util.HashMap;

import static com.android.launcher3.Launcher.TAG;

/**
 * Created by Michele on 21/06/2017.
 */

public class PixelIcon {
    private static HashMap<String, Drawable> listOfRoundedIconBySystem = new HashMap<>();

    private static Drawable getRoundIcon(Context context, String packageName, int iconDpi) {
        try {
            Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(packageName);
            AssetManager assets = resourcesForApplication.getAssets();
            XmlResourceParser parseXml = assets.openXmlResourceParser("AndroidManifest.xml");
            int eventType;
            while ((eventType = parseXml.nextToken()) != XmlPullParser.END_DOCUMENT)
                if (eventType == XmlPullParser.START_TAG && parseXml.getName().equals("application"))
                    for (int i = 0; i < parseXml.getAttributeCount(); i++) {
                        if (parseXml.getAttributeName(i).equals("roundIcon")) {
                            return resourcesForApplication.getDrawableForDensity(Integer.parseInt(parseXml.getAttributeValue(i).substring(1)), iconDpi);
                        }
                    }
            parseXml.close();
        } catch (Exception ex) {
            Log.w("getRoundIcon", ex);
        }
        return null;
    }

    private static Drawable getRoundIconDynamicCalendar(Context context, String packageName, int iconDpi) {
        try {
            Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(packageName);
            AssetManager assets = resourcesForApplication.getAssets();
            XmlResourceParser parseXml = assets.openXmlResourceParser("AndroidManifest.xml");
            int eventType;
            while ((eventType = parseXml.nextToken()) != XmlPullParser.END_DOCUMENT)
                if (eventType == XmlPullParser.START_TAG && parseXml.getName().equals("application"))
                    for (int i = 0; i < parseXml.getAttributeCount(); i++) {
                        if (parseXml.getAttributeName(i).equals("icon")) {
                            return resourcesForApplication.getDrawableForDensity(Integer.parseInt(parseXml.getAttributeValue(i).substring(1)), iconDpi);
                        }
                    }
            parseXml.close();
        } catch (Exception ex) {
            Log.w("getRoundIcon", ex);
        }
        return null;
    }

    public static boolean isCalendar(final String s) {
        return "com.google.android.calendar".equals(s);
    }

    public static int dayOfMonth() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - 1;
    }

    private static int getCorrectShape(Bundle bundle, Resources resources) {
        if (bundle != null) {
            int roundIcons = bundle.getInt("com.google.android.calendar.dynamic_icons_nexus_round", 0);
            if (roundIcons != 0) {
                try {
                    TypedArray obtainTypedArray = resources.obtainTypedArray(roundIcons);
                    int resourceId = obtainTypedArray.getResourceId(dayOfMonth(), 0);
                    obtainTypedArray.recycle();
                    return resourceId;
                } catch (Resources.NotFoundException ex) {
                }
            }
        }

        return 0;
    }

    private static int getCorrectShapeDynamicCalendar(Bundle bundle, Resources resources) {
        if (bundle != null) {
            int roundIcons = bundle.getInt("com.google.android.calendar.dynamic_icons", 0);
            if (roundIcons != 0) {
                try {
                    TypedArray obtainTypedArray = resources.obtainTypedArray(roundIcons);
                    int resourceId = obtainTypedArray.getResourceId(dayOfMonth(), 0);
                    obtainTypedArray.recycle();
                    return resourceId;
                } catch (Resources.NotFoundException ex) {
                }
            }
        }

        return 0;
    }

    private static Drawable getRoundIconBySystem(Context context, ItemInfo info, int iconDpi){
        Drawable drawable;
        try {
            drawable = getRoundIcon(context, info.getTargetComponent().getPackageName(), iconDpi);
            String packageName = info.getTargetComponent().getPackageName();
            if (isCalendar(packageName)) {
                try {
                    ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(info.getTargetComponent(), PackageManager.GET_META_DATA | PackageManager.MATCH_UNINSTALLED_PACKAGES);
                    Bundle metaData = activityInfo.metaData;
                    Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(packageName);
                    int shape = getCorrectShape(metaData, resourcesForApplication);
                    if (shape != 0) {
                        drawable = resourcesForApplication.getDrawableForDensity(shape, iconDpi);
                    }
                } catch (PackageManager.NameNotFoundException ignored) {
                }
            }
        }catch (Exception e){
            return null;
        }

        if (drawable == null) {
            return null;
        }
        return drawable;
    }

    public static Drawable getRoundIconBySystemDynamicCalendar(Context context, ItemInfo info){
        Drawable drawable;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int iconDpi = metrics.densityDpi;
        try {
            drawable = getRoundIconDynamicCalendar(context, info.getTargetComponent().getPackageName(), iconDpi);
            String packageName = info.getTargetComponent().getPackageName();
            if (isCalendar(packageName)) {
                try {
                    ActivityInfo activityInfo = context.getPackageManager().getActivityInfo(info.getTargetComponent(), PackageManager.GET_META_DATA | PackageManager.MATCH_UNINSTALLED_PACKAGES);
                    Bundle metaData = activityInfo.metaData;
                    Resources resourcesForApplication = context.getPackageManager().getResourcesForApplication(packageName);
                    int shape = getCorrectShapeDynamicCalendar(metaData, resourcesForApplication);
                    if (shape != 0) {
                        drawable = resourcesForApplication.getDrawableForDensity(shape, iconDpi);
                    }
                } catch (PackageManager.NameNotFoundException ignored) {
                }
            }
        }catch (Exception e){
            return null;
        }

        if (drawable == null) {
            return null;
        }
        return drawable;
    }

    private static HashMap<String, Drawable> getListOfRoundIconBySystem(Context context, int iconDpi){
        HashMap<String, Drawable> listOfRoundedIcon = new HashMap<>();
        for(AppInfo appInfo : AllAppsList.data){
            try {
                if(appInfo.getTargetComponent() != null && appInfo != null) {
                    listOfRoundedIcon.put(appInfo.getTargetComponent().getPackageName(), getRoundIconBySystem(context, appInfo, iconDpi));
                }
            }catch (Exception e){
                Log.e(TAG, "getListOfRoundIconBySystem() problem");
            }
        }
        return listOfRoundedIcon;
    }

    public static void initializeMapOfRoundedIcon(Context context){
        try {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            if (listOfRoundedIconBySystem == null || listOfRoundedIconBySystem.size() == 0 || listOfRoundedIconBySystem.isEmpty()) {
                listOfRoundedIconBySystem = PixelIcon.getListOfRoundIconBySystem(context, (int) (metrics.densityDpi * 1.5));
            }
        }catch (Exception e){
            Log.e(TAG, "List of Rounded icon..");
        }
    }

    public static HashMap<String, Drawable> getListOfRoundedIconBySystem() {
        return listOfRoundedIconBySystem;
    }

}
