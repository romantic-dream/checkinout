package com.qiu.mobileoa.checkinout.service;

import com.github.pagehelper.PageInfo;
import com.qiu.mobileoa.checkinout.po.CheckInOutRecord;
import com.qiu.mobileoa.checkinout.po.User;

import java.util.Date;
import java.util.List;

public interface UserService {

    void create(User user);

    User getById(String openid);

    void checkInOut(String openId, Date time);

    PageInfo<User> getWithPage(Integer pageNum);

}
