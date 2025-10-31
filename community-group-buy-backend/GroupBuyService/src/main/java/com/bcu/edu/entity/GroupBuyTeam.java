package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 团实例实体（⭐核心实体）
 * 
 * <p>表名: group_buy_team
 * <p>描述: 团长发起的具体的团，包含团状态、成员数量、过期时间等
 * 
 * <p>v3.0特性：
 * <ul>
 *   <li>仅团长可发起（launcher_id = leader_id）</li>
 *   <li>自动关联团长的社区（community_id）</li>
 *   <li>社区优先推荐（ORDER BY CASE）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Entity
@Table(name = "group_buy_team")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyTeam {
    
    /**
     * 团ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long teamId;
    
    /**
     * 团号（唯一，格式：T20251031001）
     */
    @Column(name = "team_no", unique = true, nullable = false, length = 32)
    private String teamNo;
    
    /**
     * 关联活动ID（物理外键）
     */
    @Column(name = "activity_id", nullable = false)
    private Long activityId;
    
    /**
     * 发起人用户ID（跨库关联到user_service_db.sys_user）
     * v3.0: 仅团长可发起，launcher_id = leader_id
     */
    @Column(name = "launcher_id", nullable = false)
    private Long launcherId;
    
    /**
     * 归属团长ID（配送团点，跨库关联到user_service_db.sys_user）
     * v3.0: launcher_id = leader_id
     */
    @Column(name = "leader_id", nullable = false)
    private Long leaderId;
    
    /**
     * 归属社区ID（v3.0新增，跨库关联到leader_service_db.community）
     * 团长发起时自动关联团长的社区，用于社区优先推荐
     */
    @Column(name = "community_id")
    private Long communityId;
    
    /**
     * 成团人数（从活动复制）
     */
    @Column(name = "required_num", nullable = false)
    private Integer requiredNum;
    
    /**
     * 当前人数
     */
    @Column(name = "current_num", nullable = false)
    private Integer currentNum = 0;
    
    /**
     * 团状态
     * 0-拼团中；1-已成团；2-已失败
     */
    @Column(name = "team_status", nullable = false)
    private Integer teamStatus = 0;
    
    /**
     * 成团时间
     */
    @Column(name = "success_time")
    private LocalDateTime successTime;
    
    /**
     * 过期时间（24小时后）
     */
    @Column(name = "expire_time", nullable = false)
    private LocalDateTime expireTime;
    
    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    @PrePersist
    protected void onCreate() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (currentNum == null) {
            currentNum = 0;
        }
        if (teamStatus == null) {
            teamStatus = 0;
        }
        if (expireTime == null) {
            expireTime = LocalDateTime.now().plusHours(24);
        }
    }
}

