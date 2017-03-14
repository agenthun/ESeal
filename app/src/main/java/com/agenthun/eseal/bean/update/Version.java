package com.agenthun.eseal.bean.update;

import com.google.gson.annotations.SerializedName;

/**
 * Created by wayne on 10/11/2016.
 */

public class Version {
    @SerializedName("UpdateUrl")
    private String updateUrl;

    @SerializedName("VersionCode")
    private int versionCode;

    @SerializedName("ApkSize")
    private long apkSize;

    @SerializedName("VersionName")
    private String versionName;

    @SerializedName("UpdateContent")
    private String updateContent;

    @SerializedName("Force")
    private boolean force;

    public String getUpdateUrl() {
        return updateUrl;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public long getApkSize() {
        return apkSize;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getUpdateContent() {
        return updateContent;
    }

    public boolean isForce() {
        return force;
    }
}