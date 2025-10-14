package com.bcu.edu.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志查询条件
 */
@Data
public class OperationLogQuery {

    /**
     * 操作人ID
     */
    private Long userId;

    /**
     * 操作人用户名（模糊查询）
     */
    private String username;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 开始时间
     */
    private LocalDateTime startDate;

    /**
     * 结束时间
     */
    private LocalDateTime endDate;

    /**
     * 关键词（操作内容模糊查询）
     */
    private String keyword;

    /**
     * 页码（默认第1页）
     */
    private Integer page = 1;

    /**
     * 每页大小（默认10条）
     */
    private Integer size = 10;

    /**
     * 获取偏移量（用于分页）
     */
    public int getOffset() {
        return (page - 1) * size;
    }
}

