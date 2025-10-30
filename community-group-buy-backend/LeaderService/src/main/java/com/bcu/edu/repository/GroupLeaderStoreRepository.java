package com.bcu.edu.repository;

import com.bcu.edu.entity.GroupLeaderStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 团长团点Repository
 *
 * @author 耿康瑞
 * @date 2025-10-30
 */
@Repository
public interface GroupLeaderStoreRepository extends JpaRepository<GroupLeaderStore, Long> {

    /**
     * 根据团长ID查询团点
     * （一个用户只能有一个团点）
     */
    Optional<GroupLeaderStore> findByLeaderId(Long leaderId);

    /**
     * 根据社区ID查询所有团点
     */
    List<GroupLeaderStore> findByCommunityId(Long communityId);

    /**
     * 根据状态查询团点列表
     * @param status 0-待审核 1-正常运营 2-已停用
     */
    List<GroupLeaderStore> findByStatusOrderByCreatedAtDesc(Integer status);

    /**
     * 查询所有正常运营的团点
     */
    @Query("SELECT s FROM GroupLeaderStore s WHERE s.status = 1 ORDER BY s.createdAt DESC")
    List<GroupLeaderStore> findAllActiveStores();

    /**
     * 查询所有待审核的团长申请
     */
    @Query("SELECT s FROM GroupLeaderStore s WHERE s.status = 0 ORDER BY s.createdAt ASC")
    List<GroupLeaderStore> findAllPendingApplications();

    /**
     * 检查用户是否已是团长（包含待审核、正常运营）
     */
    @Query("SELECT COUNT(s) > 0 FROM GroupLeaderStore s WHERE s.leaderId = :leaderId AND s.status IN (0, 1)")
    boolean existsByLeaderIdAndActiveStatus(Long leaderId);

    /**
     * 检查用户是否已是团长
     */
    boolean existsByLeaderId(Long leaderId);

    /**
     * 根据社区ID和状态查询团点
     */
    List<GroupLeaderStore> findByCommunityIdAndStatus(Long communityId, Integer status);
}

