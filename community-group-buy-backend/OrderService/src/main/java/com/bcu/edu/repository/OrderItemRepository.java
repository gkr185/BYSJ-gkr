package com.bcu.edu.repository;

import com.bcu.edu.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单明细Repository
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 根据订单ID查询所有订单项
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 根据商品ID查询订单项（统计商品销量）
     */
    List<OrderItem> findByProductId(Long productId);

    /**
     * 根据活动ID查询订单项（统计拼团活动销量）
     */
    List<OrderItem> findByActivityId(Long activityId);

    /**
     * 统计商品总销量
     */
    @Query("SELECT SUM(o.quantity) FROM OrderItem o WHERE o.productId = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    /**
     * 统计拼团活动总销量
     */
    @Query("SELECT SUM(o.quantity) FROM OrderItem o WHERE o.activityId = :activityId")
    Integer sumQuantityByActivityId(@Param("activityId") Long activityId);

    /**
     * 删除订单的所有明细
     */
    void deleteByOrderId(Long orderId);
}

