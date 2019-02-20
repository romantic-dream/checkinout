package com.qiu.mobileoa.checkinout.dto;

import com.qiu.mobileoa.checkinout.po.CheckInOutRecord;
import com.qiu.mobileoa.checkinout.po.User;

public class CheckRecordDTO extends CheckInOutRecord {

    private Long timestamp;
    private User user;

    public CheckRecordDTO(Long timestamp, User user) {
        this.timestamp = timestamp;
        this.user = user;
    }

    public CheckRecordDTO() {

    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


}
