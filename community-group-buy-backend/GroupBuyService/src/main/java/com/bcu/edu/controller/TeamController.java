package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.JoinTeamRequest;
import com.bcu.edu.dto.request.LaunchTeamRequest;
import com.bcu.edu.dto.response.JoinResult;
import com.bcu.edu.dto.response.TeamDetailResponse;
import com.bcu.edu.service.RefundService;
import com.bcu.edu.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团管理Controller（⭐⭐⭐核心Controller）
 * 
 * <p>核心接口：
 * <ul>
 *   <li>POST /api/groupbuy/team/launch - 团长发起拼团（v3.0核心）</li>
 *   <li>POST /api/groupbuy/team/join - 用户参与拼团（核心）</li>
 *   <li>POST /api/groupbuy/payment/callback - 支付回调（内部接口）</li>
 *   <li>GET /api/groupbuy/team/{teamId}/detail - 获取团详情</li>
 *   <li>GET /api/groupbuy/activity/{activityId}/teams - 获取活动团列表（社区优先）</li>
 *   <li>POST /api/groupbuy/team/quit - 退出拼团</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@RestController
@RequestMapping("/api/groupbuy")
@Slf4j
@Tag(name = "拼团管理", description = "团长发起、用户参团、成团管理、查询接口")
public class TeamController {
    
    @Autowired
    private TeamService teamService;
    
    @Autowired
    private RefundService refundService;
    
    /**
     * 团长发起拼团（⭐v3.0核心接口）
     * 
     * <p>特性：
     * <ul>
     *   <li>仅团长可发起（role=2校验）</li>
     *   <li>自动关联团长的社区</li>
     *   <li>团长可选择是否立即参与</li>
     * </ul>
     */
    @PostMapping("/team/launch")
    @Operation(summary = "团长发起拼团", description = "v3.0核心功能：仅团长可发起，自动关联社区")
    public Result<TeamDetailResponse> launchTeam(
        @Valid @RequestBody LaunchTeamRequest request,
        HttpServletRequest httpRequest) {
        
        // 从请求头获取用户ID（Gateway解析JWT传递）
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }
        
        Long userId = Long.parseLong(userIdHeader);
        request.setUserId(userId);
        
        TeamDetailResponse response = teamService.launchTeam(request);
        return Result.success(response);
    }
    
    /**
     * 用户参与拼团（⭐核心接口）
     * 
     * <p>流程：
     * <ol>
     *   <li>行锁检查团状态</li>
     *   <li>防重复参团</li>
     *   <li>创建订单</li>
     *   <li>记录参团</li>
     *   <li>返回订单ID（跳转支付）</li>
     * </ol>
     */
    @PostMapping("/team/join")
    @Operation(summary = "参与拼团", description = "用户参团，创建订单，行锁防并发")
    public Result<JoinResult> joinTeam(
        @Valid @RequestBody JoinTeamRequest request,
        HttpServletRequest httpRequest) {
        
        // 从请求头获取用户ID
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }
        
        Long userId = Long.parseLong(userIdHeader);
        request.setUserId(userId);
        
        JoinResult result = teamService.joinTeam(request);
        return Result.success(result);
    }
    
    /**
     * 支付回调（⭐核心接口，内部调用）
     * 
     * <p>由PaymentService回调通知
     */
    @PostMapping("/payment/callback")
    @Operation(summary = "支付回调", description = "PaymentService回调通知，触发成团检查")
    public Result<Void> paymentCallback(@RequestParam("orderId") Long orderId) {
        log.info("收到支付回调，orderId={}", orderId);
        
        teamService.paymentCallback(orderId);
        return Result.success();
    }
    
    /**
     * 获取团详情
     */
    @GetMapping("/team/{teamId}/detail")
    @Operation(summary = "获取团详情", description = "包含团信息、成员列表")
    public Result<TeamDetailResponse> getTeamDetail(
        @Parameter(description = "团ID") @PathVariable Long teamId) {
        
        TeamDetailResponse response = teamService.getTeamDetail(teamId);
        return Result.success(response);
    }
    
    /**
     * 获取活动的团列表（⭐社区优先排序）
     * 
     * <p>v3.0特性：
     * <ul>
     *   <li>优先显示本社区的团</li>
     *   <li>然后显示其他社区的团</li>
     *   <li>SQL ORDER BY CASE实现</li>
     * </ul>
     */
    @GetMapping("/activity/{activityId}/teams")
    @Operation(summary = "获取活动团列表", description = "v3.0：社区优先排序")
    public Result<List<TeamDetailResponse>> getActivityTeams(
        @Parameter(description = "活动ID") @PathVariable Long activityId,
        @Parameter(description = "用户社区ID（可选）") @RequestParam(required = false) Long communityId) {
        
        List<TeamDetailResponse> teams = teamService.getActivityTeams(activityId, communityId);
        return Result.success(teams);
    }
    
    /**
     * 退出拼团（成团前）
     */
    @PostMapping("/team/quit")
    @Operation(summary = "退出拼团", description = "成团前可退，自动退款")
    public Result<Void> quitTeam(
        @RequestParam("teamId") Long teamId,
        HttpServletRequest httpRequest) {
        
        // 从请求头获取用户ID
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }
        
        Long userId = Long.parseLong(userIdHeader);
        
        refundService.quitTeam(teamId, userId);
        return Result.success("退出成功，已退款");
    }
    
    /**
     * 获取用户的拼团记录
     * 
     * <p>返回用户参与的所有拼团，按参团时间倒序
     */
    @GetMapping("/teams/my")
    @Operation(summary = "我的拼团记录", description = "查询用户参与的所有拼团，按参团时间倒序")
    public Result<List<TeamDetailResponse>> getMyTeams(HttpServletRequest httpRequest) {
        // 从请求头获取用户ID
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }
        
        Long userId = Long.parseLong(userIdHeader);
        
        List<TeamDetailResponse> teams = teamService.getUserTeams(userId);
        return Result.success(teams);
    }
}

