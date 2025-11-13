package com.bcu.edu.repository;

import com.bcu.edu.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 配送单数据访问层
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    /**
     * 根据分单组查询配送单
     */
    Optional<Delivery> findByDispatchGroup(String dispatchGroup);

    /**
     * 根据团长ID查询配送单列表
     */
    List<Delivery> findByLeaderIdOrderByCreateTimeDesc(Long leaderId);

    /**
     * 根据状态查询配送单列表
     */
    List<Delivery> findByStatusOrderByCreateTimeDesc(Integer status);

    /**
     * 根据团长ID和状态查询配送单列表
     */
    List<Delivery> findByLeaderIdAndStatusOrderByCreateTimeDesc(Long leaderId, Integer status);

    /**
     * 查询指定时间范围内的配送单
     */
    @Query("SELECT d FROM Delivery d WHERE d.createTime BETWEEN :startTime AND :endTime ORDER BY d.createTime DESC")
    List<Delivery> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 查询团长指定时间范围内的配送单
     */
    @Query("SELECT d FROM Delivery d WHERE d.leaderId = :leaderId AND d.createTime BETWEEN :startTime AND :endTime ORDER BY d.createTime DESC")
    List<Delivery> findByLeaderIdAndCreateTimeBetween(@Param("leaderId") Long leaderId,
                                                     @Param("startTime") LocalDateTime startTime, 
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计团长的配送单数量
     */
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.leaderId = :leaderId")
    Long countByLeaderId(@Param("leaderId") Long leaderId);

    /**
     * 统计指定状态的配送单数量
     */
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.status = :status")
    Long countByStatus(@Param("status") Integer status);

    /**
     * 查询进行中的配送单（状态为配送中）
     */
    @Query("SELECT d FROM Delivery d WHERE d.status = 1 ORDER BY d.startTime ASC")
    List<Delivery> findDelivering();

    /**
     * 查询超时未完成的配送单
     */
    @Query("SELECT d FROM Delivery d WHERE d.status = 1 AND d.startTime IS NOT NULL AND d.startTime < :timeoutTime")
    List<Delivery> findTimeoutDeliveries(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 根据算法类型统计配送单数量
     */
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.algorithmUsed = :algorithm")
    Long countByAlgorithmUsed(@Param("algorithm") String algorithm);

    /**
     * 查询指定日期的配送统计
     */
    @Query("SELECT " +
           "COUNT(d) as totalCount, " +
           "AVG(d.distance) as avgDistance, " +
           "AVG(d.estimatedDuration) as avgDuration " +
           "FROM Delivery d WHERE DATE(d.createTime) = DATE(:date)")
    Object[] getDeliveryStatsByDate(@Param("date") LocalDateTime date);

    /**
     * 检查分单组是否已存在
     */
    boolean existsByDispatchGroup(String dispatchGroup);
}
