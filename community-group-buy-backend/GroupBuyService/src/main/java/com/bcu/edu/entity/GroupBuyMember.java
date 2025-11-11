package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 参团记录实体（⭐核心实体）
 * 
 * <p>表名: group_buy_member
 * <p>描述: 用户参团记录，包含订单关联、支付金额、参团状态
 * 
 * <p>关键特性：
 * <ul>
 *   <li>唯一索引（team_id + user_id）防止重复参团</li>
 *   <li>关联订单ID（跨库关联到order_service_db.order_main）</li>
 *   <li>is_launcher标识发起人（每个团只有1个）</li>
 *   <li>status状态机：0待支付→1已支付→2已成团/3已取消</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Entity
@Table(name = "group_buy_member", uniqueConstraints = {
    @UniqueConstraint(name = "uk_team_user", columnNames = {"team_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyMember {
    
    /**
     * 参团记录ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;
    
    /**
     * 团ID（物理外键）
     */
    @Column(name = "team_id", nullable = false)
    private Long teamId;
    
    /**
     * 参团用户ID（跨库关联到user_service_db.sys_user）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 关联订单ID（跨库关联到order_service_db.order_main）
     */
    @Column(name = "order_id")
    private Long orderId;
    
    /**
     * 是否发起人
     * 0-否；1-是（每个团只有一个发起人）
     */
    @Column(name = "is_launcher", nullable = false)
    private Integer isLauncher = 0;

    /**
     * 购买商品数量
     */
    @Column(name = "product_quantity", nullable = false)
    private Integer productQuantity = 1;
    
    /**
     * 支付金额
     */
    @Column(name = "pay_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payAmount;
    
    /**
     * 参团时间
     */
    @Column(name = "join_time", nullable = false, updatable = false)
    private LocalDateTime joinTime = LocalDateTime.now();
    
    /**
     * 参团状态
     * 0-待支付；1-已支付；2-已成团；3-已取消
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;
    
    @PrePersist
    protected void onCreate() {
        if (joinTime == null) {
            joinTime = LocalDateTime.now();
        }
        if (isLauncher == null) {
            isLauncher = 0;
        }
        if (status == null) {
            status = 0;
        }
    }
}

