package com.bcu.edu.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和解析JWT Token
 * 支持静态方法调用和Spring Bean注入
 *
 * @author 耿康瑞
 * @date 2025-10-12
 */
@Slf4j
public class JwtUtil {

    /**
     * 实例方法：生成Token（委托给静态方法）
     */
    public String createToken(String username, Map<String, Object> claims) {
        return generateToken(username, claims);
    }

    /**
     * 实例方法：从Token获取用户名
     */
    public String extractUsername(String token) {
        return getUsernameFromToken(token);
    }

    /**
     * 实例方法：从Token获取Claim
     */
    public Object extractClaim(String token, String claimKey) {
        return getClaimFromToken(token, claimKey);
    }

    /**
     * 实例方法：验证Token
     */
    public boolean validate(String token) {
        return validateToken(token);
    }

    /**
     * 实例方法：获取过期时间
     */
    public long expiration() {
        return getExpiration();
    }

    /**
     * 密钥（生产环境应从配置文件读取，至少32位）
     * ⚠️ 必须与Gateway的jwt.secret保持一致
     */
    private static final String SECRET_KEY = "bcu-community-group-buy-system-jwt-secret-key-2025-must-be-at-least-256-bits-long-for-hs256-algorithm";

    /**
     * Token过期时间（默认7天，单位：毫秒）
     */
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Header中的Token键名
     */
    public static final String HEADER_NAME = "Authorization";

    /**
     * 获取签名密钥
     */
    private static Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT Token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT Token
     */
    public static String generateToken(Long userId, String username, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return generateToken(claims);
    }

    /**
     * 生成JWT Token（自定义Claims）
     *
     * @param claims 自定义声明
     * @return JWT Token
     */
    public static String generateToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成JWT Token（兼容UserService的调用方式）
     *
     * @param username 用户名
     * @param claims 自定义声明
     * @return JWT Token
     */
    public static String generateToken(String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成JWT Token（指定过期时间）
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @param expirationMs 过期时间（毫秒）
     * @return JWT Token
     */
    public static String generateToken(Long userId, String username, Integer role, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT Token
     *
     * @param token JWT Token
     * @return Claims对象
     */
    public static Claims parseToken(String token) {
        try {
            // 移除Bearer前缀
            if (token.startsWith(TOKEN_PREFIX)) {
                token = token.substring(TOKEN_PREFIX.length());
            }

            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("Token已过期", e);
            throw new RuntimeException("Token已过期");
        } catch (UnsupportedJwtException e) {
            log.error("不支持的Token", e);
            throw new RuntimeException("不支持的Token");
        } catch (MalformedJwtException e) {
            log.error("Token格式错误", e);
            throw new RuntimeException("Token格式错误");
        } catch (SecurityException e) {
            log.error("Token签名验证失败", e);
            throw new RuntimeException("Token签名验证失败");
        } catch (IllegalArgumentException e) {
            log.error("Token参数错误", e);
            throw new RuntimeException("Token参数错误");
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        }
        return (Long) userId;
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return (String) claims.get("username");
    }

    /**
     * 从Token中获取用户名（别名方法，兼容UserService）
     *
     * @param token JWT Token
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        return getUsername(token);
    }

    /**
     * 从Token中获取指定Claim
     *
     * @param token JWT Token
     * @param claimKey Claim键名
     * @return Claim值
     */
    public static Object getClaimFromToken(String token, String claimKey) {
        Claims claims = parseToken(token);
        return claims.get(claimKey);
    }

    /**
     * 获取Token过期时间（毫秒）
     *
     * @return 过期时间
     */
    public static long getExpiration() {
        return EXPIRATION_TIME;
    }

    /**
     * 从Token中获取用户角色
     *
     * @param token JWT Token
     * @return 用户角色
     */
    public static Integer getRole(String token) {
        Claims claims = parseToken(token);
        return (Integer) claims.get("role");
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查Token是否过期
     *
     * @param token JWT Token
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 刷新Token（延长过期时间）
     *
     * @param token 原Token
     * @return 新Token
     */
    public static String refreshToken(String token) {
        Claims claims = parseToken(token);
        Long userId = getUserId(token);
        String username = getUsername(token);
        Integer role = getRole(token);
        return generateToken(userId, username, role);
    }

    /**
     * 从请求头中获取Token
     *
     * @param authorizationHeader Authorization请求头值
     * @return Token（去除Bearer前缀）
     */
    public static String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}

