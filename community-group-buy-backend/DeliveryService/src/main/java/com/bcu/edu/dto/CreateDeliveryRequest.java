package com.bcu.edu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建配送单请求
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Data
@Schema(description = "创建配送单请求")
public class CreateDeliveryRequest {

    @NotBlank(message = "分单组不能为空")
    @Schema(description = "分单组标识", example = "DG20251113001", required = true)
    private String dispatchGroup;

    @NotNull(message = "团长ID不能为空")
    @Schema(description = "负责团长ID", example = "1001", required = true)
    private Long leaderId;

    @Schema(description = "路径策略", example = "shortest-time", 
            allowableValues = {"shortest-time", "shortest-distance", "avoid-congestion"})
    private String routeStrategy = "shortest-time";

    @Schema(description = "仓库ID", example = "1")
    private Long warehouseId;

    @Schema(description = "是否立即生成路径", example = "true")
    private Boolean generateRoute = true;

    @Schema(description = "备注", example = "紧急配送")
    private String remark;
}
