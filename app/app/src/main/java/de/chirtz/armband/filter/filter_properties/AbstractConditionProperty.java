/***
 * Represents the properties for a condition that needs to be fulfilled for the filter to be effective.
 */
package de.chirtz.armband.filter.filter_properties;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import de.chirtz.armband.R;


public abstract class AbstractConditionProperty extends FilterProperty {

    AbstractConditionProperty(Context context, ViewGroup rootView, Bundle values) {
        super(context, ((ViewGroup) rootView.findViewById(R.id.filter_conditions)), values);
    }

    AbstractConditionProperty(Bundle values) {
        super(values);
    }

    @Override
    public FilterPropertyType getType() {
        return FilterPropertyType.Condition;
    }

}
