package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送单实体类（严格匹配delivery_service_db.delivery表）
 * 
 * <p>表名: delivery
 * <p>说明: 存储配送路径与分单结果，支持Dijkstra算法路径规划
 * 
 * <p>关键字段：
 * <ul>
 *   <li>delivery_mode: 1-团长团点模式；2-用户地址模式</li>
 *   <li>status: 0-待分配；1-配送中；2-已完成</li>
 *   <li>algorithm_used: dijkstra（当前版本仅支持Dijkstra算法）</li>
 *   <li>route_strategy: 0-最短时间；1-最短距离；2-避开拥堵</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Entity
@Table(name = "delivery", indexes = {
        @Index(name = "idx_leader_id", columnList = "leader_id"),
        @Index(name = "idx_dispatch_group", columnList = "dispatch_group"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_algorithm_used", columnList = "algorithm_used"),
        @Index(name = "idx_route_strategy", columnList = "route_strategy"),
        @Index(name = "idx_delivery_mode", columnList = "delivery_mode"),
        @Index(name = "idx_warehouse_id", columnList = "warehouse_id"),
        @Index(name = "idx_created_by", columnList = "created_by"),
        @Index(name = "idx_create_time", columnList = "create_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DeliveryEntity {

    /**
     * 配送单ID（主键）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id", nullable = false)
    private Long deliveryId;

    /**
     * 负责团长ID（可空，仅团长团点模式使用）
     * 跨库关联：user_service_db.sys_user
     */
    @Column(name = "leader_id")
    private Long leaderId;

    /**
     * 分单组标识（同批次配送订单）
     * 格式：SHIP + yyyyMMddHHmmss + 3位随机数
     */
    @Column(name = "dispatch_group", nullable = false, length = 50)
    private String dispatchGroup;

    /**
     * 发货方式
     * 1-团长团点模式；2-用户地址模式
     */
    @Column(name = "delivery_mode", nullable = false, columnDefinition = "TINYINT")
    private Integer deliveryMode = 1;

    /**
     * 起点仓库ID
     * 关联：warehouse_config.id
     */
    @Column(name = "warehouse_id")
    private Long warehouseId;

    /**
     * 终点仓库ID（可选，如果不回仓库则为NULL）
     * 关联：warehouse_config.id
     */
    @Column(name = "end_warehouse_id")
    private Long endWarehouseId;

    /**
     * 途经点数量
     */
    @Column(name = "waypoint_count", nullable = false, columnDefinition = "INT")
    private Integer waypointCount = 0;

    /**
     * 关联订单ID列表（JSON数组）
     * 示例：[1001, 1002, 1003]
     */
    @Column(name = "order_ids", nullable = false, columnDefinition = "TEXT")
    private String orderIds;

    /**
     * 途经点详细信息（JSON数组）
     * 示例：[{sequence:1, orderId:1001, address:"...", longitude:..., latitude:...}]
     */
    @Column(name = "waypoints_data", columnDefinition = "TEXT")
    private String waypointsData;

    /**
     * 最优路径（经纬度序列，Dijkstra算法计算结果）
     * 格式：lat1,lng1;lat2,lng2;lat3,lng3;...
     */
    @Column(name = "optimal_route", nullable = false, columnDefinition = "TEXT")
    private String optimalRoute;

    /**
     * 总配送距离（米）
     */
    @Column(name = "distance", nullable = false, precision = 10, scale = 2)
    private BigDecimal distance;

    /**
     * 预估配送时间（分钟）
     */
    @Column(name = "estimated_duration", columnDefinition = "INT")
    private Integer estimatedDuration;

    /**
     * 配送状态
     * 0-待分配；1-配送中；2-已完成
     */
    @Column(name = "status", nullable = false, columnDefinition = "TINYINT")
    private Integer status = 0;

    /**
     * 路径策略
     * 0-最短时间；1-最短距离；2-避开拥堵
     * 默认：1-最短距离
     */
    @Column(name = "route_strategy", columnDefinition = "TINYINT")
    private Integer routeStrategy = 1;

    /**
     * 使用的算法
     * dijkstra - Dijkstra算法（当前版本）
     * gaode - 高德地图API（后续扩展）
     */
    @Column(name = "algorithm_used", length = 20)
    private String algorithmUsed = "dijkstra";

    /**
     * 前端地图展示数据（JSON格式）
     * 包含：路径坐标、途经点标记、距离、时间等
     */
    @Column(name = "route_display_data", columnDefinition = "TEXT")
    private String routeDisplayData;

    /**
     * 配送开始时间（配送员开始配送）
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 配送完成时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 实际开始配送时间
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 创建人ID（管理员ID）
     * 跨库关联：user_service_db.sys_user
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}

