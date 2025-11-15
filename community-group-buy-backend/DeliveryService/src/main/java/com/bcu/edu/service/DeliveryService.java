package com.bcu.edu.service;

import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.dto.DeliveryDetailDTO;
import com.bcu.edu.dto.RouteRequest;
import com.bcu.edu.dto.RouteResult;
import com.bcu.edu.dto.WaypointInfo;
import com.bcu.edu.entity.DeliveryEntity;
import com.bcu.edu.entity.WarehouseConfig;
import com.bcu.edu.feign.OrderServiceClient;
import com.bcu.edu.repository.DeliveryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配送管理服务
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final WarehouseService warehouseService;
    private final RouteService routeService;
    private final OrderServiceClient orderServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * 分页查询配送单列表
     */
    public Page<DeliveryEntity> listDeliveries(Integer status, Pageable pageable) {
        if (status != null) {
            return deliveryRepository.findByStatus(status, pageable);
        }
        return deliveryRepository.findAllByOrderByCreateTimeDesc(pageable);
    }

    /**
     * 查询配送单详情
     */
    public DeliveryDetailDTO getDeliveryDetail(Long deliveryId) {
        // 1. 查询配送单
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException("配送单不存在，ID=" + deliveryId));

        // 2. 构建详情DTO
        DeliveryDetailDTO detail = new DeliveryDetailDTO();
        detail.setDeliveryId(delivery.getDeliveryId());
        detail.setDispatchGroup(delivery.getDispatchGroup());
        detail.setDeliveryMode(delivery.getDeliveryMode());
        detail.setStatus(delivery.getStatus());
        detail.setTotalDistance(delivery.getDistance());
        detail.setEstimatedDuration(delivery.getEstimatedDuration());
        detail.setAlgorithmUsed(delivery.getAlgorithmUsed());
        detail.setRouteStrategy(delivery.getRouteStrategy());
        detail.setStartTime(delivery.getStartTime());
        detail.setEndTime(delivery.getEndTime());
        detail.setCreateTime(delivery.getCreateTime());

        // 3. 解析途经点信息
        try {
            List<WaypointInfo> waypoints = objectMapper.readValue(
                    delivery.getWaypointsData(),
                    new TypeReference<List<WaypointInfo>>() {}
            );
            detail.setWaypoints(waypoints);
        } catch (JsonProcessingException e) {
            log.error("解析途经点数据失败", e);
        }

        // 4. 解析订单ID列表并查询订单信息
        try {
            List<Long> orderIds = objectMapper.readValue(
                    delivery.getOrderIds(),
                    new TypeReference<List<Long>>() {}
            );
            // Feign调用OrderService查询订单
            var ordersResult = orderServiceClient.batchQueryOrders(orderIds);
            if (ordersResult.getCode() == 200) {
                detail.setOrders(ordersResult.getData());
            }
        } catch (JsonProcessingException e) {
            log.error("解析订单ID列表失败", e);
        }

        // 5. 查询仓库信息
        if (delivery.getWarehouseId() != null) {
            WarehouseConfig warehouse = warehouseService.getWarehouseById(delivery.getWarehouseId());
            DeliveryDetailDTO.WarehouseInfo warehouseInfo = new DeliveryDetailDTO.WarehouseInfo();
            warehouseInfo.setId(warehouse.getId());
            warehouseInfo.setWarehouseName(warehouse.getWarehouseName());
            warehouseInfo.setAddress(warehouse.getAddress());
            warehouseInfo.setLongitude(warehouse.getLongitude());
            warehouseInfo.setLatitude(warehouse.getLatitude());
            detail.setStartWarehouse(warehouseInfo);
        }

        return detail;
    }

    /**
     * 重新规划路径
     * 
     * @param deliveryId 配送单ID
     * @return 新的路径结果
     */
    @Transactional(rollbackFor = Exception.class)
    public RouteResult replanRoute(Long deliveryId) {
        log.info("开始重新规划路径，deliveryId={}", deliveryId);

        // 1. 查询配送单
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException("配送单不存在，ID=" + deliveryId));

        // 2. 校验状态（只有配送中的才能重新规划）
        if (delivery.getStatus() == 2) {
            throw new BusinessException("配送已完成，无法重新规划");
        }

        // 3. 解析途经点数据
        List<WaypointInfo> waypoints;
        try {
            waypoints = objectMapper.readValue(
                    delivery.getWaypointsData(),
                    new TypeReference<List<WaypointInfo>>() {}
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException("途经点数据解析失败");
        }

        // 4. 获取仓库坐标
        WarehouseConfig warehouse = warehouseService.getWarehouseById(delivery.getWarehouseId());
        RouteRequest.Coordinate start = new RouteRequest.Coordinate(
                warehouse.getLongitude(),
                warehouse.getLatitude(),
                warehouse.getId(),
                warehouse.getWarehouseName()
        );

        // 5. 构建路径规划请求
        RouteRequest request = new RouteRequest();
        request.setStart(start);
        request.setWaypoints(routeService.extractCoordinates(waypoints));
        request.setStrategy(delivery.getRouteStrategy());

        // 如果有终点
        if (delivery.getEndWarehouseId() != null) {
            WarehouseConfig endWarehouse = warehouseService.getWarehouseById(delivery.getEndWarehouseId());
            RouteRequest.Coordinate end = new RouteRequest.Coordinate(
                    endWarehouse.getLongitude(),
                    endWarehouse.getLatitude(),
                    endWarehouse.getId(),
                    endWarehouse.getWarehouseName()
            );
            request.setEnd(end);
        }

        // 6. 重新规划路径
        RouteResult result = routeService.planRoute(request);

        // 7. 更新配送单路径信息
        delivery.setOptimalRoute(result.getOptimalRoute());
        delivery.setDistance(result.getTotalDistance());
        delivery.setEstimatedDuration(result.getEstimatedDuration());
        delivery.setRouteDisplayData(routeService.generateMapDisplayData(result, waypoints));

        deliveryRepository.save(delivery);

        log.info("路径重新规划完成，deliveryId={}, 新距离={}米", deliveryId, result.getTotalDistance());

        return result;
    }

    /**
     * 手动完成配送
     * 
     * @param deliveryId 配送单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void completeDelivery(Long deliveryId) {
        log.info("开始手动完成配送，deliveryId={}", deliveryId);

        // 1. 查询配送单
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException("配送单不存在，ID=" + deliveryId));

        // 2. 校验状态
        if (delivery.getStatus() == 2) {
            throw new BusinessException("配送已完成，无需重复操作");
        }

        // 3. 更新配送单状态
        delivery.setStatus(2); // 已完成
        delivery.setEndTime(LocalDateTime.now());
        deliveryRepository.save(delivery);

        // 4. 批量更新订单状态为"已送达"（Feign调用OrderService）
        try {
            List<Long> orderIds = objectMapper.readValue(
                    delivery.getOrderIds(),
                    new TypeReference<List<Long>>() {}
            );

            var result = orderServiceClient.batchUpdateToDelivered(orderIds);
            if (result.getCode() != 200) {
                throw new BusinessException("更新订单状态失败：" + result.getMessage());
            }

            log.info("配送完成，deliveryId={}, 订单数量={}", deliveryId, orderIds.size());
        } catch (JsonProcessingException e) {
            throw new BusinessException("订单ID列表解析失败");
        }
    }

    /**
     * 取消配送
     * 
     * @param deliveryId 配送单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelDelivery(Long deliveryId) {
        log.info("开始取消配送，deliveryId={}", deliveryId);

        // 1. 查询配送单
        DeliveryEntity delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new BusinessException("配送单不存在，ID=" + deliveryId));

        // 2. 校验状态
        if (delivery.getStatus() == 2) {
            throw new BusinessException("配送已完成，无法取消");
        }

        // 3. 删除配送单（或标记为取消）
        // TODO: 如果需要保留历史记录，可以添加status=3（已取消）
        deliveryRepository.deleteById(deliveryId);

        log.info("配送取消成功，deliveryId={}", deliveryId);
    }
}

