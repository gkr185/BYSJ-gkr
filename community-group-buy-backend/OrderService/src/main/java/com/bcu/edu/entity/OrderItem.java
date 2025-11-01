package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单明细表实体（严格匹配order_service_db.sql）
 * 
 * <p>表名: order_item
 * <p>说明: 存储订单商品明细（快照设计）
 * 
 * <p>快照设计：
 * <ul>
 *   <li>product_name: 商品名称（下单时快照）</li>
 *   <li>product_img: 商品图片（下单时快照）</li>
 *   <li>price: 购买单价（拼团价/原价）</li>
 * </ul>
 * 
 * <p>快照目的：避免商品信息变更影响历史订单
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Entity
@Table(name = "order_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    /**
     * 关联订单ID
     */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    /**
     * 关联商品ID（跨库关联product_service_db.product）
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 关联拼团活动ID（非拼团为null，跨库关联groupbuy_service_db.group_buy）
     */
    @Column(name = "activity_id")
    private Long activityId;

    /**
     * 商品名称（下单时快照）
     */
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    /**
     * 商品图片（下单时快照）
     */
    @Column(name = "product_img", length = 255)
    private String productImg;

    /**
     * 购买单价（拼团价/原价）
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * 购买数量
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 小计金额（price×quantity）
     */
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
}

