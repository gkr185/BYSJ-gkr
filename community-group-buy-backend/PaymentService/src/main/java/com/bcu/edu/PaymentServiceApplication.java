package com.bcu.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * PaymentService启动类
 * 
 * <p>支付服务 - 支付流转的中枢服务
 * <p>端口: 8066
 * <p>数据库: payment_service_db
 * 
 * <p>核心功能:
 * <ul>
 *   <li>支付发起（余额支付、微信支付、支付宝支付）</li>
 *   <li>支付处理（支付回调、支付成功后处理）</li>
 *   <li>退款处理（申请退款、退款回调）</li>
 *   <li>充值管理（余额充值）</li>
 *   <li>支付查询（支付记录查询）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
        System.out.println("""
            
            ============================================
            PaymentService启动成功！
            ============================================
            服务名称: payment-service
            服务端口: 8066
            数据库: payment_service_db
            Swagger文档: http://localhost:8066/swagger-ui.html
            ============================================
            """);
    }
}

