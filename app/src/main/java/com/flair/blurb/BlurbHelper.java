package com.flair.blurb;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by sivaram-3911 on 07/01/17.
 */
public class BlurbHelper {

    public static final String TAG = BlurbOn.class.getSimpleName();
    private static int idCounter = 0;

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
    public void instantiateBlurb(Context context, DataChangeNotfier notifier) {
        classifyInstalledApps(context, notifier);
    }

    public static int getId() {
        return idCounter++;
    }

    public static String getKey(StatusBarNotification notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH) {
            return App.getApi18Key(notification.getId());
        } else {
            return notification.getKey();
        }
    }

    private void classifyInstalledApps(Context context, DataChangeNotfier notifier) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        firebaseDatabase.setPersistenceEnabled(false);
        final DatabaseReference writeNodeRef = firebaseDatabase.getReference(context.getString(R.string.root_node_key)).child(context.getString(R.string.write_node_key));
//        writeNodeRef.keepSynced(true);

        List<ApplicationInfo> applist = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : applist) {

            String pkgname = applicationInfo.packageName.replace('.', '-');
            String category = (applicationInfo.flags
                    & ApplicationInfo.FLAG_SYSTEM) != 0 ?
                    App.CATEGORY_SYSTEM : App.CATEGORY_UNCATEGORIZED;

            writeNodeRef.child(pkgname).addValueEventListener(new AppCategoryChangeListener(writeNodeRef, pkgname, category, notifier));
        }
    }


    synchronized void transferNotifications(String pkgname, HashMap<String, StatusBarNotification> mapToReceiveTransferredValues, HashMap<String, StatusBarNotification>... maps) {
        HashMap<String, StatusBarNotification> receiver = mapToReceiveTransferredValues;

        for (HashMap<String, StatusBarNotification> map : maps) {

            Iterator<StatusBarNotification> iterator = map.values().iterator();
            while (iterator.hasNext()) {
                StatusBarNotification notification = iterator.next();
                if (notification.getPackageName().replace('.', '-').equals(pkgname)) {
                    String key = getKey(notification);
                    receiver.put(key, notification);
                    map.remove(key);
                }
            }

        }
    }

    void postBlurbNotification(Context context) {
        NotificationManager nm = ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE));
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.blurb_ongoing);

        String intent_key = context.getString(R.string.intent_key);
        Intent socialIntent = new Intent();
        socialIntent.putExtra(intent_key, App.REQUEST_CODE_SOCIAL);

        PendingIntent social = PendingIntent.getService(context, App.REQUEST_CODE_SOCIAL, new Intent(context, BlurbNotificationService.class).putExtra(intent_key, App.REQUEST_CODE_SOCIAL), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent news = PendingIntent.getService(context, App.REQUEST_CODE_NEWS, new Intent(context, BlurbNotificationService.class).putExtra(intent_key, App.REQUEST_CODE_NEWS), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent system = PendingIntent.getService(context, App.REQUEST_CODE_SYSTEM, new Intent(context, BlurbNotificationService.class).putExtra(intent_key, App.REQUEST_CODE_SYSTEM), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent more = PendingIntent.getService(context, App.REQUEST_CODE_MORE, new Intent(context, BlurbNotificationService.class).putExtra(intent_key, App.REQUEST_CODE_MORE), PendingIntent.FLAG_UPDATE_CURRENT);

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

        nm.notify(App.BLURB_NOTIFICATION_ID, notification);
    }
}
