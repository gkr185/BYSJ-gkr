package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.BatchShipRequest;
import com.bcu.edu.dto.BatchShipResult;
import com.bcu.edu.service.BatchShipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 批量发货控制器
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@RestController
@RequestMapping("/api/delivery/batch")
@Tag(name = "批量发货", description = "管理端批量发货操作")
@Validated
public class BatchShipController {

    @Autowired
    private BatchShipService batchShipService;

    /**
     * 批量发货
     */
    @PostMapping("/ship")
    @Operation(summary = "批量发货", description = "批量处理订单发货，生成配送单和配送路径")
    public Result<BatchShipResult> batchShipOrders(@Valid @RequestBody BatchShipRequest request) {
        log.info("批量发货请求: 订单数量={}, 操作员={}", request.getOrderIds().size(), request.getOperatorName());
        
        BatchShipResult result = batchShipService.batchShipOrders(request);
        
        if (result.getSuccess()) {
            return Result.success("批量发货处理完成", result);
        } else {
            return Result.error("批量发货失败: " + result.getMessage());
        }
    }

    /**
     * 重新发货失败的订单
     */
    @PostMapping("/retry")
    @Operation(summary = "重新发货", description = "重新处理批量发货中失败的订单")
    public Result<BatchShipResult> retryFailedOrders(
            @Parameter(description = "失败的订单ID列表", required = true)
            @RequestParam List<Long> failedOrderIds,
            @Parameter(description = "原始分单组", required = true)
            @RequestParam String originalDispatchGroup,
            @Valid @RequestBody BatchShipRequest originalRequest) {
        
        log.info("重新发货失败订单: 数量={}, 原分单组={}", failedOrderIds.size(), originalDispatchGroup);
        
        BatchShipResult result = batchShipService.retryFailedOrders(failedOrderIds, originalDispatchGroup, originalRequest);
        
        if (result.getSuccess()) {
            return Result.success("重新发货处理完成", result);
        } else {
            return Result.error("重新发货失败: " + result.getMessage());
        }
    }

    /**
     * 取消批量发货
     */
    @PostMapping("/cancel")
    @Operation(summary = "取消批量发货", description = "取消已执行的批量发货操作，恢复订单状态")
    public Result<Void> cancelBatchShip(
            @Parameter(description = "分单组", required = true)
            @RequestParam String dispatchGroup,
            @Parameter(description = "取消原因", required = true)
            @RequestParam String reason) {
        
        log.info("取消批量发货: 分单组={}, 原因={}", dispatchGroup, reason);
        
        batchShipService.cancelBatchShip(dispatchGroup, reason);
        return Result.success("批量发货已取消");
    }
}
