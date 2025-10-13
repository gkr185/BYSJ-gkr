package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.AccountResponse;
import com.bcu.edu.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 账户Controller
 */
@Tag(name = "账户管理", description = "用户账户余额管理接口")
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "获取账户信息", description = "查询用户账户余额、冻结金额等信息")
    @GetMapping("/{userId}")
    public Result<AccountResponse> getAccount(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(accountService.getAccount(userId));
    }

    @Operation(summary = "账户充值", description = "用户充值，增加可用余额")
    @PostMapping("/recharge/{userId}")
    public Result<AccountResponse> recharge(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "充值金额") @RequestParam BigDecimal amount) {
        return Result.success(accountService.recharge(userId, amount));
    }

    @Operation(summary = "账户扣款", description = "扣除用户可用余额（如支付订单）")
    @PostMapping("/deduct/{userId}")
    public Result<AccountResponse> deduct(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "扣款金额") @RequestParam BigDecimal amount) {
        return Result.success(accountService.deduct(userId, amount));
    }

    @Operation(summary = "冻结金额", description = "冻结用户部分余额（如待结算佣金）")
    @PostMapping("/freeze/{userId}")
    public Result<AccountResponse> freeze(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "冻结金额") @RequestParam BigDecimal amount) {
        return Result.success(accountService.freeze(userId, amount));
    }

    @Operation(summary = "解冻金额", description = "解冻用户冻结金额，恢复到可用余额")
    @PostMapping("/unfreeze/{userId}")
    public Result<AccountResponse> unfreeze(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "解冻金额") @RequestParam BigDecimal amount) {
        return Result.success(accountService.unfreeze(userId, amount));
    }
}

