package com.bcu.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 路径规划请求
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Data
@Schema(description = "路径规划请求")
public class RouteRequest {

    @NotBlank(message = "分单组不能为空")
    @Schema(description = "分单组标识", example = "DG20251113001", required = true)
    private String dispatchGroup;

    @NotNull(message = "团长ID不能为空")
    @Schema(description = "负责团长ID", example = "1001", required = true)
    private Long leaderId;

    @NotNull(message = "起点坐标不能为空")
    @Schema(description = "起点坐标（仓库地址）", required = true)
    private GeoPoint origin;

    @NotEmpty(message = "配送点列表不能为空")
    @Schema(description = "配送点列表", required = true)
    private List<GeoPoint> waypoints;

    @Schema(description = "路径策略", example = "shortest-time", 
            allowableValues = {"shortest-time", "shortest-distance", "avoid-congestion"})
    private String routeStrategy = "shortest-time";

    @Schema(description = "是否强制使用高德API", example = "false")
    private Boolean forceGaodeApi = false;

    @Schema(description = "是否启用Dijkstra降级", example = "true")
    private Boolean enableDijkstraFallback = true;

    // 便利方法

    /**
     * 获取配送点数量
     */
    public int getWaypointCount() {
        return waypoints != null ? waypoints.size() : 0;
    }

    /**
     * 是否超过最大配送点限制
     */
    public boolean isExceedMaxWaypoints(int maxWaypoints) {
        return getWaypointCount() > maxWaypoints;
    }

    /**
     * 验证请求参数
     */
    public void validate(int maxWaypoints) {
        if (isExceedMaxWaypoints(maxWaypoints)) {
            throw new IllegalArgumentException(
                String.format("配送点数量(%d)超过最大限制(%d)", getWaypointCount(), maxWaypoints));
        }
        
        if (waypoints != null) {
            for (int i = 0; i < waypoints.size(); i++) {
                GeoPoint point = waypoints.get(i);
                if (point.getLatitude() == null || point.getLongitude() == null) {
                    throw new IllegalArgumentException(
                        String.format("配送点%d的坐标信息不完整", i + 1));
                }
            }
        }
    }
}
