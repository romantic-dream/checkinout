package com.qiu.mobileoa.checkinout.controller;

import com.alibaba.fastjson.JSONObject;
import com.grum.geocalc.Coordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import com.qiu.mobileoa.checkinout.dto.*;
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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/message")
public class MessageController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WeixinClientImpl weixinClient;

    @Autowired
    private UserServiceImpl userService;


    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${checkInOut.latitude}")
    private Double checkLatitude;

    @Value("${checkInOut.longitude}")
    private Double checkLongitude;

    public String accessToken;

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
        if (msgType.equals("event")){
            String fromUserName = jsonObject.getString("FromUserName");
            String toUserName = jsonObject.getString("ToUserName");
            String event = jsonObject.getString("Event");
            if (event.equals("subscribe")){
                if (redisTemplate.opsForValue().get("access_token")==null){
                    weixinClient.getAccessToken();
                }
                if (new Date().getTime()>(Long)redisTemplate.opsForValue().get("expire_in")+7200*1000){
                    weixinClient.getAccessToken();
                }
                accessToken = redisTemplate.opsForValue().get("access_token").toString();
                JSONObject userInfo = weixinClient.getUserInfo(accessToken, fromUserName);
                String nickname = userInfo.getString("nickname");
                User oldUser = userService.getById(fromUserName);
                if (oldUser!=null){
                    MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                    messageAutoResponseDTO.setContent(String.format("你好，%s,欢迎回来！",nickname));
                    return messageAutoResponseDTO;
                }
                MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                messageAutoResponseDTO.setContent(String.format("你好，%s,欢迎订阅！",nickname));

                User user = new User();
                user.setOpenid(fromUserName);
                Integer gender = userInfo.getInteger("sex");
                String avatarUrl = userInfo.getString("headimgurl");
                user.setNickname(nickname);
                user.setGender(gender);
                user.setAvatarUrl(avatarUrl);
                userService.create(user);
                return messageAutoResponseDTO;
            }

            if (event.equals("unsubscribe")){
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
                Long nowTime = new Date().getTime();
                if (redisTemplate.opsForValue().get(fromUserName+"onWork")!=null && nowTime<((Long) redisTemplate.opsForValue().get(fromUserName+"onWork")+(5*60*60*1000))){
                    MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                    messageAutoResponseDTO.setContent("已经打过卡了哦！");
                    return messageAutoResponseDTO;
                }

                if (redisTemplate.opsForValue().get(fromUserName+"offWork")!=null && nowTime<((Long)redisTemplate.opsForValue().get(fromUserName+"offWork")+(19*60*60*1000))){
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
                    if(redisTemplate.opsForHash().get(positionUserKey, "latitude")==null ||  redisTemplate.opsForHash().get(positionUserKey, "longitude")==null){
                        MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                        messageAutoResponseDTO.setContent("请点击上方的按钮，开启位置信息");
                        return messageAutoResponseDTO;
                    }
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
                    if(distance>600){
                        MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                        messageAutoResponseDTO.setContent("不在打卡范围内，别想偷懒！");
                        return messageAutoResponseDTO;
                    }

                    //设置上下班的时间
                    Date now = new Date();
                    LocalTime time = now.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
                    LocalTime onWorkStart = LocalTime.parse("08:00:00");
                    LocalTime onWorkEnd = LocalTime.parse("09:00:00");
                    LocalTime offWorkStart = LocalTime.parse("17:00:00");
                    LocalTime offWorkEnd = LocalTime.parse("18:00:00");

                    String content="";
                    if (time.isAfter(onWorkStart)&&time.isBefore(onWorkEnd)){
                        content="上班打卡成功,祝您工作愉快！";
                        userService.checkInOut(fromUserName,new Date());
                        Calendar onWorkTime = Calendar.getInstance();
                        onWorkTime.set(Calendar.HOUR, 9);
                        onWorkTime.set(Calendar.SECOND, 0);
                        onWorkTime.set(Calendar.MINUTE, 0);
                        onWorkTime.set(Calendar.MILLISECOND,0);

                        redisTemplate.opsForValue().set(fromUserName+"onWork",onWorkTime.getTimeInMillis());
                        redisTemplate.expire(fromUserName+"onWork",8, TimeUnit.HOURS);
                    }else if (time.isAfter(offWorkStart)&&time.isBefore(offWorkEnd)){
                        content="下班打卡成功！";
                        userService.checkInOut(fromUserName,new Date());
                        Calendar offWorkTime = Calendar.getInstance();
                        offWorkTime.set(Calendar.HOUR, 18);
                        offWorkTime.set(Calendar.SECOND, 0);
                        offWorkTime.set(Calendar.MINUTE, 0);
                        offWorkTime.set(Calendar.MILLISECOND,0);
                        redisTemplate.opsForValue().set(fromUserName+"offWork",offWorkTime.getTimeInMillis());
                        redisTemplate.expire(fromUserName+"offWork",14, TimeUnit.HOURS);
                    }else {
                        content="不在打卡时间内,请获取文字消息！";
                    }
                    MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                    messageAutoResponseDTO.setContent(content);
                    return messageAutoResponseDTO;
                }

                if (eventKey.equals("word")){
                    MessageAutoResponseDTO messageAutoResponseDTO = getMessageAutoResponseDTO(fromUserName, toUserName);
                    messageAutoResponseDTO.setContent("打卡规定时间：上午8:00-9:00 ，下午17:00-18:00");
                    return messageAutoResponseDTO;
                }

                if (eventKey.equals("image")){
                    ImageAutoResponseDTO imageAutoResponseDTO = new ImageAutoResponseDTO();
                    imageAutoResponseDTO.setToUserName(fromUserName);
                    imageAutoResponseDTO.setFromUserName(toUserName);
                    imageAutoResponseDTO.setCreateTime(new Date().getTime());
                    imageAutoResponseDTO.setMsgType("image");
                    MediaId mediaId = new MediaId();
                    mediaId.setMeidaId("rFX3usyThZ0hNRidJPeBayzk0unwYRPPrjog8QHFxqk");
                    imageAutoResponseDTO.setMediaId(mediaId);
                    return imageAutoResponseDTO;
                }

                if (eventKey.equals("wordimage")){
                    NewsAutoResponseDTO newsAutoResponseDTO = new NewsAutoResponseDTO();
                    newsAutoResponseDTO.setToUserName(fromUserName);
                    newsAutoResponseDTO.setFromUserName(toUserName);
                    newsAutoResponseDTO.setCreateTime(new Date().getTime());
                    newsAutoResponseDTO.setMsgType("news");

                    List<Articles> articles = new LinkedList<Articles>();
                    Articles articles1 = new Articles();
                    articles1.setDescription("打领带的简单教学");
                    articles1.setTitle("打领带的图片");
                    articles1.setPicUrl("http://mmbiz.qpic.cn/mmbiz_jpg/nibTRwTVPfofobwyz9YFsEG0XFsKl8f08k6D4OCCMrWdpJ3gyOFv7RzvWtosc8q4ickX2TMiachZA5km96kjsgAjg/0?wx_fmt=jpeg");
                    articles1.setUrl("http://mp.weixin.qq.com/s?__biz=MzU3NTgyMzI2Nw==&mid=100000003&idx=1&sn=215b58eb3733754f459653497457c9a4&chksm=7d1c0dcf4a6b84d927f5d9794c053fef21767e74f244e3c451cda004cc236d3eb31a4b1965b3#rd");
                    articles.add(articles1);

                    newsAutoResponseDTO.setArticleCount(articles.size());
                    newsAutoResponseDTO.setArticles(articles);
                    return newsAutoResponseDTO;
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
