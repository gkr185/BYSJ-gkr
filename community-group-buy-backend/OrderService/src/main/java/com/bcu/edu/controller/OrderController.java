package com.bcu.edu.controller;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.result.PageResult;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.OrderDetailVO;
import com.bcu.edu.dto.response.OrderVO;
import com.bcu.edu.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单Controller（用户端API）
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@RestController
@RequestMapping("/api/order")
@Tag(name = "订单管理", description = "订单查询、订单取消、确认收货等")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 查询我的订单列表
     */
    @GetMapping("/my")
    @Operation(summary = "查询我的订单", description = "分页查询当前用户的订单列表")
    @OperationLog(value = "查询我的订单", module = "订单管理")
    public Result<PageResult<OrderVO>> getMyOrders(
        HttpServletRequest request,
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "10") Integer size
    ) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("查询我的订单: userId={}, page={}, size={}", userId, page, size);

        PageResult<OrderVO> result = orderService.getMyOrders(userId, page, size);
        return Result.success(result);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{orderId}")
    @Operation(summary = "查询订单详情", description = "根据订单ID查询订单详细信息")
    @OperationLog(value = "查询订单详情", module = "订单管理")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        log.info("查询订单详情: orderId={}", orderId);

        OrderDetailVO detail = orderService.getOrderDetail(orderId);
        return Result.success(detail);
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderId}")
    @Operation(summary = "取消订单", description = "用户主动取消订单")
    @OperationLog(value = "取消订单", module = "订单管理")
    public Result<Void> cancelOrder(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("取消订单: orderId={}, userId={}", orderId, userId);

        orderService.cancelOrder(orderId);
        return Result.success("订单已取消");
    }

    /**
     * 确认收货
     */
    @PostMapping("/confirm/{orderId}")
    @Operation(summary = "确认收货", description = "用户确认收货")
    @OperationLog(value = "确认收货", module = "订单管理")
    public Result<Void> confirmReceipt(@PathVariable Long orderId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        log.info("确认收货: orderId={}, userId={}", orderId, userId);

        // 更新订单状态为已送达
        orderService.updateOrderStatus(orderId, 3);
        return Result.success("确认收货成功");
    }
}

