package com.qiu.mobileoa.checkinout.controller;

import com.alibaba.fastjson.JSONObject;
import com.qiu.mobileoa.checkinout.dto.MessageAutoResponseDTO;
import com.qiu.mobileoa.checkinout.po.User;
import com.qiu.mobileoa.checkinout.service.impl.UserServiceImpl;
import com.qiu.mobileoa.checkinout.service.impl.WeixinClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;

@RestController
@RequestMapping("/message")
public class MessageController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WeixinClientImpl weixinClient;

    @Autowired
    private UserServiceImpl userService;

    @Value("${weixin.accessToken}")
    private String accessToken;

    /*@GetMapping("/receive")
    public String receive(@RequestParam Map<String,String> allparams){
        logger.info("{}",allparams);
        String echostr = allparams.get("echostr");
        return echostr;
    }*/

    @PostMapping(value = "receive",produces = MediaType.APPLICATION_XML_VALUE)
    public Object receive(@RequestBody JSONObject jsonObject) throws IOException {
        logger.info("{}",jsonObject);
        String msgType = jsonObject.getString("MsgType");
        if (msgType == "event"){
            String fromUserName = jsonObject.getString("FromUserName");
            String toUserName = jsonObject.getString("ToUserName");
            JSONObject userInfo = weixinClient.getSnsUserInfo(accessToken,fromUserName);
            String nickname = userInfo.getString("nickname");
            String openid = userInfo.getString("openid");

            MessageAutoResponseDTO messageAutoResponseDTO = new MessageAutoResponseDTO();
            messageAutoResponseDTO.setToUserName(fromUserName);
            messageAutoResponseDTO.setFromUserName(toUserName);
            messageAutoResponseDTO.setCreateTime(new Date().getTime());
            messageAutoResponseDTO.setMsgType("text");
            messageAutoResponseDTO.setContent(String.format("你好，%s,欢迎订阅！",nickname));

            User user = new User();
            user.setOpenid(openid);
            Integer gender = userInfo.getInteger("sex");
            String avatarUrl = userInfo.getString("headimgurl");
            user.setNickname(nickname);
            user.setGender(gender);
            user.setAvatarUrl(avatarUrl);
            userService.create(user);
            return messageAutoResponseDTO;
        }



        return null;
    }
}
