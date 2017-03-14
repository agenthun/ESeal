package com.agenthun.eseallite.connectivity.manager.cookie;

import android.content.Context;

import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/23 14:15.
 */

public class CookieJarManager implements CookieJar {
    private static Context mContext;
    private static PersistentCookieStore cookieStore;

    public CookieJarManager(Context context) {
        mContext = context;
        if (cookieStore == null) {
            cookieStore = new PersistentCookieStore(mContext);
        }
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies != null && cookies.size() > 0) {
            for (Cookie item : cookies) {
                cookieStore.add(url, item);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url);
        return cookies;
    }
}
