package com.bcu.edu.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情视图对象
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
public class OrderDetailVO {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 订单状态文本
     */
    private String orderStatusText;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 团长ID
     */
    private Long leaderId;

    /**
     * 团长名称
     */
    private String leaderName;

    /**
     * 收货地址ID
     */
    private Long receiveAddressId;

    /**
     * 收货地址详情
     */
    private String receiveAddress;

    /**
     * 订单商品列表
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

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

