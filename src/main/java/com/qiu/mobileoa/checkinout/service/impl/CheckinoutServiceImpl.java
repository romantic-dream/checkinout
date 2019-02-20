package com.qiu.mobileoa.checkinout.service.impl;

import com.qiu.mobileoa.checkinout.dao.CheckInOutRecordMapper;
import com.qiu.mobileoa.checkinout.dto.CheckRecordDTO;
import com.qiu.mobileoa.checkinout.service.CheckinoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CheckinoutServiceImpl implements CheckinoutService {

    @Autowired
    private CheckInOutRecordMapper checkInOutRecordMapper;

    @Override
    public List<CheckRecordDTO> getWithTime(Date time) {
        List<CheckRecordDTO> checkInOutRecords = checkInOutRecordMapper.selectWithTime(time);
        checkInOutRecords.stream().forEach(checkRecordDTO -> checkRecordDTO.setTimestamp(checkRecordDTO.getTime().getTime()));
        return checkInOutRecords;
    }
}
