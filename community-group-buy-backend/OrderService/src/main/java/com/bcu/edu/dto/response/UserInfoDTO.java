package com.bcu.edu.dto.response;

import lombok.Data;

/**
 * 用户信息DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
public class UserInfoDTO {

    private Long userId;

    private String username;

    private String realName;

    private String phone;

    private Integer role;
}

