package com.agenthun.eseallite.pagefreighttrackmap;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.agenthun.eseallite.bean.base.LocationDetail;
import com.agenthun.eseallite.connectivity.manager.RetrofitManager;
import com.agenthun.eseallite.connectivity.service.PathType;
import com.agenthun.eseallite.pagetimepicker.TimePickerActivity;
import com.agenthun.eseallite.utils.PreferencesHelper;
import com.agenthun.eseallite.utils.scheduler.BaseSchedulerProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observer;
import rx.subscriptions.CompositeSubscription;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/3/12 17:39.
 */

public class FreightTrackMapWithWebViewPresenter implements FreightTrackMapWithWebViewContract.Presenter {

    private static final String TAG = "FreightMapPresenter";

    boolean mUsingWebView;

    private String mFreightId;

    private String mFreightName;

    private Context mContext;

    private FreightTrackMapWithWebViewContract.View mView;

    private BaseSchedulerProvider mSchedulerProvider;

    private CompositeSubscription mSubscriptions;

    private LocationDetail mLocationDetail = null;
    private List<LocationDetail> mLocationDetailList = new ArrayList<>();

    public FreightTrackMapWithWebViewPresenter(boolean usingWebView,
                                               String freightId,
                                               String freightName,
                                               Context context,
                                               FreightTrackMapWithWebViewContract.View view,
                                               BaseSchedulerProvider schedulerProvider) {
        mUsingWebView = usingWebView;
        mFreightId = freightId;
        mFreightName = freightName;
        mContext = context;
        mView = view;
        mSchedulerProvider = schedulerProvider;
        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    @Override
    public void subscribe() {
        loadFreightLocation(false, PreferencesHelper.getTOKEN(mContext), mFreightId, null, null);
    }

    @Override
    public void unsubscribe() {
        mSubscriptions.clear();
    }

    @Override
    public void result(int requestCode, int resultCode, Intent data) {
        if (resultCode == TimePickerActivity.RESULT_PICK_TIME) {
            String from = data.getStringExtra(TimePickerActivity.PICK_TIME_FROM);
            String to = data.getStringExtra(TimePickerActivity.PICK_TIME_TO);
            Log.d(TAG, "from: " + from + ", to: " + to);

            loadFreightLocation(true, PreferencesHelper.getTOKEN(mContext),
                    mFreightId, from, to);
        }
    }

    @Override
    public void loadFreightLocation(boolean isFreightTrackMode, @NonNull String token, @NonNull String id, @Nullable String from, @Nullable String to) {
        if (isFreightTrackMode) {
            //获取时间段内位置列表
            mSubscriptions.add(RetrofitManager
                    .builder(PathType.WEB_SERVICE_V2_TEST)
                    .getBeidouMasterDeviceLocationObservable(token, id, from, to)
                    .subscribeOn(mSchedulerProvider.io())
                    .observeOn(mSchedulerProvider.ui())
                    .subscribe(new Observer<List<LocationDetail>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            mView.showLoadingFreightLocationError();
                        }

                        @Override
                        public void onNext(List<LocationDetail> locationDetails) {
                            mLocationDetailList = locationDetails;
                            if (locationDetails.isEmpty()) {
                                mView.showNoFreightLocationData();
                                return;
                            }
                            if (!mUsingWebView) {
                                mView.clearLocationData();
                                mView.showBaiduMap(locationDetails);
                            } else {
                                mView.showWebViewMap(locationDetails);
                            }
                        }
                    }));
        } else {
            //获取最新位置点
            mSubscriptions.add(RetrofitManager
                    .builder(PathType.WEB_SERVICE_V2_TEST)
                    .getBeidouMasterDeviceLastLocationObservable(token, id)
                    .subscribeOn(mSchedulerProvider.io())
                    .observeOn(mSchedulerProvider.ui())
                    .subscribe(new Observer<LocationDetail>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            mView.showLoadingFreightLocationError();
                        }

                        @Override
                        public void onNext(LocationDetail locationDetail) {
                            mLocationDetail = locationDetail;
                            if (!mUsingWebView) {
                                mView.clearLocationData();
                                mView.showBaiduMap(locationDetail);
                            } else {
                                mView.showWebViewMap(Arrays.asList(locationDetail));
                            }
                        }
                    }));
        }
    }

    @Override
    public void loadFreightDataListDetail(boolean isFreightTrackMode) {
        if (isFreightTrackMode) {
            if (mLocationDetailList != null && !mLocationDetailList.isEmpty()) {
                mView.showFreightDataListByBottomSheet(PreferencesHelper.getTOKEN(mContext),
                        mFreightId, mFreightName, mLocationDetailList);
            }
        } else {
            if (mLocationDetail != null) {
                mView.showFreightDataListByBottomSheet(PreferencesHelper.getTOKEN(mContext),
                        mFreightId, mFreightName, Arrays.asList(mLocationDetail));
            }
        }
    }
}
