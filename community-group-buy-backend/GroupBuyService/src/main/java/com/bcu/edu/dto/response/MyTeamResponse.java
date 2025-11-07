package com.bcu.edu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 我的拼团响应DTO
 * 包含团信息和用户的参团信息
 * 
 * @author 耿康瑞
 * @since 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyTeamResponse {
    // ========== 参团记录信息 ==========
    /**
     * 参团记录ID
     */
    private Long memberId;
    
    /**
     * 是否发起人（0-否/1-是）
     */
    private Integer isLauncher;
    
    /**
     * 支付金额
     */
    private BigDecimal payAmount;
    
    /**
     * 参团时间
     */
    private LocalDateTime joinTime;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    // ========== 团信息 ==========
    /**
     * 团ID
     */
    private Long teamId;
    
    /**
     * 团号
     */
    private String teamNo;
    
    /**
     * 活动ID
     */
    private Long activityId;
    
    /**
     * 活动名称
     */
    private String activityName;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 商品名称
     */
    private String productName;
    
    /**
     * 商品封面图
     */
    private String productCoverImg;
    
    /**
     * 商品原价
     */
    private BigDecimal productPrice;
    
    /**
     * 拼团价
     */
    private BigDecimal groupPrice;
    
    /**
     * 团长ID
     */
    private Long leaderId;
    
    /**
     * 团长姓名
     */
    private String leaderName;
    
    /**
     * 社区ID
     */
    private Long communityId;
    
    /**
     * 社区名称
     */
    private String communityName;
    
    /**
     * 成团人数
     */
    private Integer requiredNum;
    
    /**
     * 当前人数
     */
    private Integer currentNum;
    
    /**
     * 团状态（0-拼团中/1-已成团/2-已失败）
     */
    private Integer teamStatus;
    
    /**
     * 团状态描述
     */
    private String teamStatusDesc;
    
    /**
     * 成团时间
     */
    private LocalDateTime successTime;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 成员列表（拼团详情用）
     */
    private List<MemberInfoResponse> members;
    
    /**
     * 分享链接
     */
    private String shareLink;
}

