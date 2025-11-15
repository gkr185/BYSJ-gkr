package com.bcu.edu.feign;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.OrderInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrderService Feign客户端
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@FeignClient(name = "order-service", fallback = OrderServiceClientFallback.class)
public interface OrderServiceClient {

    /**
     * 查询分单组订单列表
     */
    @GetMapping("/api/order/feign/dispatchGroup/{dispatchGroup}")
    Result<List<OrderInfoDTO>> getOrdersByDispatchGroup(@PathVariable String dispatchGroup);

    /**
     * 批量查询订单信息
     */
    @PostMapping("/api/order/feign/batchQuery")
    Result<List<OrderInfoDTO>> batchQueryOrders(@RequestBody List<Long> orderIds);

    /**
     * 批量更新订单状态为"配送中"
     * 
     * @param orderIds 订单ID列表
     * @param deliveryId 配送单ID
     * @param dispatchGroup 分单组标识
     */
    @PostMapping("/api/order/feign/batchUpdateToShipping")
    Result<Integer> batchUpdateToShipping(
            @RequestParam("orderIds") List<Long> orderIds,
            @RequestParam("deliveryId") Long deliveryId,
            @RequestParam("dispatchGroup") String dispatchGroup
    );

    /**
     * 批量更新订单状态为"已送达"
     */
    @PostMapping("/api/order/feign/batchUpdateToDelivered")
    Result<Integer> batchUpdateToDelivered(@RequestParam("orderIds") List<Long> orderIds);
}

