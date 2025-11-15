package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.AddressDTO;
import com.bcu.edu.dto.response.AddressResponse;
import com.bcu.edu.dto.response.UserInfoResponse;
import com.bcu.edu.entity.UserAddress;
import com.bcu.edu.repository.UserAddressRepository;
import com.bcu.edu.service.AccountService;
import com.bcu.edu.service.AddressService;
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
@RequestMapping("")  // ✅ 移除前缀，使用完整路径
@RequiredArgsConstructor
@Tag(name = "Feign调用接口", description = "供其他微服务调用的内部接口")
public class FeignController {

    private final UserService userService;
    private final AccountService accountService;
    private final AddressService addressService;
    private final UserAddressRepository addressRepository;

    // ========== 用户信息查询接口 ==========

    /**
     * 验证用户是否存在（OrderService专用接口）
     * 路径：/api/user/feign/validate/{userId}
     */
    @GetMapping("/api/user/feign/validate/{userId}")
    @Operation(summary = "验证用户是否存在（OrderService）", description = "供OrderService调用")
    public Result<Boolean> validateUser(@PathVariable Long userId) {
        log.info("[Feign] OrderService 调用验证用户：userId={}", userId);
        try {
            UserInfoResponse user = userService.getUserInfo(userId);
            return Result.success(user != null);
        } catch (Exception e) {
            log.error("[Feign] 验证用户失败：userId={}, error={}", userId, e.getMessage());
            return Result.success(false);
        }
    }

