package com.bcu.edu.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统操作日志实体类
 * 对应数据库表：sys_operation_log
 * 
 * @author AI Assistant
 * @since 2025-10-14
 */
@Entity
@Table(name = "sys_operation_log", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_module", columnList = "module"),
    @Index(name = "idx_create_time", columnList = "create_time"),
    @Index(name = "idx_result", columnList = "result")
})
@Data
@NoArgsConstructor
public class SysOperationLog {
    
    /**
     * 日志ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;
    
    /**
     * 操作人ID
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 操作人用户名
     */
    @Column(name = "username", length = 50)
    private String username;
    
    /**
     * 操作内容描述
     * 示例: "创建用户", "删除订单"
     */
    @Column(name = "operation", nullable = false, length = 255)
    private String operation;
    
    /**
     * 操作模块
     * 示例: "用户管理", "订单管理"
     */
    @Column(name = "module", nullable = false, length = 50)
    private String module;
    
    /**
     * 方法名（完整路径）
     * 示例: "com.bcu.edu.controller.UserController.createUser"
     */
    @Column(name = "method", length = 500)
    private String method;
    
    /**
     * 请求参数（JSON格式）
     */
    @Column(name = "params", columnDefinition = "TEXT")
    private String params;
    
    /**
     * 操作结果
     * SUCCESS - 成功
     * FAIL - 失败
     */
    @Column(name = "result", length = 20)
    private String result;
    
    /**
     * 错误信息（操作失败时记录）
     */
    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;
    
    /**
     * 执行时长（毫秒）
     */
    @Column(name = "duration")
    private Integer duration;
    
    /**
     * 操作IP地址
     */
    @Column(name = "ip", length = 50)
    private String ip;
    
    /**
     * 操作时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 实体创建前自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
    
    /**
     * 操作结果枚举
     */
    public static class Result {
        public static final String SUCCESS = "SUCCESS";
        public static final String FAIL = "FAIL";
    }
}

