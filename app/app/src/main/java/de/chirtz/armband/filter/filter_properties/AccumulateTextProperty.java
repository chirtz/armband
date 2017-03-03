/***
 * Accumulate Text when a notification is updated by the system, instead of overwriting the existing text.
 */
package de.chirtz.armband.filter.filter_properties;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import de.chirtz.armband.R;


public class AccumulateTextProperty extends AbstractEffectProperty {
    public AccumulateTextProperty(Context context, ViewGroup rootView, Bundle values) {
        super(context, rootView, values);
    }

    public AccumulateTextProperty(Bundle data) {
        super(data);
    }

    @Override
    public String getStateDescription() {
        return getValue() ? "accumulate" : null;
    }

    @Override
    public void initialize(LayoutInflater inflater) {
        View v =  inflater.inflate(R.layout.filter_property_accumulate_text, rootView);
        Switch accumulateSwitch = (Switch) v.findViewById(R.id.accumulateSwitch);
        if (values.containsKey(FILTER_EFFECT_ACCUMULATE_TEXT)) {
            accumulateSwitch.setChecked(values.getBoolean(FILTER_EFFECT_ACCUMULATE_TEXT, false));
        }

        accumulateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                values.putBoolean(FILTER_EFFECT_ACCUMULATE_TEXT, b);
            }
        });
    }

    public boolean getValue() {
        return values.getBoolean(FILTER_EFFECT_ACCUMULATE_TEXT);
    }

    public static void serialize(Bundle source, ContentValues target) {
        target.put(FILTER_EFFECT_ACCUMULATE_TEXT, source.getBoolean(FILTER_EFFECT_ACCUMULATE_TEXT) ? 1 : 0);
    }

    public static void deserialize(Cursor source, Bundle target) {
        target.putBoolean(FILTER_EFFECT_ACCUMULATE_TEXT, source.getInt(source.getColumnIndex(FILTER_EFFECT_ACCUMULATE_TEXT)) != 0);
    }
}
