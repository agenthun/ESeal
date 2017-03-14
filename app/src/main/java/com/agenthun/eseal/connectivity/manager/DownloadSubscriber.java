package com.agenthun.eseal.connectivity.manager;

import android.content.Context;

import rx.Subscriber;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/23 12:20.
 */

public class DownloadSubscriber<ResponseBody> extends Subscriber<ResponseBody> {
    private DownloadCallBack callBack;
    private Context mContext;
    private String mFileName;

    public DownloadSubscriber(Context context, String fileName, DownloadCallBack callBack) {
        mContext = context;
        mFileName = fileName;
        this.callBack = callBack;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (callBack != null) {
            callBack.onStart();
        }
    }

    @Override
    public void onCompleted() {
        if (callBack != null) {
            callBack.onCompleted();
        }
    }

    @Override
    public void onError(Throwable e) {
        if (callBack != null) {
            callBack.onError(e);
        }
    }

    @Override
    public void onNext(ResponseBody responseBody) {
        //根据名字,简单判断是否已下载过
        boolean downloaded = DownLoadFileManager.getInstance(callBack)
                .isDownloaded(mContext, mFileName);

        if (!downloaded) {
            DownLoadFileManager.getInstance(callBack)
                    .writeResponseBodyToDisk(mContext, (okhttp3.ResponseBody) responseBody, mFileName);
        }
    }
}
