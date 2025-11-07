package com.bcu.edu.client;

import com.bcu.edu.client.fallback.LeaderServiceClientFallback;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.CommunityDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * LeaderService Feign客户端
 * 
 * <p>功能：
 * <ul>
 *   <li>获取社区信息（查询团详情时展示社区名称）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@FeignClient(name = "leader-service", fallback = LeaderServiceClientFallback.class)
public interface LeaderServiceClient {
    
    /**
     * 获取社区信息
     * 
     * <p>应用场景：
     * <ul>
     *   <li>查询团详情时获取社区名称（可选）</li>
     * </ul>
     * 
     * <p>注意：LeaderService的Feign接口路径为 /feign/community/{communityId}
     * 
     * @param communityId 社区ID
     * @return Result<CommunityDTO>
     */
    @GetMapping("/feign/community/{communityId}")
    Result<CommunityDTO> getCommunity(@PathVariable("communityId") Long communityId);
}

