package com.flair.blurb;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.service.notification.StatusBarNotification;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

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

    /** Called when the service starts for the first time.
     *  It classifies the installed apps into categories.
     */
    public void instantiateBlurb(Context context, DataChangeNotfier notifier) {
        classifyInstalledApps(context, notifier);
    }

    public static int getId() {
        return idCounter++;
    }

    private void classifyInstalledApps(Context context, DataChangeNotfier notifier) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
//        firebaseDatabase.setPersistenceEnabled(false);
        final DatabaseReference writeNodeRef = firebaseDatabase.getReference(context.getString(R.string.root_node_key)).child(context.getString(R.string.write_node_key));
//        writeNodeRef.keepSynced(true);

        List<ApplicationInfo> applist = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : applist) {

            String pkgname = applicationInfo.packageName.replace('.','-');
            String category = (applicationInfo.flags
                    & ApplicationInfo.FLAG_SYSTEM) != 0 ?
                    App.CATEGORY_SYSTEM : App.CATEGORY_UNCATEGORIZED;

            writeNodeRef.child(pkgname).addValueEventListener(new AppCategoryChangeListener(writeNodeRef, pkgname, category, notifier));
        }
    }

    public void classifyActiveNotifications(Context context, StatusBarNotification[] activeNotifications) {

    }
}
