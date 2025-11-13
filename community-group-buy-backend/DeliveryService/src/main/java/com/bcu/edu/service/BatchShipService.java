package com.bcu.edu.service;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.dto.BatchShipRequest;
import com.bcu.edu.dto.BatchShipResult;
import com.bcu.edu.dto.CreateDeliveryRequest;
import com.bcu.edu.dto.OrderInfoDTO;
import com.bcu.edu.entity.Delivery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批量发货服务
 * 
 * <p>核心业务功能：
 * <ul>
 *   <li>批量发货订单处理</li>
 *   <li>分单组生成</li>
 *   <li>配送单创建</li>
 *   <li>订单状态批量更新</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@Service
@Transactional
public class BatchShipService {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private com.bcu.edu.client.OrderServiceClient orderServiceClient;

    private static final AtomicInteger BATCH_SEQUENCE = new AtomicInteger(1);

    /**
     * 批量发货处理
     * 
     * <p>业务流程：
     * <ol>
     *   <li>生成分单组标识</li>
     *   <li>验证订单状态（待发货）</li>
     *   <li>批量更新订单状态为配送中</li>
     *   <li>创建配送单并生成路径</li>
     * </ol>
     */
    @OperationLog(value = "批量发货处理", module = "批量发货")
    public BatchShipResult batchShipOrders(BatchShipRequest request) {
        log.info("开始批量发货，订单数量: {}, 操作员: {}", 
                request.getOrderIds().size(), request.getOperatorName());
        
        try {
            // 1. 生成分单组
            String dispatchGroup = generateDispatchGroup();
            
            // 2. 验证和更新订单状态
            BatchOrderUpdateResult updateResult = updateOrderStatuses(request.getOrderIds(), dispatchGroup);
            
            if (updateResult.getSuccessCount() == 0) {
                return BatchShipResult.error("所有订单更新失败，无法继续发货操作");
            }
            
            // 3. 创建配送单
            Delivery delivery = createDeliveryForBatch(request, dispatchGroup);
            
            // 4. 构建返回结果
            BatchShipResult result;
            if (updateResult.getFailedOrderIds().isEmpty()) {
                result = BatchShipResult.success(dispatchGroup, request.getOrderIds().size());
            } else {
                result = BatchShipResult.partialSuccess(
                        dispatchGroup,
                        request.getOrderIds().size(),
                        updateResult.getSuccessCount(),
                        updateResult.getFailedOrderIds(),
                        updateResult.getFailureReasons()
                );
            }
            
            result.setDelivery(delivery);
            
            log.info("批量发货完成，分单组: {}, 成功: {}/{}", 
                    dispatchGroup, updateResult.getSuccessCount(), request.getOrderIds().size());
            
            return result;
            
        } catch (Exception e) {
            log.error("批量发货失败", e);
            return BatchShipResult.error("批量发货失败: " + e.getMessage());
        }
    }

