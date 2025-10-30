package com.bcu.edu.config;

import com.bcu.edu.common.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 拦截请求，验证JWT Token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // 白名单路径，不需要JWT验证
        if (isWhitelist(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ⭐ 检查是否来自网关（网关已完成鉴权）
        String fromGateway = request.getHeader("X-Gateway-Request");
        if ("true".equals(fromGateway)) {
            // 来自网关，直接信任并从请求头获取用户信息
            String userIdStr = request.getHeader("X-User-Id");
            String username = request.getHeader("X-Username");
            String roleStr = request.getHeader("X-User-Role");
            
            if (StringUtils.hasText(userIdStr)) {
                request.setAttribute("userId", Long.parseLong(userIdStr));
                request.setAttribute("username", username);
                if (StringUtils.hasText(roleStr)) {
                    request.setAttribute("role", Integer.parseInt(roleStr));
                }
                log.debug("网关请求，用户信息: userId={}, username={}", userIdStr, username);
            }
            
            filterChain.doFilter(request, response);
            return;
        }

        // ⭐ 不是来自网关（直接访问或Feign调用），需要验证JWT
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                // 验证Token
                if (jwtUtil.validateToken(token)) {
                    // 提取用户信息
                    String username = jwtUtil.getUsernameFromToken(token);
                    
                    // 将用户信息存储到请求属性中，供后续使用
                    request.setAttribute("username", username);
                    request.setAttribute("userId", jwtUtil.getClaimFromToken(token, "userId"));
                    request.setAttribute("role", jwtUtil.getClaimFromToken(token, "role"));
                    
                    log.debug("JWT验证成功: username={}", username);
                } else {
                    log.warn("JWT Token验证失败");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"message\":\"Token验证失败\"}");
                    return;
                }
            } catch (Exception e) {
                log.error("JWT解析异常", e);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token解析失败\"}");
                return;
            }
        } else {
            log.warn("请求头中未找到Token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未提供认证Token\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 判断是否为白名单路径
     */
    private boolean isWhitelist(String path) {
        // 定义白名单路径（不需要JWT验证）
        String[] whitelist = {
                "/api/user/register",
                "/api/user/login",
                "/actuator/health",
                "/swagger-ui",
                "/api-docs",
                "/v3/api-docs"
        };

        for (String whitelistPath : whitelist) {
            if (path.contains(whitelistPath)) {
                return true;
            }
        }

        return false;
    }
}

