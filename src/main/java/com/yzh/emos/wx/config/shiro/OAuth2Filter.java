package com.yzh.emos.wx.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 因为在OAuth2Filter类中要读写ThreadLocal中的数据，所以OAuth2Filter类必须要设置成多例的，否则ThreadLocal将无法使用。
 * 客户端发送的请求都会被拦截
 */
@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    StringRedisTemplate redisTemplate;


    /**
     * shiro需要处理，拦截之后会执行create方法
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest= (HttpServletRequest) request;
        String token=getTokenString(httpRequest);
        if (StrUtil.isBlank(token)){
            return null;
        }
        //由shiro框架处理
        return new OAuth2Token(token);
    }

    /**
     * 哪些请求能被Shiro框架处理
     * @param request
     * @param response
     * @param mappedValue
     * @return true 不被处理
     * false 处理
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpRequest=(HttpServletRequest)request;
        if (httpRequest.getMethod().equals(RequestMethod.OPTIONS.name())){
            //直接放行
            return true;
        }
        return false;
    }

    /**
     * 判断token是否有效
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        //设置响应
        httpResponse.setContentType("text/html");
        httpResponse.setCharacterEncoding("UTF-8");
        //允许跨域请求
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Origin", httpResponse.getHeader("Origin"));

        //因为要更新token所以先进行清空
        threadLocalToken.clear();
        String tokenString = getTokenString(httpRequest);
        if (StrUtil.isBlank(tokenString)){
            httpResponse.setStatus(HttpStatus.SC_UNAUTHORIZED);
            httpResponse.getWriter().print("无token");
            return false;
        }
        //如果有token
        try {
            jwtUtil.verifyToken(tokenString);
        }catch (TokenExpiredException e){
            //检查redis中是否有
            if (redisTemplate.hasKey(tokenString)){
                redisTemplate.delete(tokenString);
                Integer userId = jwtUtil.getUserId(tokenString);
                String token = jwtUtil.createToken(userId);
                redisTemplate.opsForValue().set(token,userId+"",cacheExpire, TimeUnit.DAYS);
                threadLocalToken.setToken(token);
            }else {
                //缓存也不在
                httpResponse.setStatus(HttpStatus.SC_UNAUTHORIZED);
                httpResponse.getWriter().print("令牌已过期");
                return false;
            }
        }catch (JWTDecodeException e){
            //客户端发来的令牌有问题，无法解析
            httpResponse.setStatus(HttpStatus.SC_UNAUTHORIZED);
            httpResponse.getWriter().print("无效的令牌");
            return false;
        }
        //间接执行Realm类的 doGetAuthenticationInfo方法
        boolean b = executeLogin(httpRequest, httpResponse);
        return b;
    }

    /**
     * shiro 认证失败后
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        //设置响应
        httpResponse.setContentType("text/html");
        httpResponse.setCharacterEncoding("UTF-8");
        //允许跨域请求
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Allow-Origin", httpResponse.getHeader("Origin"));

        httpResponse.setStatus(HttpStatus.SC_UNAUTHORIZED);
        try {
            httpResponse.getWriter().print(e.getMessage());
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return false;
    }

    /**
     * 查看是否有token
     * @param httpRequest
     * @return
     */
    private String getTokenString(HttpServletRequest httpRequest) {
        String token = httpRequest.getHeader("token");
        if(StrUtil.isBlank(token)){
            //查看请求体
            token = httpRequest.getParameter("token");
        }
        return token;
    }

}
