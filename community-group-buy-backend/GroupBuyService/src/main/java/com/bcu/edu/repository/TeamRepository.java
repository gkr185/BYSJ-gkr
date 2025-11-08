package com.bcu.edu.repository;

import com.bcu.edu.entity.GroupBuyTeam;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 团实例Repository（⭐核心Repository）
 * 
 * <p>包含关键方法：
 * <ul>
 *   <li>行锁查询（SELECT ... FOR UPDATE）防止并发冲突</li>
 *   <li>社区优先排序查询（v3.0特性）</li>
 *   <li>过期团查询（定时任务使用）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Repository
public interface TeamRepository extends JpaRepository<GroupBuyTeam, Long> {
    
    /**
     * 查询团并加行锁（⭐核心方法）
     * 
     * <p>使用悲观锁（PESSIMISTIC_WRITE）防止并发冲突
     * <p>应用场景：
     * <ul>
     *   <li>用户参团时检查团状态</li>
     *   <li>支付回调更新团人数</li>
     *   <li>定时任务标记团失败</li>
     * </ul>
     * 
     * @param teamId 团ID
     * @return Optional<GroupBuyTeam>
     */
    @Query("SELECT t FROM GroupBuyTeam t WHERE t.teamId = :teamId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GroupBuyTeam> findByIdForUpdate(@Param("teamId") Long teamId);
    
    /**
     * 查询过期的团ID列表（定时任务使用）
     * 
     * <p>条件：team_status=0（拼团中）且expire_time < now
     * 
     * @param now 当前时间
     * @return 过期团ID列表
     */
    @Query("SELECT t.teamId FROM GroupBuyTeam t WHERE t.teamStatus = 0 AND t.expireTime < :now")
    List<Long> findExpiredTeamIds(@Param("now") LocalDateTime now);
    
    /**
     * 查询活动的团列表（社区优先排序）⭐v3.0核心
     * 
     * <p>排序规则：
     * <ol>
     *   <li>优先显示本社区的团（community_id匹配）</li>
     *   <li>然后显示其他社区的团</li>
     *   <li>同一优先级内按创建时间倒序</li>
     * </ol>
     * 
     * <p>使用原生SQL实现ORDER BY CASE
     * 
     * @param activityId 活动ID
     * @param communityId 用户的社区ID（为null时传0）
     * @param limit 返回数量限制
     * @return 团列表
     */
    @Query(value = """
        SELECT * FROM group_buy_team t
        WHERE t.activity_id = :activityId
          AND t.team_status = 0
          AND t.expire_time > NOW()
        ORDER BY 
          CASE WHEN t.community_id = :communityId THEN 0 ELSE 1 END ASC,
          t.create_time DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<GroupBuyTeam> findByActivityIdWithCommunityPriority(
        @Param("activityId") Long activityId,
        @Param("communityId") Long communityId,
        @Param("limit") int limit
    );
    
    /**
     * 查询活动的团列表（支持状态筛选和过期筛选）⭐管理端使用
     * 
     * <p>排序规则：
     * <ol>
     *   <li>优先显示本社区的团（community_id匹配）</li>
     *   <li>然后显示其他社区的团</li>
     *   <li>同一优先级内按创建时间倒序</li>
     * </ol>
     * 
     * @param activityId 活动ID
     * @param communityId 用户的社区ID（为null时传0）
     * @param status 团状态（null表示不限制状态）
     * @param includeExpired 是否包含已过期的团
     * @param limit 返回数量限制
     * @return 团列表
     */
    @Query(value = """
        SELECT * FROM group_buy_team t
        WHERE t.activity_id = :activityId
          AND (:status IS NULL OR t.team_status = :status)
          AND (:includeExpired = true OR t.expire_time > NOW())
        ORDER BY 
          CASE WHEN t.community_id = :communityId THEN 0 ELSE 1 END ASC,
          t.create_time DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<GroupBuyTeam> findByActivityIdWithFilters(
        @Param("activityId") Long activityId,
        @Param("communityId") Long communityId,
        @Param("status") Integer status,
        @Param("includeExpired") boolean includeExpired,
        @Param("limit") int limit
    );
    
    /**
     * 查询用户发起的团（按创建时间倒序）
     */
    List<GroupBuyTeam> findByLauncherIdOrderByCreateTimeDesc(Long launcherId);
    
    /**
     * 查询团长的所有团（按创建时间倒序）
     */
    List<GroupBuyTeam> findByLeaderIdOrderByCreateTimeDesc(Long leaderId);
    
    /**
     * 查询团长的团（带状态筛选，分页）
     * 
     * @param leaderId 团长ID
     * @param status 团状态（0-拼团中, 1-已成团, 2-已失败）
     * @param limit 返回数量
     * @param offset 偏移量
     * @return 团列表
     */
    @Query(value = """
        SELECT * FROM group_buy_team t
        WHERE t.leader_id = :leaderId
          AND (:status IS NULL OR t.team_status = :status)
        ORDER BY t.create_time DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<GroupBuyTeam> findByLeaderIdWithFilter(
        @Param("leaderId") Long leaderId,
        @Param("status") Integer status,
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    /**
     * 统计团长的团数量（带状态筛选）
     * 
     * @param leaderId 团长ID
     * @param status 团状态（null表示全部）
     * @return 数量
     */
    @Query("SELECT COUNT(t) FROM GroupBuyTeam t WHERE t.leaderId = :leaderId AND (:status IS NULL OR t.teamStatus = :status)")
    long countByLeaderIdAndStatus(@Param("leaderId") Long leaderId, @Param("status") Integer status);
    
    /**
     * 查询活动的所有团（不限状态）
     */
    List<GroupBuyTeam> findByActivityIdOrderByCreateTimeDesc(Long activityId);
    
    /**
     * 根据团号查询
     */
    Optional<GroupBuyTeam> findByTeamNo(String teamNo);
}

