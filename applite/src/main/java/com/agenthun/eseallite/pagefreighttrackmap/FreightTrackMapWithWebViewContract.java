package com.agenthun.eseallite.pagefreighttrackmap;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.agenthun.eseallite.BasePresenter;
import com.agenthun.eseallite.BaseView;
import com.agenthun.eseallite.bean.base.LocationDetail;

import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/3/12 17:35.
 */

public interface FreightTrackMapWithWebViewContract {
    interface View extends BaseView<Presenter> {
        void setupMapUi(boolean usingWebView);

        /**
         * 清除百度地图覆盖物
         */
        void clearLocationData();

        /**
         * 加载轨迹数据至百度地图
         */
        void showBaiduMap(List<LocationDetail> locationDetails);

        void showBaiduMap(LocationDetail locationDetail);

        /**
         * 加载轨迹数据至WebView Google地图
         */
        void showWebViewMap(List<LocationDetail> locationDetails);

        void showLoadingFreightLocationError();

        void showNoFreightLocationData();

        void startTimePickerActivity();

        void showFreightDataListByBottomSheet(String token, String containerId, final String containerNo, List<LocationDetail> details);
    }

    interface Presenter extends BasePresenter {
        void result(int requestCode, int resultCode, Intent data);

        void loadFreightLocation(boolean isFreightTrackMode,
                                 @NonNull String token, @NonNull String id,
                                 @Nullable String from, @Nullable String to);

        void loadFreightDataListDetail(boolean isFreightTrackMode);
    }
}
