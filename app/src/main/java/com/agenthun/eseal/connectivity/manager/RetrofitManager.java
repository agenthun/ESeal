package com.agenthun.eseal.connectivity.manager;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.agenthun.eseal.bean.BeidouMasterDeviceInfos;
import com.agenthun.eseal.bean.BleAndBeidouNfcDeviceInfos;
import com.agenthun.eseal.bean.DeviceLocationInfos;
import com.agenthun.eseal.bean.User;
import com.agenthun.eseal.bean.base.BeidouMasterDevice;
import com.agenthun.eseal.bean.base.BleAndBeidouNfcDevice;
import com.agenthun.eseal.bean.base.DeviceLocation;
import com.agenthun.eseal.bean.base.LocationDetail;
import com.agenthun.eseal.bean.base.Result;
import com.agenthun.eseal.bean.updateByRetrofit.UpdateResponse;
import com.agenthun.eseal.connectivity.manager.cookie.CacheInterceptor;
import com.agenthun.eseal.connectivity.manager.cookie.CookieJarManager;
import com.agenthun.eseal.connectivity.service.Api;
import com.agenthun.eseal.connectivity.service.FreightTrackWebService;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.utils.DeviceSearchSuggestion;
import com.agenthun.eseal.utils.LanguageUtil;
import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Url;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/2 下午8:59.
 */
public class RetrofitManager {
    private static final String TAG = "RetrofitManager";

    private static FreightTrackWebService freightTrackWebService;
    private static OkHttpClient mOkHttpClient = null;
    private Cache cache = null;
    private File httpCacheDirectory;
    private Context mContext;

    //创建实例
    public static RetrofitManager builder(PathType pathType) {
        return new RetrofitManager(pathType);
    }

    public static RetrofitManager builder(Context context, PathType pathType) {
        return new RetrofitManager(context, pathType);
    }

    //配置Retrofit
    public RetrofitManager(PathType pathType) {
        if (freightTrackWebService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getPath(pathType))
                    .addConverterFactory(XMLGsonConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            freightTrackWebService = retrofit.create(FreightTrackWebService.class);
        }
    }

    public RetrofitManager(Context context, PathType pathType) {
        mContext = context;
        initOkHttpClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getPath(pathType))
                .client(mOkHttpClient)
                .addConverterFactory(XMLGsonConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        freightTrackWebService = retrofit.create(FreightTrackWebService.class);
    }

