package de.chirtz.armband.filter;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;

import java.util.Map;

import de.chirtz.armband.common.Bundleable;
import de.chirtz.armband.filter.filter_properties.AccumulateTextProperty;
import de.chirtz.armband.filter.filter_properties.AppProperty;
import de.chirtz.armband.filter.filter_properties.ContentFilterProperty;
import de.chirtz.armband.filter.filter_properties.FilterProperty;
import de.chirtz.armband.filter.filter_properties.FilterPropertyType;
import de.chirtz.armband.filter.filter_properties.MinPriorityProperty;
import de.chirtz.armband.filter.filter_properties.PeopleProperty;
import de.chirtz.armband.filter.filter_properties.ShowOnDisplayProperty;

public class Filter extends Bundleable {

    public final static String FILTER_POSITION = "filter_position";
    public final static String FILTER_SYSTEM = "filter_system";
    public final static String FILTER_ENABLED = "filter_enabled";
    
    public Filter(Intent newData) {
        super(newData.getExtras());
    }

    private Filter(Bundle newData) {
        super(newData);
    }

    public boolean isEnabled() {
        // Filters enabled by default
        return !data.containsKey(FILTER_ENABLED) || data.getBoolean(FILTER_ENABLED);
    }

    public void setEnabled(boolean value) {
        data.putBoolean(FILTER_ENABLED, value);
    }


    public <T extends FilterProperty> T getProperty(FilterProperty.Type p) {
        return FilterProperty.getProperty(data, p);
    }

    public void setPosition(int position) {
        data.putInt(FILTER_POSITION, position);
    }

    public int getPosition() {
        return data.getInt(FILTER_POSITION, -1);
    }

    public void setSystemFilter(boolean value) {
        data.putBoolean(FILTER_SYSTEM, value);
    }

    public boolean isSystemFilter() {
        return data.containsKey(FILTER_SYSTEM) && data.getBoolean(FILTER_SYSTEM);
    }

    public void applyValues(Filter otherFilter) {
        data.putAll(otherFilter.data);
    }

    public ContentValues getSQLValues() {
        ContentValues values = new ContentValues();
        AppProperty.serialize(data, values);
        AccumulateTextProperty.serialize(data, values);
        MinPriorityProperty.serialize(data, values);
        ShowOnDisplayProperty.serialize(data, values);
        ContentFilterProperty.serialize(data, values);
        PeopleProperty.serialize(data, values);
        values.put(FILTER_POSITION, data.getInt(FILTER_POSITION, 0));
        values.put(FILTER_SYSTEM, isSystemFilter());
        values.put(FILTER_ENABLED, isEnabled());
        return values;
    }

    public static Filter fromCursor(Cursor cursor) {
        Bundle data = new Bundle();
        AppProperty.deserialize(cursor, data);
        AccumulateTextProperty.deserialize(cursor, data);
        MinPriorityProperty.deserialize(cursor, data);
        ShowOnDisplayProperty.deserialize(cursor, data);
        ContentFilterProperty.deserialize(cursor, data);
        PeopleProperty.deserialize(cursor, data);
        data.putInt(FILTER_POSITION, cursor.getInt(cursor.getColumnIndex(FILTER_POSITION)));
        data.putBoolean(FILTER_SYSTEM, cursor.getInt(cursor.getColumnIndex(FILTER_SYSTEM)) != 0);
        data.putBoolean(FILTER_ENABLED, cursor.getInt(cursor.getColumnIndex(FILTER_ENABLED)) != 0);
        return new Filter(data);
    }

    public Spanned getDescription() {
        StringBuilder sbConditions = new StringBuilder("<b>Conditions: </b>");
        Map<FilterProperty.Type, FilterProperty> map = FilterProperty.getProperties(data);
        for (FilterProperty prop : map.values()) {
            String desc = prop.getStateDescription();
            if (desc != null) {
                if (prop.getType().equals(FilterPropertyType.Condition)) {
                    sbConditions.append(desc);
                    sbConditions.append(", ");
                }
            }
        }
        sbConditions.deleteCharAt(sbConditions.lastIndexOf(","));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(sbConditions.toString(), Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(sbConditions.toString());
        }

    }

    @Override
    public String toString() {
        return getDescription().toString();
    }

}




