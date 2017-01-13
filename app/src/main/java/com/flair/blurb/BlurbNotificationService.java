package com.flair.blurb;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    int nCounter;
    boolean dnd;
    String notification_key, intent_key, category_key;
    String posted_by_blurb;
    String default_category;
    NotificationManager notificationManager;

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

    /**
     * On app start
     * initialize constants
     * read installed apps and fetch their categories from firebase
     * classify all active notifications
     * post blurb notification
     * dismiss them except default category
     */

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "onListenerConnected: ");
        isApi18 = Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH;
        intent_key = getString(R.string.intent_key);
        notification_key = getString(R.string.notification_key);
        category_key = getString(R.string.category_key);
        posted_by_blurb = getString(R.string.posted_by_blurb);
        default_category = helper.defaultCategoryToShow(this);
        notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));

        helper.categorizeInstalledApps(this, this);
        StatusBarNotification[] notifications = getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            classifyNotification(notification);
        }
        helper.postBlurbNotification(this);
        dismissAllNotifications();
        //Post default_category notifications
        Map<String, StatusBarNotification> map = getMapByCategory(default_category);
        Iterator<StatusBarNotification> iter = map.values().iterator();
        while (iter.hasNext()) {
            postNotification(iter.next());
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        String tag = statusBarNotification.getTag();
        Log.d(TAG, "onNotificationPosted: posted notification " + statusBarNotification.getPackageName() + " " + BlurbHelper.getKey(statusBarNotification));
        if (tag == null || !posted_by_blurb.equals(statusBarNotification.getTag())) { //Omit classifying notifications posted by blurb
            String cat = classifyNotification(statusBarNotification);
//            if (cat!=null && !cat.equals(default_category)) {
//                //Change needed..
//                // TODO all non blurb notifications must be dismissed - default category notifications must be posted again inorder to maintain delete intent
//                dismissNotification(statusBarNotification);
//            }
            dismissNotification(statusBarNotification);
            if (cat != null && cat.equals(default_category)) {
                postNotification(statusBarNotification);
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

    /**
     * Push notifications from other categories to current category
     **/
    @Override
    public void notifyCategoryChanged(String pkgname, String category) {
        Log.d(TAG, "notifyCategoryChanged: " + pkgname + " category " + category);
        apps.put(pkgname, category);
        switch (category) {
            case App.CATEGORY_SOCIAL:
                helper.transferNotifications(pkgname, social, rest, news, system);
                break;
            case App.CATEGORY_NEWS:
                helper.transferNotifications(pkgname, news, social, rest, system);
                break;
            case App.CATEGORY_SYSTEM:
                helper.transferNotifications(pkgname, system, social, news, rest);
                break;
            case App.CATEGORY_UNCATEGORIZED:
            default:
                helper.transferNotifications(pkgname, rest, social, news, system);
                break;
        }
    }

    @Override
    public void onNotificationClassified(String category, StatusBarNotification notification) {

        String key = BlurbHelper.getKey(notification);

        Log.d(TAG, "onNotificationClassified: " + category + " key " + key);

        silenceAndSetDeleteIntent(notification, key, category);

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
     * Also called when user deletes notification
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);
        int request = intent.getIntExtra(getString(R.string.intent_key), 0);
        HashMap<String, StatusBarNotification> notifications = null;

        if (request == App.REQUEST_DELETE_NOTIFICATION) {
            String key = intent.getStringExtra(notification_key);
            String category = intent.getStringExtra(category_key);
            Log.d(TAG, "onStartCommand: delete notification " + category);
            deleteNotification(category, key);
            return result;
        }

        switch (request) {
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

        Log.d(TAG, "onStartCommand: " + request);

        //On clicking category we post the notification with tweaks
        if (notifications != null) {
            Iterator iter = notifications.values().iterator();
            NotificationManager notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
            while (iter.hasNext()) {
                StatusBarNotification notification = ((StatusBarNotification) iter.next());
                notificationManager.notify(posted_by_blurb, notification.getId(), notification.getNotification());
            }
        }

        return result;
    }

    public String classifyNotification(StatusBarNotification statusBarNotification) {
        if (!statusBarNotification.isOngoing() && statusBarNotification.getId() != App.BLURB_NOTIFICATION_ID) {

            String key;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
                key = App.getApi18Key(statusBarNotification.getId());
            } else {
                key = statusBarNotification.getKey();
            }

            if (!social.containsKey(key) && !news.containsKey(key) && !system.containsKey(key) && !rest.containsKey(key)) {
                String pkgname = statusBarNotification.getPackageName().replace('.', '-');
                String category = apps.get(pkgname);
                category = category == null ? App.CATEGORY_UNCATEGORIZED : category;
                onNotificationClassified(category, statusBarNotification);
                return category;
            }

        }
        return null;
    }

    public void deleteNotification(String category, String key) {
        HashMap<String, StatusBarNotification> map = null;
        Log.d(TAG, "deleteNotification: category " + category + " key " + key);
        switch (category) {
            case App.CATEGORY_SOCIAL:
                map = social;
                break;
            case App.CATEGORY_NEWS:
                map = news;
                break;
            case App.CATEGORY_SYSTEM:
                map = system;
                break;
            case App.CATEGORY_UNCATEGORIZED:
            case "":
            default:
                map = rest;
                break;
        }

        if (map != null && map.containsKey(key)) {
            map.remove(key);
        }
    }

    public void dismissAllNotifications() {
        cancelAllNotifications();
    }

    public void dismissNotification(StatusBarNotification statusBarNotification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            cancelNotification(statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId());
        } else {
            cancelNotification(statusBarNotification.getKey());
        }
    }

    private void silenceAndSetDeleteIntent(StatusBarNotification notification, String key, String category) {
        notification.getNotification().defaults = 0;    //Disables vibration
        notification.getNotification().sound = null;    //Disables sound
        notification.getNotification().deleteIntent = PendingIntent.getService(
                this,
                App.REQUEST_DELETE_NOTIFICATION,
                new Intent(this, BlurbNotificationService.class)
                        .putExtra(intent_key, App.REQUEST_DELETE_NOTIFICATION)
                        .putExtra(notification_key, key)
                        .putExtra(category_key, category),
                PendingIntent.FLAG_UPDATE_CURRENT); //Set delete intent
    }

    private void postNotification(StatusBarNotification notification) {
        notificationManager.notify(posted_by_blurb, notification.getId(), notification.getNotification());
    }

    HashMap<String, StatusBarNotification> getMapByCategory(String category) {
        switch (category) {
            case App.CATEGORY_SOCIAL:
                return social;
            case App.CATEGORY_NEWS:
                return news;
            case App.CATEGORY_SYSTEM:
                return system;
            case App.CATEGORY_UNCATEGORIZED:
            case "":
                return rest;
        }
        return null;
    }
}
