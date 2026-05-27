package com.example.scaffold.common.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(Long userId, String username) {
        return JWT.create()
                .setPayload("userId", userId)
                .setPayload("username", username)
                .setExpiresAt(System.currentTimeMillis() + expiration)
                .setKey(secret.getBytes())
                .sign();
    }

    public boolean verify(String token) {
        try {
            return JWTUtil.verify(token, secret.getBytes());
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return Long.valueOf(jwt.getPayload("userId").toString());
    }

    public String getUsername(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return jwt.getPayload("username").toString();
    }
}
