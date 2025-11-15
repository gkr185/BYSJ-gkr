package com.bcu.edu.algorithm;

import com.bcu.edu.dto.RouteRequest;
import com.bcu.edu.dto.RouteResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Dijkstra算法实现 - TSP问题求解（旅行商问题）
 * 
 * <p>算法说明：
 * <ul>
 *   <li>输入：起点、途经点列表、终点（可选）</li>
 *   <li>算法：最近邻贪心算法（Nearest Neighbor Algorithm）</li>
 *   <li>时间复杂度：O(n²)，n为途经点数量</li>
 *   <li>空间复杂度：O(n²)，距离矩阵</li>
 * </ul>
 * 
 * <p>适用场景：
 * <ul>
 *   <li>途经点数量：≤30个（性能考虑）</li>
 *   <li>优化目标：最短距离</li>
 *   <li>约束条件：必须经过所有途经点</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@Component
public class DijkstraAlgorithm {

    /**
     * 地球半径（千米）
     */
    private static final double EARTH_RADIUS = 6371.0;

    /**
     * 平均速度（公里/小时）- 用于预估配送时间
     */
    private static final double AVERAGE_SPEED = 30.0;

    /**
     * 最大途经点数量限制
     */
    private static final int MAX_WAYPOINTS = 30;

    /**
     * 计算最优路径
     * 
     * @param request 路径规划请求
     * @return 路径规划结果
     */
    public RouteResult calculateRoute(RouteRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 参数校验
        validateRequest(request);

        // 2. 构建距离矩阵
        double[][] distanceMatrix = buildDistanceMatrix(request);

        // 3. TSP求解（贪心算法）
        List<Integer> pathSequence = solveTSP(distanceMatrix, request.getEnd() != null);

        // 4. 计算路径详细信息
        RouteResult result = buildRouteResult(request, pathSequence, distanceMatrix);

        long endTime = System.currentTimeMillis();
        log.info("Dijkstra算法计算完成，途经点数量={}，耗时={}ms", 
                request.getWaypoints().size(), endTime - startTime);

        return result;
    }

    /**
     * 校验请求参数
     */
    private void validateRequest(RouteRequest request) {
        if (request.getStart() == null) {
            throw new IllegalArgumentException("起点不能为空");
        }
        if (request.getWaypoints() == null || request.getWaypoints().isEmpty()) {
            throw new IllegalArgumentException("途经点列表不能为空");
        }
        if (request.getWaypoints().size() > MAX_WAYPOINTS) {
            throw new IllegalArgumentException(
                    String.format("途经点数量不能超过%d个，当前%d个", 
                            MAX_WAYPOINTS, request.getWaypoints().size())
            );
        }
    }

