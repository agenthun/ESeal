package com.agenthun.eseal.bean;

import com.agenthun.eseal.bean.base.DeviceLocation;
import com.agenthun.eseal.bean.base.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/12/16 05:42.
 */

public class DeviceLocationInfos {
    private List<Result> Result = new ArrayList<>();
    private List<DeviceLocation> Details = new ArrayList<>();

    public List<com.agenthun.eseal.bean.base.Result> getResult() {
        return Result;
    }

    public void setResult(List<com.agenthun.eseal.bean.base.Result> result) {
        Result = result;
    }

    public List<DeviceLocation> getDetails() {
        return Details;
    }

    public void setDetails(List<DeviceLocation> details) {
        Details = details;
    }
}
