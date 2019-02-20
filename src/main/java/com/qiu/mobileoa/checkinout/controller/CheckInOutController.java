package com.qiu.mobileoa.checkinout.controller;

import com.qiu.mobileoa.checkinout.dto.CheckRecordDTO;
import com.qiu.mobileoa.checkinout.service.CheckinoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/checkinout")
public class CheckInOutController {

    @Autowired
    private CheckinoutService checkinoutService;

    @GetMapping("/getWithTime")
    public List<CheckRecordDTO> getWithTime(@RequestParam(required = false) Long timestamp){
        Date time = new Date();
        if (timestamp!=null){
            time = new Date(timestamp);
        }
        List<CheckRecordDTO> checkInOutRecords = checkinoutService.getWithTime(time);
        return checkInOutRecords;
    }
}
