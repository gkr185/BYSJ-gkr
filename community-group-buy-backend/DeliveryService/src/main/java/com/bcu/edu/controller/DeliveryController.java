package com.bcu.edu.controller;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.result.PageResult;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.BatchShipRequest;
import com.bcu.edu.dto.BatchShipResponse;
import com.bcu.edu.dto.DeliveryDetailDTO;
import com.bcu.edu.dto.RouteResult;
import com.bcu.edu.entity.DeliveryEntity;
import com.bcu.edu.service.BatchShipService;
import com.bcu.edu.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * 配送管理Controller
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@Tag(name = "配送管理", description = "配送单管理、批量发货、路径规划")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final BatchShipService batchShipService;

    /**
     * 批量发货（核心接口）⭐⭐⭐⭐⭐
     */
    @PostMapping("/batch/ship")
    @OperationLog(value = "批量发货", module = "配送管理")
    @Operation(summary = "批量发货", description = "管理员批量发货，生成配送单并更新订单状态")
    public Result<BatchShipResponse> batchShip(@Valid @RequestBody BatchShipRequest request) {
        log.info("接收批量发货请求，订单数量={}", request.getOrderIds().size());
        
        BatchShipResponse response = batchShipService.batchShip(request);
        
        return Result.success("批量发货成功", response);
    }

    /**
     * 查询配送单列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "查询配送单列表", description = "分页查询配送单，支持按状态筛选")
    public Result<PageResult<DeliveryEntity>> listDeliveries(
            @Parameter(description = "配送状态（0-待分配；1-配送中；2-已完成）")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码（从0开始）")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页大小")
            @RequestParam(defaultValue = "10") Integer size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<DeliveryEntity> deliveryPage = deliveryService.listDeliveries(status, pageable);

        PageResult<DeliveryEntity> pageResult = new PageResult<>(
                deliveryPage.getTotalElements(),
                deliveryPage.getContent()
        );

        return Result.success(pageResult);
    }

    /**
     * 查询配送单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询配送单详情", description = "查询配送单完整信息，包含订单列表和路径信息")
    public Result<DeliveryDetailDTO> getDeliveryDetail(
            @Parameter(description = "配送单ID") @PathVariable Long id) {
        
        DeliveryDetailDTO detail = deliveryService.getDeliveryDetail(id);
        
        return Result.success(detail);
    }

    /**
     * 重新规划路径
     */
    @PostMapping("/{id}/replan")
    @OperationLog(value = "重新规划路径", module = "配送管理")
    @Operation(summary = "重新规划路径", description = "重新计算配送路径，更新配送单")
    public Result<RouteResult> replanRoute(
            @Parameter(description = "配送单ID") @PathVariable Long id) {
        
        RouteResult result = deliveryService.replanRoute(id);
        
        return Result.success("路径重新规划成功", result);
    }

    /**
     * 手动完成配送
     */
    @PostMapping("/{id}/complete")
    @OperationLog(value = "完成配送", module = "配送管理")
    @Operation(summary = "完成配送", description = "手动标记配送完成，更新订单状态为已送达")
    public Result<Void> completeDelivery(
            @Parameter(description = "配送单ID") @PathVariable Long id) {
        
        deliveryService.completeDelivery(id);
        
        return Result.success("配送完成");
    }

    /**
     * 取消配送
     */
    @PostMapping("/{id}/cancel")
    @OperationLog(value = "取消配送", module = "配送管理")
    @Operation(summary = "取消配送", description = "取消配送单，删除配送记录")
    public Result<Void> cancelDelivery(
            @Parameter(description = "配送单ID") @PathVariable Long id) {
        
        deliveryService.cancelDelivery(id);
        
        return Result.success("配送已取消");
    }
}

