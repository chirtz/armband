/***
 * Represents the properties for an effect that occurs when a filter matches, i.e. when all condition
 * properties apply.
 */
package de.chirtz.armband.filter.filter_properties;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import de.chirtz.armband.R;


public abstract class AbstractEffectProperty extends FilterProperty {

    AbstractEffectProperty(Context context, ViewGroup rootView, Bundle values) {
        super(context, ((ViewGroup) rootView.findViewById(R.id.filter_effects)), values);
    }

    AbstractEffectProperty(Bundle values) {
        super(values);
    }

    @Override
    public FilterPropertyType getType() {
        return FilterPropertyType.Effect;
    }

}
