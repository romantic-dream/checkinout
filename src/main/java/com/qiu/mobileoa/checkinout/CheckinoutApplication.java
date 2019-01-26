package com.qiu.mobileoa.checkinout;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.qiu.mobileoa.checkinout.dao")
public class CheckinoutApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckinoutApplication.class, args);
    }

}

