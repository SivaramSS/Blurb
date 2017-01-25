package com.flair.blurb;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by sivaram-3911 on 13/01/17.
 */

public class Constants {
    static final int BLURB_NOTIFICATION_ID = 5555;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({CATEGORY_UNCATEGORIZED, CATEGORY_SOCIAL, CATEGORY_SYSTEM, CATEGORY_NEWS, CATEGORY_PROMOTIONS, CATEGORY_IMPORTANT})
    public @interface CategoryDef{}

    public static final String CATEGORY_UNCATEGORIZED = "UNCATEGORIZED";
    public static final String CATEGORY_SYSTEM = "SYSTEM";
    public static final String CATEGORY_NEWS = "NEWS";
    public static final String CATEGORY_SOCIAL = "SOCIAL";
    public static final String CATEGORY_IMPORTANT = "IMPORTANT";
    public static final String CATEGORY_PROMOTIONS = "PROMOTIONS";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({REQUEST_CODE_MORE, REQUEST_CODE_NEWS, REQUEST_CODE_SOCIAL, REQUEST_CODE_SYSTEM, REQUEST_DELETE_NOTIFICATION, REQUEST_CODE_WIDGET})
    public @interface RequestCode{}
    public static final int REQUEST_CODE_SOCIAL = 1111;
    public static final int REQUEST_CODE_NEWS = 2222;
    public static final int REQUEST_CODE_SYSTEM = 3333;
    public static final int REQUEST_CODE_MORE = 4444;
    public static final int REQUEST_DELETE_NOTIFICATION = 7777;
    public static final int REQUEST_STOP_BLURB = 8888;

    public static final int REQUEST_CODE_WIDGET = 262626;
}
