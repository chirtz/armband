package de.chirtz.armband.filter.filter_properties;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ViewGroup;

import java.util.HashMap;

public abstract class FilterProperty implements FilterPropertyInterface {

    final Bundle values;
    final Context context;
    final ViewGroup rootView;

    public enum Type {
        App,
        AccumulateText,
        ContentFilter,
        MinPriority,
        People,
        ShowOnDisplay,
    }

    public final static String FILTER_CONDITION_PACKAGE = "package_name";
    public final static String FILTER_CONDITION_CONTENT_FILTER_ENABLED = "content_filter_enabled";
    public final static String FILTER_CONDITION_NOTIFICATION_PRIORITY = "filter_notification_priority";
    public final static String FILTER_CONDITION_TITLE = "filter_title";
    public final static String FILTER_CONDITION_CONTENT = "filter_content";
    public final static String FILTER_CONDITION_PEOPLE = "filter_people";

    public final static String FILTER_EFFECT_SHOW_ON_DISPLAY = "show_on_display";
    public final static String FILTER_EFFECT_ACCUMULATE_TEXT = "filter_accumulate_text";


    FilterProperty(Context context, ViewGroup rootView, Bundle values) {
        this.values = values;
        this.context = context;
        this.rootView = rootView;
    }

    FilterProperty(Bundle values) {
        this.values = values;
        this.context = null;
        this.rootView = null;
    }

    @SuppressWarnings("UnusedParameters")
    public static void serialize(Bundle source, ContentValues target) {
        throw new IllegalArgumentException("Serializer not implemented");
    }

    @SuppressWarnings("UnusedParameters")
    public static void deserialize(Cursor source, Bundle target) {
        throw new IllegalArgumentException("Serializer not implemented");
    }

    @Override
    public String toString() {
        return getStateDescription();
    }

    @Override
    public String getStateDescription() {
        return null;
    }

    @Override
    public boolean apply() {
        return true;
    }

    @SuppressWarnings("unchecked")
    public static <T extends FilterProperty> T getProperty(Bundle data, FilterProperty.Type p) {
        switch(p) {
            case App:
                return (T) new AppProperty(data);
            case AccumulateText:
                return (T) new AccumulateTextProperty(data);
            case ContentFilter:
                return (T) new ContentFilterProperty(data);
            case MinPriority:
                return (T) new MinPriorityProperty(data);
            case People:
                return (T) new PeopleProperty(data);
            case ShowOnDisplay:
                return (T) new ShowOnDisplayProperty(data);
        }
        return null;
    }

    public static HashMap<Type, FilterProperty> getProperties(Bundle data) {
        HashMap<Type, FilterProperty> props = new HashMap<>();
        for (Type t: Type.values()) {
            props.put(t, getProperty(data, t));
        }
        return props;
    }

}
