package com.bcu.edu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单统计视图对象
 *
 * @author 耿康瑞
 * @date 2025-11-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总订单数
     */
    private Long totalOrders;

    /**
     * 待支付订单数
     */
    private Long pendingPayment;

    /**
     * 待发货订单数
     */
    private Long pendingDelivery;

    /**
     * 配送中订单数
     */
    private Long inDelivery;

    /**
     * 已送达订单数
     */
    private Long delivered;

    /**
     * 已取消订单数
     */
    private Long cancelled;

    /**
     * 退款中订单数
     */
    private Long refunding;

    /**
     * 已退款订单数
     */
    private Long refunded;

    /**
     * 今日订单数
     */
    private Long todayOrders;

    /**
     * 今日销售额
     */
    private BigDecimal todaySales;

    /**
     * 总销售额
     */
    private BigDecimal totalSales;
}

