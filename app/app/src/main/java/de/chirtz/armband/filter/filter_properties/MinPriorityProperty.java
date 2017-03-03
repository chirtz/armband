/***
 * Defines the minimal priority a notification needs to have in order to be matched by the filter.
 */
package de.chirtz.armband.filter.filter_properties;

import android.app.Notification;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;



import java.util.Collections;

import de.chirtz.armband.R;
import de.chirtz.armband.notifications.SBNotification;

public class MinPriorityProperty extends AbstractConditionProperty {

    public MinPriorityProperty(Context context, ViewGroup rootView, Bundle values) {
        super(context, rootView, values);
    }

    public MinPriorityProperty(Bundle data) {
        super(data);
    }

    @Override
    public String getStateDescription() {
        return "prio >= " + SBNotification.getPriorityString(getValue()).toLowerCase();
    }

    @Override
    public void initialize(LayoutInflater inflater) {
        View v =  inflater.inflate(R.layout.filter_property_min_priority, rootView);
        Spinner prioritySpinner = (Spinner) v.findViewById(R.id.prioritySpinner);
        final ArrayAdapter<String> prioritySpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, SBNotification.priorityMap.valueList());
        prioritySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(prioritySpinnerAdapter);

        if (values.containsKey(FILTER_CONDITION_NOTIFICATION_PRIORITY)) {
            int prio = values.getInt(FILTER_CONDITION_NOTIFICATION_PRIORITY, Notification.PRIORITY_LOW);
            int pos = Collections.binarySearch(SBNotification.priorityMap.keyList(), prio);
            prioritySpinner.setSelection(Math.max(0, pos));
        } else {
            prioritySpinner.setSelection(1); // Low priority when no value is set
        }

        prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                values.putInt(FILTER_CONDITION_NOTIFICATION_PRIORITY, SBNotification.priorityMap.getKeyForValue(prioritySpinnerAdapter.getItem(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public int getValue() {
        return values.getInt(FILTER_CONDITION_NOTIFICATION_PRIORITY);
    }

    public void setValue(int priority) {
        values.putInt(FILTER_CONDITION_NOTIFICATION_PRIORITY, priority);
    }

    public static void serialize(Bundle source, ContentValues target) {
        target.put(FILTER_CONDITION_NOTIFICATION_PRIORITY, source.getInt(FILTER_CONDITION_NOTIFICATION_PRIORITY));
    }

    public static void deserialize(Cursor source, Bundle target) {
        target.putInt(FILTER_CONDITION_NOTIFICATION_PRIORITY, source.getInt(source.getColumnIndex(FILTER_CONDITION_NOTIFICATION_PRIORITY)));
    }


}
