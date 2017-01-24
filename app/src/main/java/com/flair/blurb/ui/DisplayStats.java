package com.flair.blurb.ui;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;

/**
 * Created by sivaram-3911 on 23/01/17.
 */

public class DisplayStats {

    private static String TAG = DisplayStats.class.getSimpleName();
    Context context;
    View statsLayout;

    public DisplayStats(Context context, View statsLayout) {
        this.context = context;
        this.statsLayout = statsLayout;
    }

    public void loadStats(Cursor data) {
        Log.d(TAG, "loadStats: "+data.getCount());
    }
}
