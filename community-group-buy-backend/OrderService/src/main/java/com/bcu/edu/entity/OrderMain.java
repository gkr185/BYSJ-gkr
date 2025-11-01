package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体（严格匹配order_service_db.sql）
 * 
 * <p>表名: order_main
 * <p>说明: 存储订单核心信息，关联用户、团长与配送
 * 
 * <p>关键字段：
 * <ul>
 *   <li>order_sn: 订单编号（规则：yyyyMMddHHmmss + 6位随机数）</li>
 *   <li>order_status: 0-待支付；1-待发货；2-配送中；3-已送达；4-已取消；5-退款中；6-已退款</li>
 *   <li>pay_status: 0-未支付；1-已支付</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Entity
@Table(name = "order_main")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OrderMain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 下单用户ID（跨库关联user_service_db.sys_user）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 取货团长ID（跨库关联user_service_db.sys_user）
     */
    @Column(name = "leader_id", nullable = false)
    private Long leaderId;

    /**
     * 订单编号（规则：日期+随机数）
     */
    @Column(name = "order_sn", nullable = false, unique = true, length = 50)
    private String orderSn;

    /**
     * 商品总金额
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 优惠金额
     */
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * 实付金额（total - discount）
     */
    @Column(name = "pay_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal payAmount;

    /**
     * 订单状态
     * 0-待支付；1-待发货；2-配送中；3-已送达；4-已取消；5-退款中；6-已退款
     */
    @Column(name = "order_status", nullable = false)
    private Integer orderStatus = 0;

    /**
     * 支付状态
     * 0-未支付；1-已支付
     */
    @Column(name = "pay_status", nullable = false)
    private Integer payStatus = 0;

    /**
     * 支付时间
     */
    @Column(name = "pay_time")
    private LocalDateTime payTime;

    /**
     * 收货地址ID（跨库关联user_service_db.user_address）
     */
    @Column(name = "receive_address_id", nullable = false)
    private Long receiveAddressId;

    /**
     * 分单组标识（同批次配送订单）
     */
    @Column(name = "dispatch_group", length = 50)
    private String dispatchGroup;

    /**
     * 关联配送单ID（跨库关联delivery_service_db.delivery）
     */
    @Column(name = "delivery_id")
    private Long deliveryId;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}

