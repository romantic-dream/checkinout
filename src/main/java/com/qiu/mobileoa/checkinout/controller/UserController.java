package com.qiu.mobileoa.checkinout.controller;

import com.alibaba.fastjson.JSONObject;
import com.qiu.mobileoa.checkinout.enumeration.CheckType;
import com.qiu.mobileoa.checkinout.service.UserService;
import com.qiu.mobileoa.checkinout.service.impl.WeixinClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;

@RestController
@EnableAutoConfiguration
@RequestMapping("/user")
@CrossOrigin
public class UserController {

    @Autowired
    private WeixinClientImpl weixinClient;

    @Autowired
    private UserService userService;

    @PostMapping("/checkInOut")
    public void checkInOut(@RequestBody String code) throws IOException {
        JSONObject snsAccessToken = weixinClient.getSnsAccessToken(code);
        String access_token = snsAccessToken.getString("access_token");
        String openid = snsAccessToken.getString("openid");
        JSONObject snsUserInfo = weixinClient.getSnsUserInfo(access_token, openid);
        userService.checkInOut(openid, CheckType.CheckIn,new Date());
    }
}
