package com.bcu.edu.common.dto;

import com.bcu.edu.common.entity.SysOperationLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志数据传输对象
 * 用于微服务间传递日志数据
 * 
 * @author AI Assistant
 * @since 2025-10-31
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 日志ID（查询时使用）
     */
    private Long logId;
    
    /**
     * 操作人ID
     */
    private Long userId;
    
    /**
     * 操作人用户名
     */
    private String username;
    
    /**
     * 操作内容描述
     */
    private String operation;
    
    /**
     * 操作模块
     */
    private String module;
    
    /**
     * 方法名（完整路径）
     */
    private String method;
    
    /**
     * 请求参数（JSON格式）
     */
    private String params;
    
    /**
     * 操作结果（SUCCESS/FAIL）
     */
    private String result;
    
    /**
     * 错误信息
     */
    private String errorMsg;
    
    /**
     * 执行时长（毫秒）
     */
    private Integer duration;
    
    /**
     * 操作IP地址
     */
    private String ip;
    
    /**
     * 操作时间（查询时使用）
     */
    private LocalDateTime createTime;
    
    /**
     * 从实体对象转换为DTO
     * 用于查询结果转换
     * 
     * @param entity 操作日志实体
     * @return DTO对象
     */
    public static OperationLogDTO fromEntity(SysOperationLog entity) {
        if (entity == null) {
            return null;
        }
        
        return OperationLogDTO.builder()
                .logId(entity.getLogId())
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .operation(entity.getOperation())
                .module(entity.getModule())
                .method(entity.getMethod())
                .params(entity.getParams())
                .result(entity.getResult())
                .errorMsg(entity.getErrorMsg())
                .duration(entity.getDuration())
                .ip(entity.getIp())
                .createTime(entity.getCreateTime())
                .build();
    }
}
