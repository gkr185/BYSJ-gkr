package com.bcu.edu.feign;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.AddressDTO;
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
 * @since 2025-11-15
 */
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    /**
     * 获取地址坐标信息
     */
    @GetMapping("/api/user/feign/address/{addressId}")
    Result<AddressDTO> getAddressWithCoordinates(@PathVariable Long addressId);

    /**
     * 批量获取地址信息
     */
    @PostMapping("/api/user/feign/address/batch")
    Result<List<AddressDTO>> batchGetAddresses(@RequestBody List<Long> addressIds);
}

