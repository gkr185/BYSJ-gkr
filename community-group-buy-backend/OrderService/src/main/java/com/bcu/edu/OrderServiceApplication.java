package com.bcu.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * OrderService 启动类
 * 
 * <p>服务功能：
 * <ul>
 *   <li>订单管理：创建订单、订单查询、订单状态更新</li>
 *   <li>购物车管理：添加商品、结算购物车</li>
 *   <li>订单超时处理：定时任务自动取消超时未支付订单</li>
 * </ul>
 * 
 * <p>端口: 8065
 * <p>数据库: order_service_db
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
        System.out.println("========================================");
        System.out.println("OrderService 启动成功！");
        System.out.println("端口: 8065");
        System.out.println("Swagger: http://localhost:8065/swagger-ui.html");
        System.out.println("========================================");
    }
}

