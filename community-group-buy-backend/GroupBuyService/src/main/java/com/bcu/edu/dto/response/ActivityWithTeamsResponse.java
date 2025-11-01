package com.bcu.edu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动及其团队列表响应DTO
 * 
 * <p>用于商品详情页展示拼团活动及进行中的团
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityWithTeamsResponse {
    /**
     * 活动ID
     */
    private Long activityId;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 拼团价
     */
    private BigDecimal groupPrice;
    
    /**
     * 成团人数
     */
    private Integer requiredNum;
    
    /**
     * 最大团人数
     */
    private Integer maxNum;
    
    /**
     * 活动开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 活动结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 活动状态
     */
    private Integer status;
    
    /**
     * 该活动的团列表（进行中的团，最多10个）
     */
    private List<TeamInfo> teams;
    
    /**
     * 团简化信息（用于商品详情页展示）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamInfo {
        /**
         * 团ID
         */
        private Long teamId;
        
        /**
         * 团号
         */
        private String teamNo;
        
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
         * 过期时间
         */
        private LocalDateTime expireTime;
        
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }
}

