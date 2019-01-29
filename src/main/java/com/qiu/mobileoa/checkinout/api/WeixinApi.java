package com.qiu.mobileoa.checkinout.api;

import com.alibaba.fastjson.JSONObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeixinApi {

    /**
     微信网页授权
     **/
  /*  @GET("sns/oauth2/access_token")
    Call<JSONObject> getSnsAccessToken(@Query("appid") String appid, @Query("secret") String secret, @Query("code") String code, @Query("grant_type") String grant_type);

    @GET("sns/userinfo")
    Call<JSONObject> getSnsUserInfo(@Query("access_token") String access_token,@Query("openid") String openid,@Query("lang") String lang);

    @GET("sns/oauth2/refresh_token")
    Call<JSONObject> refreshToken(@Query("appid") String appid,@Query("grant_type") String grant_type,@Query("refresh_token") String refresh_token);*/

    @GET("cgi-bin/token")
    Call<JSONObject> getAccessToken(@Query("grant_type") String grant_type,@Query("appid") String appid,@Query("secret") String secret);

    @GET("cgi-bin/user/info")
    Call<JSONObject> getUserInfo(@Query("access_token") String access_token,@Query("openid") String openid,@Query("lang") String lang);
}
