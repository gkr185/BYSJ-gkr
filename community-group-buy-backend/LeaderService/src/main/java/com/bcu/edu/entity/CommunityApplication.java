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
 * 社区申请实体类
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 社区申请表 - 存储用户/团长的社区申请记录
 * 
 * 业务流程：
 * 1. 用户/团长提交社区申请（申请人想成为新社区的团长）
 * 2. 管理员审核申请
 * 3. 审核通过：自动创建Community + 自动创建GroupLeaderStore记录
 * 4. 审核拒绝：记录拒绝原因
 */
@Entity
@Table(name = "community_application", indexes = {
        @Index(name = "idx_applicant_id", columnList = "applicant_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommunityApplication {

    /**
     * 申请ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    /**
     * 申请人ID（sys_user.user_id）
     * 外键关联UserService的用户表
     */
    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    /**
     * 申请人姓名（冗余字段，方便查询）
     */
    @Column(name = "applicant_name", length = 50)
    private String applicantName;

    /**
     * 申请人手机号（冗余字段）
     */
    @Column(name = "applicant_phone", length = 20)
    private String applicantPhone;

    /**
     * 社区名称
     */
    @Column(name = "community_name", nullable = false, length = 100)
    private String communityName;
    
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
     * 社区详细地址
     */
    @Column(nullable = false, length = 255)
    private String address;

    /**
     * 纬度（WGS-84坐标系）
     */
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;

    /**
     * 经度（WGS-84坐标系）
     */
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;

    /**
     * 服务半径（单位：米，默认3000米）
     */
    @Column(name = "service_radius", nullable = false)
    private Integer serviceRadius = 3000;

    /**
     * 社区简介
     */
    @Column(length = 500)
    private String description;

    /**
     * 申请理由
     */
    @Column(name = "application_reason", length = 500)
    private String applicationReason;

    /**
     * 申请状态
     * 0-待审核 1-审核通过 2-审核拒绝
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
     * 创建的社区ID（审核通过后自动生成）
     */
    @Column(name = "created_community_id")
    private Long createdCommunityId;

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

