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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.provider.Settings.System;
import android.provider.Telephony;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.util.ApplicationInfo;

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

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(LauncherFiles.SHARED_PREFERENCES_KEY);
            addPreferencesFromResource(R.xml.launcher_preferences);

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



            Preference iconPref = findPreference(Utilities.ALLOW_CIRCULAR_ICON_PREFERENCE_KEY);
            if (getResources().getBoolean(R.bool.allow_circular_icon)) {
                getPreferenceScreen().removePreference(iconPref);
            } else {
                iconPref.setDefaultValue(true);
            }

            final Preference gridPref = findPreference(Utilities.GRID_SIZE);
            final Activity activity = this.getActivity();

            final ContextThemeWrapper theme;
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
               theme = new ContextThemeWrapper(activity, R.style.AlertDialogCustomAPI23);
            }else{
                theme = new ContextThemeWrapper(activity, R.style.AlertDialogCustom);
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
                    if(!ApplicationInfo.isMyLauncherDefault(getContext())) {
                        Utilities.answerToChangeDefaultLauncher(getContext());
                    }else{
                        Toast.makeText(getContext(), getResources().getString(R.string.default_launcher_already_set), Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            });

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
            //Launcher.getLauncherActivity().recreate();
            super.onDestroy();
        }
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
