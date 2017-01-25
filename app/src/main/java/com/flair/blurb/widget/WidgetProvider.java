package com.flair.blurb.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.flair.blurb.BlurbNotificationService;
import com.flair.blurb.Constants;
import com.flair.blurb.R;

/**
 * Created by sivaram-3911 on 25/01/17.
 */

public class WidgetProvider extends AppWidgetProvider {

    public WidgetProvider() {
        super();
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(WidgetProvider.class.getSimpleName(), "onUpdate: ");
        PendingIntent pendingIntent = PendingIntent.getService(context, Constants.REQUEST_CODE_WIDGET, new Intent(context, BlurbNotificationService.class)
                .putExtra(context.getString(R.string.intent_request_key), Constants.REQUEST_CODE_WIDGET)
                .putExtra(context.getString(R.string.widget_id_key), appWidgetIds), PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        remoteViews.setOnClickPendingIntent(R.id.button_dnd, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }
}
