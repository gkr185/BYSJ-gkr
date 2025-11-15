package com.bcu.edu.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量发货请求DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Data
public class BatchShipRequest {

    /**
     * 订单ID列表（必填）
     */
    @NotEmpty(message = "订单ID列表不能为空")
    private List<Long> orderIds;

    /**
     * 发货方式（必填）
     * 1-团长团点模式；2-用户地址模式
     */
    @NotNull(message = "发货方式不能为空")
    private Integer deliveryMode;

    /**
     * 起点仓库ID（必填）
     */
    @NotNull(message = "起点仓库ID不能为空")
    private Long warehouseId;

    /**
     * 终点仓库ID（可选，如果不回仓库则为NULL）
     */
    private Long endWarehouseId;

    /**
     * 路径策略（可选，默认1-最短距离）
     * 0-最短时间；1-最短距离；2-避开拥堵
     */
    private Integer routeStrategy = 1;

    /**
     * 创建人ID（管理员ID）
     */
    private Long createdBy;
}

