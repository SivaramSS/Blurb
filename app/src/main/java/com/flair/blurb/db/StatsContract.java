package com.flair.blurb.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by sivaram-3911 on 23/01/17.
 */

public class StatsContract {
    private StatsContract() {
    }

    public static class StatsEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATS_TABLE_STRING).build();
        public static final String TABLE_NAME = "logtable";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_CATEGORY = "category";
        public static final String COLUMN_TIMESTAMP = "timestamp";

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY_STRING
                + "/" + PATH_STATS_TABLE_STRING;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY_STRING
                + "/" + PATH_STATS_TABLE_STRING;

        public static Uri buildUriWithId(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    static final String CREATE_STATS_TABLE = "CREATE TABLE " + StatsEntry.TABLE_NAME + " ("
            + StatsEntry._ID + " INTEGER PRIMARY KEY,"
            + StatsEntry.COLUMN_KEY + " TEXT,"
            + StatsEntry.COLUMN_CATEGORY + " TEXT,"
            + StatsEntry.COLUMN_TIMESTAMP + " TEXT"
            + ");";

    static final String DELETE_STATS_TABLE = " DROP TABLE IF EXISTS " + StatsEntry.TABLE_NAME;

    static final String CONTENT_AUTHORITY_STRING = "com.flair.blurb";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY_STRING);

    public static final String PATH_STATS_TABLE_STRING = StatsEntry.TABLE_NAME;
}
