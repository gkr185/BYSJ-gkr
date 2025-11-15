package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 仓库配置实体类（严格匹配delivery_service_db.warehouse_config表）
 * 
 * <p>表名: warehouse_config
 * <p>说明: 管理配送仓库信息，支持多仓库配送和默认仓库机制
 * 
 * <p>关键字段：
 * <ul>
 *   <li>is_default: 0-否；1-是（系统只能有一个默认仓库）</li>
 *   <li>status: 0-禁用；1-启用</li>
 *   <li>longitude/latitude: 仓库经纬度（路径规划起点）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Entity
@Table(name = "warehouse_config", indexes = {
        @Index(name = "uk_warehouse_name", columnList = "warehouse_name", unique = true),
        @Index(name = "idx_is_default", columnList = "is_default"),
        @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WarehouseConfig {

    /**
     * 仓库ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 仓库名称（唯一）
     */
    @Column(name = "warehouse_name", nullable = false, unique = true, length = 100)
    private String warehouseName;

    /**
     * 仓库地址
     */
    @Column(name = "address", nullable = false, length = 255)
    private String address;

    /**
     * 仓库经度（路径规划起点）
     */
    @Column(name = "longitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;

    /**
     * 仓库纬度（路径规划起点）
     */
    @Column(name = "latitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;

    /**
     * 是否默认仓库
     * 0-否；1-是（系统只能有一个默认仓库）
     */
    @Column(name = "is_default", nullable = false)
    private Integer isDefault = 0;

    /**
     * 状态
     * 0-禁用；1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 联系人
     */
    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    /**
     * 联系电话
     */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * 描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}

