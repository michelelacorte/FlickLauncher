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

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.provider.Settings.System;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.ListUpdateCallback;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.util.ApplicationInfo;
import com.android.launcher3.util.ArrayAdapterWithIcon;
import com.android.launcher3.util.StringFilter;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.ArrayList;
import java.util.List;

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


            final Preference iconPref = findPreference(Utilities.ALLOW_CIRCULAR_ICON_PREFERENCE_KEY);
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
            } else {
                theme = new ContextThemeWrapper(activity, R.style.AlertDialogCustom);
            }
            final int themeInt;
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                themeInt = R.style.AlertDialogCustomAPI23;
            } else {
                themeInt = R.style.AlertDialogCustom;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context = getContext();
            } else {
                context = activity.getApplicationContext();
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
                        Utilities.answerToChangeDefaultLauncher(activity.getApplicationContext());
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
                    Utilities.answerToRestartLauncher(Launcher.getLauncherActivity(), activity.getApplicationContext(), 2000);
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
                    for (AppInfo app : AllAppsList.data) {
                        items.add(app.title.toString());
                        icons.add(app.iconBitmap);
                    }
                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.alert_choose_app))
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items.get(item).equals(getString(R.string.nothing))) {
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
                    //items.add("Wi-Fi");
                    //icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_wifi_black_24dp)));
                    items.add(getString(R.string.nothing));
                    icons.add(Utils.convertDrawableToBitmap(ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.ic_clear_black_24dp)));
                    for (AppInfo app : AllAppsList.data) {
                        items.add(app.title.toString());
                        icons.add(app.iconBitmap);
                    }
                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.alert_choose_app))
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items.get(item).equals("Wi-Fi")) {
                                        swipeUpAppPref.setSummary(getString(R.string.choose_double_tap_summary) + ": Wi-Fi");
                                        Utilities.setAppSwipeUpValue(activity.getApplicationContext(), items.get(item), "WIFI", "WIFI");
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
                    for (AppInfo app : AllAppsList.data) {
                        items.add(app.title.toString());
                        icons.add(app.iconBitmap);
                    }
                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.alert_choose_app))
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items.get(item).equals(getString(R.string.nothing))) {
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
                    for (AppInfo app : AllAppsList.data) {
                        items.add(app.title.toString());
                        icons.add(app.iconBitmap);
                    }
                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.alert_choose_app))
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items.get(item).equals(getString(R.string.nothing))) {
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
                    for (AppInfo app : AllAppsList.data) {
                        items.add(app.title.toString());
                        icons.add(app.iconBitmap);
                    }
                    ListAdapter adapter = new ArrayAdapterWithIcon(getActivity(), items, icons);

                    new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.alert_choose_app))
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    if (items.get(item).equals(getString(R.string.nothing))) {
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
