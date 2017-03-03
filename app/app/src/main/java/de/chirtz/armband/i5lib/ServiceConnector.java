package de.chirtz.armband.i5lib;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

import android.os.Message;
import android.os.Messenger;

import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.util.LinkedList;


import de.chirtz.armband.R;

import static de.chirtz.armband.i5lib.BluetoothLeService.DEVICE;
import static de.chirtz.armband.i5lib.BluetoothLeService.DEVICE_ANSWER;
import static de.chirtz.armband.i5lib.BluetoothLeService.DEVICE_CONNECTION;
import static de.chirtz.armband.i5lib.BluetoothLeService.STATE_CONNECTED;
import static de.chirtz.armband.i5lib.BluetoothLeService.STATE_CONNECTING;
import static de.chirtz.armband.i5lib.BluetoothLeService.STATE_DISCONNECTED;
import static de.chirtz.armband.i5lib.BluetoothLeService.STATE_DISCONNECTING;
import static de.chirtz.armband.i5lib.BluetoothLeService.STATE_LOST;


public class ServiceConnector extends BroadcastReceiver {

    private final static String TAG = "ServiceConnector";
    private final Context context;
    private Messenger serviceConnection;
    private final IntentFilter filter;
    private final LinkedList<ConnectionStateChangedListener> listeners = new LinkedList<>();
    private byte connectionState = BluetoothLeService.STATE_DISCONNECTED;

    public interface ConnectionStateChangedListener {
        void onConnected();
        void onDisconnected();
        void onConnectionLost();
        void onConnectionChanging(byte connectionState);
    }

    public ServiceConnector(Context context) {
        this.context = context;
        filter = new IntentFilter();
        filter.addAction(DEVICE_CONNECTION);
        filter.addAction(DEVICE_ANSWER);
        registerReceiver();
    }


    public void addListener(ConnectionStateChangedListener listener) {
        this.listeners.add(listener);
    }

   public void connect() {
       Message msg = Message.obtain();
       msg.what = BluetoothLeService.MSG_CONNECT;
       Bundle data = new Bundle();
       SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
       String deviceId = String.valueOf(prefs.getInt(context.getString(R.string.PREFERENCE_DEVICE_ID), 0));
       data.putString(DEVICE, deviceId);
       msg.setData(data);
       try {
           serviceConnection.send(msg);
       } catch (RemoteException e) {
           e.printStackTrace();
       }
   }


    public void startService() {
        Intent intent = new Intent("de.chirtz.armband.i5lib.BluetoothLeService");
        intent.setPackage("de.chirtz.armband");
        context.startService(intent);
        context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    public void stopService() {
        Intent intent = new Intent("de.chirtz.armband.i5lib.BluetoothLeService");
        intent.setPackage("de.chirtz.armband");
        context.stopService(intent);
    }


    public void sendBandSettingsUpdated() {
        Message msg = Message.obtain();
        msg.what = BluetoothLeService.MSG_SETTINGS_CHANGED;
        try {
            serviceConnection.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendAlarmUpdated(int alarmId) {
        Message msg = Message.obtain();
        msg.what = BluetoothLeService.MSG_ALARMS_CHANGED;
        msg.arg1 = alarmId;
        try {
            serviceConnection.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendUserParamsUpdated() {
        Message msg = Message.obtain();
        msg.what = BluetoothLeService.MSG_USER_PARAMS_CHANGED;
        try {
            serviceConnection.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



 public void disconnect() {
     Message msg = Message.obtain();
     msg.what = BluetoothLeService.MSG_DISCONNECT;
     try {
         serviceConnection.send(msg);
     } catch (RemoteException e) {
         e.printStackTrace();
     }
 }

    public void bind() {
        if (serviceConnection == null) {
            Intent intent = new Intent("de.chirtz.armband.i5lib.BluetoothLeService");
            intent.setPackage("de.chirtz.armband");
            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public byte getConnectionState() {
        return connectionState;
    }


    public void unbind() {
        if (serviceConnection != null)
            try {
                context.unbindService(mConnection);
            } catch (IllegalArgumentException ignored) { /* Service not registered */ }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (serviceConnection == null)
                serviceConnection = new Messenger(service);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "Service disconnected");
        }
    };

    public void unregisterReceiver() {
        Log.d(TAG, "unregistered receiver");
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        //unregisterReceiver(this);
    }

    private void registerReceiver() {
        Log.d(TAG, "registered receiver");
        LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
        //registerReceiver(this, filter);
    }


    private void sendConnectionState() {
        switch(connectionState) {
            case STATE_CONNECTED:
                for (ConnectionStateChangedListener l: listeners) {
                    l.onConnected();
                }
                break;
            case STATE_DISCONNECTED:
                for (ConnectionStateChangedListener l: listeners) {
                    l.onDisconnected();
                }
                break;
            case STATE_LOST:
                for (ConnectionStateChangedListener l: listeners) {
                    l.onConnectionLost();
                }
                break;
            case STATE_CONNECTING:
            case STATE_DISCONNECTING:
                for (ConnectionStateChangedListener l: listeners) {
                    l.onConnectionChanging(connectionState);
                }
                break;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch(intent.getAction()) {
            case DEVICE_CONNECTION:
                connectionState = intent.getByteExtra(BluetoothLeService.I_CONNECTION_STATE, (byte) -1);
                sendConnectionState();
                break;
            case DEVICE_ANSWER:
                if (connectionState == STATE_DISCONNECTED) {
                    connectionState = STATE_CONNECTED;
                    sendConnectionState();
                }
        }
    }
}
