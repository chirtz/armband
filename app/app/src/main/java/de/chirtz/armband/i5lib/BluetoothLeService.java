package de.chirtz.armband.i5lib;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.common.primitives.Bytes;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import de.chirtz.armband.Alarm;
import de.chirtz.armband.MainActivity;
import de.chirtz.armband.R;
import de.chirtz.armband.common.Tools;


public class BluetoothLeService extends Service implements DeviceConnector.DeviceConnectorCallback {

    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private final Messenger mMessenger = new Messenger(new Handler(new IncomingHandler()));
    private static final UUID BAND_CHARACTERISTIC_NEW_WRITE = UUID.fromString("0000ff21-0000-1000-8000-00805f9b34fb");
    private static final UUID BAND_CHARACTERISTIC_NOTIFICATION = UUID.fromString("0000ff23-0000-1000-8000-00805f9b34fb");
    private static final UUID BAND_NOTIFICATION_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private final List<BluetoothGattCharacteristic> characteristics = new LinkedList<>();

    public static final String BROADCAST = "broadcast";
    public static final String CMD = "cmd";
    public static final String CMD_WRITE = "cmd_write";
    private static final int CMD_STOP_THREAD = -5;
    public static final String COMMAND_PKG = "command_pkg";


    public static final String DEVICE_CONNECTION = "device_connection";
    public static final String I_CONNECTION_STATE = "device_connection";
    public final static byte STATE_DISCONNECTED = 1;
    public final static byte STATE_CONNECTED = 2;
    public final static byte STATE_LOST = 3;
    public final static byte STATE_CONNECTING = 4;
    public final static byte STATE_DISCONNECTING = 5;

    public static final String DEVICE = "DEVICE";
    public static final String DEVICE_ANSWER = "device_answer";
    public static final String DEVICE_ANSWER_TYPE = "device_answer_type";


    public static final byte DATA_DEVICE_INFO = 0;
    public static final String I_DEVICE_INFO = "device_info";
    public static final byte DATA_DEVICE_POWER = 1;
    public static final String I_DEVICE_POWER = "device_power";
    private static final String I_DEVICE_DATE = "device_date";
    private static final byte DATA_DEVICE_DATE = 17;
    private static final String I_DEVICE_BLE = "device_ble";
    private static final byte DATA_DEVICE_BLE = 19;

    private static final byte DATA_USER_PARAMS = 33;
    private static final String I_USER_PARAMS = "user_params";
    private static final byte DATA_ALARM = 21;



    private static final byte DATA_DEVICE_USER_INPUT = 77;
    public static final byte DATA_DEVICE_UNKNOWN = 99;
    private static final String I_DEVICE_CONFIG = "device_config";
    private static final byte DATA_DEVICE_CONFIG = 25;
    private static final byte DATA_USER_INPUT = 64;
    public static final byte DATA_USER_INPUT_MENU = 2;
    public static final byte DATA_USER_INPUT_CAMERA_SHORT = 1;
    public static final byte DATA_USER_INPUT_CAMERA_LONG = 7;
    private static final String I_DEVICE_USER_INPUT = "user_input_cfg";



    public static final int MSG_DISCONNECT = 0;
    public static final int MSG_CONNECT = 1;
    public static final int MSG_SETTINGS_CHANGED = 2;
    public static final int MSG_USER_PARAMS_CHANGED = 3;
    public static final int MSG_ALARMS_CHANGED = 4;


    private String PREFERENCE_24HOUR;
    private String PREFERENCE_LIGHT;
    private String PREFERENCE_AUTO_SLEEP;
    private String PREFERENCE_GESTURE;
    private String PREFERENCE_ENGLISH_UNITS;

    private String USER_PREFERENCE_AGE;
    private String USER_PREFERENCE_GENDER;
    private String USER_PREFERENCE_WEIGHT;
    private String USER_PREFERENCE_HEIGHT;
    private String USER_PREFERENCE_STEP_GOAL;

    public final static String ALARM_PREFIX = "ALARM_";
    public final static int MAX_ALARMS = 7;

    private BroadcastReceiver nReceiver;
    private byte[] recvBuffer;
    private int recvBufferLength = 0;
    private boolean dataEnd = true;
    private IntentFilter filter;
    private final LinkedBlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();
    private ExecutorService executorService;
    private String deviceName = "";

    private Notification.Builder builder;

    private DeviceConnector connector;
    private BluetoothGatt mGatt;
    private byte connectionState = STATE_DISCONNECTED;
    private final static byte NOTIF_ID = 1;
    private NotificationManager nm;
    private Band smartWatch;

