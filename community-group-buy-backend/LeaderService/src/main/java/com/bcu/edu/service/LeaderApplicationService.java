package com.bcu.edu.service;

import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.Community;
import com.bcu.edu.entity.GroupLeaderStore;
import com.bcu.edu.repository.CommunityRepository;
import com.bcu.edu.repository.GroupLeaderStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 团长申请服务
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 核心功能：
 * 1. 团长申请提交
 * 2. 管理员审核
 * 3. 审核通过自动调用UserService更新用户角色为2（团长）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderApplicationService {

    private final GroupLeaderStoreRepository leaderStoreRepository;
    private final CommunityRepository communityRepository;
    private final UserServiceClient userServiceClient;

    /**
     * 提交团长申请
     * 
     * 业务规则：
     * 1. 用户只能申请一次团长（包含待审核、正常运营状态）
     * 2. 如果申请已有社区，社区必须存在
     * 3. 如果申请新社区，communityId为null，等待管理员审核后分配
     * 4. 验证用户是否存在（调用UserService）
     */
    @Transactional
    public GroupLeaderStore submitApplication(GroupLeaderStore application) {
        // 规则1：检查用户是否已是团长
        if (leaderStoreRepository.existsByLeaderIdAndActiveStatus(application.getLeaderId())) {
            throw new IllegalArgumentException("您已是团长，无法重复申请");
        }

        // 规则2：验证用户是否存在（调用UserService）
        try {
            Result<Boolean> result = userServiceClient.existsUser(application.getLeaderId());
            if (result.getData() == null || !result.getData()) {
                throw new IllegalArgumentException("用户不存在：" + application.getLeaderId());
            }
        } catch (Exception e) {
            log.error("调用UserService验证用户失败：{}", e.getMessage());
            throw new IllegalStateException("验证用户信息失败，请稍后重试");
        }

        // 规则3：如果申请已有社区，验证社区是否存在并自动填充社区信息
        if (application.getCommunityId() != null) {
            Community community = communityRepository.findById(application.getCommunityId())
                    .orElseThrow(() -> new IllegalArgumentException("申请的社区不存在：" + application.getCommunityId()));
            
            // 自动填充社区相关信息
            application.setCommunityName(community.getName());
            application.setProvince(community.getProvince());
            application.setCity(community.getCity());
            application.setDistrict(community.getDistrict());
            
            log.info("用户{}提交团长申请，选择已有社区：{}", application.getLeaderId(), application.getCommunityId());
        } else {
            // 申请新社区时，communityId为null，等待管理员审核社区申请后关联
            log.info("用户{}提交团长申请，同时申请新社区", application.getLeaderId());
        }
        
        // 规则4：检查是否有已停用的记录，如果有则更新而不是创建新记录
        Optional<GroupLeaderStore> existingStore = leaderStoreRepository.findByLeaderId(application.getLeaderId());
        if (existingStore.isPresent() && existingStore.get().getStatus() == 2) {
            // 已停用的记录，更新而不是创建新记录
            GroupLeaderStore store = existingStore.get();
            store.setStoreName(application.getStoreName());
            store.setAddress(application.getAddress());
            store.setCommunityId(application.getCommunityId());
            store.setCommunityName(application.getCommunityName());
            store.setProvince(application.getProvince());
            store.setCity(application.getCity());
            store.setDistrict(application.getDistrict());
            store.setDescription(application.getDescription());
            store.setStatus(0); // 0-待审核
            // 保留历史佣金数据
            
            log.info("用户{}重新提交团长申请（更新已停用记录），团点ID：{}", application.getLeaderId(), store.getStoreId());
            return leaderStoreRepository.save(store);
        } else {
            // 设置初始状态，创建新记录
            application.setStatus(0); // 0-待审核
            return leaderStoreRepository.save(application);
        }
    }

    /**
     * 管理员补充团长申请的经纬度信息（审核前调用）
     * 
     * @param storeId 团点ID
     * @param latitude 纬度
     * @param longitude 经度
     */
    @Transactional
    public GroupLeaderStore updateStoreCoordinates(
            Long storeId,
            java.math.BigDecimal latitude,
            java.math.BigDecimal longitude
    ) {
        GroupLeaderStore store = leaderStoreRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("团点不存在：" + storeId));
        
        // 只有待审核的申请可以补充信息
        if (store.getStatus() != 0) {
            throw new IllegalStateException("只有待审核的申请可以补充信息");
        }
        
        store.setLatitude(latitude);
        store.setLongitude(longitude);
        
        log.info("管理员补充团点{}的经纬度：{}, {}", storeId, latitude, longitude);
        return leaderStoreRepository.save(store);
    }

    /**
     * 管理员审核团长申请
     * 
     * @param storeId 团点ID
     * @param reviewerId 审核人ID（管理员）
     * @param approved 是否通过（true-通过，false-拒绝）
     * @param reviewComment 审核意见
     */
    @Transactional
    public GroupLeaderStore reviewApplication(
            Long storeId,
            Long reviewerId,
            boolean approved,
            String reviewComment
    ) {
        GroupLeaderStore store = leaderStoreRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("团长申请不存在：" + storeId));

        // 检查申请状态
        if (store.getStatus() != 0) {
            throw new IllegalStateException("该申请已被审核，无法重复审核");
        }
        
        // 审核通过前，验证必要字段（经纬度可选，但如果要支持路径规划则必须）
        if (approved && (store.getLatitude() == null || store.getLongitude() == null)) {
            log.warn("审核通过，但团点{}缺少经纬度信息，路径规划功能将不可用", storeId);
        }

        // 更新审核信息
        store.setReviewerId(reviewerId);
        store.setReviewComment(reviewComment);
        store.setReviewedAt(LocalDateTime.now());

        if (approved) {
            // 审核通过
            store.setStatus(1); // 1-正常运营

            // 调用UserService更新用户角色为团长（role=2）
            try {
                Result<Void> result = userServiceClient.updateUserRole(store.getLeaderId(), 2);
                if (!result.isSuccess()) {
                    log.error("更新用户角色失败：{}", result.getMessage());
                    throw new IllegalStateException("更新用户角色失败：" + result.getMessage());
                }
                log.info("审核通过，用户{}成为团长，社区：{}", store.getLeaderId(), store.getCommunityId());
            } catch (Exception e) {
                log.error("调用UserService更新角色失败：{}", e.getMessage());
                throw new IllegalStateException("更新用户角色失败，请稍后重试");
            }
        } else {
            // 审核拒绝
            store.setStatus(2); // 2-已停用
            log.info("审核拒绝，团点ID：{}，原因：{}", storeId, reviewComment);
        }

        return leaderStoreRepository.save(store);
    }

    /**
     * 查询所有待审核的团长申请
     */
    public List<GroupLeaderStore> getAllPendingApplications() {
        return leaderStoreRepository.findAllPendingApplications();
    }

    /**
     * 根据状态查询团长列表
     */
    public List<GroupLeaderStore> getLeadersByStatus(Integer status) {
        return leaderStoreRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * 查询所有正常运营的团长
     */
    public List<GroupLeaderStore> getAllActiveLeaders() {
        return leaderStoreRepository.findAllActiveStores();
    }

    /**
     * 根据团长ID查询团点信息
     */
    public Optional<GroupLeaderStore> getLeaderByUserId(Long userId) {
        return leaderStoreRepository.findByLeaderId(userId);
    }

    /**
     * 根据社区ID查询该社区的所有团长
     */
    public List<GroupLeaderStore> getLeadersByCommunity(Long communityId) {
        return leaderStoreRepository.findByCommunityId(communityId);
    }

    /**
     * 根据ID查询团点详情
     */
    public Optional<GroupLeaderStore> getLeaderById(Long storeId) {
        return leaderStoreRepository.findById(storeId);
    }

    /**
     * 更新团点信息
     */
    @Transactional
    public GroupLeaderStore updateLeaderStore(Long storeId, GroupLeaderStore updatedStore) {
        GroupLeaderStore existing = leaderStoreRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("团点不存在：" + storeId));

        // 只允许更新部分字段
        existing.setStoreName(updatedStore.getStoreName());
        existing.setAddress(updatedStore.getAddress());
        existing.setDescription(updatedStore.getDescription());
        
        // 允许更新所属社区
        if (updatedStore.getCommunityId() != null && !updatedStore.getCommunityId().equals(existing.getCommunityId())) {
            // 验证社区是否存在
            Community community = communityRepository.findById(updatedStore.getCommunityId())
                    .orElseThrow(() -> new IllegalArgumentException("社区不存在：" + updatedStore.getCommunityId()));
            
            existing.setCommunityId(community.getCommunityId());
            existing.setCommunityName(community.getName());
            log.info("团长{}更新所属社区：{} -> {}", existing.getLeaderId(), 
                    existing.getCommunityName(), community.getName());
        }
        
        // 允许更新经纬度
        if (updatedStore.getLatitude() != null) {
            existing.setLatitude(updatedStore.getLatitude());
        }
        if (updatedStore.getLongitude() != null) {
            existing.setLongitude(updatedStore.getLongitude());
        }
        
        // 允许更新佣金比例
        if (updatedStore.getCommissionRate() != null) {
            existing.setCommissionRate(updatedStore.getCommissionRate());
        }

        log.info("团长{}更新团点信息", existing.getLeaderId());
        return leaderStoreRepository.save(existing);
    }

    /**
     * 停用团点
     */
    @Transactional
    public void disableLeader(Long storeId) {
        GroupLeaderStore store = leaderStoreRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("团点不存在：" + storeId));

        store.setStatus(2); // 2-已停用
        leaderStoreRepository.save(store);

        log.info("团点{}已停用", storeId);
    }

    /**
     * 批量获取团长团点信息（⭐新增方法 - 供DeliveryService调用）
     * 
     * @param leaderIds 团长ID列表
     * @return 团长团点列表
     */
    public List<GroupLeaderStore> batchGetLeaderStores(List<Long> leaderIds) {
        log.info("批量获取团长团点: leaderIds={}", leaderIds);
        
        List<GroupLeaderStore> stores = leaderStoreRepository.findByLeaderIdIn(leaderIds);
        
        log.info("批量获取成功: 共{}个团点", stores.size());
        return stores;
    }
}

