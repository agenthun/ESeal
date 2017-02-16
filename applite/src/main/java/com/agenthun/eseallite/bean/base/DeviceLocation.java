package com.agenthun.eseallite.bean.base;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/12/16 05:40.
 */

public class DeviceLocation implements Parcelable {
    private String ReportTime;
    private String Status;
    private String BaiduCoordinate;

    public DeviceLocation(String reportTime, String status, String baiduCoordinate) {
        ReportTime = reportTime;
        Status = status;
        BaiduCoordinate = baiduCoordinate;
    }

    public String getReportTime() {
        return ReportTime;
    }

    public void setReportTime(String reportTime) {
        ReportTime = reportTime;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getBaiduCoordinate() {
        return BaiduCoordinate;
    }

    public void setBaiduCoordinate(String baiduCoordinate) {
        BaiduCoordinate = baiduCoordinate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ReportTime);
        dest.writeString(this.Status);
        dest.writeString(this.BaiduCoordinate);
    }

    protected DeviceLocation(Parcel in) {
        this.ReportTime = in.readString();
        this.Status = in.readString();
        this.BaiduCoordinate = in.readString();
    }

    public static final Creator<DeviceLocation> CREATOR = new Creator<DeviceLocation>() {
        @Override
        public DeviceLocation createFromParcel(Parcel source) {
            return new DeviceLocation(source);
        }

        @Override
        public DeviceLocation[] newArray(int size) {
            return new DeviceLocation[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceLocation that = (DeviceLocation) o;

        if (!ReportTime.equals(that.ReportTime)) return false;
        if (!Status.equals(that.Status)) return false;
        return BaiduCoordinate.equals(that.BaiduCoordinate);

    }

    @Override
    public int hashCode() {
        int result = ReportTime.hashCode();
        result = 31 * result + Status.hashCode();
        result = 31 * result + BaiduCoordinate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "DeviceLocation{" +
                "ReportTime='" + ReportTime + '\'' +
                ", Status='" + Status + '\'' +
                ", BaiduCoordinate='" + BaiduCoordinate + '\'' +
                '}';
    }

    public Boolean isInvalid() {
        return ReportTime == null || Status == null || BaiduCoordinate == null;
    }
}
