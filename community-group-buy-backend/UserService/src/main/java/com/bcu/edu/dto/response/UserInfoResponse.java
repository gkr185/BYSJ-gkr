package com.bcu.edu.dto.response;

import com.bcu.edu.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private Long userId;
    private String username;
    private Integer role;
    private String roleName;
    private String realName;
    private String phone;
    private String wxOpenid;
    private String avatar;
    private Long communityId;  // v3.0新增
    private String communityName;  // v3.0新增（跨库查询）
    private Integer status;
    private String statusName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 从实体类转换
     */
    public static UserInfoResponse fromEntity(SysUser user) {
        return UserInfoResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .roleName(getRoleName(user.getRole()))
                .realName(user.getRealName())
                .phone(user.getPhone())
                .wxOpenid(user.getWxOpenid())
                .avatar(user.getAvatar())
                .communityId(user.getCommunityId())
                // communityName需要跨服务调用LeaderService获取，暂时为null
                .status(user.getStatus())
                .statusName(getStatusName(user.getStatus()))
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    private static String getRoleName(Integer role) {
        if (role == null) return null;
        return switch (role) {
            case 1 -> "普通用户";
            case 2 -> "团长";
            case 3 -> "管理员";
            default -> "未知";
        };
    }

    private static String getStatusName(Integer status) {
        if (status == null) return null;
        return status == 1 ? "正常" : "禁用";
    }
}

