package com.agenthun.eseallite.connectivity.manager;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/23 13:02.
 */

public interface DownloadCallBack {
    void onStart(); //开始下载

    void onProgress(long current, long total); //下载进度

    void onCompleted(); //下载完成

    void onSuccess(String path, String name, long fileSize); //下载成功

    void onError(Throwable e); //下载失败
}
