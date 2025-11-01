package com.bcu.edu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动详情响应（包含商品信息）
 * 用于前端展示活动列表
 * 
 * @author AI Assistant
 * @since 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityWithProductResponse {
    /**
     * 活动ID
     */
    private Long activityId;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品封面图
     */
    private String productImage;
    
    /**
     * 商品原价
     */
    private BigDecimal originalPrice;
    
    /**
     * 拼团价
     */
    private BigDecimal groupPrice;
    
    /**
     * 成团人数
     */
    private Integer requiredNum;
    
    /**
     * 最大人数限制
     */
    private Integer maxNum;
    
    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 活动状态（0-未开始；1-进行中；2-已结束）
     */
    private Integer status;
}

