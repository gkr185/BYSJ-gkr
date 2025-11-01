package com.bcu.edu.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项视图对象
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
public class OrderItemVO {

    /**
     * 订单项ID
     */
    private Long itemId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImg;

    /**
     * 购买单价
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 小计金额
     */
    private BigDecimal totalPrice;

    /**
     * 拼团活动ID（非拼团为null）
     */
    private Long activityId;
}

