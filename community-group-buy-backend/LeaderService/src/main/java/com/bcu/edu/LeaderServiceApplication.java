package com.bcu.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LeaderService 启动类
 * 
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 团长服务 - 管理社区、团长申请审核、佣金结算
 * 
 * 功能模块：
 * 1. 社区管理 (Community Management)
 * 2. 社区申请审核 (Community Application)
 * 3. 团长管理 (Leader Management)
 * 4. 佣金管理 (Commission Management)
 * 
 * 数据库：leader_service_db (4张表)
 * - community (社区表)
 * - community_application (社区申请表)
 * - group_leader_store (团长团点表)
 * - commission_record (佣金记录表)
 * 
 * 端口：8068
 * Swagger文档：http://localhost:8068/swagger-ui.html
 */
@SpringBootApplication
@EnableDiscoveryClient  // 启用服务发现
@EnableFeignClients     // 启用Feign客户端
@EnableScheduling       // 启用定时任务（佣金结算）
public class LeaderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeaderServiceApplication.class, args);
        System.out.println("\n==============================================");
        System.out.println("   LeaderService 启动成功！");
        System.out.println("   端口：8068");
        System.out.println("   Swagger文档：http://localhost:8068/swagger-ui.html");
        System.out.println("   API文档：http://localhost:8068/api-docs");
        System.out.println("   健康检查：http://localhost:8068/actuator/health");
        System.out.println("==============================================\n");
    }
}

