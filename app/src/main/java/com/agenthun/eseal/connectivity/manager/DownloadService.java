package com.agenthun.eseal.connectivity.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.agenthun.eseal.R;
import com.agenthun.eseal.connectivity.service.PathType;

import java.io.File;

import okhttp3.ResponseBody;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/24 15:26.
 */

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";

    public static final String DOWNLOAD_URL = "DOWNLOAD_URL";
    public static final String DOWNLOAD_FILE_NAME = "DOWNLOAD_FILE_NAME";

    public static final String DOWNLOAD_ACTION = "DOWNLOAD_ACTION";
    public static final int DOWNLOAD_ACTION_START = 110;
    public static final int DOWNLOAD_ACTION_STOP = 111;
    public static final int DOWNLOAD_ACTION_CANCEL = 112;

    public static final int NOTIFICATION_STATE_NORMAL = 120;
    public static final int NOTIFICATION_STATE_DOWNLOADING = 121;
    public static final int NOTIFICATION_STATE_INSTALL_NOW = 122;
    public static final int NOTIFICATION_STATE_INSTALL_LATER = 123;

    private Context mContext;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;

    private boolean downloading = false;

    //默认DOWNLOAD_ACTION为DOWNLOAD_ACTION_START启动下载
    public static void start(Context context, String url, String fileName) {
        Intent starter = new Intent(context, DownloadService.class);
        starter.putExtra(DOWNLOAD_URL, url);
        starter.putExtra(DOWNLOAD_FILE_NAME, fileName);
        starter.putExtra(DOWNLOAD_ACTION, DOWNLOAD_ACTION_START);
        context.startService(starter);
    }

    public static void stop(Context context) {
        Intent stoper = new Intent(context, DownloadService.class);
        stoper.putExtra(DOWNLOAD_ACTION, DOWNLOAD_ACTION_STOP);
        context.stopService(stoper);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = this;

        notificationBuilder = new NotificationCompat.Builder(mContext);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int action = intent.getIntExtra(DOWNLOAD_ACTION, 0);
            switch (action) {
                case DOWNLOAD_ACTION_START:
                    String url = intent.getStringExtra(DOWNLOAD_URL);
                    String name = intent.getStringExtra(DOWNLOAD_FILE_NAME);
                    if (!downloading && url != null && name != null) {
                        downloadLatestApp(url, name); //启动下载
                    }
                    break;
                case DOWNLOAD_ACTION_STOP:
                    break;
                case DOWNLOAD_ACTION_CANCEL:
                    break;
                default:
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 设置下载进度条Notification显示
     */
    private void setupNotificationDownloading(PendingIntent pendingIntent, int smallIcon, String ticker,
                                              String title, String content, boolean sound, boolean vibrate, boolean lights) {
//        // 如果当前Activity启动在前台，则不开启新的Activity。
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        // 当设置下面PendingIntent.FLAG_UPDATE_CURRENT这个参数的时候，常常使得点击通知栏没效果，你需要给notification设置一个独一无二的requestCode
//        // 将Intent封装进PendingIntent中，点击通知的消息后，就会启动对应的程序
//        PendingIntent pIntent = PendingIntent.getActivity(mContext,
//                requestCode, intent, FLAG);
        notificationBuilder.setContentIntent(pendingIntent);// 该通知要启动的Intent
        notificationBuilder.setSmallIcon(smallIcon);// 设置顶部状态栏的小图标
        notificationBuilder.setTicker(ticker);// 在顶部状态栏中的提示信息

        notificationBuilder.setContentTitle(title);// 设置通知中心的标题
        notificationBuilder.setContentText(content);// 设置通知中心中的内容
        notificationBuilder.setWhen(System.currentTimeMillis());

		/*
         * 将AutoCancel设为true后，当你点击通知栏的notification后，它会自动被取消消失,
		 * 不设置的话点击消息后也不清除，但可以滑动删除
		 */
        notificationBuilder.setAutoCancel(false);
        // 将Ongoing设为true 那么notification将不能滑动删除
        // notifyBuilder.setOngoing(true);
        /*
         * 从Android4.1开始，可以通过以下方法，设置notification的优先级，
		 * 优先级越高的，通知排的越靠前，优先级低的，不会在手机最顶部的状态栏显示图标
		 */
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        /*
         * Notification.DEFAULT_ALL：铃声、闪光、震动均系统默认。
		 * Notification.DEFAULT_SOUND：系统默认铃声。
		 * Notification.DEFAULT_VIBRATE：系统默认震动。
		 * Notification.DEFAULT_LIGHTS：系统默认闪光。
		 * notifyBuilder.setDefaults(Notification.DEFAULT_ALL);
		 */
        int defaults = 0;

        if (sound) {
            defaults |= Notification.DEFAULT_SOUND;
        }
        if (vibrate) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (lights) {
            defaults |= Notification.DEFAULT_LIGHTS;
        }

        notificationBuilder.setDefaults(defaults);
    }

    /**
     * 刷新下载进度
     */
    private void showProgressDownloadNotificationCompat(int percent) {
        notificationBuilder.setProgress(100, percent, false);
        notificationManager.notify(NOTIFICATION_STATE_NORMAL, notificationBuilder.build());
    }

    private void showDownloadFileSuccess(String path, String name) {
        notificationBuilder.setContentText(getString(R.string.success_download_file))
                .setProgress(0, 0, false);

        //以后再安装PendingIntent
        Intent installLaterIntent = new Intent();
        installLaterIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent installLaterPendingIntent = PendingIntent.getActivity(mContext,
                NOTIFICATION_STATE_INSTALL_LATER, installLaterIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //现在安装PendingIntent
        Intent installNowIntent = new Intent(Intent.ACTION_VIEW);
        installNowIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installNowIntent.setDataAndType(Uri.fromFile(new File(path)),
                "application/vnd.android.package-archive");
        PendingIntent installNowPendingIntent = PendingIntent.getActivity(mContext,
                NOTIFICATION_STATE_INSTALL_NOW, installNowIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        boolean usingButton = false; //控制是否使用按钮

        if (usingButton) {
            //添加按钮
            notificationBuilder.addAction(R.drawable.ic_timelapse_black_24dp,
                    getString(R.string.text_app_update_later),
                    installLaterPendingIntent);
            notificationBuilder.addAction(R.drawable.ic_done_black_24dp,
                    getString(R.string.text_app_install_now),
                    installNowPendingIntent);
        } else {
            //不加按钮
            notificationBuilder.setContentTitle(name);// 设置通知中心的标题
            notificationBuilder.setContentText(getString(R.string.success_download_file) +
                    ", " + getString(R.string.text_app_install_now));// 设置通知中心中的内容
            notificationBuilder.setContentIntent(installNowPendingIntent);
        }
        notificationBuilder.setAutoCancel(true); //点击后消失

        notificationManager.notify(NOTIFICATION_STATE_NORMAL, notificationBuilder.build());

        stop(mContext);
//        stopSelf();
    }

    private void showDownloadFileError() {
        showMessage(getString(R.string.error_download_file));

        notificationBuilder.setContentText(getString(R.string.error_download_file))
                .setProgress(100, 0, false);

        notificationBuilder.setAutoCancel(true); //点击后消失
        notificationManager.notify(NOTIFICATION_STATE_NORMAL, notificationBuilder.build());
    }

    private void showMessage(String message) {
/*        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text)))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
        snackbar.show();*/
    }

    private void showMessage(String message, String textActionButton, @Nullable View.OnClickListener onClickListener) {
/*        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction(textActionButton, onClickListener);
        ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text)))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
        snackbar.show();*/
    }

    private void processInstallApk(Context context, String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(path)),
                "application/vnd.android.package-archive");
        ContextCompat.startActivity(context, intent, null);
    }

    private void processUninstallApk(Context context, String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        ContextCompat.startActivity(context, intent, null);
    }

    /**
     * 下载APP文件
     *
     * @param url
     * @param fileName
     */
    private void downloadLatestApp(final String url, final String fileName) {
        RetrofitManager.builder(mContext, PathType.WEB_SERVICE_V2_TEST)
                .downloadFileObservable(url)
                .subscribe(new DownloadSubscriber<ResponseBody>(mContext, fileName, new DownloadCallBack() {
                    int progressTemp = 0;

                    @Override
                    public void onStart() {
                        downloading = true;
                        showMessage(getString(R.string.text_download_file));

                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                                NOTIFICATION_STATE_DOWNLOADING, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        setupNotificationDownloading(pendingIntent,
                                R.drawable.ic_app,
                                getString(R.string.text_new_notification_message),
                                fileName + ".apk",
                                getString(R.string.text_downloading_file),
                                false, false, false);
                        notificationManager.notify(NOTIFICATION_STATE_NORMAL, notificationBuilder.build());
                    }

                    @Override
                    public void onProgress(long current, long total) {
                        Log.d(TAG, "downloaded: " + current + "/" + total);
                        int percent = (int) (current * 100.0f / total);
                        if (progressTemp != percent) {
                            showProgressDownloadNotificationCompat(percent);
                        }
                        progressTemp = percent;
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onSuccess(final String path, final String name, long fileSize) {
                        showMessage(getString(R.string.success_download_file),
                                getString(R.string.text_app_install_now),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        processInstallApk(mContext, path);
                                    }
                                });

                        showDownloadFileSuccess(path, name);
                        processInstallApk(mContext, path);
                        downloading = false;
                    }

                    @Override
                    public void onError(Throwable e) {
                        showDownloadFileError();
                        downloading = false;
                    }
                }));
    }
}
