package com.flair.blurb;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.View;

import com.flair.blurb.service.BlurbNotificationService;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import static com.flair.blurb.data.Notifications.intent_category_key;
import static com.flair.blurb.data.Notifications.intent_notification_key;
import static com.flair.blurb.data.Notifications.intent_request_key;

/**
 * Created by sivaram-3911 on 13/01/17.
 */

public class Util {
    private static final String TAG = Util.class.getSimpleName();

    public static String getKey(StatusBarNotification notification) {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
//            return notification.getId() + "_" + System.currentTimeMillis();
//        } else {
//            return notification.getKey();
//        }
        return notification.getKey();
    }

    @Constants.CategoryDef
    public static String getCategoryForRequestcode(@Constants.RequestCode int request) {
        switch (request) {
            case Constants.REQUEST_CODE_MORE:
                return Constants.CATEGORY_UNCATEGORIZED;
            case Constants.REQUEST_CODE_NEWS:
                return Constants.CATEGORY_NEWS;
            case Constants.REQUEST_CODE_SOCIAL:
                return Constants.CATEGORY_SOCIAL;
            case Constants.REQUEST_CODE_SYSTEM:
                return Constants.CATEGORY_SYSTEM;
        }
        return null;
    }

    public static void addNotificationExtras(Context context, StatusBarNotification notification, String category, String key) {
        notification.getNotification().defaults = 0;    //Disables vibration
        notification.getNotification().sound = null;    //Disables sound
        notification.getNotification().deleteIntent = PendingIntent.getService(
                context,
                Constants.REQUEST_DELETE_NOTIFICATION,
                new Intent(context, BlurbNotificationService.class)
                        .putExtra(intent_request_key, Constants.REQUEST_DELETE_NOTIFICATION)
                        .putExtra(intent_notification_key, key)
                        .putExtra(intent_category_key, category)
                        .setAction(System.currentTimeMillis() + ""),

                PendingIntent.FLAG_ONE_SHOT); //Set delete intent
        notification.getNotification().priority = Notification.PRIORITY_MIN;
        notification.getNotification().flags = notification.getNotification().flags | Notification.FLAG_AUTO_CANCEL;
    }

    public static void mergeNotifications(ConcurrentHashMap<String, StatusBarNotification> map, StatusBarNotification notification, NotificationListenerService service) {

        Iterator<StatusBarNotification> iter = map.values().iterator();

        while (iter.hasNext()) {

            StatusBarNotification n = iter.next();

            if (n.getPackageName().equals(notification.getPackageName()) && n.getId() == notification.getId()) {

                Log.d(TAG, "mergeNotifications: merging "+notification.getPackageName()+" ID "+notification.getId());
                /**The new notification has same ID but different key.
                 * So we must remove our old notification object*/
                iter.remove();
                service.cancelNotification(n.getKey());
//                n.getNotification().contentIntent = notification.getNotification().contentIntent;
//                map.put(Util.getKey(notification), n);
//                return true;

            }
        }
//        return false;
    }

    public static int[] getCenterCoordsOfView(View view) {
        return new int[]{view.getLeft() + view.getWidth() / 2, view.getTop() + view.getHeight() / 2};
    }

}
