package com.agenthun.eseal.bean.update;

/**
 * Created by wayne on 10/11/2016.
 */

public class ResponseResult<T> {
    private T Entity;

    private ResponseBase error;

    public T getEntity() {
        return Entity;
    }

    public ResponseBase getError() {
        return error;
    }
}