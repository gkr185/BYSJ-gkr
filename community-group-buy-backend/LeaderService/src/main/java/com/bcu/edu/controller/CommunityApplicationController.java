package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.CommunityApplication;
import com.bcu.edu.service.CommunityApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区申请Controller
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 面向C端用户和管理员的社区申请接口
 */
@Slf4j
@RestController
@RequestMapping("/api/community-application")
@RequiredArgsConstructor
@Tag(name = "社区申请管理", description = "社区申请提交、查询、审核")
public class CommunityApplicationController {

    private final CommunityApplicationService applicationService;

    /**
     * 提交社区申请
     * 
     * 场景：用户/团长申请成为新社区的团长
     * 
     * POST /api/community-application
     */
    @PostMapping
    @Operation(summary = "提交社区申请", description = "用户申请成为新社区的团长")
    public Result<CommunityApplication> submitApplication(@RequestBody CommunityApplication application) {
        log.info("用户{}提交社区申请：{}", application.getApplicantId(), application.getCommunityName());

        try {
            CommunityApplication created = applicationService.submitApplication(application);
            return Result.success("申请提交成功，请等待审核", created);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询当前用户的申请记录
     * 
     * GET /api/community-application/my?userId=1
     */
    @GetMapping("/my")
    @Operation(summary = "查询我的申请记录", description = "用户查询自己的所有社区申请记录")
    public Result<List<CommunityApplication>> getMyApplications(
            @Parameter(description = "用户ID") @RequestParam Long userId
    ) {
        log.info("用户{}查询自己的申请记录", userId);
        List<CommunityApplication> applications = applicationService.getApplicationsByApplicantId(userId);
        return Result.success(applications);
    }

    /**
     * 根据ID查询申请详情
     * 
     * GET /api/community-application/{applicationId}
     */
    @GetMapping("/{applicationId}")
    @Operation(summary = "查询申请详情", description = "根据申请ID查询详细信息")
    public Result<CommunityApplication> getApplicationById(
            @Parameter(description = "申请ID") @PathVariable Long applicationId
    ) {
        log.info("查询申请详情：applicationId={}", applicationId);

        return applicationService.getApplicationById(applicationId)
                .map(Result::success)
                .orElse(Result.error("申请不存在"));
    }

    /**
     * 【管理员】审核申请
     * 
     * POST /api/community-application/{applicationId}/review
     */
    @PostMapping("/{applicationId}/review")
    @Operation(summary = "审核社区申请", description = "管理员审核社区申请（通过/拒绝）")
    public Result<CommunityApplication> reviewApplication(
            @Parameter(description = "申请ID") @PathVariable Long applicationId,
            @Parameter(description = "审核人ID（管理员）") @RequestParam Long reviewerId,
            @Parameter(description = "是否通过（true-通过，false-拒绝）") @RequestParam boolean approved,
            @Parameter(description = "审核意见") @RequestParam(required = false) String reviewComment
    ) {
        log.info("管理员{}审核申请：applicationId={}，approved={}", reviewerId, applicationId, approved);

        try {
            CommunityApplication reviewed = applicationService.reviewApplication(
                    applicationId, reviewerId, approved, reviewComment
            );

            String message = approved ? "审核通过，社区创建成功" : "审核拒绝";
            return Result.success(message, reviewed);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 【管理员】查询所有待审核的申请
     * 
     * GET /api/community-application/pending
     */
    @GetMapping("/pending")
    @Operation(summary = "查询待审核申请", description = "管理员查询所有待审核的社区申请")
    public Result<List<CommunityApplication>> getPendingApplications() {
        log.info("管理员查询待审核申请");
        List<CommunityApplication> applications = applicationService.getAllPendingApplications();
        return Result.success(applications);
    }

    /**
     * 【管理员】根据状态查询申请列表
     * 
     * GET /api/community-application/list?status=1
     */
    @GetMapping("/list")
    @Operation(summary = "根据状态查询申请", description = "管理员根据状态查询申请列表（0-待审核 1-已通过 2-已拒绝）")
    public Result<List<CommunityApplication>> getApplicationsByStatus(
            @Parameter(description = "状态：0-待审核 1-已通过 2-已拒绝") @RequestParam Integer status
    ) {
        log.info("管理员查询申请：status={}", status);
        List<CommunityApplication> applications = applicationService.getApplicationsByStatus(status);
        return Result.success(applications);
    }
}

