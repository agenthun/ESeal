package com.agenthun.eseal.connectivity.service;

import android.support.annotation.Nullable;

import com.agenthun.eseal.bean.AllDynamicDataByContainerId;
import com.agenthun.eseal.bean.DynamicDataDetailByPositionId;
import com.agenthun.eseal.bean.FreightInfoByImplementID;
import com.agenthun.eseal.bean.FreightInfosByToken;
import com.agenthun.eseal.bean.MACByOpenCloseContainer;
import com.agenthun.eseal.bean.ResetImplementByContainerId;
import com.agenthun.eseal.bean.UserInfoByGetToken;
import com.agenthun.eseal.bean.base.BaseWebServiceResponseBody;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/2 上午11:02.
 */
public interface FreightTrackWebService {

/*    //登陆，获取Token
    @GET("GetTokenByUserNameAndPassword")
    Observable<UserInfoByGetToken> userInfoByGetToken(
            @Query("userName") String userName,
            @Query("password") String password,
            @Query("language") String language);*/

    //登陆，获取Token
    @GET("GetTokenByUserNameAndPassword")
    Call<UserInfoByGetToken> userInfoByGetToken(
            @Query("userName") String userName,
            @Query("password") String password,
            @Query("language") String language);

    //根据Token获取所有在途中的货物设置信息
    @GET("GetFreightInfoByToken")
    Call<FreightInfosByToken> getFreightInfoByToken(
            @Query("token") String token,
            @Query("language") String language);

    //设置终端参数
    @GET("ResetImplement")
    Call<ResetImplementByContainerId> resetImplement(
            @Query("token") String token,
            @Query("containerId") String containerId,
            @Query("frequency") String frequency,
            @Query("tempThreshold") String tempThreshold,
            @Query("humThreshold") String humThreshold,
            @Query("vibThreshold") String vibThreshold,
            @Query("language") String language);

    //配置终端货物信息参数
    @GET("ConfigureCargo")
    Call<BaseWebServiceResponseBody> configureCargo(
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

    //获取某集装箱containerId动态数据列表
    @GET("GetAllDynamicData")
    Call<AllDynamicDataByContainerId> getAllDynamicData(
            @Query("token") String token,
            @Query("containerId") String containerId,
            @Query("currentPageIndex") Integer currentPageIndex,
            @Query("language") String language);

    //获取某一具体位置positionId动态数据
    @GET("GetDynamicDataDetail")
    Call<DynamicDataDetailByPositionId> getDynamicDataDetail(
            @Query("token") String token,
            @Query("positionId") String containerId,
            @Query("language") String language);

    //根据ContainerId获取轨迹
    @GET("FreightTrackPath.aspx")
    Call<ResponseBody> getFreightTrackPath(
            @Query("token") String token,
            @Query("type") String type,
            @Query("containerId") String containerId,
            @Query("language") String language);

    //根据设备ID获取货物信息
    @GET("GetFreightInfoByImplementID")
    Call<FreightInfoByImplementID> getFreightInfoByImplementID(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("language") String language);

    //开箱操作 - 获取MAC
    @GET("OpenContainer")
    Call<MACByOpenCloseContainer> openContainer(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("random") String random,
            @Query("language") String language);

    //关箱操作(海关 / 普通用户) - 获取MAC
    @GET("CloseContainer")
    Call<MACByOpenCloseContainer> closeContainer(
            @Query("token") String token,
            @Query("implementID") String implementID,
            @Query("random") String random,
            @Query("language") String language);
}
