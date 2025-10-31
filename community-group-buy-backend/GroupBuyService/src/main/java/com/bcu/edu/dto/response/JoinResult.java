package com.bcu.edu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 参团结果响应DTO
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinResult {
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 团ID
     */
    private Long teamId;
    
    /**
     * 团号
     */
    private String teamNo;
    
    /**
     * 当前人数
     */
    private Integer currentNum;
    
    /**
     * 成团人数
     */
    private Integer requiredNum;
    
    /**
     * 还差几人
     */
    private Integer remainNum;
    
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 过期时间
     */
    private String expireTime;
}

