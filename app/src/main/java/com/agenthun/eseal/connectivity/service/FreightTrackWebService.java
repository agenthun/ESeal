package com.agenthun.eseal.connectivity.service;

import android.support.annotation.Nullable;

import com.agenthun.eseal.bean.FreightInfos;
import com.agenthun.eseal.bean.LocationInfos;
import com.agenthun.eseal.bean.MACByOpenCloseContainer;
import com.agenthun.eseal.bean.UserInfoByGetToken;
import com.agenthun.eseal.bean.base.Result;

import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/2 上午11:02.
 */
public interface FreightTrackWebService {

    //登陆，获取Token
    @GET("GetTokenByUserNameAndPassword")
    Observable<UserInfoByGetToken> userInfoByGetToken(
            @Query("userName") String userName,
            @Query("password") String password,
            @Query("language") String language);

    //配置终端货物信息参数
    @GET("ConfigureCargo")
    Observable<Result> configureCargo(
            @Query("token") String token,
            @Query("implementID") String implementID,
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

    //解封、开箱操作 - 获取MAC
    @GET("OpenContainer")
    Observable<MACByOpenCloseContainer> openContainer(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("RFID") String RFID,
            @Nullable @Query("images") String images,
            @Nullable @Query("coordinate") String coordinate,
            @Query("operateTime") String operateTime,
            @Query("language") String language);

    //上封、关箱操作(海关 / 普通用户) - 获取MAC
    @GET("CloseContainer")
    Observable<MACByOpenCloseContainer> closeContainer(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("RFID") String RFID,
            @Nullable @Query("images") String images,
            @Nullable @Query("coordinate") String coordinate,
            @Query("operateTime") String operateTime,
            @Query("language") String language);


    /**
     * @description 蓝牙锁访问链路
     */
    //根据Token获取所有在途中的货物设置信息
    @GET("GetFreightInfoByToken")
    Observable<FreightInfos> getBleDeviceFreightList(
            @Query("token") String token,
            @Query("language") String language);

    //根据containerId获取该货物状态列表
    @GET("GetAllBaiduCoordinateByContainerId")
    Observable<LocationInfos> getBleDeviceLocation(
            @Query("token") String token,
            @Query("containerId") String containerId,
            @Query("language") String language);



/*    //获取某集装箱containerId动态数据列表
    @GET("GetAllDynamicData")
    Call<AllDynamicDataByContainerId> getAllDynamicData(
            @Query("token") String token,
            @Query("containerId") String containerId,
            @Query("currentPageIndex") Integer currentPageIndex,
            @Query("language") String language);*/

    /**
     * @description 北斗终端帽访问链路
     */
    //根据Token获取所有在途中的货物设置信息
    @GET("GetAllImplement")
    Observable<FreightInfos> getBeidouMasterDeviceFreightList(
            @Query("token") String token,
            @Query("language") String language);

    //根据implementID获取该货物状态列表
    @GET("GetImplementPositionInfoByID")
    Observable<LocationInfos> getBeidouMasterDeviceLocation(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("language") String language);

    /**
     * @description 北斗终端NFC访问链路
     */
    //根据Token获取所有在途中的货物设置信息
    @GET("GetAllNFCByToken")
    Observable<FreightInfos> getBeidouNfcDeviceFreightList(
            @Query("token") String token,
            @Query("language") String language);

    //根据NFCId获取该货物状态列表
    @GET("GetNFCPositionInfoByID")
    Observable<LocationInfos> getBeidouNfcDeviceLocation(
            @Query("token") String token,
            @Query("NFCId") String nfcId,
            @Query("language") String language);

}
