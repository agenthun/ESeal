package com.agenthun.eseal.connectivity.manager;

import android.support.annotation.Nullable;

import com.agenthun.eseal.App;
import com.agenthun.eseal.bean.FreightInfos;
import com.agenthun.eseal.bean.LocationInfos;
import com.agenthun.eseal.bean.MACByOpenCloseContainer;
import com.agenthun.eseal.bean.UserInfoByGetToken;
import com.agenthun.eseal.bean.base.BaseWebServiceResponseBody;
import com.agenthun.eseal.bean.base.Result;
import com.agenthun.eseal.connectivity.service.Api;
import com.agenthun.eseal.connectivity.service.FreightTrackWebService;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.utils.LanguageUtil;
import com.agenthun.eseal.utils.NetUtil;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/2 下午8:59.
 */
public class RetrofitManager {

    //设缓存有效期为一天
    protected static final long CACHE_STALE_SEC = 60 * 60 * 24;
    //查询缓存的Cache-Control设置，为if-only-cache时只查询缓存而不会请求服务器，max-stale可以配合设置缓存失效时间
    protected static final String CACHE_CONTROL_CACHE = "only-if-cached, max-stale=" + CACHE_STALE_SEC;
    //查询网络的Cache-Control设置，头部Cache-Control设为max-age=0时则不会使用缓存而请求服务器
    protected static final String CACHE_CONTROL_NETWORK = "max-age=0";

    public static final String TOKEN = "TOKEN";

    private final FreightTrackWebService freightTrackWebService;
    private static OkHttpClient mOkHttpClient;


    //创建实例
    public static RetrofitManager builder(PathType pathType) {
        return new RetrofitManager(pathType);
    }

    //配置Retrofit
    public RetrofitManager(PathType pathType) {
//        initOkHttpClient();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getPath(pathType))
//                .client(mOkHttpClient)
                .addConverterFactory(XMLGsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        freightTrackWebService = retrofit.create(FreightTrackWebService.class);
    }

    //配置OKHttpClient
    private void initOkHttpClient() {
        if (mOkHttpClient == null) {
            synchronized (RetrofitManager.class) {
                if (mOkHttpClient == null) {
                    File cacheFile = new File(App.getContext().getCacheDir(), "HttpCache");
                    Cache cache = new Cache(cacheFile, 1024 * 1024 * 10);
                    Interceptor interceptor = new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request request = chain.request();
                            if (!NetUtil.isConnected(App.getContext())) {
                                request = request.newBuilder()
                                        .cacheControl(CacheControl.FORCE_CACHE)
                                        .build();
                            }
                            Response response = chain.proceed(request);
                            if (NetUtil.isConnected(App.getContext())) {
                                String cacheControl = request.cacheControl().toString();
                                return response.newBuilder()
                                        .header("Cache-Control", cacheControl)
                                        .removeHeader("Pragma").build();
                            } else {
                                return response.newBuilder()
                                        .header("Cache-Control", "public, only-if-cache, " + CACHE_STALE_SEC)
                                        .removeHeader("Pragma").build();
                            }
                        }
                    };
                    mOkHttpClient = new OkHttpClient.Builder().cache(cache)
                            .addNetworkInterceptor(interceptor)
                            .addInterceptor(interceptor)
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
    }

    //获取相应的web路径
    private String getPath(PathType pathType) {
        switch (pathType) {
            case BASE_WEB_SERVICE:
                return Api.K_API_BASE_URL_STRING;
            case AMAP_SERVICE:
                return Api.AMAP_SERVICE_URL_STRING;
            case WEB_SERVICE_V2_TEST:
                return Api.WEB_SERVICE_V2_TEST;
            case WEB_SERVICE_V2_RELEASE:
                return Api.WEB_SERVICE_V2_RELEASE;
            case MAP_SERVICE_V2_TEST:
                return Api.MAP_SERVICE_V2_URL_STRING;
        }
        return "";
    }

    public static String getCacheControlCache() {
        return NetUtil.isConnected(App.getContext()) ? CACHE_CONTROL_NETWORK : CACHE_CONTROL_CACHE;
    }


    //登陆,获取token
    public Observable<UserInfoByGetToken> getTokenObservable(String userName, String password) {
        return freightTrackWebService.userInfoByGetToken(userName, password, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //配置终端货物信息参数
    public Observable<Result> configureDevice(String token, String implementID,
                                              @Nullable String containerNo, @Nullable String freightOwner, @Nullable String freightName, @Nullable String origin, @Nullable String destination, @Nullable String VesselName, @Nullable String voyage,
                                              @Nullable String frequency,
                                              String RFID,
                                              @Nullable String images,
                                              @Nullable String coordinate,
                                              String operateTime) {
        return freightTrackWebService
                .configureCargo(token, implementID,
                        containerNo, freightOwner, freightName, origin, destination, VesselName, voyage,
                        frequency,
                        RFID,
                        images,
                        coordinate,
                        operateTime,
                        LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //开箱操作 - 获取MAC implementID="12345678"
    public Observable<MACByOpenCloseContainer> getMACByOpenOperationObservable(String token, String implementID, String RFID, @Nullable String images, @Nullable String coordinate, String operateTime) {
        return freightTrackWebService.openContainer(token, implementID, RFID, images, coordinate, operateTime, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //关箱操作 - 获取MAC implementID="12345678"
    public Observable<MACByOpenCloseContainer> getMACByCloseOperationObservable(String token, String implementID, String RFID, @Nullable String images, @Nullable String coordinate, String operateTime) {
        return freightTrackWebService.closeContainer(token, implementID, RFID, images, coordinate, operateTime, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }


/*    //获取集装箱数据列表 containerId=1070, currentPageIndex=1
    public Call<AllDynamicDataByContainerId> getFreightDataListObservable(final String token, final String containerId, final Integer currentPageIndex) {
        return freightTrackWebService.getAllDynamicData(token, containerId, currentPageIndex, LanguageUtil.getLanguage());
*//*                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());*//*
    }*/

    /**
     * @description 蓝牙锁访问链路
     */
    //根据Token获取所有在途中的货物设置信息
    public Observable<FreightInfos> getBleDeviceFreightListObservable(String token) {
        return freightTrackWebService.getBleDeviceFreightList(token, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //根据containerId获取该货物状态列表
    public Observable<LocationInfos> getBleDeviceLocationObservable(String token, String containerId) {
        return freightTrackWebService.getBleDeviceLocation(token, containerId, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }


    /**
     * @description 北斗终端帽访问链路
     */
    //根据Token获取所有在途中的货物设置信息
    public Observable<FreightInfos> getBeidouMasterDeviceFreightListObservable(String token) {
        return freightTrackWebService.getBeidouMasterDeviceFreightList(token, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //根据implementID获取该货物状态列表
    public Observable<LocationInfos> getBeidouMasterDeviceLocationObservable(String token, String implementID) {
        return freightTrackWebService.getBeidouMasterDeviceLocation(token, implementID, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }


    /**
     * @description 北斗终端NFC访问链路
     */
    //根据Token获取所有在途中的货物设置信息
    public Observable<FreightInfos> getBeidouNfcDeviceFreightListObservable(String token) {
        return freightTrackWebService.getBeidouNfcDeviceFreightList(token, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //根据NFCId获取该货物状态列表
    public Observable<LocationInfos> getBeidouNfcDeviceLocationObservable(String token, String nfcId) {
        return freightTrackWebService.getBeidouNfcDeviceLocation(token, nfcId, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }
}
