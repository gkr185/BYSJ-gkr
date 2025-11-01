package com.bcu.edu.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 购物车结算请求
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
public class CheckoutRequest {

    /**
     * 购物车项ID列表
     */
    private List<Long> cartIds;

    /**
     * 收货地址ID
     */
    private Long addressId;

    /**
     * 团长ID
     */
    private Long leaderId;
}

