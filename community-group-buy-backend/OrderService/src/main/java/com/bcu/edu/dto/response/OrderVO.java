package com.bcu.edu.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图对象（列表展示）
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
public class OrderVO {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态
     * 0-待支付；1-待发货；2-配送中；3-已送达；4-已取消；5-退款中；6-已退款
     */
    private Integer orderStatus;

    /**
     * 订单状态文本
     */
    private String orderStatusText;

    /**
     * 支付状态
     * 0-未支付；1-已支付
     */
    private Integer payStatus;

    /**
     * 订单商品列表（简化版）
     */
    private List<OrderItemVO> items;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;
}

