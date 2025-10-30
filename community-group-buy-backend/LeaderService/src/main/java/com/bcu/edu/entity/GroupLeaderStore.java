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
 * 团长团点实体类
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 团长团点表 - 存储团长信息和所属社区
 * 
 * 业务规则：
 * 1. 一个社区可以有多个团长（未来扩展）
 * 2. 一个用户只能在一个社区担任团长
 * 3. 团长必须关联到已存在的社区
 * 4. 团长审核通过后，需要调用UserService更新用户角色为2（团长）
 */
@Entity
@Table(name = "group_leader_store", indexes = {
        @Index(name = "idx_leader_id", columnList = "leader_id", unique = true),
        @Index(name = "idx_community_id", columnList = "community_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupLeaderStore {

    /**
     * 团点ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long storeId;

    /**
     * 团长ID（sys_user.user_id）
     * 外键关联UserService的用户表
     * 一个用户只能在一个社区担任团长
     */
    @Column(name = "leader_id", nullable = false, unique = true)
    private Long leaderId;

    /**
     * 团长姓名（冗余字段）
     */
    @Column(name = "leader_name", length = 50)
    private String leaderName;

    /**
     * 团长手机号（冗余字段）
     */
    @Column(name = "leader_phone", length = 20)
    private String leaderPhone;

    /**
     * 所属社区ID（关联community表）
     */
    @Column(name = "community_id", nullable = false)
    private Long communityId;

    /**
     * 社区名称（冗余字段）
     */
    @Column(name = "community_name", length = 100)
    private String communityName;

    /**
     * 团点名称
     * 示例："小王团点"、"阳光小区1号团点"
     */
    @Column(name = "store_name", length = 100)
    private String storeName;

    /**
     * 团点地址（自提点地址）
     */
    @Column(length = 255)
    private String address;
    
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
     */
    @Column(name = "detail_address", length = 255)
    private String detailAddress;
    
    /**
     * 经度
     */
    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;
    
    /**
     * 最大配送范围（米）
     */
    @Column(name = "max_delivery_range")
    private Integer maxDeliveryRange = 3000;

    /**
     * 团点简介
     */
    @Column(length = 500)
    private String description;

    /**
     * 团长佣金比例（百分比）
     * 示例：10.00 表示10%
     * 默认10%
     */
    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate = BigDecimal.valueOf(10.00);

    /**
     * 累计佣金（单位：元）
     */
    @Column(name = "total_commission", precision = 10, scale = 2)
    private BigDecimal totalCommission = BigDecimal.ZERO;

    /**
     * 团长状态
     * 0-待审核 1-正常运营 2-已停用
     */
    @Column(nullable = false)
    private Integer status = 0;

    /**
     * 审核人ID（管理员ID）
     */
    @Column(name = "reviewer_id")
    private Long reviewerId;

    /**
     * 审核意见
     */
    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    /**
     * 审核时间
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

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

