package com.flair.blurb;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

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
    @Constants.RequestCode
    int selected_category;
    NotificationManager notificationManager;
    Notifications activeNotifications;
    Apps applist;
    NotificationCompat.Builder blurbNotificationBuilder;
    RemoteViews contentView;
    ActivityManager activityManager;

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
        activityManager = ((ActivityManager) getSystemService(ACTIVITY_SERVICE));
        notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        default_category = helper.defaultCategoryToShow(this);
        posted_by_blurb = getString(R.string.posted_by_blurb);

        activeNotifications = new Notifications(this);
        applist = new Apps();

        helper.categorizeInstalledApps(this, this);
        blurbNotificationBuilder = helper.postBlurbNotification(this);
        contentView = helper.getContentView();

        //Categorize active notifcations and post only default_category
        StatusBarNotification[] notifications = getActiveNotifications();
        for (StatusBarNotification notification : notifications) {
            classifyNotification(notification);
        }
        refreshCount();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        Log.d(TAG, "onNotificationPosted: "+Util.getKey(statusBarNotification));
        classifyNotification(statusBarNotification);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification statusBarNotification) {
//        PendingIntent intent = statusBarNotification.getNotification().deleteIntent;
//        Log.d(TAG, "onNotificationRemoved: ");
//        if (intent != null) {
//            String pkgname = statusBarNotification.getTag();
//            Log.d(TAG, "onNotificationRemoved: intent!=null pkgname " + pkgname);
//            List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo runningApp : runningApps) {
//                Log.d(TAG, "onNotificationRemoved: running " + runningApp.processName);
//                if (runningApp.processName.equals(pkgname)) { //The notification that was removed and the package running are same
//                    //so probably user has clicked on notification
//                    Log.d(TAG, "onNotificationRemoved: removing from list");
//                    activeNotifications.removeNotification(null, Util.getKey(statusBarNotification));
//                    refreshCount();
//                }
//            }
//        }
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "onListenerDisconnected: ");
    }

    String classifyNotification(StatusBarNotification notification) {
        String tag = notification.getTag();
        if (tag == null || !notification.getPackageName().equals(getPackageName())) { //Omit classifying notifications posted by blurb
            String key = Util.getKey(notification);
            Log.d(TAG, "classifyNotification: " + notification.getPackageName() + " tag " + tag);
            if (!notification.isOngoing() && notification.getId() != Constants.BLURB_NOTIFICATION_ID && !activeNotifications.containsKey(key)) {
                String pkgname = notification.getPackageName().replace('.', '-');
                @Constants.CategoryDef String category = applist.getCategory(pkgname);
                category = category == null ? Constants.CATEGORY_UNCATEGORIZED : category;
                activeNotifications.addNotification(category, notification);
                dismissNotification(notification);
                if (category.equals(default_category)) {
                    postNotification(notification.getPackageName(), notification);
                }
                refreshCount();
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

        //Refresh active notifications
        if (category.equals(Util.getCategoryForRequestcode(selected_category))) {
            dismissAllNotifications();
            HashMap<String, StatusBarNotification> notifications = activeNotifications.getMapByRequestCode(selected_category);
            if (notifications != null) {
                Iterator<StatusBarNotification> iterator = notifications.values().iterator();
                while (iterator.hasNext()) {
                    StatusBarNotification notification = iterator.next();
                    Util.addNotificationExtras(this, notification, category, Util.getKey(notification));
                    postNotification(notification.getPackageName(), notification);
                }
            }
        }
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
            refreshCount();
            return result;
        }

        notifications = activeNotifications.getMapByRequestCode(request);
        dismissAllNotifications();
        //On clicking category we post the notification with tweaks
        if (notifications != null) {
            selected_category = request;
            Iterator<StatusBarNotification> iter = notifications.values().iterator();
            while (iter.hasNext()) {
                StatusBarNotification notification = iter.next();
                postNotification(notification.getPackageName(), notification);
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

    private void postNotification(String origpkgname, StatusBarNotification notification) {
        Log.d(TAG, "postNotification: notification id "+notification.getId());
        notificationManager.notify(origpkgname, notification.getId(), notification.getNotification());
    }

    public void refreshCount() {
//        int size = activeNotifications.size();
//        if(size == 1) {
//            blurbNotificationBuilder.setTicker("1 Notification");
//        } else if(size > 1) {
//            blurbNotificationBuilder.setTicker(size + " Notifications");
//        }
        contentView.setTextViewText(R.id.social_count, activeNotifications.getMapSizeByCategory(Constants.CATEGORY_SOCIAL)+"");
        contentView.setTextViewText(R.id.news_count, activeNotifications.getMapSizeByCategory(Constants.CATEGORY_NEWS)+"");
        contentView.setTextViewText(R.id.system_count, activeNotifications.getMapSizeByCategory(Constants.CATEGORY_SYSTEM)+"");
        contentView.setTextViewText(R.id.rest_count, activeNotifications.getMapSizeByCategory(Constants.CATEGORY_UNCATEGORIZED)+"");
        blurbNotificationBuilder.setContent(contentView);
        notificationManager.notify(Constants.BLURB_NOTIFICATION_ID, blurbNotificationBuilder.build());
    }
}
