package it.michelelacorte.androidshortcuts.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.widget.AdapterView;
import android.widget.GridView;

import it.michelelacorte.androidshortcuts.R;
import it.michelelacorte.androidshortcuts.RemoteShortcuts;
import it.michelelacorte.androidshortcuts.ShortcutsCreation;

/**
 * Created by Michele on 09/12/2016.
 */

public class Utils {
    private static final String TAG = "Utils";

    /**
     * Get grid size
     * @param gridView AdapterView
     * @return GridSize
     */
    public static GridSize getGridSize(AdapterView gridView){
        int nColumn = ((GridView) gridView).getNumColumns();
        double nRow = Math.ceil((double)gridView.getCount()/(double)((GridView) gridView).getNumColumns());
        Log.d(TAG, "Number of Row: " + (int)nRow + "\nNumber of Column: " + nColumn);
        return new GridSize(nColumn, (int)nRow);
    }

    /**
     * Get toolbar height
     * @param activity Activity
     * @return int
     */
    public static int getToolbarHeight(Activity activity){
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            Log.d(TAG, "Toolbar found, height: " + String.valueOf(TypedValue.complexToDimensionPixelSize(tv.data,activity.getApplicationContext().getResources().getDisplayMetrics())));
            return TypedValue.complexToDimensionPixelSize(tv.data,activity.getApplicationContext().getResources().getDisplayMetrics());
        }else{
            Log.d(TAG, "Toolbar not found, height: 0");
            return 0;
        }
    }

    /**
     * Create shortcuts on launcher based on params
     * @param activity Activity
     * @param shortcutsImage Bitmap
     * @param shortcutsText String
     * @param className String
     * @param packageName String
     * @throws ClassNotFoundException
     */
    public static void createShortcutsOnLauncher(Activity activity, Bitmap shortcutsImage, String shortcutsText, String className, String packageName, Drawable packageImage, Bitmap shortcutsImageBadge) throws ClassNotFoundException {

        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.INSTALL_SHORTCUT);
        if (result != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.INSTALL_SHORTCUT)) {
                Log.e(TAG, "Install Shortcuts permission allows us to create shortcuts on launcher. Please allow this permission in App Settings.");
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.INSTALL_SHORTCUT}, 111);
                Log.d(TAG, "Install Shortcuts permission allows us to create shortcuts on launcher.");
            }
        }

        Intent shortcutIntent = new Intent(activity.getApplicationContext(), activity.getClass());
        shortcutIntent.setComponent(new ComponentName(
                packageName, className.replaceAll(packageName, "")));
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutsText);
        if(shortcutsImageBadge  == null && !RemoteShortcuts.USE_SHORTCUTS_FROM_API_25) {
            Bitmap roundedBitmap = getRoundedBitmap(shortcutsImage, packageImage);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, roundedBitmap);
        }else if(ShortcutsCreation.USE_SHORTCUTS_FOR_LAUNCHER_3){
            Bitmap roundedBitmap = getRoundedBitmapForLauncher3(activity, shortcutsImage, packageImage);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, roundedBitmap);
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && shortcutsImageBadge != null) {
                Bitmap roundedBitmap = getRoundedBitmapForAPI25(shortcutsImageBadge, packageImage);
                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, roundedBitmap);
            }else{
                Log.e(TAG, "This call requires API " + Build.VERSION_CODES.N_MR1);
                return;
            }
        }
        addIntent.putExtra("duplicate", false);
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        activity.getApplicationContext().sendBroadcast(addIntent);
    }

    /**
     * Resize bitmap method
     * @param bm Bitmap
     * @param newWidth int
     * @param newHeight int
     * @return Bitmap
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        //bm.recycle();
        return resizedBitmap;
    }

    /**
     * This method return image with badge from icon and package icon.
     * @param bitmap Bitmap
     * @param packageImage Drawable
     * @return Bitmap
     */
    @TargetApi(25)
    @RequiresApi(25)
    public static Bitmap getRoundedBitmapForAPI25(Bitmap bitmap, Drawable packageImage)
    {
        Bitmap packageIcon = null;
        if(packageImage != null) {
            packageIcon = convertDrawableToBitmap(packageImage);
        }
        Bitmap packageIconScaled = getResizedBitmap(packageIcon, bitmap.getWidth()/2, bitmap.getHeight()/2);

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(result);

        final int color = 0xffF5F5F5;
        final int colorShape = 0xffBDBDBD;
        final Paint paint = new Paint();
        final Paint paintShape = new Paint();

        paint.setAntiAlias(true);
        paintShape.setAntiAlias(true);

        paint.setColor(color);
        paintShape.setColor(colorShape);

        canvas.drawARGB(0, 0, 0, 0);
        paintShape.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        canvas.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2+2, 45, paintShape);
        canvas.drawBitmap(bitmap, 0f, 0f, paint);
        canvas.drawBitmap(packageIconScaled, bitmap.getWidth()/2, bitmap.getHeight()/2, null);
        return result;
    }


    public static Bitmap getRoundedBitmapForLauncher3(Activity activity, Bitmap bitmap, Drawable packageImage)
    {
        Bitmap packageIcon = null;
        if(packageImage != null) {
            packageIcon = convertDrawableToBitmap(packageImage);
        }
        Bitmap packageIconScaled = getResizedBitmap(packageIcon, bitmap.getWidth(), bitmap.getHeight());
        Bitmap shortcutsIconScaled = getResizedBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight());


        final int dominantColor = getDominantColor(packageIconScaled);
        //Find dominant color and set it to bitmap icon shortcuts
        if (dominantColor != 0) {
            shortcutsIconScaled = setColorOnBitmap(shortcutsIconScaled, dominantColor);
        }

        final Bitmap output = Bitmap.createBitmap(packageIcon.getWidth(), packageIcon.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = 0xffF5F5F5;
        final int colorShape = 0xffBDBDBD;
        final Paint paint = new Paint();
        final Paint paintShape = new Paint();

        paint.setAntiAlias(true);
        paintShape.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        paintShape.setColor(colorShape);
        paintShape.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        switch (getScreenXDimension(activity)) {
            case 720:
                canvas.drawCircle(packageIcon.getWidth()/2, packageIcon.getHeight()/2, 45, paint);
                canvas.drawCircle(packageIcon.getWidth()/2, packageIcon.getHeight()/2+2, 45, paintShape);
                break;
            case 1080:
                canvas.drawCircle(packageIcon.getWidth()/2, packageIcon.getHeight()/2, 70, paint);
                canvas.drawCircle(packageIcon.getWidth()/2, packageIcon.getHeight()/2+2, 70, paintShape);
                break;
            case 1440:
                canvas.drawCircle(packageIcon.getWidth()/2, packageIcon.getHeight()/2, 90, paint);
                canvas.drawCircle(packageIcon.getWidth()/2, packageIcon.getHeight()/2+2, 90, paintShape);
                break;
            default:
                Log.e(TAG, "Resolution of screen not supported!");
                break;
        }
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas.drawBitmap(shortcutsIconScaled, shortcutsIconScaled.getWidth()/2, shortcutsIconScaled.getHeight()/2, paint);
        if(packageImage != null && packageIcon != null)
            canvas.drawBitmap(packageIconScaled, bitmap.getWidth(), bitmap.getHeight(), paint);
        //bitmap.recycle();

        return output;
    }

    /**
     * Get rounded bitmap like Nougat shortcuts
     * @param bitmap Bitmap
     * @param packageImage Drawable
     * @return Bitmap
     */
    public static Bitmap getRoundedBitmap(Bitmap bitmap, Drawable packageImage)
    {
        Bitmap packageIcon = null;
        if(packageImage != null) {
            packageIcon = convertDrawableToBitmap(packageImage);
        }
        Bitmap packageIconScaled = getResizedBitmap(packageIcon, bitmap.getWidth()/2, bitmap.getHeight()/2);
        Bitmap shortcutsIconScaled = getResizedBitmap(bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2);


        final int dominantColor = getDominantColor(packageIconScaled);
        //Find dominant color and set it to bitmap icon shortcuts
        if (dominantColor != 0) {
            shortcutsIconScaled = setColorOnBitmap(shortcutsIconScaled, dominantColor);
        }

        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = 0xffF5F5F5;
        final int colorShape = 0xffBDBDBD;
        final Paint paint = new Paint();
        final Paint paintShape = new Paint();

        paint.setAntiAlias(true);
        paintShape.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        paintShape.setColor(colorShape);
        paintShape.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        canvas.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, 45, paint);
        canvas.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2+2, 45, paintShape);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        canvas.drawBitmap(shortcutsIconScaled, shortcutsIconScaled.getWidth()/2, shortcutsIconScaled.getHeight()/2, paint);
        if(packageImage != null && packageIcon != null)
            canvas.drawBitmap(packageIconScaled, bitmap.getWidth()/2, bitmap.getHeight()/2, paint);
        //bitmap.recycle();

        return output;
    }

    /**
     * Get dominant color of bitmap
     * @param bitmap Bitmap
     * @return int
     */
    public static int getDominantColor(Bitmap bitmap){
        Palette palette = Palette.from(bitmap).generate();
        Palette.Swatch dominantSwatch = palette.getDominantSwatch();
        if(dominantSwatch != null){
            return dominantSwatch.getRgb();
        }
        return 0;
    }

    /**
     * Change color of Bitmap
     * @param bitmap Bitmap
     * @param color int
     * @return Bitmap
     */
    public static Bitmap setColorOnBitmap(Bitmap bitmap, int color){
        Bitmap bm = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Paint paintColorDominant = new Paint();
        if(!RemoteShortcuts.USE_SHORTCUTS_FROM_API_25) {
            ColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
            paintColorDominant.setColorFilter(filter);
        }
        Canvas canvasColorDominant = new Canvas(bm);
        canvasColorDominant.drawBitmap(bm, 0, 0, paintColorDominant);
        return bm;
    }

    /**
     * Convert drawable to bitmap
     * @param drawable Drawable
     * @return Bitmap
     */
    public static Bitmap convertDrawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Get screen dimension
     */
    public static int getScreenXDimension(Activity activity){
        Display mdisp = activity.getWindowManager().getDefaultDisplay();
        Point mdispSize = new Point();
        mdisp.getSize(mdispSize);
        return mdispSize.x;
    }
}
