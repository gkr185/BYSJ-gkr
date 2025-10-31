package com.bcu.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ProductService 启动类
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.bcu.edu.common.feign", "com.bcu.edu"})
public class ProductServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("🚀 ProductService启动成功！");
        System.out.println("📄 Swagger文档: http://localhost:8062/swagger-ui.html");
        System.out.println("📊 Consul注册: http://localhost:8500");
        System.out.println("⚡ 服务端口: 8062");
        System.out.println("========================================");
    }
}

