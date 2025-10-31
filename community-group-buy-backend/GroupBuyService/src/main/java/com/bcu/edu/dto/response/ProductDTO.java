package com.bcu.edu.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品信息DTO（Feign响应）
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Data
public class ProductDTO {
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 封面图
     */
    private String coverImg;
    
    /**
     * 原价
     */
    private BigDecimal price;
    
    /**
     * 库存
     */
    private Integer stock;
    
    /**
     * 状态（0-下架/1-上架）
     */
    private Integer status;
}

