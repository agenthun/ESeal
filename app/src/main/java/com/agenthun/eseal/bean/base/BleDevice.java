package com.agenthun.eseal.bean.base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/12/16 03:43.
 */

public class BleDevice implements Parcelable {
    private String ContainerId;
    private String ContainerNo;
    private String FreightName;
    private String Origin;
    private String Frequency;
    private String NFCID;
    private String Operationer;
    private String Status;

    public BleDevice(String containerId, String containerNo, String freightName, String origin, String frequency, String NFCID, String operationer, String status) {
        ContainerId = containerId;
        ContainerNo = containerNo;
        FreightName = freightName;
        Origin = origin;
        Frequency = frequency;
        this.NFCID = NFCID;
        Operationer = operationer;
        Status = status;
    }

    public String getContainerId() {
        return ContainerId;
    }

    public void setContainerId(String containerId) {
        ContainerId = containerId;
    }

    public String getContainerNo() {
        return ContainerNo;
    }

    public void setContainerNo(String containerNo) {
        ContainerNo = containerNo;
    }

    public String getFreightName() {
        return FreightName;
    }

    public void setFreightName(String freightName) {
        FreightName = freightName;
    }

    public String getOrigin() {
        return Origin;
    }

    public void setOrigin(String origin) {
        Origin = origin;
    }

    public String getFrequency() {
        return Frequency;
    }

    public void setFrequency(String frequency) {
        Frequency = frequency;
    }

    public String getNFCID() {
        return NFCID;
    }

    public void setNFCID(String NFCID) {
        this.NFCID = NFCID;
    }

    public String getOperationer() {
        return Operationer;
    }

    public void setOperationer(String operationer) {
        Operationer = operationer;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ContainerId);
        dest.writeString(this.ContainerNo);
        dest.writeString(this.FreightName);
        dest.writeString(this.Origin);
        dest.writeString(this.Frequency);
        dest.writeString(this.NFCID);
        dest.writeString(this.Operationer);
        dest.writeString(this.Status);
    }

    protected BleDevice(Parcel in) {
        this.ContainerId = in.readString();
        this.ContainerNo = in.readString();
        this.FreightName = in.readString();
        this.Origin = in.readString();
        this.Frequency = in.readString();
        this.NFCID = in.readString();
        this.Operationer = in.readString();
        this.Status = in.readString();
    }

    public static final Parcelable.Creator<BleDevice> CREATOR = new Parcelable.Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel source) {
            return new BleDevice(source);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BleDevice that = (BleDevice) o;

        return ContainerId.equals(that.ContainerId);

    }

    @Override
    public int hashCode() {
        return ContainerId.hashCode();
    }

    @Override
    public String toString() {
        return "BleDevice{" +
                "ContainerId='" + ContainerId + '\'' +
                ", ContainerNo='" + ContainerNo + '\'' +
                ", FreightName='" + FreightName + '\'' +
                ", Origin='" + Origin + '\'' +
                ", Frequency='" + Frequency + '\'' +
                ", NFCID='" + NFCID + '\'' +
                ", Operationer='" + Operationer + '\'' +
                ", Status='" + Status + '\'' +
                '}';
    }
}
