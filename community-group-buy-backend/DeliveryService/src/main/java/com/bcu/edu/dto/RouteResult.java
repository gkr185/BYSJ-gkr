package com.bcu.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 路径规划结果DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResult {

    /**
     * 最优路径序列（途经点索引顺序）
     * 示例：[0, 2, 1, 3] 表示按起点→途经点2→途经点1→途经点3→终点
     */
    private List<Integer> pathSequence;

    /**
     * 最优路径坐标序列（用于地图展示）
     * 格式：lat1,lng1;lat2,lng2;lat3,lng3;...
     */
    private String optimalRoute;

    /**
     * 总距离（米）
     */
    private BigDecimal totalDistance;

    /**
     * 预估时间（分钟）
     */
    private Integer estimatedDuration;

    /**
     * 使用的算法
     * dijkstra / gaode
     */
    private String algorithmUsed = "dijkstra";

    /**
     * 详细路径信息（包含每段距离）
     */
    private List<SegmentInfo> segments;

    /**
     * 路段信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SegmentInfo {
        /**
         * 起点索引
         */
        private Integer fromIndex;

        /**
         * 终点索引
         */
        private Integer toIndex;

        /**
         * 距离（米）
         */
        private BigDecimal distance;

        /**
         * 预估时间（分钟）
         */
        private Integer duration;
    }
}

