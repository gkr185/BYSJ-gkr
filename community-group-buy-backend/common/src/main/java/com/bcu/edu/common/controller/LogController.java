package com.bcu.edu.common.controller;

import com.bcu.edu.common.dto.OperationLogDTO;
import com.bcu.edu.common.dto.OperationLogQuery;
import com.bcu.edu.common.result.PageResult;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.common.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 系统日志管理控制器
 */
@RestController
@RequestMapping("/api/admin/logs")
@Tag(name = "系统日志管理", description = "操作日志查询、导出、模块列表")
@Slf4j
public class LogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/operations")
    @Operation(summary = "分页查询操作日志", description = "支持多条件筛选：操作人、模块、时间范围、关键词")
    public Result<PageResult<OperationLogDTO>> queryLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        // 构建查询条件
        OperationLogQuery query = new OperationLogQuery();
        query.setUserId(userId);
        query.setUsername(username);
        query.setModule(module);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setKeyword(keyword);
        query.setPage(page);
        query.setSize(size);

        // 执行查询
        PageResult<OperationLogDTO> result = operationLogService.queryLogs(query);
        return Result.success(result);
    }

    /**
     * 导出操作日志为Excel
     */
    @GetMapping("/export")
    @Operation(summary = "导出操作日志为Excel", description = "根据查询条件导出Excel文件（最大10000条）")
    public void exportLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            HttpServletResponse response
    ) {
        try {
            // 构建查询条件
            OperationLogQuery query = new OperationLogQuery();
            query.setUserId(userId);
            query.setModule(module);
            query.setStartDate(startDate);
            query.setEndDate(endDate);

            // 导出Excel
            byte[] data = operationLogService.exportLogs(query);

            // 生成文件名（带时间戳）
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "operation_logs_" + timestamp + ".xlsx";

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            // 写入响应流
            response.getOutputStream().write(data);
            response.getOutputStream().flush();

            log.info("导出操作日志成功: {}", filename);
        } catch (IOException e) {
            log.error("导出操作日志失败", e);
            throw new RuntimeException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 获取操作模块列表
     */
    @GetMapping("/modules")
    @Operation(summary = "获取操作模块列表", description = "获取所有已记录的操作模块（用于筛选下拉框）")
    public Result<List<String>> getModules() {
        List<String> modules = operationLogService.getModules();
        return Result.success(modules);
    }
}

