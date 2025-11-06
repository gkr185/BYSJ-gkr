package com.bcu.edu.service;

import com.bcu.edu.client.GroupBuyServiceClient;
import com.bcu.edu.client.OrderServiceClient;
import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.request.CreatePaymentRequest;
import com.bcu.edu.dto.request.RefundRequest;
import com.bcu.edu.dto.response.PaymentResponse;
import com.bcu.edu.dto.response.PaymentStatistics;
import com.bcu.edu.entity.PaymentRecord;
import com.bcu.edu.enums.PayStatus;
import com.bcu.edu.enums.PayType;
import com.bcu.edu.repository.PaymentRecordRepository;
import com.bcu.edu.util.SecurityUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 支付服务（⭐⭐⭐⭐⭐核心Service）
 * 
 * <p>核心功能：
 * <ul>
 *   <li>支付发起（余额支付、微信支付、支付宝支付）</li>
 *   <li>支付成功后处理（更新订单、回调GroupBuyService）</li>
 *   <li>退款处理（余额退款、微信退款、支付宝退款）</li>
 *   <li>充值管理（余额充值）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Service
@Slf4j
public class PaymentService {

    @Autowired
    private PaymentRecordRepository paymentRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private GroupBuyServiceClient groupBuyServiceClient;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 创建支付（⭐⭐⭐⭐⭐最核心方法）
     * 
     * <p>流程：
     * <ol>
     *   <li>验证订单是否存在</li>
     *   <li>创建支付记录</li>
     *   <li>根据支付方式分流：
     *     <ul>
     *       <li>余额支付：调用UserService扣款，立即完成</li>
     *       <li>微信支付：调用微信统一下单API，返回支付参数</li>
     *       <li>支付宝支付：调用支付宝统一下单API，返回支付参数</li>
     *     </ul>
     *   </li>
     * </ol>
     * 
     * @param request 支付请求
     * @param userId 用户ID（从JWT解析）
     * @return 支付响应
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "创建支付", module = "支付管理")
    public PaymentResponse createPayment(CreatePaymentRequest request, Long userId) {
        log.info("开始创建支付: userId={}, orderId={}, payType={}, amount={}", 
                 userId, request.getOrderId(), request.getPayType(), request.getAmount());

        // 1. 创建支付记录
        PaymentRecord record = new PaymentRecord();
        record.setUserId(userId);
        record.setOrderId(request.getOrderId());
        record.setPayType(request.getPayType());
        record.setAmount(request.getAmount());
        record.setPayStatus(PayStatus.FAILED.getCode());
        
        // 生成签名（防篡改）
        String signContent = userId + "_" + request.getOrderId() + "_" + request.getAmount();
        String sign = securityUtil.sha256Sign(signContent);
        record.setEncryptSign(sign);

        // 保存支付记录
        PaymentRecord savedRecord = paymentRepository.save(record);
        log.info("支付记录已创建: payId={}", savedRecord.getPayId());

        // 2. 根据支付方式分流
        PaymentResponse response = new PaymentResponse();
        response.setPayId(savedRecord.getPayId());
        response.setPayType(request.getPayType());

        if (request.getPayType().equals(PayType.BALANCE.getCode())) {
            // 余额支付
            return handleBalancePayment(savedRecord, userId, response);
        } else if (request.getPayType().equals(PayType.WECHAT.getCode())) {
            // 微信支付（TODO: 实际项目需接入微信支付SDK）
            log.info("微信支付暂未实现，使用Mock数据");
            response.setPayStatus(PayStatus.FAILED.getCode());
            response.setErrorMessage("微信支付暂未开放，请使用余额支付");
            return response;
        } else if (request.getPayType().equals(PayType.ALIPAY.getCode())) {
            // 支付宝支付（TODO: 实际项目需接入支付宝SDK）
            log.info("支付宝支付暂未实现，使用Mock数据");
            response.setPayStatus(PayStatus.FAILED.getCode());
            response.setErrorMessage("支付宝支付暂未开放，请使用余额支付");
            return response;
        } else {
            throw new BusinessException("不支持的支付方式");
        }
    }

    /**
     * 处理余额支付（⭐⭐⭐⭐⭐核心方法）
     * 
     * <p>流程：
     * <ol>
     *   <li>调用UserService扣款</li>
     *   <li>扣款成功 → 更新支付记录 → 执行支付成功后处理</li>
     *   <li>扣款失败 → 返回错误信息</li>
     * </ol>
     */
    private PaymentResponse handleBalancePayment(PaymentRecord record, Long userId, 
                                                  PaymentResponse response) {
        log.info("开始余额支付: userId={}, amount={}", userId, record.getAmount());

        try {
            // 1. 调用UserService扣款
            Result<Void> deductResult = userServiceClient.deduct(userId, record.getAmount());
            
            if (deductResult.getCode() == 200) {
                // 2. 扣款成功，更新支付记录
                record.setPayStatus(PayStatus.SUCCESS.getCode());
                record.setTransactionId("BALANCE_" + System.currentTimeMillis());
                paymentRepository.save(record);
                
                log.info("余额支付成功: payId={}, userId={}", record.getPayId(), userId);
                
                // 3. 执行支付成功后处理
                handleOrderPaymentSuccess(record.getOrderId(), record.getPayId());
                
                response.setPayStatus(PayStatus.SUCCESS.getCode());
                return response;
            } else {
                // 扣款失败
                log.warn("余额扣款失败: userId={}, msg={}", userId, deductResult.getMessage());
                response.setPayStatus(PayStatus.FAILED.getCode());
                response.setErrorMessage(deductResult.getMessage());
                return response;
            }
        } catch (Exception e) {
            log.error("余额支付异常: userId={}", userId, e);
            response.setPayStatus(PayStatus.FAILED.getCode());
            response.setErrorMessage("支付失败: " + e.getMessage());
            return response;
        }
    }

    /**
     * 支付成功后处理（⭐⭐⭐⭐⭐核心方法）
     * 
     * <p>流程：
     * <ol>
     *   <li>调用OrderService更新订单支付状态</li>
     *   <li>判断是否拼团订单</li>
     *   <li>如果是拼团订单，调用GroupBuyService支付回调（触发成团检查）</li>
     * </ol>
     * 
     * @param orderId 订单ID
     * @param payId 支付记录ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderPaymentSuccess(Long orderId, Long payId) {
        log.info("开始处理支付成功后逻辑: orderId={}, payId={}", orderId, payId);

        try {
            // 1. 更新订单支付状态
            Result<Void> updateResult = orderServiceClient.updatePayStatus(orderId, 1);
            if (updateResult.getCode() != 200) {
                log.error("更新订单支付状态失败: orderId={}, msg={}", orderId, updateResult.getMessage());
                throw new BusinessException("更新订单支付状态失败");
            }
            log.info("订单支付状态已更新: orderId={}", orderId);

            // 2. 判断是否拼团订单
            Result<Boolean> isGroupBuyResult = orderServiceClient.isGroupBuyOrder(orderId);
            if (isGroupBuyResult.getCode() != 200) {
                log.error("判断订单类型失败: orderId={}", orderId);
                return; // 不抛异常，支付已成功
            }

            Boolean isGroupBuy = isGroupBuyResult.getData();
            if (isGroupBuy != null && isGroupBuy) {
                // 3. 拼团订单，调用GroupBuyService支付回调
                log.info("检测到拼团订单，调用GroupBuyService支付回调: orderId={}", orderId);
                Result<Void> callbackResult = groupBuyServiceClient.paymentCallback(orderId);
                
                if (callbackResult.getCode() == 200) {
                    log.info("GroupBuyService支付回调成功: orderId={}", orderId);
                } else {
                    log.error("GroupBuyService支付回调失败: orderId={}, msg={}", 
                             orderId, callbackResult.getMessage());
                    // 不抛异常，后续可通过补偿任务修复
                }
            } else {
                log.info("普通订单，无需回调GroupBuyService: orderId={}", orderId);
            }

            log.info("支付成功后处理完成: orderId={}, payId={}", orderId, payId);
        } catch (Exception e) {
            log.error("支付成功后处理异常: orderId={}", orderId, e);
            throw e;
        }
    }

    /**
     * 申请退款（供OrderService/GroupBuyService调用）⭐⭐⭐⭐
     * 
     * <p>流程：
     * <ol>
     *   <li>查询原支付记录</li>
     *   <li>验证是否已退款（防重复）</li>
     *   <li>创建退款记录（amount为负数）</li>
     *   <li>根据原支付方式退款：
     *     <ul>
     *       <li>余额支付：调用UserService增加余额，立即完成</li>
     *       <li>微信支付：调用微信退款API，异步等待回调</li>
     *       <li>支付宝支付：调用支付宝退款API，异步等待回调</li>
     *     </ul>
     *   </li>
     * </ol>
     * 
     * @param request 退款请求
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "申请退款", module = "支付管理")
    public void refund(RefundRequest request) {
        log.info("开始处理退款: orderId={}, reason={}", request.getOrderId(), request.getReason());

        // 1. 查询原支付记录
        PaymentRecord originalRecord = paymentRepository.findByOrderId(request.getOrderId())
            .orElseThrow(() -> new BusinessException("未找到支付记录"));

        // 2. 验证是否已退款
        if (originalRecord.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            log.warn("订单已退款，无需重复操作: orderId={}", request.getOrderId());
            return;
        }

        // 3. 创建退款记录（amount为负数）
        PaymentRecord refundRecord = new PaymentRecord();
        refundRecord.setUserId(originalRecord.getUserId());
        refundRecord.setOrderId(request.getOrderId());
        refundRecord.setPayType(originalRecord.getPayType());
        refundRecord.setAmount(originalRecord.getAmount().negate()); // 负数
        refundRecord.setPayStatus(PayStatus.FAILED.getCode());
        
        // 生成签名
        String signContent = originalRecord.getUserId() + "_" + request.getOrderId() + "_" + 
                           refundRecord.getAmount();
        String sign = securityUtil.sha256Sign(signContent);
        refundRecord.setEncryptSign(sign);

        paymentRepository.save(refundRecord);
        log.info("退款记录已创建: payId={}", refundRecord.getPayId());

        // 4. 根据原支付方式退款
        if (originalRecord.getPayType().equals(PayType.BALANCE.getCode())) {
            // 余额退款
            handleBalanceRefund(refundRecord, originalRecord.getUserId());
        } else if (originalRecord.getPayType().equals(PayType.WECHAT.getCode())) {
            // 微信退款（TODO: 实际项目需接入微信退款API）
            log.info("微信退款暂未实现");
            throw new BusinessException("微信退款功能暂未开放");
        } else if (originalRecord.getPayType().equals(PayType.ALIPAY.getCode())) {
            // 支付宝退款（TODO: 实际项目需接入支付宝退款API）
            log.info("支付宝退款暂未实现");
            throw new BusinessException("支付宝退款功能暂未开放");
        }
    }

    /**
     * 处理余额退款
     */
    private void handleBalanceRefund(PaymentRecord refundRecord, Long userId) {
        log.info("开始余额退款: userId={}, amount={}", userId, refundRecord.getAmount().abs());

        try {
            // 调用UserService增加余额
            Result<Void> rechargeResult = userServiceClient.recharge(userId, refundRecord.getAmount().abs());
            
            if (rechargeResult.getCode() == 200) {
                // 退款成功
                refundRecord.setPayStatus(PayStatus.SUCCESS.getCode());
                refundRecord.setTransactionId("REFUND_" + System.currentTimeMillis());
                paymentRepository.save(refundRecord);
                
                log.info("余额退款成功: payId={}, userId={}", refundRecord.getPayId(), userId);
            } else {
                log.error("余额退款失败: userId={}, msg={}", userId, rechargeResult.getMessage());
                throw new BusinessException("退款失败: " + rechargeResult.getMessage());
            }
        } catch (Exception e) {
            log.error("余额退款异常: userId={}", userId, e);
            throw new BusinessException("退款失败: " + e.getMessage());
        }
    }

    /**
     * 查询支付记录
     */
    public PaymentRecord getPaymentRecord(Long payId) {
        return paymentRepository.findById(payId)
            .orElseThrow(() -> new BusinessException("支付记录不存在"));
    }

    /**
     * 查询订单的支付记录
     */
    public PaymentRecord getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new BusinessException("未找到订单的支付记录"));
    }

    /**
     * 余额充值（⭐简单实现版本）
     * 
     * <p>说明：当前为简化版本，直接调用UserService增加余额
     * <p>TODO: 后续集成微信支付/支付宝支付后，充值需要先创建支付订单，等待支付回调
     * 
     * @param userId 用户ID
     * @param amount 充值金额
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "余额充值", module = "支付管理")
    public PaymentRecord recharge(Long userId, BigDecimal amount) {
        log.info("开始余额充值: userId={}, amount={}", userId, amount);

        // 1. 创建充值记录（order_id为null）
        PaymentRecord record = new PaymentRecord();
        record.setUserId(userId);
        record.setOrderId(null); // 充值记录没有订单ID
        record.setPayType(PayType.BALANCE.getCode()); // 简化版本标记为余额类型
        record.setAmount(amount);
        record.setPayStatus(PayStatus.FAILED.getCode());
        
        // 生成签名
        String signContent = userId + "_RECHARGE_" + amount;
        String sign = securityUtil.sha256Sign(signContent);
        record.setEncryptSign(sign);

        PaymentRecord savedRecord = paymentRepository.save(record);
        log.info("充值记录已创建: payId={}", savedRecord.getPayId());

        try {
            // 2. 调用UserService增加余额
            Result<Void> rechargeResult = userServiceClient.recharge(userId, amount);
            
            if (rechargeResult.getCode() == 200) {
                // 充值成功
                savedRecord.setPayStatus(PayStatus.SUCCESS.getCode());
                savedRecord.setTransactionId("RECHARGE_" + System.currentTimeMillis());
                paymentRepository.save(savedRecord);
                
                log.info("余额充值成功: payId={}, userId={}, amount={}", 
                         savedRecord.getPayId(), userId, amount);
                return savedRecord;
            } else {
                log.error("余额充值失败: userId={}, msg={}", userId, rechargeResult.getMessage());
                throw new BusinessException("充值失败: " + rechargeResult.getMessage());
            }
        } catch (Exception e) {
            log.error("余额充值异常: userId={}", userId, e);
            throw new BusinessException("充值失败: " + e.getMessage());
        }
    }

    /**
     * 查询用户的所有支付记录（包含充值记录）
     */
    public List<PaymentRecord> getUserPaymentRecords(Long userId) {
        return paymentRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    /**
     * 查询用户的充值记录
     */
    public List<PaymentRecord> getUserRechargeRecords(Long userId) {
        return paymentRepository.findByUserIdAndOrderIdIsNullOrderByCreateTimeDesc(userId);
    }

    // ==================== 管理端方法 ====================

    /**
     * 查询所有用户的支付记录（管理端）
     * 
     * <p>支持多条件筛选：
     * <ul>
     *   <li>支付方式（payType）</li>
     *   <li>支付状态（payStatus）</li>
     *   <li>记录类型（recordType: recharge/payment/refund）</li>
     *   <li>关键词搜索（keyword: 订单号/交易号）</li>
     *   <li>日期范围（startTime/endTime）</li>
     * </ul>
     * 
     * @param pageable 分页参数
     * @param payType 支付方式（可选）
     * @param payStatus 支付状态（可选）
     * @param recordType 记录类型（可选：recharge/payment/refund）
     * @param keyword 搜索关键词（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 分页的支付记录列表
     */
    public Page<PaymentRecord> getAdminPaymentRecords(Pageable pageable, 
                                                       Integer payType, 
                                                       Integer payStatus,
                                                       String recordType, 
                                                       String keyword,
                                                       LocalDateTime startTime, 
                                                       LocalDateTime endTime) {
        log.info("管理端查询支付记录: payType={}, payStatus={}, recordType={}, keyword={}", 
                 payType, payStatus, recordType, keyword);

        // 构建动态查询条件
        Specification<PaymentRecord> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 支付方式筛选
            if (payType != null) {
                predicates.add(criteriaBuilder.equal(root.get("payType"), payType));
            }

            // 支付状态筛选
            if (payStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("payStatus"), payStatus));
            }

            // 记录类型筛选
            if (recordType != null) {
                switch (recordType) {
                    case "recharge":
                        // 充值记录：orderId为null，amount为正
                        predicates.add(criteriaBuilder.isNull(root.get("orderId")));
                        predicates.add(criteriaBuilder.greaterThan(root.get("amount"), BigDecimal.ZERO));
                        break;
                    case "payment":
                        // 支付记录：orderId不为null，amount为正
                        predicates.add(criteriaBuilder.isNotNull(root.get("orderId")));
                        predicates.add(criteriaBuilder.greaterThan(root.get("amount"), BigDecimal.ZERO));
                        break;
                    case "refund":
                        // 退款记录：amount为负
                        predicates.add(criteriaBuilder.lessThan(root.get("amount"), BigDecimal.ZERO));
                        break;
                }
            }

            // 关键词搜索（订单号或交易号）
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword.trim() + "%";
                Predicate orderIdPredicate = criteriaBuilder.like(
                    criteriaBuilder.toString(root.get("orderId")), likeKeyword);
                Predicate transactionIdPredicate = criteriaBuilder.like(
                    root.get("transactionId"), likeKeyword);
                predicates.add(criteriaBuilder.or(orderIdPredicate, transactionIdPredicate));
            }

            // 日期范围筛选
            if (startTime != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), startTime));
            }
            if (endTime != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), endTime));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return paymentRepository.findAll(spec, pageable);
    }

    /**
     * 支付统计（管理端）
     * 
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 统计数据
     */
    public PaymentStatistics getPaymentStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("管理端查询支付统计: startTime={}, endTime={}", startTime, endTime);

        PaymentStatistics statistics = new PaymentStatistics();

        // 1. 总记录数
        Long total = paymentRepository.countByCreateTimeBetween(startTime, endTime);
        statistics.setTotal(total);

        // 2. 充值统计
        Long rechargeCount = paymentRepository.countRechargeRecords(startTime, endTime);
        BigDecimal rechargeAmount = paymentRepository.sumRechargeAmount(startTime, endTime);
        statistics.setRechargeCount(rechargeCount);
        statistics.setRechargeAmount(rechargeAmount);

        // 3. 支付统计（订单支付）
        Long paymentCount = paymentRepository.countPaymentRecords(startTime, endTime);
        BigDecimal paymentAmount = paymentRepository.sumPaymentAmount(startTime, endTime);
        statistics.setPaymentCount(paymentCount);
        statistics.setPaymentAmount(paymentAmount);

        // 4. 退款统计
        Long refundCount = paymentRepository.countRefundRecords(startTime, endTime);
        BigDecimal refundAmount = paymentRepository.sumRefundAmount(startTime, endTime);
        statistics.setRefundCount(refundCount);
        statistics.setRefundAmount(refundAmount);

        // 5. 总交易金额（充值 + 支付 + 退款）
        BigDecimal totalAmount = rechargeAmount.add(paymentAmount).add(refundAmount);
        statistics.setTotalAmount(totalAmount);

        // 6. 支付成功率
        if (total > 0) {
            Long successCount = paymentRepository.countSuccessRecords(startTime, endTime);
            double successRate = (successCount.doubleValue() / total.doubleValue()) * 100;
            statistics.setSuccessRate(BigDecimal.valueOf(successRate)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());
        }

        // 7. 各支付方式占比
        if (total > 0) {
            // 余额支付
            Long balanceCount = paymentRepository.countByPayType(PayType.BALANCE.getCode(), startTime, endTime);
            double balanceRate = (balanceCount.doubleValue() / total.doubleValue()) * 100;
            statistics.setBalancePayRate(BigDecimal.valueOf(balanceRate)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());

            // 微信支付
            Long wechatCount = paymentRepository.countByPayType(PayType.WECHAT.getCode(), startTime, endTime);
            double wechatRate = (wechatCount.doubleValue() / total.doubleValue()) * 100;
            statistics.setWechatPayRate(BigDecimal.valueOf(wechatRate)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());

            // 支付宝支付
            Long alipayCount = paymentRepository.countByPayType(PayType.ALIPAY.getCode(), startTime, endTime);
            double alipayRate = (alipayCount.doubleValue() / total.doubleValue()) * 100;
            statistics.setAlipayPayRate(BigDecimal.valueOf(alipayRate)
                    .setScale(2, RoundingMode.HALF_UP).doubleValue());
        }

        log.info("统计完成: total={}, recharge={}, payment={}, refund={}", 
                 total, rechargeCount, paymentCount, refundCount);

        return statistics;
    }
}

