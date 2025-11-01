package com.bcu.edu.client.fallback;

import com.bcu.edu.client.LeaderServiceClient;
import com.bcu.edu.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LeaderServiceClientFallback implements LeaderServiceClient {

    @Override
    public Result<Boolean> validateLeader(Long leaderId) {
        log.error("LeaderService调用失败: validateLeader, leaderId={}", leaderId);
        return Result.error("团长服务暂时不可用");
    }
}

