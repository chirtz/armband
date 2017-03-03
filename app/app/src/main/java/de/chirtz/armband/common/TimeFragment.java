package de.chirtz.armband.common;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import de.chirtz.armband.R;

import static de.chirtz.armband.common.ModifyAlarmFragment.ARG_HOUR;
import static de.chirtz.armband.common.ModifyAlarmFragment.ARG_MINUTE;

public class TimeFragment extends Fragment {

    public TimeFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Bundle args) {
        TimeFragment fragment = new TimeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_time, container, false);
        TimePicker timePicker = (TimePicker) v.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                ((ModifyAlarmFragment)getParentFragment()).onTimeChanged(hour, minute);
            }
        });
        if (getArguments() != null) {
            int hour = getArguments().getInt(ARG_HOUR);
            int minutes = getArguments().getInt(ARG_MINUTE);
            timePicker.setHour(hour);
            timePicker.setMinute(minutes);
        }
        return v;
    }
}
