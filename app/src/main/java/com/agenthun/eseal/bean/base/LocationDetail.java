package com.agenthun.eseal.bean.base;

import com.baidu.mapapi.model.LatLng;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/11/22 20:45.
 */

public class LocationDetail {
    private String reportTime;
    private String status;
    private LatLng latLng;

    public LocationDetail() {
    }

    public LocationDetail(String reportTime, String status, LatLng latLng) {
        this.reportTime = reportTime;
        this.status = status;
        this.latLng = latLng;
    }

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
