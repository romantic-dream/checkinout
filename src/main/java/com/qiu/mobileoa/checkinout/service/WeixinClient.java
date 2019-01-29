package com.qiu.mobileoa.checkinout.service;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

public interface WeixinClient {

    /*JSONObject getSnsAccessToken(String code) throws IOException;

    JSONObject getSnsUserInfo(String access_token,String openid) throws IOException;

    JSONObject getRefreshToken(String refreshtoken) throws IOException;*/

    JSONObject getAccessToken() throws IOException;

    JSONObject getUserInfo(String access_token,String openid) throws IOException;
}
