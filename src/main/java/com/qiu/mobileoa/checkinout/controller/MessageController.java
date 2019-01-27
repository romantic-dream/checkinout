package com.qiu.mobileoa.checkinout.controller;

import com.alibaba.fastjson.JSONObject;
import com.qiu.mobileoa.checkinout.dto.MessageAutoResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/message")
public class MessageController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /*@GetMapping("/receive")
    public String receive(@RequestParam Map<String,String> allparams){
        logger.info("{}",allparams);
        String echostr = allparams.get("echostr");
        return echostr;
    }*/

    @PostMapping(value = "receive",produces = MediaType.APPLICATION_XML_VALUE)
    public MessageAutoResponseDTO receive(@RequestBody JSONObject jsonObject){
        logger.info("{}",jsonObject);
        MessageAutoResponseDTO messageAutoResponseDTO = new MessageAutoResponseDTO();
        messageAutoResponseDTO.setToUserName("oo7si1eVBb6OYP-zGINj5yspYv7E");
        messageAutoResponseDTO.setFromUserName("gh_242a22109582");
        messageAutoResponseDTO.setCreateTime(new Date().getTime());
        messageAutoResponseDTO.setMsgType("text");
        messageAutoResponseDTO.setContent("welcome");
        return messageAutoResponseDTO;
    }
}
