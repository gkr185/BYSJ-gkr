package com.bcu.edu.client.fallback;

import com.bcu.edu.client.OrderServiceClient;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.OrderInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * OrderService Feign客户端降级处理
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Component
@Slf4j
public class OrderServiceClientFallback implements OrderServiceClient {

    @Override
    public Result<List<OrderInfoDTO>> getOrdersByDispatchGroup(String dispatchGroup) {
        log.error("OrderService调用失败: getOrdersByDispatchGroup, dispatchGroup={}", dispatchGroup);
        return Result.error("订单服务暂时不可用，无法获取分单组订单");
    }

    @Override
    public Result<List<OrderInfoDTO>> getOrdersByIds(List<Long> orderIds) {
        log.error("OrderService调用失败: getOrdersByIds, orderIds={}", orderIds);
        return Result.error("订单服务暂时不可用，无法获取订单信息");
    }

    @Override
    public Result<BatchUpdateResult> batchUpdateToShipping(BatchShipUpdateRequest request) {
        log.error("OrderService调用失败: batchUpdateToShipping, orderIds={}", request.getOrderIds());
        
        BatchUpdateResult result = new BatchUpdateResult();
        result.setSuccessCount(0);
        result.setFailedCount(request.getOrderIds().size());
        result.setFailedOrderIds(request.getOrderIds());
        
        return Result.error("订单服务暂时不可用，批量更新失败");
    }

    @Override
    public Result<Void> updateDeliveryId(UpdateDeliveryRequest request) {
        log.error("OrderService调用失败: updateDeliveryId, dispatchGroup={}, deliveryId={}", 
                request.getDispatchGroup(), request.getDeliveryId());
        return Result.error("订单服务暂时不可用，无法更新配送单ID");
    }
}
