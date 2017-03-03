package de.chirtz.armband;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;

import de.chirtz.armband.common.ModifyAlarmFragment;
import de.chirtz.armband.i5lib.ServiceConnector;

import static de.chirtz.armband.i5lib.BluetoothLeService.ALARM_PREFIX;
import static de.chirtz.armband.i5lib.BluetoothLeService.MAX_ALARMS;


public class AlarmFragment extends ListFragment {
    private final static String TAG = "AlarmFragment";

    private final static int REQUEST_ADD_ALARM = 1;
    private final static int REQUEST_MODIFY_ALARM = 2;
    private HashSet<Integer> alarmIds;
    private SharedPreferences prefs;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.floating_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (alarmIds.size() == MAX_ALARMS) {
                    Snackbar.make(view, R.string.max_alarms_error, Snackbar.LENGTH_LONG).show();
                    //Toast.makeText(getContext(), R.string.max_alarms_error, Toast.LENGTH_LONG).show();

                } else
                    showModifyAlarmDialog(-1);
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        alarmIds = new HashSet<>();
        ArrayList<Alarm> alarms = new ArrayList<>();
        for (int i=0; i<MAX_ALARMS; i++) {
            if (prefs.contains(ALARM_PREFIX + i)) {
                String alarmString = prefs.getString(ALARM_PREFIX+i, "");
                if (!alarmString.equals("")) {
                    alarms.add(Alarm.fromString(i, alarmString));
                    alarmIds.add(i);
                }
            }
        }
        AlarmArrayAdapter adapter = new AlarmArrayAdapter(getContext(), this, alarms);
        setListAdapter(adapter);
    }

    public static Fragment newInstance() {
        Log.d(TAG, "New Alarm Fragment instance");
        AlarmFragment fragment = new AlarmFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        registerForContextMenu(this.getListView());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.alarm_context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete_alarm:
                Alarm a = (Alarm) getListAdapter().getItem(info.position);
                deleteAlarm(a);
                ((AlarmArrayAdapter) getListAdapter()).remove(a);
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    private void showModifyAlarmDialog(int position) {
        DialogFragment fragment;
        if (position < 0) {
            fragment = ModifyAlarmFragment.newInstance();
            fragment.setTargetFragment(AlarmFragment.this, REQUEST_ADD_ALARM);
        } else {
            fragment = ModifyAlarmFragment.newInstance(position, (Alarm) getListAdapter().getItem(position));
            fragment.setTargetFragment(this, REQUEST_MODIFY_ALARM);
        }
        getFragmentManager().beginTransaction().show(fragment).commit();
        fragment.show(getFragmentManager(), ModifyAlarmFragment.TAG);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        showModifyAlarmDialog(position);
    }

    private int findFreeAlarmID() {
        for (int i=0; i<MAX_ALARMS; i++) {
            if (!alarmIds.contains(i))
                return i;
        }
        return -1;
    }

    public void saveAlarm(Alarm a) {
        Log.d(TAG, "alarm " + a.getId() + " saved");
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ALARM_PREFIX + a.getId(), a.toString());
        editor.commit();
        alarmIds.add(a.getId());
        ServiceConnector connector = ((MainActivity) getActivity()).getServiceConnector();
        connector.sendAlarmUpdated(a.getId());
    }

    private void deleteAlarm(Alarm a) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(ALARM_PREFIX + a.getId());
        editor.commit();
        alarmIds.remove(a.getId());
        ServiceConnector connector = ((MainActivity) getActivity()).getServiceConnector();
        connector.sendAlarmUpdated(a.getId());
    }

    public void onModifyAlarmResult(int position, byte weekDay, int hour, int minute) {
        if (position >= 0) {
            Alarm a = (Alarm) getListAdapter().getItem(position);
            a.setData(weekDay, hour, minute);
            ((ArrayAdapter) getListAdapter()).notifyDataSetChanged();
            saveAlarm(a);
        } else {
            int id = findFreeAlarmID();
            if (id >= 0) {
                Alarm a = new Alarm(id, weekDay, hour, minute, true);
                ((ArrayAdapter) getListAdapter()).add(a);
                saveAlarm(a);

            } else {
                Log.d(TAG, "All alarms occupied");
            }
        }
    }
}
