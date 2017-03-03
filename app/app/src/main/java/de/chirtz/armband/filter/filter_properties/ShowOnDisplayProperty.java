/***
 * Defines what is shown on the display when a notification occurs.
 */
package de.chirtz.armband.filter.filter_properties;

import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import de.chirtz.armband.R;
import de.chirtz.armband.filter.ModifyFilterDialogFragment;


public class ShowOnDisplayProperty extends AbstractEffectProperty implements ModifyFilterDialogFragment.ActivityResultListener{

    private Fragment fragment;
    private final String[] displayTextSpinnerArray;

    public ShowOnDisplayProperty(Context context, ViewGroup rootView, Bundle values, Fragment frag) {
        super(context, rootView, values);
        this.fragment = frag;
        displayTextSpinnerArray = context.getResources().getStringArray(R.array.display_text_options);
    }

    public ShowOnDisplayProperty(Bundle data) {
        super(data);
        displayTextSpinnerArray = null;
    }

    @Override
    public String getStateDescription() {
        switch (getValue()) {
            case 0:
                return "display title";
            case 1:
                return "display content";
            case 2:
                return "display title+content";
            default:
                return null;
        }
    }


    @Override
    public void initialize(LayoutInflater inflater) {
        ((ModifyFilterDialogFragment)fragment).addActivityResultListener(this);
        View v =  inflater.inflate(R.layout.filter_property_show_on_display, rootView);
        Spinner showOnDisplaySpinner = (Spinner) v.findViewById(R.id.displaySpinner);

        final ArrayAdapter<String> displayTextSpinnerAdapter = new ArrayAdapter<>(context, R.layout.spinner_textview, displayTextSpinnerArray);
        displayTextSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        showOnDisplaySpinner.setAdapter(displayTextSpinnerAdapter);
        if (values.containsKey(FILTER_EFFECT_SHOW_ON_DISPLAY))
            showOnDisplaySpinner.setSelection(values.getInt(FILTER_EFFECT_SHOW_ON_DISPLAY, 0));

        showOnDisplaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                values.putInt(FILTER_EFFECT_SHOW_ON_DISPLAY, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    @Override
    public boolean apply() {
        return true;
    }


    public int getValue() {
        return values.getInt(FILTER_EFFECT_SHOW_ON_DISPLAY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public static void serialize(Bundle source, ContentValues target) {
        target.put(FILTER_EFFECT_SHOW_ON_DISPLAY, source.getInt(FILTER_EFFECT_SHOW_ON_DISPLAY));
    }

    public static void deserialize(Cursor source, Bundle target) {
        target.putInt(FILTER_EFFECT_SHOW_ON_DISPLAY, source.getInt(source.getColumnIndex(FILTER_EFFECT_SHOW_ON_DISPLAY)));
    }
}
