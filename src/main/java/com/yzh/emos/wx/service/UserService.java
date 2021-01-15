package com.yzh.emos.wx.service;

import java.util.Set;

public interface UserService {
    /**
     *
     * @param registerCode 页面所填入的注册码
     * @param Code 授权码
     * @param nickName 昵称
     * @param photo 头像
     * @return
     */
    public int registerUser(String registerCode,String Code,String nickName,String photo);

    public Set<String> searchUserPermissions(Integer userId);

    public Integer login(String code);
}
