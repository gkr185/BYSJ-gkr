package com.bcu.edu.service;

import com.bcu.edu.client.LeaderServiceClient;
import com.bcu.edu.client.ProductServiceClient;
import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.*;
import com.bcu.edu.entity.GroupBuy;
import com.bcu.edu.entity.GroupBuyTeam;
import com.bcu.edu.enums.ActivityStatus;
import com.bcu.edu.repository.GroupBuyRepository;
import com.bcu.edu.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 拼团活动管理服务
 * 
 * <p>核心功能：
 * <ul>
 *   <li>创建活动（管理员）</li>
 *   <li>更新活动（管理员）</li>
 *   <li>删除活动（管理员）</li>
 *   <li>查询活动列表</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Service
@Slf4j
public class GroupBuyService {
    
    @Autowired
    private GroupBuyRepository activityRepository;
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private ProductServiceClient productServiceClient;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private LeaderServiceClient leaderServiceClient;
    
    /**
     * 创建拼团活动
     * 
     * @param activity 活动信息
     * @return 活动实体
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "创建拼团活动", module = "活动管理")
    public GroupBuy createActivity(GroupBuy activity) {
        // 1. Feign验证商品存在
        Result<ProductDTO> productResult = productServiceClient.getProduct(activity.getProductId());
        if (productResult.getCode() != 200 || productResult.getData() == null) {
            throw new BusinessException("商品不存在，productId=" + activity.getProductId());
        }
        
        ProductDTO product = productResult.getData();
        
        // 2. 验证拼团价必须小于原价
        if (activity.getGroupPrice().compareTo(product.getPrice()) >= 0) {
            throw new BusinessException("拼团价必须小于商品原价，商品原价=" + product.getPrice());
        }
        
        // 3. 验证时间
        if (activity.getStartTime().isAfter(activity.getEndTime())) {
            throw new BusinessException("活动开始时间不能晚于结束时间");
        }
        
        // 4. 保存活动
        activity.setStatus(ActivityStatus.ONGOING.getCode());
        activity.setCreateTime(LocalDateTime.now());
        activityRepository.save(activity);
        
        log.info("拼团活动创建成功，activityId={}, productId={}, groupPrice={}", 
            activity.getActivityId(), activity.getProductId(), activity.getGroupPrice());
        
        return activity;
    }
    
    /**
     * 更新活动
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "更新拼团活动", module = "活动管理")
    public GroupBuy updateActivity(Long activityId, GroupBuy activityUpdate) {
        GroupBuy activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new BusinessException("活动不存在"));
        
        // 更新字段
        if (activityUpdate.getGroupPrice() != null) {
            activity.setGroupPrice(activityUpdate.getGroupPrice());
        }
        if (activityUpdate.getRequiredNum() != null) {
            activity.setRequiredNum(activityUpdate.getRequiredNum());
        }
        if (activityUpdate.getMaxNum() != null) {
            activity.setMaxNum(activityUpdate.getMaxNum());
        }
        if (activityUpdate.getStartTime() != null) {
            activity.setStartTime(activityUpdate.getStartTime());
        }
        if (activityUpdate.getEndTime() != null) {
            activity.setEndTime(activityUpdate.getEndTime());
        }
        if (activityUpdate.getStatus() != null) {
            activity.setStatus(activityUpdate.getStatus());
        }
        
        activityRepository.save(activity);
        log.info("活动更新成功，activityId={}", activityId);
        
        return activity;
    }
    
    /**
     * 删除活动
     */
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(value = "删除拼团活动", module = "活动管理")
    public void deleteActivity(Long activityId) {
        GroupBuy activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new BusinessException("活动不存在"));
        
