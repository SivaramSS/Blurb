package com.flair.blurb;

import android.os.Build;
import android.service.notification.StatusBarNotification;

/**
 * Created by sivaram-3911 on 13/01/17.
 */

public class Util {

    public static String getKey(StatusBarNotification notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
            return notification.getId() +"_"+System.currentTimeMillis();
        } else {
            return notification.getKey();
        }
    }
}
