package com.bcu.edu.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品DTO - 用于Feign接口返回
 *
 * @author 耿康瑞
 * @since 2025-11-04
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
     * 封面图片
     */
    private String coverImg;

    /**
     * 商品价格
     */
    private BigDecimal price;

    /**
     * 拼团价格
     */
    private BigDecimal groupPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 商品状态（0-下架；1-上架）
     */
    private Integer status;
}
