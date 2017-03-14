package com.agenthun.eseal.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 跟网络相关的工具类
 *
 * @author zhy
 */
public class NetUtil {
    private NetUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 判断网络是否连接
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != connectivity) {

            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {

                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是wifi连接
     */
    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null)
            return false;
        return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;

    }

    /**
     * 打开网络设置界面
     */
    public static void openSetting(Activity activity) {
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent, 0);
    }

    /**
     * 获取所有搜索的WiFi的SSID
     */
    public static List<String> getScanWifiSSIDList(Activity activity) {
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<String> result = new ArrayList<>();
        for (ScanResult scanResult :
                wifiManager.getScanResults()) {
            result.add(scanResult.SSID);
        }
        return result;
    }

    /**
     * 获取WiFi信息
     */
    public static WifiInfo getWifiInfo(Activity activity) {
        WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConnectionInfo();
    }

    /**
     * 获取WiFi MAC地址
     */
    public static String getWifiMacAddress(Activity activity) {
        return getWifiInfo(activity).getMacAddress();
    }

    /**
     * 获取WiFi SSID
     */
    public static String getWifiSSID(Activity activity) {
        return getWifiInfo(activity).getSSID();
    }

    /**
     * 获取WiFi 网络信号强度,不是信噪比!
     */
    public static int getWifiRssi(Activity activity) {
        return getWifiInfo(activity).getRssi();
    }


    /**
     * 获取手机基站运行商
     */
    public static String getTelephonyNetWorkOperator(Activity activity) {
        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperator();
    }

    /**
     * 获取手机基站运行商的MCC (Mobile Country Code)
     */
    public static String getTelephonyNetWorkOperatorMcc(Activity activity) {
        return getTelephonyNetWorkOperator(activity).substring(0, 3);
    }

    /**
     * 获取手机基站运行商的MNC (Mobile Network Code)
     */
    public static String getTelephonyNetWorkOperatorMnc(Activity activity) {
        return getTelephonyNetWorkOperator(activity).substring(3);
    }

    /**
     * 获取手机基站的LAC (Location Aera Code)
     */
    public static int getCellLocationLac(Activity activity) {
        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
        return location.getLac();
    }

    /**
     * 获取手机基站的CID (Cell Tower ID)
     */
    public static int getCellLocationCid(Activity activity) {
        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation location = (GsmCellLocation) telephonyManager.getCellLocation();
        return location.getCid();
    }
}
