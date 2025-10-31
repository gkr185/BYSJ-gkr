package com.bcu.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品查询请求DTO
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
@Schema(description = "商品查询请求")
public class ProductQueryRequest {
    
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;
    
    @Schema(description = "搜索关键词", example = "苹果")
    private String keyword;
    
    @Schema(description = "页码（从0开始）", example = "0")
    private Integer page = 0;
    
    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
    
    @Schema(description = "排序方式（price_asc/price_desc/create_time）", example = "create_time")
    private String sort = "create_time";
}

