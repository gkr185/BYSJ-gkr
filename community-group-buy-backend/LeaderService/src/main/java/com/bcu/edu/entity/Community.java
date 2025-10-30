package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 社区实体类
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 社区表 - 存储社区基本信息和地理位置
 * 
 * 业务规则：
 * 1. 每个社区有唯一的名称和地址
 * 2. 经纬度用于计算用户与社区的距离（Haversine公式）
 * 3. 服务半径定义社区覆盖范围（单位：米）
 * 4. 社区必须有团长才能运营
 */
@Entity
@Table(name = "community", indexes = {
        @Index(name = "idx_latitude_longitude", columnList = "latitude, longitude"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Community {

    /**
     * 社区ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_id")
    private Long communityId;

    /**
     * 社区名称
     * 示例："阳光小区"、"幸福家园"
     */
    @Column(name = "community_name", nullable = false, length = 100)
    private String name;
    
    /**
     * 省份
     */
    @Column(length = 20)
    private String province;
    
    /**
     * 城市
     */
    @Column(length = 20)
    private String city;
    
    /**
     * 区/县
     */
    @Column(length = 20)
    private String district;

    /**
     * 详细地址
     * 示例："北京市朝阳区建国路88号"
     */
    @Column(nullable = false, length = 255)
    private String address;

    /**
     * 纬度（WGS-84坐标系）
     * 范围：-90 ~ 90
     * 精度：小数点后6位（约11cm精度）
     */
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;

    /**
     * 经度（WGS-84坐标系）
     * 范围：-180 ~ 180
     * 精度：小数点后6位（约11cm精度）
     */
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;

    /**
     * 服务半径（单位：米）
     * 默认3000米（3公里）
     * 用于判断用户是否在社区服务范围内
     */
    @Column(name = "service_radius", nullable = false)
    private Integer serviceRadius = 3000;

    /**
     * 社区状态
     * 0-待审核 1-正常运营 2-已关闭
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 社区简介
     */
    @Column(length = 500)
    private String description;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

