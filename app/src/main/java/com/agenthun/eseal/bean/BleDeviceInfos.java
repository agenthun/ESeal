package com.agenthun.eseal.bean;

import com.agenthun.eseal.bean.base.BleDevice;
import com.agenthun.eseal.bean.base.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/12/16 04:33.
 */

public class BleDeviceInfos {
    private List<Result> Result = new ArrayList<>();
    private List<BleDevice> Details = new ArrayList<>();

    public List<Result> getResult() {
        return Result;
    }

    public void setResult(List<Result> result) {
        Result = result;
    }

    public List<BleDevice> getDetails() {
        return Details;
    }

    public void setDetails(List<BleDevice> details) {
        Details = details;
    }
}
