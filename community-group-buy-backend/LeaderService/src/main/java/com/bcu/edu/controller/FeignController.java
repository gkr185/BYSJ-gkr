package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.CommissionRecord;
import com.bcu.edu.entity.Community;
import com.bcu.edu.entity.GroupLeaderStore;
import com.bcu.edu.service.CommissionService;
import com.bcu.edu.service.CommunityService;
import com.bcu.edu.service.LeaderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Feign调用接口控制器
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 专门提供给其他微服务调用的内部接口
 */
@Slf4j
@RestController
@RequestMapping("/feign")
@RequiredArgsConstructor
@Tag(name = "Feign调用接口", description = "供其他微服务调用的内部接口")
public class FeignController {

    private final CommunityService communityService;
    private final LeaderApplicationService leaderApplicationService;
    private final CommissionService commissionService;

    // ========== 社区相关接口 ==========

    /**
     * 根据社区ID获取社区信息
     * 供OrderService、GroupBuyService调用
     * 
     * GET /feign/community/{communityId}
     */
    @GetMapping("/community/{communityId}")
    @Operation(summary = "获取社区信息", description = "根据社区ID查询社区详情")
    public Result<Community> getCommunityById(
            @Parameter(description = "社区ID") @PathVariable Long communityId
    ) {
        log.info("[Feign] 查询社区：communityId={}", communityId);

        return communityService.getCommunityById(communityId)
                .map(Result::success)
                .orElse(Result.error("社区不存在"));
    }

    /**
     * 根据用户经纬度匹配最近的社区
     * 供UserService调用（用户注册、选择地址时自动匹配社区）
     * 
     * GET /feign/community/nearest?latitude=39.9042&longitude=116.4074
     */
    @GetMapping("/community/nearest")
    @Operation(summary = "匹配最近的社区", description = "根据用户经纬度匹配最近的社区")
    public Result<Community> findNearestCommunity(
            @Parameter(description = "纬度") @RequestParam BigDecimal latitude,
            @Parameter(description = "经度") @RequestParam BigDecimal longitude
    ) {
        log.info("[Feign] 匹配最近社区：latitude={}, longitude={}", latitude, longitude);

        return communityService.findNearestCommunity(latitude, longitude)
                .map(Result::success)
                .orElse(Result.error("您的位置不在任何社区服务范围内"));
    }

    /**
     * 验证社区是否存在
     * 
     * GET /feign/community/exists/{communityId}
     */
    @GetMapping("/community/exists/{communityId}")
    @Operation(summary = "验证社区是否存在")
    public Result<Boolean> existsCommunity(
            @Parameter(description = "社区ID") @PathVariable Long communityId
    ) {
        log.info("[Feign] 验证社区是否存在：communityId={}", communityId);

        boolean exists = communityService.getCommunityById(communityId).isPresent();
        return Result.success(exists);
    }

    // ========== 团长相关接口 ==========

    /**
     * 根据团长ID获取团长信息
     * 供OrderService、GroupBuyService调用
     * 
     * GET /feign/leader/{leaderId}
     */
    @GetMapping("/leader/{leaderId}")
    @Operation(summary = "获取团长信息", description = "根据团长ID查询团长详情")
    public Result<GroupLeaderStore> getLeaderByUserId(
            @Parameter(description = "团长ID（用户ID）") @PathVariable Long leaderId
    ) {
        log.info("[Feign] 查询团长：leaderId={}", leaderId);

        return leaderApplicationService.getLeaderByUserId(leaderId)
                .map(Result::success)
                .orElse(Result.error("团长不存在"));
    }

    /**
     * 根据社区ID获取该社区的所有团长
     * 供GroupBuyService调用（创建拼团时查询社区团长）
     * 
     * GET /feign/community/{communityId}/leaders
     */
    @GetMapping("/community/{communityId}/leaders")
    @Operation(summary = "查询社区的团长列表", description = "根据社区ID查询该社区的所有团长")
    public Result<List<GroupLeaderStore>> getLeadersByCommunity(
            @Parameter(description = "社区ID") @PathVariable Long communityId
    ) {
        log.info("[Feign] 查询社区团长：communityId={}", communityId);

        List<GroupLeaderStore> leaders = leaderApplicationService.getLeadersByCommunity(communityId);
        return Result.success(leaders);
    }

