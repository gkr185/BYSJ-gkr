package com.bcu.edu.client;

import com.bcu.edu.client.fallback.UserServiceClientFallback;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.AddressWithCoordinatesDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * UserService Feign客户端
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@FeignClient(name = "user-service", contextId = "delivery-user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    /**
     * 获取地址详情（包含经纬度）
     */
    @GetMapping("/api/user/feign/address-with-coordinates/{addressId}")
    Result<AddressWithCoordinatesDTO> getAddressWithCoordinates(@PathVariable("addressId") Long addressId);

    /**
     * 批量获取地址详情（包含经纬度）
     */
    @PostMapping("/api/user/feign/addresses-with-coordinates")
    Result<List<AddressWithCoordinatesDTO>> getAddressesWithCoordinates(@RequestBody List<Long> addressIds);

    /**
     * 验证用户是否存在
     */
    @GetMapping("/api/user/feign/validate/{userId}")
    Result<Boolean> validateUser(@PathVariable("userId") Long userId);
}
