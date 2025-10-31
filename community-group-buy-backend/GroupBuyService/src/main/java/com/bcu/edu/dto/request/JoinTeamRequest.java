package com.bcu.edu.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 参团请求DTO（⭐核心请求）
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Data
public class JoinTeamRequest {
    
    /**
     * 用户ID（从JWT Token解析）
     */
    private Long userId;
    
    /**
     * 团ID
     */
    @NotNull(message = "团ID不能为空")
    private Long teamId;
    
    /**
     * 收货地址ID
     */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;
    
    /**
     * 购买数量（默认1）
     */
    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量最少为1")
    private Integer quantity = 1;
}

