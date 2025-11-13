package com.bcu.edu.service.algorithm;

import com.bcu.edu.dto.GeoPoint;
import com.bcu.edu.dto.RouteRequest;
import com.bcu.edu.dto.RouteResult;
import com.bcu.edu.enums.RouteStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Dijkstra算法路径规划服务
 * 
 * <p>基于图论的最短路径算法，用作高德API的降级方案
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@Service
public class DijkstraRouteService {

    /**
     * 使用Dijkstra算法计算最优路径
     */
    public RouteResult calculateOptimalRoute(RouteRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始Dijkstra算法路径规划，分单组: {}, 配送点数量: {}", 
                    request.getDispatchGroup(), request.getWaypointCount());
            
            // 1. 构建图结构
            List<GeoPoint> allPoints = buildPointList(request);
            double[][] distanceMatrix = buildDistanceMatrix(allPoints);
            
            // 2. 执行TSP近似算法
            List<Integer> optimalPath = solveTSP(distanceMatrix);
            
            // 3. 构建结果
            RouteResult result = buildRouteResult(request, allPoints, optimalPath, distanceMatrix, startTime);
            
            log.info("Dijkstra算法路径规划完成，总距离: {}米, 耗时: {}ms", 
                    result.getTotalDistance(), result.getApiCallInfo().getDuration());
            
