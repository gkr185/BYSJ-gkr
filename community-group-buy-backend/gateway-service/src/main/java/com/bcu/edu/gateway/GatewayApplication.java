package com.bcu.edu.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway 启动类
 * 
 * @author 耿康瑞
 * @since 2025-10-30
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("""
            
            ========================================
             API Gateway 启动成功！
             端口: 9000
             Consul: http://localhost:8500
             健康检查: http://localhost:9000/actuator/health
            ========================================
            """);
    }
}

