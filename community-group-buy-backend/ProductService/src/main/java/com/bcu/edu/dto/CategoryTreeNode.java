package com.bcu.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分类树节点DTO
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
@Schema(description = "分类树节点")
public class CategoryTreeNode {
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "分类名称")
    private String categoryName;
    
    @Schema(description = "父分类ID")
    private Long parentId;
    
    @Schema(description = "排序权重")
    private Integer sort;
    
    @Schema(description = "状态（0-禁用；1-启用）")
    private Integer status;
    
    @Schema(description = "子分类列表")
    private List<CategoryTreeNode> children;
}

