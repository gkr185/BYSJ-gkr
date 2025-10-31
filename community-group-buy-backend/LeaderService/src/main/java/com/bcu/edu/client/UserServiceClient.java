package com.bcu.edu.client;

import com.bcu.edu.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * UserService Feign客户端
 *
 * @author 耿康瑞
 * @date 2025-10-30
 * @description 调用UserService的内部接口
 */
@FeignClient(
    name = "UserService",
    contextId = "userServiceClient",  // 指定唯一的 contextId，避免与 LogFeignClient 冲突
    path = "/feign"
)
public interface UserServiceClient {

    /**
     * 更新用户角色
     * 
     * @param userId 用户ID
     * @param role 角色（0-普通用户 1-管理员 2-团长）
     * @return 更新结果
     */
    @PostMapping("/user/{userId}/role")
    Result<Void> updateUserRole(
            @PathVariable("userId") Long userId,
            @RequestParam("role") Integer role
    );

    /**
     * 验证用户是否存在
     * 
     * @param userId 用户ID
     * @return true-存在，false-不存在
     */
    @GetMapping("/user/exists/{userId}")
    Result<Boolean> existsUser(@PathVariable("userId") Long userId);

    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息（包含姓名、手机号等）
     */
    @GetMapping("/user/{userId}")
    Result<Object> getUserById(@PathVariable("userId") Long userId);
}

