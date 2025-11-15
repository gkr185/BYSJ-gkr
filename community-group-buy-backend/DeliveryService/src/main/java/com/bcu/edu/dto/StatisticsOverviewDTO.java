package com.bcu.edu.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 配送统计总览DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
public class StatisticsOverviewDTO {

    /**
     * 配送单总数
     */
    private Long totalDeliveries;

    /**
     * 待分配配送单数
     */
    private Long pendingDeliveries;

    /**
     * 配送中配送单数
     */
    private Long shippingDeliveries;

    /**
     * 已完成配送单数
     */
    private Long completedDeliveries;

    /**
     * 配送订单总数
     */
    private Long totalOrders;

    /**
     * 累计配送距离（米）
     */
    private BigDecimal totalDistance;

    /**
     * 累计配送时间（分钟）
     */
    private Long totalDuration;

    /**
     * 平均配送距离（米）
     */
    private BigDecimal averageDistance;

    /**
     * 平均途经点数量
     */
    private Double averageWaypointCount;
}

