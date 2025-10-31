package com.bcu.edu.gateway.filter;

import com.bcu.edu.gateway.config.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT鉴权过滤器
 * 在网关层统一进行JWT Token验证
 * 
 * @author 耿康瑞
 * @since 2025-10-30
 */
@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // 白名单（直接定义，避免注入问题）
    private static final List<String> WHITELIST = Arrays.asList(
        // UserService - 公开接口
        "/api/user/register",
        "/api/user/login",
        
        // GroupBuyService - 公开接口（查询类）
        "/api/groupbuy/team/*/detail",           // 团详情
        "/api/groupbuy/activity/*/teams",        // 活动团列表（社区优先）
        "/api/groupbuy/activities",              // 活动列表
        "/api/groupbuy/activities/ongoing",      // 进行中的活动
        "/api/groupbuy/activity/*",              // 活动详情（仅GET）
        
        // ProductService - 公开接口（查询类）
        "/api/product/**",                       // 商品查询（C端）
        "/api/category/**",                      // 分类查询（C端）
        
        // LeaderService - 公开接口（查询类）
        "/api/community/list",                   // 社区列表
        
        // Swagger & Actuator
        "/api-docs/**",
        "/swagger-ui/**",
        "/actuator/health",
        "/actuator/info"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        log.debug("Gateway请求: {} {}", method, path);

        // 白名单检查（无需认证）
        if (isWhitelist(path)) {
            log.debug("白名单路径，跳过鉴权: {}", path);
            return chain.filter(exchange);
        }

        // OPTIONS请求放行（CORS预检）
        if ("OPTIONS".equals(method)) {
            return chain.filter(exchange);
        }

        // 获取Token
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            log.warn("请求缺少Token: {}", path);
            return unauthorized(exchange, "请先登录");
        }

        try {
            // 验证JWT并提取用户信息
            Claims claims = JwtUtil.parseToken(token, jwtSecret);
            Long userId = JwtUtil.getUserId(claims);
            String username = JwtUtil.getUsername(claims);
            Integer role = JwtUtil.getRole(claims);

            if (userId == null) {
                log.error("Token中缺少userId");
                return unauthorized(exchange, "Token无效");
            }

            // 将用户信息传递给后端服务（通过请求头）
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-Username", username != null ? username : "")
                    .header("X-User-Role", role != null ? String.valueOf(role) : "")
                    .header("X-Gateway-Request", "true")  // 标识请求来自网关
                    .build();

            log.info("用户认证成功: userId={}, username={}, role={}, path={}",
                    userId, username, role, path);

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return unauthorized(exchange, "登录已过期，请重新登录");
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage(), e);
            return unauthorized(exchange, "Token无效");
        }
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelist(String path) {
        return WHITELIST.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 从请求头中提取Token
     */
    private String extractToken(ServerHttpRequest request) {
        // 从Authorization头获取
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 从查询参数获取（兼容某些场景）
        List<String> tokenParams = request.getQueryParams().get("token");
        if (tokenParams != null && !tokenParams.isEmpty()) {
            return tokenParams.get(0);
        }

        return null;
    }

    /**
     * 返回401未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());

        try {
            String json = objectMapper.writeValueAsString(result);
            DataBuffer buffer = response.bufferFactory()
                    .wrap(json.getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败", e);
            return response.setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;  // 优先级高，先执行鉴权
    }
}

