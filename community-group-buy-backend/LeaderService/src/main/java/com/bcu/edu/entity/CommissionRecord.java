package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 佣金记录实体类
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 佣金记录表 - 存储团长的佣金明细
 * 
 * 业务流程：
 * 1. 订单完成时，OrderService调用LeaderService生成佣金记录
 * 2. 佣金记录状态为"待结算"
 * 3. 定时任务（每月1号）统一结算，调用UserService增加团长余额
 * 4. 结算成功后，状态变为"已结算"
 */
@Entity
@Table(name = "commission_record", indexes = {
        @Index(name = "idx_leader_id", columnList = "leader_id"),
        @Index(name = "idx_order_id", columnList = "order_id", unique = true),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRecord {

    /**
     * 佣金记录ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commission_id")
    private Long recordId;

    /**
     * 团长ID（group_leader_store.leader_id）
     */
    @Column(name = "leader_id", nullable = false)
    private Long leaderId;

    /**
     * 团长姓名（冗余字段）
     */
    @Column(name = "leader_name", length = 50)
    private String leaderName;

    /**
     * 订单ID（order_service_db.order.order_id）
     * 外键关联OrderService的订单表
     * 一个订单只生成一条佣金记录
     */
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    /**
     * 订单金额（单位：元）
     */
    @Column(name = "order_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal orderAmount;

    /**
     * 佣金比例（百分比）
     * 示例：10.00 表示10%
     */
    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    /**
     * 佣金金额（单位：元）
     * 计算公式：commission_amount = order_amount * commission_rate / 100
     */
    @Column(name = "commission_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    /**
     * 佣金状态
     * 0-待结算 1-已结算 2-结算失败
     */
    @Column(nullable = false)
    private Integer status = 0;

    /**
     * 结算时间
     */
    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    /**
     * 结算批次号（用于批量结算）
     * 格式：YYYYMMDD（例如：20251101）
     */
    @Column(name = "settlement_batch", length = 20)
    private String settlementBatch;

    /**
     * 备注
     */
    @Column(length = 255)
    private String remark;

    /**
     * 创建时间（订单完成时间）
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

