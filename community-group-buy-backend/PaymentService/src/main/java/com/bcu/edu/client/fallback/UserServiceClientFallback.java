package com.bcu.edu.client.fallback;

import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * UserService Feign客户端降级处理
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Result<Void> deduct(Long userId, BigDecimal amount, String sagaId) {
        log.error("UserService服务调用失败(扣款)，进入降级: userId={}, amount={}, sagaId={}", 
            userId, amount, sagaId);
        return Result.error("用户服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Void> recharge(Long userId, BigDecimal amount) {
        log.error("UserService服务调用失败(充值)，进入降级: userId={}, amount={}", userId, amount);
        return Result.error("用户服务暂时不可用，请稍后重试");
    }
    
    @Override
    public Result<Void> refundToBalance(Long userId, BigDecimal amount) {
        log.error("UserService服务调用失败(退款)，进入降级: userId={}, amount={}", userId, amount);
        return Result.error("退款服务暂时不可用，请稍后重试");
    }
}

