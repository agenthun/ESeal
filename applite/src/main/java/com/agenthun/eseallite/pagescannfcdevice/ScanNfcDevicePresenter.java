package com.agenthun.eseallite.pagescannfcdevice;

import android.content.Context;

import com.agenthun.eseallite.connectivity.manager.RetrofitManager;
import com.agenthun.eseallite.connectivity.service.PathType;
import com.agenthun.eseallite.utils.DeviceSearchSuggestion;
import com.agenthun.eseallite.utils.PreferencesHelper;
import com.agenthun.eseallite.utils.scheduler.BaseSchedulerProvider;

import java.util.List;

import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/3/1 08:30.
 */

public class ScanNfcDevicePresenter implements ScanNfcDeviceContract.Presenter {
    private static final String TAG = "ScanNfcDevicePresenter";

    private Context mContext;

    private ScanNfcDeviceContract.View mView;

    private BaseSchedulerProvider mSchedulerProvider;

    private CompositeSubscription mSubscriptions;

    public ScanNfcDevicePresenter(Context context,
                                  ScanNfcDeviceContract.View view,
                                  BaseSchedulerProvider schedulerProvider) {
        mContext = context;
        mView = view;
        mSchedulerProvider = schedulerProvider;

        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        loadDevices(true, PreferencesHelper.getTOKEN(mContext));
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void loadDevices(boolean isShowLoadingIndicator, String token) {
        if (isShowLoadingIndicator) {
            mView.setLoadingIndicator(true);
        }

        mSubscriptions.add(RetrofitManager
                .builder(PathType.WEB_SERVICE_V2_TEST)
                .getBeidouMasterDeviceFreightListWithFormatObservable(token)
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<List<DeviceSearchSuggestion>>() {
                    @Override
                    public void onCompleted() {
                        if (isShowLoadingIndicator) {
                            mView.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        mView.showLoadingDevicesError();
                        if (isShowLoadingIndicator) {
                            mView.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onNext(List<DeviceSearchSuggestion> deviceSearchSuggestions) {
                        processDevices(deviceSearchSuggestions);
                    }
                }));
    }

    private void processDevices(List<DeviceSearchSuggestion> deviceSearchSuggestions) {
        if (deviceSearchSuggestions.isEmpty()) {
            mView.showNoDevices();
        } else {
            mView.showDevices(deviceSearchSuggestions);
        }
    }
}
