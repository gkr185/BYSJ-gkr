package com.bcu.edu.client.fallback;

import com.bcu.edu.client.OrderServiceClient;
import com.bcu.edu.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OrderService Feign客户端降级处理
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Component
@Slf4j
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public Result<Void> updatePayStatus(Long orderId, Integer payStatus) {
        log.error("OrderService服务调用失败(更新支付状态)，进入降级: orderId={}, payStatus={}", orderId, payStatus);
        return Result.error("订单服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> isGroupBuyOrder(Long orderId) {
        log.error("OrderService服务调用失败(判断订单类型)，进入降级: orderId={}", orderId);
        return Result.error("订单服务暂时不可用，请稍后重试");
    }
}

