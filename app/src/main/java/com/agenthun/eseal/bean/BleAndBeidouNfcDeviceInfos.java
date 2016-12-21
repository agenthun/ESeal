package com.agenthun.eseal.bean;

import com.agenthun.eseal.bean.base.BleAndBeidouNfcDevice;
import com.agenthun.eseal.bean.base.BleDevice;
import com.agenthun.eseal.bean.base.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/12/16 04:33.
 */

public class BleAndBeidouNfcDeviceInfos {
    private List<Result> Result = new ArrayList<>();
    private List<BleAndBeidouNfcDevice> Details = new ArrayList<>();

    public List<Result> getResult() {
        return Result;
    }

    public void setResult(List<Result> result) {
        Result = result;
    }

    public List<BleAndBeidouNfcDevice> getDetails() {
        return Details;
    }

    public void setDetails(List<BleAndBeidouNfcDevice> details) {
        Details = details;
    }
}
