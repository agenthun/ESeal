package com.agenthun.eseal;

import android.app.Application;
import android.content.Context;
import android.os.Vibrator;

import com.agenthun.eseal.utils.baidumap.LocationService;
import com.agenthun.eseal.utils.update.UpdateConfig;
import com.baidu.mapapi.SDKInitializer;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/4 上午6:48.
 */
public class App extends Application {
    private static Context mApplicationContext;
    private static String token;
    //    private static String tagId = "00000000000000";
    private static String tagId = "043B88F2994080";
    private static String deviceId = "13003";

    public static final String GOOGLE_MAP_API_KEY = "AIzaSyBy5WtHdZ7Pbe-A2N57Kbf7iR0OIgo3yuY";

    public LocationService locationService;
    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplicationContext = this;

        /***
         * 初始化版本升级模块
         */
        UpdateConfig.initGet(this);

        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());
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

    public static String getDeviceId() {
        return deviceId;
    }

    public static void setDeviceId(String deviceId) {
        App.deviceId = deviceId;
    }
}
