package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.JoinTeamRequest;
import com.bcu.edu.dto.request.LaunchTeamRequest;
import com.bcu.edu.dto.response.JoinResult;
import com.bcu.edu.dto.response.MyTeamResponse;
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
    public Result<List<MyTeamResponse>> getMyTeams(HttpServletRequest httpRequest) {
        // 从请求头获取用户ID
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }
        
        Long userId = Long.parseLong(userIdHeader);
        
        List<MyTeamResponse> teams = teamService.getUserTeams(userId);
        return Result.success(teams);
    }
    
    /**
     * 获取团长发起的拼团记录（带分页、状态筛选）
     * 
     * <p>团长端使用，查询团长发起的所有拼团
     * 
     * @param leaderId 团长ID
     * @param status 团状态（0-拼团中, 1-已成团, 2-已失败，null-全部）
     * @param page 页码（从1开始）
     * @param limit 每页数量
     * @return 分页结果
     */
    @GetMapping("/teams/leader")
    @Operation(summary = "团长拼团记录", description = "查询团长发起的所有拼团，支持分页和状态筛选")
    public Result<com.bcu.edu.common.result.PageResult<TeamDetailResponse>> getLeaderTeams(
        @Parameter(description = "团长ID") @RequestParam Long leaderId,
        @Parameter(description = "团状态（0-拼团中, 1-已成团, 2-已失败）") @RequestParam(required = false) Integer status,
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int limit) {
        
        log.info("查询团长{}的拼团记录，status={}, page={}, limit={}", leaderId, status, page, limit);
        
        com.bcu.edu.common.result.PageResult<TeamDetailResponse> result = 
            teamService.getLeaderTeams(leaderId, status, page, limit);
        
        return Result.success(result);
    }
    
    // ==================== 团长管理团队接口 ====================
    
    /**
     * 团长提前结束拼团（已达起拼人数）
     * 
     * <p>当达到起拼人数后，团长可以选择提前结束拼团
     * 
     * @param teamId 团ID
     * @param httpRequest HTTP请求
     * @return 操作结果
     */
    @PostMapping("/team/{teamId}/finish")
    @Operation(summary = "提前结束拼团", description = "团长权限，已达起拼人数时可提前结束")
    public Result<Void> finishTeamEarly(
        @Parameter(description = "团ID") @PathVariable Long teamId,
        HttpServletRequest httpRequest) {
        
        // 从请求头获取用户ID
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }
        
        Long userId = Long.parseLong(userIdHeader);
        
        log.info("团长{}请求提前结束拼团，teamId={}", userId, teamId);
        
        teamService.finishTeamEarly(teamId, userId);
        return Result.success("拼团已成功结束");
    }
    
    /**
     * 团长取消拼团
     * 
     * <p>团长可以取消拼团，自动退款给所有成员
     * 
     * @param teamId 团ID
     * @param request 包含取消原因的请求体
     * @param httpRequest HTTP请求
     * @return 操作结果
     */
    @PostMapping("/team/{teamId}/cancel")
    @Operation(summary = "取消拼团", description = "团长权限，取消拼团并退款给所有成员")
    public Result<Void> cancelTeam(
        @Parameter(description = "团ID") @PathVariable Long teamId,
        @RequestBody(required = false) java.util.Map<String, String> request,
        HttpServletRequest httpRequest) {
        
        // 从请求头获取用户ID
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }
        
        Long userId = Long.parseLong(userIdHeader);
        String reason = request != null ? request.get("reason") : "团长取消拼团";
        
        log.info("团长{}请求取消拼团，teamId={}, reason={}", userId, teamId, reason);
        
        teamService.cancelTeam(teamId, userId, reason);
        return Result.success("拼团已取消，已退款给所有成员");
    }
    
    /**
     * 团长移除团队成员
     * 
     * <p>团长可以移除团队中的成员（成团前），自动退款给该成员
     * 
     * @param teamId 团ID
     * @param memberId 成员ID
     * @param request 包含移除原因的请求体
     * @param httpRequest HTTP请求
     * @return 操作结果
     */
    @PostMapping("/team/{teamId}/member/{memberId}/remove")
    @Operation(summary = "移除团队成员", description = "团长权限，移除成员并退款")
    public Result<Void> removeMember(
        @Parameter(description = "团ID") @PathVariable Long teamId,
        @Parameter(description = "成员ID") @PathVariable Long memberId,
        @RequestBody(required = false) java.util.Map<String, String> request,
        HttpServletRequest httpRequest) {
        
        // 从请求头获取用户ID
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }
        
        Long userId = Long.parseLong(userIdHeader);
        String reason = request != null ? request.get("reason") : "团长移除成员";
        
        log.info("团长{}请求移除成员，teamId={}, memberId={}, reason={}", userId, teamId, memberId, reason);
        
        teamService.removeMember(teamId, memberId, userId, reason);
        return Result.success("已移除成员，已退款给该成员");
    }
}

