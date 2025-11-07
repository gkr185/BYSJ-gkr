package com.bcu.edu.client.fallback;

import com.bcu.edu.client.PaymentServiceClient;
import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.CreatePaymentRequest;
import com.bcu.edu.dto.request.RefundRequest;
import com.bcu.edu.dto.response.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * PaymentService Feign客户端降级处理
 * 
 * @author 系统
 * @since 2025-11-07
 */
@Component
@Slf4j
public class PaymentServiceClientFallback implements PaymentServiceClient {
    
    @Override
    public Result<PaymentResponse> createPayment(Long userId, CreatePaymentRequest request) {
        log.error("PaymentService调用失败，支付创建失败，userId={}, request={}", userId, request);
        throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, 
            "支付服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Void> refund(RefundRequest request) {
        log.error("PaymentService调用失败，退款失败，orderId={}, reason={}", 
            request.getOrderId(), request.getReason());
        throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, 
            "退款服务暂时不可用，请稍后重试");
    }
}

