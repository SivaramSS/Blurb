package com.flair.blurb;

import android.service.notification.StatusBarNotification;

/**
 * Created by sivaram-3911 on 08/01/17.
 */

public interface DataChangeNotfier {

    void notifyCategoryChanged(String pkgname, String category);

    void onNotificationClassified(String category, StatusBarNotification notification);
}