    /**
     * 获取地址详情（OrderService专用接口）
     * 路径：/api/user/feign/address/{addressId}
     */
    @GetMapping("/api/user/feign/address/{addressId}")
    @Operation(summary = "获取地址详情（OrderService）", description = "供OrderService调用")
    public Result<AddressResponse> getAddress(@PathVariable Long addressId) {
        log.info("[Feign] OrderService 调用获取地址：addressId={}", addressId);
        try {
            UserAddress address = addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("地址不存在"));
            AddressResponse response = AddressResponse.fromEntity(address);
            return Result.success(response);
        } catch (Exception e) {
            log.error("[Feign] 获取地址失败：addressId={}, error={}", addressId, e.getMessage(), e);
            return Result.error("地址不存在");
        }
    }

    /**
     * 根据用户ID获取用户信息（GroupBuyService专用接口）
     * 路径：/api/user/feign/info/{userId}
     */
    @GetMapping("/api/user/feign/info/{userId}")
    @Operation(summary = "获取用户信息（GroupBuyService）", description = "供GroupBuyService调用")
    public Result<UserInfoResponse> getUserInfo(@PathVariable Long userId) {
        log.info("[Feign] GroupBuyService 调用获取用户信息：userId={}", userId);
        UserInfoResponse user = userService.getUserInfo(userId);
        return Result.success(user);
    }
    
    /**
     * 根据用户ID获取用户信息（旧接口，保持兼容）
     * 路径：/feign/user/{userId}
     */
    @GetMapping("/feign/user/{userId}")
    @Operation(summary = "获取用户信息", description = "供其他服务调用，验证用户是否存在")
    public Result<UserInfoResponse> getUserById(@PathVariable Long userId) {
        UserInfoResponse user = userService.getUserInfo(userId);
        return Result.success(user);
    }

    /**
     * 批量获取用户信息
     */
    @PostMapping("/feign/user/batch")
    @Operation(summary = "批量获取用户信息")
    public Result<List<UserInfoResponse>> getUsersByIds(@RequestBody List<Long> userIds) {
        // TODO: 实现批量查询
        return Result.success(null);
    }

    /**
     * 验证用户是否存在
     */
    @GetMapping("/feign/user/exists/{userId}")
    @Operation(summary = "验证用户是否存在")
    public Result<Boolean> existsUser(@PathVariable Long userId) {
        try {
            userService.getUserInfo(userId);
            return Result.success(true);
        } catch (Exception e) {
            return Result.success(false);
        }
    }

    /**
     * 更新用户角色（供LeaderService调用）
     * 
     * 角色说明：
     * 0 - 普通用户
     * 1 - 管理员
     * 2 - 团长
     */
    @PostMapping("/feign/user/{userId}/role")
    @Operation(summary = "更新用户角色", description = "供LeaderService调用，团长审核通过后更新用户角色")
    public Result<Void> updateUserRole(
            @PathVariable Long userId,
            @RequestParam Integer role) {
        log.info("[Feign] 更新用户角色：userId={}, role={}", userId, role);
        
        try {
            userService.updateUserRole(userId, role);
            return Result.success("用户角色更新成功");
        } catch (Exception e) {
            log.error("[Feign] 更新用户角色失败：{}", e.getMessage(), e);
            return Result.error("用户角色更新失败：" + e.getMessage());
        }
    }

    // ========== 社区相关接口 ==========

    /**
     * 查询社区内的所有用户
     */
    @GetMapping("/feign/community/{communityId}/users")
    @Operation(summary = "查询社区内的用户")
    public Result<List<UserInfoResponse>> getUsersByCommunity(@PathVariable Long communityId) {
        List<UserInfoResponse> users = userService.getUsersByCommunity(communityId);
        return Result.success(users);
    }

    /**
     * 查询社区内的团长
     */
    @GetMapping("/feign/community/{communityId}/leaders")
    @Operation(summary = "查询社区内的团长")
    public Result<List<UserInfoResponse>> getLeadersByCommunity(@PathVariable Long communityId) {
        List<UserInfoResponse> leaders = userService.getLeadersByCommunity(communityId);
        return Result.success(leaders);
    }

    /**
     * 统计社区内的用户数量
     */
    @GetMapping("/feign/community/{communityId}/count")
    @Operation(summary = "统计社区内的用户数量")
    public Result<Long> countUsersByCommunity(@PathVariable Long communityId) {
        long count = userService.countUsersByCommunity(communityId);
        return Result.success(count);
    }

    // ========== 账户余额接口（支持分布式事务） ==========

    /**
     * 扣减用户余额（供订单服务、支付服务调用）
     */
    @PostMapping("/feign/account/deduct")
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
     * ⭐ GroupBuyService 调用此接口进行退款
     */
    @PostMapping("/api/account/feign/refund")
    @Operation(summary = "返还余额", description = "用于退款或Saga补偿事务")
    public Result<Void> refundBalance(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount) {
        log.info("[Feign] 退款到用户余额：userId={}, amount={}", userId, amount);
        accountService.refundBalanceForFeign(userId, amount, "groupbuy-refund-" + System.currentTimeMillis());
        return Result.success();
    }

    /**
     * 验证余额是否充足
     */
    @GetMapping("/feign/account/check")
    @Operation(summary = "验证余额是否充足")
    public Result<Boolean> checkBalance(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount) {
        boolean sufficient = accountService.checkBalance(userId, amount);
        return Result.success(sufficient);
    }

    /**
     * 充值（供PaymentService调用）
     * ⭐ PaymentService 调用此接口进行余额充值
     */
    @PostMapping("/feign/account/recharge")
    @Operation(summary = "余额充值", description = "供PaymentService调用，用于用户充值")
    public Result<Void> rechargeBalance(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount) {
        log.info("[Feign] PaymentService 调用余额充值：userId={}, amount={}", userId, amount);
        accountService.recharge(userId, amount);
        return Result.success("充值成功");
    }

    /**
     * 增加余额（供LeaderService佣金结算调用）
     * ⭐ LeaderService 调用此接口进行佣金结算到余额
     */
    @PostMapping("/feign/account/addBalance")
    @Operation(summary = "增加余额", description = "供LeaderService佣金结算调用")
    public Result<Void> addBalanceForCommission(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String remark) {
        log.info("[Feign] LeaderService 调用佣金结算增加余额：userId={}, amount={}, remark={}", 
            userId, amount, remark);
        accountService.addBalanceForCommission(userId, amount, remark);
        return Result.success("余额增加成功");
    }

    /**
     * 批量获取地址信息（⭐新增接口 - 供DeliveryService调用）
     * ⭐ DeliveryService 调用此接口批量获取用户收货地址坐标
     * 路径：/api/user/feign/address/batch
     */
    @PostMapping("/api/user/feign/address/batch")
    @Operation(summary = "批量获取地址信息", description = "供DeliveryService调用，批量获取地址坐标")
    public Result<List<AddressDTO>> batchGetAddresses(@RequestBody List<Long> addressIds) {
        log.info("[Feign] DeliveryService 调用批量获取地址：addressIds={}", addressIds);
        
        try {
            List<AddressDTO> addresses = addressService.batchGetAddresses(addressIds);
            log.info("批量获取地址成功: 共{}个地址", addresses.size());
            return Result.success(addresses);
        } catch (Exception e) {
            log.error("批量获取地址失败", e);
            return Result.error("批量获取地址失败: " + e.getMessage());
        }
    }
}

