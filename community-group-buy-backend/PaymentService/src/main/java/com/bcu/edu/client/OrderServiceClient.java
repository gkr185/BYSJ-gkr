package com.bcu.edu.client;

import com.bcu.edu.client.fallback.OrderServiceClientFallback;
import com.bcu.edu.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OrderService Feign客户端
 * 
 * <p>调用OrderService的订单支付状态更新和判断订单类型接口
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@FeignClient(name = "order-service", fallback = OrderServiceClientFallback.class)
public interface OrderServiceClient {

    /**
     * 更新订单支付状态（支付成功后调用）
     * 
     * @param orderId 订单ID
     * @param payStatus 支付状态（0-未支付；1-已支付）
     * @return 成功/失败
     */
    @PostMapping("/api/order/feign/updatePayStatus")
    Result<Void> updatePayStatus(@RequestParam("orderId") Long orderId, 
                                  @RequestParam("payStatus") Integer payStatus);

    /**
     * 判断是否拼团订单（支付成功后调用）
     * 
     * @param orderId 订单ID
     * @return true-拼团订单；false-普通订单
     */
    @GetMapping("/api/order/feign/isGroupBuyOrder/{orderId}")
    Result<Boolean> isGroupBuyOrder(@PathVariable("orderId") Long orderId);
}

