package com.qiu.mobileoa.checkinout.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qiu.mobileoa.checkinout.po.CheckInOutRecord;
import com.qiu.mobileoa.checkinout.po.User;
import com.qiu.mobileoa.checkinout.service.UserService;
import com.qiu.mobileoa.checkinout.service.impl.WeixinClientImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@EnableAutoConfiguration
@RequestMapping("/user")
@CrossOrigin
public class UserController {


    @Autowired
    private UserService userService;


    @GetMapping("/getWithPage")
    public PageInfo<User> getWithPage(@RequestParam(required = false,defaultValue = "1") Integer pageNum){
        PageInfo<User> userPageInfo = userService.getWithPage(pageNum);
        return userPageInfo;
    }


}
