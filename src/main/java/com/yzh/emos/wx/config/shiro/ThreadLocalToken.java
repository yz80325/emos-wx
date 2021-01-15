package com.yzh.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

/**
 * 封装ThreadLocal保持每个线程私有,从而方便判断token是否更新过
 */
@Component
public class ThreadLocalToken {
    private ThreadLocal<String> threadLocal=new ThreadLocal<String>();


    public void setToken(String token){
        threadLocal.set(token);
    }

    public String getToken(){
        return threadLocal.get();
    }

    public void clear(){
        threadLocal.remove();
    }

}
