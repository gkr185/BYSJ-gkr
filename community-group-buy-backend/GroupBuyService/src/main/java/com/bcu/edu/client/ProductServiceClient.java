package com.bcu.edu.client;

import com.bcu.edu.client.fallback.ProductServiceClientFallback;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ProductService Feign客户端
 * 
 * <p>功能：
 * <ul>
 *   <li>获取商品信息（创建活动时验证商品存在）</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@FeignClient(name = "product-service", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {
    
    /**
     * 获取商品信息
     * 
     * <p>应用场景：
     * <ul>
     *   <li>创建拼团活动时验证商品存在</li>
     *   <li>验证拼团价必须小于商品原价</li>
     * </ul>
     * 
     * @param productId 商品ID
     * @return Result<ProductDTO>
     */
    @GetMapping("/feign/product/{productId}/info")
    Result<ProductDTO> getProduct(@PathVariable("productId") Long productId);
}

