package com.bcu.edu.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
public class ProductDTO {

    private Long productId;

    private String productName;

    private String coverImg;

    private BigDecimal price;

    private BigDecimal groupPrice;

    private Integer stock;

    private Integer status;
}

