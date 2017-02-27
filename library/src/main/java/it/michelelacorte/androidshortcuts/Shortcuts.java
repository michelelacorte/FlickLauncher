package it.michelelacorte.androidshortcuts;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;

import it.michelelacorte.androidshortcuts.util.StyleOption;
import it.michelelacorte.androidshortcuts.util.Utils;

/**
 * Created by Michele on 24/11/2016.
 */

public class Shortcuts implements Serializable{
    private final String TAG = "Shortcuts";
    private static final long serialVersionUID = -29238982928391L;

    private String shortcutsText;
    private int shortcutsImage;
    private Bitmap shortcutsImageBitmap;
    private Bitmap shortcutsImageBadgeBitmap;
    private int rank;
    private String targetClass;
    private String targetPackage;

    private View.OnClickListener onShortcutsClickListener;
    private View.OnClickListener onShortcutsOptionClickListener;

    public static final int MAX_CHAR_SHORTCUTS = 20;

    /**
     * Public constructor for create custom shortcuts
     * @param shortcutsImage int
     * @param shortcutsText String (Max lenght 16 char)
     * @param onShortcutsClickListener View.OnClickListener
     */
    public Shortcuts(int shortcutsImage, String shortcutsText, View.OnClickListener onShortcutsClickListener){
        this.shortcutsImage = shortcutsImage;
        if(shortcutsText.toCharArray().length > MAX_CHAR_SHORTCUTS){
            this.shortcutsText = "NULL";
            Log.e(TAG, "Impossible to have string > " + MAX_CHAR_SHORTCUTS + " chars, setted to NULL string!");
        }else{
            this.shortcutsText = shortcutsText;
        }
        if(onShortcutsClickListener != null) {
            this.onShortcutsClickListener = onShortcutsClickListener;
        }else{
            Log.e(TAG, "OnClickListener must be different from NULL");
        }
    }


    /**
     * Public constructor for create custom shortcuts
     * @param shortcutsImage int
     * @param shortcutsText String (Max lenght 16 char)
     */
    public Shortcuts(int shortcutsImage, String shortcutsText){
        this.shortcutsImage = shortcutsImage;
        if(shortcutsText.toCharArray().length > MAX_CHAR_SHORTCUTS){
            this.shortcutsText = "NULL";
            Log.e(TAG, "Impossible to have string > " + MAX_CHAR_SHORTCUTS + " chars, setted to NULL string!");
        }else {
            this.shortcutsText = shortcutsText;
        }
    }


    /**
     * Public constructor for create custom shortcuts
     * @param shortcutsImage Bitmap
     * @param shortcutsText String
     */
    public Shortcuts(Bitmap shortcutsImage, String shortcutsText){
        this.shortcutsImageBitmap = shortcutsImage;
        this.shortcutsImage = 0;
        if(shortcutsText.toCharArray().length > MAX_CHAR_SHORTCUTS){
            this.shortcutsText = "NULL";
            Log.e(TAG, "Impossible to have string > " + MAX_CHAR_SHORTCUTS + " chars, setted to NULL string!");
        }else {
            this.shortcutsText = shortcutsText;
        }
    }

    /**
     * Public constructor for create custom shortcuts, only for remote use.
     * @param shortcutsImage Bitmap
     * @param shortcutsText String
     * @param targetClass String
     * @param targetPackage String
     */
    public Shortcuts(int shortcutsImage, String shortcutsText, String targetClass, String targetPackage){
        this.shortcutsImage = shortcutsImage;
        if(shortcutsText.toCharArray().length > MAX_CHAR_SHORTCUTS){
            this.shortcutsText = "NULL";
            Log.e(TAG, "Impossible to have string > " + MAX_CHAR_SHORTCUTS + " chars, setted to NULL string!");
        }else{
            this.shortcutsText = shortcutsText;
        }
        this.targetClass= targetClass;
        this.targetPackage = targetPackage;
    }


    /**
     * Public constructor for create custom shortcuts, only for remote use.
     * @param shortcutsImage Bitmap
     * @param shortcutsText String
     * @param targetClass String
     * @param targetPackage String
     */
    public Shortcuts(Bitmap shortcutsImage, String shortcutsText, String targetClass, String targetPackage){
        this.shortcutsImageBitmap = shortcutsImage;
        this.shortcutsImage = 0;
        if(shortcutsText.toCharArray().length > MAX_CHAR_SHORTCUTS){
            this.shortcutsText = "NULL";
            Log.e(TAG, "Impossible to have string > " + MAX_CHAR_SHORTCUTS + " chars, setted to NULL string!");
        }else{
            this.shortcutsText = shortcutsText;
        }
        this.targetClass = targetClass;
        this.targetPackage = targetPackage;
    }

