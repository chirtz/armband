package de.chirtz.armband.notifications;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.LinkedList;

import de.chirtz.armband.filter.Filter;
import de.chirtz.armband.filter.FilterDatabase;
import de.chirtz.armband.filter.filter_properties.AccumulateTextProperty;
import de.chirtz.armband.filter.filter_properties.FilterProperty;
import de.chirtz.armband.filter.filter_properties.ShowOnDisplayProperty;
import de.chirtz.armband.i5lib.Band;


public class NotificationService extends NotificationListenerService {

    private PackageManager packageManager;
    private LinkedList<Filter> filterList;
    private boolean active = false;
    private final static String TAG = "NotificationService";
    private final SparseArray<SBNotification> notifications = new SparseArray<>(); // Notification hash -> SBNotification

    public final static String NOTIFICATION_SERVICE_CMD = "notif_srv_cmd";
    public final static byte CMD_FILTER_UPDATED = 13;
    public final static byte CMD_STOP_LISTENING = 14;
    public final static byte CMD_START_LISTENING = 15;


    @Override
    public void onCreate() {
        packageManager = getPackageManager();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_SERVICE_CMD);
        registerReceiver(receiver, filter);
        //LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
        Log.d(TAG, "STARTED");
        loadFilters();
    }

    @Override
    public void onDestroy() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        unregisterReceiver(receiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!active) return;
        SBNotification notification = createOrGetNotification(sbn);
        int hash = notification.hashCode();

        Filter f = notification.getFirstMatchingFilter(filterList, sbn, getApplicationContext());
        if (f != null) {
            if (notifications.get(hash) != null) {
                AccumulateTextProperty accumulateTextProperty = f.getProperty(FilterProperty.Type.AccumulateText);
                notification.update(sbn, accumulateTextProperty.getValue());
            }
            notifications.put(hash, notification);
            Log.d(TAG, "Notification posted");
            Band smartWatch = new Band(this);

            ShowOnDisplayProperty displayProperty = f.getProperty(FilterProperty.Type.ShowOnDisplay);
            String msg;
            switch (displayProperty.getValue()) {
                case 0: // Message title
                    msg = notification.getTitle();
                    break;
                case 1: // Message content
                    msg = notification.getText();
                    break;
                case 2: // both
                    msg = notification.getTitle() + ": " + notification.getText();
                    break;
                default:
                    msg = "";
            }
            smartWatch.sendMessage(msg);
        }
    }





    private void removeNotification(int hash) {
        Log.d(TAG, "REMOVED");
        //    Log.d(TAG, "REMOVE: " + hash + "LEDNotification ID: " + ledAssignments.get(hash));
        if (notifications.get(hash) != null) {
            // Tell interested clients that a notification has been removed
            //SBNotification notif = notifications.get(hash);
            notifications.remove(hash);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName().trim();
        if (pkg.equals("android") || pkg.equals(getPackageName())) return;
        int hash = SBNotification.hashCode(sbn);
        removeNotification(hash);
    }


    private SBNotification createOrGetNotification(StatusBarNotification sbn) {
        int hash = SBNotification.hashCode(sbn);
        if (notifications.get(hash) != null)
            return notifications.get(hash);
        String appName = "";
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(sbn.getPackageName(), 0);
            appName = packageManager.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException ignored) {}
        return new SBNotification(appName, sbn, hash, getApplicationContext());
    }

    private void loadFilters() {
        FilterDatabase db = new FilterDatabase(getApplicationContext());
        filterList = db.getEntries();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getByteExtra(NOTIFICATION_SERVICE_CMD, (byte) -1)) {
                case CMD_FILTER_UPDATED:
                    loadFilters();
                    Log.d(TAG, "Filters reloaded");
                    break;
                case CMD_START_LISTENING:
                    active = true;
                    Log.d(TAG, "Listener active");
                    break;
                case CMD_STOP_LISTENING:
                    active = false;
                    Log.d(TAG, "Listener inactive");
                    break;
                default:
                    Log.d(TAG, "Received unknown command");

            }

        }
    };

}
