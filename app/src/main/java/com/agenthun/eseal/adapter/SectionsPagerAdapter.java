package com.agenthun.eseal.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.base.LocationDetail;
import com.agenthun.eseal.fragment.FreightTrackBaiduMapFragment;
import com.agenthun.eseal.fragment.NfcDeviceFragment;
import com.agenthun.eseal.fragment.NfcDeviceFragmentX;
import com.agenthun.eseal.fragment.ScanDeviceFragment;

import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/7 上午5:25.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "SectionsPagerAdapter";

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Fragment freightTrackFragment = FreightTrackBaiduMapFragment.newInstance();
                ((FreightTrackBaiduMapFragment) freightTrackFragment).setOnItemClickListener(new FreightTrackBaiduMapFragment.OnItemClickListener() {
                    @Override
                    public void onItemClick(String containerNo, String containerId, List<LocationDetail> details) {
                        Log.d(TAG, "get containerNo: " + containerNo + ", containerId: " + containerId);
                        if (mOnDataChangeListener != null) {
                            mOnDataChangeListener.onContainerDataChange(containerNo, containerId, details);
                        }
                    }
                });
                return freightTrackFragment;
            case 1:
                return ScanDeviceFragment.newInstance();
            case 2:
                return NfcDeviceFragmentX.newInstance();
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
                return App.getContext().getString(R.string.page_title_scan_ble_device);
            case 2:
                return App.getContext().getString(R.string.page_title_nfc_device);
        }
        return null;
    }

    //itemClick interface
    public interface OnDataChangeListener {
        void onContainerDataChange(String containerNo, String containerId, List<LocationDetail> details);
    }

    private OnDataChangeListener mOnDataChangeListener;

    public void setOnDataChangeListener(OnDataChangeListener mOnDataChangeListener) {
        this.mOnDataChangeListener = mOnDataChangeListener;
    }
}