    /**
     * 生成分单组标识
     * 
     * <p>格式：DG + YYYYMMDD + 序号
     * <p>例如：DG20251113001
     */
    private String generateDispatchGroup() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int sequence = BATCH_SEQUENCE.getAndIncrement();
        return String.format("DG%s%03d", dateStr, sequence % 1000);
    }

    /**
     * 批量更新订单状态
     * 
     * <p>将订单状态从"待发货"更新为"配送中"，并设置分单组
     */
    private BatchOrderUpdateResult updateOrderStatuses(List<Long> orderIds, String dispatchGroup) {
        log.info("批量更新订单状态，订单数量: {}, 分单组: {}", orderIds.size(), dispatchGroup);
        
        BatchOrderUpdateResult result = new BatchOrderUpdateResult();
        result.setSuccessCount(0);
        result.setFailedOrderIds(new ArrayList<>());
        result.setFailureReasons(new ArrayList<>());
        
        try {
            // 调用OrderService的批量更新接口
            com.bcu.edu.client.OrderServiceClient.BatchShipUpdateRequest request = 
                    new com.bcu.edu.client.OrderServiceClient.BatchShipUpdateRequest();
            request.setOrderIds(orderIds);
            request.setDispatchGroup(dispatchGroup);
            request.setOperatorName("DeliveryService");
            
            com.bcu.edu.common.result.Result<com.bcu.edu.client.OrderServiceClient.BatchUpdateResult> updateResult = 
                    orderServiceClient.batchUpdateToShipping(request);
            
            if (updateResult.isSuccess() && updateResult.getData() != null) {
                com.bcu.edu.client.OrderServiceClient.BatchUpdateResult data = updateResult.getData();
                result.setSuccessCount(data.getSuccessCount());
                result.setFailedOrderIds(data.getFailedOrderIds() != null ? data.getFailedOrderIds() : new ArrayList<>());
                
                // 生成失败原因
                if (!result.getFailedOrderIds().isEmpty()) {
                    List<String> reasons = new ArrayList<>();
                    for (Long orderId : result.getFailedOrderIds()) {
                        reasons.add("订单" + orderId + "状态更新失败");
                    }
                    result.setFailureReasons(reasons);
                }
            } else {
                log.warn("批量更新订单状态失败: {}", updateResult.getMessage());
                // 全部失败
                result.setSuccessCount(0);
                result.setFailedOrderIds(orderIds);
                List<String> reasons = new ArrayList<>();
                for (Long orderId : orderIds) {
                    reasons.add("订单" + orderId + "更新失败: " + updateResult.getMessage());
                }
                result.setFailureReasons(reasons);
            }
            
        } catch (Exception e) {
            log.error("批量更新订单状态异常", e);
            // 全部失败
            result.setSuccessCount(0);
            result.setFailedOrderIds(orderIds);
            List<String> reasons = new ArrayList<>();
            for (Long orderId : orderIds) {
                reasons.add("订单" + orderId + "更新异常: " + e.getMessage());
            }
            result.setFailureReasons(reasons);
        }
        
        log.info("订单状态更新完成，成功: {}, 失败: {}", 
                result.getSuccessCount(), result.getFailedOrderIds().size());
        
        return result;
    }


    /**
     * 为批量发货创建配送单
     */
    private Delivery createDeliveryForBatch(BatchShipRequest request, String dispatchGroup) {
        log.info("为批量发货创建配送单，分单组: {}", dispatchGroup);
        
        // 从订单信息推断团长ID（临时实现）
        Long leaderId = inferLeaderIdFromOrders(request.getOrderIds());
        
        CreateDeliveryRequest deliveryRequest = new CreateDeliveryRequest();
        deliveryRequest.setDispatchGroup(dispatchGroup);
        deliveryRequest.setLeaderId(leaderId);
        deliveryRequest.setRouteStrategy(request.getRouteStrategy());
        deliveryRequest.setWarehouseId(request.getWarehouseId());
        deliveryRequest.setGenerateRoute(true);
        deliveryRequest.setRemark("批量发货创建 - " + request.getRemark());
        
        return deliveryService.createDelivery(deliveryRequest);
    }

    /**
     * 从订单列表推断团长ID
     */
    private Long inferLeaderIdFromOrders(List<Long> orderIds) {
        try {
            com.bcu.edu.common.result.Result<List<OrderInfoDTO>> result = 
                    orderServiceClient.getOrdersByIds(orderIds);
            
            if (result.isSuccess() && result.getData() != null && !result.getData().isEmpty()) {
                Long leaderId = result.getData().get(0).getLeaderId();
                log.info("从订单中获取团长ID: {}", leaderId);
                return leaderId;
            } else {
                log.warn("获取订单团长信息失败: {}", result.getMessage());
            }
        } catch (Exception e) {
            log.warn("查询订单团长信息异常", e);
        }
        
        // 降级返回默认团长ID
        log.warn("使用默认团长ID，请检查OrderService连接");
        return 1001L;
    }

    /**
     * 重新发货（针对失败的订单）
     */
    @OperationLog(value = "重新发货", module = "批量发货")
    public BatchShipResult retryFailedOrders(List<Long> failedOrderIds, String originalDispatchGroup, 
                                           BatchShipRequest originalRequest) {
        log.info("重新发货失败订单，数量: {}, 原分单组: {}", failedOrderIds.size(), originalDispatchGroup);
        
        BatchShipRequest retryRequest = new BatchShipRequest();
        retryRequest.setOrderIds(failedOrderIds);
        retryRequest.setWarehouseId(originalRequest.getWarehouseId());
        retryRequest.setRouteStrategy(originalRequest.getRouteStrategy());
        retryRequest.setRemark("重新发货 - " + originalRequest.getRemark());
        retryRequest.setOperatorId(originalRequest.getOperatorId());
        retryRequest.setOperatorName(originalRequest.getOperatorName());
        
        return batchShipOrders(retryRequest);
    }

    /**
     * 取消批量发货
     */
    @OperationLog(value = "取消发货", module = "批量发货")
    public void cancelBatchShip(String dispatchGroup, String reason) {
        log.info("取消批量发货，分单组: {}, 原因: {}", dispatchGroup, reason);
        
        try {
            // 1. 恢复订单状态
            // TODO: 调用OrderService恢复订单状态
            
            // 2. 删除配送单
            Delivery delivery = deliveryService.getDeliveryByDispatchGroup(dispatchGroup);
            deliveryService.deleteDelivery(delivery.getDeliveryId());
            
            log.info("批量发货取消成功");
            
        } catch (Exception e) {
            log.error("取消批量发货失败", e);
            throw new RuntimeException("取消批量发货失败: " + e.getMessage());
        }
    }

    /**
     * 批量订单更新结果
     */
    private static class BatchOrderUpdateResult {
        private Integer successCount;
        private List<Long> failedOrderIds;
        private List<String> failureReasons;

        // Getters and Setters
        public Integer getSuccessCount() { return successCount; }
        public void setSuccessCount(Integer successCount) { this.successCount = successCount; }

        public List<Long> getFailedOrderIds() { return failedOrderIds; }
        public void setFailedOrderIds(List<Long> failedOrderIds) { this.failedOrderIds = failedOrderIds; }

        public List<String> getFailureReasons() { return failureReasons; }
        public void setFailureReasons(List<String> failureReasons) { this.failureReasons = failureReasons; }
    }
}
