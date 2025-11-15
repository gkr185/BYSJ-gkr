package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.service.LeaderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

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
     * 验证团长是否存在且可用（⭐修复：排除已停用团长）
     * 供OrderService调用
     * 
     * <p>业务规则：
     * <ul>
     *   <li>团长必须存在</li>
     *   <li>团长状态必须为正常运营（status=1）</li>
     *   <li>排除待审核（status=0）和已停用（status=2）的团长</li>
     * </ul>
     * 
     * GET /api/leader/feign/validate/{leaderId}
     */
    @GetMapping("/validate/{leaderId}")
    @Operation(summary = "验证团长是否存在且可用", description = "OrderService调用，验证团长ID是否有效且未停用")
    public Result<Boolean> validateLeader(
            @Parameter(description = "团长ID（用户ID）") @PathVariable Long leaderId
    ) {
        log.info("[Feign-OrderService] 验证团长是否存在且可用：leaderId={}", leaderId);

        // 检查团长是否存在并获取状态
        var leaderOptional = leaderApplicationService.getLeaderByUserId(leaderId);
        
        if (leaderOptional.isEmpty()) {
            log.warn("[Feign-OrderService] 团长不存在：leaderId={}", leaderId);
            return Result.success(false);
        }
        
        // 检查团长状态（只有status=1正常运营才可用）
        Integer status = leaderOptional.get().getStatus();
        boolean isValid = status != null && status == 1;
        
        if (!isValid) {
            log.warn("[Feign-OrderService] 团长已停用或待审核：leaderId={}, status={}", leaderId, status);
        }
        
        log.info("[Feign-OrderService] 团长{}验证结果：{}, status={}", leaderId, isValid, status);
        
        return Result.success(isValid);
    }

    /**
     * 根据用户地址自动匹配最近的可用团长（⭐新增接口）
     * 供OrderService调用（普通购买时自动分配团长）
     * 
     * <p>业务规则：
     * <ul>
     *   <li>根据用户地址坐标匹配最近的社区</li>
     *   <li>查询该社区的可用团长（status=1）</li>
     *   <li>返回第一个可用团长</li>
     * </ul>
     * 
     * GET /api/leader/feign/match?latitude=39.9042&longitude=116.4074
     */
    @GetMapping("/match")
    @Operation(summary = "自动匹配可用团长", description = "根据用户地址坐标匹配最近的可用团长")
    public Result<Long> matchNearestActiveLeader(
            @Parameter(description = "纬度") @RequestParam BigDecimal latitude,
            @Parameter(description = "经度") @RequestParam BigDecimal longitude
    ) {
        log.info("[Feign-OrderService] 自动匹配可用团长：latitude={}, longitude={}", latitude, longitude);

        try {
            Long leaderId = leaderApplicationService.matchNearestActiveLeader(latitude, longitude);
            
            if (leaderId != null) {
                log.info("[Feign-OrderService] 匹配成功：leaderId={}", leaderId);
                return Result.success(leaderId);
            } else {
                log.warn("[Feign-OrderService] 未找到可用团长：latitude={}, longitude={}", latitude, longitude);
                return Result.error("您的位置附近暂无可用团长，请稍后重试或联系客服");
            }
        } catch (Exception e) {
            log.error("[Feign-OrderService] 匹配团长失败", e);
            return Result.error("匹配团长失败: " + e.getMessage());
        }
    }
}

