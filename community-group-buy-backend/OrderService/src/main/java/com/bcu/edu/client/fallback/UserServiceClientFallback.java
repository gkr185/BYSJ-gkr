package com.bcu.edu.client.fallback;

import com.bcu.edu.client.UserServiceClient;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.AddressDTO;
import com.bcu.edu.dto.response.UserInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public Result<Boolean> validateUser(Long userId) {
        log.error("UserService调用失败: validateUser, userId={}", userId);
        return Result.error("用户服务暂时不可用");
    }

    @Override
    public Result<UserInfoDTO> getUserInfo(Long userId) {
        log.error("UserService调用失败: getUserInfo, userId={}", userId);
        return Result.error("用户服务暂时不可用");
    }

    @Override
    public Result<AddressDTO> getAddress(Long addressId) {
        log.error("UserService调用失败: getAddress, addressId={}", addressId);
        return Result.error("用户服务暂时不可用");
    }
}

