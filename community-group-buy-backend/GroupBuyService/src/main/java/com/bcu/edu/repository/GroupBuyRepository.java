package com.bcu.edu.repository;

import com.bcu.edu.entity.GroupBuy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拼团活动Repository
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Repository
public interface GroupBuyRepository extends JpaRepository<GroupBuy, Long> {
    
    /**
     * 查询进行中的活动（status=1，且在活动时间范围内）
     */
    @Query("SELECT g FROM GroupBuy g WHERE g.status = 1 AND g.startTime <= :now AND g.endTime >= :now")
    List<GroupBuy> findOngoingActivities(@Param("now") LocalDateTime now);
    
    /**
     * 根据商品ID查询活动
     */
    List<GroupBuy> findByProductId(Long productId);
    
    /**
     * 根据状态查询活动
     */
    List<GroupBuy> findByStatusOrderByCreateTimeDesc(Integer status);
}