    @TargetApi(25)
    @RequiresApi(25)
    public Shortcuts(Bitmap shortcutsImage, Bitmap shortcutsImageBadge, String shortcutsText, String targetClass, String targetPackage, int rank){
        this.shortcutsImageBitmap = shortcutsImage;
        this.shortcutsImageBadgeBitmap = shortcutsImageBadge;
        this.rank = rank;
        this.shortcutsImage = 0;
        if(shortcutsText.toCharArray().length > MAX_CHAR_SHORTCUTS){
            this.shortcutsText = "NULL";
            Log.e(TAG, "Impossible to have string > " + MAX_CHAR_SHORTCUTS + " chars, setted to NULL string!");
        }else{
            this.shortcutsText = shortcutsText;
        }
        this.targetClass = targetClass;
        this.targetPackage = targetPackage;
    }


    /**
     * Public constructor for create custom shortcuts
     * @param shortcutsImage Bitmap
     * @param shortcutsText String
     * @param onShortcutsClickListener View.OnClickListener
     * @param onShortcutsOptionClickListener View.OnClickListener
     */
    public Shortcuts(int shortcutsImage, String shortcutsText, View.OnClickListener onShortcutsClickListener, View.OnClickListener onShortcutsOptionClickListener){
        this.shortcutsImage = shortcutsImage;
        if(shortcutsText.toCharArray().length > MAX_CHAR_SHORTCUTS){
            this.shortcutsText = "NULL";
            Log.e(TAG, "Impossible to have string > " + MAX_CHAR_SHORTCUTS + " chars, setted to NULL string!");
        }else{
            this.shortcutsText = shortcutsText;
        }
        if(onShortcutsClickListener != null) {
            this.onShortcutsClickListener = onShortcutsClickListener;
        }else{
            Log.e(TAG, "OnClickListener must be different from NULL");
        }
        if(onShortcutsOptionClickListener != null) {
            this.onShortcutsOptionClickListener = onShortcutsOptionClickListener;
        }else{
            Log.e(TAG, "OnClickListener must be different from NULL");
        }
    }


