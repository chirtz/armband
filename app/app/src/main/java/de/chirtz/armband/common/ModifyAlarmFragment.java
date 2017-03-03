package de.chirtz.armband.common;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import java.util.Arrays;

import de.chirtz.armband.Alarm;
import de.chirtz.armband.AlarmFragment;
import de.chirtz.armband.R;


public class ModifyAlarmFragment extends DialogFragment implements ModifyAlarmFragmentListener {

    public final static String TAG = "ModifyAlarmFragment";
    public final static String ARG_HOUR = "arg_hour";
    public final static String ARG_MINUTE = "arg_minute";
    public final static String ARG_WEEKDAYS = "arg_weekday";
    private final static String ARG_POSITION = "arg_position";
    private byte weekDays;
    private int hour;
    private int minutes;
    private int position;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modify_alarm, container, false);

        final TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);

        final PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        Button saveButton = (Button) view.findViewById(R.id.saveButton);

        Bundle args = getArguments();
        if (args != null) {
            weekDays = args.getByte(ARG_WEEKDAYS);
            minutes = args.getInt(ARG_MINUTE);
            hour = args.getInt(ARG_HOUR);
            position = args.getInt(ARG_POSITION);
        } else {
            position = -1;
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkValues()) {
                    getActivity().getSupportFragmentManager().beginTransaction().remove(ModifyAlarmFragment.this).commit();
                    ((AlarmFragment) getTargetFragment()).onModifyAlarmResult(position, weekDays, hour, minutes);
                }
            }
        });

        return view;
    }

    private boolean checkValues() {
        if (weekDays == -128) {
            DialogFragment frag = InfoDialogFragment.newInstance(getString(R.string.error), getString(R.string.error_day_selection));
            frag.show(getChildFragmentManager(), InfoDialogFragment.TAG);
            return false;
        }

        return true;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    public static DialogFragment newInstance() {
        ModifyAlarmFragment f = new ModifyAlarmFragment();
        return f;
    }

    public static DialogFragment newInstance(int position, Alarm a) {
        ModifyAlarmFragment f = new ModifyAlarmFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_HOUR, a.getHour());
        args.putInt(ARG_MINUTE, a.getMinute());
        args.putByte(ARG_WEEKDAYS, a.getWeekDays());
        args.putInt(ARG_POSITION, position);
        f.setArguments(args);
        return f;
    }



    @Override
    public void onTimeChanged(int hour, int minute) {
        Log.d(TAG, "Time changed: " + hour + " " + minute);
        this.hour = hour;
        this.minutes = minute;
    }

    @Override
    public void onDaysChanged(byte days) {
        Log.d(TAG, ""+ Arrays.toString(Tools.byteToWeekDays(days)));
        this.weekDays = days;
    }


    private class PagerAdapter extends FragmentStatePagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return TimeFragment.newInstance(getArguments());
                case 1:
                    return DayFragment.newInstance(getArguments());
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Time";
                case 1:
                    return "Day";
                default:
                    return null;
            }
        }
    }

}
