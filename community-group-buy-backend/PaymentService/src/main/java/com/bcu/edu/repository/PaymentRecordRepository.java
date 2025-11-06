package com.bcu.edu.repository;

import com.bcu.edu.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 支付记录Repository
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long>, JpaSpecificationExecutor<PaymentRecord> {

    /**
     * 根据订单ID查询支付记录
     */
    Optional<PaymentRecord> findByOrderId(Long orderId);

    /**
     * 根据第三方流水号查询
     */
    Optional<PaymentRecord> findByTransactionId(String transactionId);

    /**
     * 根据微信流水号查询
     */
    Optional<PaymentRecord> findByWxTransactionId(String wxTransactionId);

    /**
     * 查询用户的所有支付记录
     */
    List<PaymentRecord> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 查询用户的充值记录（order_id为null）
     */
    List<PaymentRecord> findByUserIdAndOrderIdIsNullOrderByCreateTimeDesc(Long userId);

    /**
     * 根据用户ID和支付状态查询
     */
    List<PaymentRecord> findByUserIdAndPayStatusOrderByCreateTimeDesc(Long userId, Integer payStatus);

    // ==================== 管理端查询方法 ====================

    /**
     * 查询指定时间范围内的支付记录数
     */
    @Query("SELECT COUNT(p) FROM PaymentRecord p WHERE " +
           "(:startTime IS NULL OR p.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR p.createTime <= :endTime)")
    Long countByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的充值记录数
     */
    @Query("SELECT COUNT(p) FROM PaymentRecord p WHERE p.orderId IS NULL AND " +
           "p.amount > 0 AND " +
           "(:startTime IS NULL OR p.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR p.createTime <= :endTime)")
    Long countRechargeRecords(@Param("startTime") LocalDateTime startTime, 
                              @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的充值总额
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentRecord p WHERE p.orderId IS NULL AND " +
           "p.amount > 0 AND p.payStatus = 1 AND " +
           "(:startTime IS NULL OR p.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR p.createTime <= :endTime)")
    BigDecimal sumRechargeAmount(@Param("startTime") LocalDateTime startTime, 
                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的支付记录数（订单支付）
     */
    @Query("SELECT COUNT(p) FROM PaymentRecord p WHERE p.orderId IS NOT NULL AND " +
           "p.amount > 0 AND " +
           "(:startTime IS NULL OR p.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR p.createTime <= :endTime)")
    Long countPaymentRecords(@Param("startTime") LocalDateTime startTime, 
                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的支付总额（订单支付）
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentRecord p WHERE p.orderId IS NOT NULL AND " +
           "p.amount > 0 AND p.payStatus = 1 AND " +
           "(:startTime IS NULL OR p.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR p.createTime <= :endTime)")
    BigDecimal sumPaymentAmount(@Param("startTime") LocalDateTime startTime, 
                                @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的退款记录数
     */
    @Query("SELECT COUNT(p) FROM PaymentRecord p WHERE p.amount < 0 AND " +
           "(:startTime IS NULL OR p.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR p.createTime <= :endTime)")
    Long countRefundRecords(@Param("startTime") LocalDateTime startTime, 
                            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的退款总额
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentRecord p WHERE p.amount < 0 AND " +
           "p.payStatus = 1 AND " +
           "(:startTime IS NULL OR p.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR p.createTime <= :endTime)")
    BigDecimal sumRefundAmount(@Param("startTime") LocalDateTime startTime, 
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定时间范围内的支付成功记录数
     */
    @Query("SELECT COUNT(p) FROM PaymentRecord p WHERE p.payStatus = 1 AND " +
           "(:startTime IS NULL OR p.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR p.createTime <= :endTime)")
    Long countSuccessRecords(@Param("startTime") LocalDateTime startTime, 
                             @Param("endTime") LocalDateTime endTime);

    /**
     * 按支付方式统计记录数
     */
    @Query("SELECT COUNT(p) FROM PaymentRecord p WHERE p.payType = :payType AND " +
           "p.payStatus = 1 AND " +
           "(:startTime IS NULL OR p.createTime >= :startTime) AND " +
           "(:endTime IS NULL OR p.createTime <= :endTime)")
    Long countByPayType(@Param("payType") Integer payType,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);
}

