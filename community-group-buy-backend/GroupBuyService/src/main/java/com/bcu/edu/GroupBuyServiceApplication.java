package com.bcu.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * GroupBuyService - 拼团服务
 * 
 * <p>功能模块：
 * <ul>
 *   <li>1. 拼团活动管理（管理员创建、查询、删除）</li>
 *   <li>2. 团长发起拼团（v3.0：仅团长可发起，自动关联社区）</li>
 *   <li>3. 用户参与拼团（创建订单、记录参团、行锁防并发）</li>
 *   <li>4. 支付回调处理（成团检查、幂等性保证）</li>
 *   <li>5. 成团逻辑（状态更新、批量更新订单）</li>
 *   <li>6. 查询功能（团详情、活动团列表、我的拼团）</li>
 *   <li>7. 退出拼团（成团前可退、自动退款）</li>
 *   <li>8. 定时任务（过期团检查、自动退款）</li>
 * </ul>
 * 
 * <p>技术亮点：
 * <ul>
 *   <li>⭐ 无Redis分布式锁，使用数据库行锁（SELECT ... FOR UPDATE）</li>
 *   <li>⭐ 幂等性设计（支付回调、成团逻辑、定时任务）</li>
 *   <li>⭐ Saga模式跨服务调用（Feign + 补偿机制）</li>
 *   <li>⭐ 社区优先推荐（SQL ORDER BY CASE实现）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @version 1.0
 * @since 2025-10-31
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.bcu.edu.common.feign", "com.bcu.edu.client"})
@EnableScheduling
public class GroupBuyServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupBuyServiceApplication.class, args);
        System.out.println("""
                
                ====================================
                GroupBuyService 启动成功！
                端口: 8063
                Swagger: http://localhost:8063/swagger-ui.html
                API Docs: http://localhost:8063/v3/api-docs
                ====================================
                """);
    }
}

