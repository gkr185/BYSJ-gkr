package com.bcu.edu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成员信息响应DTO
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoResponse {
    /**
     * 参团记录ID
     */
    private Long memberId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名（冗余）
     */
    private String username;
    
    /**
     * 真实姓名（冗余）
     */
    private String realName;
    
    /**
     * 头像（冗余）
     */
    private String avatar;
    
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
     * 参团状态（0-待支付/1-已支付/2-已成团/3-已取消）
     */
    private Integer status;
    
    /**
     * 参团状态描述
     */
    private String statusDesc;

    /**
     * 购买商品数量
     */
    private Integer productQuantity;
}

