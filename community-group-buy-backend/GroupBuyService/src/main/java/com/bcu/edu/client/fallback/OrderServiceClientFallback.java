package com.bcu.edu.client.fallback;

import com.bcu.edu.client.OrderServiceClient;
import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.CreateOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OrderService Feign客户端降级处理
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Component
@Slf4j
public class OrderServiceClientFallback implements OrderServiceClient {
    
    @Override
    public Result<Long> createOrder(CreateOrderRequest request) {
        log.error("OrderService调用失败，订单创建失败，request={}", request);
        throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, 
            "订单服务暂时不可用，请稍后重试");
    }
    
    @Override
    public Result<Void> batchUpdateOrderStatus(List<Long> orderIds, Integer status) {
        log.error("OrderService批量更新订单状态失败，orderIds={}, status={}", orderIds, status);
        // 成团逻辑已完成，订单状态更新失败记录日志，不抛异常
        // 后续可通过补偿任务修复
        return Result.error(ResultCode.SERVICE_UNAVAILABLE);
    }
    
    @Override
    public Result<Void> updateOrderStatus(Long orderId, Integer status) {
        log.error("OrderService更新订单状态失败，orderId={}, status={}", orderId, status);
        throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, 
            "订单服务暂时不可用，请稍后重试");
    }
    
    @Override
    public Result<Void> cancelOrder(Long orderId) {
        log.error("OrderService取消订单失败，orderId={}", orderId);
        // 参团失败时取消订单，失败记录日志
        // 订单会在30分钟后自动过期
        return Result.error(ResultCode.SERVICE_UNAVAILABLE);
    }
}

