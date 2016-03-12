package com.agenthun.eseal.bean.base;

import java.util.ArrayList;
import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/3 下午9:23.
 */
public class BaseWebServiceResponseBody {
    private List<Result> Result = new ArrayList<>();
    private List<Detail> Details = new ArrayList<>();

    public List<com.agenthun.eseal.bean.base.Result> getResult() {
        return Result;
    }

    public void setResult(List<com.agenthun.eseal.bean.base.Result> result) {
        Result = result;
    }

    public List<Detail> getDetails() {
        return Details;
    }

    public void setDetails(List<Detail> details) {
        Details = details;
    }
}
