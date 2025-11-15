package com.bcu.edu.feign;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.AddressDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * UserService Feign降级处理
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Result<AddressDTO> getAddressWithCoordinates(Long addressId) {
        log.error("调用UserService获取地址失败，addressId={}", addressId);
        return Result.error("UserService服务不可用");
    }

    @Override
    public Result<List<AddressDTO>> batchGetAddresses(List<Long> addressIds) {
        log.error("调用UserService批量获取地址失败，addressIds={}", addressIds);
        return Result.error("UserService服务不可用");
    }
}