    /**
     * Public method to initializate shortcuts, do not use this!
     * @param layout View
     */
    public void init(View layout, int optionLayoutStyle, final Activity activity, final Drawable packageImage, final ShortcutsCreation shortcutsCreation){
        ImageView mShortcutsImage = (ImageView) layout.findViewById(R.id.shortcut_image);
        TextView mShortcutsText = (TextView) layout.findViewById(R.id.shortcut_text);
        RelativeLayout mShortcutsParent = (RelativeLayout) layout.findViewById(R.id.shortcut_parent);
        ImageView mShortcutsOptions = (ImageView) layout.findViewById(R.id.shortcut_options);

        if(onShortcutsClickListener != null)
            mShortcutsParent.setOnClickListener(onShortcutsClickListener);

        mShortcutsParent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    if(RemoteShortcuts.USE_SHORTCUTS_FROM_API_25){
                        shortcutsCreation.clearAllLayout();
                        Utils.createShortcutsOnLauncher(activity, shortcutsImageBitmap, shortcutsText, targetClass, targetPackage, packageImage, shortcutsImageBadgeBitmap);
                    } else if(shortcutsImageBitmap != null && !RemoteShortcuts.USE_SHORTCUTS_FROM_API_25) {
                        shortcutsCreation.clearAllLayout();
                        Utils.createShortcutsOnLauncher(activity, shortcutsImageBitmap, shortcutsText, targetClass, targetPackage, packageImage, null);
                    }else if (!RemoteShortcuts.USE_SHORTCUTS_FROM_API_25){
                        shortcutsCreation.clearAllLayout();
                        Drawable drawable = ContextCompat.getDrawable(activity.getApplicationContext(), shortcutsImage);
                        Bitmap toBitmap = Utils.convertDrawableToBitmap(drawable);
                        Utils.createShortcutsOnLauncher(activity, toBitmap, shortcutsText, targetClass, targetPackage, packageImage, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        if(targetPackage != null && targetClass != null) {
                mShortcutsParent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(targetPackage, targetClass));
                        activity.startActivity(intent);
                    }
                });
        }

        if(shortcutsImage != 0) {
            if(packageImage != null) {
                int color = Utils.getDominantColor(Utils.convertDrawableToBitmap(packageImage));
                if (color != 0) {
                    Bitmap shortcutsImageBitmap = BitmapFactory.decodeResource(activity.getResources(), shortcutsImage);
                    Bitmap coloredBitmap = Utils.setColorOnBitmap(shortcutsImageBitmap, color);
                    mShortcutsImage.setImageBitmap(coloredBitmap);
                } else {
                    mShortcutsImage.setImageResource(shortcutsImage);
                }
            }else{
                mShortcutsImage.setImageResource(shortcutsImage);
            }
        }
        if(shortcutsImageBitmap != null) {
            if(packageImage != null) {
                int color = Utils.getDominantColor(Utils.convertDrawableToBitmap(packageImage));
                if (color != 0) {
                    if(RemoteShortcuts.USE_SHORTCUTS_FROM_API_25 || ShortcutsCreation.USE_SHORTCUTS_FOR_LAUNCHER_3){
                        mShortcutsImage.setImageBitmap(shortcutsImageBitmap);
                    }else{
                        Bitmap coloredBitmap = Utils.setColorOnBitmap(shortcutsImageBitmap, color);
                        mShortcutsImage.setImageBitmap(coloredBitmap);
                    }
                } else {
                    mShortcutsImage.setImageBitmap(shortcutsImageBitmap);
                }
            }else{
                mShortcutsImage.setImageBitmap(shortcutsImageBitmap);
            }
        }
        mShortcutsText.setText(shortcutsText);

        if(onShortcutsOptionClickListener != null) {
            mShortcutsOptions.setOnClickListener(onShortcutsOptionClickListener);
        } else if(targetClass != null && targetPackage != null) {
            mShortcutsOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if(RemoteShortcuts.USE_SHORTCUTS_FROM_API_25){
                            shortcutsCreation.clearAllLayout();
                            Utils.createShortcutsOnLauncher(activity, shortcutsImageBitmap, shortcutsText, targetClass, targetPackage, packageImage, shortcutsImageBadgeBitmap);
                        }
                        if(shortcutsImageBitmap != null && !RemoteShortcuts.USE_SHORTCUTS_FROM_API_25) {
                            shortcutsCreation.clearAllLayout();
                            Utils.createShortcutsOnLauncher(activity, shortcutsImageBitmap, shortcutsText, targetClass, targetPackage, packageImage, null);
                        }else if (!RemoteShortcuts.USE_SHORTCUTS_FROM_API_25){
                            shortcutsCreation.clearAllLayout();
                            Drawable drawable = ContextCompat.getDrawable(activity.getApplicationContext(), shortcutsImage);
                            Bitmap toBitmap = Utils.convertDrawableToBitmap(drawable);
                            Utils.createShortcutsOnLauncher(activity, toBitmap, shortcutsText, targetClass, targetPackage, packageImage, null);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if(StyleOption.getStyleFromInt(optionLayoutStyle) == -1){
            mShortcutsOptions.setVisibility(View.INVISIBLE);
        }else {
            mShortcutsOptions.setBackgroundResource(StyleOption.getStyleFromInt(optionLayoutStyle));
        }

        Log.d(TAG, "Init completed!");
    }

    /**
     * Get target class string
     * @return String
     */
    public String getTargetClass() {
        return targetClass;
    }

    /**
     * Get target package string
     * @return String
     */
    public String getTargetPackage() {
        return targetPackage;
    }

    /**
     * Get listener of shortcuts
     * @return View.OnClickListener
     */
    public View.OnClickListener getOnShortcutsClickListener() {
        return onShortcutsClickListener;
    }

    /**
     * Get shortcuts text
     * @return String
     */
    public String getShortcutsText() {
        return shortcutsText;
    }

    /**
     * Get shortcuts image
     * @return Int
     */
    public int getShortcutsImage() {
        return shortcutsImage;
    }

    /**
     * Get shortcuts image
     * @return Bitmap
     */
    public Bitmap getShortcutsImageBitmap() {
        return shortcutsImageBitmap;
    }

    /**
     * Get listener of option menù (right menù of shortcuts)
     * @return View.OnClickListener
     */
    public View.OnClickListener getOnShortcutsOptionClickListener() {
        return onShortcutsOptionClickListener;
    }


    /**
     * Get rounded image for icon in launcher
     * @return Bitmap
     */
    @TargetApi(25)
    @RequiresApi(25)
    public Bitmap getShortcutsImageBadgeBitmap() {
        return shortcutsImageBadgeBitmap;
    }

    /**
     * Get rank of shortcuts
     * @return int
     */
    @TargetApi(25)
    @RequiresApi(25)
    public int getRank() {
        return rank;
    }
}
