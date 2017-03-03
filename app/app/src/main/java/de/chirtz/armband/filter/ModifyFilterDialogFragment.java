package de.chirtz.armband.filter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.chirtz.armband.MainActivity;
import de.chirtz.armband.R;
import de.chirtz.armband.filter.filter_properties.AccumulateTextProperty;
import de.chirtz.armband.filter.filter_properties.AppProperty;
import de.chirtz.armband.filter.filter_properties.ContentFilterProperty;
import de.chirtz.armband.filter.filter_properties.FilterProperty;
import de.chirtz.armband.filter.filter_properties.MinPriorityProperty;
import de.chirtz.armband.filter.filter_properties.PeopleProperty;
import de.chirtz.armband.filter.filter_properties.ShowOnDisplayProperty;

public class ModifyFilterDialogFragment extends DialogFragment {


    public interface ActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    private List<FilterProperty> properties;
    public final static String TAG = "ModifyFilterDialogFrag";
    private final Bundle resultData = new Bundle();
    private String title = "";
    private Bundle previousData;
    private final LinkedList<ActivityResultListener> activityResultListeners = new LinkedList<>();

    static public ModifyFilterDialogFragment newInstance(Context context) {
        ModifyFilterDialogFragment f = new ModifyFilterDialogFragment();
        //f.resultData.putExtra(LEDColorProperty.FILTER_EFFECT_COLOR, LEDColorProperty.DEFAULT_COLOR);
        f.title = context.getString(R.string.add_filter);
        return f;
    }


    static public ModifyFilterDialogFragment newInstance(Context context, Filter filter) {
        ModifyFilterDialogFragment f = new ModifyFilterDialogFragment();
        f.importResults(filter);
        //f.resultData.putExtra(LEDColorProperty.FILTER_EFFECT_COLOR, LEDColorProperty.DEFAULT_COLOR);
        f.title = context.getString(R.string.add_filter);
        return f;
    }

    static public ModifyFilterDialogFragment newInstance(Context context, Filter filter, int position) {
        ModifyFilterDialogFragment f = new ModifyFilterDialogFragment();
        f.title = context.getString(R.string.change_filter);
        f.resultData.putAll(filter.getBundle());
        f.resultData.putInt(Filter.FILTER_POSITION, position);
        return f;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null)
        {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }


    private void saveFilter() {
        sendResults();
    }

    private void deleteFilter() {
        showFilterDeleteDialog();
    }


    private boolean bundleEquals(@NonNull Bundle a, @NonNull Bundle b) {
        for (String k: a.keySet()) {
            if (!b.containsKey(k)) return false;
            Object aO = a.get(k);
            Object bO = b.get(k);
            if (aO == null) {
                if (bO != null) return false;
            } else
                if (!aO.equals(bO)) return false;
        }
        return true;
    }

    private void sendResults() {
        Intent intent = new Intent();
        intent.putExtras(resultData);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
    }

    public void dismiss() {
        assignResults(true);
        if (!bundleEquals(resultData, previousData)) { // filter properties were changed
            AlertDialog.Builder alertBox = new AlertDialog.Builder(getActivity());
            alertBox.setTitle(getString(R.string.discard_filter));
            alertBox.setMessage(getString(R.string.discard_filter_msg));

            alertBox.setPositiveButton(getString(R.string.save),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            if (assignResults(false)) {
                                sendResults();
                                getFragmentManager().popBackStack();
                            }
                        }
                    });

            alertBox.setNeutralButton(getString(R.string.discard),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            getFragmentManager().popBackStack();
                        }
                    });

            alertBox.show();
        } else
            getFragmentManager().popBackStack();

    }

    private void showFilterDeleteDialog() {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(getActivity());
        alertBox.setTitle(getString(R.string.delete_filter));
        alertBox.setMessage(getString(R.string.delete_filter_msg));
        alertBox.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FilterDatabase db = new FilterDatabase(getActivity());
                int pos = resultData.getInt(Filter.FILTER_POSITION, -1);
                if (pos != -1)
                    db.deleteEntry(pos);
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null)
                        ((MainActivity) getActivity()).sendFiltersUpdatedBroadcast();
                getFragmentManager().popBackStack();
            }
        });
        alertBox.setNegativeButton(getString(R.string.keep), null);
        alertBox.show();
    }

    /***
     * Assigns values entered in the GUI to the resultData bundle
     */
    private boolean assignResults(boolean discard) {
        if (discard) return false;
        for (FilterProperty prop: properties) {
            if (!prop.apply())
                return false;
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_modify_filter, container, false);


        Button saveButton = (Button) view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFilter();
                getActivity().getSupportFragmentManager().beginTransaction().remove(ModifyFilterDialogFragment.this).commit();
            }
        });

        previousData = new Bundle();
        previousData.putAll(resultData);

        /***
         * Add further property constructors to this array, if required.
         */
        properties = Arrays.asList(
                // Conditions
                new AppProperty(getActivity(), view, resultData, this),
                new MinPriorityProperty(getActivity(), view, resultData),
                new PeopleProperty(getActivity(), view, resultData, this),
                new ContentFilterProperty(getActivity(), view, resultData),

                // Effects
                new ShowOnDisplayProperty(getActivity(), view, resultData, this),
                new AccumulateTextProperty(getActivity(), view, resultData)
        );

        for (FilterProperty prop: properties) {
                prop.initialize(inflater);
        }

        return view;
    }

    public void addActivityResultListener(ActivityResultListener listener) {
        activityResultListeners.add(listener);
    }

    private void importResults(Filter f) {
        this.resultData.putAll(f.getBundle());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (ActivityResultListener listener: activityResultListeners) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }



}
