package com.agenthun.eseal.connectivity.service;

import android.support.annotation.Nullable;

import com.agenthun.eseal.bean.BeidouMasterDeviceInfos;
import com.agenthun.eseal.bean.BleAndBeidouNfcDeviceInfos;
import com.agenthun.eseal.bean.DeviceLocationInfos;
import com.agenthun.eseal.bean.User;
import com.agenthun.eseal.bean.base.Result;
import com.agenthun.eseal.bean.updateByRetrofit.UpdateResponse;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/2 上午11:02.
 */
public interface FreightTrackWebService {

    //登陆，获取Token
    @GET("GetTokenByUserNameAndPassword")
    Observable<User> getToken(
            @Query("userName") String userName,
            @Query("password") String password,
            @Query("language") String language);

    /**
     * @description 蓝牙锁、北斗终端NFC 配置设备
     */
    //配置终端货物信息参数
    @GET("ConfigureCargo")
    Observable<Result> configureDevice(
            @Query("token") String token,
            @Nullable @Query("DeviceType") String deviceType,
            @Nullable @Query("implementID") String implementID,
            @Nullable @Query("containerNo") String containerNo,
            @Nullable @Query("freightOwner") String freightOwner,
            @Nullable @Query("freightName") String freightName,
            @Nullable @Query("origin") String origin,
            @Nullable @Query("destination") String destination,
            @Nullable @Query("VesselName") String VesselName,
            @Nullable @Query("voyage") String voyage,
            @Nullable @Query("frequency") String frequency,
            @Query("RFID") String RFID,
            @Nullable @Query("images") String images,
            @Nullable @Query("coordinate") String coordinate,
            @Query("operateTime") String operateTime,
            @Query("language") String language);

    /**
     * @description 蓝牙锁、北斗终端NFC设备的上封/解封操作
     */
    //解封、开箱操作 - 获取MAC
    @GET("OpenContainer")
    Observable<Result> openDevice(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("RFID") String RFID,
            @Nullable @Query("images") String images,
            @Nullable @Query("coordinate") String coordinate,
            @Query("operateTime") String operateTime,
            @Query("language") String language);

    //上封、关箱操作(海关 / 普通用户) - 获取MAC
    @GET("CloseContainer")
    Observable<Result> closeDevice(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("RFID") String RFID,
            @Nullable @Query("images") String images,
            @Nullable @Query("coordinate") String coordinate,
            @Query("operateTime") String operateTime,
            @Query("language") String language);


    /**
     * @description 蓝牙锁、北斗终端NFC设备访问链路
     */
    //根据Token获取蓝牙锁和BeidouNfc设备的所有在途中的货物信息
    @GET("GetFreightInfoByToken")
    Observable<BleAndBeidouNfcDeviceInfos> getBleAndBeidouNfcDeviceFreightList(
            @Query("token") String token,
            @Query("language") String language);

    //根据containerId获取蓝牙锁和BeidouNfc设备的该货物状态列表
    @GET("GetAllBaiduCoordinateByContainerId")
    Observable<DeviceLocationInfos> getBleAndBeidouNfcDeviceLocation(
            @Query("token") String token,
            @Query("containerId") String containerId,
            @Query("language") String language);

    /**
     * @description 北斗终端帽访问链路
     */
    //根据Token获取北斗终端帽的所有在途中的货物信息
    @GET("GetAllImplement")
    Observable<BeidouMasterDeviceInfos> getBeidouMasterDeviceFreightList(
            @Query("token") String token,
            @Query("language") String language);

    //根据implementID获取该货物状态列表
    @GET("GetImplementPositionInfoByID")
    Observable<DeviceLocationInfos> getBeidouMasterDeviceLocation(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("language") String language);

    //根据implementID获取北斗终端帽的该货物所选时间段的状态列表
    @GET("GetImplementPositionInfoByIDAndTime")
    Observable<DeviceLocationInfos> getBeidouMasterDeviceLocation(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("startTime") String startTime,
            @Query("endTime") String endTime,
            @Query("language") String language);

    //根据implementID获取北斗终端帽的该设备的最新货物状态
    @GET("GetLastImplementData")
    Observable<DeviceLocationInfos> getBeidouMasterDeviceLastLocation(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("language") String language);

    /**
     * @description 版本检测更新
     */
    //APP 版本检测更新
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @GET(Api.ESeal_UPDATE_SERVICE_URL)
    Observable<UpdateResponse> checkAppUpdate();

    //APP Lite 版本检测更新
    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @GET(Api.ESeal_LITE_UPDATE_SERVICE_URL)
    Observable<UpdateResponse> checkAppLiteUpdate();

    /**
     * @description 下载文件
     */
    //下载APK文件
    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@Url String fileUrl);

    @Streaming
    @GET
    Observable<Response<ResponseBody>> downloadFile(@Header("Range") String range, @Url String fileUrl);
}
