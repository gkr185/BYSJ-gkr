package com.bcu.edu.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 购物车表实体（严格匹配order_service_db.sql）
 * 
 * <p>表名: shopping_cart
 * <p>说明: 存储用户待购商品，支持拼团/普通商品混存
 * 
 * <p>业务规则：
 * <ul>
 *   <li>activity_id为NULL时表示普通购买</li>
 *   <li>activity_id不为NULL时表示拼团购买</li>
 *   <li>唯一索引：(user_id, product_id, activity_id) 防止重复添加</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Entity
@Table(name = "shopping_cart", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_product_activity", 
                      columnNames = {"user_id", "product_id", "activity_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    /**
     * 关联用户ID（跨库关联user_service_db.sys_user）
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 关联商品ID（跨库关联product_service_db.product）
     */
    @Column(name = "product_id", nullable = false)
    private Long productId;

    /**
     * 关联拼团活动ID（非拼团商品为null，跨库关联groupbuy_service_db.group_buy）
     */
    @Column(name = "activity_id")
    private Long activityId;

    /**
     * 数量
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    /**
     * 添加时间
     */
    @CreatedDate
    @Column(name = "add_time", nullable = false, updatable = false)
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}

