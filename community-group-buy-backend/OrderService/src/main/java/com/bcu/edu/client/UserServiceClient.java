package com.bcu.edu.client;

import com.bcu.edu.client.fallback.UserServiceClientFallback;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.AddressDTO;
import com.bcu.edu.dto.response.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * UserService Feign客户端
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    /**
     * 验证用户是否存在
     */
    @GetMapping("/api/user/feign/validate/{userId}")
    Result<Boolean> validateUser(@PathVariable("userId") Long userId);

    /**
     * 获取用户信息
     */
    @GetMapping("/api/user/feign/info/{userId}")
    Result<UserInfoDTO> getUserInfo(@PathVariable("userId") Long userId);

    /**
     * 获取地址详情
     * ⭐ 返回类型需要与UserService的AddressResponse一致
     */
    @GetMapping("/api/user/feign/address/{addressId}")
    Result<AddressDTO> getAddress(@PathVariable("addressId") Long addressId);
}

