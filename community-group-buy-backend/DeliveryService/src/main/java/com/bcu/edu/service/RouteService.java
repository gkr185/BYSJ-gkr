package com.bcu.edu.service;

import com.bcu.edu.dto.RouteRequest;
import com.bcu.edu.dto.RouteResult;
import com.bcu.edu.service.algorithm.DijkstraRouteService;
import com.bcu.edu.service.algorithm.GaodeRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 路径规划服务
 * 
 * <p>集成双引擎路径规划，智能选择最优算法
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@Service
public class RouteService {

    @Value("${delivery.route.max-waypoints:30}")
    private int maxWaypoints;

    @Value("${delivery.route.enable-dijkstra-fallback:true}")
    private boolean enableDijkstraFallback;

    @Value("${delivery.route.default-strategy:shortest-time}")
    private String defaultStrategy;

    @Autowired
    private GaodeRouteService gaodeRouteService;

    @Autowired
    private DijkstraRouteService dijkstraRouteService;

    /**
     * 计算最优路径（主入口）
     * 
     * <p>策略：
     * <ol>
     *   <li>优先使用高德地图API（如果可用且未强制使用Dijkstra）</li>
     *   <li>高德API失败时，自动降级到Dijkstra算法（如果启用降级）</li>
     *   <li>返回最终的路径规划结果</li>
     * </ol>
     */
    public RouteResult planOptimalRoute(RouteRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始双引擎路径规划，分单组: {}, 配送点: {}, 策略: {}", 
                    request.getDispatchGroup(), request.getWaypointCount(), request.getRouteStrategy());
            
            // 1. 参数验证
            validateRequest(request);
            
            // 2. 设置默认策略
            if (request.getRouteStrategy() == null || request.getRouteStrategy().trim().isEmpty()) {
                request.setRouteStrategy(defaultStrategy);
            }
            
            // 3. 选择算法引擎
            RouteResult result = selectAndExecuteAlgorithm(request);
            
            // 4. 记录执行结果
            logExecutionResult(result, System.currentTimeMillis() - startTime);
            
