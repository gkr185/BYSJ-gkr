package com.bcu.edu.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置跨域访问等
 *
 * @author 耿康瑞
 * @date 2025-10-12
 * 
 * ⚠️ 注意：使用API Gateway后，CORS统一在Gateway层配置
 * 业务服务不需要配置CORS，避免响应头重复
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * ⚠️ 已注释：使用API Gateway后，CORS统一在Gateway层配置
     * 业务服务不需要配置CORS，避免响应头重复导致跨域失败
     */
    /*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的前端域名（生产环境应配置具体域名）
                .allowedOriginPatterns("*")
                // 允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 允许的请求头
                .allowedHeaders("*")
                // 是否允许发送Cookie
                .allowCredentials(true)
                // 预检请求的有效期（单位：秒）
                .maxAge(3600);
    }
    */
}

