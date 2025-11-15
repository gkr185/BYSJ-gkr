package com.bcu.edu.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单信息DTO（OrderService返回）
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
public class OrderInfoDTO {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 团长ID
     */
    private Long leaderId;

    /**
     * 收货地址ID
     */
    private Long receiveAddressId;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 支付状态
     */
    private Integer payStatus;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 分单组标识
     */
    private String dispatchGroup;

    /**
     * 配送单ID
     */
    private Long deliveryId;
}

