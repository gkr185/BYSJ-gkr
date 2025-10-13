package com.bcu.edu.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户信息更新请求
 */
@Data
public class UserUpdateRequest {

    @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    private String password;

    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;

    @Size(min = 11, max = 11, message = "手机号必须为11位")
    private String phone;

    private String avatar;

    private String wxOpenid;
}

