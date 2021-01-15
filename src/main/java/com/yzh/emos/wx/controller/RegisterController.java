package com.yzh.emos.wx.controller;

import com.yzh.emos.wx.config.shiro.JwtUtil;
import com.yzh.emos.wx.controller.form.RegistFrom;
import com.yzh.emos.wx.service.UserService;
import com.yzh.emos.wx.util.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Api("用户模板Web接口")
public class RegisterController {

    @Autowired
    UserService userService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Value("${emos.jwt.cache-expire}")
    private Integer cacheExpire;

    @PostMapping("/register")
    @ApiOperation("注册用户")
    public R register(@Valid @RequestBody RegistFrom registFrom){
        int id = userService.registerUser(registFrom.getRegisterCode(), registFrom.getCode(), registFrom.getNickname(), registFrom.getPhoto());
        String token = jwtUtil.createToken(id);
        Set<String> permsSet = userService.searchUserPermissions(id);
        //存储token
        saveCacheToken(token,id);

        return R.ok("用户注册成功").put("token",token).put("permission",permsSet);

    }

    /**
     * 存入缓存中
     * @param token
     * @param userId
     */
    private void saveCacheToken(String token,Integer userId){
        redisTemplate.opsForValue().set(token,userId+"",cacheExpire, TimeUnit.DAYS);
    }


}
