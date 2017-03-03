package de.chirtz.armband.filter.filter_properties;

import android.view.LayoutInflater;

public interface FilterPropertyInterface {
    FilterPropertyType getType();
    String getStateDescription();
    void initialize(LayoutInflater inflater);
    boolean apply();
}
