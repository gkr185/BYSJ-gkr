package com.bcu.edu.controller;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.result.PageResult;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.OrderDetailVO;
import com.bcu.edu.dto.response.OrderStatisticsVO;
import com.bcu.edu.dto.response.OrderVO;
import com.bcu.edu.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 订单管理端控制器
 *
 * @author 耿康瑞
 * @date 2025-11-01
 */
@Slf4j
@RestController
@RequestMapping("/api/order/admin")
@Tag(name = "订单管理端接口", description = "供管理员使用的订单管理接口")
public class AdminController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取订单列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "获取订单列表", description = "分页查询所有订单")
    @OperationLog(value = "查询订单列表", module = "订单管理")
    public Result<PageResult<OrderVO>> getOrderList(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "订单状态（可选）") @RequestParam(required = false) Integer status,
            @Parameter(description = "支付状态（可选）") @RequestParam(required = false) Integer payStatus) {
        
        log.info("管理端查询订单列表: page={}, size={}, status={}, payStatus={}", page, size, status, payStatus);
        PageResult<OrderVO> result = orderService.getOrderListForAdmin(page, size, status, payStatus);
        return Result.success(result);
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取订单统计", description = "获取各状态订单数量统计")
    @OperationLog(value = "查询订单统计", module = "订单管理")
    public Result<OrderStatisticsVO> getOrderStatistics() {
        log.info("管理端查询订单统计");
        OrderStatisticsVO statistics = orderService.getOrderStatistics();
        return Result.success(statistics);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/status/{orderId}")
    @Operation(summary = "更新订单状态", description = "管理员更新单个订单状态")
    @OperationLog(value = "更新订单状态", module = "订单管理")
    public Result<Void> updateOrderStatus(
            @Parameter(description = "订单ID") @PathVariable Long orderId,
            @Parameter(description = "订单状态") @RequestParam Integer status) {
        
        log.info("管理端更新订单状态: orderId={}, status={}", orderId, status);
        orderService.updateOrderStatusByAdmin(orderId, status);
        return Result.success("订单状态更新成功");
    }

    /**
     * 批量更新订单状态
     */
    @PostMapping("/batchUpdateStatus")
    @Operation(summary = "批量更新订单状态", description = "批量更新多个订单状态")
    @OperationLog(value = "批量更新订单状态", module = "订单管理")
    public Result<Void> batchUpdateOrderStatus(
            @RequestBody List<Long> orderIds,
            @Parameter(description = "订单状态") @RequestParam Integer status) {
        
        log.info("管理端批量更新订单状态: orderIds={}, status={}", orderIds, status);
        orderService.batchUpdateStatus(orderIds, status);
        return Result.success("批量更新成功");
    }

    /**
     * 按状态查询订单
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "按状态查询订单", description = "查询指定状态的订单列表")
    @OperationLog(value = "按状态查询订单", module = "订单管理")
    public Result<PageResult<OrderVO>> getOrdersByStatus(
            @Parameter(description = "订单状态") @PathVariable Integer status,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("管理端按状态查询订单: status={}, page={}, size={}", status, page, size);
        PageResult<OrderVO> result = orderService.getOrderListForAdmin(page, size, status, null);
        return Result.success(result);
    }

    /**
     * 搜索订单
     */
    @GetMapping("/search")
    @Operation(summary = "搜索订单", description = "根据订单号或商品名称搜索订单")
    @OperationLog(value = "搜索订单", module = "订单管理")
    public Result<PageResult<OrderVO>> searchOrders(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("管理端搜索订单: keyword={}, page={}, size={}", keyword, page, size);
        PageResult<OrderVO> result = orderService.searchOrders(keyword, page, size);
        return Result.success(result);
    }

    /**
     * 导出订单
     */
    @GetMapping("/export")
    @Operation(summary = "导出订单", description = "导出订单数据为Excel文件")
    @OperationLog(value = "导出订单", module = "订单管理")
    public void exportOrders(
            @Parameter(description = "订单状态（可选）") @RequestParam(required = false) Integer status,
            @Parameter(description = "支付状态（可选）") @RequestParam(required = false) Integer payStatus,
            @Parameter(description = "开始日期（可选）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（可选）") @RequestParam(required = false) String endDate,
            HttpServletResponse response) {
        
        log.info("管理端导出订单: status={}, payStatus={}, startDate={}, endDate={}", 
                status, payStatus, startDate, endDate);
        orderService.exportOrders(status, payStatus, startDate, endDate, response);
    }

    /**
     * 获取用户的订单列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户订单", description = "查询指定用户的订单列表")
    @OperationLog(value = "查询用户订单", module = "订单管理")
    public Result<PageResult<OrderVO>> getUserOrders(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("管理端查询用户订单: userId={}, page={}, size={}", userId, page, size);
        PageResult<OrderVO> result = orderService.getMyOrders(userId, page, size);
        return Result.success(result);
    }

    /**
     * 获取团长的订单列表
     */
    @GetMapping("/leader/{leaderId}")
    @Operation(summary = "获取团长订单", description = "查询指定团长的订单列表")
    @OperationLog(value = "查询团长订单", module = "订单管理")
    public Result<PageResult<OrderVO>> getLeaderOrders(
            @Parameter(description = "团长ID") @PathVariable Long leaderId,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer size) {
        
        log.info("管理端查询团长订单: leaderId={}, page={}, size={}", leaderId, page, size);
        PageResult<OrderVO> result = orderService.getLeaderOrders(leaderId, page, size);
        return Result.success(result);
    }
}

