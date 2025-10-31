package com.bcu.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分类请求DTO（创建/更新通用）
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
@Schema(description = "分类请求")
public class CategoryRequest {
    
    @NotNull(message = "父分类ID不能为空")
    @Schema(description = "父分类ID（0表示顶级分类）", example = "0")
    private Long parentId;
    
    @NotBlank(message = "分类名称不能为空")
    @Schema(description = "分类名称", example = "生鲜水果")
    private String categoryName;
    
    @Schema(description = "排序权重（默认0）", example = "1")
    private Integer sort = 0;
}

