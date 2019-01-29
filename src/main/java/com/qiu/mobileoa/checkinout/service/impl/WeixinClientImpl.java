package com.qiu.mobileoa.checkinout.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.qiu.mobileoa.checkinout.api.WeixinApi;
import com.qiu.mobileoa.checkinout.service.WeixinClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;

@Service
public class WeixinClientImpl implements WeixinClient {

    private Retrofit retrofit;
    private WeixinApi weixinApi;

    @Value("${mp.appid}")
    private String appid;

    @Value("${mp.secret}")
    private String secret;

    @Autowired
    private RedisTemplate redisTemplate;

    private Logger logger = LoggerFactory.getLogger(this.getClass());



    public WeixinClientImpl(@Value("${weixin.baseUrl}") String url) throws MalformedURLException {
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(FastJsonConverterFactory.create())
                .build();
        weixinApi = retrofit.create(WeixinApi.class);
    }

    @Override
    public JSONObject getSnsAccessToken(String code) throws IOException {
        Call<JSONObject> call = weixinApi.getSnsAccessToken(appid, secret, code, "authorization_code");
        Response<JSONObject> response = call.execute();
        JSONObject jsonObject = response.body();
        String refresh_token = jsonObject.getString("refresh_token");
        String access_token = jsonObject.getString("access_token");
        redisTemplate.opsForValue().set("expire_in",new Date().getTime());
        redisTemplate.opsForValue().set("refresh_token",refresh_token);
        redisTemplate.opsForValue().set("access_token",access_token);
        return jsonObject;
    }

    @Override
    public JSONObject getSnsUserInfo(String access_token, String openid) throws IOException {
        Call<JSONObject> call = weixinApi.getSnsUserInfo(access_token, openid, "zh_CN");
        Response<JSONObject> response = call.execute();
        JSONObject jsonObject = response.body();
        return jsonObject;
    }

    @Override
    public JSONObject getRefreshToken(String refreshtoken) throws IOException {
        Call<JSONObject> call = weixinApi.refreshToken(appid, "refresh_token", refreshtoken);
        Response<JSONObject> response = call.execute();
        JSONObject jsonObject = response.body();
        String refresh_token = jsonObject.getString("refresh_token");
        String access_token = jsonObject.getString("access_token");
        redisTemplate.opsForValue().set("expire",new Date().getTime());
        redisTemplate.opsForValue().set("refresh_token",refresh_token);
        redisTemplate.opsForValue().set("access_token",access_token);
        return jsonObject;
    }
}
