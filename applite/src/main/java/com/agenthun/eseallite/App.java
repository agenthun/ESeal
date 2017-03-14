package com.agenthun.eseallite;

import android.app.Application;

import com.agenthun.eseallite.utils.update.UpdateConfig;


/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/4 上午6:48.
 */
public class App extends Application {

    public static final String GOOGLE_MAP_API_KEY = "AIzaSyBy5WtHdZ7Pbe-A2N57Kbf7iR0OIgo3yuY";

    @Override
    public void onCreate() {
        super.onCreate();
        UpdateConfig.initGet(this);
    }

}