package com.bcu.edu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建支付请求DTO
 * 
 * @author 系统
 * @since 2025-11-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentRequest {
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 支付方式（1-微信；2-支付宝；3-余额）
     */
    private Integer payType;
    
    /**
     * 支付金额
     */
    private BigDecimal amount;
    
    /**
     * 用户ID（从Header传递）
     */
    private Long userId;
}

