package com.bcu.edu.controller;

import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.service.DeliveryService;
import com.bcu.edu.service.RouteService;
import com.bcu.edu.service.WarehouseConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 配送服务监控控制器
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@RestController
@RequestMapping("/api/delivery/monitor")
@Tag(name = "配送监控", description = "配送服务状态监控和健康检查")
public class MonitorController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private WarehouseConfigService warehouseConfigService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查配送服务的基础健康状态")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", "DeliveryService");
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        return Result.success("服务正常运行", health);
    }

    /**
     * 服务状态检查
     */
    @GetMapping("/status")
    @Operation(summary = "服务状态检查", description = "检查配送服务各组件的运行状态")
    public Result<Map<String, Object>> statusCheck() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 算法引擎状态
            RouteService.AlgorithmStatus algorithmStatus = routeService.getAlgorithmStatus();
            status.put("algorithmStatus", algorithmStatus);
            
            // 仓库配置状态
            WarehouseConfigService.WarehouseStatistics warehouseStats = 
                    warehouseConfigService.getWarehouseStatistics();
            status.put("warehouseStatus", warehouseStats);
            
            // 配送统计状态
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime tomorrow = today.plusDays(1);
            DeliveryService.DeliveryStatistics deliveryStats = 
                    deliveryService.getDeliveryStatistics(null, today, tomorrow);
            status.put("deliveryStatus", deliveryStats);
            
            status.put("overall", "healthy");
            status.put("timestamp", LocalDateTime.now());
            
            return Result.success("状态检查完成", status);
            
        } catch (Exception e) {
            log.error("状态检查失败", e);
            status.put("overall", "unhealthy");
            status.put("error", e.getMessage());
            status.put("timestamp", LocalDateTime.now());
            
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR, status);
        }
    }

    /**
     * 版本信息
     */
    @GetMapping("/version")
    @Operation(summary = "版本信息", description = "获取配送服务版本和构建信息")
    public Result<Map<String, Object>> versionInfo() {
        Map<String, Object> version = new HashMap<>();
        version.put("serviceName", "DeliveryService");
        version.put("version", "1.0.0");
        version.put("buildDate", "2025-11-13");
        version.put("features", new String[]{
            "Dijkstra算法路径规划",
            "高德地图API集成", 
            "双引擎智能切换",
            "批量发货处理",
            "配送状态管理",
            "地图可视化支持"
        });
        version.put("port", 8067);
        version.put("database", "delivery_service_db");
        
        return Result.success(version);
    }
}
