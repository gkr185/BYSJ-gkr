package com.bcu.edu.service;

import com.bcu.edu.algorithm.DijkstraAlgorithm;
import com.bcu.edu.dto.RouteRequest;
import com.bcu.edu.dto.RouteResult;
import com.bcu.edu.dto.WaypointInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 路径规划服务
 * 
 * <p>功能：
 * <ul>
 *   <li>调用Dijkstra算法计算最优路径</li>
 *   <li>生成地图展示数据</li>
 *   <li>路径优化与调整</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final DijkstraAlgorithm dijkstraAlgorithm;

    /**
     * 最大途经点数量限制
     */
    private static final int MAX_WAYPOINTS = 30;

    /**
     * 规划路径（使用Dijkstra算法）
     * 
     * @param request 路径规划请求
     * @return 路径规划结果
     */
    public RouteResult planRoute(RouteRequest request) {
        log.info("开始路径规划，起点={}, 途经点数量={}, 有终点={}", 
                request.getStart(), 
                request.getWaypoints().size(),
                request.getEnd() != null);

        // 1. 校验途经点数量
        validateWaypointCount(request.getWaypoints().size());

        // 2. 调用Dijkstra算法
        RouteResult result = dijkstraAlgorithm.calculateRoute(request);

        log.info("路径规划完成，总距离={}米，预估时间={}分钟", 
                result.getTotalDistance(), result.getEstimatedDuration());

        return result;
    }

    /**
     * 校验途经点数量
     */
    private void validateWaypointCount(int count) {
        if (count == 0) {
            throw new IllegalArgumentException("途经点列表不能为空");
        }
        if (count > MAX_WAYPOINTS) {
            throw new IllegalArgumentException(
                    String.format("途经点数量不能超过%d个，当前%d个，建议分批发货", 
                            MAX_WAYPOINTS, count)
            );
        }
    }

    /**
     * 根据路径结果生成地图展示数据
     * 
     * @param result 路径结果
     * @param waypoints 途经点信息列表
     * @return 地图展示数据（JSON字符串）
     */
    public String generateMapDisplayData(RouteResult result, List<WaypointInfo> waypoints) {
        // 构建地图展示数据（JSON格式）
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // 路径序列
        json.append("\"pathSequence\":").append(result.getPathSequence()).append(",");
        
        // 总距离
        json.append("\"totalDistance\":").append(result.getTotalDistance()).append(",");
        
        // 预估时间
        json.append("\"estimatedDuration\":").append(result.getEstimatedDuration()).append(",");
        
        // 途经点标记
        json.append("\"waypoints\":[");
        for (int i = 0; i < waypoints.size(); i++) {
            WaypointInfo wp = waypoints.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                    .append("\"sequence\":").append(wp.getSequence()).append(",")
                    .append("\"orderId\":").append(wp.getOrderId()).append(",")
                    .append("\"address\":\"").append(wp.getAddress()).append("\",")
                    .append("\"longitude\":").append(wp.getLongitude()).append(",")
                    .append("\"latitude\":").append(wp.getLatitude())
                    .append("}");
        }
        json.append("]}");

        return json.toString();
    }

    /**
     * 提取途经点坐标列表
     * 
     * @param waypoints 途经点信息列表
     * @return 坐标列表
     */
    public List<RouteRequest.Coordinate> extractCoordinates(List<WaypointInfo> waypoints) {
        return waypoints.stream()
                .map(wp -> new RouteRequest.Coordinate(
                        wp.getLongitude(),
                        wp.getLatitude(),
                        wp.getOrderId(),
                        wp.getAddress()
                ))
                .toList();
    }
}

