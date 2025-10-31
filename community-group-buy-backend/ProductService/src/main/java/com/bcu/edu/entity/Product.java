package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品信息实体
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Data
@Entity
@Table(name = "product", indexes = {
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_create_time", columnList = "create_time")
})
public class Product {
    
    /**
     * 商品ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;
    
    /**
     * 关联分类ID（外键）
     */
    @Column(name = "category_id")
    private Long categoryId;
    
    /**
     * 商品名称
     */
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    /**
     * 封面图URL
     */
    @Column(name = "cover_img", nullable = false, length = 255)
    private String coverImg;
    
    /**
     * 商品详情（富文本）
     */
    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;
    
    /**
     * 原价
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    /**
     * 拼团参考价（可被活动覆盖）
     */
    @Column(name = "group_price", precision = 10, scale = 2)
    private BigDecimal groupPrice;
    
    /**
     * 库存数量
     */
    @Column(name = "stock", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer stock = 0;
    
    /**
     * 状态（0-下架；1-上架）
     */
    @Column(name = "status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}

