package com.bcu.edu.repository;

import com.bcu.edu.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 支付记录Repository
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

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
}

