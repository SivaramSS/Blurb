package com.flair.blurb;

/**
 * Created by sivaram-3911 on 07/01/17.
 */

public class App {

    static final int BLURB_NOTIFICATION_ID = 5555;

    static final String CATEGORY_UNCATEGORIZED = "UNCATEGORIZED";
    static final String CATEGORY_SYSTEM = "SYSTEM";
    static final String CATEGORY_NEWS = "NEWS";
    static final String CATEGORY_SOCIAL = "SOCIAL";
    static final String CATEGORY_IMPORTANT = "IMPORTANT";
    static final String CATEGORY_PROMOTIONS = "PROMOTIONS";

    static final int REQUEST_CODE_SOCIAL = 1111;
    static final int REQUEST_CODE_NEWS = 2222;
    static final int REQUEST_CODE_SYSTEM = 3333;
    static final int REQUEST_CODE_MORE = 4444;

    String pkgname, category;

    public App() {
    }

    public App(String pkgname, String category) {
        this.pkgname = pkgname;
        this.category = category;
    }

    public String getPkgname() {
        return pkgname;
    }

    public String getCategory() {
        return category;
    }

    public static String getApi18Key(int id) {
        return id+"_"+BLURB_NOTIFICATION_ID;
    }
}
