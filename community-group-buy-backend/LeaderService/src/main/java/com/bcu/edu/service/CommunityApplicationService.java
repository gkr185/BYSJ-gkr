package com.bcu.edu.service;

import com.bcu.edu.entity.Community;
import com.bcu.edu.entity.CommunityApplication;
import com.bcu.edu.repository.CommunityApplicationRepository;
import com.bcu.edu.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * 4. 申请人需要单独申请成为团长
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityApplicationService {

    private final CommunityApplicationRepository applicationRepository;
    private final CommunityRepository communityRepository;

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
            // 审核通过前，验证必要字段
            if (application.getLatitude() == null || application.getLongitude() == null) {
                throw new IllegalArgumentException("审核通过前必须补充经纬度信息");
            }
            
            // 审核通过
            application.setStatus(1);

            // 自动创建Community
            Community newCommunity = createCommunityFromApplication(application);
            application.setCreatedCommunityId(newCommunity.getCommunityId());

            log.info("审核通过，社区{}创建成功。申请人{}需要单独申请成为团长", 
                    newCommunity.getName(), application.getApplicantId());
        } else {
            // 审核拒绝
            application.setStatus(2);
            log.info("审核拒绝，申请ID：{}，原因：{}", applicationId, reviewComment);
        }

        return applicationRepository.save(application);
    }
    
    /**
     * 管理员补充社区申请的经纬度信息（审核前调用）
     * 
     * @param applicationId 申请ID
     * @param latitude 纬度
     * @param longitude 经度
     */
    @Transactional
    public CommunityApplication updateApplicationCoordinates(
            Long applicationId,
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude
    ) {
        CommunityApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在：" + applicationId));
        
        // 只有待审核的申请可以补充信息
        if (application.getStatus() != 0) {
            throw new IllegalStateException("只有待审核的申请可以补充信息");
        }
        
        application.setLatitude(latitude);
        application.setLongitude(longitude);
        
        log.info("管理员补充申请{}的经纬度：{}, {}", applicationId, latitude, longitude);
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

