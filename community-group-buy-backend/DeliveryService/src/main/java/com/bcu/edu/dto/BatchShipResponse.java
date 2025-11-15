package com.bcu.edu.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量发货响应DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
public class BatchShipResponse {

    /**
     * 配送单ID
     */
    private Long deliveryId;

    /**
     * 分单组标识
     */
    private String dispatchGroup;

    /**
     * 关联订单ID列表
     */
    private List<Long> orderIds;

    /**
     * 发货方式
     * 1-团长团点；2-用户地址
     */
    private Integer deliveryMode;

    /**
     * 途经点数量
     */
    private Integer waypointCount;

    /**
     * 总距离（米）
     */
    private BigDecimal totalDistance;

    /**
     * 预估时间（分钟）
     */
    private Integer estimatedDuration;

    /**
     * 路径序列（用于地图展示）
     */
    private List<WaypointInfo> waypoints;

    /**
     * 使用的算法
     */
    private String algorithmUsed;

    /**
     * 消息提示
     */
    private String message;
}

