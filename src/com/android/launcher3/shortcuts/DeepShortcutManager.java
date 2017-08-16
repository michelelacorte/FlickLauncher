/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.launcher3.shortcuts;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherApps.ShortcutQuery;
import android.content.pm.ShortcutInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Utilities;
import android.os.UserHandle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DeepShortcutManager {
    private static DeepShortcutManager sInstance;
    private static final Object sInstanceLock = new Object();

    public static DeepShortcutManager getInstance(Context context) {
        DeepShortcutManager deepShortcutManager;
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                if (Utilities.isNycMR1OrAbove())
                    sInstance = new DeepShortcutManagerN(context.getApplicationContext());
                else
                    sInstance = new DeepShortcutManagerPreN(context.getApplicationContext());
            }
            deepShortcutManager = sInstance;
        }
        return deepShortcutManager;
    }

    public static boolean supportsShortcuts(ItemInfo itemInfo) {
        return itemInfo.itemType == 0 && !itemInfo.isDisabled();
    }

    public abstract boolean wasLastCallSuccess();

    public abstract void onShortcutsChanged(List list);

    public abstract List<ShortcutInfoCompat> queryForFullDetails(String str, List<String> list, UserHandle userHandle);

    public abstract List<ShortcutInfoCompat> queryForShortcutsContainer(ComponentName componentName, List<String> list, UserHandle userHandle);

    public abstract void unpinShortcut(ShortcutKey shortcutKey);

    public abstract void pinShortcut(ShortcutKey shortcutKey);

    public abstract void startShortcut(String packageName, String shortcutId, Rect sourceBounds, Bundle startActivityOptions, UserHandle user);

    public abstract Drawable getShortcutIconDrawable(ShortcutInfoCompat shortcutInfoCompat, int i);

    public List<ShortcutInfoCompat> queryForPinnedShortcuts(String str, UserHandle userHandle) {
        return query(2, str, null, null, userHandle);
    }

    public List<ShortcutInfoCompat> queryForAllShortcuts(UserHandle userHandle) {
        return query(11, null, null, null, userHandle);
    }

    protected abstract List<String> extractIds(List<ShortcutInfoCompat> list);

    protected abstract List<ShortcutInfoCompat> query(int flags, String packageName, ComponentName componentName, List<String> shortcutIds, UserHandle userHandle);

    public abstract boolean hasHostPermission();
}