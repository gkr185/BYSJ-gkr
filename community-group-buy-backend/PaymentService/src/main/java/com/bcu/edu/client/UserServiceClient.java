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
     * @return 成功/失败
     */
    @PostMapping("/feign/account/deduct")
    Result<Void> deduct(@RequestParam("userId") Long userId, 
                        @RequestParam("amount") BigDecimal amount);

    /**
     * 余额充值（充值或退款时调用）
     * 
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 成功/失败
     */
    @PostMapping("/feign/account/recharge")
    Result<Void> recharge(@RequestParam("userId") Long userId, 
                          @RequestParam("amount") BigDecimal amount);
}

