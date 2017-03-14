package com.pekingopera.versionupdate.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.pekingopera.versionupdate.UpdateHelper;

/**
 * @author Administrator
 */
public class NetworkUtil {

    /**
     * Whether or not to connect to the Internet
     */
    public static boolean isConnected () {
        NetworkInfo info = getNetworkInfos();
        if (info == null || !info.isConnected()) {
            return false;
        }
        return true;
    }

    /**
     * Determine whether to use wifi
     */
    public static boolean isConnectedByWifi() {
        NetworkInfo info = getNetworkInfos();
        return info != null
                && info.isConnected()
                && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    static NetworkInfo getNetworkInfos() {
        Context context = UpdateHelper.getInstance().getContext();
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getActiveNetworkInfo();
    }
}
