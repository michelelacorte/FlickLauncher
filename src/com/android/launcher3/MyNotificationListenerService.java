package com.android.launcher3;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import static com.android.launcher3.Utilities.getPrefs;

/**
 * Created by Michele on 02/04/2017.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)

public class MyNotificationListenerService extends NotificationListenerService {
    public static int notificationCount = 0;

    @TargetApi(20)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn, NotificationListenerService.RankingMap rankingMap) {
        if (sbn.getTag() != null && Utilities.isAllowNotificationCountPrefEnabled(getApplicationContext())) {
            notificationCount++;
            for (final AppInfo app : AllAppsList.data) {
                if (sbn.getPackageName().equals(app.getTargetComponent().getPackageName())) {
                    if(getNotificationCount(getApplicationContext(), app.getTargetComponent().getPackageName()) != -1){
                        int notificationCountPrev = getNotificationCount(getApplicationContext(), app.getTargetComponent().getPackageName());
                        notificationCountPrev++;
                        setNotificationCount(getApplicationContext(), app.getTargetComponent().getPackageName(), notificationCountPrev);
                    }else {
                        setNotificationCount(getApplicationContext(), app.getTargetComponent().getPackageName(), notificationCount);
                    }
                    Launcher.getLauncherAppState().reloadWorkspace();
                }
            }
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){

    }

    public static void setNotificationCount(Context context, String packageName, int number){
        getPrefs(context).edit().putInt("NOTIFICATION" + packageName, number).apply();
    }

    public static int getNotificationCount(Context context, String packageName){
        return getPrefs(context).getInt("NOTIFICATION" + packageName, -1);
    }
}
