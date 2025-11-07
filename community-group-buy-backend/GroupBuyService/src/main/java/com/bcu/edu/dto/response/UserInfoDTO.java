package com.bcu.edu.dto.response;

import lombok.Data;

/**
 * 用户信息DTO（Feign响应）
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Data
public class UserInfoDTO {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 角色（1-普通用户/2-团长/3-管理员）
     */
    private Integer role;
    
    /**
     * 归属社区ID（v3.0核心）
     */
    private Long communityId;
    
    /**
     * 状态（0-禁用/1-正常）
     */
    private Integer status;
}

