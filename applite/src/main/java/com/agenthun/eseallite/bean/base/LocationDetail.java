package com.agenthun.eseallite.bean.base;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.model.LatLng;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/11/22 20:45.
 */

public class LocationDetail implements Parcelable {
    private String reportTime;
    private String uploadType;
    private String securityLevel;
    private String closedFlag;
    private LatLng latLng;

    public LocationDetail() {
    }

    public LocationDetail(String reportTime, String uploadType, String securityLevel, String closedFlag, LatLng latLng) {
        this.reportTime = reportTime;
        this.uploadType = uploadType;
        this.securityLevel = securityLevel;
        this.closedFlag = closedFlag;
        this.latLng = latLng;
    }

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }

    public String getUploadType() {
        return uploadType;
    }

    public void setUploadType(String uploadType) {
        this.uploadType = uploadType;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String getClosedFlag() {
        return closedFlag;
    }

    public void setClosedFlag(String closedFlag) {
        this.closedFlag = closedFlag;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.reportTime);
        dest.writeString(this.uploadType);
        dest.writeString(this.securityLevel);
        dest.writeString(this.closedFlag);
        dest.writeParcelable(this.latLng, flags);
    }

    protected LocationDetail(Parcel in) {
        this.reportTime = in.readString();
        this.uploadType = in.readString();
        this.securityLevel = in.readString();
        this.closedFlag = in.readString();
        this.latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<LocationDetail> CREATOR = new Creator<LocationDetail>() {
        @Override
        public LocationDetail createFromParcel(Parcel source) {
            return new LocationDetail(source);
        }

        @Override
        public LocationDetail[] newArray(int size) {
            return new LocationDetail[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocationDetail that = (LocationDetail) o;

        if (!reportTime.equals(that.reportTime)) return false;
        return latLng.equals(that.latLng);

    }

    @Override
    public int hashCode() {
        int result = reportTime.hashCode();
        result = 31 * result + latLng.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LocationDetail{" +
                "reportTime='" + reportTime + '\'' +
                ", uploadType='" + uploadType + '\'' +
                ", securityLevel='" + securityLevel + '\'' +
                ", closedFlag='" + closedFlag + '\'' +
                ", latLng=" + latLng +
                '}';
    }

    public Boolean isInvalid() {
        return reportTime == null || uploadType == null || latLng == null;
    }
}
