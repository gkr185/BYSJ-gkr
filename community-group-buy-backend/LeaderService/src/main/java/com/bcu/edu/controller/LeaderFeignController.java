package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.service.LeaderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 团长Feign接口控制器（OrderService专用）
 *
 * @author 耿康瑞
 * @date 2025-11-01
 * @description 供OrderService调用的团长验证接口
 */
@Slf4j
@RestController
@RequestMapping("/api/leader/feign")
@RequiredArgsConstructor
@Tag(name = "团长Feign接口", description = "供OrderService调用的内部接口")
public class LeaderFeignController {

    private final LeaderApplicationService leaderApplicationService;

    /**
     * 验证团长是否存在
     * 供OrderService调用
     * 
     * GET /api/leader/feign/validate/{leaderId}
     */
    @GetMapping("/validate/{leaderId}")
    @Operation(summary = "验证团长是否存在", description = "OrderService调用，验证团长ID是否有效")
    public Result<Boolean> validateLeader(
            @Parameter(description = "团长ID（用户ID）") @PathVariable Long leaderId
    ) {
        log.info("[Feign-OrderService] 验证团长是否存在：leaderId={}", leaderId);

        boolean exists = leaderApplicationService.getLeaderByUserId(leaderId).isPresent();
        
        log.info("[Feign-OrderService] 团长{}验证结果：{}", leaderId, exists);
        
        return Result.success(exists);
    }
}

