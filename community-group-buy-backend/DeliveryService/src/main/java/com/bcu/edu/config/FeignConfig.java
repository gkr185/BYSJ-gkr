package com.bcu.edu.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * Feign配置类
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Configuration
@EnableFeignClients(basePackages = "com.bcu.edu.client")
public class FeignConfig {
    
}
