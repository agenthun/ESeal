package com.pekingopera.versionupdate;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.dou361.download.DownloadManager;
import com.dou361.download.DownloadModel;
import com.dou361.download.SqliteManager;
import com.pekingopera.versionupdate.listener.ForceListener;
import com.pekingopera.versionupdate.listener.OnlineCheckListener;
import com.pekingopera.versionupdate.listener.UpdateListener;
import com.pekingopera.versionupdate.type.RequestType;
import com.pekingopera.versionupdate.type.UpdateType;
import com.pekingopera.versionupdate.util.UpdateSP;

import java.util.List;
import java.util.TreeMap;

/**
 */
public class UpdateHelper {

    private SqliteManager manager;
    private Context mContext;
    private String checkUrl;
    private TreeMap<String, Object> checkParams;
    private TreeMap<String, Object> onlineParams;
    private String onlineUrl;
    private ParseData parserCheckJson;
    private ParseData parserOnlineJson;

    private static UpdateHelper instance;
    private UpdateListener mUpdateListener;
    private ForceListener mForceListener;
    private OnlineCheckListener mOnlineCheckListener;

    //设置更新对话框的布局
    public UpdateHelper setDialogLayout(int view) {
        UpdateSP.setDialogLayout(view);
        return this;
    }

    //联网请求方式
    private RequestType mRequestType = RequestType.get;

    //默认需要检测更新
    private UpdateType mUpdateType = UpdateType.autoupdate;

    public static UpdateHelper getInstance() {
        if (instance == null) {
            throw new RuntimeException("UpdateHelper not initialized!");
        } else {
            return instance;
        }
    }

    public static void init(Context appContext) {
        instance = new UpdateHelper(appContext);
    }

    public UpdateHelper(Context context) {
        this.mContext = context;
        //添加下载状态列表
        manager = SqliteManager.getInstance(mContext);
        List<DownloadModel> models = manager.getAllDownloadInfo();
        DownloadManager.getInstance(mContext).addStateMap(models);

    }

    public UpdateHelper setMethod(RequestType requestType) {
        this.mRequestType = requestType;
        return this;
    }

    public UpdateHelper setCheckUrl(String url) {
        UpdateSP.setDialogLayout(0);
        this.checkUrl = url;
        return this;
    }

    public UpdateHelper setCheckUrl(String url, TreeMap<String, Object> params) {
        this.checkUrl = url;
        this.checkParams = params;
        return this;
    }

    public UpdateHelper setOnlineUrl(String url) {
        this.onlineUrl = url;
        return this;
    }

    public UpdateHelper setOnlineUrl(String url, TreeMap<String, Object> params) {
        this.onlineUrl = url;
        this.onlineParams = params;
        return this;
    }

    public UpdateHelper setUpdateListener(UpdateListener listener) {
        this.mUpdateListener = listener;
        return this;
    }

    public UpdateHelper setForceListener(ForceListener listener) {
        this.mForceListener = listener;
        return this;
    }

    public UpdateHelper setOnlineCheckListener(OnlineCheckListener listener) {
        this.mOnlineCheckListener = listener;
        return this;
    }

    public UpdateHelper setCheckJsonParser(ParseData jsonParser) {
        this.parserCheckJson = jsonParser;
        return this;
    }

    public UpdateHelper setOnlineJsonParser(ParseData jsonParser) {
        this.parserOnlineJson = jsonParser;
        return this;
    }

    public UpdateHelper setUpdateType(UpdateType updateType) {
        this.mUpdateType = updateType;
        return this;
    }

    public UpdateHelper setForced(boolean isForce) {
        UpdateSP.setForced(isForce);
        return this;
    }

    public Context getContext() {
        if (mContext == null) {
            throw new RuntimeException("should call UpdateConfig.install first");
        }
        return mContext;
    }

    public UpdateType getUpdateType() {
        return mUpdateType;
    }

    public String getCheckUrl() {
        if (TextUtils.isEmpty(checkUrl)) {
            throw new IllegalArgumentException("checkUrl is null");
        }
        return checkUrl;
    }

    public String getOnlineUrl() {
        return onlineUrl;
    }

    public TreeMap<String, Object> getOnlineParams() {
        return onlineParams;
    }

    public TreeMap<String, Object> getCheckParams() {
        return checkParams;
    }

    public RequestType getRequestMethod() {
        return mRequestType;
    }

    public ParseData getCheckJsonParser() {
        if (parserCheckJson == null) {
            throw new IllegalStateException("update parser is null");
        }
        return parserCheckJson;
    }

    public ParseData getOnlineJsonParser() {
        return parserOnlineJson;
    }


    public void check(Activity activity) {
        UpdateAgent.getInstance().checkUpdate(activity);
    }

    public UpdateListener getUpdateListener() {
        return mUpdateListener;
    }

    public ForceListener getForceListener() {
        return mForceListener;
    }

    public OnlineCheckListener getOnlineCheckListener() {
        return mOnlineCheckListener;
    }

}
