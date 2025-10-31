package com.bcu.edu.client;

import com.bcu.edu.client.fallback.UserServiceClientFallback;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * UserService Feign客户端（⭐核心客户端）
 * 
 * <p>功能：
 * <ul>
 *   <li>获取用户信息（验证团长身份、获取社区ID）</li>
 *   <li>退款到用户余额（定时任务退款、中途退出退款）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {
    
    /**
     * 获取用户信息（⭐核心接口）
     * 
     * <p>应用场景：
     * <ul>
     *   <li>团长发起拼团时验证身份（role=2）</li>
     *   <li>获取团长的社区ID（v3.0自动关联）</li>
     * </ul>
     * 
     * @param userId 用户ID
     * @return Result<UserInfoDTO>
     */
    @GetMapping("/api/user/feign/info/{userId}")
    Result<UserInfoDTO> getUserInfo(@PathVariable("userId") Long userId);
    
    /**
     * 退款到用户余额
     * 
     * <p>应用场景：
     * <ul>
     *   <li>定时任务：过期团自动退款</li>
     *   <li>用户主动退出拼团</li>
     * </ul>
     * 
     * @param userId 用户ID
     * @param amount 退款金额
     * @return Result<Void>
     */
    @PostMapping("/api/account/feign/refund")
    Result<Void> refundToBalance(@RequestParam("userId") Long userId, 
                                  @RequestParam("amount") BigDecimal amount);
}

