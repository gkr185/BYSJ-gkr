package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体（严格匹配payment_service_db.sql）
 * 
 * <p>表名: payment_record
 * <p>说明: 记录支付/充值/退款明细，保障交易安全
 * 
 * <p>关键字段：
 * <ul>
 *   <li>pay_type: 1-微信；2-支付宝；3-余额</li>
 *   <li>pay_status: 0-失败；1-成功</li>
 *   <li>amount: 正数-支付/充值；负数-退款</li>
 *   <li>pay_callback_info: 支付回调信息（AES加密存储）</li>
 *   <li>encrypt_sign: 数据加密签名（防篡改，SHA256+盐值）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Entity
@Table(name = "payment_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PaymentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pay_id", nullable = false)
    private Long payId;

    /**
     * 关联用户ID（跨库关联user_service_db.sys_user）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 关联订单ID（充值时为null，跨库关联order_service_db.order_main）
     */
    @Column(name = "order_id")
    private Long orderId;

    /**
     * 支付类型
     * 1-微信；2-支付宝；3-余额
     */
    @Column(name = "pay_type", nullable = false)
    private Integer payType;

    /**
     * 金额（正数-支付/充值；负数-退款）
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * 状态（0-失败；1-成功）
     */
    @Column(name = "pay_status", nullable = false)
    private Integer payStatus = 0;

    /**
     * 第三方支付流水号（通用）
     */
    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    /**
     * 微信支付专用流水号
     */
    @Column(name = "wx_transaction_id", length = 100)
    private String wxTransactionId;

    /**
     * 支付回调信息（AES加密存储）
     */
    @Column(name = "pay_callback_info", columnDefinition = "TEXT")
    private String payCallbackInfo;

    /**
     * 数据加密签名（防篡改，SHA256+盐值）
     */
    @Column(name = "encrypt_sign", nullable = false, length = 255)
    private String encryptSign;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;
}

