package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.PaymentStatistics;
import com.bcu.edu.entity.PaymentRecord;
import com.bcu.edu.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 支付管理Controller（管理端接口）
 * 
 * <p>核心接口：
 * <ul>
 *   <li>GET /api/payment/admin/records - 查询所有用户支付记录（⭐最关键）</li>
 *   <li>GET /api/payment/admin/statistics - 支付统计</li>
 * </ul>
 * 
 * <p>权限要求：需要管理员权限
 * 
 * @author 耿康瑞
 * @since 2025-11-06
 */
@RestController
@RequestMapping("/api/payment/admin")
@Tag(name = "支付管理（管理端）", description = "管理端支付查询、统计接口")
@Slf4j
public class AdminPaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * 查询所有用户的支付记录（管理端）
     * 
     * <p>支持筛选条件：
     * <ul>
     *   <li>支付方式（payType）</li>
     *   <li>支付状态（payStatus）</li>
     *   <li>记录类型（recordType: recharge/payment/refund）</li>
     *   <li>关键词搜索（keyword: 订单号/交易号）</li>
     *   <li>日期范围（startDate/endDate）</li>
     * </ul>
     * 
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param payType 支付方式（可选）
     * @param payStatus 支付状态（可选）
     * @param recordType 记录类型（可选：recharge/payment/refund）
     * @param keyword 搜索关键词（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 分页的支付记录列表
     */
    @GetMapping("/records")
    @Operation(summary = "查询所有支付记录（管理端）", description = "支持分页和多条件筛选")
    public Result<Page<PaymentRecord>> getAdminPaymentRecords(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "支付方式（1-微信；2-支付宝；3-余额）") @RequestParam(required = false) Integer payType,
            @Parameter(description = "支付状态（0-失败；1-成功）") @RequestParam(required = false) Integer payStatus,
            @Parameter(description = "记录类型（recharge/payment/refund）") @RequestParam(required = false) String recordType,
            @Parameter(description = "搜索关键词（订单号/交易号）") @RequestParam(required = false) String keyword,
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        log.info("管理端查询支付记录: page={}, size={}, payType={}, payStatus={}, recordType={}, keyword={}, startDate={}, endDate={}",
                 page, size, payType, payStatus, recordType, keyword, startDate, endDate);

        try {
            // 转换日期为时间范围
            LocalDateTime startDateTime = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
            LocalDateTime endDateTime = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;

            // 创建分页对象（按创建时间倒序）
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));

            // 调用Service查询
            Page<PaymentRecord> records = paymentService.getAdminPaymentRecords(
                    pageable, payType, payStatus, recordType, keyword, startDateTime, endDateTime);

            log.info("查询成功: 总记录数={}, 当前页记录数={}", records.getTotalElements(), records.getNumberOfElements());
            return Result.success(records);
        } catch (Exception e) {
            log.error("查询支付记录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 支付统计（管理端）
     * 
     * <p>统计内容：
     * <ul>
     *   <li>总记录数、总交易金额</li>
     *   <li>充值记录数、充值总额</li>
     *   <li>支付记录数、支付总额</li>
     *   <li>退款记录数、退款总额</li>
     *   <li>支付成功率</li>
     * </ul>
     * 
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "支付统计（管理端）", description = "统计支付相关数据")
    public Result<PaymentStatistics> getStatistics(
            @Parameter(description = "开始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        log.info("管理端查询支付统计: startDate={}, endDate={}", startDate, endDate);

        try {
            // 转换日期为时间范围
            LocalDateTime startDateTime = startDate != null ? LocalDateTime.of(startDate, LocalTime.MIN) : null;
            LocalDateTime endDateTime = endDate != null ? LocalDateTime.of(endDate, LocalTime.MAX) : null;

            // 调用Service统计
            PaymentStatistics statistics = paymentService.getPaymentStatistics(startDateTime, endDateTime);

            log.info("统计成功: 总记录数={}, 总交易金额={}", statistics.getTotal(), statistics.getTotalAmount());
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("查询统计数据失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定用户的支付记录（管理端）
     * 
     * @param userId 用户ID
     * @return 支付记录列表
     */
    @GetMapping("/user/{userId}/records")
    @Operation(summary = "查询指定用户支付记录（管理端）", description = "查询某个用户的所有支付记录")
    public Result<List<PaymentRecord>> getUserPaymentRecords(@PathVariable Long userId) {
        log.info("管理端查询用户支付记录: userId={}", userId);

        try {
            List<PaymentRecord> records = paymentService.getUserPaymentRecords(userId);
            return Result.success(records);
        } catch (Exception e) {
            log.error("查询用户支付记录失败: userId={}", userId, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}

