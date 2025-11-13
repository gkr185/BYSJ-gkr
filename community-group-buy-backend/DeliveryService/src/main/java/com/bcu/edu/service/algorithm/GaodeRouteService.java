package com.bcu.edu.service.algorithm;

import com.bcu.edu.dto.GeoPoint;
import com.bcu.edu.dto.RouteRequest;
import com.bcu.edu.dto.RouteResult;
import com.bcu.edu.enums.RouteStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高德地图API路径规划服务
 * 
 * <p>集成高德地图API实现实时路径规划
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@Service
public class GaodeRouteService {

    @Value("${gaode.api.key}")
    private String apiKey;

    @Value("${gaode.api.base-url}")
    private String baseUrl;

    @Value("${gaode.api.timeout:5000}")
    private int timeout;

    @Value("${gaode.api.retry-count:3}")
    private int retryCount;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GaodeRouteService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 使用高德API计算最优路径
     */
    public RouteResult calculateOptimalRoute(RouteRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("开始高德API路径规划，分单组: {}, 配送点数量: {}", 
                    request.getDispatchGroup(), request.getWaypointCount());
            
            // 1. 验证API Key
            if (apiKey == null || apiKey.trim().isEmpty() || "your_api_key_here".equals(apiKey)) {
                log.warn("高德API Key未配置，无法使用高德API服务");
                return RouteResult.error("gaode", "高德API Key未配置");
            }
            
            // 2. 构建请求参数
            Map<String, String> params = buildApiParams(request);
            
            // 3. 调用高德API
            String responseBody = callGaodeApiWithRetry(params);
            
            // 4. 解析响应
            RouteResult result = parseApiResponse(responseBody, request, startTime);
            
            log.info("高德API路径规划完成，总距离: {}米, 耗时: {}ms", 
                    result.getTotalDistance(), result.getApiCallInfo().getDuration());
            
            return result;
            
        } catch (Exception e) {
            log.error("高德API路径规划失败", e);
            return RouteResult.error("gaode", "高德API调用失败: " + e.getMessage());
        }
    }

    /**
     * 构建高德API请求参数
     */
    private Map<String, String> buildApiParams(RouteRequest request) {
        Map<String, String> params = new HashMap<>();
        
        // 基础参数
        params.put("key", apiKey);
        params.put("origin", request.getOrigin().toCoordinateString());
        
        // 目的地（第一个配送点）
        if (!request.getWaypoints().isEmpty()) {
            params.put("destination", request.getWaypoints().get(0).toCoordinateString());
        }
        
        // 途经点（其他配送点）
        if (request.getWaypoints().size() > 1) {
            List<String> waypoints = new ArrayList<>();
            for (int i = 1; i < request.getWaypoints().size(); i++) {
                waypoints.add(request.getWaypoints().get(i).toCoordinateString());
            }
            params.put("waypoints", String.join(";", waypoints));
        }
        
        // 路径策略
        String strategy = convertRouteStrategy(request.getRouteStrategy());
        params.put("strategy", strategy);
        
        // 其他参数
        params.put("extensions", "all");  // 返回详细信息
        params.put("output", "json");     // JSON格式返回
        params.put("show_fields", "cost"); // 显示费用信息
        
        return params;
    }

    /**
     * 转换路径策略为高德API参数
     */
    private String convertRouteStrategy(String strategy) {
        return switch (strategy) {
            case "shortest-time" -> "0";      // 最快捷路线
            case "shortest-distance" -> "1";  // 最短距离
            case "avoid-congestion" -> "2";   // 避免拥堵
            default -> "0";
        };
    }

    /**
     * 带重试的高德API调用
     */
    private String callGaodeApiWithRetry(Map<String, String> params) {
        String url = buildApiUrl(params);
        
        Mono<String> apiCall = webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(httpStatus -> httpStatus.isError(), response -> {
                    log.warn("高德API返回错误状态: {}", response.statusCode());
                    return Mono.error(new RuntimeException("API调用失败，状态码: " + response.statusCode()));
                })
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeout));
        
        return apiCall
                .retryWhen(Retry.fixedDelay(retryCount, Duration.ofMillis(1000))
                        .doBeforeRetry(retrySignal -> 
                                log.warn("高德API调用失败，正在重试第{}次", retrySignal.totalRetries() + 1)))
                .block();
    }

    /**
     * 构建API URL
     */
    private String buildApiUrl(Map<String, String> params) {
        StringBuilder url = new StringBuilder(baseUrl + "/direction/driving");
        url.append("?");
        
        params.forEach((key, value) -> {
            url.append(key).append("=").append(value).append("&");
        });
        
        // 移除最后的&
        if (url.length() > 0) {
            url.setLength(url.length() - 1);
        }
        
        return url.toString();
    }

    /**
     * 解析高德API响应
     */
    private RouteResult parseApiResponse(String responseBody, RouteRequest request, long startTime) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // 检查API返回状态
            String status = root.path("status").asText();
            if (!"1".equals(status)) {
                String info = root.path("info").asText();
                log.warn("高德API返回错误: status={}, info={}", status, info);
                return RouteResult.error("gaode", "高德API错误: " + info);
            }
            
            // 解析路径信息
            JsonNode route = root.path("route");
            JsonNode paths = route.path("paths");
            
            if (paths.isEmpty()) {
                return RouteResult.error("gaode", "未找到有效路径");
            }
            
            // 取第一条路径（最优路径）
            JsonNode firstPath = paths.get(0);
            
            RouteResult result = RouteResult.success("gaode");
            result.setRouteStrategy(RouteStrategy.fromStrategy(request.getRouteStrategy()));
            
            // 解析距离和时间
            int distance = firstPath.path("distance").asInt(); // 米
            int duration = firstPath.path("duration").asInt(); // 秒
            
            result.setTotalDistance(BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP));
            result.setEstimatedDuration(duration / 60); // 转换为分钟
            
            // 解析路径坐标
            JsonNode steps = firstPath.path("steps");
            List<String> coordinates = new ArrayList<>();
            List<RouteResult.RoutePoint> routePoints = new ArrayList<>();
            
            // 构建路径点
            for (int i = 0; i < request.getWaypoints().size(); i++) {
                RouteResult.RoutePoint routePoint = new RouteResult.RoutePoint();
                routePoint.setSequence(i + 1);
                routePoint.setGeoPoint(request.getWaypoints().get(i));
                routePoint.setAddressId(request.getWaypoints().get(i).getAddressId());
                
                // 计算到达时间（简单估算）
                routePoint.setEstimatedArrivalTime((i + 1) * duration / 60 / request.getWaypoints().size());
                
                routePoints.add(routePoint);
                coordinates.add(request.getWaypoints().get(i).toCoordinateString());
            }
            
            result.setOptimizedWaypoints(routePoints);
            result.setRoutePath(String.join(";", coordinates));
            
            // 构建地图展示数据
            result.setMapDisplayData(buildMapDisplayData(firstPath, coordinates));
            
            // API调用信息
            RouteResult.ApiCallInfo apiInfo = new RouteResult.ApiCallInfo();
            apiInfo.setDuration(System.currentTimeMillis() - startTime);
            apiInfo.setFromCache(false);
            apiInfo.setResponseCode(200);
            apiInfo.setRetryCount(0);
            result.setApiCallInfo(apiInfo);
            
            return result;
            
        } catch (Exception e) {
            log.error("解析高德API响应失败", e);
            return RouteResult.error("gaode", "解析API响应失败: " + e.getMessage());
        }
    }

    /**
     * 构建地图展示数据
     */
    private String buildMapDisplayData(JsonNode pathData, List<String> coordinates) {
        try {
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("algorithm", "gaode");
            mapData.put("routeType", "realRoute");
            mapData.put("coordinates", coordinates);
            mapData.put("waypoints", coordinates.size());
            
            // 添加高德API特有信息
            mapData.put("distance", pathData.path("distance").asInt());
            mapData.put("duration", pathData.path("duration").asInt());
            mapData.put("tolls", pathData.path("tolls").asText("0"));
            mapData.put("traffic", pathData.path("traffic_lights").asInt(0));
            
            return objectMapper.writeValueAsString(mapData);
            
        } catch (Exception e) {
            log.warn("构建地图展示数据失败", e);
            return String.format(
                "{\"algorithm\":\"gaode\",\"routeType\":\"realRoute\",\"coordinates\":[%s],\"waypoints\":%d}",
                "\"" + String.join("\",\"", coordinates) + "\"",
                coordinates.size()
            );
        }
    }

    /**
     * 验证高德API是否可用
     */
    public boolean isApiAvailable() {
        try {
            if (apiKey == null || apiKey.trim().isEmpty() || "your_api_key_here".equals(apiKey)) {
                return false;
            }
            
            // 简单的API可用性测试
            Map<String, String> testParams = new HashMap<>();
            testParams.put("key", apiKey);
            testParams.put("origin", "116.397128,39.916527");
            testParams.put("destination", "116.404,39.925");
            testParams.put("output", "json");
            
            String response = callGaodeApiWithRetry(testParams);
            JsonNode root = objectMapper.readTree(response);
            return "1".equals(root.path("status").asText());
            
        } catch (Exception e) {
            log.warn("高德API可用性检测失败", e);
            return false;
        }
    }
}
