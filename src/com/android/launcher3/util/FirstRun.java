package com.android.launcher3.util;

/**
 * Created by Michele on 04/04/2017.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

import android.content.Context;

/**
 * This class check and save if app is running for the first time
 *
 * Created by Michele on 02/05/2016.
 */
public class FirstRun {
    public static final String CHECK_FIRST_RUN = "CHECKFIRSTRUN";

    /**
     * Get boolean rapresent first launch
     * @param context Context
     * @return boolean
     */
    public synchronized static boolean isFirstLaunch(Context context) throws RuntimeException{
        String sID = null;
        boolean launchFlag = false;
        if (sID == null) {
            File installation = new File(context.getFilesDir(), CHECK_FIRST_RUN);
            try {
                if (!installation.exists()) {
                    launchFlag = true;
                    writeInstallationFile(installation);
                }
                sID = readInstallationFile(installation);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return launchFlag;
    }

    /**
     * Read state from saved file
     * @param installation File
     * @return String
     * @throws IOException
     */
    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");// read only mode
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();

        return new String(bytes);
    }

    /**
     * Write state on file
     * @param installation File
     * @throws IOException
     */
    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
}
