package com.bcu.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 路径规划请求DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {

    /**
     * 起点坐标
     */
    private Coordinate start;

    /**
     * 途经点坐标列表
     */
    private List<Coordinate> waypoints;

    /**
     * 终点坐标（可选）
     */
    private Coordinate end;

    /**
     * 路径策略
     * 0-最短时间；1-最短距离；2-避开拥堵
     */
    private Integer strategy = 1;

    /**
     * 坐标点DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinate {
        /**
         * 经度
         */
        private BigDecimal longitude;

        /**
         * 纬度
         */
        private BigDecimal latitude;

        /**
         * 关联ID（订单ID或团长ID）
         */
        private Long relatedId;

        /**
         * 地址名称
         */
        private String addressName;
    }
}

