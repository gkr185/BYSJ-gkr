package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.GroupLeaderStore;
import com.bcu.edu.service.LeaderApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团长管理Controller
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 面向C端用户、团长和管理员的团长相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/leader")
@RequiredArgsConstructor
@Tag(name = "团长管理", description = "团长申请、查询、审核")
public class LeaderController {

    private final LeaderApplicationService leaderApplicationService;

    /**
     * 【用户】提交团长申请
     * 
     * 场景：用户申请成为团长
     * 
     * POST /api/leader/apply
     */
    @PostMapping("/apply")
    @Operation(summary = "提交团长申请", description = "用户申请成为团长")
    public Result<GroupLeaderStore> applyForLeader(@RequestBody GroupLeaderStore application) {
        log.info("用户{}提交团长申请", application.getLeaderId());

        try {
            GroupLeaderStore created = leaderApplicationService.submitApplication(application);
            return Result.success("申请提交成功，请等待审核", created);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 【用户】查询我的团长信息
     * 
     * GET /api/leader/my?userId=1
     */
    @GetMapping("/my")
    @Operation(summary = "查询我的团长信息", description = "用户查询自己的团长团点信息")
    public Result<GroupLeaderStore> getMyLeaderInfo(
            @Parameter(description = "用户ID") @RequestParam Long userId
    ) {
        log.info("用户{}查询自己的团长信息", userId);

        return leaderApplicationService.getLeaderByUserId(userId)
                .map(Result::success)
                .orElse(Result.error("您还不是团长"));
    }

    /**
     * 【用户/团长】根据社区ID查询该社区的所有团长
     * 
     * GET /api/leader/community/{communityId}
     */
    @GetMapping("/community/{communityId}")
    @Operation(summary = "查询社区的团长列表", description = "根据社区ID查询该社区的所有团长")
    public Result<List<GroupLeaderStore>> getLeadersByCommunity(
            @Parameter(description = "社区ID") @PathVariable Long communityId
    ) {
        log.info("查询社区{}的团长列表", communityId);
        List<GroupLeaderStore> leaders = leaderApplicationService.getLeadersByCommunity(communityId);
        return Result.success(leaders);
    }

    /**
     * 【用户/团长】根据ID查询团长详情
     * 
     * GET /api/leader/{storeId}
     */
    @GetMapping("/{storeId}")
    @Operation(summary = "查询团长详情", description = "根据团点ID查询团长详细信息")
    public Result<GroupLeaderStore> getLeaderById(
            @Parameter(description = "团点ID") @PathVariable Long storeId
    ) {
        log.info("查询团长详情：storeId={}", storeId);

        return leaderApplicationService.getLeaderById(storeId)
                .map(Result::success)
                .orElse(Result.error("团长不存在"));
    }

    /**
     * 【团长】更新团点信息
     * 
     * PUT /api/leader/{storeId}
     */
    @PutMapping("/{storeId}")
    @Operation(summary = "更新团点信息", description = "团长更新自己的团点信息")
    public Result<GroupLeaderStore> updateLeaderStore(
            @Parameter(description = "团点ID") @PathVariable Long storeId,
            @RequestBody GroupLeaderStore store
    ) {
        log.info("团长更新团点信息：storeId={}", storeId);

        try {
            GroupLeaderStore updated = leaderApplicationService.updateLeaderStore(storeId, store);
            return Result.success("团点信息更新成功", updated);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 【管理员】补充团点经纬度信息（审核前调用）
     * 
     * POST /api/leader/{storeId}/coordinates
     */
    @PostMapping("/{storeId}/coordinates")
    @Operation(summary = "补充团点经纬度信息", description = "管理员在审核前补充团点的经纬度信息")
    public Result<GroupLeaderStore> updateCoordinates(
            @Parameter(description = "团点ID") @PathVariable Long storeId,
            @Parameter(description = "纬度") @RequestParam java.math.BigDecimal latitude,
            @Parameter(description = "经度") @RequestParam java.math.BigDecimal longitude
    ) {
        log.info("管理员补充团点{}的经纬度：{}, {}", storeId, latitude, longitude);

        try {
            GroupLeaderStore updated = leaderApplicationService.updateStoreCoordinates(
                    storeId, latitude, longitude
            );
            return Result.success("经纬度信息已补充", updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 【管理员】审核团长申请
     * 
     * POST /api/leader/{storeId}/review
     */
    @PostMapping("/{storeId}/review")
    @Operation(summary = "审核团长申请", description = "管理员审核团长申请（通过/拒绝）")
    public Result<GroupLeaderStore> reviewApplication(
            @Parameter(description = "团点ID") @PathVariable Long storeId,
            @Parameter(description = "审核人ID（管理员）") @RequestParam Long reviewerId,
            @Parameter(description = "是否通过（true-通过，false-拒绝）") @RequestParam boolean approved,
            @Parameter(description = "审核意见") @RequestParam(required = false) String reviewComment
    ) {
        log.info("管理员{}审核团长申请：storeId={}，approved={}", reviewerId, storeId, approved);

        try {
            GroupLeaderStore reviewed = leaderApplicationService.reviewApplication(
                    storeId, reviewerId, approved, reviewComment
            );

            String message = approved ? "审核通过，用户成为团长" : "审核拒绝";
            return Result.success(message, reviewed);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 【管理员】查询所有待审核的团长申请
     * 
     * GET /api/leader/pending
     */
    @GetMapping("/pending")
    @Operation(summary = "查询待审核团长申请", description = "管理员查询所有待审核的团长申请")
    public Result<List<GroupLeaderStore>> getPendingApplications() {
        log.info("管理员查询待审核团长申请");
        List<GroupLeaderStore> applications = leaderApplicationService.getAllPendingApplications();
        return Result.success(applications);
    }

    /**
     * 【管理员】查询所有团长
     * 
     * GET /api/leader/list?status=1
     */
    @GetMapping("/list")
    @Operation(summary = "查询团长列表", description = "管理员根据状态查询团长列表（0-待审核 1-正常运营 2-已停用）")
    public Result<List<GroupLeaderStore>> getLeadersByStatus(
            @Parameter(description = "状态：0-待审核 1-正常运营 2-已停用，不传则查询全部") @RequestParam(required = false) Integer status
    ) {
        log.info("管理员查询团长列表：status={}", status);

        List<GroupLeaderStore> leaders;
        if (status != null) {
            leaders = leaderApplicationService.getLeadersByStatus(status);
        } else {
            leaders = leaderApplicationService.getAllActiveLeaders();
        }

        return Result.success(leaders);
    }

    /**
     * 【管理员】停用团长
     * 
     * POST /api/leader/{storeId}/disable
     */
    @PostMapping("/{storeId}/disable")
    @Operation(summary = "停用团长", description = "管理员停用团长")
    public Result<Void> disableLeader(
            @Parameter(description = "团点ID") @PathVariable Long storeId
    ) {
        log.info("管理员停用团长：storeId={}", storeId);

        try {
            leaderApplicationService.disableLeader(storeId);
            return Result.success("团长已停用");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }
}

