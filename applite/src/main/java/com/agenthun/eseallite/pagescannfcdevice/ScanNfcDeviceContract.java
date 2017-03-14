package com.agenthun.eseallite.pagescannfcdevice;

import com.agenthun.eseallite.BasePresenter;
import com.agenthun.eseallite.BaseView;
import com.agenthun.eseallite.utils.DeviceSearchSuggestion;

import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/3/1 08:28.
 */

public interface ScanNfcDeviceContract {
    interface View extends BaseView<Presenter> {
        void setLoadingIndicator(boolean active); //设置下拉刷新

        void showDevices(List<DeviceSearchSuggestion> devices);

        void showLoadingDevicesError();

        void showNoDevices();

        void showDeviceDetailsUi(DeviceSearchSuggestion deviceSearchSuggestion);

        void showDeviceDetailsUi(String id, Integer type, String name);
    }

    interface Presenter extends BasePresenter {
        void loadDevices(boolean isShowLoadingIndicator, String token);
    }
}