    //配置OKHttpClient
    private void initOkHttpClient() {
        if (httpCacheDirectory == null) {
            httpCacheDirectory = new File(mContext.getCacheDir(), "okhttp_cache");
        }

        try {
            if (cache == null) {
                cache = new Cache(httpCacheDirectory, 10 * 1024 * 1024);
            }
        } catch (Exception e) {
            Log.e("OKHttp", "Could not create http cache", e);
        }
        mOkHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .cookieJar(new CookieJarManager(mContext))
                .cache(cache)
                .addInterceptor(new CacheInterceptor(mContext))
                .addNetworkInterceptor(new CacheInterceptor(mContext))
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 10, TimeUnit.SECONDS))
                .build();
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
            case ESeal_UPDATE_SERVICE_URL:
                return Api.ESeal_UPDATE_SERVICE_URL;
            case ESeal_LITE_UPDATE_SERVICE_URL:
                return Api.ESeal_LITE_UPDATE_SERVICE_URL;
        }
        return "";
    }


    //登陆,获取token
    public Observable<User> getTokenObservable(String userName, String password) {
        return freightTrackWebService.getToken(userName, password, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    /**
     * @description 蓝牙锁、北斗终端NFC 配置设备
     */
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

    /**
     * @description 蓝牙锁、北斗终端NFC设备的上封/解封操作
     */
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

    /**
     * @description 蓝牙锁、北斗终端NFC设备访问链路
     */
    //根据Token获取蓝牙锁和BeidouNfc设备的所有在途中的货物信息
    public Observable<BleAndBeidouNfcDeviceInfos> getBleAndBeidouNfcDeviceFreightListObservable(String token) {
        return freightTrackWebService.getBleAndBeidouNfcDeviceFreightList(token, LanguageUtil.getLanguage());
    }


    //根据containerId获取蓝牙锁和BeidouNfc设备的该货物状态列表
    public Observable<DeviceLocationInfos> getBleAndBeidouNfcDeviceLocationObservable(String token, String containerId) {
        return freightTrackWebService.getBleAndBeidouNfcDeviceLocation(token, containerId, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    /**
     * @description 北斗终端帽访问链路
     */
    //根据Token获取北斗终端帽的所有在途中的货物信息
    public Observable<BeidouMasterDeviceInfos> getBeidouMasterDeviceFreightListObservable(String token) {
        return freightTrackWebService.getBeidouMasterDeviceFreightList(token, LanguageUtil.getLanguage());
    }

    //根据Token获取北斗终端帽的所有在途中的货物信息, 返回DeviceSearchSuggestion列表
    public Observable<List<DeviceSearchSuggestion>> getBeidouMasterDeviceFreightListWithFormatObservable(String token) {
        Observable<BeidouMasterDeviceInfos> beidouMasterDeviceInfos = getBeidouMasterDeviceFreightListObservable(token);
        return beidouMasterDeviceInfos
                .map(new Func1<BeidouMasterDeviceInfos, List<DeviceSearchSuggestion>>() {
                    @Override
                    public List<DeviceSearchSuggestion> call(BeidouMasterDeviceInfos beidouMasterDeviceInfos) {
                        List<DeviceSearchSuggestion> result = new ArrayList<>();

                        if (beidouMasterDeviceInfos != null
                                && beidouMasterDeviceInfos.getResult().get(0).getRESULT() == 1) {
                            if (beidouMasterDeviceInfos.getResult().get(0).getICOUNT() == 0) {
                                return result;
                            }
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
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }


    //根据implementID获取该货物状态列表
    public Observable<DeviceLocationInfos> getBeidouMasterDeviceLocationObservable(String token, String implementID) {
        return freightTrackWebService.getBeidouMasterDeviceLocation(token, implementID, LanguageUtil.getLanguage())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    //根据implementID获取北斗终端帽的该货物所选时间段的状态列表
    public Observable<List<LocationDetail>> getBeidouMasterDeviceLocationObservable(String token, String id, String from, String to) {
        Observable<DeviceLocationInfos> deviceLocationInfos = freightTrackWebService
                .getBeidouMasterDeviceLocation(token, id, from, to, LanguageUtil.getLanguage());

        return deviceLocationInfos
                .map(new Func1<DeviceLocationInfos, List<LocationDetail>>() {
                    @Override
                    public List<LocationDetail> call(DeviceLocationInfos deviceLocationInfos) {
                        List<LocationDetail> list = new ArrayList<>();
                        if (deviceLocationInfos != null
                                && deviceLocationInfos.getResult().get(0).getRESULT() == 1) {

                            //查询无数据返回
                            if (deviceLocationInfos.getResult().get(0).getICOUNT() == 0) {
                                return list;
                            }
                            //GPS坐标转百度地图坐标
//                    CoordinateConverter converter = new CoordinateConverter();
//                    converter.from(CoordinateConverter.CoordType.GPS);

                            for (DeviceLocation deviceLocation :
                                    deviceLocationInfos.getDetails()) {
                                String reportTime = deviceLocation.getReportTime();
                                String uploadType = deviceLocation.getUploadType();
                                String securityLevel = deviceLocation.getSecurityLevel();
                                String closedFlag = deviceLocation.getClosedFlag();

                                String coordinate = deviceLocation.getBaiduCoordinate();
                                //去除无效数据
                                if (coordinate.isEmpty() || !coordinate.contains(",")) {
                                    continue;
                                }

                                String[] location = coordinate.split(",");
                                LatLng latLng = new LatLng(
                                        Double.parseDouble(location[0]),
                                        Double.parseDouble(location[1])
                                );
//                        converter.coord(latLng);
//                        latLng = converter.convert();

                                LocationDetail d = new LocationDetail(reportTime,
                                        uploadType,
                                        securityLevel,
                                        closedFlag,
                                        latLng);
                                list.add(d);
                            }
                        }
                        return list;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());

/*
        //构造测试数据
        List<LocationDetail> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            LocationDetail detail = new LocationDetail("2017/02/14 13:14:51", "0", String.valueOf(2), "1", new LatLng(45.6406300000 + Math.cos(i / 39.9f), -73.8472210000 + Math.cos(i / 99.9f)));
            list.add(detail);
        }
        return Observable
                .just(list)
                .delay(3000, TimeUnit.MILLISECONDS);*/
    }

    //根据implementID获取北斗终端帽的该设备的最新货物状态
    public Observable<LocationDetail> getBeidouMasterDeviceLastLocationObservable(String token, String id) {
        Observable<DeviceLocationInfos> deviceLocationInfos = freightTrackWebService.getBeidouMasterDeviceLastLocation(token, id, LanguageUtil.getLanguage());

        return deviceLocationInfos
                .map(new Func1<DeviceLocationInfos, LocationDetail>() {
                    @Override
                    public LocationDetail call(DeviceLocationInfos deviceLocationInfos) {
                        if (deviceLocationInfos != null
                                && deviceLocationInfos.getResult().get(0).getRESULT() == 1) {
                            DeviceLocation deviceLocation = deviceLocationInfos.getDetails().get(0); //最新位置点

                            //GPS坐标转百度地图坐标
//                    CoordinateConverter converter = new CoordinateConverter();
//                    converter.from(CoordinateConverter.CoordType.GPS);

                            String reportTime = deviceLocation.getReportTime();
                            String uploadType = deviceLocation.getUploadType();
                            String securityLevel = deviceLocation.getSecurityLevel();
                            String closedFlag = deviceLocation.getClosedFlag();
                            String[] location = deviceLocation.getBaiduCoordinate().split(",");
                            LatLng latLng = new LatLng(
                                    Double.parseDouble(location[0]),
                                    Double.parseDouble(location[1])
                            );
//                    converter.coord(latLng);
//                    latLng = converter.convert();

                            LocationDetail d = new LocationDetail(reportTime,
                                    uploadType,
                                    securityLevel,
                                    closedFlag,
                                    latLng);

                            return d;
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());

/*        //构造测试数据
        return Observable
                .just(new LocationDetail("2017/02/14 13:14:51", "0", "1", "1", new LatLng(45.6406300000, -73.8472210000)))
                .delay(500, TimeUnit.MILLISECONDS);*/
    }

    /**
     * @description 获取所有终端(蓝牙锁、北斗终端NFC、北斗终端帽)的货物信息
     * 返回DeviceSearchSuggestion列表
     */
    //根据Token获取所有终端的货物信息, 返回DeviceSearchSuggestion列表
    public Observable<List<DeviceSearchSuggestion>> getAllDeviceFreightListObservable(String token) {
        Observable<BleAndBeidouNfcDeviceInfos> bleAndBeidouNfcDeviceInfos = getBleAndBeidouNfcDeviceFreightListObservable(token);
        Observable<BeidouMasterDeviceInfos> beidouMasterDeviceInfos = getBeidouMasterDeviceFreightListObservable(token);

        return Observable.zip(bleAndBeidouNfcDeviceInfos, beidouMasterDeviceInfos, new Func2<BleAndBeidouNfcDeviceInfos, BeidouMasterDeviceInfos, List<DeviceSearchSuggestion>>() {
            @Override
            public List<DeviceSearchSuggestion> call(BleAndBeidouNfcDeviceInfos bleAndBeidouNfcDeviceInfos, BeidouMasterDeviceInfos beidouMasterDeviceInfos) {
                List<DeviceSearchSuggestion> result = new ArrayList<DeviceSearchSuggestion>();

                if (bleAndBeidouNfcDeviceInfos != null &&
                        bleAndBeidouNfcDeviceInfos.getResult().get(0).getRESULT() == 1) {
                    List<BleAndBeidouNfcDevice> details = bleAndBeidouNfcDeviceInfos.getDetails();

                    if (details != null && !details.isEmpty()) {
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
                }

                if (beidouMasterDeviceInfos != null
                        && beidouMasterDeviceInfos.getResult().get(0).getRESULT() == 1) {
                    List<BeidouMasterDevice> details = beidouMasterDeviceInfos.getDetails();
                    if (details != null && !details.isEmpty()) {
                        for (BeidouMasterDevice detail :
                                details) {
                            Log.d(TAG, "getBeidouMasterDevice(): " + detail.toString());
                            DeviceSearchSuggestion suggestion = new DeviceSearchSuggestion(detail);
                            result.add(suggestion);
                        }
                    }
                }

                return result;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    /**
     * @description 版本检测更新
     */
    //APP 版本检测更新
    public Observable<UpdateResponse.Entity> checkAppUpdateObservable() {
        Observable<UpdateResponse> response = freightTrackWebService.checkAppUpdate();
        return response.map(new Func1<UpdateResponse, UpdateResponse.Entity>() {
            @Override
            public UpdateResponse.Entity call(UpdateResponse updateResponse) {
                if (updateResponse == null) {
                    return null;
                }
                if (updateResponse.getError() == null || updateResponse.getError().getResult() != 1) {
                    return null;
                }
                if (updateResponse.getEntity() != null) {
                    return updateResponse.getEntity();
                }
                return null;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());

        //构造测试数据
/*        String testDownloadUrl = "http://www.freight-track.com/files/ESeal_Lite_v_1_0_3_2017-02-25.apk";
        return Observable
                .just(new UpdateResponse.Entity(12537361, false,
                        "1.谷歌地图更新\r\n2.UI更新",
                        testDownloadUrl,
                        100,
                        "x.0.x"))
                .delay(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());*/
    }

    //APP Lite 版本检测更新
    public Observable<UpdateResponse.Entity> checkAppLiteUpdateObservable() {
        Observable<UpdateResponse> response = freightTrackWebService.checkAppLiteUpdate();
        return response.map(new Func1<UpdateResponse, UpdateResponse.Entity>() {
            @Override
            public UpdateResponse.Entity call(UpdateResponse updateResponse) {
                if (updateResponse == null) {
                    return null;
                }
                if (updateResponse.getError() == null || updateResponse.getError().getResult() != 1) {
                    return null;
                }
                if (updateResponse.getEntity() != null) {
                    return updateResponse.getEntity();
                }
                return null;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());

        //构造测试数据
/*        String testDownloadUrl = "http://www.freight-track.com/files/ESeal_Lite_v_1_0_3_2017-02-25.apk";
        return Observable
                .just(new UpdateResponse.Entity(12537361, false,
                        "1.谷歌地图更新\r\n2.UI更新",
                        testDownloadUrl,
                        100,
                        "x.0.x"))
                .delay(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());*/
    }

    /**
     * @description 下载文件
     */
    //下载文件
    public Observable<ResponseBody> downloadFileObservable(@Url String fileUrl) {
        return freightTrackWebService.downloadFile(fileUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io());
    }

    public void downloadFileObservable(@Url String fileUrl, String fileName, DownloadCallBack callBack) {
        freightTrackWebService.downloadFile(fileUrl)
                .compose(schedulersTransformer())
                .subscribe(new DownloadSubscriber<ResponseBody>(mContext, fileName, callBack));
    }

    /**
     * @description 进程调度
     */
    Observable.Transformer schedulersTransformer() {
        return new Observable.Transformer() {
            @Override
            public Object call(Object o) {
                return ((Observable) o).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .unsubscribeOn(Schedulers.io());
            }
        };
    }
}
