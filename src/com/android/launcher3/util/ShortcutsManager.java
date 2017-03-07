package com.android.launcher3.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;

import java.util.ArrayList;
import java.util.List;

import it.michelelacorte.androidshortcuts.Shortcuts;
import it.michelelacorte.androidshortcuts.util.Utils;

/**
 * Created by Michele on 04/03/2017.
 */

public class ShortcutsManager {
    private static final String TAG = "ShortcutsManager";

    public static List<Shortcuts> getShortcutsBasedOnTag(Context context, ShortcutInfo shortcutInfo, Drawable icon){
        List<Shortcuts> shortcutses = new ArrayList<Shortcuts>();

        switch (shortcutInfo.getTargetComponent().getPackageName()){
            case "com.android.chrome":
                Bitmap bitmap = getBitmap(R.drawable.ic_visibility_off_black_24dp, context);
                bitmap = Utils.setColorOnBitmap(bitmap, ContextCompat.getColor(context, R.color.chrome_blue));
                shortcutses.add(new Shortcuts(bitmap, context.getResources().getString(R.string.new_incognito_tab), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName()));
                Bitmap bitmap1 = getBitmap(R.drawable.ic_add_black_24dp, context);
                bitmap1 = Utils.setColorOnBitmap(bitmap1, ContextCompat.getColor(context, R.color.chrome_blue));
                shortcutses.add(new Shortcuts(bitmap1, context.getResources().getString(R.string.new_tab), shortcutInfo.getTargetComponent().getClassName(), shortcutInfo.getTargetComponent().getPackageName()));
                break;
            case "com.google.android.apps.maps":
                break;
            default:
                shortcutses.add(new Shortcuts(R.drawable.ic_add_black_24dp, "Shortcuts"));
                shortcutses.add(new Shortcuts(R.drawable.ic_done_black_24dp, "Nougat!", "it.michelelacorte.exampleandroidshortcuts.MainActivity", "it.michelelacorte.exampleandroidshortcuts"));
                shortcutses.add(new Shortcuts(R.drawable.ic_code_black_24dp, "App Shortcuts!", "it.michelelacorte.exampleandroidshortcuts.MainActivity", "it.michelelacorte.exampleandroidshortcuts"));
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