        activityRepository.delete(activity);
        log.info("活动删除成功，activityId={}", activityId);
    }
    
    /**
     * 获取活动列表
     */
    public List<GroupBuy> getActivities() {
        return activityRepository.findAll();
    }
    
    /**
     * 获取进行中的活动
     */
    public List<GroupBuy> getOngoingActivities() {
        return activityRepository.findOngoingActivities(LocalDateTime.now());
    }
    
    /**
     * 获取活动详情
     */
    public GroupBuy getActivityById(Long activityId) {
        return activityRepository.findById(activityId)
            .orElseThrow(() -> new BusinessException("活动不存在"));
    }
    
    /**
     * 获取进行中的活动（包含商品信息）
     * 用于前端展示活动列表
     */
    public List<ActivityWithProductResponse> getOngoingActivitiesWithProduct() {
        List<GroupBuy> activities = activityRepository.findOngoingActivities(LocalDateTime.now());
        
        return activities.stream()
            .map(this::buildActivityWithProduct)
            .filter(activity -> activity != null)
            .collect(Collectors.toList());
    }
    
    /**
     * 构建包含商品信息的活动响应
     */
    private ActivityWithProductResponse buildActivityWithProduct(GroupBuy activity) {
        try {
            // 获取商品信息
            Result<ProductDTO> productResult = productServiceClient.getProduct(activity.getProductId());
            
            if (productResult == null || productResult.getCode() != 200 || productResult.getData() == null) {
                log.warn("获取商品{}信息失败", activity.getProductId());
                return null;
            }
            
            ProductDTO product = productResult.getData();
            
            return ActivityWithProductResponse.builder()
                .activityId(activity.getActivityId())
                .productId(activity.getProductId())
                .productName(product.getProductName())
                .productImage(product.getCoverImg())
                .originalPrice(product.getPrice())
                .groupPrice(activity.getGroupPrice())
                .requiredNum(activity.getRequiredNum())
                .maxNum(activity.getMaxNum())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .status(activity.getStatus())
                .build();
        } catch (Exception e) {
            log.error("构建活动{}的商品信息失败: {}", activity.getActivityId(), e.getMessage());
            return null;
        }
    }
    
    /**
     * 根据商品ID获取拼团活动（包含团列表）⭐商品详情页专用
     * 
     * <p>特性：
     * <ul>
     *   <li>返回该商品的所有进行中拼团活动</li>
     *   <li>每个活动包含进行中的团列表（最多10个）</li>
     *   <li>支持社区优先排序</li>
     * </ul>
     * 
     * @param productId 商品ID
     * @param communityId 用户社区ID（可选，用于社区优先排序）
     * @return 活动列表（每个活动包含teams字段）
     */
    public List<ActivityWithTeamsResponse> getProductActivitiesWithTeams(Long productId, Long communityId) {
        // 1. 查询该商品的所有进行中活动
        List<GroupBuy> activities = activityRepository.findByProductId(productId).stream()
            .filter(activity -> activity.getStatus() == ActivityStatus.ONGOING.getCode())
            .filter(activity -> {
                LocalDateTime now = LocalDateTime.now();
                return activity.getStartTime().isBefore(now) && activity.getEndTime().isAfter(now);
            })
            .collect(Collectors.toList());
        
        log.info("商品{}有{}个进行中的拼团活动", productId, activities.size());
        
        // 2. 为每个活动构建响应（包含团列表）
        return activities.stream()
            .map(activity -> buildActivityWithTeams(activity, communityId))
            .filter(activity -> activity != null)
            .collect(Collectors.toList());
    }
    
    /**
     * 构建包含团列表的活动响应
     */
    private ActivityWithTeamsResponse buildActivityWithTeams(GroupBuy activity, Long communityId) {
        try {
            // 1. 查询该活动的进行中团列表（社区优先，最多10个）
            Long queryCommunityId = (communityId != null) ? communityId : 0L;
            List<GroupBuyTeam> teams = teamRepository.findByActivityIdWithCommunityPriority(
                activity.getActivityId(), 
                queryCommunityId, 
                10
            );
            
            log.info("活动{}有{}个进行中的团", activity.getActivityId(), teams.size());
            
            // 2. 构建团信息列表
            List<ActivityWithTeamsResponse.TeamInfo> teamInfos = teams.stream()
                .map(this::buildTeamInfo)
                .filter(teamInfo -> teamInfo != null)
                .collect(Collectors.toList());
            
            // 3. 构建活动响应
            return ActivityWithTeamsResponse.builder()
                .activityId(activity.getActivityId())
                .productId(activity.getProductId())
                .groupPrice(activity.getGroupPrice())
                .requiredNum(activity.getRequiredNum())
                .maxNum(activity.getMaxNum())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .status(activity.getStatus())
                .teams(teamInfos)
                .build();
        } catch (Exception e) {
            log.error("构建活动{}的团列表失败: {}", activity.getActivityId(), e.getMessage());
            return null;
        }
    }
    
    /**
     * 构建团信息
     */
    private ActivityWithTeamsResponse.TeamInfo buildTeamInfo(GroupBuyTeam team) {
        try {
            // 获取团长姓名
            String leaderName = "未知团长";
            try {
                Result<UserInfoDTO> userResult = userServiceClient.getUserInfo(team.getLeaderId());
                if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                    leaderName = userResult.getData().getRealName();
                    if (leaderName == null || leaderName.trim().isEmpty()) {
                        leaderName = userResult.getData().getUsername();
                    }
                }
            } catch (Exception e) {
                log.warn("获取团长{}信息失败: {}", team.getLeaderId(), e.getMessage());
            }
            
            // 获取社区名称
            String communityName = null;
            if (team.getCommunityId() != null) {
                try {
                    Result<CommunityDTO> communityResult = leaderServiceClient.getCommunity(team.getCommunityId());
                    if (communityResult != null && communityResult.getCode() == 200 && communityResult.getData() != null) {
                        communityName = communityResult.getData().getCommunityName();
                    }
                } catch (Exception e) {
                    log.warn("获取社区{}信息失败: {}", team.getCommunityId(), e.getMessage());
                }
            }
            
            return ActivityWithTeamsResponse.TeamInfo.builder()
                .teamId(team.getTeamId())
                .teamNo(team.getTeamNo())
                .leaderId(team.getLeaderId())
                .leaderName(leaderName)
                .communityId(team.getCommunityId())
                .communityName(communityName)
                .requiredNum(team.getRequiredNum())
                .currentNum(team.getCurrentNum())
                .teamStatus(team.getTeamStatus())
                .expireTime(team.getExpireTime())
                .createTime(team.getCreateTime())
                .build();
        } catch (Exception e) {
            log.error("构建团{}信息失败: {}", team.getTeamId(), e.getMessage());
            return null;
        }
    }

    /**
     * 验证拼团活动是否存在（供Feign调用）
     */
    public Boolean activityExists(Long activityId) {
        try {
            return activityRepository.existsById(activityId);
        } catch (Exception e) {
            log.error("验证拼团活动存在性失败: activityId={}", activityId, e);
            return false;
        }
    }

    /**
     * 获取拼团活动价格（供Feign调用）
     */
    public BigDecimal getActivityPrice(Long activityId) {
        try {
            GroupBuy activity = activityRepository.findById(activityId).orElse(null);
            return activity != null ? activity.getGroupPrice() : null;
        } catch (Exception e) {
            log.error("获取拼团活动价格失败: activityId={}", activityId, e);
            return null;
        }
    }
}

