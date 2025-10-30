package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.Community;
import com.bcu.edu.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员-社区管理Controller
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 面向管理员的社区CRUD接口
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/community")
@RequiredArgsConstructor
@Tag(name = "管理员-社区管理", description = "社区的创建、修改、删除")
public class AdminCommunityController {

    private final CommunityService communityService;

    /**
     * 创建社区
     * 
     * POST /api/admin/community
     */
    @PostMapping
    @Operation(summary = "创建社区", description = "管理员创建新社区")
    public Result<Community> createCommunity(@RequestBody Community community) {
        log.info("管理员创建社区：{}", community.getName());

        try {
            Community created = communityService.createCommunity(community);
            return Result.success("社区创建成功", created);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新社区信息
     * 
     * PUT /api/admin/community/{communityId}
     */
    @PutMapping("/{communityId}")
    @Operation(summary = "更新社区", description = "修改社区信息")
    public Result<Community> updateCommunity(
            @Parameter(description = "社区ID") @PathVariable Long communityId,
            @RequestBody Community community
    ) {
        log.info("管理员更新社区：communityId={}", communityId);

        try {
            Community updated = communityService.updateCommunity(communityId, community);
            return Result.success("社区更新成功", updated);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除社区（软删除）
     * 
     * DELETE /api/admin/community/{communityId}
     */
    @DeleteMapping("/{communityId}")
    @Operation(summary = "删除社区", description = "软删除社区（修改状态为已关闭）")
    public Result<Void> deleteCommunity(
            @Parameter(description = "社区ID") @PathVariable Long communityId
    ) {
        log.info("管理员删除社区：communityId={}", communityId);

        try {
            communityService.deleteCommunity(communityId);
            return Result.success("社区删除成功");
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 查询所有社区（包含已关闭）
     * 
     * GET /api/admin/community/all?status=1
     */
    @GetMapping("/all")
    @Operation(summary = "查询所有社区", description = "根据状态查询社区列表（0-待审核 1-正常运营 2-已关闭）")
    public Result<List<Community>> getCommunitiesByStatus(
            @Parameter(description = "状态：0-待审核 1-正常运营 2-已关闭，不传则查询全部")
            @RequestParam(required = false) Integer status
    ) {
        log.info("管理员查询社区：status={}", status);

        List<Community> communities;
        if (status != null) {
            communities = communityService.getCommunitiesByStatus(status);
        } else {
            communities = communityService.getAllActiveCommunities();
        }

        return Result.success(communities);
    }
}

