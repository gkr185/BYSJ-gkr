package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.CreatePaymentRequest;
import com.bcu.edu.dto.response.PaymentResponse;
import com.bcu.edu.entity.PaymentRecord;
import com.bcu.edu.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 支付Controller（用户端接口）
 * 
 * <p>核心接口：
 * <ul>
 *   <li>POST /api/payment/create - 创建支付（⭐最关键）</li>
 *   <li>GET /api/payment/record/{payId} - 查询支付记录</li>
 *   <li>GET /api/payment/order/{orderId} - 查询订单支付记录</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@RestController
@RequestMapping("/api/payment")
@Tag(name = "支付管理", description = "支付、充值、退款接口")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 创建支付（⭐⭐⭐⭐⭐最核心接口）
     * 
     * <p>流程：
     * <ol>
     *   <li>创建支付记录</li>
     *   <li>根据支付方式分流：
     *     <ul>
     *       <li>余额支付：调用UserService扣款，立即完成</li>
     *       <li>微信支付：返回支付参数给前端</li>
     *       <li>支付宝支付：返回支付参数给前端</li>
     *     </ul>
     *   </li>
     * </ol>
     * 
     * @param request 支付请求
     * @param httpRequest HTTP请求（获取用户ID）
     * @return 支付响应
     */
    @PostMapping("/create")
    @Operation(summary = "创建支付", description = "用户下单后创建支付订单")
    public Result<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                  HttpServletRequest httpRequest) {
        // 从请求头获取用户ID（Gateway解析JWT传递）
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }

        Long userId = Long.parseLong(userIdHeader);
        log.info("用户发起支付: userId={}, orderId={}, payType={}, amount={}", 
                 userId, request.getOrderId(), request.getPayType(), request.getAmount());

        try {
            PaymentResponse response = paymentService.createPayment(request, userId);
            
            if (response.getPayStatus() == 1) {
                return Result.success("支付成功", response);
            } else {
                // 支付失败，返回错误信息和响应数据
                return new Result<>(500, response.getErrorMessage(), response);
            }
        } catch (Exception e) {
            log.error("创建支付失败: userId={}", userId, e);
            return Result.error("支付失败: " + e.getMessage());
        }
    }

    /**
     * 查询支付记录
     * 
     * @param payId 支付记录ID
     * @return 支付记录
     */
    @GetMapping("/record/{payId}")
    @Operation(summary = "查询支付记录", description = "根据支付记录ID查询详情")
    public Result<PaymentRecord> getPaymentRecord(@PathVariable Long payId) {
        log.info("查询支付记录: payId={}", payId);

        try {
            PaymentRecord record = paymentService.getPaymentRecord(payId);
            return Result.success(record);
        } catch (Exception e) {
            log.error("查询支付记录失败: payId={}", payId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单支付记录
     * 
     * @param orderId 订单ID
     * @return 支付记录
     */
    @GetMapping("/order/{orderId}")
    @Operation(summary = "查询订单支付记录", description = "根据订单ID查询支付记录")
    public Result<PaymentRecord> getPaymentByOrderId(@PathVariable Long orderId) {
        log.info("查询订单支付记录: orderId={}", orderId);

        try {
            PaymentRecord record = paymentService.getPaymentByOrderId(orderId);
            return Result.success(record);
        } catch (Exception e) {
            log.error("查询订单支付记录失败: orderId={}", orderId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 余额充值（⭐简单实现版本）
     * 
     * <p>说明：当前为简化版本，直接增加余额
     * <p>TODO: 后续集成微信支付/支付宝支付后，需要先创建支付订单，等待支付回调
     * 
     * @param request 充值请求
     * @param httpRequest HTTP请求（获取用户ID）
     * @return 充值记录
     */
    @PostMapping("/recharge")
    @Operation(summary = "余额充值", description = "用户充值余额（简化版本）")
    public Result<PaymentRecord> recharge(@Valid @RequestBody com.bcu.edu.dto.request.RechargeRequest request,
                                          HttpServletRequest httpRequest) {
        // 从请求头获取用户ID（Gateway解析JWT传递）
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }

        Long userId = Long.parseLong(userIdHeader);
        log.info("用户发起充值: userId={}, amount={}", userId, request.getAmount());

        try {
            PaymentRecord record = paymentService.recharge(userId, request.getAmount());
            return Result.success("充值成功", record);
        } catch (Exception e) {
            log.error("充值失败: userId={}", userId, e);
            return Result.error("充值失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户所有支付记录（包含充值记录）
     * 
     * @param httpRequest HTTP请求（获取用户ID）
     * @return 支付记录列表
     */
    @GetMapping("/records")
    @Operation(summary = "查询所有支付记录", description = "查询当前用户的所有支付/充值记录")
    public Result<java.util.List<PaymentRecord>> getUserPaymentRecords(HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }

        Long userId = Long.parseLong(userIdHeader);
        log.info("查询用户支付记录: userId={}", userId);

        try {
            java.util.List<PaymentRecord> records = paymentService.getUserPaymentRecords(userId);
            return Result.success(records);
        } catch (Exception e) {
            log.error("查询支付记录失败: userId={}", userId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户充值记录
     * 
     * @param httpRequest HTTP请求（获取用户ID）
     * @return 充值记录列表
     */
    @GetMapping("/recharge/records")
    @Operation(summary = "查询充值记录", description = "查询当前用户的充值记录")
    public Result<java.util.List<PaymentRecord>> getUserRechargeRecords(HttpServletRequest httpRequest) {
        String userIdHeader = httpRequest.getHeader("X-User-Id");
        if (userIdHeader == null) {
            return Result.error(401, "未登录");
        }

        Long userId = Long.parseLong(userIdHeader);
        log.info("查询用户充值记录: userId={}", userId);

        try {
            java.util.List<PaymentRecord> records = paymentService.getUserRechargeRecords(userId);
            return Result.success(records);
        } catch (Exception e) {
            log.error("查询充值记录失败: userId={}", userId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}

