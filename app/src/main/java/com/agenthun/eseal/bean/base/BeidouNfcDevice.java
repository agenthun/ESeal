package com.agenthun.eseal.bean.base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/12/16 04:08.
 */

public class BeidouNfcDevice implements Parcelable {
    private String NFCId;

    public BeidouNfcDevice(String NFCId) {
        this.NFCId = NFCId;
    }

    public String getNFCId() {
        return NFCId;
    }

    public void setNFCId(String NFCId) {
        this.NFCId = NFCId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.NFCId);
    }

    protected BeidouNfcDevice(Parcel in) {
        this.NFCId = in.readString();
    }

    public static final Parcelable.Creator<BeidouNfcDevice> CREATOR = new Parcelable.Creator<BeidouNfcDevice>() {
        @Override
        public BeidouNfcDevice createFromParcel(Parcel source) {
            return new BeidouNfcDevice(source);
        }

        @Override
        public BeidouNfcDevice[] newArray(int size) {
            return new BeidouNfcDevice[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeidouNfcDevice that = (BeidouNfcDevice) o;

        return NFCId.equals(that.NFCId);

    }

    @Override
    public int hashCode() {
        return NFCId.hashCode();
    }

    @Override
    public String toString() {
        return "BeidouNfcDevice{" +
                "NFCId='" + NFCId + '\'' +
                '}';
    }
}
