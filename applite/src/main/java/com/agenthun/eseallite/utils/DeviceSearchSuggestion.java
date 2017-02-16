package com.agenthun.eseallite.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.agenthun.eseallite.bean.base.BeidouMasterDevice;
import com.agenthun.eseallite.bean.base.BeidouNfcDevice;
import com.agenthun.eseallite.bean.base.BleAndBeidouNfcDevice;
import com.agenthun.eseallite.bean.base.BleDevice;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/12/16 05:01.
 */

public class DeviceSearchSuggestion implements Parcelable {
    //蓝牙锁
    public static final int DEVICE_BLE = 0;
    //北斗终端帽
    public static final int DEVICE_BEIDOU_MASTER = 1;
    //北斗终端NFC
    public static final int DEVICE_BEIDOU_NFC = 2;

    @NonNull
    private String id;

    @Nullable
    private String name;
    private Integer type;
    private Integer isHistory;

    public DeviceSearchSuggestion(BleDevice device) {
        this(device.getContainerId(), device.getContainerNo(), DEVICE_BLE, 0);
    }

    public DeviceSearchSuggestion(BeidouMasterDevice device) {
        this(device.getImplementID(), device.getImplementID(), DEVICE_BEIDOU_MASTER, 0);
    }

    public DeviceSearchSuggestion(BeidouNfcDevice device) {
        this(device.getNFCId(), device.getNFCId(), DEVICE_BEIDOU_NFC, 0);
    }

    public DeviceSearchSuggestion(BleAndBeidouNfcDevice device, int deviceType) {
        if (deviceType == DEVICE_BLE) {
            this.id = device.getContainerId();
            this.name = device.getContainerNo();
            this.type = DEVICE_BLE;
            this.isHistory = 0;
        } else if (deviceType == DEVICE_BEIDOU_NFC) {
            this.id = device.getContainerId();
            this.name = device.getContainerNo();
            this.type = DEVICE_BEIDOU_NFC;
            this.isHistory = 0;
        }
    }

    public DeviceSearchSuggestion(@NonNull String id, String name, Integer type, Integer isHistory) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isHistory = isHistory;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getIsHistory() {
        return isHistory;
    }

    public void setIsHistory(Integer isHistory) {
        this.isHistory = isHistory;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeValue(this.type);
        dest.writeValue(this.isHistory);
    }

    protected DeviceSearchSuggestion(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.type = (Integer) in.readValue(Integer.class.getClassLoader());
        this.isHistory = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<DeviceSearchSuggestion> CREATOR = new Parcelable.Creator<DeviceSearchSuggestion>() {
        @Override
        public DeviceSearchSuggestion createFromParcel(Parcel source) {
            return new DeviceSearchSuggestion(source);
        }

        @Override
        public DeviceSearchSuggestion[] newArray(int size) {
            return new DeviceSearchSuggestion[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceSearchSuggestion that = (DeviceSearchSuggestion) o;

        if (!id.equals(that.id)) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DeviceSearchSuggestion{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", isHistory=" + isHistory +
                '}';
    }
}
