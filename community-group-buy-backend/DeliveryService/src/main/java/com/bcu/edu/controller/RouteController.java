package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.RouteRequest;
import com.bcu.edu.dto.RouteResult;
import com.bcu.edu.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 路径规划控制器
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@RestController
@RequestMapping("/api/delivery/route")
@Tag(name = "路径规划", description = "智能配送路径规划服务（Dijkstra + 高德地图双引擎）")
@Validated
public class RouteController {

    @Autowired
    private RouteService routeService;

    /**
     * 规划配送路径
     */
    @PostMapping("/plan")
    @Operation(summary = "规划配送路径", description = "使用双引擎（高德API + Dijkstra）计算最优配送路径")
    public Result<RouteResult> planRoute(@Valid @RequestBody RouteRequest request) {
        log.info("路径规划请求: 分单组={}, 配送点数={}", request.getDispatchGroup(), request.getWaypointCount());
        
        RouteResult result = routeService.planOptimalRoute(request);
        
        if (result.getSuccess()) {
            return Result.success("路径规划成功", result);
        } else {
            return Result.error("路径规划失败: " + result.getMessage());
        }
    }

    /**
     * 获取算法引擎状态
     */
    @GetMapping("/status")
    @Operation(summary = "获取算法引擎状态", description = "检查高德API和Dijkstra算法的可用状态")
    public Result<RouteService.AlgorithmStatus> getAlgorithmStatus() {
        RouteService.AlgorithmStatus status = routeService.getAlgorithmStatus();
        return Result.success("算法状态查询成功", status);
    }

    /**
     * 测试算法引擎
     */
    @PostMapping("/test/{algorithm}")
    @Operation(summary = "测试算法引擎", description = "使用预设数据测试指定算法的可用性和性能")
    public Result<RouteResult> testAlgorithm(
            @Parameter(description = "算法类型", required = true)
            @PathVariable String algorithm) {
        
        log.info("测试算法引擎: {}", algorithm);
        
        RouteResult result = routeService.testAlgorithm(algorithm);
        
        if (result.getSuccess()) {
            return Result.success("算法测试成功", result);
        } else {
            return Result.error("算法测试失败: " + result.getMessage());
        }
    }
}
