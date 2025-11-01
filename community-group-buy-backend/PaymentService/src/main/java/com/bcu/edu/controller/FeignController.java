package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.RefundRequest;
import com.bcu.edu.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Feign内部接口Controller（供其他微服务调用）
 * 
 * <p>🔴 核心接口！供OrderService/GroupBuyService调用
 * 
 * <p>核心接口：
 * <ul>
 *   <li>POST /api/payment/feign/refund - 申请退款（⭐关键）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@RestController
@RequestMapping("/api/payment/feign")
@Tag(name = "支付Feign接口", description = "供其他微服务调用的内部接口")
@Slf4j
public class FeignController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 申请退款（⭐⭐⭐⭐⭐关键接口）
     * 
     * <p>调用方: OrderService/GroupBuyService
     * <p>场景: 订单取消或拼团失败时退款
     * 
     * <p>流程：
     * <ol>
     *   <li>查询原支付记录</li>
     *   <li>创建退款记录（amount为负数）</li>
     *   <li>根据原支付方式退款：
     *     <ul>
     *       <li>余额支付：调用UserService增加余额</li>
     *       <li>微信支付：调用微信退款API</li>
     *       <li>支付宝支付：调用支付宝退款API</li>
     *     </ul>
     *   </li>
     * </ol>
     * 
     * @param request 退款请求
     * @return 成功/失败
     */
    @PostMapping("/refund")
    @Operation(summary = "申请退款", description = "供其他服务调用，订单取消或拼团失败时退款")
    public Result<Void> refund(@Valid @RequestBody RefundRequest request) {
        log.info("Feign调用: 申请退款, orderId={}, reason={}", request.getOrderId(), request.getReason());

        try {
            paymentService.refund(request);
            log.info("退款成功: orderId={}", request.getOrderId());
            return Result.success("退款成功");
        } catch (Exception e) {
            log.error("退款失败: orderId={}", request.getOrderId(), e);
            return Result.error("退款失败: " + e.getMessage());
        }
    }
}

