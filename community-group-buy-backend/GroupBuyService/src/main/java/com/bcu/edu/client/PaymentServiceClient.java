package com.bcu.edu.client;

import com.bcu.edu.client.fallback.PaymentServiceClientFallback;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.CreatePaymentRequest;
import com.bcu.edu.dto.request.RefundRequest;
import com.bcu.edu.dto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * PaymentService Feign客户端
 * 
 * <p>功能：
 * <ul>
 *   <li>创建支付（团长发起并立即参与时）</li>
 *   <li>退款处理（拼团失败时）</li>
 * </ul>
 * 
 * @author 系统
 * @since 2025-11-07
 */
@FeignClient(name = "payment-service", fallback = PaymentServiceClientFallback.class)
public interface PaymentServiceClient {
    
    /**
     * 创建支付（⭐核心接口）
     * 
     * <p>应用场景：
     * <ul>
     *   <li>团长发起并立即参与拼团时自动支付</li>
     * </ul>
     * 
     * @param userId 用户ID（从Header传递）
     * @param request 创建支付请求
     * @return Result<PaymentResponse> 支付结果
     */
    @PostMapping("/api/payment/create")
    Result<PaymentResponse> createPayment(
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody CreatePaymentRequest request);
    
    /**
     * 申请退款（⭐核心接口）
     * 
     * <p>应用场景：
     * <ul>
     *   <li>用户主动退出拼团</li>
     *   <li>拼团失败自动退款</li>
     *   <li>团长移除成员</li>
     * </ul>
     * 
     * @param request 退款请求
     * @return Result<Void> 退款结果
     */
    @PostMapping("/api/payment/feign/refund")
    Result<Void> refund(@RequestBody RefundRequest request);
}

