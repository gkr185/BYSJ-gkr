package com.bcu.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品请求DTO（创建/更新通用）
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
@Schema(description = "商品请求")
public class ProductRequest {
    
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;
    
    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称", example = "新鲜苹果")
    private String productName;
    
    @NotBlank(message = "封面图不能为空")
    @Schema(description = "封面图URL", example = "http://localhost:8062/uploads/product/xxx.jpg")
    private String coverImg;
    
    @Schema(description = "商品详情（富文本）", example = "<p>商品详情</p>")
    private String detail;
    
    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    @Schema(description = "商品原价", example = "12.50")
    private BigDecimal price;
    
    @Schema(description = "拼团参考价", example = "9.90")
    private BigDecimal groupPrice;
    
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    @Schema(description = "库存数量", example = "100")
    private Integer stock;
}

