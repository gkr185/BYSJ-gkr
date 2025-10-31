package com.bcu.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品统计VO
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
@Schema(description = "商品统计数据")
public class ProductStatisticsVO {
    
    @Schema(description = "商品总数")
    private Long totalProducts;
    
    @Schema(description = "上架商品数")
    private Long onSaleProducts;
    
    @Schema(description = "下架商品数")
    private Long offSaleProducts;
    
    @Schema(description = "库存预警商品数（库存≤10）")
    private Long lowStockProducts;
    
    @Schema(description = "今日新增商品数")
    private Long todayNewProducts;
    
    @Schema(description = "分类统计")
    private List<CategoryStatVO> categoryStats;
}

/**
 * 分类统计VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分类统计")
class CategoryStatVO {
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "分类名称")
    private String categoryName;
    
    @Schema(description = "商品数量")
    private Long productCount;
}

