package com.bcu.edu.controller;

import com.bcu.edu.common.result.PageResult;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.CommissionRecord;
import com.bcu.edu.service.CommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 佣金管理Controller
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 面向团长和管理员的佣金查询接口
 */
@Slf4j
@RestController
@RequestMapping("/api/commission")
@RequiredArgsConstructor
@Tag(name = "佣金管理", description = "佣金查询、统计、结算")
public class CommissionController {

    private final CommissionService commissionService;

    /**
     * 【团长】查询我的佣金记录
     * 
     * GET /api/commission/my?leaderId=1&status=0&page=0&limit=10
     */
    @GetMapping("/my")
    @Operation(summary = "查询我的佣金记录", description = "团长分页查询自己的佣金记录")
    public Result<PageResult<CommissionRecord>> getMyCommissions(
            @Parameter(description = "团长ID") @RequestParam Long leaderId,
            @Parameter(description = "结算状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer limit
    ) {
        log.info("团长{}查询佣金记录: status={}, page={}, limit={}", leaderId, status, page, limit);
        PageResult<CommissionRecord> result = commissionService.getCommissionsByLeaderPage(leaderId, status, page, limit);
        return Result.success(result);
    }

    /**
     * 【团长】查询我的佣金统计
     * 
     * GET /api/commission/my/summary?leaderId=1
     */
    @GetMapping("/my/summary")
    @Operation(summary = "查询佣金统计", description = "团长查询待结算/已结算佣金总额")
    public Result<Map<String, Object>> getMyCommissionSummary(
            @Parameter(description = "团长ID") @RequestParam Long leaderId
    ) {
        log.info("团长{}查询佣金统计", leaderId);

        BigDecimal pendingCommission = commissionService.getPendingCommissionByLeader(leaderId);
        BigDecimal settledCommission = commissionService.getSettledCommissionByLeader(leaderId);
        BigDecimal totalCommission = pendingCommission.add(settledCommission);

        Map<String, Object> summary = new HashMap<>();
        summary.put("pendingCommission", pendingCommission);  // 待结算佣金
        summary.put("settledCommission", settledCommission);  // 已结算佣金
        summary.put("totalCommission", totalCommission);      // 累计佣金

        return Result.success(summary);
    }

    /**
     * 【管理员】查询所有待结算佣金记录
     * 
     * GET /api/commission/pending
     */
    @GetMapping("/pending")
    @Operation(summary = "查询待结算佣金", description = "管理员查询所有待结算的佣金记录")
    public Result<List<CommissionRecord>> getAllPendingCommissions() {
        log.info("管理员查询待结算佣金");
        List<CommissionRecord> records = commissionService.getAllPendingCommissions();
        return Result.success(records);
    }

    /**
     * 【管理员】根据结算批次号查询佣金记录
     * 
     * GET /api/commission/batch/{settlementBatch}
     */
    @GetMapping("/batch/{settlementBatch}")
    @Operation(summary = "查询结算批次", description = "根据结算批次号查询佣金记录")
    public Result<List<CommissionRecord>> getCommissionsByBatch(
            @Parameter(description = "结算批次号") @PathVariable String settlementBatch
    ) {
        log.info("管理员查询结算批次：{}", settlementBatch);
        List<CommissionRecord> records = commissionService.getCommissionsBySettlementBatch(settlementBatch);
        return Result.success(records);
    }

    /**
     * 【管理员】查询所有已结算佣金记录
     * 
     * GET /api/commission/settled
     */
    @GetMapping("/settled")
    @Operation(summary = "查询已结算佣金", description = "管理员查询所有已结算的佣金记录")
    public Result<List<CommissionRecord>> getAllSettledCommissions() {
        log.info("管理员查询已结算佣金");
        List<CommissionRecord> records = commissionService.getAllSettledCommissions();
        return Result.success(records);
    }

    /**
     * 【管理员】手动触发佣金结算
     * 
     * POST /api/commission/settle
     */
    @PostMapping("/settle")
    @Operation(summary = "手动结算佣金", description = "管理员手动触发佣金结算")
    public Result<Map<String, Object>> manualSettleCommissions() {
        log.info("管理员手动触发佣金结算");

        try {
            String settlementBatch = commissionService.generateSettlementBatch() + "_MANUAL";
            int settledCount = commissionService.settleCommissions(settlementBatch);

            Map<String, Object> result = new HashMap<>();
            result.put("settlementBatch", settlementBatch);
            result.put("settledCount", settledCount);

            return Result.success("佣金结算成功", result);
        } catch (Exception e) {
            log.error("佣金结算失败：{}", e.getMessage());
            return Result.error("佣金结算失败：" + e.getMessage());
        }
    }
}

