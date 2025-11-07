package com.bcu.edu.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 用户上下文过滤器
 * 从请求头中提取用户信息（X-User-Id, X-Username, X-User-Role）并设置到request attribute中
 * 
 * @author 耿康瑞
 * @since 2025-11-07
 */
@Slf4j
@Component
@Order(1)
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 从请求头获取用户信息（网关JWT解析后传递）
        String userIdHeader = request.getHeader("X-User-Id");
        String usernameHeader = request.getHeader("X-Username");
        String userRoleHeader = request.getHeader("X-User-Role");
        
        // 打印日志（调试用）
        log.debug("UserContextFilter - X-User-Id: {}, X-Username: {}, X-User-Role: {}", 
                 userIdHeader, usernameHeader, userRoleHeader);
        
        // 设置到request attribute中，供Controller使用
        if (userIdHeader != null) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                request.setAttribute("userId", userId);
                log.debug("设置userId到request attribute: {}", userId);
            } catch (NumberFormatException e) {
                log.warn("X-User-Id格式错误: {}", userIdHeader);
            }
        }
        
        if (usernameHeader != null) {
            request.setAttribute("username", usernameHeader);
        }
        
        if (userRoleHeader != null) {
            try {
                Integer userRole = Integer.parseInt(userRoleHeader);
                request.setAttribute("userRole", userRole);
            } catch (NumberFormatException e) {
                log.warn("X-User-Role格式错误: {}", userRoleHeader);
            }
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }
    
    /**
     * 判断是否需要过滤
     * 排除Actuator健康检查和Swagger文档接口
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/") 
            || path.startsWith("/swagger-ui/")
            || path.startsWith("/v3/api-docs");
    }
}

