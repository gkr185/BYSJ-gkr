package com.bcu.edu.client.fallback;

import com.bcu.edu.client.ProductServiceClient;
import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * ProductService Feign客户端降级处理
 * 
 * @author 耿康瑞
 * @since 2025-10-31
 */
@Component
@Slf4j
public class ProductServiceClientFallback implements ProductServiceClient {
    
    @Override
    public Result<ProductDTO> getProduct(Long productId) {
        log.error("ProductService调用失败，productId={}", productId);
        throw new BusinessException(ResultCode.SERVICE_UNAVAILABLE, 
            "商品服务暂时不可用，请稍后重试");
    }
}

