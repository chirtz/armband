package de.chirtz.armband.common;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.EditText;

import de.chirtz.armband.R;


public class NumberPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    private EditText editText;

    public static NumberPreferenceDialogFragmentCompat  newInstance(String key) {
        final NumberPreferenceDialogFragmentCompat
                fragment = new NumberPreferenceDialogFragmentCompat ();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }


    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        editText = (EditText) view.findViewById(R.id.edit_text);

        // Exception when there is no TimePicker
        if (editText == null) {
            throw new IllegalStateException("ERRRR");
        }

        DialogPreference preference = getPreference();
        int number = 0;
        if (preference instanceof NumberPreference) {
            number = ((NumberPreference) preference).getNumber();
        }
        editText.setText(String.valueOf(number));
    }


    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            // Get the related Preference and save the value
            DialogPreference preference = getPreference();
            String text = editText.getText().toString();
            if (preference instanceof NumberPreference) {
                NumberPreference numberPreference = ((NumberPreference) preference);
                // This allows the client to ignore the user value.
                if (numberPreference.callChangeListener(text)) {
                    // Save the value
                    numberPreference.setNumber(Integer.parseInt(text));
                }
            }
        }
    }

}

