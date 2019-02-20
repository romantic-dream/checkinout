package com.qiu.mobileoa.checkinout.service;

import com.qiu.mobileoa.checkinout.dto.CheckRecordDTO;

import java.util.Date;
import java.util.List;

public interface CheckinoutService {
    List<CheckRecordDTO> getWithTime(Date time);
}
