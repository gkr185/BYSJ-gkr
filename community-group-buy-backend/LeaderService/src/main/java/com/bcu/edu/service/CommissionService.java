package com.bcu.edu.service;

import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.result.PageResult;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.CommissionRecord;
import com.bcu.edu.entity.GroupLeaderStore;
import com.bcu.edu.repository.CommissionRecordRepository;
import com.bcu.edu.repository.GroupLeaderStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 佣金服务
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 核心功能：
 * 1. 订单完成时生成佣金记录
 * 2. 佣金计算（订单金额 * 佣金比例）
 * 3. 批量结算佣金（调用UserService增加团长余额）
 * 4. 佣金统计查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionService {

    private final CommissionRecordRepository commissionRecordRepository;
    private final GroupLeaderStoreRepository leaderStoreRepository;
    private final UserServiceClient userServiceClient;

    /**
     * 【核心接口】生成佣金记录
     * 供OrderService调用，订单完成时触发
     * 
     * @param leaderId 团长ID
     * @param orderId 订单ID
     * @param orderAmount 订单金额
     * @return 佣金记录
     */
    @Transactional
    public CommissionRecord generateCommission(Long leaderId, Long orderId, BigDecimal orderAmount) {
        // 防止重复生成
        if (commissionRecordRepository.existsByOrderId(orderId)) {
            throw new IllegalArgumentException("订单佣金记录已存在：" + orderId);
        }

        // 查询团长信息（获取佣金比例）
        GroupLeaderStore leaderStore = leaderStoreRepository.findByLeaderId(leaderId)
                .orElseThrow(() -> new IllegalArgumentException("团长不存在：" + leaderId));

        // 计算佣金金额
        BigDecimal commissionRate = leaderStore.getCommissionRate();
        BigDecimal commissionAmount = calculateCommissionAmount(orderAmount, commissionRate);

        // 创建佣金记录
        CommissionRecord record = new CommissionRecord();
        record.setLeaderId(leaderId);
        record.setLeaderName(leaderStore.getLeaderName());
        record.setOrderId(orderId);
        record.setOrderAmount(orderAmount);
        record.setCommissionRate(commissionRate);
        record.setCommissionAmount(commissionAmount);
        record.setStatus(0); // 0-待结算
        record.setRemark("订单完成，生成佣金记录");

        CommissionRecord saved = commissionRecordRepository.save(record);
        log.info("生成佣金记录：团长{}，订单{}，佣金{}", leaderId, orderId, commissionAmount);

        return saved;
    }

    /**
     * 【核心算法】计算佣金金额
     * 
     * 公式：佣金金额 = 订单金额 * 佣金比例 / 100
     * 示例：订单100元，佣金比例10%，则佣金 = 100 * 10 / 100 = 10元
     * 
     * @param orderAmount 订单金额
     * @param commissionRate 佣金比例（百分比）
     * @return 佣金金额（保留2位小数）
     */
    private BigDecimal calculateCommissionAmount(BigDecimal orderAmount, BigDecimal commissionRate) {
        return orderAmount
                .multiply(commissionRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 【核心接口】批量结算佣金
     * 
     * 业务流程：
     * 1. 查询所有待结算的佣金记录
     * 2. 按团长分组，计算每个团长的佣金总额
     * 3. 调用UserService为团长增加余额
     * 4. 更新佣金记录状态为"已结算"
     * 5. 更新团长的累计佣金
     * 
     * @param settlementBatch 结算批次号（例如：20251101）
     * @return 结算的佣金记录数量
     */
    @Transactional
    public int settleCommissions(String settlementBatch) {
        // 查询所有待结算的佣金记录
        List<CommissionRecord> pendingRecords = commissionRecordRepository.findAllPendingRecords();

        if (pendingRecords.isEmpty()) {
            log.info("无待结算佣金记录");
            return 0;
        }

        int settledCount = 0;

        // 按团长分组结算
        var groupedByLeader = pendingRecords.stream()
                .collect(java.util.stream.Collectors.groupingBy(CommissionRecord::getLeaderId));

        for (var entry : groupedByLeader.entrySet()) {
            Long leaderId = entry.getKey();
            List<CommissionRecord> leaderRecords = entry.getValue();

            // 计算该团长的佣金总额
            BigDecimal totalCommission = leaderRecords.stream()
                    .map(CommissionRecord::getCommissionAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            try {
                // 调用UserService为团长增加余额
                String remark = "佣金结算-批次:" + settlementBatch;
                Result<Void> result = userServiceClient.addBalanceForCommission(
                    leaderId, totalCommission, remark);
                
                if (result == null || result.getCode() != 200) {
                    String errorMsg = result != null ? result.getMessage() : "调用失败";
                    throw new IllegalStateException("增加团长余额失败：" + errorMsg);
                }
                
                log.info("团长{}余额增加成功：{}元", leaderId, totalCommission);

                // 更新佣金记录状态
                for (CommissionRecord record : leaderRecords) {
                    record.setStatus(1); // 1-已结算
                    record.setSettledAt(LocalDateTime.now());
                    record.setSettlementBatch(settlementBatch);
                    commissionRecordRepository.save(record);
                    settledCount++;
                }

                // 更新团长的累计佣金
                GroupLeaderStore leaderStore = leaderStoreRepository.findByLeaderId(leaderId)
                        .orElseThrow(() -> new IllegalArgumentException("团长不存在：" + leaderId));
                
                BigDecimal currentTotal = leaderStore.getTotalCommission();
                leaderStore.setTotalCommission(currentTotal.add(totalCommission));
                leaderStoreRepository.save(leaderStore);

                log.info("团长{}佣金结算成功：共{}条记录，总额{}元", leaderId, leaderRecords.size(), totalCommission);
            } catch (Exception e) {
                log.error("团长{}佣金结算失败：{}", leaderId, e.getMessage());
                // 标记为结算失败
                for (CommissionRecord record : leaderRecords) {
                    record.setStatus(2); // 2-结算失败
                    record.setRemark("结算失败：" + e.getMessage());
                    commissionRecordRepository.save(record);
                }
            }
        }

        log.info("佣金结算完成：批次{}，共结算{}条记录", settlementBatch, settledCount);
        return settledCount;
    }

    /**
     * 生成结算批次号（格式：YYYYMMDD）
     */
    public String generateSettlementBatch() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * 查询团长的佣金记录
     */
    public List<CommissionRecord> getCommissionsByLeader(Long leaderId) {
        return commissionRecordRepository.findByLeaderIdOrderByCreatedAtDesc(leaderId);
    }

    /**
     * 分页查询团长的佣金记录
     */
    public PageResult<CommissionRecord> getCommissionsByLeaderPage(Long leaderId, Integer status, Integer page, Integer limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<CommissionRecord> recordPage;
        
        if (status != null) {
            recordPage = commissionRecordRepository.findByLeaderIdAndStatusOrderByCreatedAtDesc(leaderId, status, pageable);
        } else {
            recordPage = commissionRecordRepository.findByLeaderIdOrderByCreatedAtDesc(leaderId, pageable);
        }
        
        return new PageResult<>(
            recordPage.getNumber(),
            recordPage.getSize(),
            recordPage.getTotalElements(),
            recordPage.getContent()
        );
    }

    /**
     * 查询团长的待结算佣金总额
     */
    public BigDecimal getPendingCommissionByLeader(Long leaderId) {
        BigDecimal amount = commissionRecordRepository.sumPendingCommissionByLeader(leaderId);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    /**
     * 查询团长的已结算佣金总额
     */
    public BigDecimal getSettledCommissionByLeader(Long leaderId) {
        BigDecimal amount = commissionRecordRepository.sumSettledCommissionByLeader(leaderId);
        return amount != null ? amount : BigDecimal.ZERO;
    }

    /**
     * 查询所有待结算的佣金记录
     */
    public List<CommissionRecord> getAllPendingCommissions() {
        return commissionRecordRepository.findAllPendingRecords();
    }

    /**
     * 根据结算批次号查询佣金记录
     */
    public List<CommissionRecord> getCommissionsBySettlementBatch(String settlementBatch) {
        return commissionRecordRepository.findBySettlementBatchOrderByCreatedAtDesc(settlementBatch);
    }

    /**
     * 查询所有已结算的佣金记录
     */
    public List<CommissionRecord> getAllSettledCommissions() {
        return commissionRecordRepository.findByStatusOrderByCreatedAtDesc(1);
    }
}

