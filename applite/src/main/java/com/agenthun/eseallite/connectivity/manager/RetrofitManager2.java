package com.agenthun.eseallite.connectivity.manager;

import android.support.annotation.Nullable;
import android.util.Log;

import com.agenthun.eseallite.bean.BeidouMasterDeviceInfos;
import com.agenthun.eseallite.bean.BleAndBeidouNfcDeviceInfos;
import com.agenthun.eseallite.bean.DeviceLocationInfos;
import com.agenthun.eseallite.bean.User;
import com.agenthun.eseallite.bean.base.BeidouMasterDevice;
import com.agenthun.eseallite.bean.base.BleAndBeidouNfcDevice;
import com.agenthun.eseallite.bean.base.LocationDetail;
import com.agenthun.eseallite.bean.base.Result;
import com.agenthun.eseallite.connectivity.service.Api;
import com.agenthun.eseallite.connectivity.service.FreightTrackWebService;
import com.agenthun.eseallite.connectivity.service.PathType;
import com.agenthun.eseallite.utils.DeviceSearchSuggestion;
import com.agenthun.eseallite.utils.LanguageUtil;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/2 下午8:59.
 */
public class RetrofitManager2 {

    private static final String TAG = "RetrofitManager2";
    //设缓存有效期为一天
    protected static final long CACHE_STALE_SEC = 60 * 60 * 24;
    //查询缓存的Cache-Control设置，为if-only-cache时只查询缓存而不会请求服务器，max-stale可以配合设置缓存失效时间
    protected static final String CACHE_CONTROL_CACHE = "only-if-cached, max-stale=" + CACHE_STALE_SEC;

    //查询网络的Cache-Control设置，头部Cache-Control设为max-age=0时则不会使用缓存而请求服务器
    protected static final String CACHE_CONTROL_NETWORK = "max-age=0";
    public static final String TOKEN = "TOKEN";

    private static FreightTrackWebService freightTrackWebService;
    private static OkHttpClient mOkHttpClient;


    //创建实例
    public static RetrofitManager2 builder(PathType pathType) {
        return new RetrofitManager2(pathType);
    }

    //配置Retrofit
    public RetrofitManager2(PathType pathType) {
//        initOkHttpClient();
        if (freightTrackWebService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getPath(pathType))
//                .client(mOkHttpClient)
                    .addConverterFactory(XMLGsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            freightTrackWebService = retrofit.create(FreightTrackWebService.class);
        }
    }

