package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatProperties weChatProperties;
    //微信服务接口地址
    public static final String WX_LOGIN="https://api.weixin.qq.com/sns/jscode2session";


    @Override
    public User login(UserLoginDTO userLoginDTO) {
           String openid = getOpenId(userLoginDTO);

           if(openid==null){

               throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
           }

           User user = userMapper.getByOpenId(openid);

           if(user==null){
               User user1 = User.builder()
                       .openid(openid)
                       .createTime(LocalDateTime.now())
                       .build();
               userMapper.save(user1);
           }

           return user;

    }

    private String getOpenId(UserLoginDTO userLoginDTO) {

        String code = userLoginDTO.getCode();

        HashMap<String,String> map = new HashMap<>();

        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN,map);
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");

    }


}
