package com.bcu.edu.task;

import com.bcu.edu.repository.TeamRepository;
import com.bcu.edu.service.RefundService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 过期团检查定时任务（⭐核心定时任务）
 * 
 * <p>功能：
 * <ul>
 *   <li>每小时执行一次（cron: 0 0 * * * ?）</li>
 *   <li>查询过期的团（team_status=0 且 expire_time < now）</li>
 *   <li>遍历退款（幂等性保证）</li>
 *   <li>异常隔离（单个团失败不影响其他团）</li>
 * </ul>
 * 
 * <p>技术亮点：
 * <ul>
 *   <li>⭐ 幂等性设计：行锁 + 状态检查</li>
 *   <li>⭐ 异常隔离：try-catch捕获单个团异常</li>
 *   <li>⭐ 事务独立：每个团单独事务处理</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Component
@Slf4j
public class TeamExpireTask {
    
    @Autowired
    private TeamRepository teamRepository;
    
    @Autowired
    private RefundService refundService;
    
    /**
     * 每小时检查过期团（⭐核心任务）
     * 
     * <p>执行时间：每小时的0分0秒
     * <p>cron表达式：0 0 * * * ?
     * <p>示例：01:00:00, 02:00:00, 03:00:00, ...
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkExpiredTeams() {
        LocalDateTime now = LocalDateTime.now();
        log.info("====================================");
        log.info("开始检查过期团，当前时间：{}", now);
        log.info("====================================");
        
        try {
            // 1. 查询过期团ID列表
            List<Long> expiredTeamIds = teamRepository.findExpiredTeamIds(now);
            
            if (expiredTeamIds.isEmpty()) {
                log.info("未发现过期团，任务结束");
                return;
            }
            
            log.info("发现{}个过期团需要处理：{}", expiredTeamIds.size(), expiredTeamIds);
            
            // 2. 遍历处理每个团
            int successCount = 0;
            int failCount = 0;
            
            for (Long teamId : expiredTeamIds) {
                try {
                    // 每个团单独事务处理（RefundService中@Transactional）
                    refundService.refundExpiredTeam(teamId);
                    successCount++;
                    log.info("✅ 团{}退款成功", teamId);
                } catch (Exception e) {
                    failCount++;
                    log.error("❌ 团{}退款失败", teamId, e);
                    // 不抛异常，继续处理其他团（异常隔离）⭐
                }
            }
            
            // 3. 汇总统计
            log.info("====================================");
            log.info("过期团处理完成");
            log.info("总计：{}个，成功：{}个，失败：{}个", expiredTeamIds.size(), successCount, failCount);
            log.info("成功率：{}%", expiredTeamIds.size() > 0 ? (successCount * 100 / expiredTeamIds.size()) : 0);
            log.info("====================================");
            
        } catch (Exception e) {
            log.error("定时任务执行异常", e);
        }
    }
    
    /**
     * 测试任务（每5分钟执行一次，仅用于测试）
     * 
     * <p>生产环境请注释或删除此方法
     */
    // @Scheduled(cron = "0 */5 * * * ?")
    public void testTask() {
        log.info("【测试】定时任务测试，当前时间：{}", LocalDateTime.now());
    }
}

