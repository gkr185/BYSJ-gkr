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

import java.math.BigDecimal;
import java.util.List;

/**
 * 社区管理Controller
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 面向C端用户和团长的社区查询接口
 */
@Slf4j
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Tag(name = "社区管理", description = "社区查询、匹配接口")
public class CommunityController {

    private final CommunityService communityService;

    /**
     * 【核心接口】根据用户经纬度匹配最近的社区
     * 
     * 场景：用户注册、选择收货地址时自动匹配所属社区
     * 
     * GET /api/community/nearest?latitude=39.9042&longitude=116.4074
     */
    @GetMapping("/nearest")
    @Operation(summary = "匹配最近的社区", description = "根据用户经纬度，使用Haversine公式计算距离，返回最近的社区")
    public Result<Community> findNearestCommunity(
            @Parameter(description = "用户纬度", example = "39.9042")
            @RequestParam BigDecimal latitude,
            @Parameter(description = "用户经度", example = "116.4074")
            @RequestParam BigDecimal longitude
    ) {
        log.info("查询最近社区：latitude={}, longitude={}", latitude, longitude);

        return communityService.findNearestCommunity(latitude, longitude)
                .map(community -> Result.success("匹配成功", community))
                .orElse(Result.error("您的位置不在任何社区服务范围内"));
    }

    /**
     * 查询所有正常运营的社区
     * 
     * 场景：用户手动选择社区、团长查看可申请的社区列表
     * 
     * GET /api/community/list
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有社区", description = "返回所有正常运营的社区列表")
    public Result<List<Community>> getAllActiveCommunities() {
        log.info("查询所有正常运营的社区");
        List<Community> communities = communityService.getAllActiveCommunities();
        return Result.success(communities);
    }

    /**
     * 根据ID查询社区详情
     * 
     * GET /api/community/{communityId}
     */
    @GetMapping("/{communityId}")
    @Operation(summary = "查询社区详情", description = "根据社区ID查询详细信息")
    public Result<Community> getCommunityById(
            @Parameter(description = "社区ID") @PathVariable Long communityId
    ) {
        log.info("查询社区详情：communityId={}", communityId);

        return communityService.getCommunityById(communityId)
                .map(Result::success)
                .orElse(Result.error("社区不存在"));
    }
}

