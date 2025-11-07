package com.bcu.edu.client;

import com.bcu.edu.client.fallback.UserServiceClientFallback;
import com.bcu.edu.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * UserService Feign客户端
 * 
 * <p>调用UserService的余额扣款和充值接口
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    /**
     * 余额扣款（支付订单时调用）
     * 
     * @param userId 用户ID
     * @param amount 扣款金额
     * @param sagaId Saga事务ID（用于分布式事务追踪）
     * @return 成功/失败
     */
    @PostMapping("/feign/account/deduct")
    Result<Void> deduct(@RequestParam("userId") Long userId, 
                        @RequestParam("amount") BigDecimal amount,
                        @RequestParam("sagaId") String sagaId);

    /**
     * 余额充值（充值时调用）
     * 
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 成功/失败
     */
    @PostMapping("/feign/account/recharge")
    Result<Void> recharge(@RequestParam("userId") Long userId, 
                          @RequestParam("amount") BigDecimal amount);
    
    /**
     * 退款到用户余额（退款时调用）⭐推荐使用此接口进行退款
     * 
     * @param userId 用户ID
     * @param amount 退款金额
     * @return 成功/失败
     */
    @PostMapping("/api/account/feign/refund")
    Result<Void> refundToBalance(@RequestParam("userId") Long userId, 
                                  @RequestParam("amount") BigDecimal amount);
}

