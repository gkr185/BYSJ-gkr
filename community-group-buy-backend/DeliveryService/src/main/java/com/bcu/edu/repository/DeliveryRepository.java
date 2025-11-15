package com.bcu.edu.repository;

import com.bcu.edu.entity.DeliveryEntity;
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
 * 配送单Repository
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Repository
public interface DeliveryRepository extends JpaRepository<DeliveryEntity, Long> {

    /**
     * 根据分单组查询配送单
     */
    Optional<DeliveryEntity> findByDispatchGroup(String dispatchGroup);

    /**
     * 根据状态查询配送单列表
     */
    List<DeliveryEntity> findByStatus(Integer status);

    /**
     * 根据团长ID查询配送单列表
     */
    List<DeliveryEntity> findByLeaderId(Long leaderId);

    /**
     * 根据状态分页查询配送单
     */
    Page<DeliveryEntity> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据创建人查询配送单列表
     */
    List<DeliveryEntity> findByCreatedBy(Long createdBy);

    /**
     * 根据仓库ID查询配送单列表
     */
    List<DeliveryEntity> findByWarehouseId(Long warehouseId);

    /**
     * 查询指定时间范围内的配送单
     */
    @Query("SELECT d FROM DeliveryEntity d WHERE d.createTime BETWEEN :startTime AND :endTime")
    List<DeliveryEntity> findByCreateTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计配送单总数
     */
    @Query("SELECT COUNT(d) FROM DeliveryEntity d WHERE d.status = :status")
    Long countByStatus(@Param("status") Integer status);

    /**
     * 统计指定时间范围内的配送单数量
     */
    @Query("SELECT COUNT(d) FROM DeliveryEntity d WHERE d.createTime BETWEEN :startTime AND :endTime")
    Long countByCreateTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询所有已完成配送单的总距离
     */
    @Query("SELECT SUM(d.distance) FROM DeliveryEntity d WHERE d.status = 2")
    Double sumDistanceByCompleted();

    /**
     * 按团长统计配送单数量
     */
    @Query("SELECT d.leaderId, COUNT(d) FROM DeliveryEntity d WHERE d.leaderId IS NOT NULL GROUP BY d.leaderId")
    List<Object[]> countGroupByLeaderId();

    /**
     * 查询最近的配送单（分页）
     */
    Page<DeliveryEntity> findAllByOrderByCreateTimeDesc(Pageable pageable);
}

