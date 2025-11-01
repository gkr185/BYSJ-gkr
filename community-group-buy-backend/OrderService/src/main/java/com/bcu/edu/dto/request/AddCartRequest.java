package com.bcu.edu.dto.request;

import lombok.Data;

/**
 * 添加购物车请求
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
public class AddCartRequest {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 拼团活动ID（非拼团为null）
     */
    private Long activityId;

    /**
     * 数量
     */
    private Integer quantity = 1;
}

