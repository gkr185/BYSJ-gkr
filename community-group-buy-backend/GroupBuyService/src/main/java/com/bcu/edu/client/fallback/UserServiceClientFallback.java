package com.bcu.edu.client.fallback;

import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.UserInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * UserService Feign客户端降级处理
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {
    
    @Override
    public Result<UserInfoDTO> getUserInfo(Long userId) {
        log.error("UserService调用失败，userId={}", userId);
        throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, 
            "用户服务暂时不可用，请稍后重试");
    }
    
    @Override
    public Result<Void> refundToBalance(Long userId, BigDecimal amount) {
        log.error("UserService退款调用失败，userId={}, amount={}", userId, amount);
        throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, 
            "退款服务暂时不可用，请联系管理员");
    }
}

