package com.qiu.mobileoa.checkinout.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.qiu.mobileoa.checkinout.api.WeixinApi;
import com.qiu.mobileoa.checkinout.service.WeixinClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.fastjson.FastJsonConverterFactory;

import java.io.IOException;
import java.net.MalformedURLException;

@Service
public class WeixinClientImpl implements WeixinClient {

    private Retrofit retrofit;
    private WeixinApi weixinApi;

    @Value("${mp.appid}")
    private String appid;

    @Value("${mp.secret}")
    private String secret;


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
        return jsonObject;
    }

    @Override
    public JSONObject getSnsUserInfo(String access_token, String openid) throws IOException {
        Call<JSONObject> call = weixinApi.getSnsUserInfo(access_token, openid, "zh_CN");
        Response<JSONObject> response = call.execute();
        JSONObject jsonObject = response.body();
        return jsonObject;
    }
}
