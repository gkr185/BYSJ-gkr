package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.CreateOrderRequest;
import com.bcu.edu.dto.response.OrderDetailVO;
import com.bcu.edu.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign内部接口Controller（供其他微服务调用）
 * 
 * <p>🔴 核心接口！供GroupBuyService调用
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@RestController
@RequestMapping("/api/order/feign")
@Tag(name = "订单Feign接口", description = "供其他微服务调用的内部接口")
@Slf4j
public class FeignController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单（⭐最关键接口）
     * 
     * <p>调用方: GroupBuyService.joinTeam()
     * <p>场景: 用户参团时创建订单
     * 
     * @param request 创建订单请求
     * @return 订单ID
     */
    @PostMapping("/create")
    @Operation(summary = "创建订单", description = "供GroupBuyService调用，用户参团时创建订单")
    public Result<Long> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("Feign调用: 创建订单, userId={}, productId={}, quantity={}", 
                 request.getUserId(), request.getProductId(), request.getQuantity());

        try {
            Long orderId = orderService.createOrder(request);
            log.info("订单创建成功: orderId={}", orderId);
            return Result.success("订单创建成功", orderId);
        } catch (Exception e) {
            log.error("订单创建失败", e);
            return Result.error("订单创建失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新订单状态（⭐核心接口）
     * 
     * <p>调用方: GroupBuyService成团逻辑
     * <p>场景: 成团后批量更新所有成员订单状态为"待发货"
     * 
     * @param orderIds 订单ID列表
     * @param status 订单状态（1-待发货）
     * @return 成功/失败
     */
    @PostMapping("/batchUpdateStatus")
    @Operation(summary = "批量更新订单状态", description = "成团后批量更新订单状态")
    public Result<Void> batchUpdateOrderStatus(@RequestBody List<Long> orderIds, 
                                                @RequestParam("status") Integer status) {
        log.info("Feign调用: 批量更新订单状态, orderIds={}, status={}", orderIds, status);

        try {
            orderService.batchUpdateStatus(orderIds, status);
            log.info("批量更新成功: 共{}条订单", orderIds.size());
            return Result.success("订单状态更新成功");
        } catch (Exception e) {
            log.error("批量更新失败", e);
            return Result.error("订单状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新单个订单状态（⭐核心接口）
     * 
     * <p>调用方: GroupBuyService退款逻辑
     * <p>场景: 退款时更新订单状态为"已退款"
     * 
     * @param orderId 订单ID
     * @param status 订单状态（6-已退款）
     * @return 成功/失败
     */
    @PostMapping("/updateStatus")
    @Operation(summary = "更新订单状态", description = "更新单个订单状态")
    public Result<Void> updateOrderStatus(@RequestParam("orderId") Long orderId, 
                                          @RequestParam("status") Integer status) {
        log.info("Feign调用: 更新订单状态, orderId={}, status={}", orderId, status);

        try {
            orderService.updateOrderStatus(orderId, status);
            log.info("订单状态更新成功: orderId={}", orderId);
            return Result.success("订单状态更新成功");
        } catch (Exception e) {
            log.error("订单状态更新失败", e);
            return Result.error("订单状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 取消订单（⭐核心接口）
     * 
     * <p>调用方: GroupBuyService退团逻辑
     * <p>场景: 用户退团或拼团失败时取消订单
     * 
     * @param orderId 订单ID
     * @return 成功/失败
     */
    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单", description = "取消订单（退团时调用）")
    public Result<Void> cancelOrder(@PathVariable("orderId") Long orderId) {
        log.info("Feign调用: 取消订单, orderId={}", orderId);

        try {
            orderService.cancelOrder(orderId);
            log.info("订单取消成功: orderId={}", orderId);
            return Result.success("订单取消成功");
        } catch (Exception e) {
            log.error("订单取消失败", e);
            return Result.error("订单取消失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单详情（供其他服务调用）
     * 
     * @param orderId 订单ID
     * @return 订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "查询订单详情", description = "获取订单详细信息")
    public Result<OrderDetailVO> getOrder(@PathVariable("orderId") Long orderId) {
        log.info("Feign调用: 查询订单详情, orderId={}", orderId);

        try {
            OrderDetailVO order = orderService.getOrderDetail(orderId);
            return Result.success(order);
        } catch (Exception e) {
            log.error("查询订单失败", e);
            return Result.error("查询订单失败: " + e.getMessage());
        }
    }
}

