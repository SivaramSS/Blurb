package com.flair.blurb.data;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.flair.blurb.BlurbNotificationService;
import com.flair.blurb.Constants;
import com.flair.blurb.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.flair.blurb.Util.getKey;

/**
 * Created by sivaram-3911 on 13/01/17.
 */

public class Notifications {

    private static final String TAG = Notifications.class.getSimpleName();
    Context context;
    public static String intent_request_key, intent_category_key, intent_notification_key;
    HashMap<String, StatusBarNotification> important, social, system, promotions, news, rest;
    HashMap<String, String> apps;

    private Notifications() {}

    public Notifications(Context context) {
        this.context = context;
        apps = new HashMap<>();
        social = new HashMap<>();
        system = new HashMap<>();
        news = new HashMap<>();
        rest = new HashMap<>();
        intent_request_key = context.getString(R.string.intent_request_key);
        intent_notification_key = context.getString(R.string.notification_key);
        intent_category_key = context.getString(R.string.category_key);
    }

    public boolean containsKey(String key) {
        return social.containsKey(key) || news.containsKey(key) || system.containsKey(key) || rest.containsKey(key);
    }

    public void addNotification(@Constants.CategoryDef String category, StatusBarNotification notification) {

        String key = getKey(notification);

        Log.d(TAG, "addNotification: "+key);

        notification.getNotification().defaults = 0;    //Disables vibration
        notification.getNotification().sound = null;    //Disables sound
        notification.getNotification().deleteIntent = PendingIntent.getService(
                context,
                Constants.REQUEST_DELETE_NOTIFICATION,
                new Intent(context, BlurbNotificationService.class)
                        .putExtra(intent_request_key, Constants.REQUEST_DELETE_NOTIFICATION)
                        .putExtra(intent_notification_key, key)
                        .putExtra(intent_category_key, category)
                .setAction(System.currentTimeMillis()+""),
                PendingIntent.FLAG_ONE_SHOT); //Set delete intent
        notification.getNotification().flags = notification.getNotification().flags | Notification.FLAG_AUTO_CANCEL;
        HashMap<String, StatusBarNotification> map = getMapByCategory(category);
        map.put(key, notification);
    }

    public void removeNotification(String category, String key) {
        HashMap<String, StatusBarNotification> map = getMapByCategory(category);
        Log.d(TAG, "deleteNotification: category " + category + " key " + key);

        if (map != null && map.containsKey(key)) {
            map.remove(key);
        }
    }

    public HashMap<String, StatusBarNotification> getMapByCategory(@Constants.CategoryDef String category) {
        switch (category) {
            case Constants.CATEGORY_SOCIAL:
                return social;
            case Constants.CATEGORY_NEWS:
                return news;
            case Constants.CATEGORY_SYSTEM:
                return system;
            case Constants.CATEGORY_UNCATEGORIZED:
            case "":
                return rest;
        }
        return null;
    }

    public void changeCategory(String pkgname, @Constants.CategoryDef String category) {

        HashMap<String, StatusBarNotification> receiver;
        ArrayList<HashMap<String, StatusBarNotification>> maps = new ArrayList<>();
        Log.d(TAG, "notifyCategoryChanged: " + pkgname + " category " + category);
        switch (category) {
            case Constants.CATEGORY_SOCIAL:
                receiver = social;
                maps.add(rest);
                maps.add(news);
                maps.add(system);
                break;
            case Constants.CATEGORY_NEWS:
                receiver = news;
                maps.add(social);
                maps.add(system);
                maps.add(rest);
                break;
            case Constants.CATEGORY_SYSTEM:
                receiver = system;
                maps.add(rest);
                maps.add(social);
                maps.add(news);
                break;
            case Constants.CATEGORY_UNCATEGORIZED:
            default:
                receiver = rest;
                maps.add(social);
                maps.add(news);
                maps.add(system);
                break;
        }

        for (HashMap<String, StatusBarNotification> map : maps) {

            Iterator<StatusBarNotification> iterator = map.values().iterator();
            while (iterator.hasNext()) {
                StatusBarNotification notification = iterator.next();
                if (notification.getPackageName().replace('.', '-').equals(pkgname)) {
                    String key = getKey(notification);
                    receiver.put(key, notification);
                    map.remove(key);
                }
            }

        }
    }

    public HashMap<String, StatusBarNotification> getMapByRequestCode(@Constants.RequestCode int requestCode) {
        switch (requestCode) {
            case Constants.REQUEST_CODE_SOCIAL:
                return social;
            case Constants.REQUEST_CODE_MORE:
                return rest;
            case Constants.REQUEST_CODE_SYSTEM:
                return system;
            case Constants.REQUEST_CODE_NEWS:
                return news;
        }
        return null;
    }
}