package com.qiu.mobileoa.checkinout.controller;

import com.alibaba.fastjson.JSONObject;
import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import com.qiu.mobileoa.checkinout.dto.MessageAutoResponseDTO;
import com.qiu.mobileoa.checkinout.po.User;
import com.qiu.mobileoa.checkinout.service.impl.UserServiceImpl;
import com.qiu.mobileoa.checkinout.service.impl.WeixinClientImpl;
import org.apache.ibatis.annotations.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${checkInOut.latitude}")
    private Double checkLatitude;

    @Value("${checkInOut.longitude}")
    private Double checkLongitude;

    /*@GetMapping("/receive")
    public String receive(@RequestParam Map<String,String> allparams){
        logger.info("{}",allparams);
        String echostr = allparams.get("echostr");
        return echostr;
    }*/

    @PostMapping(value = "receive",produces = MediaType.APPLICATION_XML_VALUE)
    public Object receive(@RequestBody JSONObject jsonObject) throws IOException, ParseException {
        logger.info("{}",jsonObject);
        String msgType = jsonObject.getString("MsgType");
        if (msgType.equals("event")){
            String fromUserName = jsonObject.getString("FromUserName");
            String toUserName = jsonObject.getString("ToUserName");
            String event = jsonObject.getString("Event");
            if (event.equals("subscribe")){
                JSONObject userInfo = weixinClient.getSnsUserInfo(accessToken,fromUserName);
                String nickname = userInfo.getString("nickname");
                String openid = userInfo.getString("openid");
                User oldUser = userService.getById(openid);
                if (oldUser!=null){
                    MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                    messageAutoResponseDTO.setContent(String.format("你好，%s,欢迎订阅！",nickname));
                    return messageAutoResponseDTO;
                }
                MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
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

            if (event.equals("unsubscribe")){
                JSONObject userInfo = weixinClient.getSnsUserInfo(accessToken,fromUserName);
                String nickname = userInfo.getString("nickname");
                String openid = userInfo.getString("openid");

                MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                return messageAutoResponseDTO;
            }

            if (event.equals("LOCATION")){
                Double latitude = jsonObject.getDouble("Latitude");
                Double longitude = jsonObject.getDouble("Longitude");
                JSONObject position = new JSONObject();
                position.put("latitude",latitude);
                position.put("longitude",longitude);
                String positionUserKey = "position" + fromUserName;

                redisTemplate.opsForHash().putAll(positionUserKey,position);
                redisTemplate.expire(positionUserKey,300, TimeUnit.SECONDS);
                return "success";
            }

            if (event.equals("CLICK")){
                Long morningTime = (Long) redisTemplate.opsForValue().get(fromUserName + "onWork");
                Long noonTime = (Long) redisTemplate.opsForValue().get(fromUserName+"offWork");
                Long nowTime = new Date().getTime();
                if (morningTime!=null && nowTime<(morningTime+(5*60*60*1000))){
                    MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                    messageAutoResponseDTO.setContent("已经打过卡了哦！");
                    return messageAutoResponseDTO;
                }

                if (noonTime!=null && nowTime<(noonTime+(19*60*60*1000))){
                    MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                    messageAutoResponseDTO.setContent("已经打过卡了哦！");
                    return messageAutoResponseDTO;
                }
                String eventKey = jsonObject.getString("EventKey");
                if (eventKey == null){
                    return "success";
                }

                if (eventKey.equals("checkinout")){
                    String positionUserKey="position" + fromUserName;
                    Double latitude = (Double) redisTemplate.opsForHash().get(positionUserKey, "latitude");
                    Double longitude = (Double) redisTemplate.opsForHash().get(positionUserKey, "longitude");
                    //当前位置的经纬度
                    Coordinate lat = Coordinate.fromDegrees(latitude);
                    Coordinate lng = Coordinate.fromDegrees(longitude);
                    Point userCurrentPosition = Point.at(lat, lng);

                    //公司打卡位置的经纬度
                    lat = Coordinate.fromDegrees(checkLatitude);
                    lng = Coordinate.fromDegrees(checkLongitude);
                    Point checkPosition = Point.at(lat, lng);

                    //计算当前位置与公司打卡位置的距离
                    double distance = EarthCalc.harvesineDistance(userCurrentPosition, checkPosition); //in meters
                    if(distance<200){
                        MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                        messageAutoResponseDTO.setContent("不在打卡范围内，别想偷懒！");
                        return messageAutoResponseDTO;
                    }

                    //设置上下班的时间
                    Date now = new Date();
                    LocalTime time = now.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
                    LocalTime onWorkStart = LocalTime.parse("08:00:00");
                    LocalTime onWorkEnd = LocalTime.parse("09:00:00");
                    LocalTime offWorkStart = LocalTime.parse("14:00:00");
                    LocalTime offWorkEnd = LocalTime.parse("18:00:00");

                    String content="";
                    if (time.isAfter(onWorkStart)&&time.isBefore(onWorkEnd)){
                        content="上班打卡成功";
                        userService.checkInOut(fromUserName,new Date());
                        Calendar onWorkTime = Calendar.getInstance();
                        onWorkTime.set(Calendar.HOUR, 9);
                        onWorkTime.set(Calendar.SECOND, 0);
                        onWorkTime.set(Calendar.MINUTE, 0);
                        onWorkTime.set(Calendar.MILLISECOND,0);
                        redisTemplate.opsForValue().set(fromUserName+"onWork",onWorkTime);
                        redisTemplate.expire(fromUserName+"onWork",5, TimeUnit.HOURS);
                    }else if (time.isAfter(offWorkStart)&&time.isBefore(offWorkEnd)){
                        content="下班打卡成功";
                        userService.checkInOut(fromUserName,new Date());
                        Calendar offWorkTime = Calendar.getInstance();
                        offWorkTime.set(Calendar.HOUR, 18);
                        offWorkTime.set(Calendar.SECOND, 0);
                        offWorkTime.set(Calendar.MINUTE, 0);
                        offWorkTime.set(Calendar.MILLISECOND,0);
                        redisTemplate.opsForValue().set(fromUserName+"offWork",offWorkTime);
                        redisTemplate.expire(fromUserName+"offWork",19, TimeUnit.HOURS);
                    }else {
                        content="不在打卡时间内";
                    }
                    MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                    messageAutoResponseDTO.setContent(content);
                    return messageAutoResponseDTO;
                }
            }

        }



        return null;
    }

    private MessageAutoResponseDTO getMessageAutoResponseDTO(String fromUserName, String toUserName) {
        MessageAutoResponseDTO messageAutoResponseDTO = new MessageAutoResponseDTO();
        messageAutoResponseDTO.setToUserName(fromUserName);
        messageAutoResponseDTO.setFromUserName(toUserName);
        messageAutoResponseDTO.setCreateTime(new Date().getTime());
        messageAutoResponseDTO.setMsgType("text");
        return messageAutoResponseDTO;
    }
}
