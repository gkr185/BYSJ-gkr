package com.bcu.edu.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 创建订单请求（供GroupBuyService调用）
 * 
 * <p>调用场景：
 * <ul>
 *   <li>用户参团时创建订单</li>
 *   <li>团长发起并参与时创建订单</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

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
    private Long addressId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称（快照）
     */
    private String productName;

    /**
     * 商品图片（快照）
     */
    private String productImg;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 购买单价（拼团价/原价）
     */
    private BigDecimal price;

    /**
     * 拼团活动ID（非拼团为null）
     */
    private Long activityId;
}

