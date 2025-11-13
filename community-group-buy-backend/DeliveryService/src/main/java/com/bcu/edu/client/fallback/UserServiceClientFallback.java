package com.bcu.edu.client.fallback;

import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.AddressWithCoordinatesDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * UserService Feign客户端降级处理
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Result<AddressWithCoordinatesDTO> getAddressWithCoordinates(Long addressId) {
        log.error("UserService调用失败: getAddressWithCoordinates, addressId={}", addressId);
        return Result.error("用户服务暂时不可用，无法获取地址信息");
    }

    @Override
    public Result<List<AddressWithCoordinatesDTO>> getAddressesWithCoordinates(List<Long> addressIds) {
        log.error("UserService调用失败: getAddressesWithCoordinates, addressIds={}", addressIds);
        return Result.error("用户服务暂时不可用，无法批量获取地址信息");
    }

    @Override
    public Result<Boolean> validateUser(Long userId) {
        log.error("UserService调用失败: validateUser, userId={}", userId);
        return Result.error("用户服务暂时不可用，无法验证用户");
    }
}
