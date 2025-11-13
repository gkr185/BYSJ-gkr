package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.CreateDeliveryRequest;
import com.bcu.edu.entity.Delivery;
import com.bcu.edu.enums.DeliveryStatus;
import com.bcu.edu.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 配送单管理控制器
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@RestController
@RequestMapping("/api/delivery")
@Tag(name = "配送单管理", description = "配送单创建、查询、状态更新等操作")
@Validated
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    /**
     * 创建配送单
     */
    @PostMapping
    @Operation(summary = "创建配送单", description = "根据分单组创建配送单并生成配送路径")
    public Result<Delivery> createDelivery(@Valid @RequestBody CreateDeliveryRequest request) {
        log.info("创建配送单请求: {}", request.getDispatchGroup());
        
        Delivery delivery = deliveryService.createDelivery(request);
        return Result.success("配送单创建成功", delivery);
    }

    /**
     * 根据ID查询配送单
     */
    @GetMapping("/{deliveryId}")
    @Operation(summary = "查询配送单详情", description = "根据配送单ID查询详细信息")
    public Result<Delivery> getDelivery(
            @Parameter(description = "配送单ID", required = true)
            @PathVariable @NotNull Long deliveryId) {
        
        Delivery delivery = deliveryService.getDeliveryById(deliveryId);
        return Result.success(delivery);
    }

    /**
     * 根据分单组查询配送单
     */
    @GetMapping("/dispatch-group/{dispatchGroup}")
    @Operation(summary = "根据分单组查询配送单", description = "根据分单组标识查询对应的配送单")
    public Result<Delivery> getDeliveryByDispatchGroup(
            @Parameter(description = "分单组标识", required = true)
            @PathVariable String dispatchGroup) {
        
        Delivery delivery = deliveryService.getDeliveryByDispatchGroup(dispatchGroup);
        return Result.success(delivery);
    }

    /**
     * 查询团长的配送单列表
     */
    @GetMapping("/leader/{leaderId}")
    @Operation(summary = "查询团长配送单", description = "查询指定团长的所有配送单")
    public Result<List<Delivery>> getDeliveriesByLeader(
            @Parameter(description = "团长ID", required = true)
            @PathVariable @NotNull Long leaderId) {
        
        List<Delivery> deliveries = deliveryService.getDeliveriesByLeaderId(leaderId);
        return Result.success(deliveries);
    }

    /**
     * 查询团长指定状态的配送单
     */
    @GetMapping("/leader/{leaderId}/status/{status}")
    @Operation(summary = "查询团长指定状态的配送单", description = "查询团长指定状态的配送单列表")
    public Result<List<Delivery>> getDeliveriesByLeaderAndStatus(
            @Parameter(description = "团长ID", required = true)
            @PathVariable @NotNull Long leaderId,
            @Parameter(description = "配送状态", required = true)
            @PathVariable Integer status) {
        
        DeliveryStatus deliveryStatus = DeliveryStatus.fromCode(status);
        List<Delivery> deliveries = deliveryService.getDeliveriesByLeaderAndStatus(leaderId, deliveryStatus);
        return Result.success(deliveries);
    }

    /**
     * 更新配送状态
     */
    @PutMapping("/{deliveryId}/status")
    @Operation(summary = "更新配送状态", description = "更新配送单的状态（待分配→配送中→已完成）")
    public Result<Delivery> updateDeliveryStatus(
            @Parameter(description = "配送单ID", required = true)
            @PathVariable @NotNull Long deliveryId,
            @Parameter(description = "新状态（0-待分配；1-配送中；2-已完成）", required = true)
            @RequestParam @NotNull Integer status) {
        
        DeliveryStatus newStatus = DeliveryStatus.fromCode(status);
        Delivery delivery = deliveryService.updateDeliveryStatus(deliveryId, newStatus);
        return Result.success("配送状态更新成功", delivery);
    }

    /**
     * 开始配送
     */
    @PutMapping("/{deliveryId}/start")
    @Operation(summary = "开始配送", description = "将配送状态从待分配更新为配送中")
    public Result<Delivery> startDelivery(
            @Parameter(description = "配送单ID", required = true)
            @PathVariable @NotNull Long deliveryId) {
        
        Delivery delivery = deliveryService.startDelivery(deliveryId);
        return Result.success("配送已开始", delivery);
    }

    /**
     * 完成配送
     */
    @PutMapping("/{deliveryId}/complete")
    @Operation(summary = "完成配送", description = "将配送状态从配送中更新为已完成")
    public Result<Delivery> completeDelivery(
            @Parameter(description = "配送单ID", required = true)
            @PathVariable @NotNull Long deliveryId) {
        
        Delivery delivery = deliveryService.completeDelivery(deliveryId);
        return Result.success("配送已完成", delivery);
    }

    /**
     * 重新生成配送路径
     */
    @PutMapping("/{deliveryId}/regenerate-route")
    @Operation(summary = "重新生成配送路径", description = "重新计算配送路径（仅限待分配状态）")
    public Result<Delivery> regenerateRoute(
            @Parameter(description = "配送单ID", required = true)
            @PathVariable @NotNull Long deliveryId,
            @Parameter(description = "路径策略", required = true)
            @RequestParam String routeStrategy) {
        
        Delivery delivery = deliveryService.regenerateRoute(deliveryId, routeStrategy);
        return Result.success("配送路径重新生成成功", delivery);
    }

    /**
     * 删除配送单
     */
    @DeleteMapping("/{deliveryId}")
    @Operation(summary = "删除配送单", description = "删除配送单（仅限待分配状态）")
    public Result<Void> deleteDelivery(
            @Parameter(description = "配送单ID", required = true)
            @PathVariable @NotNull Long deliveryId) {
        
        deliveryService.deleteDelivery(deliveryId);
        return Result.success("配送单删除成功");
    }

    /**
     * 获取配送统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取配送统计信息", description = "获取指定时间范围内的配送统计数据")
    public Result<DeliveryService.DeliveryStatistics> getDeliveryStatistics(
            @Parameter(description = "团长ID（可选，不传则统计所有团长）")
            @RequestParam(required = false) Long leaderId,
            @Parameter(description = "开始日期", required = true)
            @RequestParam String startDate,
            @Parameter(description = "结束日期", required = true)
            @RequestParam String endDate) {
        
        LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
        
        DeliveryService.DeliveryStatistics statistics = 
                deliveryService.getDeliveryStatistics(leaderId, startDateTime, endDateTime);
        
        return Result.success(statistics);
    }
}