    //配置OKHttpClient
    private void initOkHttpClient() {
        if (mOkHttpClient == null) {
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
//        return NetUtil.isConnected(App.getContext()) ? CACHE_CONTROL_NETWORK : CACHE_CONTROL_CACHE;
        return CACHE_CONTROL_NETWORK;
    }


    //获取freightTrackWebService
    public FreightTrackWebService getFreightTrackWebService() {
        return freightTrackWebService;
    }

    //登陆,获取token
    public Observable<User> getTokenObservable(String userName, String password) {
        return freightTrackWebService.getToken(userName, password, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //配置终端货物信息参数
    public Observable<Result> configureDeviceObservable(String token, @Nullable String deviceType, @Nullable String implementID,
                                                        @Nullable String containerNo, @Nullable String freightOwner, @Nullable String freightName, @Nullable String origin, @Nullable String destination, @Nullable String VesselName, @Nullable String voyage,
                                                        @Nullable String frequency,
                                                        String RFID,
                                                        @Nullable String images,
                                                        @Nullable String coordinate,
                                                        String operateTime) {
        return freightTrackWebService
                .configureDevice(token, deviceType, implementID,
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
    public Observable<Result> openDeviceObservable(String token, String implementID, String RFID, @Nullable String images, @Nullable String coordinate, String operateTime) {
        return freightTrackWebService.openDevice(token, implementID, RFID, images, coordinate, operateTime, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //关箱操作 - 获取MAC implementID="12345678"
    public Observable<Result> closeDeviceObservable(String token, String implementID, String RFID, @Nullable String images, @Nullable String coordinate, String operateTime) {
        return freightTrackWebService.closeDevice(token, implementID, RFID, images, coordinate, operateTime, LanguageUtil.getLanguage())
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
    public Observable<BleAndBeidouNfcDeviceInfos> getBleDeviceFreightListObservable(String token) {
        return freightTrackWebService.getBleDeviceFreightList(token, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //根据containerId获取该货物状态列表
    public Observable<DeviceLocationInfos> getBleDeviceLocationObservable(String token, String containerId) {
        return freightTrackWebService.getBleDeviceLocation(token, containerId, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }


    /**
     * @description 北斗终端帽访问链路
     */
    //根据Token获取所有在途中的货物设置信息
    public Observable<BeidouMasterDeviceInfos> getBeidouMasterDeviceFreightListObservable(String token) {
        return freightTrackWebService.getBeidouMasterDeviceFreightList(token, LanguageUtil.getLanguage());
    }

    //根据implementID获取该货物状态列表
    public Observable<DeviceLocationInfos> getBeidouMasterDeviceLocationObservable(String token, String implementID) {
        return freightTrackWebService.getBeidouMasterDeviceLocation(token, implementID, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }


    /**
     * @description 北斗终端NFC访问链路
     */
    //根据Token获取所有在途中的货物设置信息
    public Observable<BleAndBeidouNfcDeviceInfos> getBleAndBeidouNfcDeviceFreightListObservable(String token) {
        return freightTrackWebService.getBleAndBeidouNfcDeviceFreightList(token, LanguageUtil.getLanguage());
    }

    public Observable<BleAndBeidouNfcDeviceInfos> getBeidouNfcDeviceFreightListObservable(String token) {
        return freightTrackWebService.getBeidouNfcDeviceFreightList(token, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //根据NFCId获取该货物状态列表
    public Observable<DeviceLocationInfos> getBeidouNfcDeviceLocationObservable(String token, String nfcId) {
        return freightTrackWebService.getBeidouNfcDeviceLocation(token, nfcId, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    /**
     * @description 获取所有终端货物信息
     */
    //根据Token获取所有在途中的货物设置信息
    public Observable<List<DeviceSearchSuggestion>> getAllDeviceFreightListObservable(String token) {
        Observable<BleAndBeidouNfcDeviceInfos> bleAndBeidouNfcDeviceInfos = RetrofitManager2.builder(PathType.WEB_SERVICE_V2_TEST)
                .getBleAndBeidouNfcDeviceFreightListObservable(token);
        Observable<BeidouMasterDeviceInfos> beidouMasterDeviceInfos = RetrofitManager2.builder(PathType.WEB_SERVICE_V2_TEST)
                .getBeidouMasterDeviceFreightListObservable(token);

        return Observable.zip(bleAndBeidouNfcDeviceInfos, beidouMasterDeviceInfos, new Func2<BleAndBeidouNfcDeviceInfos, BeidouMasterDeviceInfos, List<DeviceSearchSuggestion>>() {
            @Override
            public List<DeviceSearchSuggestion> call(BleAndBeidouNfcDeviceInfos bleAndBeidouNfcDeviceInfos, BeidouMasterDeviceInfos beidouMasterDeviceInfos) {
                List<DeviceSearchSuggestion> result = new ArrayList<DeviceSearchSuggestion>();

                if (bleAndBeidouNfcDeviceInfos != null &&
                        bleAndBeidouNfcDeviceInfos.getResult().get(0).getRESULT() == 1) {
                    List<BleAndBeidouNfcDevice> details = bleAndBeidouNfcDeviceInfos.getDetails();
                    for (BleAndBeidouNfcDevice detail :
                            details) {
                        Log.d(TAG, "getBleAndBeidouNfcDevice(): " + detail.toString());
                        if (detail.getDeviceType().equals(Api.DEVICE_TYPE_BLE)) {
                            DeviceSearchSuggestion suggestion = new DeviceSearchSuggestion(detail,
                                    DeviceSearchSuggestion.DEVICE_BLE);
                            result.add(suggestion);
                        } else if (detail.getDeviceType().equals(Api.DEVICE_TYPE_BEIDOU_NFC)) {
                            DeviceSearchSuggestion suggestion = new DeviceSearchSuggestion(detail,
                                    DeviceSearchSuggestion.DEVICE_BEIDOU_NFC);
                            result.add(suggestion);
                        }
                    }
                }

                if (beidouMasterDeviceInfos != null
                        && beidouMasterDeviceInfos.getResult().get(0).getRESULT() == 1) {
                    List<BeidouMasterDevice> details = beidouMasterDeviceInfos.getDetails();
                    for (BeidouMasterDevice detail :
                            details) {
                        Log.d(TAG, "getBeidouMasterDevice(): " + detail.toString());
                        DeviceSearchSuggestion suggestion = new DeviceSearchSuggestion(detail);
                        result.add(suggestion);
                    }
                }

                return result;
            }
        });
    }

    //根据Token获取该货物的最新位置状态
    public Observable<LocationDetail> getFreightLocationObservable(String token, String id) {
//        Observable<DeviceLocationInfos> deviceLocationInfos = RetrofitManager2.builder(PathType.WEB_SERVICE_V2_TEST)
//                .getBleDeviceLocationObservable(token, id);
//
//        return deviceLocationInfos.map(new Func1<DeviceLocationInfos, LocationDetail>() {
//            @Override
//            public LocationDetail call(DeviceLocationInfos deviceLocationInfos) {
//                return new LocationDetail("2017/2/14 13:14", "0", new LatLng(40.6406300000, -73.8472210000));
//            }
//        });

        return Observable
                .just(new LocationDetail("2017/02/14 13:14:51", "0", new LatLng(45.6406300000, -73.8472210000)))
                .delay(500, TimeUnit.MILLISECONDS);
    }

    //根据Token获取该货物所选时间段的位置状态列表
    public Observable<List<LocationDetail>> getFreightLocationListObservable(String token, String id, String from, String to) {
//        Observable<DeviceLocationInfos> deviceLocationInfos = RetrofitManager2.builder(PathType.WEB_SERVICE_V2_TEST)
//                .getBleDeviceLocationObservable(token, id);
//
//        return deviceLocationInfos.map(new Func1<DeviceLocationInfos, LocationDetail>() {
//            @Override
//            public LocationDetail call(DeviceLocationInfos deviceLocationInfos) {
//                return new LocationDetail("2017/2/14 13:14", "0", new LatLng(40.6406300000, -73.8472210000));
//            }
//        });

        List<LocationDetail> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            LocationDetail detail = new LocationDetail("2017/02/14 13:14:51", "0", new LatLng(45.6406300000 + Math.cos(i / 39.9f), -73.8472210000 + Math.cos(i / 99.9f)));
            list.add(detail);
        }
        return Observable
                .just(list)
                .delay(3000, TimeUnit.MILLISECONDS);
    }
}