    /**
     * 构建距离矩阵
     * 
     * <p>矩阵说明：
     * <ul>
     *   <li>索引0: 起点</li>
     *   <li>索引1~n: 途经点</li>
     *   <li>索引n+1: 终点（如果有）</li>
     * </ul>
     * 
     * @param request 路径规划请求
     * @return 距离矩阵（单位：米）
     */
    private double[][] buildDistanceMatrix(RouteRequest request) {
        // 构建所有点的列表
        List<RouteRequest.Coordinate> allPoints = new ArrayList<>();
        allPoints.add(request.getStart()); // 索引0: 起点
        allPoints.addAll(request.getWaypoints()); // 索引1~n: 途经点
        if (request.getEnd() != null) {
            allPoints.add(request.getEnd()); // 索引n+1: 终点
        }

        int size = allPoints.size();
        double[][] matrix = new double[size][size];

        // 计算任意两点间的距离（Haversine公式）
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                } else {
                    matrix[i][j] = calculateDistance(
                            allPoints.get(i).getLatitude().doubleValue(),
                            allPoints.get(i).getLongitude().doubleValue(),
                            allPoints.get(j).getLatitude().doubleValue(),
                            allPoints.get(j).getLongitude().doubleValue()
                    );
                }
            }
        }

        log.debug("距离矩阵构建完成，矩阵大小={}x{}", size, size);
        return matrix;
    }

    /**
     * Haversine公式计算两点间球面距离
     * 
     * <p>公式说明：
     * <pre>
     * a = sin²(Δφ/2) + cos(φ1) * cos(φ2) * sin²(Δλ/2)
     * c = 2 * atan2(√a, √(1−a))
     * d = R * c
     * </pre>
     * 
     * <p>其中：
     * <ul>
     *   <li>φ 表示纬度（latitude）</li>
     *   <li>λ 表示经度（longitude）</li>
     *   <li>R 表示地球半径（6371公里）</li>
     * </ul>
     * 
     * @param lat1 点1纬度
     * @param lon1 点1经度
     * @param lat2 点2纬度
     * @param lon2 点2经度
     * @return 两点间距离（米）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 计算差值
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Haversine公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 距离（米）
        return EARTH_RADIUS * c * 1000;
    }

    /**
     * TSP求解 - 最近邻贪心算法
     * 
     * <p>算法步骤：
     * <ol>
     *   <li>从起点开始（索引0）</li>
     *   <li>每次选择距离当前点最近的未访问点</li>
     *   <li>标记该点为已访问</li>
     *   <li>重复步骤2-3，直到所有点都被访问</li>
     *   <li>如果有终点，最后访问终点</li>
     * </ol>
     * 
     * @param distanceMatrix 距离矩阵
     * @param hasEnd 是否有终点
     * @return 路径序列（点的索引顺序）
     */
    private List<Integer> solveTSP(double[][] distanceMatrix, boolean hasEnd) {
        int n = distanceMatrix.length;
        List<Integer> path = new ArrayList<>();
        boolean[] visited = new boolean[n];

        // 起点（索引0）
        int current = 0;
        path.add(current);
        visited[current] = true;

        // 如果有终点，终点索引为n-1，不参与贪心选择
        int endIndex = hasEnd ? n - 1 : -1;
        int targetCount = hasEnd ? n - 1 : n;

        // 贪心选择：每次选择最近的未访问点
        for (int i = 1; i < targetCount; i++) {
            double minDistance = Double.MAX_VALUE;
            int nearest = -1;

            for (int j = 1; j < n; j++) {
                // 跳过已访问点和终点
                if (visited[j] || j == endIndex) {
                    continue;
                }

                double distance = distanceMatrix[current][j];
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = j;
                }
            }

            if (nearest != -1) {
                current = nearest;
                path.add(current);
                visited[current] = true;
            }
        }

        // 如果有终点，最后访问终点
        if (hasEnd) {
            path.add(endIndex);
        }

        log.debug("TSP求解完成，路径序列={}", path);
        return path;
    }

    /**
     * 构建路径结果
     * 
     * @param request 原始请求
     * @param pathSequence 路径序列
     * @param distanceMatrix 距离矩阵
     * @return 路径结果
     */
    private RouteResult buildRouteResult(RouteRequest request, 
                                          List<Integer> pathSequence, 
                                          double[][] distanceMatrix) {
        RouteResult result = new RouteResult();
        result.setPathSequence(pathSequence);
        result.setAlgorithmUsed("dijkstra");

        // 计算总距离和路段信息
        BigDecimal totalDistance = BigDecimal.ZERO;
        List<RouteResult.SegmentInfo> segments = new ArrayList<>();

        for (int i = 0; i < pathSequence.size() - 1; i++) {
            int fromIndex = pathSequence.get(i);
            int toIndex = pathSequence.get(i + 1);
            double segmentDistance = distanceMatrix[fromIndex][toIndex];

            totalDistance = totalDistance.add(
                    BigDecimal.valueOf(segmentDistance).setScale(2, RoundingMode.HALF_UP)
            );

            // 添加路段信息
            RouteResult.SegmentInfo segment = new RouteResult.SegmentInfo();
            segment.setFromIndex(fromIndex);
            segment.setToIndex(toIndex);
            segment.setDistance(BigDecimal.valueOf(segmentDistance).setScale(2, RoundingMode.HALF_UP));
            segment.setDuration(estimateDuration(segmentDistance));
            segments.add(segment);
        }

        result.setTotalDistance(totalDistance);
        result.setSegments(segments);

        // 预估总时间（分钟）
        double totalKm = totalDistance.doubleValue() / 1000.0;
        int estimatedDuration = (int) Math.ceil((totalKm / AVERAGE_SPEED) * 60);
        result.setEstimatedDuration(estimatedDuration);

        // 生成最优路径字符串（用于数据库存储）
        result.setOptimalRoute(buildOptimalRouteString(request, pathSequence));

        log.info("路径规划结果：总距离={}米，预估时间={}分钟", 
                totalDistance, estimatedDuration);

        return result;
    }

    /**
     * 预估路段时间（分钟）
     * 
     * @param distance 距离（米）
     * @return 预估时间（分钟）
     */
    private Integer estimateDuration(double distance) {
        double km = distance / 1000.0;
        return (int) Math.ceil((km / AVERAGE_SPEED) * 60);
    }

    /**
     * 构建最优路径字符串
     * 格式：lat1,lng1;lat2,lng2;lat3,lng3;...
     * 
     * @param request 原始请求
     * @param pathSequence 路径序列
     * @return 路径字符串
     */
    private String buildOptimalRouteString(RouteRequest request, List<Integer> pathSequence) {
        // 构建所有点的列表
        List<RouteRequest.Coordinate> allPoints = new ArrayList<>();
        allPoints.add(request.getStart());
        allPoints.addAll(request.getWaypoints());
        if (request.getEnd() != null) {
            allPoints.add(request.getEnd());
        }

        StringBuilder route = new StringBuilder();
        for (Integer index : pathSequence) {
            RouteRequest.Coordinate point = allPoints.get(index);
            if (route.length() > 0) {
                route.append(";");
            }
            route.append(point.getLatitude())
                    .append(",")
                    .append(point.getLongitude());
        }

        return route.toString();
    }
}

