package com.bcu.edu.controller;

import com.bcu.edu.common.dto.OperationLogDTO;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 日志服务 Feign 接口控制器
 * 提供给其他微服务调用的日志保存接口
 * 
 * @author AI Assistant
 * @since 2025-10-31
 */
@RestController
@RequestMapping("/feign/log")
@Tag(name = "日志服务（内部调用）", description = "供微服务间调用的日志接口")
@Slf4j
public class LogController {

    @Autowired
    private LogService logService;

    /**
     * 保存操作日志
     * 供其他微服务通过Feign调用
     * 
     * @param logDTO 日志数据
     * @return 保存结果
     */
    @PostMapping("/save")
    @Operation(summary = "保存操作日志", description = "供其他微服务通过Feign调用保存日志")
    public Result<Void> saveLog(@RequestBody OperationLogDTO logDTO) {
        try {
            log.debug("收到日志保存请求: module={}, operation={}, user={}", 
                    logDTO.getModule(), 
                    logDTO.getOperation(), 
                    logDTO.getUsername());
            
            // 异步保存日志
            logService.saveLog(logDTO);
            
            return Result.success();
        } catch (Exception e) {
            log.error("保存日志失败", e);
            // 即使失败也返回成功，避免影响调用方业务
            return Result.success();
        }
    }
}

