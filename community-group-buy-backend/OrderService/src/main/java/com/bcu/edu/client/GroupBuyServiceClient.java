package com.bcu.edu.client;

import com.bcu.edu.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

/**
 * GroupBuyService Feign客户端
 *
 * @author 耿康瑞
 * @since 2025-11-04
 */
@FeignClient(name = "groupbuy-service", fallback = com.bcu.edu.client.fallback.GroupBuyServiceClientFallback.class)
public interface GroupBuyServiceClient {

    /**
     * 验证拼团活动是否存在
     */
    @GetMapping("/api/groupbuy/feign/validateActivity/{activityId}")
    Result<Boolean> validateActivity(@PathVariable("activityId") Long activityId);

    /**
     * 获取拼团活动价格
     */
    @GetMapping("/api/groupbuy/feign/activityPrice/{activityId}")
    Result<BigDecimal> getActivityPrice(@PathVariable("activityId") Long activityId);

    /**
     * 获取团的订单ID列表
     */
    @GetMapping("/api/groupbuy/feign/teamOrderIds/{teamId}")
    Result<java.util.List<Long>> getTeamOrderIds(@PathVariable("teamId") Long teamId);
}
