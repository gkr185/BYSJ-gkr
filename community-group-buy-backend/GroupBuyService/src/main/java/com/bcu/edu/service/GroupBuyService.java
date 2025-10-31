package com.bcu.edu.service;

import com.bcu.edu.client.ProductServiceClient;
import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.ProductDTO;
import com.bcu.edu.entity.GroupBuy;
import com.bcu.edu.enums.ActivityStatus;
import com.bcu.edu.repository.GroupBuyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private ProductServiceClient productServiceClient;
    
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
}

