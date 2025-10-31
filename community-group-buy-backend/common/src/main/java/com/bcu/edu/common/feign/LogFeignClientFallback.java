package com.bcu.edu.common.feign;

import com.bcu.edu.common.dto.OperationLogDTO;
import com.bcu.edu.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 日志服务 Feign 客户端降级处理
 * 当日志服务不可用时，不影响业务执行
 * 
 * @author AI Assistant
 * @since 2025-10-31
 */
@Slf4j
@Component
public class LogFeignClientFallback implements LogFeignClient {
    
    @Override
    public Result<Void> saveLog(OperationLogDTO logDTO) {
        // 日志保存失败时，记录本地日志，不影响业务
        log.warn("日志服务不可用，本地记录: module={}, operation={}, user={}", 
                logDTO.getModule(), 
                logDTO.getOperation(), 
                logDTO.getUsername());
        
        // 返回成功，避免影响业务
        return Result.success();
    }
}

