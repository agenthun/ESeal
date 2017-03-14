package com.pekingopera.versionupdate.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.pekingopera.versionupdate.UpdateHelper;

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
 * 创建日期：2016/6/14
 * <p>
 * 描 述：
 * <p>
 * <p>
 * 修订历史：
 * <p>
 * ========================================
 */
public class UpdateSP {


    public static final String KEY_DOWN_SIZE = "update_download_size";

    public static boolean isIgnore(String version) {
        SharedPreferences sp = UpdateHelper.getInstance().getContext().getSharedPreferences(KEY_DOWN_SIZE, Context.MODE_PRIVATE);
        return sp.getString("_update_version_ignore", "").equals(version);
    }

    public static boolean isForced() {
        SharedPreferences sp = UpdateHelper.getInstance().getContext().getSharedPreferences(KEY_DOWN_SIZE, Context.MODE_PRIVATE);
        return sp.getBoolean("_update_version_forced", false);
    }

    public static int getDialogLayout() {
        SharedPreferences sp = UpdateHelper.getInstance().getContext().getSharedPreferences(KEY_DOWN_SIZE, Context.MODE_PRIVATE);
        return sp.getInt("_update_version_layout_id", 0);
    }

    public static void setIgnore(String version) {
        SharedPreferences sp = UpdateHelper.getInstance().getContext().getSharedPreferences(KEY_DOWN_SIZE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("_update_version_ignore", version);
        editor.commit();
    }

    public static void setForced(boolean def) {
        SharedPreferences sp = UpdateHelper.getInstance().getContext().getSharedPreferences(KEY_DOWN_SIZE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("_update_version_forced", def);
        editor.commit();
    }

    public static void setDialogLayout(int def) {
        SharedPreferences sp = UpdateHelper.getInstance().getContext().getSharedPreferences(KEY_DOWN_SIZE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("_update_version_layout_id", def);
        editor.commit();
    }
}
