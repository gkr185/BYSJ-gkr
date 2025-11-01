package com.bcu.edu.client.fallback;

import com.bcu.edu.client.GroupBuyServiceClient;
import com.bcu.edu.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GroupBuyService Feign客户端降级处理
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Component
@Slf4j
public class GroupBuyServiceClientFallback implements GroupBuyServiceClient {

    @Override
    public Result<Void> paymentCallback(Long orderId) {
        log.error("GroupBuyService服务调用失败(支付回调)，进入降级: orderId={}", orderId);
        // 支付回调失败不影响支付本身，可以后续通过补偿任务修复
        return Result.error("拼团服务暂时不可用，支付已成功，成团状态稍后更新");
    }
}

