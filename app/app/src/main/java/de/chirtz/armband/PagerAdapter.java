package de.chirtz.armband;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import de.chirtz.armband.filter.FilterFragment;


class PagerAdapter extends FragmentPagerAdapter {

    private final static String TAG = "PagerAdapter";


        private final Context context;

        public PagerAdapter(Context context) {
            super(((AppCompatActivity) context).getSupportFragmentManager());
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment f = null;
            switch (position) {
                case 0:
                    f = DeviceFragment.newInstance();
                    break;
                case 1:
                    f = FilterFragment.newInstance(context);
                    break;
                case 2:
                    f = AlarmFragment.newInstance();
                    break;
                case 3:
                    f = PrefsFragment.newInstance();
                    break;
            }
            return f;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return context.getString(R.string.tab_device);
                case 1:
                    return context.getString(R.string.tab_filters);
                case 2:
                    return context.getString(R.string.tab_alarms);
                case 3:
                    return context.getString(R.string.tab_preferences);
            }
            return null;
        }

}
