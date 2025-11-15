package com.bcu.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DeliveryService 启动类
 * 
 * <p>服务功能：
 * <ul>
 *   <li>智能路径规划：基于Dijkstra算法的最短路径优化</li>
 *   <li>配送单管理：创建配送单、状态更新、配送监控</li>
 *   <li>批量发货：与管理端集成的批量发货处理</li>
 *   <li>配送统计：配送数据分析和效率统计</li>
 * </ul>
 * 
 * <p>端口: 8067
 * <p>数据库: delivery_service_db
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.bcu.edu.feign")
@EnableJpaAuditing
@EnableScheduling
@EntityScan(basePackages = "com.bcu.edu.entity")
@EnableJpaRepositories(basePackages = "com.bcu.edu.repository")
public class DeliveryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("DeliveryService 启动成功！");
        System.out.println("端口: 8067");
        System.out.println("Swagger: http://localhost:8067/swagger-ui.html");
        System.out.println("Dijkstra算法路径规划已就绪");
        System.out.println("========================================");
    }
}
