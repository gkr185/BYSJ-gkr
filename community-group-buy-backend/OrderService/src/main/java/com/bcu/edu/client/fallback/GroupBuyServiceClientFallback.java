package com.bcu.edu.client.fallback;

import com.bcu.edu.client.GroupBuyServiceClient;
import com.bcu.edu.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * GroupBuyService Feign客户端降级处理
 *
 * @author 耿康瑞
 * @since 2025-11-04
 */
@Component
@Slf4j
public class GroupBuyServiceClientFallback implements FallbackFactory<GroupBuyServiceClient> {

    @Override
    public GroupBuyServiceClient create(Throwable cause) {
        log.error("GroupBuyService调用失败", cause);

        return new GroupBuyServiceClient() {
            @Override
            public Result<Boolean> validateActivity(Long activityId) {
                log.warn("降级处理: validateActivity, activityId={}", activityId);
                return Result.error("拼团服务暂不可用");
            }

            @Override
            public Result<BigDecimal> getActivityPrice(Long activityId) {
                log.warn("降级处理: getActivityPrice, activityId={}", activityId);
                return Result.error("拼团服务暂不可用");
            }

            @Override
            public Result<java.util.List<Long>> getTeamOrderIds(Long teamId) {
                log.warn("降级处理: getTeamOrderIds, teamId={}", teamId);
                return Result.error("拼团服务暂不可用");
            }
        };
    }
}
