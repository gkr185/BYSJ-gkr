package com.bcu.edu.task;

import com.bcu.edu.entity.OrderMain;
import com.bcu.edu.repository.OrderMainRepository;
import com.bcu.edu.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时取消定时任务
 * 
 * <p>功能: 定时检查超时未支付订单并自动取消
 * <p>执行频率: 每5分钟执行一次
 * <p>超时时间: 可配置（默认30分钟）
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@Component
@Slf4j
public class OrderExpireTask {

    @Autowired
    private OrderMainRepository orderMainRepository;

    @Autowired
    private OrderService orderService;

    /**
     * 订单超时时间（分钟）
     */
    @Value("${order.expire.minutes:30}")
    private Integer expireMinutes;

    /**
     * 每5分钟检查一次超时订单
     * 
     * <p>Cron表达式: 0 *\/5 * * * ?
     * <p>含义: 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void cancelExpiredOrders() {
        log.info("========== 开始检查超时订单 ==========");
        log.info("超时时间设置: {}分钟", expireMinutes);

        try {
            // 计算过期时间点
            LocalDateTime expireTime = LocalDateTime.now().minusMinutes(expireMinutes);
            log.info("过期时间点: {}", expireTime);

            // 查询超时订单
            List<OrderMain> expiredOrders = orderMainRepository.findExpiredOrders(expireTime);
            log.info("找到{}条超时订单", expiredOrders.size());

            if (expiredOrders.isEmpty()) {
                log.info("没有超时订单，任务结束");
                return;
            }

            // 取消超时订单
            int successCount = 0;
            int failCount = 0;

            for (OrderMain order : expiredOrders) {
                try {
                    log.info("取消超时订单: orderId={}, orderSn={}, createTime={}", 
                             order.getOrderId(), order.getOrderSn(), order.getCreateTime());

                    orderService.cancelOrder(order.getOrderId());
                    successCount++;

                    log.info("订单{}取消成功", order.getOrderSn());
                } catch (Exception e) {
                    failCount++;
                    log.error("取消订单{}失败: {}", order.getOrderSn(), e.getMessage(), e);
                }
            }

            log.info("========== 超时订单处理完成 ==========");
            log.info("处理结果: 成功{}条, 失败{}条", successCount, failCount);

        } catch (Exception e) {
            log.error("检查超时订单任务执行失败", e);
        }
    }
}

