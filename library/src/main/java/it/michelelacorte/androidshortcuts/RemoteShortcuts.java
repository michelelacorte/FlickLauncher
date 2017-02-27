package it.michelelacorte.androidshortcuts;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.UserHandle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.michelelacorte.androidshortcuts.util.Utils;

/**
 * Created by Michele on 10/01/2017.
 */

/**
 * Remote Shortcuts class provide method to serialize and deserialize shortcuts for save/get shortcuts from different apps
 */
public class RemoteShortcuts {
    private static final String TAG = "RemoteShorctus";
    public static boolean USE_SHORTCUTS_FROM_API_25 = false;

    /**
     * Save shortcuts on file
     * @param activity Activity
     * @param listOfShortcuts ArrayList<Shortcuts>
     */
    public static void saveRemoteShortcuts(Activity activity, ArrayList<Shortcuts> listOfShortcuts){
        String fileName = activity.getPackageName() + "/shortcut.shc";
        ObjectOutput out = null;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            checkPermission(activity);
        }

        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/Shortcuts/"+fileName);
            file.getParentFile().mkdirs();
            file.createNewFile();
            out = new ObjectOutputStream(new FileOutputStream(file, false));
            for(Shortcuts shortcuts : listOfShortcuts){
                if(shortcuts.getShortcutsText() != null) {
                    out.writeUTF(shortcuts.getShortcutsText());
                }if(shortcuts.getShortcutsImage() != 0) {
                    Bitmap image = BitmapFactory.decodeResource(activity.getResources(), shortcuts.getShortcutsImage());
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    final byte[] imageByteArray = stream.toByteArray();
                    out.writeInt(imageByteArray.length);
                    out.write(imageByteArray);
                }else if (shortcuts.getShortcutsImageBitmap() != null){
                    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    shortcuts.getShortcutsImageBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);
                    final byte[] imageByteArray = stream.toByteArray();
                    out.writeInt(imageByteArray.length);
                    out.write(imageByteArray);
                }
                if(shortcuts.getTargetPackage() != null && shortcuts.getTargetClass() != null){
                    out.writeUTF(shortcuts.getTargetPackage());
                    out.writeUTF(shortcuts.getTargetClass());
                }else{
                    out.writeUTF(activity.getPackageName());
                    out.writeUTF(activity.getPackageName()+"."+activity.getLocalClassName());
                }
            }
            out.close();
            Log.d(TAG, "Shortcuts saved into: " + Environment.getExternalStorageDirectory() + "/Shortcuts/"+fileName);
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.toString());

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

    }

    /**
     * Get shortcuts from file
     * @param activity Activity
     * @return ArrayList<Shotrcuts>
     */
    public static ArrayList<Shortcuts> getRemoteShortcuts(Activity activity, String packageName){
        String fileName = packageName + "/shortcut.shc";
        ObjectInputStream input;
        ArrayList<Shortcuts> listOfShortcuts = new ArrayList<>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            checkPermission(activity);
        }

        try {
            input = new ObjectInputStream(new FileInputStream(Environment.getExternalStorageDirectory() + "/Shortcuts/"+fileName));
            try{
                while(true) {
                    String shortcutsText = input.readUTF();
                    final int length = input.readInt();
                    final byte[] imageByteArray = new byte[length];
                    input.readFully(imageByteArray);
                    Bitmap shortcutsImage = BitmapFactory.decodeByteArray(imageByteArray, 0, length);
                    String targetPackage = input.readUTF();
                    String targetClass = input.readUTF();
                    listOfShortcuts.add(new Shortcuts(shortcutsImage, shortcutsText, targetClass, targetPackage));
                }
            }catch (EOFException e){}
            input.close();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Shortcuts getted from: " + Environment.getExternalStorageDirectory() + "/Shortcuts/"+fileName);
        return listOfShortcuts;
    }


    /**
     * This method get shortcuts defined by App in Android 7.1 Nougat (API 25), returned shortcuts are sorted by rank
     * in according to Google Doc
     * @param activity Activity
     * @param targetPackageName String
     * @param uid int
     * @return ArrayList<Shortcuts>
     * @throws Exception
     */
    @TargetApi(25)
    @RequiresApi(25)
    public static ArrayList<Shortcuts> getRemoteShortcutsOnAPI25(Activity activity, String targetPackageName, int uid) throws Exception {
        LauncherApps launcherApps = (LauncherApps) activity.getApplicationContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);
        if (!launcherApps.hasShortcutHostPermission()) {
            Log.e(TAG, "Don't have permission, you may need set this app as default launcher!");
            throw new Exception("Don't have permission, you may need set this app as default launcher!");
        }

        PackageManager packageManager = activity.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList;
        if (packageManager == null || (resolveInfoList = packageManager.queryIntentActivities(mainIntent, 0)) == null) {
            Log.e(TAG, "No Main and Launcher Activity!");
            throw new Exception("No Main and Launcher Activity!");
        }

        ArrayList<Shortcuts> shortcutsArrayList = new ArrayList<>();
        int queryFlags = LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC | LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED;
        List<ShortcutInfo> shortcutInfoList = launcherApps.getShortcuts(
                new LauncherApps.ShortcutQuery().setPackage(targetPackageName).setQueryFlags(queryFlags),
                UserHandle.getUserHandleForUid(uid));


        for (int j = 0; j < shortcutInfoList.size(); j++) {
            if (shortcutInfoList.get(j) != null) {
                try {
                    //Get shortcuts text (short label)
                    String shortcutsText = shortcutInfoList.get(j).getShortLabel().toString();
                    //Get packageName
                    String packageName = shortcutInfoList.get(j).getActivity().getPackageName();
                    //Get className
                    String className = shortcutInfoList.get(j).getActivity().getClassName();
                    //Get display metrics and get shortcuts drawable
                    DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
                    Drawable shortcutsImage = launcherApps.getShortcutIconDrawable(shortcutInfoList.get(j), metrics.densityDpi);
                    Bitmap shortcutsImageBitmap = Utils.convertDrawableToBitmap(shortcutsImage);
                    //Get image badge with density adjust
                    Drawable shortcutsImageBadged = launcherApps.getShortcutBadgedIconDrawable(shortcutInfoList.get(j), metrics.densityDpi);
                    Bitmap shortcutsImageBadgedBitmap = Utils.convertDrawableToBitmap(shortcutsImageBadged);
                    //Get rank to order list
                    int rank = shortcutInfoList.get(j).getRank();

                    //Initialize shortcuts
                    shortcutsArrayList.add(new Shortcuts(shortcutsImageBitmap, shortcutsImageBadgedBitmap, shortcutsText, className, packageName, rank));
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
        USE_SHORTCUTS_FROM_API_25 = true;

        //Order by rank (lowest to highest)
        Collections.sort(shortcutsArrayList, new Comparator<Shortcuts>() {
            @Override
            public int compare(Shortcuts shortcuts, Shortcuts shortcuts1) {
                return shortcuts1.getRank() - shortcuts.getRank();
            }
        });
        //return sorted arraylist
        return shortcutsArrayList;
    }

    /**
     * Check if user had permission
     * @param activity Activity
     */
    private static void checkPermission(Activity activity) {
        int result = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED) {
            requestPermission(activity);
        }
    }

    /**
     * Make request permission
     * @param activity Activity
     */
    private static void requestPermission(Activity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.d(TAG, "Write External Storage permission allows us to do store shortcuts data. Please allow this permission in App Settings.");
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
            Log.d(TAG, "Write External Storage permission allows us to do store shortcuts data.");
        }
    }
}
