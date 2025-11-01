package com.bcu.edu.client;

import com.bcu.edu.client.fallback.ProductServiceClientFallback;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ProductService Feign客户端
 * 
 * @author 耿康瑞
 * @since 2025-11-01
 */
@FeignClient(name = "product-service", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {

    /**
     * 获取商品详情
     */
    @GetMapping("/api/product/feign/{productId}")
    Result<ProductDTO> getProduct(@PathVariable("productId") Long productId);

    /**
     * 扣减库存
     */
    @PostMapping("/api/product/feign/deductStock")
    Result<Void> deductStock(@RequestParam("productId") Long productId, 
                             @RequestParam("quantity") Integer quantity);

    /**
     * 恢复库存（取消订单时）
     */
    @PostMapping("/api/product/feign/restoreStock")
    Result<Void> restoreStock(@RequestParam("productId") Long productId, 
                              @RequestParam("quantity") Integer quantity);
}

