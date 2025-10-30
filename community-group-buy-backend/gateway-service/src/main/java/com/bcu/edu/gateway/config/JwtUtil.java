package com.bcu.edu.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * JWT工具类
 * 
 * @author 耿康瑞
 * @since 2025-10-30
 */
@Slf4j
public class JwtUtil {

    /**
     * 解析JWT Token
     *
     * @param token     JWT Token
     * @param jwtSecret JWT密钥
     * @return Claims
     */
    public static Claims parseToken(String token, String jwtSecret) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Claims中获取用户ID
     *
     * @param claims Claims
     * @return 用户ID
     */
    public static Long getUserId(Claims claims) {
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof String) {
            return Long.parseLong((String) userIdObj);
        }
        return null;
    }

    /**
     * 从Claims中获取用户名
     *
     * @param claims Claims
     * @return 用户名
     */
    public static String getUsername(Claims claims) {
        return claims.getSubject();
    }

    /**
     * 从Claims中获取用户角色
     *
     * @param claims Claims
     * @return 用户角色
     */
    public static Integer getRole(Claims claims) {
        Object roleObj = claims.get("role");
        if (roleObj instanceof Integer) {
            return (Integer) roleObj;
        } else if (roleObj instanceof String) {
            return Integer.parseInt((String) roleObj);
        }
        return null;
    }
}

