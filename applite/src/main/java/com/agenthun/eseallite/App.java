package com.agenthun.eseallite;

import android.app.Application;
import android.os.Vibrator;

import com.agenthun.eseallite.utils.baidumap.LocationService;
import com.baidu.mapapi.SDKInitializer;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/4 上午6:48.
 */
public class App extends Application {

    public LocationService locationService;
    public Vibrator mVibrator;

    @Override
    public void onCreate() {
        super.onCreate();

        /***
         * 初始化定位sdk，建议在Application中创建
         */
        locationService = new LocationService(getApplicationContext());
        mVibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());
    }
}
