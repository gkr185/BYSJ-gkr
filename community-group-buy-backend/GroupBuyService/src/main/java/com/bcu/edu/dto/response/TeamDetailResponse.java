package com.bcu.edu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 团详情响应DTO
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDetailResponse {
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
     * 活动名称（冗余）
     */
    private String activityName;
    
    /**
     * 拼团价（冗余）
     */
    private BigDecimal groupPrice;
    
    /**
     * 团长ID
     */
    private Long leaderId;
    
    /**
     * 团长姓名（冗余）
     */
    private String leaderName;
    
    /**
     * 社区ID
     */
    private Long communityId;
    
    /**
     * 社区名称（冗余，v3.0）
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
     * 还差几人（计算字段）
     */
    private Integer remainNum;
    
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
     * 成员列表
     */
    private List<MemberInfoResponse> members;
    
    /**
     * 分享链接
     */
    private String shareLink;
}

