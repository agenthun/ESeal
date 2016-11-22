package com.agenthun.eseal.utils;

import android.os.Parcel;

import com.agenthun.eseal.bean.base.Detail;
import com.agenthun.eseal.connectivity.service.Api;
import com.agenthun.eseal.connectivity.service.PathType;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/7 下午9:41.
 */
public class ContainerNoSuggestion implements SearchSuggestion {

    private Detail detail;
    private String mContainerNo;
    private boolean mIsHistory;
    private DeviceType deviceType;

    public ContainerNoSuggestion(Detail detail) {
        this.detail = detail;
        this.mContainerNo = detail.getContainerNo();
        this.mIsHistory = false;
        this.deviceType = DeviceType.DEVICE_BLE;
    }

    public ContainerNoSuggestion(Detail detail, DeviceType deviceType) {
        this.detail = detail;
        this.mContainerNo = detail.getContainerNo();
        this.mIsHistory = false;
        this.deviceType = deviceType;
    }

    public ContainerNoSuggestion(Parcel source) {
        this.mContainerNo = source.readString();
        this.mIsHistory = (source.readInt() != 0);
        this.deviceType = getDeviceType(source.readInt());
    }

    public Detail getDetail() {
        return detail;
    }

    @Override
    public String getBody() {
        return detail.getContainerNo();
    }

    public boolean getIsHistory() {
        return mIsHistory;
    }

    public void setIsHistory(boolean mIsHistory) {
        this.mIsHistory = mIsHistory;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public static final Creator<ContainerNoSuggestion> CREATOR = new Creator<ContainerNoSuggestion>() {
        @Override
        public ContainerNoSuggestion createFromParcel(Parcel source) {
            return new ContainerNoSuggestion(source);
        }

        @Override
        public ContainerNoSuggestion[] newArray(int size) {
            return new ContainerNoSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mContainerNo);
        dest.writeInt(mIsHistory ? 1 : 0);
        dest.writeInt(getDeviceIndexByType(deviceType));
    }

    public enum DeviceType {
        //蓝牙锁
        DEVICE_BLE,
        //北斗终端帽
        DEVICE_BEIDOU_MASTER,
        //北斗终端NFC
        DEVICE_BEIDOU_NFC
    }

    //获取相应的设备
    private DeviceType getDeviceType(Integer i) {
        switch (i) {
            case 0:
                return DeviceType.DEVICE_BLE;
            case 1:
                return DeviceType.DEVICE_BEIDOU_MASTER;
            case 2:
                return DeviceType.DEVICE_BEIDOU_NFC;
        }
        return DeviceType.DEVICE_BLE;
    }

    private Integer getDeviceIndexByType(DeviceType deviceType) {
        switch (deviceType) {
            case DEVICE_BLE:
                return 0;
            case DEVICE_BEIDOU_MASTER:
                return 1;
            case DEVICE_BEIDOU_NFC:
                return 2;
        }
        return 0;
    }
}
