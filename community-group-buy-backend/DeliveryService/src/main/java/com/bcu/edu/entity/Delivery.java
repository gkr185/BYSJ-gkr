package com.bcu.edu.entity;

import com.bcu.edu.enums.DeliveryStatus;
import com.bcu.edu.enums.RouteStrategy;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送单实体
 * 
 * <p>支持双引擎路径规划：
 * <ul>
 *   <li>Dijkstra算法：离线路径计算，作为降级方案</li>
 *   <li>高德地图API：实时路况，精确路径规划</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "delivery")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "配送单")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    @Comment("配送单ID")
    @Schema(description = "配送单ID", example = "1")
    private Long deliveryId;

    @Column(name = "leader_id", nullable = false)
    @Comment("负责团长ID（跨库关联）")
    @Schema(description = "负责团长ID", example = "1001", required = true)
    private Long leaderId;

    @Column(name = "dispatch_group", nullable = false, length = 50)
    @Comment("关联分单组")
    @Schema(description = "分单组标识", example = "DG20251113001", required = true)
    private String dispatchGroup;

    @Column(name = "start_time")
    @Comment("配送开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "配送开始时间", example = "2025-11-13 10:00:00")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @Comment("配送完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "配送完成时间", example = "2025-11-13 14:00:00")
    private LocalDateTime endTime;

    @Column(name = "optimal_route", columnDefinition = "TEXT", nullable = false)
    @Comment("最优路径（JSON格式存储路径信息）")
    @Schema(description = "路径数据（JSON格式）", required = true)
    private String optimalRoute;

    @Column(name = "distance", nullable = false, precision = 10, scale = 2)
    @Comment("总配送距离（米）")
    @Schema(description = "配送距离（米）", example = "5280.50", required = true)
    private BigDecimal distance;

    @Column(name = "status", nullable = false)
    @Comment("配送状态（0-待分配；1-配送中；2-已完成）")
    @Schema(description = "配送状态", example = "0", required = true)
    private Integer status;

    // 扩展字段（为支持高德API新增）
    
    @Column(name = "route_strategy")
    @Comment("路径策略（0-最短时间；1-最短距离；2-避开拥堵）")
    @Schema(description = "路径策略", example = "0")
    private Integer routeStrategy;

    @Column(name = "estimated_duration")
    @Comment("预估配送时间（分钟）")
    @Schema(description = "预估配送时间（分钟）", example = "120")
    private Integer estimatedDuration;

    @Column(name = "actual_start_time")
    @Comment("实际开始配送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "实际开始配送时间")
    private LocalDateTime actualStartTime;

    @Column(name = "route_display_data", columnDefinition = "TEXT")
    @Comment("前端地图展示数据")
    @Schema(description = "地图展示数据（JSON格式）")
    private String routeDisplayData;

    @Column(name = "algorithm_used", length = 20)
    @Comment("使用的算法（dijkstra/gaode）")
    @Schema(description = "使用的算法", example = "gaode")
    private String algorithmUsed;

    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    @Comment("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2025-11-13 09:00:00")
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    @Comment("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2025-11-13 09:30:00")
    private LocalDateTime updateTime;

    // 便利方法

    /**
     * 获取配送状态枚举
     */
    public DeliveryStatus getDeliveryStatus() {
        return DeliveryStatus.fromCode(this.status);
    }

    /**
     * 设置配送状态枚举
     */
    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.status = deliveryStatus != null ? deliveryStatus.getCode() : null;
    }

    /**
     * 获取路径策略枚举
     */
    public RouteStrategy getRouteStrategyEnum() {
        return RouteStrategy.fromStrategy(this.getRouteStrategyString());
    }

    /**
     * 设置路径策略枚举
     */
    public void setRouteStrategyEnum(RouteStrategy routeStrategy) {
        this.routeStrategy = routeStrategy != null ? routeStrategy.getCode() : null;
    }

    /**
     * 获取路径策略字符串
     */
    private String getRouteStrategyString() {
        if (routeStrategy == null) return "shortest-time";
        return switch (routeStrategy) {
            case 0 -> "shortest-time";
            case 1 -> "shortest-distance";
            case 2 -> "avoid-congestion";
            default -> "shortest-time";
        };
    }

    /**
     * 是否已完成配送
     */
    public boolean isCompleted() {
        return DeliveryStatus.COMPLETED.equals(getDeliveryStatus());
    }

    /**
     * 是否正在配送中
     */
    public boolean isDelivering() {
        return DeliveryStatus.DELIVERING.equals(getDeliveryStatus());
    }
}
