package de.chirtz.armband.i5lib;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class DeviceConnector {
    private final static String TAG = "DeviceConnector";
    private final ScanSettings settings;
    private final List<ScanFilter> filters = new ArrayList<>(1);
    private Timer t;
    private final BluetoothLeScanner scanner;
    private final static byte STATE_IDLE = 0;
    private final static byte STATE_SCANNING = 1;
    private boolean deviceFound = false;
    private static final long SCAN_PERIOD_LONG = 10000;
    private static final long SCAN_PERIOD = 4000;
    private final BluetoothGattCallback mGattCallback;
    private final BluetoothLeService service;
    private final static String NAME_PREFIX = "Braceli5-";

    public interface DeviceConnectorCallback {
        void onDeviceConnected(BluetoothGatt gatt);
    }


    public DeviceConnector(String deviceId, BluetoothGattCallback callback, BluetoothLeService service) {
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String deviceId1 = deviceId;
        ScanFilter s = new ScanFilter.Builder().setDeviceName(NAME_PREFIX+deviceId).build();
        filters.add(s);
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        mGattCallback = callback;
        this.service = service;
    }


    public void connect() {
        deviceFound = false;
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!deviceFound) {
                    scanner.stopScan(mScanCallback);
                    scanner.startScan(filters, settings, mScanCallback);
                }
            }
        }, SCAN_PERIOD, SCAN_PERIOD_LONG);
        Log.d(TAG, "Start Scan with retries");
        scanner.startScan(filters, settings, mScanCallback);
    }

    public void stopScanning() {
        scanner.stopScan(mScanCallback);
        t.cancel();
    }


    private void connectToDevice(BluetoothDevice device) {
        deviceFound = true;
        stopScanning();
        scanner.stopScan(mScanCallback);
        BluetoothGatt mGatt = device.connectGatt(service, false, mGattCallback);
        service.onDeviceConnected(mGatt);
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice btDevice = result.getDevice();
            connectToDevice(btDevice);
        }

        public void onScanFailed(int errorCode) {
            Log.d(TAG, "Scan failed: " + errorCode);
        }
    };
}
