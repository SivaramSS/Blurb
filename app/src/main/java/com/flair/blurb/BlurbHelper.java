package com.flair.blurb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.flair.blurb.data.Notifications;
import com.flair.blurb.firebase.AppCategoryChangeListener;
import com.flair.blurb.firebase.DataChangeNotfier;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by sivaram-3911 on 07/01/17.
 */
public class BlurbHelper {

    private static BlurbHelper ourInstance = new BlurbHelper();

    public static BlurbHelper getInstance() {
        return ourInstance;
    }

    private BlurbHelper() {
    }

    /**
     * Called when the service starts for the first time.
     * It classifies the installed apps into categories.
     */
    public void categorizeInstalledApps(Context context, DataChangeNotfier notifier) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        firebaseDatabase.setPersistenceEnabled(false);
        final DatabaseReference writeNodeRef = firebaseDatabase.getReference(context.getString(R.string.root_node_key)).child(context.getString(R.string.write_node_key));
//        writeNodeRef.keepSynced(true);

        List<ApplicationInfo> applist = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : applist) {

            String pkgname = applicationInfo.packageName.replace('.', '-');
            String category = (applicationInfo.flags
                    & ApplicationInfo.FLAG_SYSTEM) != 0 ?
                    Constants.CATEGORY_SYSTEM : Constants.CATEGORY_UNCATEGORIZED;

            writeNodeRef.child(pkgname).addValueEventListener(new AppCategoryChangeListener(writeNodeRef, pkgname, category, notifier));
        }
    }

    @Constants.CategoryDef
    public String defaultCategoryToShow(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        @Constants.CategoryDef String category = prefs.getString(context.getString(R.string.default_category_to_show_key), Constants.CATEGORY_SOCIAL);
        return category;
    }

    void postBlurbNotification(Context context) {
        NotificationManager nm = ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE));
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.blurb_ongoing);

        Intent socialIntent = new Intent();
        socialIntent.putExtra(Notifications.intent_request_key, Constants.REQUEST_CODE_SOCIAL);

        PendingIntent social = PendingIntent.getService(context, Constants.REQUEST_CODE_SOCIAL, new Intent(context, BlurbNotificationService.class).putExtra(Notifications.intent_request_key, Constants.REQUEST_CODE_SOCIAL), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent news = PendingIntent.getService(context, Constants.REQUEST_CODE_NEWS, new Intent(context, BlurbNotificationService.class).putExtra(Notifications.intent_request_key, Constants.REQUEST_CODE_NEWS), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent system = PendingIntent.getService(context, Constants.REQUEST_CODE_SYSTEM, new Intent(context, BlurbNotificationService.class).putExtra(Notifications.intent_request_key, Constants.REQUEST_CODE_SYSTEM), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent more = PendingIntent.getService(context, Constants.REQUEST_CODE_MORE, new Intent(context, BlurbNotificationService.class).putExtra(Notifications.intent_request_key, Constants.REQUEST_CODE_MORE), PendingIntent.FLAG_UPDATE_CURRENT);

        contentView.setOnClickPendingIntent(R.id.social, social);
        contentView.setOnClickPendingIntent(R.id.news, news);
        contentView.setOnClickPendingIntent(R.id.system, system);
        contentView.setOnClickPendingIntent(R.id.more, more);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(contentView)
                .setPriority(Notification.PRIORITY_MIN)
                .setOngoing(true)
                .build();

        nm.notify(Constants.BLURB_NOTIFICATION_ID, notification);
    }

}
