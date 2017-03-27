/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.provider.Settings.System;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.security.fingerprint.settings.FingerprintActivitySettings;
import com.android.launcher3.security.password.settings.PasswordActivitySettings;
import com.android.launcher3.util.ApplicationInfo;
import com.android.launcher3.util.ArrayAdapterWithIcon;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.michelelacorte.androidshortcuts.Shortcuts;
import it.michelelacorte.androidshortcuts.ShortcutsCreation;
import it.michelelacorte.androidshortcuts.util.Utils;

/**
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LauncherSettingsFragment())
                .commit();
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class LauncherSettingsFragment extends PreferenceFragment {

        private SystemDisplayRotationLockObserver mRotationLockObserver;
        private Context context;
        private int themeInt;
        private ContextThemeWrapper theme;
        private CharSequence[] items = null;
        private boolean[] selectedItemsBool = null;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            addPreferencesFromResource(R.xml.launcher_preferences);

            final ArrayList<AppInfo> apps = new ArrayList<>(AllAppsList.data);

            Collections.sort(apps, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo appInfo, AppInfo t1) {
                    return appInfo.title.toString().compareTo(t1.title.toString());
                }
            });

            //((AppCompatActivity)getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#5e8bff")));

            // Setup allow rotation preference
            final Preference rotationPref = findPreference(Utilities.ALLOW_ROTATION_PREFERENCE_KEY);
            if (getResources().getBoolean(R.bool.allow_rotation)) {
                // Launcher supports rotation by default. No need to show this setting.
                getPreferenceScreen().removePreference(rotationPref);
            } else {
                ContentResolver resolver = getActivity().getContentResolver();
                mRotationLockObserver = new SystemDisplayRotationLockObserver(rotationPref, resolver);

                // Register a content observer to listen for system setting changes while
                // this UI is active.
                resolver.registerContentObserver(
                        Settings.System.getUriFor(System.ACCELEROMETER_ROTATION),
                        false, mRotationLockObserver);

                // Initialize the UI once
                mRotationLockObserver.onChange(true);
                rotationPref.setDefaultValue(Utilities.getAllowRotationDefaultValue(getActivity()));
            }


            final Preference iconPref = findPreference(Utilities.ALLOW_CIRCULAR_ICON_PREFERENCE_KEY);
            if (getResources().getBoolean(R.bool.allow_circular_icon)) {
                getPreferenceScreen().removePreference(iconPref);
            } else {
                iconPref.setDefaultValue(true);
            }

            final Preference gridPref = findPreference(Utilities.GRID_SIZE);
            final Activity activity = this.getActivity();

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                theme = new ContextThemeWrapper(activity, R.style.AlertDialogCustomAPI23);
            } else {
                theme = new ContextThemeWrapper(activity, R.style.AlertDialogCustom);
            }
            //final int themeInt;
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                themeInt = R.style.AlertDialogCustomAPI23;
            } else {
                themeInt = R.style.AlertDialogCustom;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context = getContext();
            } else {
                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1){
                    context = getPreferenceScreen().getContext();
                }else {
                    context = activity.getApplicationContext();
                }
            }

            gridPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(theme);
                    LinearLayout layout = new LinearLayout(activity.getApplicationContext());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(100, 50, 100, 100);


                    final NumberPicker column = new NumberPicker(activity.getApplicationContext());
                    final TextView columnTitle = new TextView(activity.getApplicationContext());
                    columnTitle.setText(getString(R.string.column_size));
                    column.setMinValue(4);
                    column.setMaxValue(6);
                    column.setValue(Utilities.getGridSizeColumnDefaultValue(activity.getApplicationContext()));
                    column.setWrapSelectorWheel(false);

                    layout.addView(columnTitle);
                    layout.addView(column);

                    final NumberPicker row = new NumberPicker(activity.getApplicationContext());
                    final TextView rowTitle = new TextView(activity.getApplicationContext());
                    rowTitle.setText(getResources().getString(R.string.row_size));
                    row.setMinValue(4);
                    row.setMaxValue(12);
                    row.setValue(Utilities.getGridSizeRowDefaultValue(activity.getApplicationContext()));
                    row.setWrapSelectorWheel(false);

                    layout.addView(rowTitle);
                    layout.addView(row);

                    alert.setTitle(getResources().getString(R.string.grid_size));
                    alert.setView(layout);

                    alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Utilities.setGridSizeColumnDefaultValue(activity.getApplicationContext(), column.getValue());
                            Utilities.setGridSizeRowDefaultValue(activity.getApplicationContext(), row.getValue());
                            Utilities.restart(Launcher.getLauncherActivity(), 2000);
                        }
                    });

                    alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                    return true;
                }
            });

            final Preference dockPref = findPreference(Utilities.DOCK_SIZE);

            dockPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(theme);
                    LinearLayout layout = new LinearLayout(activity.getApplicationContext());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(100, 50, 100, 100);


                    final NumberPicker dock = new NumberPicker(activity.getApplicationContext());
                    final TextView dockTitle = new TextView(activity.getApplicationContext());
                    dockTitle.setText(getResources().getString(R.string.dock_size));
                    dock.setMinValue(4);
                    dock.setMaxValue(6);
                    dock.setValue(Utilities.getDockSizeDefaultValue(activity.getApplicationContext()));
                    dock.setWrapSelectorWheel(false);

                    layout.addView(dockTitle);
                    layout.addView(dock);


                    alert.setTitle(getResources().getString(R.string.dock_size));
                    alert.setView(layout);

                    alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Utilities.setDockSizeDefaultValue(activity.getApplicationContext(), dock.getValue());
                            Utilities.restart(Launcher.getLauncherActivity(), 2000);
                        }
                    });

                    alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                    return true;
                }
            });

            final Preference defaultLauncherPref = findPreference(Utilities.DEFAULT_LAUNCHER);
            defaultLauncherPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (!ApplicationInfo.isMyLauncherDefault(activity.getApplicationContext())) {
                        Utilities.answerToChangeDefaultLauncher(context);
                    } else {
                        Toast.makeText(activity.getApplicationContext(), getResources().getString(R.string.default_launcher_already_set), Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            });

            final Preference restartLauncherPref = findPreference(Utilities.RESTART_LAUNCHER);
            restartLauncherPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utilities.answerToRestartLauncher(Launcher.getLauncherActivity(), context, 2000);
                    return true;
                }
            });


            Preference doubleTapPref = findPreference(Utilities.DOUBLE_TAP_TO_SLEEP);
            final Preference doubleTapAppPref = findPreference(Utilities.CHOOSE_DOUBLE_TAP);
            final Preference swipeUpAppPref = findPreference(Utilities.CHOOSE_SWIPE_UP);
            final Preference swipeBottomAppPref = findPreference(Utilities.CHOOSE_SWIPE_BOTTOM);
            final Preference swipeBottomTwoFingersAppPref = findPreference(Utilities.CHOOSE_SWIPE_BOTTOM_TWO_FINGERS);
            final Preference swipeUpTwoFingersAppPref = findPreference(Utilities.CHOOSE_SWIPE_UP_TWO_FINGERS);

            if (Utilities.isAllowDoubleTapToSleepPrefEnabled(activity.getApplicationContext())) {
                doubleTapAppPref.setEnabled(false);
            } else if (!Utilities.isAllowDoubleTapToSleepPrefEnabled(activity.getApplicationContext())) {
                doubleTapAppPref.setEnabled(true);
            }

            if (Utilities.getAppDoubleTapPrefEnabled(activity.getApplicationContext()) != null) {
                doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + Utilities.getAppDoubleTapPrefEnabled(activity.getApplicationContext()));
            }
            if (Utilities.getAppSwipeUpPrefEnabled(activity.getApplicationContext()) != null) {
                swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + Utilities.getAppSwipeUpPrefEnabled(activity.getApplicationContext()));
            }
            if (Utilities.getAppSwipeBottomPrefEnabled(activity.getApplicationContext()) != null) {
                swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + Utilities.getAppSwipeBottomPrefEnabled(activity.getApplicationContext()));
            }
            if (Utilities.getAppSwipeBottomTwoFingersPrefEnabled(activity.getApplicationContext()) != null) {
                swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + Utilities.getAppSwipeBottomTwoFingersPrefEnabled(activity.getApplicationContext()));
            }
            if (Utilities.getAppSwipeUpTwoFingersPrefEnabled(activity.getApplicationContext()) != null) {
                swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + Utilities.getAppSwipeUpTwoFingersPrefEnabled(activity.getApplicationContext()));
            }


            if (getResources().getBoolean(R.bool.allow_sleep)) {
                getPreferenceScreen().removePreference(doubleTapPref);
            } else {
                doubleTapPref.setDefaultValue(true);
            }

            doubleTapPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (Utilities.isAllowDoubleTapToSleepPrefEnabled(activity.getApplicationContext())) {
                        doubleTapAppPref.setEnabled(true);
                    } else if (!Utilities.isAllowDoubleTapToSleepPrefEnabled(activity.getApplicationContext())) {
                        doubleTapAppPref.setEnabled(false);
                    }
                    return true;
                }
            });

            doubleTapAppPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final List<String> items = new ArrayList<String>();
                    final List<Bitmap> icons = new ArrayList<Bitmap>();

                    items.add(getString(R.string.nothing));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_clear_black_24dp)));

                    items.add(getString(R.string.wifi));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_wifi_black_24dp)));

                    items.add(getString(R.string.torch));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_lightbulb_outline_black_24dp)));

                    items.add(getString(R.string.bluetooth));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_bluetooth_black_24dp)));

                    items.add(getString(R.string.screenshot));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_photo_black_24dp)));

                    items.add(getString(R.string.sleep));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_screen_lock_portrait_black_24dp)));

                    items.add(getString(R.string.mode_silent));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_volume_off_black_24dp)));

                    items.add(getString(R.string.mode_vibrate));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_vibration_black_24dp)));

                    items.add(getString(R.string.mode_normal));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_volume_up_black_24dp)));

                    items.add(getString(R.string.settings));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_settings_black_24dp)));

                    for (AppInfo app : apps) {
                        items.add(app.title.toString());
                        icons.add(app.iconBitmap);
                    }
                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.alert_choose_app))
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items.get(item).equals(getString(R.string.torch))) {
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), items.get(item), Utilities.TORCH, Utilities.TORCH);
                                    } else if(items.get(item).equals(getString(R.string.bluetooth))){
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), items.get(item), Utilities.BLUETOOTH, Utilities.BLUETOOTH);
                                    } else if(items.get(item).equals(getString(R.string.settings))){
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), items.get(item), Utilities.SETTINGS, Utilities.SETTINGS);
                                    } else if(items.get(item).equals(getString(R.string.wifi))){
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), items.get(item), Utilities.WIFI, Utilities.WIFI);
                                    } else if(items.get(item).equals(getString(R.string.screenshot))){
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), items.get(item), Utilities.SCREENSHOT, Utilities.SCREENSHOT);
                                    } else if(items.get(item).equals(getString(R.string.sleep))){
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), items.get(item), Utilities.SLEEP, Utilities.SLEEP);
                                    }else if(items.get(item).equals(getString(R.string.mode_silent))){
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_SILENT, Utilities.MODE_SILENT);
                                    }else if(items.get(item).equals(getString(R.string.mode_vibrate))){
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_VIBRATE, Utilities.MODE_VIBRATE);
                                    }else if(items.get(item).equals(getString(R.string.mode_normal))){
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_NORMAL, Utilities.MODE_NORMAL);
                                    }else if (items.get(item).equals(getString(R.string.nothing))) {
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary));
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), null, null, null);
                                    } else {
                                        doubleTapAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        String packageName = null;
                                        String className = null;
                                        for (AppInfo app : AllAppsList.data) {
                                            if (items.get(item).equalsIgnoreCase(app.title.toString())) {
                                                packageName = app.componentName.getPackageName();
                                                className = app.componentName.getClassName();
                                            }
                                        }
                                        Utilities.setAppDoubleTapValue(activity.getApplicationContext(), items.get(item), packageName, className);
                                    }
                                }
                            }).show();
                    return true;
                }
            });


            swipeUpAppPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final List<String> items = new ArrayList<String>();
                    final List<Bitmap> icons = new ArrayList<Bitmap>();
                    items.add(getString(R.string.nothing));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_clear_black_24dp)));

                    items.add(getString(R.string.wifi));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_wifi_black_24dp)));

                    items.add(getString(R.string.torch));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_lightbulb_outline_black_24dp)));

                    items.add(getString(R.string.bluetooth));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_bluetooth_black_24dp)));

                    items.add(getString(R.string.screenshot));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_photo_black_24dp)));

                    items.add(getString(R.string.sleep));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_screen_lock_portrait_black_24dp)));

                    items.add(getString(R.string.mode_silent));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_volume_off_black_24dp)));

                    items.add(getString(R.string.mode_vibrate));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_vibration_black_24dp)));

                    items.add(getString(R.string.mode_normal));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_volume_up_black_24dp)));

                    items.add(getString(R.string.settings));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_settings_black_24dp)));

                    for (AppInfo app : apps) {
                        items.add(app.title.toString());
                        icons.add(app.iconBitmap);
                    }
                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.alert_choose_app))
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items.get(item).equals(getString(R.string.torch))) {
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), Utilities.TORCH, Utilities.TORCH);
                                    } else if(items.get(item).equals(getString(R.string.bluetooth))){
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), Utilities.BLUETOOTH, Utilities.BLUETOOTH);
                                    } else if(items.get(item).equals(getString(R.string.settings))){
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), Utilities.SETTINGS, Utilities.SETTINGS);
                                    } else if(items.get(item).equals(getString(R.string.wifi))){
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), Utilities.WIFI, Utilities.WIFI);
                                    } else if(items.get(item).equals(getString(R.string.screenshot))){
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), Utilities.SCREENSHOT, Utilities.SCREENSHOT);
                                    } else if(items.get(item).equals(getString(R.string.sleep))){
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), Utilities.SLEEP, Utilities.SLEEP);
                                    }else if(items.get(item).equals(getString(R.string.mode_silent))){
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_SILENT, Utilities.MODE_SILENT);
                                    }else if(items.get(item).equals(getString(R.string.mode_vibrate))){
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_VIBRATE, Utilities.MODE_VIBRATE);
                                    }else if(items.get(item).equals(getString(R.string.mode_normal))){
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_NORMAL, Utilities.MODE_NORMAL);
                                    } else if (items.get(item).equals(getString(R.string.nothing))) {
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary));
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), null, null, null);
                                    } else {
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        String packageName = null;
                                        String className = null;
                                        for (AppInfo app : AllAppsList.data) {
                                            if (items.get(item).equalsIgnoreCase(app.title.toString())) {
                                                packageName = app.componentName.getPackageName();
                                                className = app.componentName.getClassName();
                                            }
                                        }
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), packageName, className);
                                    }
                                }
                            }).show();
                    return true;
                }
            });


            swipeBottomAppPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final List<String> items = new ArrayList<String>();
                    final List<Bitmap> icons = new ArrayList<Bitmap>();
                    items.add(getString(R.string.nothing));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_clear_black_24dp)));

                    items.add(getString(R.string.wifi));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_wifi_black_24dp)));

                    items.add(getString(R.string.torch));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_lightbulb_outline_black_24dp)));

                    items.add(getString(R.string.bluetooth));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_bluetooth_black_24dp)));

                    items.add(getString(R.string.screenshot));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_photo_black_24dp)));

                    items.add(getString(R.string.sleep));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_screen_lock_portrait_black_24dp)));

                    items.add(getString(R.string.mode_silent));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_volume_off_black_24dp)));

                    items.add(getString(R.string.mode_vibrate));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_vibration_black_24dp)));

                    items.add(getString(R.string.mode_normal));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_volume_up_black_24dp)));

                    items.add(getString(R.string.settings));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_settings_black_24dp)));
                    for (AppInfo app : apps) {
                        items.add(app.title.toString());
                        icons.add(app.iconBitmap);
                    }
                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.alert_choose_app))
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items.get(item).equals(getString(R.string.torch))) {
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), items.get(item), Utilities.TORCH, Utilities.TORCH);
                                    } else if(items.get(item).equals(getString(R.string.bluetooth))){
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), items.get(item), Utilities.BLUETOOTH, Utilities.BLUETOOTH);
                                    } else if(items.get(item).equals(getString(R.string.settings))){
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), items.get(item), Utilities.SETTINGS, Utilities.SETTINGS);
                                    } else if(items.get(item).equals(getString(R.string.wifi))){
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), items.get(item), Utilities.WIFI, Utilities.WIFI);
                                    } else if(items.get(item).equals(getString(R.string.screenshot))){
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), items.get(item), Utilities.SCREENSHOT, Utilities.SCREENSHOT);
                                    } else if(items.get(item).equals(getString(R.string.sleep))){
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), items.get(item), Utilities.SLEEP, Utilities.SLEEP);
                                    }else if(items.get(item).equals(getString(R.string.mode_silent))){
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_SILENT, Utilities.MODE_SILENT);
                                    }else if(items.get(item).equals(getString(R.string.mode_vibrate))){
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_VIBRATE, Utilities.MODE_VIBRATE);
                                    }else if(items.get(item).equals(getString(R.string.mode_normal))){
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_NORMAL, Utilities.MODE_NORMAL);
                                    }else if (items.get(item).equals(getString(R.string.nothing))) {
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary));
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), null, null, null);
                                    } else {
                                        swipeBottomAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        String packageName = null;
                                        String className = null;
                                        for (AppInfo app : AllAppsList.data) {
                                            if (items.get(item).equalsIgnoreCase(app.title.toString())) {
                                                packageName = app.componentName.getPackageName();
                                                className = app.componentName.getClassName();
                                            }
                                        }
                                        Utilities.setAppSwipeBottomValue(activity.getApplicationContext(), items.get(item), packageName, className);
                                    }
                                }
                            }).show();
                    return true;
                }
            });

            swipeBottomTwoFingersAppPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final List<String> items = new ArrayList<String>();
                    final List<Bitmap> icons = new ArrayList<Bitmap>();
                    items.add(getString(R.string.nothing));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_clear_black_24dp)));

                    items.add(getString(R.string.wifi));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_wifi_black_24dp)));

                    items.add(getString(R.string.torch));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_lightbulb_outline_black_24dp)));

                    items.add(getString(R.string.bluetooth));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_bluetooth_black_24dp)));

                    items.add(getString(R.string.screenshot));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_photo_black_24dp)));

                    items.add(getString(R.string.sleep));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_screen_lock_portrait_black_24dp)));

                    items.add(getString(R.string.mode_silent));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_volume_off_black_24dp)));

                    items.add(getString(R.string.mode_vibrate));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_vibration_black_24dp)));

                    items.add(getString(R.string.mode_normal));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_volume_up_black_24dp)));

                    items.add(getString(R.string.settings));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_settings_black_24dp)));
                    for (AppInfo app : apps) {
                        items.add(app.title.toString());
                        icons.add(app.iconBitmap);
                    }
                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.alert_choose_app))
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items.get(item).equals(getString(R.string.torch))) {
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.TORCH, Utilities.TORCH);
                                    } else if(items.get(item).equals(getString(R.string.bluetooth))){
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.BLUETOOTH, Utilities.BLUETOOTH);
                                    } else if(items.get(item).equals(getString(R.string.settings))){
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.SETTINGS, Utilities.SETTINGS);
                                    } else if(items.get(item).equals(getString(R.string.wifi))){
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.WIFI, Utilities.WIFI);
                                    } else if(items.get(item).equals(getString(R.string.screenshot))){
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.SCREENSHOT, Utilities.SCREENSHOT);
                                    } else if(items.get(item).equals(getString(R.string.sleep))){
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.SLEEP, Utilities.SLEEP);
                                    }else if(items.get(item).equals(getString(R.string.mode_silent))){
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_SILENT, Utilities.MODE_SILENT);
                                    }else if(items.get(item).equals(getString(R.string.mode_vibrate))){
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_VIBRATE, Utilities.MODE_VIBRATE);
                                    }else if(items.get(item).equals(getString(R.string.mode_normal))){
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_NORMAL, Utilities.MODE_NORMAL);
                                    }else if (items.get(item).equals(getString(R.string.nothing))) {
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary));
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), null, null, null);
                                    } else {
                                        swipeBottomTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        String packageName = null;
                                        String className = null;
                                        for (AppInfo app : AllAppsList.data) {
                                            if (items.get(item).equalsIgnoreCase(app.title.toString())) {
                                                packageName = app.componentName.getPackageName();
                                                className = app.componentName.getClassName();
                                            }
                                        }
                                        Utilities.setAppSwipeBottomTwoFingersValue(activity.getApplicationContext(), items.get(item), packageName, className);
                                    }
                                }
                            }).show();
                    return true;
                }
            });

            swipeUpTwoFingersAppPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final List<String> items = new ArrayList<String>();
                    final List<Bitmap> icons = new ArrayList<Bitmap>();
                    items.add(getString(R.string.nothing));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_clear_black_24dp)));

                    items.add(getString(R.string.wifi));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_wifi_black_24dp)));

                    items.add(getString(R.string.torch));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_lightbulb_outline_black_24dp)));

                    items.add(getString(R.string.bluetooth));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_bluetooth_black_24dp)));

                    items.add(getString(R.string.screenshot));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_photo_black_24dp)));

                    items.add(getString(R.string.sleep));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_screen_lock_portrait_black_24dp)));

                    items.add(getString(R.string.mode_silent));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_volume_off_black_24dp)));

                    items.add(getString(R.string.mode_vibrate));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_vibration_black_24dp)));

                    items.add(getString(R.string.mode_normal));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_volume_up_black_24dp)));

                    items.add(getString(R.string.settings));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_settings_black_24dp)));
                    for (AppInfo app : apps) {
                        items.add(app.title.toString());
                        icons.add(app.iconBitmap);
                    }
                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.alert_choose_app))
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items.get(item).equals(getString(R.string.torch))) {
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.TORCH, Utilities.TORCH);
                                    } else if(items.get(item).equals(getString(R.string.bluetooth))){
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.BLUETOOTH, Utilities.BLUETOOTH);
                                    } else if(items.get(item).equals(getString(R.string.settings))){
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.SETTINGS, Utilities.SETTINGS);
                                    } else if(items.get(item).equals(getString(R.string.wifi))){
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.WIFI, Utilities.WIFI);
                                    } else if(items.get(item).equals(getString(R.string.screenshot))){
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.SCREENSHOT, Utilities.SCREENSHOT);
                                    } else if(items.get(item).equals(getString(R.string.sleep))){
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.SLEEP, Utilities.SLEEP);
                                    }else if(items.get(item).equals(getString(R.string.mode_silent))){
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_SILENT, Utilities.MODE_SILENT);
                                    }else if(items.get(item).equals(getString(R.string.mode_vibrate))){
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_VIBRATE, Utilities.MODE_VIBRATE);
                                    }else if(items.get(item).equals(getString(R.string.mode_normal))){
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), items.get(item), Utilities.MODE_NORMAL, Utilities.MODE_NORMAL);
                                    }else if (items.get(item).equals(getString(R.string.nothing))) {
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary));
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), null, null, null);
                                    } else {
                                        swipeUpTwoFingersAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": " + items.get(item));
                                        String packageName = null;
                                        String className = null;
                                        for (AppInfo app : AllAppsList.data) {
                                            if (items.get(item).equalsIgnoreCase(app.title.toString())) {
                                                packageName = app.componentName.getPackageName();
                                                className = app.componentName.getClassName();
                                            }
                                        }
                                        Utilities.setAppSwipeUpTwoFingersValue(activity.getApplicationContext(), items.get(item), packageName, className);
                                    }
                                }
                            }).show();
                    return true;
                }
            });

            final Preference infoPref = findPreference(Utilities.INFORMATION);
            infoPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utilities.aboutAlertDialog(context);
                    return true;
                }
            });

            final Preference donationPref = findPreference(Utilities.DONATION);
            donationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent donationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=it.michelelacorte.githubdonation"));
                    startActivity(donationIntent);
                    return true;
                }
            });

            final Preference allAppsPref = findPreference(Utilities.ALL_APPS_SIZE);
            allAppsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(theme);
                    LinearLayout layout = new LinearLayout(activity.getApplicationContext());
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.setPadding(100, 50, 100, 100);


                    final NumberPicker allApps = new NumberPicker(activity.getApplicationContext());
                    final TextView allAppsTitle = new TextView(activity.getApplicationContext());
                    allAppsTitle.setText(getResources().getString(R.string.all_apps_dialog));
                    allApps.setMinValue(2);
                    allApps.setMaxValue(12);
                    allApps.setValue(Utilities.getAllAppsSizeDefaultValue(activity.getApplicationContext()));
                    allApps.setWrapSelectorWheel(false);

                    layout.addView(allAppsTitle);
                    layout.addView(allApps);


                    alert.setTitle(getResources().getString(R.string.all_apps_dialog));
                    alert.setView(layout);

                    alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Utilities.setAllAppsSizeDefaultValue(activity.getApplicationContext(), allApps.getValue());
                        }
                    });

                    alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                    return true;
                }
            });


            final Preference folderBackgroundPref = findPreference(Utilities.FOLDER_BACKGROUND);
            folderBackgroundPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int initialColor;
                    if ((initialColor = Utilities.getFolderBackgroundPrefEnabled(activity.getApplicationContext())) == -1) {
                        initialColor = 0xffffffff;
                    }
                    ColorPickerDialogBuilder
                            .with(context, themeInt)
                            .setTitle(getString(R.string.choose_color))
                            .initialColor(initialColor)
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .setOnColorSelectedListener(new OnColorSelectedListener() {
                                @Override
                                public void onColorSelected(int selectedColor) {
                                    //Integer.toHexString(selectedColor);
                                }
                            })
                            .setPositiveButton(getString(R.string.ok), new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    Utilities.setFolderBackgroundValue(activity.getApplicationContext(), selectedColor);
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .build()
                            .show();
                    return true;
                }
            });

            final Preference transparentPref = findPreference(Utilities.FOLDER_TRANSPARENT);
            final Preference folderPreviewBackgroundPref = findPreference(Utilities.FOLDER_PREVIEW_BACKGROUND);

            if (Utilities.isAllowFolderTransparentPrefEnabled(activity.getApplicationContext())) {
                folderPreviewBackgroundPref.setEnabled(false);

            } else if (!Utilities.isAllowFolderTransparentPrefEnabled(activity.getApplicationContext())) {
                folderPreviewBackgroundPref.setEnabled(true);

            }

            if (getResources().getBoolean(R.bool.allow_transparent_folder_on_launcher)) {
                getPreferenceScreen().removePreference(transparentPref);
            } else {
                transparentPref.setDefaultValue(true);
            }

            transparentPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (Utilities.isAllowFolderTransparentPrefEnabled(activity.getApplicationContext())) {
                        folderPreviewBackgroundPref.setEnabled(true);

                    } else if (!Utilities.isAllowFolderTransparentPrefEnabled(activity.getApplicationContext())) {
                        folderPreviewBackgroundPref.setEnabled(false);

                    }
                    return true;
                }
            });

            folderPreviewBackgroundPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int initialColor;
                    if ((initialColor = Utilities.getFolderPreviewBackgroundPrefEnabled(activity.getApplicationContext())) == -1) {
                        initialColor = 0xffffffff;
                    }
                    ColorPickerDialogBuilder
                            .with(context, themeInt)
                            .setTitle(getString(R.string.choose_color))
                            .initialColor(initialColor)
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .setOnColorSelectedListener(new OnColorSelectedListener() {
                                @Override
                                public void onColorSelected(int selectedColor) {
                                    //Integer.toHexString(selectedColor);
                                }
                            })
                            .setPositiveButton(getString(R.string.ok), new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    Utilities.setFolderPreviewBackgroundValue(activity.getApplicationContext(), selectedColor);
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .build()
                            .show();
                    return true;
                }
            });


            final Preference folderPreviewCirclePref = findPreference(Utilities.FOLDER_PREVIEW_CIRCLE);
            folderPreviewCirclePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int initialColor;
                    if ((initialColor = Utilities.getFolderPreviewCirclePrefEnabled(activity.getApplicationContext())) == -1) {
                        initialColor = 0xffffffff;
                    }
                    ColorPickerDialogBuilder
                            .with(context, themeInt)
                            .setTitle(getString(R.string.choose_color))
                            .initialColor(initialColor)
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .setOnColorSelectedListener(new OnColorSelectedListener() {
                                @Override
                                public void onColorSelected(int selectedColor) {
                                    //Integer.toHexString(selectedColor);
                                }
                            })
                            .setPositiveButton(getString(R.string.ok), new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    Utilities.setFolderPreviewCircleValue(activity.getApplicationContext(), selectedColor);
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .build()
                            .show();
                    return true;
                }
            });

            final Preference licensePref = findPreference(Utilities.LICENSE);
            licensePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Utilities.licenseAlertDialog(context);
                    return true;
                }
            });


            final Preference fingerprintPref = findPreference(Utilities.FINGERPRINT);

            if(!Utilities.checkFingerprintHardwareAndPermission(context, Launcher.getFingerprintManager())){
                fingerprintPref.setEnabled(false);
            }

            fingerprintPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int i = 0;
                    boolean needAuth = false;
                    selectedItemsBool = new boolean[AllAppsList.data.size()];
                    items = new CharSequence[AllAppsList.data.size()];

                    for (AppInfo app : apps) {
                        items[i] = app.title;
                        i++;
                    }

                    for(int j = 0; j < selectedItemsBool.length; j++){
                        if(Utilities.getFingerprintPosPrefEnabled(activity.getApplicationContext(), j) != -1){
                            selectedItemsBool[j] = true;
                            needAuth = true;
                        }else{
                            selectedItemsBool[j] = false;
                        }
                    }

                    if(needAuth){
                        startActivityForResult(new Intent(context, FingerprintActivitySettings.class), 200);
                    }else{
                        showFingerprintDialog(context, themeInt, items, selectedItemsBool);
                    }
                    return true;
                }
            });

            final Preference persistentSearchBarPref = findPreference(Utilities.PERSISENT_SEARCH_BAR);
            if (getResources().getBoolean(R.bool.allow_persistent_search_bar)) {
                getPreferenceScreen().removePreference(persistentSearchBarPref);
            } else {
                persistentSearchBarPref.setDefaultValue(true);
            }


            final Preference dockBackgroundPref = findPreference(Utilities.DOCK_BACKGROUND);
            dockBackgroundPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int initialColor;
                    if ((initialColor = Utilities.getDockBackgroundPrefEnabled(activity.getApplicationContext())) == -1) {
                        initialColor = 0xffffffff;
                    }
                    ColorPickerDialogBuilder
                            .with(context, themeInt)
                            .setTitle(getString(R.string.choose_color))
                            .initialColor(initialColor)
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .setOnColorSelectedListener(new OnColorSelectedListener() {
                                @Override
                                public void onColorSelected(int selectedColor) {
                                    //Integer.toHexString(selectedColor);
                                }
                            })
                            .setPositiveButton(getString(R.string.ok), new ColorPickerClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                    Utilities.setDockBackgroundValue(activity.getApplicationContext(), selectedColor);
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .build()
                            .show();
                    return true;
                }
            });


            final Preference passwordPref = findPreference(Utilities.PASSWORD);

            passwordPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int i = 0;
                    boolean needAuth = false;
                    selectedItemsBool = new boolean[AllAppsList.data.size()];
                    items = new CharSequence[AllAppsList.data.size()];

                    for (AppInfo app : apps) {
                        items[i] = app.title;
                        i++;
                    }

                    for(int j = 0; j < selectedItemsBool.length; j++){
                        if(Utilities.getPasswordPosPrefEnabled(activity.getApplicationContext(), j) != -1){
                            selectedItemsBool[j] = true;
                            needAuth = true;
                        }else{
                            selectedItemsBool[j] = false;
                        }
                    }

                    SharedPreferences preferences = context.getSharedPreferences(Utilities.PASSWORD_SHARED_PREF, MODE_PRIVATE);
                    String passEncrypted = preferences.getString(Utilities.encrypt("password"), Utilities.encrypt("NULLPASS"));
                    String pass = Utilities.decrypt(passEncrypted);
                    if(pass.equals("NULLPASS")){
                        showPasswordInputDialog(theme, context);
                    }else {
                        if (needAuth) {
                            startActivityForResult(new Intent(context, PasswordActivitySettings.class), 300);
                        } else {
                            showPasswordDialog(context, themeInt, items, selectedItemsBool);
                        }
                    }
                    return true;
                }
            });

            final Preference passwordResetPref = findPreference(Utilities.PASSWORD_RESET);
            passwordResetPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SharedPreferences preferences = context.getSharedPreferences(Utilities.PASSWORD_SHARED_PREF, MODE_PRIVATE);
                    String passEncrypted = preferences.getString(Utilities.encrypt("password"), Utilities.encrypt("NULLPASS"));
                    String pass = Utilities.decrypt(passEncrypted);
                    if(pass.equals("NULLPASS")){
                        showPasswordInputDialog(theme, context);
                    }else {
                        startActivityForResult(new Intent(context, PasswordActivitySettings.class), 301);
                    }
                    return true;
                }
            });


        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (requestCode == 200) {
                if(resultCode == Activity.RESULT_OK){
                    boolean result=data.getBooleanExtra("result", false);
                    if(result){
                        showFingerprintDialog(context, themeInt, items, selectedItemsBool);
                    }
                }
            }

            if (requestCode == 300) {
                if(resultCode == Activity.RESULT_OK){
                    boolean result=data.getBooleanExtra("resultPassword", false);
                    if(result){
                        showPasswordDialog(context, themeInt, items, selectedItemsBool);
                    }
                }
            }

            if (requestCode == 301) {
                if(resultCode == Activity.RESULT_OK){
                    boolean result=data.getBooleanExtra("resultPassword", false);
                    if(result){
                        showPasswordInputDialog(theme, context);
                    }
                }
            }
        }

        public static void showFingerprintDialog(final Context context, int themeInt, final CharSequence[] items, final boolean[] selectedItemsBool){
            AlertDialog dialog = new AlertDialog.Builder(context, themeInt)
                    .setTitle(context.getString(R.string.select_apps))
                    .setMultiChoiceItems(items, selectedItemsBool, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                            for (int i = 0; i < AllAppsList.data.size(); i++) {
                                if (i == indexSelected && isChecked) {
                                    CharSequence selectedName = items[indexSelected];
                                    String packageName = AllAppsList.data.get(i).componentName.getPackageName();
                                    String className = AllAppsList.data.get(i).componentName.getClassName();
                                    Utilities.setFingerprintAppsValue(context, selectedName, packageName, className, i);
                                } else if (i == indexSelected && !isChecked) {
                                    Utilities.setFingerprintNullAppsValue(context, i);
                                }
                            }
                        }
                    }).setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing
                        }
                    }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing
                        }
                    }).create();
            dialog.show();
        }

        public static void showPasswordDialog(final Context context, int themeInt, final CharSequence[] items, final boolean[] selectedItemsBool){
            AlertDialog dialog = new AlertDialog.Builder(context, themeInt)
                    .setTitle(context.getString(R.string.select_apps))
                    .setMultiChoiceItems(items, selectedItemsBool, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                            for (int i = 0; i < AllAppsList.data.size(); i++) {
                                if (i == indexSelected && isChecked) {
                                    CharSequence selectedName = items[indexSelected];
                                    String packageName = AllAppsList.data.get(i).componentName.getPackageName();
                                    String className = AllAppsList.data.get(i).componentName.getClassName();
                                    Utilities.setPasswordAppsValue(context, selectedName, packageName, className, i);
                                } else if (i == indexSelected && !isChecked) {
                                    Utilities.setPasswordNullAppsValue(context, i);
                                }
                            }
                        }
                    }).setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing
                        }
                    }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            //Do nothing
                        }
                    }).create();
            dialog.show();
        }

        public static void showPasswordInputDialog(ContextThemeWrapper theme, final Context context){
            AlertDialog.Builder alert = new AlertDialog.Builder(theme);
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(100, 50, 100, 100);


            final EditText passwordBox = new EditText(context);
            passwordBox.setHint(context.getResources().getString(R.string.dialog_password));
            passwordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordBox.getBackground().mutate().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            layout.addView(passwordBox);


            final EditText passwordCheckBox = new EditText(context);
            passwordCheckBox.setHint(context.getResources().getString(R.string.dialog_password));
            passwordBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordCheckBox.getBackground().mutate().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
            layout.addView(passwordCheckBox);

            alert.setTitle(context.getResources().getString(R.string.dialog_password_title));

            alert.setView(layout);

            alert.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if(passwordBox.getText() != null && passwordCheckBox.getText() != null){
                        if(passwordBox.getText().toString().equals(passwordCheckBox.getText().toString())){
                            SharedPreferences preferences = context.getSharedPreferences(Utilities.PASSWORD_SHARED_PREF, MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(Utilities.encrypt("password"), Utilities.encrypt(passwordBox.getText().toString()));
                            editor.apply();
                        }
                    }
                }
            });

            alert.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //Do nothing
                }
            });
            alert.show();
        }


        @Override
        public void onDestroy() {
            if (mRotationLockObserver != null) {
                getActivity().getContentResolver().unregisterContentObserver(mRotationLockObserver);
                mRotationLockObserver = null;
            }
            Intent intent = Launcher.getLauncherActivity().getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            Launcher.getLauncherActivity().finish();
            startActivity(intent);
            super.onDestroy();
        }


        /**
         * Content observer which listens for system auto-rotate setting changes, and enables/disables
         * the launcher rotation setting accordingly.
         */
        private static class SystemDisplayRotationLockObserver extends ContentObserver {

            private final Preference mRotationPref;
            private final ContentResolver mResolver;

            public SystemDisplayRotationLockObserver(
                    Preference rotationPref, ContentResolver resolver) {
                super(new Handler());
                mRotationPref = rotationPref;
                mResolver = resolver;
            }

            @Override
            public void onChange(boolean selfChange) {
                boolean enabled = Settings.System.getInt(mResolver,
                        Settings.System.ACCELEROMETER_ROTATION, 1) == 1;
                mRotationPref.setEnabled(enabled);
                mRotationPref.setSummary(enabled
                        ? R.string.allow_rotation_desc : R.string.allow_rotation_blocked_desc);
            }
        }
    }
}
