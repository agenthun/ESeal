package com.pekingopera.versionupdate.listener;

import com.pekingopera.versionupdate.bean.Update;

/**
 * The update check callback
 */
public abstract class UpdateListener {

    /**
     * 有更新信息并回传一个更新的Update对象
     * There are a new version of APK on network
     */
    public void hasUpdate(Update update) {

    }

    /**
     * 没有更新信息
     * There are no new version for update
     */
    public abstract void noUpdate();

    /**
     * 更新检查失败，对应的网络返回码
     * http check error,
     *
     * @param code     http code
     * @param errorMsg http error msg
     */
    public abstract void onCheckError(int code, String errorMsg);

    /**
     * 用户取消更新操作
     * to be invoked by user press cancel button.
     */
    public void onUserCancel() {

    }

    /**
     * 用户取消下载操作
     * to be invoked by user press cancel button.
     */
    public void onUserCancelDowning() {
        onUserCancel();
    }

    /**
     * 用户取消安装操作,注意用户进入系统安装界面后取消这是是不做监听的
     * to be invoked by user press cancel button.
     */
    public void onUserCancelInstall() {
        onUserCancel();
    }
}
