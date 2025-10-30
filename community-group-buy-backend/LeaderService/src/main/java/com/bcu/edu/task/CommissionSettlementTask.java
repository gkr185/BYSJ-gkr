package com.bcu.edu.task;

import com.bcu.edu.service.CommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 佣金结算定时任务
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 每月1号凌晨执行佣金结算
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommissionSettlementTask {

    private final CommissionService commissionService;

    /**
     * 每月1号凌晨2点执行佣金结算
     * 
     * Cron表达式：0 0 2 1 * ?
     * - 0：秒
     * - 0：分
     * - 2：2点
     * - 1：每月1号
     * - *：任意月份
     * - ?：任意星期
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void settleMonthlyCommissions() {
        log.info("========== 开始执行佣金结算定时任务 ==========");

        try {
            // 生成结算批次号（格式：YYYYMMDD）
            String settlementBatch = commissionService.generateSettlementBatch();

            // 执行批量结算
            int settledCount = commissionService.settleCommissions(settlementBatch);

            log.info("========== 佣金结算完成：批次{}，共结算{}条记录 ==========", settlementBatch, settledCount);
        } catch (Exception e) {
            log.error("========== 佣金结算失败：{} ==========", e.getMessage(), e);
        }
    }

    /**
     * 【测试接口】手动触发佣金结算（仅用于开发测试）
     * 每5分钟执行一次（可在开发环境启用）
     * 
     * 注意：生产环境请删除或注释此方法
     */
    // @Scheduled(cron = "0 */5 * * * ?")
    public void testSettleCommissions() {
        log.info("========== [测试] 手动触发佣金结算 ==========");

        try {
            String settlementBatch = commissionService.generateSettlementBatch() + "_TEST";
            int settledCount = commissionService.settleCommissions(settlementBatch);
            log.info("========== [测试] 佣金结算完成：批次{}，共结算{}条记录 ==========", settlementBatch, settledCount);
        } catch (Exception e) {
            log.error("========== [测试] 佣金结算失败：{} ==========", e.getMessage(), e);
        }
    }
}

