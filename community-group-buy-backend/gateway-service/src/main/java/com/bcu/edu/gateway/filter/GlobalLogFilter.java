package com.bcu.edu.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 全局日志过滤器
 * 记录所有通过网关的请求
 * 
 * @author 耿康瑞
 * @since 2025-10-30
 */
@Slf4j
@Component
public class GlobalLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 生成请求ID
        String requestId = UUID.randomUUID().toString().replace("-", "");
        
        // 获取请求信息
        String method = request.getMethod().name();
        String path = request.getURI().getPath();
        String remoteAddr = getRemoteAddr(request);
        String userAgent = request.getHeaders().getFirst("User-Agent");
        
        long startTime = System.currentTimeMillis();
        
        log.info("===> Gateway请求开始 | RequestId: {} | {} {} | IP: {} | UA: {}",
                requestId, method, path, remoteAddr, userAgent);
        
        // 将requestId添加到请求头
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-Id", requestId)
                .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null ?
                            exchange.getResponse().getStatusCode().value() : 0;
                    
                    log.info("<=== Gateway请求结束 | RequestId: {} | {} {} | 状态码: {} | 耗时: {}ms",
                            requestId, method, path, statusCode, duration);
                }));
    }

    /**
     * 获取真实客户端IP
     */
    private String getRemoteAddr(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddress() != null ?
                    request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        }
        return ip;
    }

    @Override
    public int getOrder() {
        return -200;  // 优先级最高，最先执行
    }
}

