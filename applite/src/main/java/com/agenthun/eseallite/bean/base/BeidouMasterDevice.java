package com.agenthun.eseallite.bean.base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/12/16 04:06.
 */

public class BeidouMasterDevice implements Parcelable {
    private String ImplementID;

    public BeidouMasterDevice(String implementID) {
        ImplementID = implementID;
    }

    public String getImplementID() {
        return ImplementID;
    }

    public void setImplementID(String implementID) {
        ImplementID = implementID;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ImplementID);
    }

    protected BeidouMasterDevice(Parcel in) {
        this.ImplementID = in.readString();
    }

    public static final Creator<BeidouMasterDevice> CREATOR = new Creator<BeidouMasterDevice>() {
        @Override
        public BeidouMasterDevice createFromParcel(Parcel source) {
            return new BeidouMasterDevice(source);
        }

        @Override
        public BeidouMasterDevice[] newArray(int size) {
            return new BeidouMasterDevice[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeidouMasterDevice that = (BeidouMasterDevice) o;

        return ImplementID.equals(that.ImplementID);

    }

    @Override
    public int hashCode() {
        return ImplementID.hashCode();
    }

    @Override
    public String toString() {
        return "BeidouMasterDevice{" +
                "ImplementID='" + ImplementID + '\'' +
                '}';
    }
}
