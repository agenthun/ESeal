package com.agenthun.eseallite.connectivity.manager;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/23 12:51.
 */

public class DownLoadFileManager {
    private static final String TAG = "DownLoadFileManager";

    private static final String APK_CONTENTTYPE = "application/vnd.android.package-archive";
    private static final String PNG_CONTENTTYPE = "image/png";
    private static final String JPG_CONTENTTYPE = "image/jpg";

    private static DownLoadFileManager instance;
    private DownloadCallBack callBack;
    private Handler handler;

    public DownLoadFileManager(DownloadCallBack callBack) {
        this.callBack = callBack;
    }

    public static DownLoadFileManager getInstance(DownloadCallBack callBack) {
        if (instance == null) {
            instance = new DownLoadFileManager(callBack);
        }
        return instance;
    }

    /**
     * 写入磁盘
     *
     * @param context
     * @param body
     * @param fileName
     * @return
     */
    public boolean writeResponseBodyToDisk(Context context, ResponseBody body, String fileName) {
        String type = body.contentType().toString();
//        Log.d(TAG, "file type: " + type);

        String fileSuffix = "";
        if (APK_CONTENTTYPE.equals(type)) {
            fileSuffix = ".apk";
        } else if (PNG_CONTENTTYPE.equals(type)) {
            fileSuffix = ".png";
        } else if (JPG_CONTENTTYPE.equals(type)) {
            fileSuffix = ".jpg";
        }

        final String name = fileName + fileSuffix;
        final String path = context.getExternalFilesDir(null) + File.separator + name;
//        Log.d(TAG, "file path: " + path);

        try {
            File futureStudioIconFile = new File(path); //具有访问权限, 存储在/storage/emulated/0/Android/data/包名/files/

            if (futureStudioIconFile.exists()) {
                long bodySize = body.contentLength();
                final long size = futureStudioIconFile.length();

                if (size == bodySize) {
                    //大小名字相同,初略判断为同一文件,不下载,直接安装.最好判断签名,MD5等
                    if (callBack != null) {
                        handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onSuccess(path, name, size);
                            }
                        });
                    }
                    return true;
                } else {
                    //大小不同, 删除文件
                    futureStudioIconFile.delete();
                }
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                final long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
//                Log.d(TAG, "file length: " + fileSize);
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
//                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);

                    if (callBack != null) {
                        final long finalFileSizeDownloaded = fileSizeDownloaded;
                        handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onProgress(finalFileSizeDownloaded, fileSize);
                            }
                        });

                    }
                }

                outputStream.flush();
//                Log.d(TAG, "file downloaded: " + fileSizeDownloaded + " of " + fileSize);

                if (callBack != null) {
                    handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onSuccess(path, name, fileSize);
                        }
                    });
//                    Log.d(TAG, "file downloaded: " + fileSizeDownloaded + " of " + fileSize);
                }

                return true;
            } catch (IOException e) {
                if (callBack != null) {
                    callBack.onError(e);
                }
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            if (callBack != null) {
                callBack.onError(e);
            }
            return false;
        }
    }

    /**
     * 判断是否已经下载过该文件
     * 最好以MD5,签名等验证
     *
     * @param context
     * @param fileName
     * @return
     */
    public boolean isDownloaded(Context context, String fileName) {
        boolean downloaded = false;

        final String name = fileName + ".apk";
        final String path = context.getExternalFilesDir(null) + File.separator + name;
        File futureStudioIconFile = new File(path);

        if (futureStudioIconFile.exists()) {
            downloaded = true;
            final long size = futureStudioIconFile.length();

            if (callBack != null) {
                handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onSuccess(path, name, size);
                    }
                });
            }
        }

        return downloaded;
    }
}
