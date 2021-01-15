package com.yzh.emos.wx.aop;

import com.yzh.emos.wx.config.shiro.ThreadLocalToken;
import com.yzh.emos.wx.util.R;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TokenAspect {
    @Autowired
    ThreadLocalToken threadLocalToken;

    @Pointcut("execution(public * com.yzh.emos.wx.controller.*.*(..)))")
    public void aspect(){

    }

    @Around("aspect()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        R r = (R) point.proceed();
        String token = threadLocalToken.getToken();
        if (token!=null){
            //说明token已经被更新
            r.put("token",token);
            threadLocalToken.clear();
        }
        return r;
    }
}
