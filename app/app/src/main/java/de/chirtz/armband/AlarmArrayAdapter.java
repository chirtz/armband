package de.chirtz.armband;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import de.chirtz.armband.common.Tools;

class AlarmArrayAdapter extends ArrayAdapter<Alarm> {

        private final AlarmFragment frag;
        private final static String TAG = "AlarmArrayAdapter";
        private final String[] dayNames;

        public AlarmArrayAdapter(Context context, AlarmFragment frag, ArrayList<Alarm> values) {
            super(context, -1, values);
            this.frag = frag;
            this.dayNames = context.getResources().getStringArray(R.array.week_days);
        }

        private String getDayNamesFromByte(byte weekDays) {
            StringBuilder sb = new StringBuilder();
            boolean[] bits = Tools.byteToWeekDays(weekDays);
            boolean allTrue = true;
            for (int i=0; i< bits.length; i++) {
                if (bits[i]) {
                    sb.append(dayNames[i]);
                    sb.append(", ");
                } else
                    allTrue = false;
            }
            if (allTrue)
                return getContext().getString(R.string.every_day);
            sb.delete(sb.length()-2, sb.length()-1);
            return sb.toString();
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.alarm_item, parent, false);


                TextView textViewTitle = (TextView) rowView.findViewById(R.id.textViewTitle);
                TextView textViewDescription = (TextView) rowView.findViewById(R.id.textViewDescription);
                Switch switchEnable = (Switch) rowView.findViewById(R.id.switchEnable);


            final Alarm item = getItem(position);
            assert item != null;
            textViewTitle.setText(getDayNamesFromByte(item.getWeekDays()));
            textViewDescription.setText(item.getTimeString());
            switchEnable.setChecked(item.isEnabled());
            switchEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    item.setEnabled(b);
                    frag.saveAlarm(item);
                }
            });
            return rowView;
        }
}

