package com.android.launcher3.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;

import java.util.ArrayList;
import java.util.List;

import it.michelelacorte.androidshortcuts.Shortcuts;
import it.michelelacorte.androidshortcuts.ShortcutsCreation;
import it.michelelacorte.androidshortcuts.util.Utils;

/**
 * Created by Michele on 04/03/2017.
 */

public class ShortcutsManager {
    private static final String TAG = "ShortcutsManager";
    private static List<Shortcuts> shortcutsesFavorite = new ArrayList<Shortcuts>();

    public static List<Shortcuts> getShortcutsBasedOnTag(final Context context, final Activity activity, final ShortcutInfo shortcutInfo, final Drawable icon){
        final List<Shortcuts> shortcutses = new ArrayList<Shortcuts>();
        Bitmap bitmap;

        switch (shortcutInfo.getTargetComponent().getPackageName()){
            case "com.android.chrome":
                bitmap = getBitmap(R.drawable.ic_visibility_off_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.chrome_blue));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.new_incognito_tab), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName(), "chromium.shortcut.action.OPEN_NEW_INCOGNITO_TAB"));
                bitmap = getBitmap(R.drawable.ic_add_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.chrome_blue));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.new_tab), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName(), ""));
                break;
            case "com.google.android.apps.maps":
                bitmap = getBitmap(R.drawable.ic_home_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.maps_green));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.maps_home), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName(), ""));

                bitmap = getBitmap(R.drawable.ic_work_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.maps_green));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.maps_work), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName(), ""));
                break;
            case "com.ebay.mobile":
                bitmap = getBitmap(R.drawable.ic_search_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.ebay_search));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.search), "com.ebay.mobile.search.landing.SearchLandingPageActivity", shortcutInfo.getTargetComponent().getPackageName(), "android.intent.action.VIEW"));

                bitmap = getBitmap(R.drawable.ic_remove_red_eye_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.ebay_watching)); //com.ebay.mobile.connection.myebay.WatchingHandler
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.ebay_watching), "com.ebay.mobile.activities.MainActivity", shortcutInfo.getTargetComponent().getPackageName(), "android.intent.action.VIEW"));

                bitmap = getBitmap(R.drawable.ic_local_offer_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.ebay_local_offer)); //com.ebay.mobile.activities.SellingActivity
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.ebay_local_offer), "com.ebay.mobile.activities.MainActivity", shortcutInfo.getTargetComponent().getPackageName(), "android.intent.action.VIEW"));

                bitmap = getBitmap(R.drawable.ic_youtube_searched_for_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.ebay_following)); //com.ebay.mobile.following.BrowseFollowingActivity
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.ebay_following), "com.ebay.mobile.activities.MainActivity", shortcutInfo.getTargetComponent().getPackageName(), "android.intent.action.VIEW"));
                break;
            case "com.evernote":
                bitmap = getBitmap(R.drawable.ic_search_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.evernote_green));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.search), "com.evernote.ui.WidgetSearchActivity", shortcutInfo.getTargetComponent().getPackageName(), ""));

                bitmap = getBitmap(R.drawable.ic_note_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.evernote_green));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.evernote_note), "com.evernote.ui.dialog.QuickNoteDialogActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.evernote.widget.action.CREATE_QUICK_NOTE"));

                bitmap = getBitmap(R.drawable.ic_mic_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.evernote_green));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.evernote_audio), "com.evernote.ui.WidgetNewNoteReroutingActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.evernote.widget.action.NEW_VOICE_NOTE"));

                bitmap = getBitmap(R.drawable.ic_photo_camera_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.evernote_green));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.evernote_camera), "com.evernote.ui.WidgetNewNoteReroutingActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.evernote.widget.action.NEW_SNAPSHOT"));
                break;
            case "com.google.android.gm":
                bitmap = getBitmap(R.drawable.ic_mode_edit_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.gmail_red));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.gmail_compose), "com.google.android.gm.ConversationListActivityGmail", shortcutInfo.getTargetComponent().getPackageName(), ""));
                break;
            case "com.google.android.apps.fireball":
                bitmap = getBitmap(R.drawable.ic_add_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.allo_conversation), "com.google.android.apps.fireball.ui.conversationlist.ConversationListActivity", shortcutInfo.getTargetComponent().getPackageName(), ""));
                break;
            case "com.google.android.apps.docs":
                bitmap = getBitmap(R.drawable.ic_file_upload_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.drive_blu));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.drive_upload), "com.google.android.apps.docs.app.NewMainProxyActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.apps.docs.CreateNewDocument.UPLOAD_FILE"));

                bitmap = getBitmap(R.drawable.ic_photo_camera_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.drive_blu));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.drive_scan), "com.google.android.apps.docs.app.NewMainProxyActivity", shortcutInfo.getTargetComponent().getPackageName(), "android.intent.action.SEND"));

                bitmap = getBitmap(R.drawable.ic_search_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.drive_blu));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.search), "com.google.android.apps.docs.app.NewMainProxyActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.apps.docs.actions.SEARCH_SHORTCUT_ACTION"));
                break;
            case "com.google.android.youtube":
                bitmap = getBitmap(R.drawable.ic_search_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.search), "com.google.android.apps.youtube.app.WatchWhileActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.youtube.action.open.search"));

                bitmap = getBitmap(R.drawable.ic_subscriptions_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.youtube_sub), "com.google.android.youtube.app.honeycomb.Shell$HomeActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.youtube.action.open.subscriptions"));

                bitmap = getBitmap(R.drawable.ic_whatshot_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.youtube_trending), "com.google.android.youtube.app.honeycomb.Shell$HomeActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.youtube.action.open.trending"));
                break;
            case "com.google.android.calendar":
                bitmap = getBitmap(R.drawable.ic_event_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.calendar_event), "com.android.calendar.event.LaunchInfoActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.calendar.EVENT_INSERT"));

                bitmap = getBitmap(R.drawable.ic_add_alarm_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.calendar_reminder), "com.android.calendar.event.LaunchInfoActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.calendar.REMINDER_INSERT"));
                break;
            case "com.google.android.apps.photos":
                bitmap = getBitmap(R.drawable.ic_phonelink_erase_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.photo_free_space), "com.google.android.apps.photos.home.HomeActivity", shortcutInfo.getTargetComponent().getPackageName(), ""));

                bitmap = getBitmap(R.drawable.ic_search_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.photo_lucky), "com.google.android.apps.photos.home.HomeActivity", shortcutInfo.getTargetComponent().getPackageName(), ""));
                break;
            case "com.google.android.apps.docs.editors.docs":
                bitmap = getBitmap(R.drawable.ic_insert_drive_file_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.docs_new), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName(), "android.intent.action.VIEW"));

                bitmap = getBitmap(R.drawable.ic_format_align_justify_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.docs_template), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName(), "android.intent.action.VIEW"));

                bitmap = getBitmap(R.drawable.ic_search_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.search), "com.google.android.apps.docs.app.NewMainProxyActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.apps.docs.actions.SEARCH_SHORTCUT_ACTION"));
                break;
            case "com.oasisfeng.greenify":
                bitmap = getBitmap(R.drawable.ic_do_not_disturb_on_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.greenify_hibernate), "com.oasisfeng.greenify.GreenifyShortcut", shortcutInfo.getTargetComponent().getPackageName(), "com.oasisfeng.greenify.action.HIBERNATE"));

                bitmap = getBitmap(R.drawable.ic_do_not_disturb_on_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.greenify_sleep), "com.oasisfeng.greenify.GreenifyShortcut", shortcutInfo.getTargetComponent().getPackageName(), "com.oasisfeng.greenify.action.SLEEP"));
                break;
            case "com.google.android.videos":
                bitmap = getBitmap(R.drawable.ic_movie_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.movies_my), "com.google.android.videos.activity.LauncherActivity", shortcutInfo.getTargetComponent().getPackageName(), "android.intent.action.VIEW"));

                bitmap = getBitmap(R.drawable.ic_tv_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.movies_tv), "com.google.android.videos.activity.LauncherActivity", shortcutInfo.getTargetComponent().getPackageName(), "android.intent.action.VIEW"));

                bitmap = getBitmap(R.drawable.ic_bookmark_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.movies_wishlist), "com.google.android.videos.activity.LauncherActivity", shortcutInfo.getTargetComponent().getPackageName(), "android.intent.action.VIEW"));
                break;
            case "com.google.android.music":
                bitmap = getBitmap(R.drawable.ic_casino_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.music_orange));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.photo_lucky), "com.google.android.music.ui.navigation.ShortcutTrampolineActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.music.shortcuts.START_IFL"));

                bitmap = getBitmap(R.drawable.ic_library_music_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.music_orange));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.music_library), "com.google.android.music.ui.navigation.ShortcutTrampolineActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.music.shortcuts.MY_LIBRARY"));

                bitmap = getBitmap(R.drawable.ic_history_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.music_orange));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.music_recent), "com.google.android.music.ui.navigation.ShortcutTrampolineActivity", shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.music.shortcuts.RECENT_ACTIVITY"));
                break;
            case "com.google.android.talk":
                bitmap = getBitmap(R.drawable.ic_message_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.hang_chat), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.apps.hangouts.shortcuts.new_conversation"));

                bitmap = getBitmap(R.drawable.ic_videocam_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.hang_video), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.apps.hangouts.shortcuts.new_video_call"));

                bitmap = getBitmap(R.drawable.ic_call_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.hang_call), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName(), "com.google.android.apps.hangouts.shortcuts.new_voice_call"));
                break;
            default:
                final String packageName = shortcutInfo.getTargetComponent().getPackageName();

                bitmap = getBitmap(R.drawable.ic_add_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.add_shortcuts), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.AlertDialogCustom));
                        LinearLayout layout = new LinearLayout(context);
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(100, 50, 100, 100);


                        final EditText titleBox = new EditText(context);
                        titleBox.setHint(context.getResources().getString(R.string.alert_title));
                        titleBox.getBackground().mutate().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                        layout.addView(titleBox);

                        final EditText packageBox = new EditText(context);
                        packageBox.setHint(shortcutInfo.getTargetComponent().getPackageName());
                        packageBox.getBackground().mutate().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                        layout.addView(packageBox);
                        packageBox.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                packageBox.setText(shortcutInfo.getTargetComponent().getPackageName());
                                return false;
                            }
                        });

                        final EditText classBox = new EditText(context);
                        classBox.setHint(shortcutInfo.getTargetComponent().getClassName());
                        classBox.getBackground().mutate().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
                        layout.addView(classBox);
                        classBox.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                classBox.setText(shortcutInfo.getTargetComponent().getClassName());
                                return false;
                            }
                        });


                        alert.setTitle(context.getResources().getString(R.string.add_shortcuts));

                        alert.setView(layout);

                        final Drawable iconD = icon;
                        alert.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(titleBox.getText() != null && packageBox.getText() != null && classBox.getText() != null){
                                    if(shortcutsesFavorite.size()+shortcutses.size() <= ShortcutsCreation.MAX_NUMBER_OF_SHORTCUTS) {
                                        Bitmap bitmap = getBitmap(R.drawable.ic_favorite_black_24dp, context);
                                        bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(iconD)));
                                        shortcutsesFavorite.add(new Shortcuts(bitmap, titleBox.getText().toString(), classBox.getText().toString(), packageBox.getText().toString(), "android.intent.action.VIEW"));
                                        Launcher.getShortcutsCreation().clearAllLayout();
                                    }else{
                                        Toast.makeText(context, context.getResources().getString(R.string.alert_max) + " " + ShortcutsCreation.MAX_NUMBER_OF_SHORTCUTS, Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });

                        alert.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Launcher.getShortcutsCreation().clearAllLayout();
                            }
                        });
                        alert.show();
                    }
                }));

                bitmap = getBitmap(R.drawable.ic_info_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.information), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ApplicationInfo.showInstalledAppDetails(context, packageName);
                        Launcher.getShortcutsCreation().clearAllLayout();
                    }
                }));

                bitmap = getBitmap(R.drawable.ic_delete_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, Utils.getDominantColor(drawableToBitmap(icon)));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.delete), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri packageURI = Uri.parse("package:"+packageName);
                        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(uninstallIntent);
                        Launcher.getShortcutsCreation().clearAllLayout();
                    }
                }));

                if(shortcutsesFavorite.size() > 0){
                    for(Shortcuts s : shortcutsesFavorite){
                        if(shortcutInfo.getTargetComponent().getPackageName().equals(s.getTargetPackage())) {
                            shortcutses.add(s);
                        }
                    }
                }
                break;
        }

        return shortcutses;
    }

    private static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    private static Bitmap getBitmap(int drawableRes, Context context) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
