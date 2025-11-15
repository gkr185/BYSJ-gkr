package com.bcu.edu.service;

import com.bcu.edu.dto.StatisticsOverviewDTO;
import com.bcu.edu.entity.DeliveryEntity;
import com.bcu.edu.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配送统计服务
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final DeliveryRepository deliveryRepository;

    /**
     * 获取配送总览统计
     */
    public StatisticsOverviewDTO getOverview() {
        StatisticsOverviewDTO overview = new StatisticsOverviewDTO();

        // 1. 统计配送单数量（按状态）
        Long totalDeliveries = deliveryRepository.count();
        Long pendingDeliveries = deliveryRepository.countByStatus(0);
        Long shippingDeliveries = deliveryRepository.countByStatus(1);
        Long completedDeliveries = deliveryRepository.countByStatus(2);

        overview.setTotalDeliveries(totalDeliveries);
        overview.setPendingDeliveries(pendingDeliveries);
        overview.setShippingDeliveries(shippingDeliveries);
        overview.setCompletedDeliveries(completedDeliveries);

        // 2. 统计配送距离
        Double totalDistanceDouble = deliveryRepository.sumDistanceByCompleted();
        BigDecimal totalDistance = totalDistanceDouble != null ? 
                BigDecimal.valueOf(totalDistanceDouble).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        overview.setTotalDistance(totalDistance);

        // 3. 计算平均配送距离
        if (completedDeliveries > 0 && totalDistanceDouble != null) {
            BigDecimal averageDistance = totalDistance.divide(
                    BigDecimal.valueOf(completedDeliveries), 2, RoundingMode.HALF_UP
            );
            overview.setAverageDistance(averageDistance);
        } else {
            overview.setAverageDistance(BigDecimal.ZERO);
        }

        // 4. 统计平均途经点数量
        List<DeliveryEntity> allDeliveries = deliveryRepository.findAll();
        if (!allDeliveries.isEmpty()) {
            double avgWaypoints = allDeliveries.stream()
                    .mapToInt(DeliveryEntity::getWaypointCount)
                    .average()
                    .orElse(0.0);
            overview.setAverageWaypointCount(avgWaypoints);
        }

        log.info("配送总览统计完成，总配送单={}, 已完成={}, 总距离={}米",
                totalDeliveries, completedDeliveries, totalDistance);

        return overview;
    }

    /**
     * 获取距离统计（按日期范围）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 距离统计数据
     */
    public Map<String, Object> getDistanceStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<DeliveryEntity> deliveries = deliveryRepository.findByCreateTimeBetween(startTime, endTime);

        Map<String, Object> statistics = new HashMap<>();
        
        // 总配送单数
        statistics.put("deliveryCount", deliveries.size());
        
        // 总距离
        BigDecimal totalDistance = deliveries.stream()
                .map(DeliveryEntity::getDistance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.put("totalDistance", totalDistance);

        // 平均距离
        if (!deliveries.isEmpty()) {
            BigDecimal averageDistance = totalDistance.divide(
                    BigDecimal.valueOf(deliveries.size()), 2, RoundingMode.HALF_UP
            );
            statistics.put("averageDistance", averageDistance);
        } else {
            statistics.put("averageDistance", BigDecimal.ZERO);
        }

        // 最长距离
        BigDecimal maxDistance = deliveries.stream()
                .map(DeliveryEntity::getDistance)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        statistics.put("maxDistance", maxDistance);

        // 最短距离
        BigDecimal minDistance = deliveries.stream()
                .map(DeliveryEntity::getDistance)
                .filter(d -> d.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        statistics.put("minDistance", minDistance);

        return statistics;
    }

    /**
     * 获取团长配送统计
     * 
     * @return 团长配送统计列表
     */
    public List<Map<String, Object>> getLeaderStatistics() {
        List<Object[]> results = deliveryRepository.countGroupByLeaderId();

        return results.stream()
                .map(row -> {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("leaderId", row[0]);
                    stat.put("deliveryCount", row[1]);
                    return stat;
                })
                .toList();
    }

    /**
     * 获取配送效率统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 效率统计数据
     */
    public Map<String, Object> getEfficiencyStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<DeliveryEntity> deliveries = deliveryRepository.findByCreateTimeBetween(startTime, endTime);

        Map<String, Object> statistics = new HashMap<>();

        // 统计完成率
        long completedCount = deliveries.stream().filter(d -> d.getStatus() == 2).count();
        double completionRate = deliveries.isEmpty() ? 0.0 : 
                (double) completedCount / deliveries.size() * 100;
        statistics.put("completionRate", BigDecimal.valueOf(completionRate).setScale(2, RoundingMode.HALF_UP));

        // 平均预估时间
        double avgEstimatedDuration = deliveries.stream()
                .filter(d -> d.getEstimatedDuration() != null)
                .mapToInt(DeliveryEntity::getEstimatedDuration)
                .average()
                .orElse(0.0);
        statistics.put("averageEstimatedDuration", Math.round(avgEstimatedDuration));

        // 平均途经点数量
        double avgWaypointCount = deliveries.stream()
                .mapToInt(DeliveryEntity::getWaypointCount)
                .average()
                .orElse(0.0);
        statistics.put("averageWaypointCount", BigDecimal.valueOf(avgWaypointCount).setScale(1, RoundingMode.HALF_UP));

        return statistics;
    }
}

