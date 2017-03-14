package com.agenthun.eseallite.connectivity.manager.cookie;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.agenthun.eseallite.utils.NetUtil;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * cacheInterceptor
 * Created by Tamic on 2016-08-09.
 */
public class CacheInterceptor implements Interceptor {

    private static final String TAG = "CacheInterceptor";

    private Context context;

    public CacheInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (NetUtil.isConnected(context)) {
            Response response = chain.proceed(request);
            // read from cache for 60 s
            int maxAge = 60;
            String cacheControl = request.cacheControl().toString();
            Log.e(TAG, "60s load cache" + cacheControl);
            return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .build();
        } else {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "当前无网络! 为你智能加载缓存", Toast.LENGTH_SHORT).show();
                }
            });
            Log.e(TAG, " no network load cahe");
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build();
            Response response = chain.proceed(request);
            //set cahe times is 3 days
            int maxStale = 60 * 60 * 24 * 3;
            return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
    }
}
