package com.bcu.edu.controller;

import com.bcu.edu.common.dto.OperationLogQuery;
import com.bcu.edu.common.entity.SysOperationLog;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.service.LogService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端日志查询控制器
 * 提供日志查询、导出等功能
 * 
 * @author AI Assistant
 * @since 2025-10-31
 */
@RestController
@RequestMapping("/api/admin/logs")
@Tag(name = "日志管理", description = "管理端日志查询接口")
@Slf4j
public class AdminLogController {

    @Autowired
    private LogService logService;

    /**
     * 分页查询操作日志
     * 
     * @param page 页码
     * @param size 每页大小
     * @param userId 操作人ID
     * @param username 操作人用户名
     * @param module 操作模块
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param keyword 关键词
     * @return 分页日志列表
     */
    @GetMapping("/operations")
    @Operation(summary = "分页查询操作日志", description = "支持多条件筛选的分页查询")
    public Result<PageInfo<SysOperationLog>> getOperationLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String keyword) {
        
        log.debug("查询操作日志: page={}, size={}, module={}, username={}", 
                page, size, module, username);
        
        // 构建查询条件
        OperationLogQuery query = OperationLogQuery.builder()
                .userId(userId)
                .username(username)
                .module(module)
                .startDate(startDate)
                .endDate(endDate)
                .keyword(keyword)
                .build();
        
        // 分页查询
        PageInfo<SysOperationLog> pageInfo = logService.getOperationLogs(query, page, size);
        
        return Result.success(pageInfo);
    }

    /**
     * 导出操作日志
     * 
     * @param userId 操作人ID
     * @param module 操作模块
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param response HTTP响应
     */
    @GetMapping("/export")
    @Operation(summary = "导出操作日志", description = "导出Excel文件，最多导出10000条")
    public void exportOperationLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletResponse response) {
        
        log.info("导出操作日志: module={}, startDate={}, endDate={}", module, startDate, endDate);
        
        // 构建查询条件
        OperationLogQuery query = OperationLogQuery.builder()
                .userId(userId)
                .module(module)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        
        // 导出Excel
        logService.exportOperationLogs(query, response);
    }

    /**
     * 获取操作模块列表
     * 
     * @return 模块列表
     */
    @GetMapping("/modules")
    @Operation(summary = "获取操作模块列表", description = "获取所有已记录的操作模块")
    public Result<List<String>> getLogModules() {
        log.debug("获取操作模块列表");
        
        List<String> modules = logService.getLogModules();
        
        return Result.success(modules);
    }
}

