package com.flair.blurb;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.flair.blurb.data.Apps;
import com.flair.blurb.data.Notifications;
import com.flair.blurb.firebase.DataChangeNotfier;

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

    int nCounter;
    boolean dnd;
    String posted_by_blurb;
    @Constants.CategoryDef
    String default_category;
    NotificationManager notificationManager;
    Notifications activeNotifications;
    Apps applist;

    public BlurbNotificationService() {
        super();
        helper = BlurbHelper.getInstance();
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
        notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        default_category = helper.defaultCategoryToShow(this);
        posted_by_blurb = getString(R.string.posted_by_blurb);

        activeNotifications = new Notifications(this);
        applist = new Apps();

        helper.categorizeInstalledApps(this, this);
        helper.postBlurbNotification(this);

        //Categorize active notifcations and post only default_category
        StatusBarNotification[] notifications = getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            classifyNotification(notification);
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        classifyNotification(statusBarNotification);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
        Log.d(TAG, "onNotificationRemoved: "+statusBarNotification.getNotification().color);
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "onListenerDisconnected: ");
    }

    String classifyNotification(StatusBarNotification notification) {
        String tag = notification.getTag();
        if (tag == null || !tag.equals(posted_by_blurb)) { //Omit classifying notifications posted by blurb
            String key = Util.getKey(notification);
            if (!notification.isOngoing() && notification.getId() != Constants.BLURB_NOTIFICATION_ID && !activeNotifications.containsKey(key)) {
                String pkgname = notification.getPackageName().replace('.', '-');
                @Constants.CategoryDef String category = applist.getCategory(pkgname);
                category = category == null ? Constants.CATEGORY_UNCATEGORIZED : category;
                activeNotifications.addNotification(category, notification);
                dismissNotification(notification);
                if (category.equals(default_category)) {
                    postNotification(notification);
                }
                return category;
            }
        }
        return null;
    }

    /**
     * Transfer notifications from other categories to current category
     **/
    @Override
    public void notifyCategoryChanged(String pkgname, String category) {
        applist.changeCateory(pkgname, category);
        activeNotifications.changeCategory(pkgname, category);
    }

    /**
     * Kind of onclick for the category buttons
     * Also called when user deletes notification
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int result = super.onStartCommand(intent, flags, startId);

        @Constants.RequestCode int request = intent.getIntExtra(Notifications.intent_request_key, 0);

        HashMap<String, StatusBarNotification> notifications = null;

        Log.d(TAG, "onStartCommand: " + request);

        if (request == Constants.REQUEST_DELETE_NOTIFICATION) {
            String key = intent.getStringExtra(Notifications.intent_notification_key);
            String category = intent.getStringExtra(Notifications.intent_category_key);
            Log.d(TAG, "onStartCommand: delete notification " + category);
            activeNotifications.removeNotification(category, key);
            return result;
        }

        notifications = activeNotifications.getMapByRequestCode(request);

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

    private void postNotification(StatusBarNotification notification) {
        notificationManager.notify(posted_by_blurb, notification.getId(), notification.getNotification());
    }


}
