package com.qiu.mobileoa.checkinout.service.impl;

import com.qiu.mobileoa.checkinout.dao.CheckInOutRecordMapper;
import com.qiu.mobileoa.checkinout.dao.UserMapper;
import com.qiu.mobileoa.checkinout.po.CheckInOutRecord;
import com.qiu.mobileoa.checkinout.po.User;
import com.qiu.mobileoa.checkinout.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CheckInOutRecordMapper checkInOutRecordMapper;

    @Override
    public void create(User user) {
        userMapper.insert(user);
    }

    @Override
    public User getById(String openid) {
        User user = userMapper.selectByPrimaryKey(openid);
        return user;
    }

    @Override
    public void checkInOut(String openId, Date time) {
        CheckInOutRecord checkInOutRecord = new CheckInOutRecord();
        checkInOutRecord.setOpenid(openId);
        checkInOutRecord.setTime(time);
        checkInOutRecordMapper.insert(checkInOutRecord);
    }
}
