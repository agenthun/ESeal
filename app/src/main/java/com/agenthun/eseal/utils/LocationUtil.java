package com.agenthun.eseal.utils;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/10/24 01:08.
 */

public class LocationUtil {

    private static final String TAG = "LocationUtil";

    private LocationUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
        } else {
        }
    }*/

    private static LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.d(TAG, "onLocationChanged() returned: " + location.getLatitude() + ", " + location.getLongitude());
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

        }
    };

    public static double[] getLocation(Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);


        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        final double[] res = new double[2];

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (provider == null || provider.equals("")) {
//                startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS));
            }
            provider = LocationManager.NETWORK_PROVIDER;

            Location location = null;
            switch (provider) {
                case "passive":
                    locationManager.requestLocationUpdates(provider, 10, 0, locationListener);
                    location = locationManager.getLastKnownLocation(provider);
                    break;
                case "network":
                    while (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
                    }
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    break;
                case "gps":
                    while (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
                    }
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    break;
            }
            if (location != null) {
//                Log.d(TAG, "getLocation() returned: " + location.getLatitude() + ", " + location.getLongitude());
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            }
        }

        return res;
    }


}
