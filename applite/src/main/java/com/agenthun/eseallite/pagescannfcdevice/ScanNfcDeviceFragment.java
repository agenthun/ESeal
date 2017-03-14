package com.agenthun.eseallite.pagescannfcdevice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.pagescannfcdevice.view.NfcDeviceAdapter;
import com.agenthun.eseallite.pagefreighttrackmap.FreightTrackMapActivity;
import com.agenthun.eseallite.utils.DeviceSearchSuggestion;
import com.agenthun.eseallite.utils.PreferencesHelper;
import com.agenthun.eseallite.pagescannfcdevice.view.ScrollChildSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/14 16:48.
 */

public class ScanNfcDeviceFragment extends Fragment implements ScanNfcDeviceContract.View {
    private static final String TAG = "ScanNfcDeviceFragment";
    private NfcDeviceAdapter mAdapter;

    RecyclerView mRecyclerView;
    View mNoDevicesView;
    ScrollChildSwipeRefreshLayout swipeRefreshLayout;

    private ScanNfcDeviceContract.Presenter mPresenter;

    public static ScanNfcDeviceFragment newInstance() {

        Bundle args = new Bundle();

        ScanNfcDeviceFragment fragment = new ScanNfcDeviceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_nfc_device, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mNoDevicesView = view.findViewById(R.id.noDevices);

        swipeRefreshLayout = (ScrollChildSwipeRefreshLayout) view.findViewById(R.id.refresh_layout);

        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccentDark)
        );
        swipeRefreshLayout.setScrollUpChild(mRecyclerView);
        swipeRefreshLayout.setOnRefreshListener(() -> mPresenter.loadDevices(true, PreferencesHelper.getTOKEN(getActivity())));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupDeviceList(mRecyclerView);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public void setPresenter(ScanNfcDeviceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void setupDeviceList(RecyclerView recyclerView) {
        mAdapter = new NfcDeviceAdapter(new ArrayList<DeviceSearchSuggestion>(0));
        mAdapter.setOnItemClickListener(deviceSearchSuggestion -> showDeviceDetailsUi(deviceSearchSuggestion));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(active));
    }

    @Override
    public void showDevices(List<DeviceSearchSuggestion> devices) {
        mAdapter.updateAllDatas(devices);

        mRecyclerView.setVisibility(View.VISIBLE);
        mNoDevicesView.setVisibility(View.GONE);
    }

    @Override
    public void showLoadingDevicesError() {
        showMessage(getString(R.string.error_query_device_group));
    }

    @Override
    public void showNoDevices() {
        mRecyclerView.setVisibility(View.GONE);
        mNoDevicesView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showDeviceDetailsUi(DeviceSearchSuggestion deviceSearchSuggestion) {
        FreightTrackMapActivity.start(getContext(), deviceSearchSuggestion);
        ActivityCompat.finishAfterTransition(getActivity());
    }

    @Override
    public void showDeviceDetailsUi(String id, Integer type, String name) {
        FreightTrackMapActivity.start(getContext(), id, type, name);
        ActivityCompat.finishAfterTransition(getActivity());
    }

    private void showMessage(String message) {
        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text)))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
        snackbar.show();
    }
}
