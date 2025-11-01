package com.bcu.edu.client;

import com.bcu.edu.client.fallback.GroupBuyServiceClientFallback;
import com.bcu.edu.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * GroupBuyService Feign客户端
 * 
 * <p>调用GroupBuyService的支付回调接口
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@FeignClient(name = "groupbuy-service", fallback = GroupBuyServiceClientFallback.class)
public interface GroupBuyServiceClient {

    /**
     * 支付回调（支付成功后调用，触发成团检查）
     * 
     * @param orderId 订单ID
     * @return 成功/失败
     */
    @PostMapping("/api/groupbuy/payment/callback")
    Result<Void> paymentCallback(@RequestParam("orderId") Long orderId);
}

