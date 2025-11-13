package com.bcu.edu.client;

import com.bcu.edu.client.fallback.OrderServiceClientFallback;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.OrderInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * OrderService Feign客户端
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@FeignClient(name = "order-service", contextId = "delivery-order-service", fallback = OrderServiceClientFallback.class)
public interface OrderServiceClient {

    /**
     * 根据分单组获取订单列表
     */
    @GetMapping("/api/order/feign/by-dispatch-group/{dispatchGroup}")
    Result<List<OrderInfoDTO>> getOrdersByDispatchGroup(@PathVariable("dispatchGroup") String dispatchGroup);

    /**
     * 批量获取订单信息
     */
    @PostMapping("/api/order/feign/batch")
    Result<List<OrderInfoDTO>> getOrdersByIds(@RequestBody List<Long> orderIds);

    /**
     * 批量更新订单状态为配送中
     */
    @PostMapping("/api/order/feign/batch-ship")
    Result<BatchUpdateResult> batchUpdateToShipping(@RequestBody BatchShipUpdateRequest request);

    /**
     * 更新配送单ID
     */
    @PostMapping("/api/order/feign/update-delivery")
    Result<Void> updateDeliveryId(@RequestBody UpdateDeliveryRequest request);

    /**
     * 批量更新请求
     */
    class BatchShipUpdateRequest {
        private List<Long> orderIds;
        private String dispatchGroup;
        private String operatorName;

        // Getters and Setters
        public List<Long> getOrderIds() { return orderIds; }
        public void setOrderIds(List<Long> orderIds) { this.orderIds = orderIds; }

        public String getDispatchGroup() { return dispatchGroup; }
        public void setDispatchGroup(String dispatchGroup) { this.dispatchGroup = dispatchGroup; }

        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    }

    /**
     * 更新配送单ID请求
     */
    class UpdateDeliveryRequest {
        private String dispatchGroup;
        private Long deliveryId;

        public UpdateDeliveryRequest() {}
        
        public UpdateDeliveryRequest(String dispatchGroup, Long deliveryId) {
            this.dispatchGroup = dispatchGroup;
            this.deliveryId = deliveryId;
        }

        // Getters and Setters
        public String getDispatchGroup() { return dispatchGroup; }
        public void setDispatchGroup(String dispatchGroup) { this.dispatchGroup = dispatchGroup; }

        public Long getDeliveryId() { return deliveryId; }
        public void setDeliveryId(Long deliveryId) { this.deliveryId = deliveryId; }
    }

    /**
     * 批量更新结果
     */
    class BatchUpdateResult {
        private Integer successCount;
        private Integer failedCount;
        private List<Long> failedOrderIds;

        // Getters and Setters
        public Integer getSuccessCount() { return successCount; }
        public void setSuccessCount(Integer successCount) { this.successCount = successCount; }

        public Integer getFailedCount() { return failedCount; }
        public void setFailedCount(Integer failedCount) { this.failedCount = failedCount; }

        public List<Long> getFailedOrderIds() { return failedOrderIds; }
        public void setFailedOrderIds(List<Long> failedOrderIds) { this.failedOrderIds = failedOrderIds; }
    }
}
