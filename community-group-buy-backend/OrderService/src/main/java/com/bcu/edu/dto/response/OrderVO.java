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
     * 用户ID
     */
    private Long userId;

    /**
     * 团长ID
     */
    private Long leaderId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

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
     * 用户名（用于团长端显示）
     */
    private String userName;

    /**
     * 主商品名称（列表展示用，取第一个商品）
     */
    private String productName;

    /**
     * 主商品图片（列表展示用，取第一个商品）
     */
    private String productImg;

    /**
     * 主商品数量（列表展示用，取第一个商品）
     */
    private Integer quantity;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 订单类型
     * 0-普通订单；1-拼团订单
     */
    private Integer orderType;

    /**
     * 收货地址ID
     */
    private Long receiveAddressId;

    /**
     * 收货地址（完整地址字符串，用于列表展示）
     */
    private String receiveAddress;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;
}

