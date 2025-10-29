package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

/**
 * 用户基础信息表
 * 存储所有用户（普通用户、团长、管理员）的基础信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sys_user", indexes = {
        @Index(name = "uk_username", columnList = "username", unique = true),
        @Index(name = "uk_phone", columnList = "phone", unique = true),
        @Index(name = "uk_wx_openid", columnList = "wx_openid", unique = true),
        @Index(name = "idx_community_id", columnList = "community_id")
})
@Comment("用户基础信息表")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @Comment("用户唯一ID")
    private Long userId;

    @Column(name = "username", nullable = false, length = 50, unique = true)
    @Comment("登录账号")
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    @Comment("加密密码（SHA256+盐值）")
    private String password;

    @Column(name = "role", nullable = false)
    @Comment("角色（1-普通用户；2-团长；3-管理员）")
    private Integer role;

    @Column(name = "real_name", length = 50)
    @Comment("真实姓名")
    private String realName;

    @Column(name = "phone", length = 128, unique = true)
    @Comment("手机号（AES加密存储）")
    private String phone;

    @Column(name = "wx_openid", length = 100, unique = true)
    @Comment("微信OpenID")
    private String wxOpenid;

    @Column(name = "avatar", length = 255)
    @Comment("头像URL")
    private String avatar;

    @Column(name = "community_id")
    @Comment("归属社区ID（v3.0新增，跨库关联）")
    private Long communityId;

    @Column(name = "status", nullable = false)
    @Comment("状态（0-禁用；1-正常）")
    private Integer status = 1;

    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    @Comment("更新时间")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * 用户角色枚举
     */
    public enum Role {
        USER(1, "普通用户"),
        LEADER(2, "团长"),
        ADMIN(3, "管理员");

        private final Integer code;
        private final String desc;

        Role(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 用户状态枚举
     */
    public enum Status {
        DISABLED(0, "禁用"),
        ENABLED(1, "正常");

        private final Integer code;
        private final String desc;

        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
}