            return result;
            
        } catch (Exception e) {
            log.error("Dijkstra算法路径规划失败", e);
            return RouteResult.error("dijkstra", "Dijkstra算法计算失败: " + e.getMessage());
        }
    }

    /**
     * 构建点列表（起点 + 配送点）
     */
    private List<GeoPoint> buildPointList(RouteRequest request) {
        List<GeoPoint> allPoints = new ArrayList<>();
        allPoints.add(request.getOrigin()); // 起点（仓库）
        allPoints.addAll(request.getWaypoints()); // 配送点
        return allPoints;
    }

    /**
     * 构建距离矩阵
     */
    private double[][] buildDistanceMatrix(List<GeoPoint> points) {
        int n = points.size();
        double[][] matrix = new double[n][n];
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else {
                    matrix[i][j] = points.get(i).distanceTo(points.get(j));
                }
            }
        }
        
        return matrix;
    }

    /**
     * 解决旅行商问题（TSP）- 使用贪心算法的近似解
     * 
     * <p>由于标准TSP是NP-hard问题，这里使用贪心算法获得近似解：
     * <ol>
     *   <li>从起点(0)开始</li>
     *   <li>每次选择距离当前点最近且未访问的点</li>
     *   <li>最后返回起点</li>
     * </ol>
     */
    private List<Integer> solveTSP(double[][] distanceMatrix) {
        int n = distanceMatrix.length;
        List<Integer> path = new ArrayList<>();
        boolean[] visited = new boolean[n];
        
        // 从起点开始
        int current = 0;
        path.add(current);
        visited[current] = true;
        
        // 贪心选择最近的未访问点
        for (int i = 1; i < n; i++) {
            int nearest = -1;
            double minDistance = Double.MAX_VALUE;
            
            for (int j = 1; j < n; j++) { // 从1开始，跳过起点
                if (!visited[j] && distanceMatrix[current][j] < minDistance) {
                    minDistance = distanceMatrix[current][j];
                    nearest = j;
                }
            }
            
            if (nearest != -1) {
                path.add(nearest);
                visited[nearest] = true;
                current = nearest;
            }
        }
        
        return path;
    }

    /**
     * 构建路径规划结果
     */
    private RouteResult buildRouteResult(RouteRequest request, List<GeoPoint> allPoints, 
                                       List<Integer> optimalPath, double[][] distanceMatrix, long startTime) {
        
        RouteResult result = RouteResult.success("dijkstra");
        result.setRouteStrategy(RouteStrategy.fromStrategy(request.getRouteStrategy()));
        
        // 计算总距离和路径点
        BigDecimal totalDistance = BigDecimal.ZERO;
        List<RouteResult.RoutePoint> routePoints = new ArrayList<>();
        List<String> pathCoordinates = new ArrayList<>();
        
        for (int i = 0; i < optimalPath.size(); i++) {
            int pointIndex = optimalPath.get(i);
            GeoPoint geoPoint = allPoints.get(pointIndex);
            
            // 构建路径点
            RouteResult.RoutePoint routePoint = new RouteResult.RoutePoint();
            routePoint.setSequence(i + 1);
            routePoint.setGeoPoint(geoPoint);
            
            if (pointIndex > 0) { // 配送点才有addressId
                routePoint.setAddressId(geoPoint.getAddressId());
            }
            
            // 计算距离
            if (i > 0) {
                int prevIndex = optimalPath.get(i - 1);
                double distance = distanceMatrix[prevIndex][pointIndex];
                routePoint.setDistanceFromPrevious(BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP));
                totalDistance = totalDistance.add(routePoint.getDistanceFromPrevious());
            } else {
                routePoint.setDistanceFromPrevious(BigDecimal.ZERO);
            }
            
            routePoints.add(routePoint);
            pathCoordinates.add(geoPoint.toCoordinateString());
        }
        
        result.setOptimizedWaypoints(routePoints);
        result.setTotalDistance(totalDistance.setScale(2, RoundingMode.HALF_UP));
        result.setRoutePath(String.join(";", pathCoordinates));
        
        // 估算配送时间（假设平均速度30km/h）
        double avgSpeedKmh = 30.0;
        double timeHours = totalDistance.doubleValue() / 1000.0 / avgSpeedKmh;
        result.setEstimatedDuration((int) Math.ceil(timeHours * 60)); // 转换为分钟
        
        // 构建地图展示数据
        result.setMapDisplayData(buildMapDisplayData(routePoints, pathCoordinates));
        
        // API调用信息
        RouteResult.ApiCallInfo apiInfo = new RouteResult.ApiCallInfo();
        apiInfo.setDuration(System.currentTimeMillis() - startTime);
        apiInfo.setFromCache(false);
        apiInfo.setResponseCode(200);
        apiInfo.setRetryCount(0);
        result.setApiCallInfo(apiInfo);
        
        return result;
    }

    /**
     * 构建地图展示数据
     */
    private String buildMapDisplayData(List<RouteResult.RoutePoint> routePoints, List<String> pathCoordinates) {
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("algorithm", "dijkstra");
        mapData.put("routeType", "straightLine");
        mapData.put("coordinates", pathCoordinates);
        mapData.put("waypoints", routePoints.size());
        
        // 简单JSON格式（实际项目中应使用Jackson）
        return String.format(
            "{\"algorithm\":\"dijkstra\",\"routeType\":\"straightLine\",\"coordinates\":[%s],\"waypoints\":%d}",
            "\"" + String.join("\",\"", pathCoordinates) + "\"",
            routePoints.size()
        );
    }

    /**
     * 优化算法 - 使用2-opt改进TSP解
     * 
     * <p>2-opt算法通过交换路径中的边来优化解：
     * <ol>
     *   <li>选择两条边进行交换</li>
     *   <li>如果交换后路径变短，则接受交换</li>
     *   <li>重复直到无法改进</li>
     * </ol>
     */
    public List<Integer> optimizeWith2Opt(List<Integer> path, double[][] distanceMatrix) {
        List<Integer> bestPath = new ArrayList<>(path);
        boolean improved = true;
        
        while (improved) {
            improved = false;
            for (int i = 1; i < bestPath.size() - 2; i++) {
                for (int j = i + 1; j < bestPath.size() - 1; j++) {
                    if (j - i == 1) continue; // 跳过相邻的边
                    
                    List<Integer> newPath = swap2Opt(bestPath, i, j);
                    if (calculatePathDistance(newPath, distanceMatrix) < calculatePathDistance(bestPath, distanceMatrix)) {
                        bestPath = newPath;
                        improved = true;
                    }
                }
            }
        }
        
        return bestPath;
    }

    /**
     * 2-opt交换
     */
    private List<Integer> swap2Opt(List<Integer> path, int i, int j) {
        List<Integer> newPath = new ArrayList<>();
        
        // 添加前部分
        newPath.addAll(path.subList(0, i));
        
        // 反转中间部分
        List<Integer> middle = new ArrayList<>(path.subList(i, j + 1));
        Collections.reverse(middle);
        newPath.addAll(middle);
        
        // 添加后部分
        newPath.addAll(path.subList(j + 1, path.size()));
        
        return newPath;
    }

    /**
     * 计算路径总距离
     */
    private double calculatePathDistance(List<Integer> path, double[][] distanceMatrix) {
        double totalDistance = 0.0;
        for (int i = 1; i < path.size(); i++) {
            totalDistance += distanceMatrix[path.get(i - 1)][path.get(i)];
        }
        return totalDistance;
    }
}
