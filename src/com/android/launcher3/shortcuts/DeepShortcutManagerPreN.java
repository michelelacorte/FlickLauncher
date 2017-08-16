package com.android.launcher3.shortcuts;

/*
 * Created by Michele on 08/08/2017.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;

import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.shortcuts.pre.ShortcutCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.android.launcher3.shortcuts.ShortcutInfoCompat.INTENT_CATEGORY;


public class DeepShortcutManagerPreN extends DeepShortcutManager {

    private final Context mContext;
    private final LauncherApps mLauncherApps;
    private final boolean mEnableBackport;
    private ShortcutCache mShortcutCache;

    public DeepShortcutManagerPreN(Context context) {
        mContext = context;
        mLauncherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        mEnableBackport = true;
    }

    @Override
    public boolean wasLastCallSuccess() {
        return true;
    }

    @Override
    public void onShortcutsChanged(List list) {

    }

    @Override
    public List<ShortcutInfoCompat> queryForFullDetails(String str, List<String> list, UserHandle userHandle) {
        return query(11, str, null, list, userHandle);
    }

    @Override
    public List<ShortcutInfoCompat> queryForShortcutsContainer(ComponentName componentName, List<String> list, UserHandle userHandle) {
        return query(9, componentName.getPackageName(), componentName, list, userHandle);
    }

    @Override
    public void unpinShortcut(ShortcutKey shortcutKey) {
        // Do nothing
    }

    @Override
    public void pinShortcut(ShortcutKey shortcutKey) {
        // Do nothing
    }

    @Override
    public void startShortcut(String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, UserHandle user) {
        if (!mEnableBackport) return;
            ShortcutInfoCompat info = getShortcutCache().getShortcut(packageName, shortcutId);
            Intent intent = info.makeIntent(mContext);
            intent.setSourceBounds(sourceBounds);
            mContext.startActivity(intent);
    }

    @Override
    public Drawable getShortcutIconDrawable(ShortcutInfoCompat shortcutInfoCompat, int i) {
        if (!mEnableBackport) return null;
        return shortcutInfoCompat.getIcon();
    }

    @Override
    protected List<String> extractIds(List<ShortcutInfoCompat> list) {
        if (!mEnableBackport) return Collections.emptyList();
        List<String> ids = new ArrayList<>(list.size());
        for (ShortcutInfoCompat item : list) {
            ids.add(item.getId());
        }
        return ids;
    }

    @Override
    protected List<ShortcutInfoCompat> query(int flags, String packageName, ComponentName componentName, List<String> shortcutIds, UserHandle userHandle) {
        if (!mEnableBackport) return Collections.emptyList();
        return getShortcutCache().query(packageName, componentName);
    }

    public ShortcutCache getShortcutCache() {
        if (mShortcutCache == null) {
            mShortcutCache = new ShortcutCache(mContext, mLauncherApps);
        }
        return mShortcutCache;
    }

    @Override
    public boolean hasHostPermission() {
        return mEnableBackport;
    }
}