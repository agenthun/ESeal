package com.agenthun.eseal.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/10/24 01:08.
 */

public class LocationHelper {

    private static final String TAG = "LocationHelper";

    private static LocationHelper locationHelper;

    private LocationCallBack mCallBack = null;

    private Context mContext;
    private LocationManager locationManager;
    private int errorRequestTime = 0;
    private static final int ERROR_REQUEST_TIME_MAX = 15;

    public static synchronized LocationHelper getInstance(Context context) {
        if (locationHelper == null) {
            locationHelper = new LocationHelper(context);
        }
        return locationHelper;
    }

    private LocationHelper(Context context) {
        mContext = context;
        getPermissions(mContext);
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    /***
     * @param callBack
     * @return
     */

    public boolean registerListener(LocationCallBack callBack) {
        boolean isSuccess = false;
        if (callBack != null) {
            mCallBack = callBack;
            isSuccess = true;
        }
        return isSuccess;
    }

    public void unregisterListener() {
        locationManager.removeUpdates(mLocationListener);
        if (mCallBack != null) {
            mCallBack = null;
        }
    }

    public void requestLocation() {
        String provider = LocationManager.GPS_PROVIDER;

        Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

        if (lastKnownLocation == null) {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (lastKnownLocation != null) {
            if (mCallBack != null) {
                mCallBack.onSuccess(lastKnownLocation);
            }
        } else {
            locationManager.requestLocationUpdates(provider, 3000, 0, mLocationListener);
        }
    }

    public void stopRequest() {
        locationManager.removeUpdates(mLocationListener);
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                if (mCallBack != null) {
                    mCallBack.onSuccess(location);
                }
            } else {
                if (++errorRequestTime > ERROR_REQUEST_TIME_MAX) {
                    if (mCallBack != null) {
                        mCallBack.onError();
                    }
                    errorRequestTime = 0;
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            if (mCallBack != null) {
                mCallBack.onError();
            }
        }
    };

    private void getPermissions(Context context) {
        List<String> permissions = new ArrayList<>();

        // 定位为必须权限，用户如果禁止，则每次进入都会申请
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions((Activity) context, permissions.toArray(new String[permissions.size()]), 0);
        }
    }

    /*    public boolean isOpen(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //通过GPS卫星定位,定位级别到街
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //通过WLAN或者移动网络确定位置
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }

    public void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvide");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));

        try {
            //使用PendingIntent发送广播告诉手机去开启GPS功能
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    public void getGPSConfi() {
        Location location;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, mLocationListener);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mLocationListener);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
        } else {
        }
    }*/

    public interface LocationCallBack {
        void onSuccess(Location location); //获取定位成功

        void onError(); //获取定位失败
    }
}
