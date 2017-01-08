package com.flair.blurb;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by sivaram-3911 on 24/12/16.
 */

public class BlurbNotificationService extends NotificationListenerService implements DataChangeNotfier {

    private static final String TAG = BlurbNotificationService.class.getSimpleName();
    /**
     * Maintains categorized active notifications
     */
    BlurbHelper helper;
    boolean isApi18 = false;

    HashMap<String, StatusBarNotification> important, social, system, promotions, news, rest;
    HashMap<String, String> apps;
    int nCounter, lastAdded;
    boolean dnd;

    public BlurbNotificationService() {
        super();
        helper = BlurbHelper.getInstance();
        important = new HashMap<>();
        apps = new HashMap<>();
        social = new HashMap<>();
        system = new HashMap<>();
        promotions = new HashMap<>();
        news = new HashMap<>();
        rest = new HashMap<>();
        this.nCounter = 0;
        this.dnd = false;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "onListenerConnected: ");
        isApi18 = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH;
//        helper.instantiateBlurb(this, this);
//        helper.classifyActiveNotifications(this, getActiveNotifications());
//        cancelAllNotifications();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        if (!statusBarNotification.isOngoing() && statusBarNotification.getId() != App.BLURB_NOTIFICATION_ID) {

            String key;
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
                key = App.getApi18Key(statusBarNotification.getId());
            } else {
                key = statusBarNotification.getKey();
            }

            if (!social.containsKey(key) && !news.containsKey(key) && !system.containsKey(key) && !rest.containsKey(key)) {
                String pkgname = statusBarNotification.getPackageName().replace('.', '-');
                String category = apps.get(pkgname);
                category = category == null ? App.CATEGORY_UNCATEGORIZED : category;
                Log.d(TAG, "onNotificationPosted: " + pkgname + " " + category+" "+statusBarNotification.getNotification().category);
                onNotificationClassified(category, statusBarNotification);
            }

        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {

    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "onListenerDisconnected: ");
    }

    @Override
    public void notifyCategoryChanged(String pkgname, String category) {
        apps.put(pkgname, category);
    }

    @Override
    public void onNotificationClassified(String category, StatusBarNotification notification) {
        String key = "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
            key = App.getApi18Key(notification.getId());
        } else {
            key = notification.getKey();
        }
        switch (category) {
            case App.CATEGORY_NEWS:
                news.put(key, notification);
                break;
            case App.CATEGORY_SOCIAL:
                social.put(key, notification);
                break;
            case App.CATEGORY_SYSTEM:
                system.put(key, notification);
                break;
            case App.CATEGORY_UNCATEGORIZED:
                rest.put(key, notification);
                break;
            case App.CATEGORY_IMPORTANT:
                break;
            case App.CATEGORY_PROMOTIONS:
                break;
            default:
                rest.put(key, notification);
        }
    }

    /**
     * Kind of onclick for the category buttons
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        int category_pressed = intent.getIntExtra(getString(R.string.intent_key), 0);
        HashMap<String, StatusBarNotification> notifications = null;

        switch (category_pressed) {
            case App.REQUEST_CODE_SOCIAL:
                notifications = social;
                break;
            case App.REQUEST_CODE_NEWS:
                notifications = news;
                break;
            case App.REQUEST_CODE_SYSTEM:
                notifications = system;
                break;
            case App.REQUEST_CODE_MORE:
                notifications = rest;
                break;
        }

        Log.d(TAG, "onStartCommand: " + category_pressed);


        if (notifications != null) {
            Iterator iter = notifications.values().iterator();
            NotificationManager notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
            while (iter.hasNext()) {
                StatusBarNotification notification = ((StatusBarNotification) iter.next());
                notification.getNotification().defaults = 0;
                notification.getNotification().sound = null;
                notificationManager.notify(notification.getId(), notification.getNotification());
            }
        }

        return result;
    }
}
