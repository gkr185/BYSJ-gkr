package com.bcu.edu.feign;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.GroupLeaderStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LeaderService Feign降级处理
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@Component
public class LeaderServiceClientFallback implements LeaderServiceClient {

    @Override
    public Result<GroupLeaderStore> getLeaderStore(Long leaderId) {
        log.error("调用LeaderService获取团长团点失败，leaderId={}", leaderId);
        return Result.error("LeaderService服务不可用");
    }

    @Override
    public Result<List<GroupLeaderStore>> batchGetLeaderStores(List<Long> leaderIds) {
        log.error("调用LeaderService批量获取团长团点失败，leaderIds={}", leaderIds);
        return Result.error("LeaderService服务不可用");
    }
}

