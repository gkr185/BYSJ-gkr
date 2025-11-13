package com.bcu.edu.dto;

import com.bcu.edu.entity.Delivery;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量发货结果
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Data
@Schema(description = "批量发货结果")
public class BatchShipResult {

    @Schema(description = "是否成功", example = "true", required = true)
    private Boolean success;

    @Schema(description = "消息", example = "批量发货成功")
    private String message;

    @Schema(description = "生成的分单组", example = "DG20251113001", required = true)
    private String dispatchGroup;

    @Schema(description = "处理的订单数量", example = "10", required = true)
    private Integer totalOrders;

    @Schema(description = "成功处理的订单数量", example = "10")
    private Integer successOrders;

    @Schema(description = "失败处理的订单数量", example = "0")
    private Integer failedOrders;

    @Schema(description = "创建的配送单")
    private Delivery delivery;

    @Schema(description = "路径规划结果")
    private RouteResult routeResult;

    @Schema(description = "失败的订单ID列表")
    private List<Long> failedOrderIds;

    @Schema(description = "失败原因")
    private List<String> failureReasons;

    @Schema(description = "处理时间", example = "2025-11-13 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;

    public BatchShipResult() {
        this.processedAt = LocalDateTime.now();
    }

    /**
     * 创建成功结果
     */
    public static BatchShipResult success(String dispatchGroup, Integer totalOrders) {
        BatchShipResult result = new BatchShipResult();
        result.setSuccess(true);
        result.setMessage("批量发货成功");
        result.setDispatchGroup(dispatchGroup);
        result.setTotalOrders(totalOrders);
        result.setSuccessOrders(totalOrders);
        result.setFailedOrders(0);
        return result;
    }

    /**
     * 创建部分成功结果
     */
    public static BatchShipResult partialSuccess(String dispatchGroup, Integer totalOrders, 
                                               Integer successOrders, List<Long> failedOrderIds, 
                                               List<String> failureReasons) {
        BatchShipResult result = new BatchShipResult();
        result.setSuccess(true);
        result.setMessage("部分订单发货成功");
        result.setDispatchGroup(dispatchGroup);
        result.setTotalOrders(totalOrders);
        result.setSuccessOrders(successOrders);
        result.setFailedOrders(totalOrders - successOrders);
        result.setFailedOrderIds(failedOrderIds);
        result.setFailureReasons(failureReasons);
        return result;
    }

    /**
     * 创建失败结果
     */
    public static BatchShipResult error(String message) {
        BatchShipResult result = new BatchShipResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}
