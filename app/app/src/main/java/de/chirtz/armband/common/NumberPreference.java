package de.chirtz.armband.common;

import android.content.Context;

import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import de.chirtz.armband.R;



public class NumberPreference extends DialogPreference {

    private int mNumber;

    public NumberPreference(Context context) {
        this(context, null);
    }
    public NumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs); // NOT this(context, attrs, 0), that makes the preference title too large
    }
    public NumberPreference(Context context, AttributeSet attrs,
                          int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }
    public NumberPreference(Context context, AttributeSet attrs,
                          int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }




    @Override
    public boolean shouldPersist() {
        return true;
    }

    @Override
    public void onClick() {
        getPreferenceManager().showDialog(this);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute. Fallback value is set to 0.
        return a.getInt(index, 0);
    }
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
                                     Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
        setNumber(restorePersistedValue ? getPersistedInt(mNumber) : (int) defaultValue);
    }

    public void setNumber(int number) {
        mNumber = number;
        persistInt(mNumber);
    }

    public int getNumber() {
        return mNumber;
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.number_preference_dialog;
    }

    @Override
    public String getPositiveButtonText() {
        return getContext().getString(R.string.positive_button);
    }

    @Override
    public String getNegativeButtonText() {
        return getContext().getString(R.string.negative_button);
    }


}
