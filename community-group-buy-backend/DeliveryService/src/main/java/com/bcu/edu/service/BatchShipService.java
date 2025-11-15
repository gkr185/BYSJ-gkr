package com.bcu.edu.service;

import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.dto.*;
import com.bcu.edu.entity.DeliveryEntity;
import com.bcu.edu.entity.WarehouseConfig;
import com.bcu.edu.feign.LeaderServiceClient;
import com.bcu.edu.feign.OrderServiceClient;
import com.bcu.edu.feign.UserServiceClient;
import com.bcu.edu.repository.DeliveryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 批量发货服务（核心业务逻辑）
 * 
 * <p>功能：
 * <ul>
 *   <li>批量发货处理</li>
 *   <li>订单验证与分组</li>
 *   <li>途经点提取（两种模式）</li>
 *   <li>路径规划调用</li>
 *   <li>配送单创建</li>
 *   <li>订单状态批量更新</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchShipService {

    private final DeliveryRepository deliveryRepository;
    private final WarehouseService warehouseService;
    private final RouteService routeService;
    private final OrderServiceClient orderServiceClient;
    private final UserServiceClient userServiceClient;
    private final LeaderServiceClient leaderServiceClient;
    private final ObjectMapper objectMapper;

    /**
     * 批量发货（核心方法）⭐⭐⭐⭐⭐
     * 
     * @param request 批量发货请求
     * @return 批量发货响应
     */
    @Transactional(rollbackFor = Exception.class)
    public BatchShipResponse batchShip(BatchShipRequest request) {
        log.info("开始批量发货，订单数量={}, 发货方式={}, 仓库ID={}", 
                request.getOrderIds().size(), request.getDeliveryMode(), request.getWarehouseId());

        // 1. 验证订单状态（必须为"待发货"）
        List<OrderInfoDTO> orders = validateOrders(request.getOrderIds());

        // 2. 生成分单组标识
        String dispatchGroup = generateDispatchGroup();

        // 3. 获取起点仓库坐标
        WarehouseConfig startWarehouse = warehouseService.getWarehouseById(request.getWarehouseId());
        RouteRequest.Coordinate start = new RouteRequest.Coordinate(
                startWarehouse.getLongitude(),
                startWarehouse.getLatitude(),
                startWarehouse.getId(),
                startWarehouse.getWarehouseName()
        );

        // 4. 提取途经点（根据发货方式）
        List<WaypointInfo> waypoints = extractWaypoints(orders, request.getDeliveryMode());

        // 5. 构建路径规划请求
        RouteRequest routeRequest = new RouteRequest();
        routeRequest.setStart(start);
        routeRequest.setWaypoints(routeService.extractCoordinates(waypoints));
        routeRequest.setStrategy(request.getRouteStrategy());

        // 如果有终点仓库
        if (request.getEndWarehouseId() != null) {
            WarehouseConfig endWarehouse = warehouseService.getWarehouseById(request.getEndWarehouseId());
            RouteRequest.Coordinate end = new RouteRequest.Coordinate(
                    endWarehouse.getLongitude(),
                    endWarehouse.getLatitude(),
                    endWarehouse.getId(),
                    endWarehouse.getWarehouseName()
            );
            routeRequest.setEnd(end);
        }

        // 6. 调用路径规划算法
        RouteResult routeResult = routeService.planRoute(routeRequest);

        // 7. 按路径序列重新排序途经点
        List<WaypointInfo> sortedWaypoints = reorderWaypoints(waypoints, routeResult.getPathSequence());

        // 8. 创建配送单（传入orders用于提取leaderId）
        DeliveryEntity delivery = createDelivery(
                request, dispatchGroup, startWarehouse, routeResult, sortedWaypoints, orders
        );

        // 9. 批量更新订单状态为"配送中"（Feign调用OrderService）
        updateOrdersToShipping(request.getOrderIds(), delivery.getDeliveryId(), dispatchGroup);

        // 10. 构建响应
        BatchShipResponse response = buildResponse(delivery, sortedWaypoints, routeResult);

        log.info("批量发货成功，deliveryId={}, 订单数量={}, 总距离={}米",
                delivery.getDeliveryId(), request.getOrderIds().size(), delivery.getDistance());

        return response;
    }

    /**
     * 验证订单状态
     * 
     * @param orderIds 订单ID列表
     * @return 订单信息列表
     */
    private List<OrderInfoDTO> validateOrders(List<Long> orderIds) {
        // Feign调用OrderService批量查询订单
        var result = orderServiceClient.batchQueryOrders(orderIds);
        if (result.getCode() != 200) {
            throw new BusinessException("查询订单失败：" + result.getMessage());
        }

        List<OrderInfoDTO> orders = result.getData();

        // 校验订单状态（必须为1-待发货）
        List<Long> invalidOrders = orders.stream()
                .filter(order -> order.getOrderStatus() != 1)
                .map(OrderInfoDTO::getOrderId)
                .collect(Collectors.toList());

        if (!invalidOrders.isEmpty()) {
            throw new BusinessException("以下订单状态不是待发货，无法发货：" + invalidOrders);
        }

        // 校验订单支付状态（必须已支付）
        List<Long> unpaidOrders = orders.stream()
                .filter(order -> order.getPayStatus() != 1)
                .map(OrderInfoDTO::getOrderId)
                .collect(Collectors.toList());

        if (!unpaidOrders.isEmpty()) {
            throw new BusinessException("以下订单未支付，无法发货：" + unpaidOrders);
        }

        log.info("订单验证通过，待发货订单数量={}", orders.size());
        return orders;
    }

    /**
     * 生成分单组标识
     * 格式：SHIP + yyyyMMddHHmmss + 3位随机数
     */
    private String generateDispatchGroup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(900) + 100; // 100-999
        return "SHIP" + timestamp + random;
    }

    /**
     * 提取途经点（根据发货方式）⭐⭐⭐⭐⭐
     * 
     * @param orders 订单列表
     * @param deliveryMode 发货方式（1-团长团点；2-用户地址）
     * @return 途经点信息列表
     */
    private List<WaypointInfo> extractWaypoints(List<OrderInfoDTO> orders, Integer deliveryMode) {
        log.info("开始提取途经点，发货方式={}", deliveryMode);

        List<WaypointInfo> waypoints = new ArrayList<>();

        if (deliveryMode == 1) {
            // 方式1：团长团点模式
            waypoints = extractLeaderStoreWaypoints(orders);
        } else if (deliveryMode == 2) {
            // 方式2：用户地址模式
            waypoints = extractUserAddressWaypoints(orders);
        } else {
            throw new BusinessException("不支持的发货方式：" + deliveryMode);
        }

        log.info("途经点提取完成，数量={}", waypoints.size());
        return waypoints;
    }

    /**
     * 提取团长团点途经点
     */
    private List<WaypointInfo> extractLeaderStoreWaypoints(List<OrderInfoDTO> orders) {
        // 1. 提取所有团长ID（去重）
        List<Long> leaderIds = orders.stream()
                .map(OrderInfoDTO::getLeaderId)
                .distinct()
                .collect(Collectors.toList());

        log.info("团长团点模式，去重后团长数量={}", leaderIds.size());

        // 2. Feign调用LeaderService批量查询团长团点信息
        var result = leaderServiceClient.batchGetLeaderStores(leaderIds);
        if (result.getCode() != 200) {
            throw new BusinessException("查询团长团点失败：" + result.getMessage());
        }

        List<com.bcu.edu.entity.GroupLeaderStore> leaderStores = result.getData();

        // 3. 构建途经点信息
        List<WaypointInfo> waypoints = new ArrayList<>();
        int sequence = 0;
        for (com.bcu.edu.entity.GroupLeaderStore store : leaderStores) {
            // 验证坐标不为空
            if (store.getLongitude() == null || store.getLatitude() == null) {
                log.warn("团长团点缺少坐标，storeId={}, 跳过", store.getStoreId());
                continue;
            }

            WaypointInfo waypoint = new WaypointInfo();
            waypoint.setSequence(sequence++);
            waypoint.setOrderId(null); // 团长团点模式，途经点不关联具体订单
            // 拼接完整地址
            String fullAddress = (store.getProvince() != null ? store.getProvince() : "") +
                                 (store.getCity() != null ? store.getCity() : "") +
                                 (store.getDistrict() != null ? store.getDistrict() : "") +
                                 (store.getDetailAddress() != null ? store.getDetailAddress() : "");
            waypoint.setAddress(fullAddress);
            waypoint.setLongitude(store.getLongitude());
            waypoint.setLatitude(store.getLatitude());
            waypoint.setReceiverName(store.getLeaderName());
            waypoint.setReceiverPhone(store.getLeaderPhone());
            waypoint.setType("leader_store");

            waypoints.add(waypoint);
        }

        return waypoints;
    }

    /**
     * 提取用户地址途经点
     */
    private List<WaypointInfo> extractUserAddressWaypoints(List<OrderInfoDTO> orders) {
        // 1. 提取所有收货地址ID
        List<Long> addressIds = orders.stream()
                .map(OrderInfoDTO::getReceiveAddressId)
                .distinct()
                .collect(Collectors.toList());

        log.info("用户地址模式，去重后地址数量={}", addressIds.size());

        // 2. Feign调用UserService批量查询地址信息
        var result = userServiceClient.batchGetAddresses(addressIds);
        if (result.getCode() != 200) {
            throw new BusinessException("查询用户地址失败：" + result.getMessage());
        }

        List<AddressDTO> addresses = result.getData();

        // 3. 构建地址ID到地址信息的映射
        Map<Long, AddressDTO> addressMap = addresses.stream()
                .collect(Collectors.toMap(AddressDTO::getAddressId, addr -> addr));

        // 4. 构建途经点信息（保留订单顺序）
        List<WaypointInfo> waypoints = new ArrayList<>();
        int sequence = 0;
        for (OrderInfoDTO order : orders) {
            AddressDTO address = addressMap.get(order.getReceiveAddressId());
            if (address == null) {
                log.warn("订单收货地址不存在，orderId={}, 跳过", order.getOrderId());
                continue;
            }

            // 验证坐标不为空
            if (address.getLongitude() == null || address.getLatitude() == null) {
                log.warn("收货地址缺少坐标，addressId={}, 跳过", address.getAddressId());
                continue;
            }

            WaypointInfo waypoint = new WaypointInfo();
            waypoint.setSequence(sequence++);
            waypoint.setOrderId(order.getOrderId());
            waypoint.setAddress(address.getFullAddress());
            waypoint.setLongitude(address.getLongitude());
            waypoint.setLatitude(address.getLatitude());
            waypoint.setReceiverName(address.getReceiver());
            waypoint.setReceiverPhone(address.getPhone());
            waypoint.setType("user_address");

            waypoints.add(waypoint);
        }

        return waypoints;
    }

    /**
     * 按路径序列重新排序途经点
     * 
     * @param waypoints 原始途经点列表
     * @param pathSequence 路径序列（索引从1开始，索引0是起点）
     * @return 排序后的途经点列表
     */
    private List<WaypointInfo> reorderWaypoints(List<WaypointInfo> waypoints, List<Integer> pathSequence) {
        List<WaypointInfo> sorted = new ArrayList<>();

        // pathSequence: [0, 3, 1, 2, 4]
        // 索引0是起点，索引1-n是途经点，索引n+1是终点（如果有）
        for (int i = 1; i < pathSequence.size(); i++) {
            int waypointIndex = pathSequence.get(i) - 1; // 减1转换为waypoints索引
            
            // 跳过终点（终点不在waypoints中）
            if (waypointIndex >= waypoints.size()) {
                break;
            }

            WaypointInfo waypoint = waypoints.get(waypointIndex);
            waypoint.setSequence(i - 1); // 重新设置序号
            sorted.add(waypoint);
        }

        return sorted;
    }

    /**
     * 创建配送单
     */
    private DeliveryEntity createDelivery(BatchShipRequest request,
                                           String dispatchGroup,
                                           WarehouseConfig startWarehouse,
                                           RouteResult routeResult,
                                           List<WaypointInfo> waypoints,
                                           List<OrderInfoDTO> orders) {
        DeliveryEntity delivery = new DeliveryEntity();

        // 基本信息
        delivery.setDispatchGroup(dispatchGroup);
        delivery.setDeliveryMode(request.getDeliveryMode());
        delivery.setWarehouseId(request.getWarehouseId());
        delivery.setEndWarehouseId(request.getEndWarehouseId());
        delivery.setWaypointCount(waypoints.size());
        delivery.setCreatedBy(request.getCreatedBy());

        // 设置负责团长ID（从订单中提取第一个团长ID）⭐⭐⭐
        // 所有订单都有团长（拼团订单或普通订单自动分配），配送单也必须有负责团长
        if (!orders.isEmpty()) {
            Long leaderId = orders.get(0).getLeaderId();
            delivery.setLeaderId(leaderId);
            log.info("设置配送单负责团长: leaderId={}", leaderId);
        } else {
            throw new BusinessException("订单列表为空，无法确定负责团长");
        }

        // 订单ID列表（JSON）
        try {
            delivery.setOrderIds(objectMapper.writeValueAsString(request.getOrderIds()));
        } catch (JsonProcessingException e) {
            throw new BusinessException("订单ID列表序列化失败");
        }

        // 途经点数据（JSON）
        try {
            delivery.setWaypointsData(objectMapper.writeValueAsString(waypoints));
        } catch (JsonProcessingException e) {
            throw new BusinessException("途经点数据序列化失败");
        }

        // 路径规划结果
        delivery.setOptimalRoute(routeResult.getOptimalRoute());
        delivery.setDistance(routeResult.getTotalDistance());
        delivery.setEstimatedDuration(routeResult.getEstimatedDuration());
        delivery.setAlgorithmUsed(routeResult.getAlgorithmUsed());
        delivery.setRouteStrategy(request.getRouteStrategy());

        // 生成地图展示数据
        String mapDisplayData = routeService.generateMapDisplayData(routeResult, waypoints);
        delivery.setRouteDisplayData(mapDisplayData);

        // 配送状态
        delivery.setStatus(1); // 1-配送中
        delivery.setStartTime(LocalDateTime.now());

        // 保存配送单
        DeliveryEntity saved = deliveryRepository.save(delivery);
        log.info("配送单创建成功，deliveryId={}, dispatchGroup={}", 
                saved.getDeliveryId(), dispatchGroup);

        return saved;
    }

    /**
     * 批量更新订单状态为"配送中"
     */
    private void updateOrdersToShipping(List<Long> orderIds, Long deliveryId, String dispatchGroup) {
        log.info("开始批量更新订单状态，orderIds={}, deliveryId={}", orderIds, deliveryId);

        // Feign调用OrderService批量更新
        var result = orderServiceClient.batchUpdateToShipping(orderIds, deliveryId, dispatchGroup);

        if (result.getCode() != 200) {
            throw new BusinessException("更新订单状态失败：" + result.getMessage());
        }

        log.info("订单状态更新成功，更新数量={}", result.getData());
    }

    /**
     * 构建批量发货响应
     */
    private BatchShipResponse buildResponse(DeliveryEntity delivery,
                                              List<WaypointInfo> waypoints,
                                              RouteResult routeResult) {
        BatchShipResponse response = new BatchShipResponse();

        response.setDeliveryId(delivery.getDeliveryId());
        response.setDispatchGroup(delivery.getDispatchGroup());
        
        // 解析订单ID列表
        try {
            List<Long> orderIds = objectMapper.readValue(
                    delivery.getOrderIds(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {}
            );
            response.setOrderIds(orderIds);
        } catch (JsonProcessingException e) {
            log.error("解析订单ID列表失败", e);
        }

        response.setDeliveryMode(delivery.getDeliveryMode());
        response.setWaypointCount(waypoints.size());
        response.setTotalDistance(delivery.getDistance());
        response.setEstimatedDuration(delivery.getEstimatedDuration());
        response.setWaypoints(waypoints);
        response.setAlgorithmUsed(delivery.getAlgorithmUsed());
        response.setMessage("批量发货成功，已生成配送单");

        return response;
    }
}

