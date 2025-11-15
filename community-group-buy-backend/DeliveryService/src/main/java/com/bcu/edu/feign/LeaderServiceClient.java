package com.bcu.edu.feign;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.GroupLeaderStore;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * LeaderService Feign客户端
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@FeignClient(name = "leader-service", fallback = LeaderServiceClientFallback.class)
public interface LeaderServiceClient {

    /**
     * 获取团长团点信息（包含坐标）
     */
    @GetMapping("/feign/leader/store/{leaderId}")
    Result<GroupLeaderStore> getLeaderStore(@PathVariable Long leaderId);

    /**
     * 批量获取团长团点信息
     */
    @PostMapping("/feign/leader/store/batch")
    Result<List<GroupLeaderStore>> batchGetLeaderStores(@RequestBody List<Long> leaderIds);
}

