package com.pekingopera.versionupdate.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pekingopera.versionupdate.UpdateHelper;
import com.pekingopera.versionupdate.util.ResourceUtils;
import com.pekingopera.versionupdate.util.UpdateSP;


/**
 * ========================================
 * <p>
 * 版 权：dou361.com 版权所有 （C） 2015
 * <p>
 * 作 者：陈冠明
 * <p>
 * 个人网站：http://www.dou361.com
 * <p>
 * 版 本：1.0
 * <p>
 * 创建日期：2016/6/17
 * <p>
 * 描 述：
 * <p>
 * <p>
 * 修订历史：
 * <p>
 * ========================================
 */
public class DownloadDialogActivity extends Activity {

    private ProgressBar pgBar;
    private TextView tvPg;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = this;
        setContentView(ResourceUtils.getResourceIdByName(mContext, "layout", "jjdxm_download_dialog"));
        pgBar = (ProgressBar) findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "jjdxm_update_progress_bar"));
        tvPg = (TextView) findViewById(ResourceUtils.getResourceIdByName(mContext, "id", "jjdxm_update_progress_text"));
        broadcast();
    }

    /**
     * 刷新下载进度
     */
    private void updateProgress(long percent) {
        if (tvPg != null) {
            tvPg.setText(percent + "%");
            pgBar.setProgress((int) percent);
        }
        if (percent >= 100) {
            finish();
        }
    }

    /**
     * 注册广播
     */
    private void broadcast() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager
                .getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter
                .addAction("com.dou361.update.downloadBroadcast");
        /** 建议把它写一个公共的变量，这里方便阅读就不写了 */
        BroadcastReceiver mItemViewListClickReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                /** 刷新数据 */
                long type = intent.getLongExtra("type", 0);
                updateProgress(type);
            }
        };
        broadcastManager.registerReceiver(mItemViewListClickReceiver,
                intentFilter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && UpdateSP.isForced()) {
            finish();
            if (UpdateHelper.getInstance().getForceListener() != null) {
                UpdateHelper.getInstance().getForceListener().onUserCancel(UpdateSP.isForced());
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
