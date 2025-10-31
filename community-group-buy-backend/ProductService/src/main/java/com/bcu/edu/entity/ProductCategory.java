package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 商品分类实体
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
@Entity
@Table(name = "product_category", indexes = {
    @Index(name = "idx_parent_id", columnList = "parent_id"),
    @Index(name = "idx_status", columnList = "status")
})
public class ProductCategory {
    
    /**
     * 分类ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;
    
    /**
     * 父分类ID（0表示顶级分类）
     */
    @Column(name = "parent_id", nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long parentId = 0L;
    
    /**
     * 分类名称
     */
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;
    
    /**
     * 排序权重（值越小越靠前）
     */
    @Column(name = "sort", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer sort = 0;
    
    /**
     * 状态（0-禁用；1-启用）
     */
    @Column(name = "status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;
}

