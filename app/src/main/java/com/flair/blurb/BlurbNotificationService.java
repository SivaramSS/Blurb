package com.flair.blurb;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.SparseArray;

import java.util.List;

/**
 * Created by sivaram-3911 on 24/12/16.
 */

public class BlurbNotificationService extends NotificationListenerService {

    SparseArray<StatusBarNotification> social, system, promotions, news, rest;
    int nCounter, lastAdded;
    boolean dnd;

    public BlurbNotificationService() {
        super();
        social = new SparseArray<>();
        system = new SparseArray<>();
        promotions = new SparseArray<>();
        news = new SparseArray<>();
        rest = new SparseArray<>();
        this.nCounter = 0;
        this.dnd = false;

        //categorize active notifications
        getActiveNotifications();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        Notification notification = statusBarNotification.getNotification();

        int notificationKey = statusBarNotification.getId();
        if (!statusBarNotification.isOngoing()) {
            //Check whether list already has notification
            if (rest.get(notificationKey) == null) {
                NotificationManager nManager = ((NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE));
                nManager.cancel(statusBarNotification.getId());
                lastAdded = statusBarNotification.getId();
                rest.put(lastAdded, statusBarNotification);
                nCounter++;
                if (!dnd) {
                    nManager.notify(statusBarNotification.getId(), statusBarNotification.getNotification());
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

    }

    private void classifyInstalledApps() {
        List<ApplicationInfo> applist = getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo applicationInfo : applist) {
            if ((applicationInfo.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM) ) != 0) {
                //system app
            } else {

            }
        }
    }
}
