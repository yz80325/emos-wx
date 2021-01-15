package com.yzh.emos.wx.config.shiro;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.yzh.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class JwtUtil {
    @Value("${emos.jwt.secret}")
    private String secret;

    @Value("${emos.jwt.expire}")
    private int expire;

    /**
     * 创建令牌
     * @param userId
     * @return
     */
    public String createToken(Integer userId){
        //创建JWT
        JWTCreator.Builder builder = JWT.create();
        //创建过期时间,偏移五天
        Date offset = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, expire).toJdkDate();
        //加密秘钥
        Algorithm algorithm = Algorithm.HMAC256(secret);

        String token = builder.withClaim("userId", userId).withExpiresAt(offset).sign(algorithm);
        return token;
    }

    public Integer getUserId(String token){
        try {
            DecodedJWT decode = JWT.decode(token);
            return decode.getClaim("userId").asInt();
        }catch (Exception e){
            throw new EmosException("令牌无效");
        }
    }
    /**
     * 验证令牌
     * 如果失败将抛出异常
     */
    public void verifyToken(String token){
        //解密
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier build = JWT.require(algorithm).build();
        build.verify(token);
    }
}
