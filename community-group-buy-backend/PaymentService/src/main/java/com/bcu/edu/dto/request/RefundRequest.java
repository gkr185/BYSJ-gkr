package com.bcu.edu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 退款请求
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
@Schema(description = "退款请求")
public class RefundRequest {

    @Schema(description = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "退款原因")
    private String reason;
}

