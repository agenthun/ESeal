package com.agenthun.eseal;

import android.app.Application;
import android.content.Context;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/4 上午6:48.
 */
public class App extends Application {
    private static Context mApplicationContext;
    private static String token;
    private static String tagId;

    @Override

    public void onCreate() {
        super.onCreate();
        mApplicationContext = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static Context getContext() {
        return mApplicationContext;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        App.token = token;
    }

    public static String getTagId() {
        return tagId;
    }

    public static void setTagId(String tagId) {
        App.tagId = tagId;
    }
}