            return result;
            
        } catch (IllegalArgumentException e) {
            log.warn("路径规划参数错误: {}", e.getMessage());
            return RouteResult.error("validation", e.getMessage());
        } catch (Exception e) {
            log.error("路径规划失败", e);
            return RouteResult.error("system", "系统错误: " + e.getMessage());
        }
    }

    /**
     * 验证请求参数
     */
    private void validateRequest(RouteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("路径规划请求不能为空");
        }
        
        if (request.getOrigin() == null) {
            throw new IllegalArgumentException("起点坐标不能为空");
        }
        
        if (request.getWaypoints() == null || request.getWaypoints().isEmpty()) {
            throw new IllegalArgumentException("配送点列表不能为空");
        }
        
        if (request.isExceedMaxWaypoints(maxWaypoints)) {
            throw new IllegalArgumentException(
                String.format("配送点数量(%d)超过最大限制(%d)", request.getWaypointCount(), maxWaypoints));
        }
        
        // 验证坐标完整性
        request.validate(maxWaypoints);
    }

    /**
     * 选择并执行算法引擎
     */
    private RouteResult selectAndExecuteAlgorithm(RouteRequest request) {
        // 强制使用Dijkstra算法
        if (Boolean.FALSE.equals(request.getForceGaodeApi()) && enableDijkstraFallback) {
            log.info("强制使用Dijkstra算法进行路径规划");
            return dijkstraRouteService.calculateOptimalRoute(request);
        }
        
        // 优先尝试高德API
        if (gaodeRouteService.isApiAvailable()) {
            log.info("使用高德地图API进行路径规划");
            RouteResult gaodeResult = gaodeRouteService.calculateOptimalRoute(request);
            
            if (gaodeResult.getSuccess()) {
                return gaodeResult;
            } else {
                log.warn("高德API路径规划失败: {}", gaodeResult.getMessage());
            }
        } else {
            log.warn("高德地图API不可用，可能是API Key未配置或网络问题");
        }
        
        // 降级到Dijkstra算法
        if (enableDijkstraFallback && Boolean.TRUE.equals(request.getEnableDijkstraFallback())) {
            log.info("降级使用Dijkstra算法进行路径规划");
            return dijkstraRouteService.calculateOptimalRoute(request);
        }
        
        // 所有引擎都不可用
        return RouteResult.error("unavailable", "所有路径规划引擎都不可用");
    }

    /**
     * 记录执行结果
     */
    private void logExecutionResult(RouteResult result, long totalTime) {
        if (result.getSuccess()) {
            log.info("路径规划成功 - 算法: {}, 总距离: {}米, 预估时间: {}分钟, 总耗时: {}ms",
                    result.getAlgorithmUsed(),
                    result.getTotalDistance(),
                    result.getEstimatedDuration(),
                    totalTime);
        } else {
            log.error("路径规划失败 - 算法: {}, 错误: {}, 总耗时: {}ms",
                    result.getAlgorithmUsed(),
                    result.getMessage(),
                    totalTime);
        }
    }

    /**
     * 获取算法引擎状态
     */
    public AlgorithmStatus getAlgorithmStatus() {
        AlgorithmStatus status = new AlgorithmStatus();
        status.setGaodeApiAvailable(gaodeRouteService.isApiAvailable());
        status.setDijkstraEnabled(enableDijkstraFallback);
        status.setMaxWaypoints(maxWaypoints);
        status.setDefaultStrategy(defaultStrategy);
        return status;
    }

    /**
     * 算法状态信息
     */
    public static class AlgorithmStatus {
        private Boolean gaodeApiAvailable;
        private Boolean dijkstraEnabled;
        private Integer maxWaypoints;
        private String defaultStrategy;

        // Getters and Setters
        public Boolean getGaodeApiAvailable() {
            return gaodeApiAvailable;
        }

        public void setGaodeApiAvailable(Boolean gaodeApiAvailable) {
            this.gaodeApiAvailable = gaodeApiAvailable;
        }

        public Boolean getDijkstraEnabled() {
            return dijkstraEnabled;
        }

        public void setDijkstraEnabled(Boolean dijkstraEnabled) {
            this.dijkstraEnabled = dijkstraEnabled;
        }

        public Integer getMaxWaypoints() {
            return maxWaypoints;
        }

        public void setMaxWaypoints(Integer maxWaypoints) {
            this.maxWaypoints = maxWaypoints;
        }

        public String getDefaultStrategy() {
            return defaultStrategy;
        }

        public void setDefaultStrategy(String defaultStrategy) {
            this.defaultStrategy = defaultStrategy;
        }
    }

    /**
     * 测试路径规划算法
     * 
     * <p>用于验证算法可用性和性能
     */
    public RouteResult testAlgorithm(String algorithm) {
        // 构建测试请求
        RouteRequest testRequest = new RouteRequest();
        testRequest.setDispatchGroup("TEST_GROUP");
        testRequest.setLeaderId(1L);
        
        // 测试坐标（北京市中心区域）
        testRequest.setOrigin(new com.bcu.edu.dto.GeoPoint(
                new java.math.BigDecimal("39.916527"), 
                new java.math.BigDecimal("116.397128"))); // 天安门
        
        java.util.List<com.bcu.edu.dto.GeoPoint> waypoints = new java.util.ArrayList<>();
        waypoints.add(new com.bcu.edu.dto.GeoPoint(
                new java.math.BigDecimal("39.925963"), 
                new java.math.BigDecimal("116.404"))); // 故宫
        waypoints.add(new com.bcu.edu.dto.GeoPoint(
                new java.math.BigDecimal("39.913818"), 
                new java.math.BigDecimal("116.363618"))); // 西单
        
        testRequest.setWaypoints(waypoints);
        testRequest.setRouteStrategy("shortest-time");
        
        // 根据指定算法执行测试
        switch (algorithm.toLowerCase()) {
            case "gaode":
                testRequest.setForceGaodeApi(true);
                testRequest.setEnableDijkstraFallback(false);
                break;
            case "dijkstra":
                testRequest.setForceGaodeApi(false);
                testRequest.setEnableDijkstraFallback(true);
                break;
            default:
                testRequest.setForceGaodeApi(null);
                testRequest.setEnableDijkstraFallback(true);
        }
        
        return planOptimalRoute(testRequest);
    }
}
