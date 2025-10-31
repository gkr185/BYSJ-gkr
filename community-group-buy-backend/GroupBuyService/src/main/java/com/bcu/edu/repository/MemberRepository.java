package com.bcu.edu.repository;

import com.bcu.edu.entity.GroupBuyMember;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 参团记录Repository（⭐核心Repository）
 * 
 * <p>包含关键方法：
 * <ul>
 *   <li>防重复参团检查（唯一索引 uk_team_user）</li>
 *   <li>行锁查询（SELECT ... FOR UPDATE）保证幂等性</li>
 *   <li>状态查询（支持批量退款）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Repository
public interface MemberRepository extends JpaRepository<GroupBuyMember, Long> {
    
    /**
     * 检查用户是否已参团（防重复参团）⭐核心
     * 
     * <p>依赖唯一索引 uk_team_user (team_id, user_id)
     * 
     * @param teamId 团ID
     * @param userId 用户ID
     * @return true-已参团；false-未参团
     */
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
    
    /**
     * 通过订单ID查询并加行锁（⭐核心方法）
     * 
     * <p>使用悲观锁（PESSIMISTIC_WRITE）保证支付回调幂等性
     * <p>应用场景：
     * <ul>
     *   <li>支付回调时更新参团状态（UNPAID → PAID）</li>
     *   <li>防止重复回调导致重复成团</li>
     * </ul>
     * 
     * @param orderId 订单ID
     * @return Optional<GroupBuyMember>
     */
    @Query("SELECT m FROM GroupBuyMember m WHERE m.orderId = :orderId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GroupBuyMember> findByOrderIdForUpdate(@Param("orderId") Long orderId);
    
    /**
     * 查询团的所有成员（按参团时间升序）
     * 
     * @param teamId 团ID
     * @return 成员列表
     */
    List<GroupBuyMember> findByTeamIdOrderByJoinTimeAsc(Long teamId);
    
    /**
     * 查询用户参与的所有团（按参团时间倒序）
     * 
     * @param userId 用户ID
     * @return 参团记录列表
     */
    List<GroupBuyMember> findByUserIdOrderByJoinTimeDesc(Long userId);
    
    /**
     * 查询团的指定状态成员
     * 
     * <p>应用场景：
     * <ul>
     *   <li>定时任务退款时查询已支付成员（status=1）</li>
     *   <li>成团逻辑查询所有已支付成员</li>
     * </ul>
     * 
     * @param teamId 团ID
     * @param status 参团状态（0待支付/1已支付/2已成团/3已取消）
     * @return 成员列表
     */
    List<GroupBuyMember> findByTeamIdAndStatus(Long teamId, Integer status);
    
    /**
     * 根据订单ID查询参团记录
     */
    Optional<GroupBuyMember> findByOrderId(Long orderId);
    
    /**
     * 查询团的成员数量
     */
    @Query("SELECT COUNT(m) FROM GroupBuyMember m WHERE m.teamId = :teamId")
    Integer countByTeamId(@Param("teamId") Long teamId);
}

