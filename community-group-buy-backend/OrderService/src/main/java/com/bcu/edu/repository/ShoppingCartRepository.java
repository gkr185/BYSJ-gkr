package com.bcu.edu.repository;

import com.bcu.edu.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 购物车Repository
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    /**
     * 查询用户购物车
     */
    List<ShoppingCart> findByUserId(Long userId);

    /**
     * 查询用户指定商品的购物车项（支持拼团/普通商品）
     */
    Optional<ShoppingCart> findByUserIdAndProductIdAndActivityId(
        Long userId, Long productId, Long activityId);

    /**
     * 清空用户购物车
     */
    void deleteByUserId(Long userId);

    /**
     * 统计用户购物车商品数量
     */
    long countByUserId(Long userId);

    /**
     * 批量删除购物车项
     */
    @Modifying
    @Query("DELETE FROM ShoppingCart sc WHERE sc.cartId IN :cartIds AND sc.userId = :userId")
    void deleteByCartIdsAndUserId(@Param("cartIds") List<Long> cartIds, 
                                   @Param("userId") Long userId);
}

