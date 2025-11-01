package com.bcu.edu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付请求
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
@Schema(description = "创建支付请求")
public class CreatePaymentRequest {

    @Schema(description = "订单ID", required = true)
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @Schema(description = "支付方式（1-微信；2-支付宝；3-余额）", required = true)
    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    @Schema(description = "支付金额", required = true)
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额必须大于0")
    private BigDecimal amount;
}

