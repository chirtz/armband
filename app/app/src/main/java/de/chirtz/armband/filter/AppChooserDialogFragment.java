package de.chirtz.armband.filter;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;


import java.util.Collections;
import java.util.List;

import de.chirtz.armband.R;
import de.chirtz.armband.filter.filter_properties.FilterProperty;

public class AppChooserDialogFragment extends DialogFragment implements AbsListView.OnItemClickListener {

    private List<ResolveInfo> apps;
    private AbsListView listView;
    private View progressBar;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AppChooserFragment.
     */
    public static AppChooserDialogFragment newInstance() {
        return new AppChooserDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_chooser, container, false);
        listView = (AbsListView) view.findViewById(R.id.app_list);
        listView.setOnItemClickListener(this);
        progressBar = view.findViewById(R.id.progress_bar);
        new LoadAppListTask().execute();
        return view;
    }

    private class LoadAppListTask extends AsyncTask<Void, Void, List<ResolveInfo>> {
        @Override
        protected List<ResolveInfo> doInBackground(Void... params) {
            PackageManager pm = getActivity().getPackageManager();
            Intent main = new Intent(Intent.ACTION_MAIN, null);
            main.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> apps = pm.queryIntentActivities(main, 0);
            Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm));
            apps.add(0, new AllAppsResolveInfo(getActivity()));
            return apps;
        }

        @Override
        protected void onPostExecute(List<ResolveInfo> result) {
            apps = result;
            AppAdapter adapter = new AppAdapter(getActivity(), getActivity().getPackageManager(), apps);
            listView.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
            progressBar = null;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        ResolveInfo info = apps.get(position);
        if (info instanceof AllAppsResolveInfo) {
            intent.putExtra(FilterProperty.FILTER_CONDITION_PACKAGE, AllAppsResolveInfo.ALL_APPS);
        } else {
            intent.putExtra(FilterProperty.FILTER_CONDITION_PACKAGE, info.activityInfo.packageName);
        }
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);
        getDialog().dismiss();
    }

    public static String getAppNameForPackage(Context context, String pkgName) {
        if (pkgName.equals(AllAppsResolveInfo.ALL_APPS))
            return AllAppsResolveInfo.title;
        PackageManager pm = context.getApplicationContext().getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(pkgName, 0);
            return pm.getApplicationLabel(ai).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }


}