    private int batteryPower = 0;


    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "BOUND");
        connectionStateChanged(connectionState);
        return mMessenger.getBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerBroadcastReceiver();
        return START_STICKY;
    }

    private final Runnable communicationTask = new Runnable() {
        public void run() {
            BluetoothGattCharacteristic characteristic = null;
            while (true) {
                try {
                    Command cmd = commandQueue.take();
                    if (cmd.getStart() == CMD_STOP_THREAD) {
                        mGatt.disconnect();
                        break;
                    }
                    if (cmd.getDelay() > 0)
                        Thread.sleep(cmd.getDelay());
                    if (characteristic == null)
                        characteristic = getCharacteristic(BAND_CHARACTERISTIC_NEW_WRITE);
                    byte[] d;
                    if (cmd.getStart() >= 0 && cmd.getEnd() >= 0)
                        d = Tools.getDataByte(Tools.getCommandHeader(cmd.getStart(), cmd.getEnd()), cmd.getPayload());
                    else
                        d = cmd.getPayload();
                    if (characteristic != null && mGatt != null) {
                        characteristic.setValue(d);
                        mGatt.writeCharacteristic(characteristic);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    @Override
    public void onCreate() {
        Log.d(TAG, "STARTED");
        filter = new IntentFilter();
        filter.addAction(BROADCAST);
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        smartWatch = new Band(LocalBroadcastManager.getInstance(this));
        PREFERENCE_24HOUR = getString(R.string.PREFERENCE_24HOUR);
        PREFERENCE_LIGHT = getString(R.string.PREFERENCE_LIGHT);
        PREFERENCE_AUTO_SLEEP = getString(R.string.PREFERENCE_AUTO_SLEEP);
        PREFERENCE_GESTURE = getString(R.string.PREFERENCE_GESTURE);
        PREFERENCE_ENGLISH_UNITS = getString(R.string.PREFERENCE_ENGLISH_UNITS);

        USER_PREFERENCE_AGE = getString(R.string.USER_PREFERENCE_AGE);
        USER_PREFERENCE_GENDER = getString(R.string.USER_PREFERENCE_GENDER);
        USER_PREFERENCE_HEIGHT = getString(R.string.USER_PREFERENCE_HEIGHT);
        USER_PREFERENCE_WEIGHT = getString(R.string.USER_PREFERENCE_WEIGHT);
        USER_PREFERENCE_STEP_GOAL = getString(R.string.USER_PREFERENCE_STEP_GOAL);
        createStatusBarNotification();
    }

    private void createStatusBarNotification() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        builder = new android.app.Notification.Builder(this)
                .setSmallIcon(R.mipmap.notification_icon)
                .setContentTitle(getString(R.string.disconnected))
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(Notification.PRIORITY_MIN)
                .setContentIntent(contentIntent);
        nm.notify(NOTIF_ID, builder.build());
        startForeground(NOTIF_ID, builder.build());
    }

    private void updateStatusBarNotification(byte state) {
        switch(state) {
            case STATE_CONNECTED:
                builder.setPriority(Notification.PRIORITY_MIN);
                builder.setContentTitle(getString(R.string.connected));
                builder.setContentText(getString(R.string.battery) + ": " + batteryPower + "%");
                break;
            case STATE_DISCONNECTED:
                builder.setPriority(Notification.PRIORITY_DEFAULT);
                builder.setContentTitle(getString(R.string.disconnected));
                break;
            case STATE_CONNECTING:
                builder.setPriority(Notification.PRIORITY_DEFAULT);
                builder.setContentTitle(getString(R.string.connecting));
                break;
            case STATE_DISCONNECTING:
                builder.setPriority(Notification.PRIORITY_DEFAULT);
                builder.setContentTitle(getString(R.string.disconnecting));
                break;
        }
        nm.notify(NOTIF_ID, builder.build());
    }

    @Override
    public boolean onUnbind(Intent i) {
        Log.d(TAG, "UNBIND");
        return super.onUnbind(i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "DESTROYED");
        unregisterBroadcastReceiver();
    }

    private void unregisterBroadcastReceiver() {
        if (nReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(nReceiver);
            unregisterReceiver(nReceiver);
            nReceiver = null;
        }
    }

    private void registerBroadcastReceiver() {
        if (nReceiver == null) {
            nReceiver = new NotificationReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(nReceiver, filter);
            registerReceiver(nReceiver, filter);
        }
    }




    private BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        for (BluetoothGattCharacteristic c: characteristics) {
            if (c.getUuid().equals(uuid))
                return c;
        }
        return null;
    }


    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    Log.i("onConnectionStateChange", "Status: " + status);
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:
                            Log.i("gattCallback", "STATE_CONNECTED");
                            gatt.discoverServices();
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            Log.d("gattCallback", "STATE_DISCONNECTED: " + status);
                            mGatt.close();
                            if (status == BluetoothGatt.GATT_SUCCESS)
                                connectionStateChanged(STATE_DISCONNECTED);
                            else {
                                connectionStateChanged(STATE_LOST);
                                connector.connect();
                                connectionStateChanged(STATE_CONNECTING);
                            }
                            break;
                        default:
                            Log.e("gattCallback", "STATE_OTHER");
                    }

                }


                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                    for (BluetoothGattService s : gatt.getServices()) {
                        for (BluetoothGattCharacteristic ch : s.getCharacteristics()) {
                            if (!characteristics.contains(ch))
                                characteristics.add(ch);
                        }
                    }
                    deviceName = gatt.getDevice().getName();
                    BluetoothGattCharacteristic c = getCharacteristic(BAND_CHARACTERISTIC_NOTIFICATION);
                    gatt.setCharacteristicNotification(c, true);

                    BluetoothGattDescriptor descriptor = c.getDescriptor(BAND_NOTIFICATION_DESCRIPTOR);
                    if ((c.getProperties() & 32) != 0)
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    else if ((c.getProperties() & 16) != 0)
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    else
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                    connectionStateChanged(STATE_CONNECTED);
                    sendSettingsToDevice();
                    sendUserParamsToDevice();
                    sendAllAlarmSettingsToDevice();
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    parseResponse(characteristic);
                }
            };

    private void connectionStateChanged(byte state) {
        connectionState = state;
        updateStatusBarNotification(state);
        Intent intent = new Intent(DEVICE_CONNECTION);
        intent.putExtra(I_CONNECTION_STATE, state);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void parseResponse(BluetoothGattCharacteristic chara){
        byte[] data = chara.getValue();
        if (data != null && data.length != 0) {
            if (this.dataEnd) {
                if (data[0] == 35) {
                    this.recvBufferLength = data[3];
                } else {
                    String buff = "";
                    Log.i(TAG, "XUnknown command");
                    for (Byte b : this.recvBuffer)
                        buff += b + " ";
                    Log.i(TAG, "Buffer: " + buff);
                    return;
                }
            }
            if (this.recvBuffer == null)
                this.recvBuffer = data;
            else
                this.recvBuffer = Bytes.concat(this.recvBuffer, data);
            if (this.recvBuffer.length - 4 >= this.recvBufferLength) {
                this.dataEnd = true;
                Intent intent = new Intent(DEVICE_ANSWER);
                if (this.recvBuffer.length >= 3) {
                    switch (this.recvBuffer[2]){
                        case DATA_DEVICE_DATE:
                            int year = Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 4, 5)) + 2000;
                            int month = Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 5, 6));
                            int day = Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 6, 7)) + 1;
                            int hour = Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 7, 8));
                            int minute = Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 8, 9));
                            int second = Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 9, 10));
                            long ts = new GregorianCalendar(year, month, day, hour, minute, second).getTimeInMillis();
                            intent.putExtra(DEVICE_ANSWER_TYPE, DATA_DEVICE_DATE);
                            intent.putExtra(I_DEVICE_DATE, ts);
                            break;
                        case DATA_DEVICE_POWER:
                            batteryPower = Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 4, 5));
                            intent.putExtra(DEVICE_ANSWER_TYPE, DATA_DEVICE_POWER);
                            intent.putExtra(I_DEVICE_POWER, batteryPower);
                            updateStatusBarNotification(connectionState);
                            break;
                        case DATA_DEVICE_BLE:
                            int ble = Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 4, 5));
                            intent.putExtra(DEVICE_ANSWER_TYPE, DATA_DEVICE_BLE);
                            intent.putExtra(I_DEVICE_BLE, ble);
                            break;
                        case DATA_USER_INPUT:
                            int code = Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 4, 5));
                            intent.putExtra(DEVICE_ANSWER_TYPE, DATA_DEVICE_USER_INPUT);
                            intent.putExtra(I_DEVICE_USER_INPUT, code);
                            break;
                        case DATA_DEVICE_CONFIG:
                            HashMap<String, Integer> dat = new HashMap<>();
                            dat.put("led", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 4, 5)));
                            dat.put("gesture", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 5, 6)));
                            dat.put("englishMetric", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 6, 7)));
                            dat.put("24hour", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 7, 8)));
                            dat.put("autoSleep", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 8, 9)));
                            intent.putExtra(DEVICE_ANSWER_TYPE, DATA_DEVICE_CONFIG);
                            intent.putExtra(I_DEVICE_CONFIG, dat);
                            break;
                        case DATA_USER_PARAMS:
                            HashMap<String, Integer> userData = new HashMap<>();
                            userData.put("height", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 4, 5)));
                            userData.put("weight", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 5, 6)));
                            userData.put("gender", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 6, 7)));
                            userData.put("age", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 7, 8)));
                            userData.put("goal_low", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 8, 9)));
                            userData.put("goal_high", Tools.bytesToInt(Arrays.copyOfRange(this.recvBuffer, 9, 10)));
                            intent.putExtra(DEVICE_ANSWER_TYPE, DATA_USER_PARAMS);
                            intent.putExtra(I_USER_PARAMS, userData);
                            break;
                        case DATA_ALARM:
                            String buf = "";
                            for (Byte b : this.recvBuffer)
                                buf += b + " ";
                            Log.i(TAG, "Buffer: " + buf);
                            break;
                        case DATA_DEVICE_INFO:
                            DeviceInfo info = DeviceInfo.fromData(this.recvBuffer);
                            info.setName(deviceName);
                            intent.putExtra(DEVICE_ANSWER_TYPE, DATA_DEVICE_INFO);
                            intent.putExtra(I_DEVICE_INFO, info);
                            break;
                        default:
                            String buff = "";
                            Log.i(TAG, "Unknown command");
                            for (Byte b : this.recvBuffer)
                                buff += b + " ";
                            Log.i(TAG, "Buffer: " + buff);
                            intent.putExtra(DEVICE_ANSWER_TYPE, DATA_DEVICE_UNKNOWN);
                    }

                }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                this.recvBuffer = new byte[0];
                return;
            }
            this.dataEnd = false;
        }
    }

    @Override
    public void onDeviceConnected(BluetoothGatt gatt) {
        this.mGatt = gatt;
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(communicationTask);
    }

    private void sendSettingsToDevice() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        smartWatch.setConfig(
                prefs.getBoolean(PREFERENCE_LIGHT, false),
                prefs.getBoolean(PREFERENCE_GESTURE, true),
                !prefs.getBoolean(PREFERENCE_ENGLISH_UNITS, false),
                !prefs.getBoolean(PREFERENCE_24HOUR, false),
                prefs.getBoolean(PREFERENCE_AUTO_SLEEP, false));
    }

    private void sendUserParamsToDevice() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        smartWatch.setUserParams(
                prefs.getInt(USER_PREFERENCE_HEIGHT, 175),
                prefs.getInt(USER_PREFERENCE_WEIGHT, 70),
                prefs.getString(USER_PREFERENCE_GENDER, "male").equals("female"),
                prefs.getInt(USER_PREFERENCE_AGE, 25),
                prefs.getInt(USER_PREFERENCE_STEP_GOAL, 5000));
    }

    private void sendAllAlarmSettingsToDevice() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d(TAG, "Sending alarm settings to device");
        for (int i=0; i<MAX_ALARMS; i++) {
            if (prefs.contains(ALARM_PREFIX+i)) {
                Alarm a = Alarm.fromString(i, prefs.getString(ALARM_PREFIX+i, null));
                smartWatch.setAlarm(a);

            } else {
                smartWatch.disableAlarm(i);
            }
        }
    }

    private void sendAlarmSettingsToDevice(int alarmId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains(ALARM_PREFIX+alarmId)) {
            Alarm a = Alarm.fromString(alarmId, prefs.getString(ALARM_PREFIX+alarmId, null));
            smartWatch.setAlarm(a);
        } else {
            smartWatch.disableAlarm(alarmId);
        }
    }

    private class IncomingHandler implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISCONNECT:
                    connector.stopScanning();
                    if (connectionState == STATE_CONNECTING) { // we were still connecting, so just stop the attempt
                        connectionStateChanged(STATE_DISCONNECTING);
                        connectionStateChanged(STATE_DISCONNECTED);
                    } else {
                        connectionStateChanged(STATE_DISCONNECTING);
                        if (commandQueue != null) {
                            commandQueue.clear();
                            commandQueue.add(new Command(CMD_STOP_THREAD, CMD_STOP_THREAD, CMD_STOP_THREAD));
                        }
                        executorService.shutdown();
                    }
                    break;
                case MSG_CONNECT:
                    connectionStateChanged(STATE_CONNECTING);
                    commandQueue.clear();
                    String deviceId = msg.getData().getString(DEVICE);
                    connector = new DeviceConnector(deviceId, mGattCallback, BluetoothLeService.this);
                    connector.connect();
                    break;
                case MSG_SETTINGS_CHANGED:
                    sendSettingsToDevice();
                    break;
                case MSG_USER_PARAMS_CHANGED:
                    sendUserParamsToDevice();
                    break;
                case MSG_ALARMS_CHANGED:
                    sendAlarmSettingsToDevice(msg.arg1);
                    break;
            }
            return true;
        }
    }


    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra(CMD)) {
                case CMD_WRITE:
                    Command cmd = new Command(intent.getBundleExtra(COMMAND_PKG));
                    commandQueue.add(cmd);
                    break;
            }
            }
    }

}
