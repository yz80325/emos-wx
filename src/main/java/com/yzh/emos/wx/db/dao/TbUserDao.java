package com.yzh.emos.wx.db.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.Set;

@Mapper
public interface TbUserDao {

    /**
     * 查询角色权限
     * @return
     */
    public boolean HaveRootUser();

    /**
     * 插入用户信息
     */
    public int insertUser(HashMap map);

    /**
     * 查询用户主键Id
     * @param openId
     * @return
     */
    public Integer searchIdByOpenId(String openId);

    public Set<String> searchUserPermissions(Integer userId);

}