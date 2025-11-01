package com.bcu.edu.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 余额充值请求
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
@Schema(description = "余额充值请求")
public class RechargeRequest {

    @Schema(description = "充值金额", required = true)
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    private BigDecimal amount;

    @Schema(description = "支付方式（1-微信；2-支付宝）", required = true)
    @NotNull(message = "支付方式不能为空")
    private Integer payType;
}

