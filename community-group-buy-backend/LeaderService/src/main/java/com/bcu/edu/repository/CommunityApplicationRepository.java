package com.bcu.edu.repository;

import com.bcu.edu.entity.CommunityApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 社区申请Repository
 *
 * @author 耿康瑞
 * @date 2025-10-30
 */
@Repository
public interface CommunityApplicationRepository extends JpaRepository<CommunityApplication, Long> {

    /**
     * 根据申请人ID查询所有申请记录
     */
    List<CommunityApplication> findByApplicantIdOrderByCreatedAtDesc(Long applicantId);

    /**
     * 根据状态查询申请列表
     * @param status 0-待审核 1-审核通过 2-审核拒绝
     */
    List<CommunityApplication> findByStatusOrderByCreatedAtDesc(Integer status);

    /**
     * 查询所有待审核的申请
     */
    @Query("SELECT ca FROM CommunityApplication ca WHERE ca.status = 0 ORDER BY ca.createdAt ASC")
    List<CommunityApplication> findAllPendingApplications();

    /**
     * 检查申请人是否有待审核的申请
     */
    boolean existsByApplicantIdAndStatus(Long applicantId, Integer status);

    /**
     * 检查社区名称是否已被申请（待审核或已通过）
     */
    @Query("SELECT COUNT(ca) > 0 FROM CommunityApplication ca WHERE ca.communityName = :communityName AND ca.status IN (0, 1)")
    boolean existsByCommunityNameAndActiveStatus(String communityName);
}

