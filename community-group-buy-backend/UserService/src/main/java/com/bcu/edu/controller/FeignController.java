package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.UserInfoResponse;
import com.bcu.edu.service.AccountService;
import com.bcu.edu.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Feign调用接口控制器
 * 专门提供给其他微服务调用的接口
 */
@Slf4j
@RestController
@RequestMapping("/feign")
@RequiredArgsConstructor
@Tag(name = "Feign调用接口", description = "供其他微服务调用的内部接口")
public class FeignController {

    private final UserService userService;
    private final AccountService accountService;

    // ========== 用户信息查询接口 ==========

    /**
     * 根据用户ID获取用户信息（供其他服务验证用户存在）
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户信息", description = "供其他服务调用，验证用户是否存在")
    public Result<UserInfoResponse> getUserById(@PathVariable Long userId) {
        UserInfoResponse user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    /**
     * 批量获取用户信息
     */
    @PostMapping("/user/batch")
    @Operation(summary = "批量获取用户信息")
    public Result<List<UserInfoResponse>> getUsersByIds(@RequestBody List<Long> userIds) {
        // TODO: 实现批量查询
        return Result.success(null);
    }

    /**
     * 验证用户是否存在
     */
    @GetMapping("/user/exists/{userId}")
    @Operation(summary = "验证用户是否存在")
    public Result<Boolean> existsUser(@PathVariable Long userId) {
        try {
            userService.getUserInfo(userId);
            return Result.success(true);
        } catch (Exception e) {
            return Result.success(false);
        }
    }

    // ========== 社区相关接口 ==========

    /**
     * 查询社区内的所有用户
     */
    @GetMapping("/community/{communityId}/users")
    @Operation(summary = "查询社区内的用户")
    public Result<List<UserInfoResponse>> getUsersByCommunity(@PathVariable Long communityId) {
        List<UserInfoResponse> users = userService.getUsersByCommunity(communityId);
        return Result.success(users);
    }

    /**
     * 查询社区内的团长
     */
    @GetMapping("/community/{communityId}/leaders")
    @Operation(summary = "查询社区内的团长")
    public Result<List<UserInfoResponse>> getLeadersByCommunity(@PathVariable Long communityId) {
        List<UserInfoResponse> leaders = userService.getLeadersByCommunity(communityId);
        return Result.success(leaders);
    }

    /**
     * 统计社区内的用户数量
     */
    @GetMapping("/community/{communityId}/count")
    @Operation(summary = "统计社区内的用户数量")
    public Result<Long> countUsersByCommunity(@PathVariable Long communityId) {
        long count = userService.countUsersByCommunity(communityId);
        return Result.success(count);
    }

    // ========== 账户余额接口（支持分布式事务） ==========

    /**
     * 扣减用户余额（供订单服务、支付服务调用）
     */
    @PostMapping("/account/deduct")
    @Operation(summary = "扣减余额", description = "供其他服务调用，支持Saga事务")
    public Result<Void> deductBalance(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam String sagaId) {
        accountService.deductBalanceForFeign(userId, amount, sagaId);
        return Result.success();
    }

    /**
     * 返还用户余额（供订单服务、支付服务调用，用于退款或补偿）
     */
    @PostMapping("/account/refund")
    @Operation(summary = "返还余额", description = "用于退款或Saga补偿事务")
    public Result<Void> refundBalance(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam String sagaId) {
        accountService.refundBalanceForFeign(userId, amount, sagaId);
        return Result.success();
    }

    /**
     * 验证余额是否充足
     */
    @GetMapping("/account/check")
    @Operation(summary = "验证余额是否充足")
    public Result<Boolean> checkBalance(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount) {
        boolean sufficient = accountService.checkBalance(userId, amount);
        return Result.success(sufficient);
    }
}

