package com.bcu.edu.common.dto;

import com.bcu.edu.common.entity.SysOperationLog;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志数据传输对象
 */
@Data
public class OperationLogDTO {

    /**
     * 日志ID
     */
    private Long logId;

    /**
     * 操作人用户名
     */
    private String username;

    /**
     * 操作内容
     */
    private String operation;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 方法名
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
     * IP地址
     */
    private String ip;

    /**
     * 执行时长（毫秒）
     */
    private Integer duration;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;

    /**
     * 从实体类转换为DTO
     */
    public static OperationLogDTO fromEntity(SysOperationLog entity) {
        if (entity == null) {
            return null;
        }

        OperationLogDTO dto = new OperationLogDTO();
        dto.setLogId(entity.getLogId());
        dto.setUsername(entity.getUsername());
        dto.setOperation(entity.getOperation());
        dto.setModule(entity.getModule());
        dto.setMethod(entity.getMethod());
        dto.setParams(entity.getParams());
        dto.setResult(entity.getResult());
        dto.setIp(entity.getIp());
        dto.setDuration(entity.getDuration());
        dto.setErrorMsg(entity.getErrorMsg());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }
}

