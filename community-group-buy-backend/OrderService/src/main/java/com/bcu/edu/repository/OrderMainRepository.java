package com.bcu.edu.repository;

import com.bcu.edu.entity.OrderMain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单主表Repository
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Repository
public interface OrderMainRepository extends JpaRepository<OrderMain, Long> {

    /**
     * 根据订单编号查询
     */
    Optional<OrderMain> findByOrderSn(String orderSn);

    /**
     * 查询用户订单（分页）
     */
    Page<OrderMain> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    /**
     * 查询团长订单（分页）
     */
    Page<OrderMain> findByLeaderIdOrderByCreateTimeDesc(Long leaderId, Pageable pageable);

    /**
     * 根据状态查询订单
     */
    List<OrderMain> findByOrderStatus(Integer orderStatus);

    /**
     * 查询超时未支付订单
     * 
     * @param expireTime 过期时间点
     * @return 超时订单列表
     */
    @Query("SELECT o FROM OrderMain o WHERE o.orderStatus = 0 " +
           "AND o.payStatus = 0 AND o.createTime < :expireTime")
    List<OrderMain> findExpiredOrders(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 查询用户指定状态的订单
     */
    Page<OrderMain> findByUserIdAndOrderStatusOrderByCreateTimeDesc(
        Long userId, Integer orderStatus, Pageable pageable);

    /**
     * 统计用户订单数量
     */
    long countByUserId(Long userId);

    /**
     * 统计团长订单数量
     */
    long countByLeaderId(Long leaderId);

    /**
     * 查询所有订单（分页，按创建时间倒序）
     */
    Page<OrderMain> findAllByOrderByCreateTimeDesc(Pageable pageable);

    /**
     * 根据订单状态分页查询
     */
    Page<OrderMain> findByOrderStatusOrderByCreateTimeDesc(Integer orderStatus, Pageable pageable);

    /**
     * 根据订单状态和支付状态分页查询
     */
    Page<OrderMain> findByOrderStatusAndPayStatusOrderByCreateTimeDesc(
        Integer orderStatus, Integer payStatus, Pageable pageable);

    /**
     * 根据支付状态分页查询
     */
    Page<OrderMain> findByPayStatusOrderByCreateTimeDesc(Integer payStatus, Pageable pageable);

    /**
     * 统计指定状态的订单数量
     */
    long countByOrderStatus(Integer orderStatus);

    /**
     * 统计今日订单数量
     */
    @Query("SELECT COUNT(o) FROM OrderMain o WHERE o.createTime >= :startTime")
    long countTodayOrders(@Param("startTime") LocalDateTime startTime);

    /**
     * 统计今日销售额
     */
    @Query("SELECT COALESCE(SUM(o.payAmount), 0) FROM OrderMain o " +
           "WHERE o.createTime >= :startTime AND o.payStatus = 1")
    java.math.BigDecimal sumTodaySales(@Param("startTime") LocalDateTime startTime);

    /**
     * 统计总销售额
     */
    @Query("SELECT COALESCE(SUM(o.payAmount), 0) FROM OrderMain o WHERE o.payStatus = 1")
    java.math.BigDecimal sumTotalSales();

    /**
     * 搜索订单（根据订单号）
     */
    @Query("SELECT o FROM OrderMain o WHERE o.orderSn LIKE %:keyword% ORDER BY o.createTime DESC")
    Page<OrderMain> searchByOrderSn(@Param("keyword") String keyword, Pageable pageable);
}

