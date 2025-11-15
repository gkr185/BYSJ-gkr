package com.bcu.edu.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 配送单详情DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
public class DeliveryDetailDTO {

    /**
     * 配送单ID
     */
    private Long deliveryId;

    /**
     * 分单组标识
     */
    private String dispatchGroup;

    /**
     * 发货方式
     * 1-团长团点；2-用户地址
     */
    private Integer deliveryMode;

    /**
     * 配送状态
     * 0-待分配；1-配送中；2-已完成
     */
    private Integer status;

    /**
     * 起点仓库信息
     */
    private WarehouseInfo startWarehouse;

    /**
     * 终点仓库信息
     */
    private WarehouseInfo endWarehouse;

    /**
     * 途经点列表
     */
    private List<WaypointInfo> waypoints;

    /**
     * 关联订单列表
     */
    private List<OrderInfoDTO> orders;

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
     */
    private String algorithmUsed;

    /**
     * 路径策略
     */
    private Integer routeStrategy;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 仓库信息内部类
     */
    @Data
    public static class WarehouseInfo {
        private Long id;
        private String warehouseName;
        private String address;
        private BigDecimal longitude;
        private BigDecimal latitude;
    }
}

