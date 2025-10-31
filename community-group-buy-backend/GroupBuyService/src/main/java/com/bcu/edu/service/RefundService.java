package com.bcu.edu.service;

import com.bcu.edu.client.OrderServiceClient;
import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.entity.GroupBuyMember;
import com.bcu.edu.entity.GroupBuyTeam;
import com.bcu.edu.enums.MemberStatus;
import com.bcu.edu.enums.TeamStatus;
import com.bcu.edu.repository.MemberRepository;
import com.bcu.edu.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 退款服务
 * 
 * <p>核心功能：
 * <ul>
 *   <li>定时任务：过期团自动退款（幂等性保证）</li>
 *   <li>用户主动退出：中途退出拼团（成团前）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Service
@Slf4j
public class RefundService {
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private OrderServiceClient orderServiceClient;
    
    /**
     * 过期团退款（⭐定时任务核心方法）
     * 
     * <p>流程：
     * <ol>
     *   <li>查询团（加行锁）⭐</li>
     *   <li>幂等性检查（team_status != JOINING 则跳过）⭐</li>
     *   <li>标记团失败（JOINING → FAILED）</li>
     *   <li>查询已支付的成员</li>
     *   <li>遍历退款：Feign退款 + 更新订单 + 更新参团状态</li>
     *   <li>发送失败通知</li>
     * </ol>
     * 
     * @param teamId 团ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void refundExpiredTeam(Long teamId) {
        // 1. 查询团（加行锁）⭐
        GroupBuyTeam team = teamRepository.findByIdForUpdate(teamId)
            .orElseThrow(() -> new BusinessException("团不存在"));
        
        // 2. 幂等性检查 ⭐
        if (team.getTeamStatus() != TeamStatus.JOINING.getCode()) {
            log.warn("团{}状态已变更，跳过退款，status={}", teamId, team.getTeamStatus());
            return;  // 已处理过，直接返回
        }
        
        log.info("开始处理过期团退款，teamId={}, teamNo={}", teamId, team.getTeamNo());
        
        // 3. 标记团失败
        team.setTeamStatus(TeamStatus.FAILED.getCode());
        teamRepository.save(team);
        log.info("团{}标记为失败", teamId);
        
        // 4. 查询已支付的成员
        List<GroupBuyMember> paidMembers = memberRepository.findByTeamIdAndStatus(
            teamId, MemberStatus.PAID.getCode()
        );
        
        log.info("团{}有{}个成员需要退款", teamId, paidMembers.size());
        
        // 5. 遍历退款
        int successCount = 0;
        int failCount = 0;
        
        for (GroupBuyMember member : paidMembers) {
            try {
                // Feign退款到余额
                userServiceClient.refundToBalance(member.getUserId(), member.getPayAmount());
                log.info("用户{}退款成功，金额={}", member.getUserId(), member.getPayAmount());
                
                // Feign更新订单状态
                orderServiceClient.updateOrderStatus(member.getOrderId(), 6);  // 6-已退款
                log.info("订单{}状态更新为已退款", member.getOrderId());
                
                // 更新参团状态
                member.setStatus(MemberStatus.CANCELLED.getCode());
                memberRepository.save(member);
                
                successCount++;
            } catch (Exception e) {
                log.error("用户{}退款失败，memberId={}", member.getUserId(), member.getMemberId(), e);
                failCount++;
                // 不抛异常，继续处理其他成员
            }
        }
        
        log.info("团{}退款完成，成功{}人，失败{}人", teamId, successCount, failCount);
        
        // 6. 发送通知（TODO）
        log.info("拼团失败通知发送（TODO）");
    }
    
    /**
     * 用户主动退出拼团（成团前）
     * 
     * @param teamId 团ID
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void quitTeam(Long teamId, Long userId) {
        // 1. 查询参团记录
        GroupBuyMember member = memberRepository.findByTeamIdAndStatus(teamId, MemberStatus.PAID.getCode())
            .stream()
            .filter(m -> m.getUserId().equals(userId))
            .findFirst()
            .orElseThrow(() -> new BusinessException("未找到参团记录或已退出"));
        
        // 2. 查询团（加行锁）
        GroupBuyTeam team = teamRepository.findByIdForUpdate(teamId)
            .orElseThrow(() -> new BusinessException("团不存在"));
        
        // 3. 检查团状态
        if (team.getTeamStatus() == TeamStatus.SUCCESS.getCode()) {
            throw new BusinessException("拼团已成功，无法退出");
        }
        
        if (team.getTeamStatus() == TeamStatus.FAILED.getCode()) {
            throw new BusinessException("拼团已失败，无需退出");
        }
        
        log.info("用户{}退出拼团，teamId={}, memberId={}", userId, teamId, member.getMemberId());
        
        // 4. 删除参团记录
        memberRepository.delete(member);
        
        // 5. 更新团人数
        if (team.getCurrentNum() > 0) {
            team.setCurrentNum(team.getCurrentNum() - 1);
            teamRepository.save(team);
        }
        
        // 6. 退款（Feign）
        try {
            userServiceClient.refundToBalance(member.getUserId(), member.getPayAmount());
            log.info("用户{}退款成功，金额={}", member.getUserId(), member.getPayAmount());
        } catch (Exception e) {
            log.error("退款失败，userId={}, amount={}", member.getUserId(), member.getPayAmount(), e);
            throw new BusinessException("退款失败，请联系管理员");
        }
        
        // 7. 更新订单状态（Feign）
        try {
            orderServiceClient.updateOrderStatus(member.getOrderId(), 6);  // 6-已退款
            log.info("订单{}状态更新为已退款", member.getOrderId());
        } catch (Exception e) {
            log.error("更新订单状态失败，orderId={}", member.getOrderId(), e);
            // 不抛异常
        }
    }
}

