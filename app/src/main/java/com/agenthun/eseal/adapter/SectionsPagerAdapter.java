package com.agenthun.eseal.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.activity.MainActivity;
import com.agenthun.eseal.fragment.FreightTrackFragment;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/7 上午5:25.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return FreightTrackFragment.newInstance("0", "1070");
            case 1:
                return MainActivity.PlaceholderFragment.newInstance(1);
            case 2:
                return MainActivity.PlaceholderFragment.newInstance(2);
        }
        return null;
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return App.getContext().getString(R.string.page_title_freight_track_query);
            case 1:
                return App.getContext().getString(R.string.page_title_scan_device);
            case 2:
                return App.getContext().getString(R.string.page_title_freight_query);
        }
        return null;
    }
}
