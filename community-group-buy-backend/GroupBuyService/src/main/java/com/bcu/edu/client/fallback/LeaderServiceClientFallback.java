package com.bcu.edu.client.fallback;

import com.bcu.edu.client.LeaderServiceClient;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.CommunityDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * LeaderService Feign客户端降级处理
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Component
@Slf4j
public class LeaderServiceClientFallback implements LeaderServiceClient {
    
    @Override
    public Result<CommunityDTO> getCommunity(Long communityId) {
        log.warn("LeaderService调用失败，communityId={}，返回null", communityId);
        // 社区信息非必需，降级返回null
        return Result.success(null);
    }
}