    /**
     * 验证用户是否是团长
     * 供GroupBuyService调用（验证是否有权限发起拼团）
     * 
     * GET /feign/leader/check/{userId}
     */
    @GetMapping("/leader/check/{userId}")
    @Operation(summary = "验证是否是团长", description = "验证用户是否是团长")
    public Result<Boolean> isLeader(
            @Parameter(description = "用户ID") @PathVariable Long userId
    ) {
        log.info("[Feign] 验证是否是团长：userId={}", userId);

        boolean isLeader = leaderApplicationService.getLeaderByUserId(userId).isPresent();
        return Result.success(isLeader);
    }

    // ========== 佣金相关接口 ==========

    /**
     * 【核心接口】生成佣金记录
     * 供OrderService调用（订单完成时触发）
     * 
     * POST /feign/commission/generate
     */
    @PostMapping("/commission/generate")
    @Operation(summary = "生成佣金记录", description = "订单完成时调用，生成佣金记录")
    public Result<CommissionRecord> generateCommission(
            @Parameter(description = "团长ID") @RequestParam Long leaderId,
            @Parameter(description = "订单ID") @RequestParam Long orderId,
            @Parameter(description = "订单金额") @RequestParam BigDecimal orderAmount
    ) {
        log.info("[Feign] 生成佣金记录：leaderId={}, orderId={}, orderAmount={}", leaderId, orderId, orderAmount);

        try {
            CommissionRecord record = commissionService.generateCommission(leaderId, orderId, orderAmount);
            return Result.success("佣金记录生成成功", record);
        } catch (IllegalArgumentException e) {
            log.error("[Feign] 生成佣金记录失败：{}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询团长的待结算佣金总额
     * 供团长端查询
     * 
     * GET /feign/commission/pending/{leaderId}
     */
    @GetMapping("/commission/pending/{leaderId}")
    @Operation(summary = "查询待结算佣金", description = "查询团长的待结算佣金总额")
    public Result<BigDecimal> getPendingCommission(
            @Parameter(description = "团长ID") @PathVariable Long leaderId
    ) {
        log.info("[Feign] 查询待结算佣金：leaderId={}", leaderId);

        BigDecimal pendingCommission = commissionService.getPendingCommissionByLeader(leaderId);
        return Result.success(pendingCommission);
    }

    // ========== 团长团点相关接口（⭐新增 - 供DeliveryService调用）==========

    /**
     * 获取团长团点信息（包含坐标）
     * 供DeliveryService调用（批量发货时获取团长团点坐标）
     * 
     * GET /api/leader/feign/store/{leaderId}
     */
    @GetMapping("/leader/store/{leaderId}")
    @Operation(summary = "获取团长团点信息", description = "根据团长ID查询团点信息（含坐标）")
    public Result<GroupLeaderStore> getLeaderStore(
            @Parameter(description = "团长ID（用户ID）") @PathVariable Long leaderId
    ) {
        log.info("[Feign] DeliveryService调用: 获取团长团点信息，leaderId={}", leaderId);

        return leaderApplicationService.getLeaderByUserId(leaderId)
                .map(Result::success)
                .orElse(Result.error("团长团点不存在"));
    }

    /**
     * 批量获取团长团点信息（⭐新增接口 - 供DeliveryService调用）
     * 
     * POST /api/leader/feign/store/batch
     */
    @PostMapping("/leader/store/batch")
    @Operation(summary = "批量获取团长团点信息", description = "批量查询团长团点信息（含坐标）")
    public Result<List<GroupLeaderStore>> batchGetLeaderStores(@RequestBody List<Long> leaderIds) {
        log.info("[Feign] DeliveryService调用: 批量获取团长团点信息，leaderIds={}", leaderIds);

        try {
            List<GroupLeaderStore> stores = leaderApplicationService.batchGetLeaderStores(leaderIds);
            log.info("批量获取团长团点成功: 共{}个团点", stores.size());
            return Result.success(stores);
        } catch (Exception e) {
            log.error("批量获取团长团点失败", e);
            return Result.error("批量获取团长团点失败: " + e.getMessage());
        }
    }
}

