package com.bcu.edu.feign;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.OrderInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OrderService Feign降级处理
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@Component
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public Result<List<OrderInfoDTO>> getOrdersByDispatchGroup(String dispatchGroup) {
        log.error("调用OrderService查询分单组订单失败，dispatchGroup={}", dispatchGroup);
        return Result.error("OrderService服务不可用");
    }

    @Override
    public Result<List<OrderInfoDTO>> batchQueryOrders(List<Long> orderIds) {
        log.error("调用OrderService批量查询订单失败，orderIds={}", orderIds);
        return Result.error("OrderService服务不可用");
    }

    @Override
    public Result<Integer> batchUpdateToShipping(List<Long> orderIds, Long deliveryId, String dispatchGroup) {
        log.error("调用OrderService批量更新订单状态失败，orderIds={}", orderIds);
        return Result.error("OrderService服务不可用");
    }

    @Override
    public Result<Integer> batchUpdateToDelivered(List<Long> orderIds) {
        log.error("调用OrderService批量更新订单为已送达失败，orderIds={}", orderIds);
        return Result.error("OrderService服务不可用");
    }
}

