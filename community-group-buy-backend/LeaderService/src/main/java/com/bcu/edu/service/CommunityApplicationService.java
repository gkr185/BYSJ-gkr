package com.bcu.edu.service;

import com.bcu.edu.entity.Community;
import com.bcu.edu.entity.CommunityApplication;
import com.bcu.edu.entity.GroupLeaderStore;
import com.bcu.edu.repository.CommunityApplicationRepository;
import com.bcu.edu.repository.CommunityRepository;
import com.bcu.edu.repository.GroupLeaderStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 社区申请服务
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 核心功能：
 * 1. 社区申请提交
 * 2. 管理员审核
 * 3. 审核通过自动创建Community
 * 4. （预留）自动创建GroupLeaderStore记录
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityApplicationService {

    private final CommunityApplicationRepository applicationRepository;
    private final CommunityRepository communityRepository;
    private final GroupLeaderStoreRepository leaderStoreRepository;

    /**
     * 提交社区申请
     * 
     * 业务规则：
     * 1. 同一用户只能有1个待审核的申请
     * 2. 同一社区名称不能重复申请
     */
    @Transactional
    public CommunityApplication submitApplication(CommunityApplication application) {
        // 规则1：检查申请人是否有待审核的申请
        if (applicationRepository.existsByApplicantIdAndStatus(application.getApplicantId(), 0)) {
            throw new IllegalArgumentException("您已有待审核的申请，请等待审核结果");
        }

        // 规则2：检查社区名称是否已被申请或已存在
        if (applicationRepository.existsByCommunityNameAndActiveStatus(application.getCommunityName())) {
            throw new IllegalArgumentException("该社区名称已被申请或已存在");
        }

        if (communityRepository.existsByName(application.getCommunityName())) {
            throw new IllegalArgumentException("该社区名称已存在");
        }

        // 设置初始状态
        application.setStatus(0); // 0-待审核
        application.setCreatedAt(LocalDateTime.now());

        log.info("用户{}提交社区申请：{}", application.getApplicantId(), application.getCommunityName());
        return applicationRepository.save(application);
    }

    /**
     * 管理员审核申请
     * 
     * @param applicationId 申请ID
     * @param reviewerId 审核人ID（管理员）
     * @param approved 是否通过（true-通过，false-拒绝）
     * @param reviewComment 审核意见
     */
    @Transactional
    public CommunityApplication reviewApplication(
            Long applicationId,
            Long reviewerId,
            boolean approved,
            String reviewComment
    ) {
        CommunityApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在：" + applicationId));

        // 检查申请状态
        if (application.getStatus() != 0) {
            throw new IllegalStateException("该申请已被审核，无法重复审核");
        }

        // 更新审核信息
        application.setReviewerId(reviewerId);
        application.setReviewComment(reviewComment);
        application.setReviewedAt(LocalDateTime.now());

        if (approved) {
            // 审核通过
            application.setStatus(1);

            // 自动创建Community
            Community newCommunity = createCommunityFromApplication(application);
            application.setCreatedCommunityId(newCommunity.getCommunityId());

            // 自动创建GroupLeaderStore记录
            GroupLeaderStore leaderStore = createLeaderStoreFromApplication(application, newCommunity);

            log.info("审核通过，社区{}创建成功，申请人{}成为团长，团点ID：{}", 
                    newCommunity.getName(), application.getApplicantId(), leaderStore.getStoreId());
        } else {
            // 审核拒绝
            application.setStatus(2);
            log.info("审核拒绝，申请ID：{}，原因：{}", applicationId, reviewComment);
        }

        return applicationRepository.save(application);
    }

    /**
     * 根据申请自动创建社区
     */
    private Community createCommunityFromApplication(CommunityApplication application) {
        Community community = new Community();
        community.setName(application.getCommunityName());
        community.setAddress(application.getAddress());
        community.setProvince(application.getProvince());
        community.setCity(application.getCity());
        community.setDistrict(application.getDistrict());
        community.setLatitude(application.getLatitude());
        community.setLongitude(application.getLongitude());
        community.setServiceRadius(application.getServiceRadius());
        community.setDescription(application.getDescription());
        community.setStatus(1); // 1-正常运营

        return communityRepository.save(community);
    }

    /**
     * 根据申请自动创建团长门店
     */
    private GroupLeaderStore createLeaderStoreFromApplication(
            CommunityApplication application, 
            Community community
    ) {
        GroupLeaderStore store = new GroupLeaderStore();
        store.setLeaderId(application.getApplicantId());
        store.setLeaderName(application.getApplicantName());
        store.setLeaderPhone(application.getApplicantPhone());
        store.setCommunityId(community.getCommunityId());
        store.setCommunityName(community.getName());
        store.setStoreName(application.getApplicantName() + "的团点");
        store.setAddress(community.getAddress());
        store.setProvince(community.getProvince());
        store.setCity(community.getCity());
        store.setDistrict(community.getDistrict());
        store.setDetailAddress(community.getAddress());
        store.setLatitude(community.getLatitude());
        store.setLongitude(community.getLongitude());
        store.setMaxDeliveryRange(community.getServiceRadius());
        store.setDescription("由社区申请自动创建");
        store.setCommissionRate(BigDecimal.valueOf(10.00));
        store.setTotalCommission(BigDecimal.ZERO);
        store.setStatus(1); // 1-正常运营
        store.setReviewerId(application.getReviewerId());
        store.setReviewComment("社区申请审核通过，自动创建团长门店");
        store.setReviewedAt(LocalDateTime.now());

        return leaderStoreRepository.save(store);
    }

    /**
     * 查询所有待审核的申请
     */
    public List<CommunityApplication> getAllPendingApplications() {
        return applicationRepository.findAllPendingApplications();
    }

    /**
     * 根据状态查询申请列表
     */
    public List<CommunityApplication> getApplicationsByStatus(Integer status) {
        return applicationRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * 根据申请人ID查询申请记录
     */
    public List<CommunityApplication> getApplicationsByApplicantId(Long applicantId) {
        return applicationRepository.findByApplicantIdOrderByCreatedAtDesc(applicantId);
    }

    /**
     * 根据ID查询申请详情
     */
    public Optional<CommunityApplication> getApplicationById(Long applicationId) {
        return applicationRepository.findById(applicationId);
    }
}

