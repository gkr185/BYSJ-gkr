package com.bcu.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量发货请求
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Data
@Schema(description = "批量发货请求")
public class BatchShipRequest {

    @NotEmpty(message = "订单ID列表不能为空")
    @Schema(description = "订单ID列表", required = true)
    private List<Long> orderIds;

    @Schema(description = "仓库ID", example = "1")
    private Long warehouseId;

    @Schema(description = "路径策略", example = "shortest-time", 
            allowableValues = {"shortest-time", "shortest-distance", "avoid-congestion"})
    private String routeStrategy = "shortest-time";

    @Schema(description = "备注", example = "批量发货操作")
    private String remark;

    @Schema(description = "操作员ID", example = "1001")
    private Long operatorId;

    @Schema(description = "操作员姓名", example = "管理员")
    private String operatorName;
}
