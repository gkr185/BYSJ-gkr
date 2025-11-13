package com.bcu.edu.dto;

import com.bcu.edu.enums.RouteStrategy;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 路径规划结果
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Data
@Schema(description = "路径规划结果")
public class RouteResult {

    @Schema(description = "是否成功", example = "true", required = true)
    private Boolean success;

    @Schema(description = "错误消息", example = "路径规划成功")
    private String message;

    @Schema(description = "使用的算法", example = "gaode", required = true)
    private String algorithmUsed;

    @Schema(description = "路径策略", required = true)
    private RouteStrategy routeStrategy;

    @Schema(description = "路径坐标序列（JSON格式）", required = true)
    private String routePath;

    @Schema(description = "总距离（米）", example = "5280.50", required = true)
    private BigDecimal totalDistance;

    @Schema(description = "预估时间（分钟）", example = "120")
    private Integer estimatedDuration;

    @Schema(description = "优化后的配送点顺序", required = true)
    private List<RoutePoint> optimizedWaypoints;

    @Schema(description = "地图展示数据（JSON格式）")
    private String mapDisplayData;

    @Schema(description = "计算时间", example = "2025-11-13 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calculatedAt;

    @Schema(description = "API调用详情")
    private ApiCallInfo apiCallInfo;

    public RouteResult() {
        this.calculatedAt = LocalDateTime.now();
    }

    /**
     * 创建成功结果
     */
    public static RouteResult success(String algorithmUsed) {
        RouteResult result = new RouteResult();
        result.setSuccess(true);
        result.setMessage("路径规划成功");
        result.setAlgorithmUsed(algorithmUsed);
        return result;
    }

    /**
     * 创建失败结果
     */
    public static RouteResult error(String algorithmUsed, String message) {
        RouteResult result = new RouteResult();
        result.setSuccess(false);
        result.setMessage(message);
        result.setAlgorithmUsed(algorithmUsed);
        return result;
    }

    /**
     * API调用信息
     */
    @Data
    @Schema(description = "API调用信息")
    public static class ApiCallInfo {
        
        @Schema(description = "调用耗时（毫秒）", example = "150")
        private Long duration;
        
        @Schema(description = "是否使用缓存", example = "false")
        private Boolean fromCache;
        
        @Schema(description = "API响应码", example = "200")
        private Integer responseCode;
        
        @Schema(description = "API错误信息")
        private String apiError;
        
        @Schema(description = "重试次数", example = "0")
        private Integer retryCount;
    }

    /**
     * 路径点信息
     */
    @Data
    @Schema(description = "路径点")
    public static class RoutePoint {
        
        @Schema(description = "序号", example = "1")
        private Integer sequence;
        
        @Schema(description = "地理坐标")
        private GeoPoint geoPoint;
        
        @Schema(description = "地址ID", example = "1001")
        private Long addressId;
        
        @Schema(description = "订单ID", example = "2001")
        private Long orderId;
        
        @Schema(description = "到达预估时间（分钟）", example = "15")
        private Integer estimatedArrivalTime;
        
        @Schema(description = "距离上个点的距离（米）", example = "1200.50")
        private BigDecimal distanceFromPrevious;
    }
}
