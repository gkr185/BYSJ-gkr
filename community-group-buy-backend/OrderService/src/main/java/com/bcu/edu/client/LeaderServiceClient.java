package com.bcu.edu.client;

import com.bcu.edu.client.fallback.LeaderServiceClientFallback;
import com.bcu.edu.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * LeaderService Feign客户端
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@FeignClient(name = "leader-service", fallback = LeaderServiceClientFallback.class)
public interface LeaderServiceClient {

    /**
     * 验证团长是否存在
     */
    @GetMapping("/api/leader/feign/validate/{leaderId}")
    Result<Boolean> validateLeader(@PathVariable("leaderId") Long leaderId);

    /**
     * 生成佣金记录（⭐确认收货时调用）
     */
    @PostMapping("/feign/commission/generate")
    Result<Object> generateCommission(
        @RequestParam("leaderId") Long leaderId,
        @RequestParam("orderId") Long orderId,
        @RequestParam("orderAmount") BigDecimal orderAmount
    );
}

