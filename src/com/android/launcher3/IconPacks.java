package com.android.launcher3.icon.iconpacksupport;

/*
 * Created by Michele on 06/07/2017.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.ArrayMap;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.compat.LauncherActivityInfoCompat;
import com.android.launcher3.util.pixel.PixelIcon;

import java.util.List;
import java.util.Map;


public class IconPacks {
    private final String mIconBack;
    private final String mIconUpon;
    private final String mIconMask;
    private final float mScale;
    private final List<String> mCalendars;
    private Map<String, IconPackProvider.IconInfo> icons = new ArrayMap<>();
    private String packageName;
    private Context mContext;

    public IconPacks(Map<String, IconPackProvider.IconInfo> icons, Context context, String packageName,
                    String iconBack, String iconUpon, String iconMask, float scale, List<String> calendars) {
        this.icons = icons;
        this.packageName = packageName;
        mContext = context;
        mIconBack = iconBack;
        mIconUpon = iconUpon;
        mIconMask = iconMask;
        mScale = scale;
        mCalendars = calendars;
    }

    public Drawable getIcon(LauncherActivityInfoCompat info) {
        return getIcon(info.getComponentName());
    }

    public Drawable getIcon(ActivityInfo info) {
        return getIcon(new ComponentName(info.packageName, info.name));
    }

    public Drawable getIcon(ComponentName info) {
        IconPackProvider.IconInfo iconInfo = icons.get(info.toString());
        if (iconInfo != null && iconInfo.prefix != null) {
            Drawable drawable = getDrawable(iconInfo.prefix + (PixelIcon.dayOfMonth() + 1));
            if (drawable != null) {
                return drawable;
            }
        }
        if (iconInfo != null && iconInfo.drawable != null)
            return getDrawable(iconInfo.drawable);
        if (mIconBack != null || mIconUpon != null || mIconMask != null)
            return getMaskedDrawable(info);
        return null;
    }

    private Drawable getMaskedDrawable(ComponentName info) {
        try {
            return new CustomIconDrawable(mContext, this, info);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Drawable getDrawable(String name) {
        Resources res;
        try {
            res = mContext.getPackageManager().getResourcesForApplication(packageName);
            int resourceId = res.getIdentifier(name, "drawable", packageName);
            if (0 != resourceId) {
                Bitmap b = BitmapFactory.decodeResource(res, resourceId);
                return new FastBitmapDrawable(b);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getIconBack() {
        return mIconBack;
    }

    public String getIconUpon() {
        return mIconUpon;
    }

    public String getIconMask() {
        return mIconMask;
    }

    public float getScale() {
        return mScale;
    }

    public List<String> getCalendars() {
        return mCalendars;
    }
}