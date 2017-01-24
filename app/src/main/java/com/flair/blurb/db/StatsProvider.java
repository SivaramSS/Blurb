package com.flair.blurb.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by sivaram-3911 on 23/01/17.
 */

public class StatsProvider extends ContentProvider {

    private static final UriMatcher matcher = buildUriMatcher();
    public static final int CODE_STATS = 1;
    public static final int CODE_STATS_ITEM = 2;
    DatabaseHelper dbHelper;

    static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StatsContract.CONTENT_AUTHORITY_STRING;
        uriMatcher.addURI(authority, StatsContract.PATH_STATS_TABLE_STRING, CODE_STATS);
        uriMatcher.addURI(authority, StatsContract.PATH_STATS_TABLE_STRING + "/#", CODE_STATS_ITEM);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = matcher.match(uri);
        switch (match) {
            case CODE_STATS:
                return StatsContract.StatsEntry.CONTENT_TYPE;
            case CODE_STATS_ITEM:
                return StatsContract.StatsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown URI");
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (matcher.match(uri)) {
            case CODE_STATS:
                return getStatsData(uri, projection, selection, selectionArgs, sortOrder);
            case CODE_STATS_ITEM:
                return getStatsDataItem(uri, projection, sortOrder);
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (matcher.match(uri)) {
            case CODE_STATS:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                long id = db.insert(uri.getLastPathSegment(), null, values);
                return StatsContract.StatsEntry.buildUriWithId(id);
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (matcher.match(uri)) {
            case CODE_STATS:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                return db.update(StatsContract.StatsEntry.TABLE_NAME, values, selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (matcher.match(uri)) {
            case CODE_STATS:
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                return db.delete(StatsContract.StatsEntry.TABLE_NAME, selection, selectionArgs);
        }
        return 0;
    }

    private Cursor getStatsData(Uri uri, String[] projection, String selection, String[] selArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String tablename = uri.getLastPathSegment();
        return db.query(tablename, projection, selection, selArgs, null, null, sortOrder);
    }

    private Cursor getStatsDataItem(Uri uri, String[] projection, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String id = uri.getLastPathSegment();
        return db.query(StatsContract.StatsEntry.TABLE_NAME, projection, StatsContract.StatsEntry._ID+"=?", new String[]{id}, null, null, sortOrder);
    }
}
