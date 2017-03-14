package com.pekingopera.versionupdate.util;

import android.content.Context;


import com.pekingopera.versionupdate.UpdateHelper;

import java.io.File;
import java.math.BigDecimal;

/**
 * ========================================
 * <p/>
 * 版 权：dou361.com 版权所有 （C） 2015
 * <p/>
 * 作 者：陈冠明
 * <p/>
 * 个人网站：http://www.dou361.com
 * <p/>
 * 版 本：1.0
 * <p/>
 * 创建日期：2016/6/16
 * <p/>
 * 描 述：
 * <p/>
 * <p/>
 * 修订历史：
 * <p/>
 * ========================================
 */
public class FileUtils {


    public static File createFile(String versionName) {
        File cacheDir = getCacheDir();
        cacheDir.mkdirs();
        return new File(cacheDir, "update_v_" + versionName);
    }

    private static File getCacheDir() {
        Context context = UpdateHelper.getInstance().getContext();
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        cacheDir = new File(cacheDir, "update");
        return cacheDir;
    }

    public static String HumanReadableFilesize(double size) {
        String[] units = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};
        double mod = 1024.0;
        int i = 0;
        for (i = 0; size >= mod; i++) {
            size /= mod;
            if (i >= units.length - 1) {
                break;
            }
        }
        try {
            BigDecimal a = new BigDecimal(size + "");
            return a.setScale(2, BigDecimal.ROUND_HALF_UP) + units[i];
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
