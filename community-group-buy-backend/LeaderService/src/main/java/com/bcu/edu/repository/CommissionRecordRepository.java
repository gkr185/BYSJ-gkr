package com.bcu.edu.repository;

import com.bcu.edu.entity.CommissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 佣金记录Repository
 *
 * @author 耿康瑞
 * @date 2025-10-30
 */
@Repository
public interface CommissionRecordRepository extends JpaRepository<CommissionRecord, Long> {

    /**
     * 根据团长ID查询所有佣金记录
     */
    List<CommissionRecord> findByLeaderIdOrderByCreatedAtDesc(Long leaderId);

    /**
     * 根据订单ID查询佣金记录
     * （一个订单只有一条佣金记录）
     */
    Optional<CommissionRecord> findByOrderId(Long orderId);

    /**
     * 根据状态查询佣金记录
     * @param status 0-待结算 1-已结算 2-结算失败
     */
    List<CommissionRecord> findByStatusOrderByCreatedAtDesc(Integer status);

    /**
     * 查询所有待结算的佣金记录
     */
    @Query("SELECT cr FROM CommissionRecord cr WHERE cr.status = 0 ORDER BY cr.createdAt ASC")
    List<CommissionRecord> findAllPendingRecords();

    /**
     * 根据团长ID和状态查询佣金记录
     */
    List<CommissionRecord> findByLeaderIdAndStatusOrderByCreatedAtDesc(Long leaderId, Integer status);

    /**
     * 根据结算批次号查询佣金记录
     */
    List<CommissionRecord> findBySettlementBatchOrderByCreatedAtDesc(String settlementBatch);

    /**
     * 统计团长的待结算佣金总额
     */
    @Query("SELECT SUM(cr.commissionAmount) FROM CommissionRecord cr WHERE cr.leaderId = :leaderId AND cr.status = 0")
    BigDecimal sumPendingCommissionByLeader(@Param("leaderId") Long leaderId);

    /**
     * 统计团长的已结算佣金总额
     */
    @Query("SELECT SUM(cr.commissionAmount) FROM CommissionRecord cr WHERE cr.leaderId = :leaderId AND cr.status = 1")
    BigDecimal sumSettledCommissionByLeader(@Param("leaderId") Long leaderId);

    /**
     * 根据时间范围查询团长的佣金记录
     */
    @Query("SELECT cr FROM CommissionRecord cr WHERE cr.leaderId = :leaderId AND cr.createdAt BETWEEN :startTime AND :endTime ORDER BY cr.createdAt DESC")
    List<CommissionRecord> findByLeaderIdAndDateRange(
            @Param("leaderId") Long leaderId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 检查订单是否已生成佣金记录
     */
    boolean existsByOrderId(Long orderId);
}

