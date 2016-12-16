package com.agenthun.eseal.bean;

import com.agenthun.eseal.bean.base.BeidouMasterDevice;
import com.agenthun.eseal.bean.base.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/12/16 04:36.
 */

public class BeidouMasterDeviceInfos {
    private List<Result> Result = new ArrayList<>();
    private List<BeidouMasterDevice> Details = new ArrayList<>();

    public List<com.agenthun.eseal.bean.base.Result> getResult() {
        return Result;
    }

    public void setResult(List<com.agenthun.eseal.bean.base.Result> result) {
        Result = result;
    }

    public List<BeidouMasterDevice> getDetails() {
        return Details;
    }

    public void setDetails(List<BeidouMasterDevice> details) {
        Details = details;
    }
}
