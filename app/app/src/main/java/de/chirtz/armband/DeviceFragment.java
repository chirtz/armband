package de.chirtz.armband;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.chirtz.armband.i5lib.Band;
import de.chirtz.armband.i5lib.BluetoothLeService;
import de.chirtz.armband.i5lib.DeviceInfo;

import static de.chirtz.armband.i5lib.BluetoothLeService.DEVICE_ANSWER;
import static de.chirtz.armband.i5lib.BluetoothLeService.DEVICE_CONNECTION;


public class DeviceFragment extends Fragment {

    private static final String TAG = "DeviceFragment";
    private TextView textViewPower, textViewFirmware, textViewAddress, textViewModel, textViewName;
    private Band smartWatch;
    private long lastTimeChecked;
    private final Handler handler = new Handler();
    private static final String DEVICE_DATA = "device_data";
    private DeviceInfo deviceInfo;
    private int devPower;
    private IntentFilter filter;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(
                R.layout.fragment_device, container, false);
        //Bundle args = getArguments();
        filter = new IntentFilter();
        filter.addAction(DEVICE_ANSWER);
        filter.addAction(DEVICE_CONNECTION);
        smartWatch = new Band(LocalBroadcastManager.getInstance(getContext()));

        textViewPower = (TextView) rootView.findViewById(R.id.textViewPower);
        textViewFirmware = (TextView) rootView.findViewById(R.id.textViewFirmware);
        textViewAddress = (TextView) rootView.findViewById(R.id.textViewAddress);
        textViewModel = (TextView) rootView.findViewById(R.id.textViewDeviceModel);
        textViewName = (TextView) rootView.findViewById(R.id.textViewDeviceName);
        FloatingActionButton fabRefresh = (FloatingActionButton) rootView.findViewById(R.id.fabRefresh);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getInfo(true);
            }
        });
        Button testButton = (Button) rootView.findViewById(R.id.buttonTest);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Band smartWatch = new Band(LocalBroadcastManager.getInstance(getContext()));
                smartWatch.sendMessage("Test ;)");
                //smartWatch.subscribeForSportUpdates();
                //smartWatch.requestDayData();
                //smartWatch.subscribeForSportUpdates();
                //smartWatch.requestDayData();
                //smartWatch.subscribeForLocalSport();
                //smartWatch.supportSports();
                //smartWatch.requestDayData();
                //smartWatch.subscribeForSportUpdates();
                //smartWatch.unBind();
            }
        });
        loadData();
        return rootView;
    }


    public static Fragment newInstance() {
        DeviceFragment fragment = new DeviceFragment();
        //Bundle args = new Bundle();
        //fragment.setArguments(args);
        Log.d(TAG, "New Device Fragment instance");
        return fragment;
    }

    private long calculateTimeDifference(long timeA, long timeB) {
        return ((timeB-timeA)/1000)/60;
    }

    private void getInfo(boolean force) {
        long now = System.currentTimeMillis();
        if (force || (calculateTimeDifference(lastTimeChecked, now) > 1)) {
                    smartWatch.requestPower();
                    smartWatch.requestVersionInfo();
                    smartWatch.setDate();
            lastTimeChecked = System.currentTimeMillis();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(deviceAnswerReceiver, filter);
        getInfo(false);
    }

    private void saveData() {
        if (deviceInfo != null) {
            SharedPreferences settings = getContext().getSharedPreferences(DEVICE_DATA, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("name", deviceInfo.getName());
            editor.putString("address", deviceInfo.getBleAddr());
            editor.putString("firmware", deviceInfo.getSwversion());
            editor.putString("model", deviceInfo.getModel());
            editor.putInt("oadmode", deviceInfo.getOadmode());
            editor.putInt("fontwidth", deviceInfo.getDisplayWidthFont());
            editor.putInt("battery", devPower);
            // Commit the edits!
            editor.apply();
        }
    }

    private void loadData() {
        SharedPreferences settings = getContext().getSharedPreferences(DEVICE_DATA, 0);
        deviceInfo = new DeviceInfo(
                settings.getString("model", "n.a."),
                settings.getInt("oadmode", 0),
                settings.getString("address", "n.a."),
                settings.getString("firmware", "n.a."),
                settings.getInt("fontwidth", 0),
                settings.getString("name", "n.a."));
        devPower = settings.getInt("battery", 0);
        updateTextViews();
    }

    @SuppressLint("SetTextI18n")
    private void updateTextViews() {
        textViewFirmware.setText(deviceInfo.getSwversion());
        textViewPower.setText(devPower+"%");
        textViewModel.setText(deviceInfo.getModel());
        textViewAddress.setText(deviceInfo.getBleAddr());
        textViewName.setText(deviceInfo.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveData();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        saveData();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(deviceAnswerReceiver);
    }

    private final BroadcastReceiver deviceAnswerReceiver = new BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case DEVICE_ANSWER:
                    switch (intent.getByteExtra(BluetoothLeService.DEVICE_ANSWER_TYPE, BluetoothLeService.DATA_DEVICE_UNKNOWN)) {
                        case BluetoothLeService.DATA_DEVICE_UNKNOWN:
                            Log.d(TAG, "Received broadcast unknown");
                            break;
                        case BluetoothLeService.DATA_DEVICE_POWER:
                            int power = intent.getIntExtra(BluetoothLeService.I_DEVICE_POWER, 0);
                            textViewPower.setText(power + "%");
                            devPower = power;
                            break;
                        case BluetoothLeService.DATA_DEVICE_INFO:
                            deviceInfo = intent.getParcelableExtra(BluetoothLeService.I_DEVICE_INFO);
                            break;
                    }
                    updateTextViews();
                    break;
                case DEVICE_CONNECTION:
                        if (intent.getByteExtra(BluetoothLeService.I_CONNECTION_STATE, (byte) -1) == BluetoothLeService.STATE_CONNECTED) {
                            Log.d(TAG, "Connected, getting info");
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getInfo(true);
                                }
                            }, 500);
                        }
                    break;
            }
        }
    };
}
