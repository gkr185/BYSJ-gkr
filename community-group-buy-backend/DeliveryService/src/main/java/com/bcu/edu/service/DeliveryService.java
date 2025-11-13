package com.bcu.edu.service;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.dto.*;
import com.bcu.edu.entity.Delivery;
import com.bcu.edu.entity.WarehouseConfig;
import com.bcu.edu.enums.DeliveryStatus;
import com.bcu.edu.enums.RouteStrategy;
import com.bcu.edu.repository.DeliveryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * 配送服务
 * 
 * <p>主要功能：
 * <ul>
 *   <li>配送单创建和管理</li>
 *   <li>路径规划集成</li>
 *   <li>配送状态管理</li>
 *   <li>配送统计报表</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@Service
@Transactional
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private RouteService routeService;

    @Autowired
    private WarehouseConfigService warehouseConfigService;

    @Autowired
    private com.bcu.edu.client.OrderServiceClient orderServiceClient;

    @Autowired
    private com.bcu.edu.client.UserServiceClient userServiceClient;

    /**
     * 创建配送单
     * 
     * <p>业务流程：
     * <ol>
     *   <li>验证分单组唯一性</li>
     *   <li>获取仓库起点信息</li>
     *   <li>调用路径规划服务</li>
     *   <li>保存配送单信息</li>
     * </ol>
     */
    @OperationLog(value = "创建配送单", module = "配送管理")
    public Delivery createDelivery(CreateDeliveryRequest request) {
        log.info("开始创建配送单，分单组: {}, 团长ID: {}", request.getDispatchGroup(), request.getLeaderId());
        
        try {
            // 1. 验证分单组唯一性
            if (deliveryRepository.existsByDispatchGroup(request.getDispatchGroup())) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "分单组已存在: " + request.getDispatchGroup());
            }
            
            // 2. 创建配送单基础信息
            Delivery delivery = new Delivery();
            delivery.setLeaderId(request.getLeaderId());
            delivery.setDispatchGroup(request.getDispatchGroup());
            delivery.setDeliveryStatus(DeliveryStatus.PENDING);
            delivery.setRouteStrategyEnum(RouteStrategy.fromStrategy(request.getRouteStrategy()));
            
            // 3. 生成路径规划（如果需要）
            if (Boolean.TRUE.equals(request.getGenerateRoute())) {
                RouteResult routeResult = generateDeliveryRoute(request);
                
                if (routeResult.getSuccess()) {
                    populateDeliveryFromRoute(delivery, routeResult);
                    log.info("配送路径规划成功，算法: {}, 距离: {}米", 
                            routeResult.getAlgorithmUsed(), routeResult.getTotalDistance());
                } else {
                    log.warn("配送路径规划失败: {}", routeResult.getMessage());
                    // 可以选择抛出异常或者创建不带路径的配送单
                    delivery.setOptimalRoute("{}");
                    delivery.setDistance(java.math.BigDecimal.ZERO);
                    delivery.setAlgorithmUsed("none");
                }
            } else {
                // 暂不生成路径
                delivery.setOptimalRoute("{}");
                delivery.setDistance(java.math.BigDecimal.ZERO);
                delivery.setAlgorithmUsed("pending");
            }
            
            // 4. 保存配送单
            delivery = deliveryRepository.save(delivery);
            
            log.info("配送单创建成功，ID: {}", delivery.getDeliveryId());
            return delivery;
            
        } catch (Exception e) {
            log.error("创建配送单失败", e);
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "创建配送单失败: " + e.getMessage());
        }
    }

    /**
     * 生成配送路径
     */
    private RouteResult generateDeliveryRoute(CreateDeliveryRequest request) {
        // 1. 获取起点（仓库地址）
        WarehouseConfig warehouse = warehouseConfigService.getDefaultWarehouse();
        GeoPoint origin = new GeoPoint(warehouse.getLatitude(), warehouse.getLongitude());
        origin.setAddress(warehouse.getAddress());
        
        // 2. 获取配送点（通过Feign调用OrderService）
        List<GeoPoint> waypoints = getDeliveryWaypoints(request.getDispatchGroup());
        
        // 3. 构建路径规划请求
        RouteRequest routeRequest = new RouteRequest();
        routeRequest.setDispatchGroup(request.getDispatchGroup());
        routeRequest.setLeaderId(request.getLeaderId());
        routeRequest.setOrigin(origin);
        routeRequest.setWaypoints(waypoints);
        routeRequest.setRouteStrategy(request.getRouteStrategy());
        
        // 4. 执行路径规划
        return routeService.planOptimalRoute(routeRequest);
    }

    /**
     * 获取配送点列表（通过Feign调用获取真实数据）
     */
    private List<GeoPoint> getDeliveryWaypoints(String dispatchGroup) {
        try {
            // 1. 通过分单组获取订单列表
            com.bcu.edu.common.result.Result<List<OrderInfoDTO>> orderResult = 
                    orderServiceClient.getOrdersByDispatchGroup(dispatchGroup);
            
            if (!orderResult.isSuccess() || orderResult.getData() == null) {
                log.warn("获取分单组{}的订单列表失败: {}", dispatchGroup, orderResult.getMessage());
                return getDefaultWaypoints();
            }
            
            // 2. 提取收货地址ID列表
            List<Long> addressIds = orderResult.getData().stream()
                    .map(OrderInfoDTO::getReceiveAddressId)
                    .filter(addressId -> addressId != null)
                    .distinct()
                    .toList();
            
            if (addressIds.isEmpty()) {
                log.warn("分单组{}没有有效的收货地址", dispatchGroup);
                return getDefaultWaypoints();
            }
            
            // 3. 批量获取地址坐标信息
            com.bcu.edu.common.result.Result<List<AddressWithCoordinatesDTO>> addressResult = 
                    userServiceClient.getAddressesWithCoordinates(addressIds);
            
            if (!addressResult.isSuccess() || addressResult.getData() == null) {
                log.warn("获取地址坐标信息失败: {}", addressResult.getMessage());
                return getDefaultWaypoints();
            }
            
            // 4. 转换为GeoPoint列表
            List<GeoPoint> waypoints = addressResult.getData().stream()
                    .filter(AddressWithCoordinatesDTO::hasCoordinates)
                    .map(AddressWithCoordinatesDTO::toGeoPoint)
                    .toList();
            
            if (waypoints.isEmpty()) {
                log.warn("没有找到有效的配送点坐标");
                return getDefaultWaypoints();
            }
            
            log.info("成功获取{}个配送点", waypoints.size());
            return waypoints;
            
        } catch (Exception e) {
            log.error("获取配送点列表失败，使用默认数据", e);
            return getDefaultWaypoints();
        }
    }

    /**
     * 获取默认配送点（降级数据）
     */
    private List<GeoPoint> getDefaultWaypoints() {
        List<GeoPoint> waypoints = new java.util.ArrayList<>();
        
        // 示例配送点
        GeoPoint point1 = new GeoPoint(
                new java.math.BigDecimal("39.925963"), 
                new java.math.BigDecimal("116.404"));
        point1.setAddress("北京市东城区示例地址1");
        waypoints.add(point1);
        
        GeoPoint point2 = new GeoPoint(
                new java.math.BigDecimal("39.913818"), 
                new java.math.BigDecimal("116.363618"));
        point2.setAddress("北京市西城区示例地址2");
        waypoints.add(point2);
        
        log.warn("使用默认配送点数据，请检查OrderService和UserService连接");
        return waypoints;
    }

    /**
     * 从路径规划结果填充配送单信息
     */
    private void populateDeliveryFromRoute(Delivery delivery, RouteResult routeResult) {
        delivery.setOptimalRoute(routeResult.getRoutePath());
        delivery.setDistance(routeResult.getTotalDistance());
        delivery.setEstimatedDuration(routeResult.getEstimatedDuration());
        delivery.setRouteDisplayData(routeResult.getMapDisplayData());
        delivery.setAlgorithmUsed(routeResult.getAlgorithmUsed());
    }

    /**
     * 根据ID查询配送单
     */
    public Delivery getDeliveryById(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "配送单不存在: " + deliveryId));
    }

    /**
     * 根据分单组查询配送单
     */
    public Delivery getDeliveryByDispatchGroup(String dispatchGroup) {
        return deliveryRepository.findByDispatchGroup(dispatchGroup)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "分单组对应的配送单不存在"));
    }

    /**
     * 查询团长的配送单列表
     */
    public List<Delivery> getDeliveriesByLeaderId(Long leaderId) {
        return deliveryRepository.findByLeaderIdOrderByCreateTimeDesc(leaderId);
    }

    /**
     * 查询团长指定状态的配送单
     */
    public List<Delivery> getDeliveriesByLeaderAndStatus(Long leaderId, DeliveryStatus status) {
        return deliveryRepository.findByLeaderIdAndStatusOrderByCreateTimeDesc(leaderId, status.getCode());
    }

    /**
     * 更新配送状态
     * 
     * <p>状态流转规则：
     * <ul>
     *   <li>待分配 → 配送中：记录实际开始时间</li>
     *   <li>配送中 → 已完成：记录完成时间</li>
     * </ul>
     */
    @OperationLog(value = "更新配送状态", module = "配送管理")
    public Delivery updateDeliveryStatus(Long deliveryId, DeliveryStatus newStatus) {
        log.info("更新配送状态，ID: {}, 新状态: {}", deliveryId, newStatus.getDescription());
        
        Delivery delivery = getDeliveryById(deliveryId);
        DeliveryStatus currentStatus = delivery.getDeliveryStatus();
        
        // 验证状态流转合法性
        validateStatusTransition(currentStatus, newStatus);
        
        // 更新状态和相关时间
        delivery.setDeliveryStatus(newStatus);
        switch (newStatus) {
            case DELIVERING:
                if (delivery.getActualStartTime() == null) {
                    delivery.setActualStartTime(LocalDateTime.now());
                }
                break;
            case COMPLETED:
                if (delivery.getEndTime() == null) {
                    delivery.setEndTime(LocalDateTime.now());
                }
                break;
        }
        
        delivery = deliveryRepository.save(delivery);
        log.info("配送状态更新成功，当前状态: {}", newStatus.getDescription());
        
        return delivery;
    }

    /**
     * 验证状态流转合法性
     */
    private void validateStatusTransition(DeliveryStatus from, DeliveryStatus to) {
        boolean isValidTransition = switch (from) {
            case PENDING -> to == DeliveryStatus.DELIVERING;
            case DELIVERING -> to == DeliveryStatus.COMPLETED;
            case COMPLETED -> false; // 已完成状态不允许再变更
        };
        
        if (!isValidTransition) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, 
                    String.format("非法的状态流转: %s → %s", from.getDescription(), to.getDescription()));
        }
    }

    /**
     * 开始配送
     */
    @OperationLog(value = "开始配送", module = "配送管理")
    public Delivery startDelivery(Long deliveryId) {
        return updateDeliveryStatus(deliveryId, DeliveryStatus.DELIVERING);
    }

    /**
     * 完成配送
     */
    @OperationLog(value = "完成配送", module = "配送管理")
    public Delivery completeDelivery(Long deliveryId) {
        return updateDeliveryStatus(deliveryId, DeliveryStatus.COMPLETED);
    }

    /**
     * 重新生成配送路径
     */
    @OperationLog(value = "重新生成路径", module = "配送管理")
    public Delivery regenerateRoute(Long deliveryId, String routeStrategy) {
        log.info("重新生成配送路径，ID: {}, 策略: {}", deliveryId, routeStrategy);
        
        Delivery delivery = getDeliveryById(deliveryId);
        
        // 只有待分配状态的配送单才能重新生成路径
        if (delivery.getDeliveryStatus() != DeliveryStatus.PENDING) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有待分配状态的配送单才能重新生成路径");
        }
        
        try {
            // 构建路径规划请求
            CreateDeliveryRequest request = new CreateDeliveryRequest();
            request.setDispatchGroup(delivery.getDispatchGroup());
            request.setLeaderId(delivery.getLeaderId());
            request.setRouteStrategy(routeStrategy);
            
            // 生成新路径
            RouteResult routeResult = generateDeliveryRoute(request);
            
            if (routeResult.getSuccess()) {
                // 更新配送单路径信息
                populateDeliveryFromRoute(delivery, routeResult);
                delivery.setRouteStrategyEnum(RouteStrategy.fromStrategy(routeStrategy));
                
                delivery = deliveryRepository.save(delivery);
                log.info("配送路径重新生成成功，算法: {}, 距离: {}米", 
                        routeResult.getAlgorithmUsed(), routeResult.getTotalDistance());
            } else {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "路径规划失败: " + routeResult.getMessage());
            }
            
        } catch (Exception e) {
            log.error("重新生成配送路径失败", e);
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "重新生成路径失败: " + e.getMessage());
        }
        
        return delivery;
    }

    /**
     * 删除配送单
     */
    @OperationLog(value = "删除配送单", module = "配送管理")
    public void deleteDelivery(Long deliveryId) {
        log.info("删除配送单，ID: {}", deliveryId);
        
        Delivery delivery = getDeliveryById(deliveryId);
        
        // 只有待分配状态的配送单才能删除
        if (delivery.getDeliveryStatus() != DeliveryStatus.PENDING) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "只有待分配状态的配送单才能删除");
        }
        
        deliveryRepository.delete(delivery);
        log.info("配送单删除成功");
    }

    /**
     * 获取配送统计信息
     */
    public DeliveryStatistics getDeliveryStatistics(Long leaderId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Delivery> deliveries;
        
        if (leaderId != null) {
            deliveries = deliveryRepository.findByLeaderIdAndCreateTimeBetween(leaderId, startDate, endDate);
        } else {
            deliveries = deliveryRepository.findByCreateTimeBetween(startDate, endDate);
        }
        
        return calculateStatistics(deliveries);
    }

    /**
     * 计算统计信息
     */
    private DeliveryStatistics calculateStatistics(List<Delivery> deliveries) {
        DeliveryStatistics stats = new DeliveryStatistics();
        
        stats.setTotalDeliveries(deliveries.size());
        stats.setPendingCount((int) deliveries.stream().filter(d -> d.getDeliveryStatus() == DeliveryStatus.PENDING).count());
        stats.setDeliveringCount((int) deliveries.stream().filter(d -> d.getDeliveryStatus() == DeliveryStatus.DELIVERING).count());
        stats.setCompletedCount((int) deliveries.stream().filter(d -> d.getDeliveryStatus() == DeliveryStatus.COMPLETED).count());
        
        // 计算平均距离
        double avgDistance = deliveries.stream()
                .mapToDouble(d -> d.getDistance().doubleValue())
                .average()
                .orElse(0.0);
        stats.setAverageDistance(java.math.BigDecimal.valueOf(avgDistance).setScale(2, java.math.RoundingMode.HALF_UP));
        
        // 计算平均时间
        double avgDuration = deliveries.stream()
                .filter(d -> d.getEstimatedDuration() != null)
                .mapToInt(d -> d.getEstimatedDuration())
                .average()
                .orElse(0.0);
        stats.setAverageDuration((int) Math.round(avgDuration));
        
        // 算法使用统计
        long dijkstraCount = deliveries.stream().filter(d -> "dijkstra".equals(d.getAlgorithmUsed())).count();
        long gaodeCount = deliveries.stream().filter(d -> "gaode".equals(d.getAlgorithmUsed())).count();
        
        stats.setDijkstraUsageCount((int) dijkstraCount);
        stats.setGaodeUsageCount((int) gaodeCount);
        
        return stats;
    }

    /**
     * 配送统计信息
     */
    public static class DeliveryStatistics {
        private Integer totalDeliveries = 0;
        private Integer pendingCount = 0;
        private Integer deliveringCount = 0;
        private Integer completedCount = 0;
        private java.math.BigDecimal averageDistance = java.math.BigDecimal.ZERO;
        private Integer averageDuration = 0;
        private Integer dijkstraUsageCount = 0;
        private Integer gaodeUsageCount = 0;

        // Getters and Setters
        public Integer getTotalDeliveries() { return totalDeliveries; }
        public void setTotalDeliveries(Integer totalDeliveries) { this.totalDeliveries = totalDeliveries; }

        public Integer getPendingCount() { return pendingCount; }
        public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }

        public Integer getDeliveringCount() { return deliveringCount; }
        public void setDeliveringCount(Integer deliveringCount) { this.deliveringCount = deliveringCount; }

        public Integer getCompletedCount() { return completedCount; }
        public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

        public java.math.BigDecimal getAverageDistance() { return averageDistance; }
        public void setAverageDistance(java.math.BigDecimal averageDistance) { this.averageDistance = averageDistance; }

        public Integer getAverageDuration() { return averageDuration; }
        public void setAverageDuration(Integer averageDuration) { this.averageDuration = averageDuration; }

        public Integer getDijkstraUsageCount() { return dijkstraUsageCount; }
        public void setDijkstraUsageCount(Integer dijkstraUsageCount) { this.dijkstraUsageCount = dijkstraUsageCount; }

        public Integer getGaodeUsageCount() { return gaodeUsageCount; }
        public void setGaodeUsageCount(Integer gaodeUsageCount) { this.gaodeUsageCount = gaodeUsageCount; }
    }
}
