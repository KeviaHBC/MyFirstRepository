package com.example.scaffold.common.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private byte[] secretBytes;

    @PostConstruct
    void init() {
        this.secretBytes = secret.getBytes();
    }

    /** JWT 解析结果 */
    public record JwtPayload(Long userId, String username) {}

    public String generateToken(Long userId, String username) {
        return JWT.create()
                .setPayload("userId", userId)
                .setPayload("username", username)
                .setExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .setKey(secretBytes)
                .sign();
    }

    public boolean verify(String token) {
        try {
            return JWTUtil.verify(token, secretBytes);
        } catch (Exception e) {
            return false;
        }
    }

    /** 一次解析同时获取 userId 和 username，避免重复 parse */
    public JwtPayload parsePayload(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        Long userId = Long.valueOf(jwt.getPayload("userId").toString());
        String username = jwt.getPayload("username").toString();
        return new JwtPayload(userId, username);
    }
}
