package com.bcu.edu.client;

import com.bcu.edu.client.fallback.OrderServiceClientFallback;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.CreateOrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrderService Feign客户端（⭐核心客户端）
 * 
 * <p>功能：
 * <ul>
 *   <li>创建订单（参团时）</li>
 *   <li>批量更新订单状态（成团时）</li>
 *   <li>更新订单状态（退款时）</li>
 *   <li>取消订单（参团失败时补偿）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@FeignClient(name = "order-service", fallback = OrderServiceClientFallback.class)
public interface OrderServiceClient {
    
    /**
     * 创建订单（⭐核心接口）
     * 
     * <p>应用场景：
     * <ul>
     *   <li>用户参团时创建订单</li>
     *   <li>团长发起并参与时创建订单</li>
     * </ul>
     * 
     * @param request 创建订单请求
     * @return Result<Long> 订单ID
     */
    @PostMapping("/api/order/feign/create")
    Result<Long> createOrder(@RequestBody CreateOrderRequest request);
    
    /**
     * 批量更新订单状态（⭐核心接口）
     * 
     * <p>应用场景：
     * <ul>
     *   <li>成团后批量更新所有成员的订单状态为"待发货"</li>
     * </ul>
     * 
     * @param orderIds 订单ID列表
     * @param status 订单状态（1-待发货）
     * @return Result<Void>
     */
    @PostMapping("/api/order/feign/batchUpdateStatus")
    Result<Void> batchUpdateOrderStatus(@RequestBody List<Long> orderIds, 
                                         @RequestParam("status") Integer status);
    
    /**
     * 更新订单状态
     * 
     * <p>应用场景：
     * <ul>
     *   <li>退款时更新订单状态为"已退款"</li>
     * </ul>
     * 
     * @param orderId 订单ID
     * @param status 订单状态（6-已退款）
     * @return Result<Void>
     */
    @PostMapping("/api/order/feign/updateStatus")
    Result<Void> updateOrderStatus(@RequestParam("orderId") Long orderId, 
                                    @RequestParam("status") Integer status);
    
    /**
     * 取消订单（补偿机制）
     *
     * <p>应用场景：
     * <ul>
     *   <li>参团失败时取消订单</li>
     * </ul>
     *
     * @param orderId 订单ID
     * @return Result<Void>
     */
    @PostMapping("/api/order/feign/cancel/{orderId}")
    Result<Void> cancelOrder(@PathVariable("orderId") Long orderId);

    /**
     * 获取订单商品总数量
     *
     * <p>应用场景：
     * <ul>
     *   <li>拼团详情页显示每个成员购买的商品数量</li>
     * </ul>
     *
     * @param orderId 订单ID
     * @return Result<Integer> 商品总数量
     */
    @GetMapping("/api/order/feign/getProductQuantity/{orderId}")
    Result<Integer> getProductQuantity(@PathVariable("orderId") Long orderId);
}

