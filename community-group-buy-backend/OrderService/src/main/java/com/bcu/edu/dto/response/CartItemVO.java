package com.bcu.edu.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车项视图对象
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
public class CartItemVO {

    /**
     * 购物车项ID
     */
    private Long cartId;

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
     * 商品价格（原价/拼团价）
     */
    private BigDecimal price;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 小计
     */
    private BigDecimal subtotal;

    /**
     * 拼团活动ID（非拼团为null）
     */
    private Long activityId;

    /**
     * 是否拼团商品
     */
    private Boolean isGroupBuy;

    /**
     * 库存状态
     */
    private Boolean inStock;

    /**
     * 添加时间
     */
    private LocalDateTime addTime;
}

