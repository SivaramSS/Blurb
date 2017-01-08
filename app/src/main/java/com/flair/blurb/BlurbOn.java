package com.flair.blurb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

public class BlurbOn extends AppCompatActivity {

    private static final String TAG = BlurbOn.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blurb_on);

        NotificationManager nm = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.blurb_ongoing);

        Intent socialIntent = new Intent();
        socialIntent.putExtra(getString(R.string.intent_key), App.REQUEST_CODE_SOCIAL);

        PendingIntent social = PendingIntent.getService(this, App.REQUEST_CODE_SOCIAL, new Intent(this, BlurbNotificationService.class).putExtra(getString(R.string.intent_key), App.REQUEST_CODE_SOCIAL), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent news = PendingIntent.getService(this, App.REQUEST_CODE_NEWS, new Intent(this, BlurbNotificationService.class).putExtra(getString(R.string.intent_key), App.REQUEST_CODE_NEWS), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent system = PendingIntent.getService(this, App.REQUEST_CODE_SYSTEM, new Intent(this, BlurbNotificationService.class).putExtra(getString(R.string.intent_key), App.REQUEST_CODE_SYSTEM), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent more = PendingIntent.getService(this, App.REQUEST_CODE_MORE, new Intent(this, BlurbNotificationService.class).putExtra(getString(R.string.intent_key), App.REQUEST_CODE_MORE), PendingIntent.FLAG_UPDATE_CURRENT);

        contentView.setOnClickPendingIntent(R.id.social, social);
        contentView.setOnClickPendingIntent(R.id.news, news);
        contentView.setOnClickPendingIntent(R.id.system, system);
        contentView.setOnClickPendingIntent(R.id.more, more);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(contentView)
                .setPriority(Notification.PRIORITY_MIN)
                .setOngoing(true)
                .build();

        nm.notify(App.BLURB_NOTIFICATION_ID, notification);
    }

}
