package com.yzh.emos.wx.service.Imp;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yzh.emos.wx.db.dao.TbUserDao;
import com.yzh.emos.wx.exception.EmosException;
import com.yzh.emos.wx.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@Scope("prototype")
public class UserServiceImp implements UserService {
    @Value("${wx.app-id}")
    private String appId;
    @Value("${wx.app-secret}")
    private String appSecret;

    @Autowired
    private TbUserDao tbUserDao;

    /**
     * 获取openId
     * @param code
     * @return
     */
    private String getOpenId(String code){
        String url="https://api.weixin.qq.com/sns/jscode2session";
        Map map = new HashMap();
        map.put("appid", appId);
        map.put("secret", appSecret);
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        String response = HttpUtil.get(url,map);
        JSONObject jsonObject = JSONUtil.parseObj(response);
        String openid = jsonObject.getStr("openid");
        if (openid==null||openid.length()==0){
            throw new RuntimeException("临时登陆凭证错误");
        }
        return openid;
    }

    @Transactional
    @Override
    public int registerUser(String registerCode, String Code, String nickName, String photo) {
          //超级管理员注册
        if(registerCode.equals("000000")){
            boolean b = tbUserDao.HaveRootUser();
            if (!b){
                String openId = getOpenId(Code);
                HashMap param=new HashMap();
                param.put("openId", openId);
                param.put("nickname", nickName);
                param.put("photo", photo);
                param.put("role", "[0]");
                param.put("status", 1);
                param.put("createTime", new Date());
                param.put("root", true);
                int i = tbUserDao.insertUser(param);
                if (i!=0){
                    Integer id = tbUserDao.searchIdByOpenId(openId);
                    return id;
                }else {
                    //插入失败数据回滚
                    throw new RuntimeException("超级管理员创建失败，请重新创建");
                }
            }else {
                throw new EmosException("无法绑定超级管理员账号，已经存在");
            }
        }else {
            //TODO 超级管理员以外
            return 0;
        }
    }

    @Override
    public Set<String> searchUserPermissions(Integer userId) {
        Set<String> strings = tbUserDao.searchUserPermissions(userId);
        return strings;
    }

    /**
     * 登录
     * @param code
     * @return
     */
    @Override
    public Integer login(String code) {
        String openId = getOpenId(code);
        Integer id = tbUserDao.searchIdByOpenId(openId);
        if (id==null){
            throw new EmosException("用户不存在");
        }
        //TODO 从消息列表取出

        return id;
    }
}
