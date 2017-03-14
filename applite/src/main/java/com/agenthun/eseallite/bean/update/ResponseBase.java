package com.agenthun.eseallite.bean.update;

/**
 * Created by wayne on 9/22/2016.
 */

public class ResponseBase {
    private int result;
    private String errorInfo;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }
}