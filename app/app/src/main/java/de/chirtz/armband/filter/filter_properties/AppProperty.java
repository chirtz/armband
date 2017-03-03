/***
 * Determines which applications are filtered
 */
package de.chirtz.armband.filter.filter_properties;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import de.chirtz.armband.common.InfoDialogFragment;
import de.chirtz.armband.R;
import de.chirtz.armband.filter.AppChooserDialogFragment;
import de.chirtz.armband.filter.Filter;
import de.chirtz.armband.filter.ModifyFilterDialogFragment;

public class AppProperty extends AbstractConditionProperty implements ModifyFilterDialogFragment.ActivityResultListener {

    private final static int REQUEST_APP_CHOOSER = 123;
    private Fragment fragment;
    private final static String TAG_APP_CHOOSER = "APP_CHOOSER";
    private Button selectAppButton;

    public AppProperty(Context context, ViewGroup rootView, Bundle values, Fragment frag) {
        super(context, rootView, values);
        this.fragment = frag;
    }

    public AppProperty(Bundle data) {
        super(data);
    }

    @Override
    public void initialize(LayoutInflater inflater) {
        ((ModifyFilterDialogFragment)fragment).addActivityResultListener(this);
        View v =  inflater.inflate(R.layout.filter_property_app, rootView);
        selectAppButton = (Button) v.findViewById(R.id.selectAppButton);
        selectAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAppChooser();
            }
        });

        if (values.containsKey(FILTER_CONDITION_PACKAGE))
            selectAppButton.setText(AppChooserDialogFragment.getAppNameForPackage(context, values.getString(FILTER_CONDITION_PACKAGE)));
        else
            selectAppButton.setText(context.getString(R.string.select_app));

        if (!(values.containsKey(Filter.FILTER_SYSTEM) && values.getBoolean(Filter.FILTER_SYSTEM, false))) {
            selectAppButton.setEnabled(true);
        } else {
            selectAppButton.setEnabled(false);
        }
        selectAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAppChooser();
            }
        });
    }

    private void showAppChooser() {
        DialogFragment appChooser = AppChooserDialogFragment.newInstance();
        appChooser.setTargetFragment(fragment, REQUEST_APP_CHOOSER);
        appChooser.show(fragment.getFragmentManager(), TAG_APP_CHOOSER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_APP_CHOOSER)
        if (resultCode == Activity.RESULT_OK) {
            values.putAll(data.getExtras());
            selectAppButton.setText(AppChooserDialogFragment.getAppNameForPackage(context, values.getString(FILTER_CONDITION_PACKAGE)));
        }
    }

    @Override
    public boolean apply() {
        if (!values.containsKey(FILTER_CONDITION_PACKAGE)) {
            InfoDialogFragment.newInstance(context.getString(R.string.error), context.getString(R.string.error_app_name_missing)).show(fragment.getFragmentManager(),
                    "ERR");
            return false;
        }
        return true;
    }


    public String getValue() {
        return values.getString(FILTER_CONDITION_PACKAGE);
    }

    public void setValue(String pkg) {
        values.putString(FILTER_CONDITION_PACKAGE, pkg);
    }

    public static void serialize(Bundle source, ContentValues target) {
        target.put(FILTER_CONDITION_PACKAGE, source.getString(FILTER_CONDITION_PACKAGE));
    }

    public static void deserialize(Cursor source, Bundle target) {
        target.putString(FILTER_CONDITION_PACKAGE, source.getString(source.getColumnIndex(FILTER_CONDITION_PACKAGE)));
    }
}
