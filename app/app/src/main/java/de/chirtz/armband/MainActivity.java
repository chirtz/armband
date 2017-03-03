package de.chirtz.armband;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import de.chirtz.armband.common.InfoDialogFragment;
import de.chirtz.armband.i5lib.ServiceConnector;
import de.chirtz.armband.notifications.NotificationService;

import static de.chirtz.armband.i5lib.BluetoothLeService.STATE_CONNECTED;
import static de.chirtz.armband.i5lib.BluetoothLeService.STATE_CONNECTING;
import static de.chirtz.armband.i5lib.BluetoothLeService.STATE_DISCONNECTED;
import static de.chirtz.armband.i5lib.BluetoothLeService.STATE_DISCONNECTING;
import static de.chirtz.armband.i5lib.BluetoothLeService.STATE_LOST;


public class MainActivity extends AppCompatActivity implements ServiceConnector.ConnectionStateChangedListener, InfoDialogFragment.InfoDialogDismissListener {

    private final static String TAG = "MainActivity";
    private ServiceConnector connector;
    private Menu menu;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean waitForDevice = false;
    private boolean btOnBeforeConnect = false;
    private final static int PERMISSIONS_REQUEST = 1;
    private static final String TAG_NOTIFICATION_LISTENER_ENABLE = "enable_notifications";
    private static final String TAG_LOCATION_ENABLE = "enable_location";
    private static final String TAG_GET_PERMISSIONS = "get_permissions";
    private final static int REQUEST_INFO_DIALOG_NOTIFICATIONS = 12;
    private final static int REQUEST_INFO_DIALOG_LOCATION = 13;
    private final static int REQUEST_INFO_DIALOG_PERMISSIONS = 14;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PagerAdapter mPagerAdapter = new PagerAdapter(this);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Log.d(TAG, "" + (savedInstanceState != null));
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        connector = new ServiceConnector(MainActivity.this);
        connector.addListener(this);
        connector.startService();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        this.menu = menu;
        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        connector.unregisterReceiver();
        connector.unbind();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        connector.unbind();
        unregisterReceiver(mBTReceiver);
    }


    @Override
    public void onResume() {
        super.onResume();
        checkPermissions();
        registerReceiver(mBTReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        if (connector != null)
            connector.bind();
        Log.d(TAG, "onResume");
    }


    private void connectPress() {
        switch(connector.getConnectionState()) {
            case STATE_CONNECTED:
            case STATE_CONNECTING:
                connector.disconnect();
                break;
            case STATE_DISCONNECTED:
                btOnBeforeConnect = mBluetoothAdapter.isEnabled();
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                    waitForDevice = true;
                } else {
                    connector.connect();
                }
                break;
            case STATE_LOST:
                Log.d(TAG, "State lost, no button action");
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connection:
                Log.d(TAG, "Connect pressed");
                    connectPress();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    public void sendFiltersUpdatedBroadcast() {
        Log.d(TAG, "Filters updated");
        Intent intent = new Intent(NotificationService.NOTIFICATION_SERVICE_CMD);
        intent.putExtra(NotificationService.NOTIFICATION_SERVICE_CMD, NotificationService.CMD_FILTER_UPDATED);
        sendBroadcast(intent);
    }

    private void sendControlNotificationListenerBroadcast(boolean startListening) {
        Intent intent = new Intent(NotificationService.NOTIFICATION_SERVICE_CMD);
        intent.putExtra(NotificationService.NOTIFICATION_SERVICE_CMD, startListening ? NotificationService.CMD_START_LISTENING: NotificationService.CMD_STOP_LISTENING);
        sendBroadcast(intent);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            InfoDialogFragment.newInstance(REQUEST_INFO_DIALOG_PERMISSIONS, getString(R.string.permission_access_title),
                    getString(R.string.permission_access_message)).show(getSupportFragmentManager(), TAG_GET_PERMISSIONS);
        }
        ContentResolver contentResolver = getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver,
                "enabled_notification_listeners");
        String packageName = getPackageName();
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName)) {
            InfoDialogFragment.newInstance(REQUEST_INFO_DIALOG_NOTIFICATIONS, getString(R.string.enable_notifications_title),
                    getString(R.string.enable_notifications_message)).show(getSupportFragmentManager(), TAG_NOTIFICATION_LISTENER_ENABLE);
        }
        try {
            int locationMode = Settings.Secure.getInt(contentResolver, Settings.Secure.LOCATION_MODE);
            if (locationMode == Settings.Secure.LOCATION_MODE_OFF) { // Location services off
                InfoDialogFragment.newInstance(REQUEST_INFO_DIALOG_LOCATION, getString(R.string.enable_location_title),
                        getString(R.string.enable_location_message)).show(getSupportFragmentManager(), TAG_LOCATION_ENABLE);
            }

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
               // if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

              //  } else {
                   // checkPermissions();
               // }
                return;
            }
        }
    }

    @Override
    public void onConnected() {
        Log.d(TAG, "onConnected");
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.action_connection);
            if (item != null)
                item.setIcon(getDrawable(R.mipmap.connect));
        }
        sendControlNotificationListenerBroadcast(true);
    }


    @Override
    public void onDisconnected() {
        Log.d(TAG, "onDisconnected");
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.action_connection);
            if (item != null)
                item.setIcon(getDrawable(R.mipmap.disconnect));
        }
        sendControlNotificationListenerBroadcast(false);
        if (!btOnBeforeConnect)
            mBluetoothAdapter.disable();
    }

    @Override
    public void onConnectionLost() {
        Log.d(TAG, "onConnectionLost");
        onDisconnected();
    }

    @Override
    public void onConnectionChanging(byte connectionState) {
        MenuItem item = null;
        if (menu != null)
            item = menu.findItem(R.id.action_connection);
        switch(connectionState) {
            case STATE_CONNECTING:
                Log.d(TAG, "onConnecting");
                    if (item != null)
                        item.setIcon(getDrawable(R.mipmap.connecting));
                break;
            case STATE_DISCONNECTING:
                Log.d(TAG, "onDisconnecting");
                if (item != null)
                    item.setIcon(getDrawable(R.mipmap.disconnect));
                break;
        }
    }

    public ServiceConnector getServiceConnector() {
        return connector;
    }


    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        //Indicates the local Bluetooth adapter is off.
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        //Indicates the local Bluetooth adapter is turning on. However local clients should wait for STATE_ON before attempting to use the adapter.
                        break;

                    case BluetoothAdapter.STATE_ON:
                        if (waitForDevice) {
                            connector.connect();
                            waitForDevice = false;
                        }
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        //Indicates the local Bluetooth adapter is turning off. Local clients should immediately attempt graceful disconnection of any remote links.
                        break;
                }
            }
        }
    };

    @Override
    public void onInfoDialogDismissed(int request) {
        switch(request) {
            case REQUEST_INFO_DIALOG_NOTIFICATIONS:
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
                break;
            case REQUEST_INFO_DIALOG_LOCATION:
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                break;
            case REQUEST_INFO_DIALOG_PERMISSIONS:
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST);
        }
    }
}
