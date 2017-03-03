package de.chirtz.armband.common;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import de.chirtz.armband.R;

import static de.chirtz.armband.common.ModifyAlarmFragment.ARG_WEEKDAYS;


public class DayFragment extends Fragment {

    public DayFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(Bundle args) {
        DayFragment fragment = new DayFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_day, container, false);
        final ListView listView = (ListView) v.findViewById(R.id.dayList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setItemsCanFocus(false);

        listView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_multiple_choice, getResources().getStringArray(R.array.week_days)));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((ModifyAlarmFragmentListener)getParentFragment()).onDaysChanged(Tools.weekDaysToByte(listView.getCheckedItemPositions()));
            }
        });

        if (getArguments() != null) {
            byte weekDays = getArguments().getByte(ARG_WEEKDAYS);
            boolean[] days = Tools.byteToWeekDays(weekDays);
            for (int i=0; i<7; i++) {
                listView.setItemChecked(i, days[i]);
            }
        }
        return v;
    }
}
