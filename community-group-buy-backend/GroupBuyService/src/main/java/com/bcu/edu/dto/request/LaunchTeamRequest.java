package com.bcu.edu.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发起拼团请求DTO（⭐核心请求）
 * 
 * <p>v3.0特性：仅团长可发起，自动关联社区
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Data
public class LaunchTeamRequest {
    
    /**
     * 用户ID（从JWT Token解析）
     */
    private Long userId;
    
    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
    
    /**
     * 是否立即参与拼团（默认false）
     */
    private Boolean joinImmediately = false;
    
    /**
     * 收货地址ID（如果参与则必填）
     */
    private Long addressId;
    
    /**
     * 购买数量（如果参与则必填，默认1）
     */
    private Integer quantity = 1;

    /**
     * 团购持续时间（小时，默认24小时）
     */
    private Integer durationHours = 24;
}

