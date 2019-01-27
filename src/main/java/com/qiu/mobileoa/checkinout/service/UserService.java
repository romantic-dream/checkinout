package com.qiu.mobileoa.checkinout.service;

import com.qiu.mobileoa.checkinout.enumeration.CheckType;
import com.qiu.mobileoa.checkinout.po.User;

import java.util.Date;

public interface UserService {

    void create(User user);

    User getById(String openid);

    void checkInOut(String openId, CheckType checkType, Date time);
}
