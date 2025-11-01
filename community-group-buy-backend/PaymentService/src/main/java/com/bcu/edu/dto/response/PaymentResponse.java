package com.bcu.edu.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 支付响应
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Data
@Schema(description = "支付响应")
public class PaymentResponse {

    @Schema(description = "支付记录ID")
    private Long payId;

    @Schema(description = "支付类型（1-微信；2-支付宝；3-余额）")
    private Integer payType;

    @Schema(description = "支付状态（0-失败；1-成功）")
    private Integer payStatus;

    @Schema(description = "微信支付参数（仅微信支付时返回）")
    private Map<String, String> wxPayParams;

    @Schema(description = "支付宝支付参数（仅支付宝支付时返回）")
    private Map<String, String> alipayParams;

    @Schema(description = "错误信息（支付失败时返回）")
    private String errorMessage;
}

