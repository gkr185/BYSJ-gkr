package com.bcu.edu.common.feign;

import com.bcu.edu.common.dto.OperationLogDTO;
import com.bcu.edu.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 日志服务 Feign 客户端
 * 调用 UserService 保存操作日志
 * 
 * @author AI Assistant
 * @since 2025-10-31
 */
@FeignClient(
    name = "UserService",
    contextId = "logFeignClient",  // 指定唯一的 contextId，避免与其他 UserService 客户端冲突
    path = "/feign/log",
    fallback = LogFeignClientFallback.class
)
public interface LogFeignClient {
    
    /**
     * 保存操作日志
     * 
     * @param logDTO 日志数据
     * @return 保存结果
     */
    @PostMapping("/save")
    Result<Void> saveLog(@RequestBody OperationLogDTO logDTO);
}

