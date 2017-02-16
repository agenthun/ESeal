package com.agenthun.eseallite.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.activity.FreightTrackMapActivity;
import com.agenthun.eseallite.adapter.NfcDeviceAdapter;
import com.agenthun.eseallite.connectivity.manager.RetrofitManager2;
import com.agenthun.eseallite.connectivity.service.PathType;
import com.agenthun.eseallite.utils.DeviceSearchSuggestion;
import com.agenthun.eseallite.utils.PreferencesHelper;
import com.agenthun.eseallite.view.ScrollChildSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/14 16:48.
 */

public class ScanNfcDeviceFragment extends Fragment {
    private static final String TAG = "ScanNfcDeviceFragment";
    private NfcDeviceAdapter mAdapter;

    @Bind(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @Bind(R.id.noDevices)
    View mNoDevicesView;
    @Bind(R.id.refresh_layout)
    ScrollChildSwipeRefreshLayout swipeRefreshLayout;

    public static ScanNfcDeviceFragment newInstance() {
        return new ScanNfcDeviceFragment();
    }

    public ScanNfcDeviceFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_nfc_device, container, false);
        ButterKnife.bind(this, view);

        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.colorAccent),
                ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark)
        );
        swipeRefreshLayout.setScrollUpChild(mRecyclerView);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDevices(true, PreferencesHelper.getTOKEN(getActivity()));
            }
        });

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
        loadDevices(true, PreferencesHelper.getTOKEN(getActivity()));
    }

    private NfcDeviceAdapter.OnItemClickListener mOnItemClickListener = new NfcDeviceAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(DeviceSearchSuggestion deviceSearchSuggestion) {
            showDeviceDetailsUi(deviceSearchSuggestion);
        }
    };

    private void setupDeviceList(RecyclerView recyclerView) {
        mAdapter = new NfcDeviceAdapter(new ArrayList<DeviceSearchSuggestion>(0));
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        recyclerView.setAdapter(mAdapter);
    }

    private void setLoadingIndicator(final boolean active) {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(active);
            }
        });
    }

    private void showDevices(List<DeviceSearchSuggestion> devices) {
        mAdapter.updateAllDatas(devices);

        mRecyclerView.setVisibility(View.VISIBLE);
        mNoDevicesView.setVisibility(View.GONE);
    }

    private void showLoadingDevicesError() {
        showMessage(getString(R.string.error_query_device_group));
    }

    private void showNoDevices() {
        mRecyclerView.setVisibility(View.GONE);
        mNoDevicesView.setVisibility(View.VISIBLE);
    }

    private void showMessage(String message) {
        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text)))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
        snackbar.show();
    }

    private void loadDevices(final boolean isShowLoadingIndicator, final String token) {
        if (isShowLoadingIndicator) {
            setLoadingIndicator(true);
        }

        RetrofitManager2.builder(PathType.WEB_SERVICE_V2_TEST).getAllDeviceFreightListObservable(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<List<DeviceSearchSuggestion>>() {
                    @Override
                    public void onCompleted() {
                        if (isShowLoadingIndicator) {
                            setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        showLoadingDevicesError();
                        if (isShowLoadingIndicator) {
                            setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onNext(List<DeviceSearchSuggestion> deviceSearchSuggestions) {
                        processDevices(deviceSearchSuggestions);
                    }
                });
    }

    private void processDevices(List<DeviceSearchSuggestion> deviceSearchSuggestions) {
        if (deviceSearchSuggestions.isEmpty()) {
            showNoDevices();
        } else {
            showDevices(deviceSearchSuggestions);
        }
    }

    private void showDeviceDetailsUi(DeviceSearchSuggestion deviceSearchSuggestion) {
        FreightTrackMapActivity.start(getContext(), deviceSearchSuggestion);
        ActivityCompat.finishAfterTransition(getActivity());
    }

    private void showDeviceDetailsUi(String id, Integer type, String name) {
        FreightTrackMapActivity.start(getContext(), id, type, name);
        ActivityCompat.finishAfterTransition(getActivity());
    }
}
