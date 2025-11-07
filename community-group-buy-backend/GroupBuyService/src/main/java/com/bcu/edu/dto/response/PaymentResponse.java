package com.bcu.edu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 支付响应DTO
 * 
 * @author 系统
 * @since 2025-11-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    
    /**
     * 支付ID
     */
    private Long payId;
    
    /**
     * 支付方式（1-微信；2-支付宝；3-余额）
     */
    private Integer payType;
    
    /**
     * 支付状态（0-待支付；1-已支付；2-已退款）
     */
    private Integer payStatus;
    
    /**
     * 错误信息（支付失败时）
     */
    private String errorMessage;
}

