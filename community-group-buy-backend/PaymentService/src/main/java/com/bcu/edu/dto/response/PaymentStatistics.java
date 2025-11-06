package com.bcu.edu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付统计响应DTO
 * 
 * @author 耿康瑞
 * @since 2025-11-06
 */
@Data
@Schema(description = "支付统计数据")
public class PaymentStatistics {

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总交易金额（包含充值、支付、退款）")
    private BigDecimal totalAmount;

    @Schema(description = "充值记录数")
    private Long rechargeCount;

    @Schema(description = "充值总额")
    private BigDecimal rechargeAmount;

    @Schema(description = "支付记录数（订单支付）")
    private Long paymentCount;

    @Schema(description = "支付总额（订单支付）")
    private BigDecimal paymentAmount;

    @Schema(description = "退款记录数")
    private Long refundCount;

    @Schema(description = "退款总额（负数）")
    private BigDecimal refundAmount;

    @Schema(description = "支付成功率（%）")
    private Double successRate;

    @Schema(description = "余额支付占比（%）")
    private Double balancePayRate;

    @Schema(description = "微信支付占比（%）")
    private Double wechatPayRate;

    @Schema(description = "支付宝支付占比（%）")
    private Double alipayPayRate;

    // 默认构造函数
    public PaymentStatistics() {
        this.total = 0L;
        this.totalAmount = BigDecimal.ZERO;
        this.rechargeCount = 0L;
        this.rechargeAmount = BigDecimal.ZERO;
        this.paymentCount = 0L;
        this.paymentAmount = BigDecimal.ZERO;
        this.refundCount = 0L;
        this.refundAmount = BigDecimal.ZERO;
        this.successRate = 0.0;
        this.balancePayRate = 0.0;
        this.wechatPayRate = 0.0;
        this.alipayPayRate = 0.0;
    }
}

