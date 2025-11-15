package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.StatisticsOverviewDTO;
import com.bcu.edu.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 配送统计Controller
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@RestController
@RequestMapping("/api/delivery/statistics")
@RequiredArgsConstructor
@Tag(name = "配送统计", description = "配送数据统计分析")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取配送总览统计
     */
    @GetMapping("/overview")
    @Operation(summary = "配送总览统计", description = "获取配送总量、距离、效率等统计数据")
    public Result<StatisticsOverviewDTO> getOverview() {
        StatisticsOverviewDTO overview = statisticsService.getOverview();
        return Result.success(overview);
    }

    /**
     * 获取距离统计
     */
    @GetMapping("/distance")
    @Operation(summary = "距离统计", description = "统计指定时间范围内的配送距离")
    public Result<Map<String, Object>> getDistanceStatistics(
            @Parameter(description = "开始时间")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> statistics = statisticsService.getDistanceStatistics(startTime, endTime);
        
        return Result.success(statistics);
    }

    /**
     * 获取配送效率统计
     */
    @GetMapping("/efficiency")
    @Operation(summary = "效率统计", description = "统计配送完成率、平均时间等效率指标")
    public Result<Map<String, Object>> getEfficiencyStatistics(
            @Parameter(description = "开始时间")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> statistics = statisticsService.getEfficiencyStatistics(startTime, endTime);
        
        return Result.success(statistics);
    }

    /**
     * 获取团长配送统计
     */
    @GetMapping("/leader")
    @Operation(summary = "团长配送统计", description = "按团长统计配送单数量")
    public Result<List<Map<String, Object>>> getLeaderStatistics() {
        List<Map<String, Object>> statistics = statisticsService.getLeaderStatistics();
        return Result.success(statistics);
    }
}

