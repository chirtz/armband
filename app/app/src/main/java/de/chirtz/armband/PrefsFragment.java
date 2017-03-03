package de.chirtz.armband;


import android.content.SharedPreferences;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import de.chirtz.armband.common.NumberPreference;
import de.chirtz.armband.common.NumberPreferenceDialogFragmentCompat;
import de.chirtz.armband.i5lib.ServiceConnector;

public class PrefsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final static String TAG = "PrefsFragment";
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        setSummaries();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    public static Fragment newInstance() {
        PrefsFragment fragment = new PrefsFragment();
        //Bundle args = new Bundle();
        //fragment.setArguments(args);
        Log.d(TAG, "New Prefs Fragment instance");
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.startsWith("user_")) {
            ((MainActivity)getActivity()).getServiceConnector().sendUserParamsUpdated();
            setSummaries();
        }
        else
            ((MainActivity)getActivity()).getServiceConnector().sendBandSettingsUpdated();
    }

    private void setSummaries() {
        findPreference(getString(R.string.USER_PREFERENCE_AGE)).setSummary(getString(R.string.user_preference_age_summary) + " (" + prefs.getInt(getString(R.string.USER_PREFERENCE_AGE), 0) + ")");
        findPreference(getString(R.string.USER_PREFERENCE_WEIGHT)).setSummary(getString(R.string.user_preference_weight_summary) + " (" + prefs.getInt(getString(R.string.USER_PREFERENCE_WEIGHT), 0) + ")");
        findPreference(getString(R.string.USER_PREFERENCE_HEIGHT)).setSummary(getString(R.string.user_preference_height_summary) + " (" + prefs.getInt(getString(R.string.USER_PREFERENCE_HEIGHT), 0) + ")");
        findPreference(getString(R.string.USER_PREFERENCE_GENDER)).setSummary(getString(R.string.user_preference_gender_summary) + " (" + prefs.getString(getString(R.string.USER_PREFERENCE_GENDER), "") + ")");
        findPreference(getString(R.string.USER_PREFERENCE_STEP_GOAL)).setSummary(getString(R.string.user_preference_step_goal_summary) + " (" + prefs.getInt(getString(R.string.USER_PREFERENCE_STEP_GOAL), 0) + ")");
        findPreference(getString(R.string.PREFERENCE_DEVICE_ID)).setSummary(getString(R.string.preference_device_id_summary) + " (" + prefs.getInt(getString(R.string.PREFERENCE_DEVICE_ID), 0) + ")");
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof NumberPreference) {
            // Create a new instance of TimePreferenceDialogFragment with the key of the related
            // Preference
            dialogFragment = NumberPreferenceDialogFragmentCompat
                    .newInstance(preference.getKey());
        }

        // If it was one of our cutom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference" +
                            ".PreferenceFragment.DIALOG");
        }
        // Could not be handled here. Try with the super method.
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}
