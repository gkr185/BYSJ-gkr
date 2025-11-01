package com.bcu.edu.client.fallback;

import com.bcu.edu.client.ProductServiceClient;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.response.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public Result<ProductDTO> getProduct(Long productId) {
        log.error("ProductService调用失败: getProduct, productId={}", productId);
        return Result.error("商品服务暂时不可用");
    }

    @Override
    public Result<Void> deductStock(Long productId, Integer quantity) {
        log.error("ProductService调用失败: deductStock, productId={}, quantity={}", productId, quantity);
        return Result.error("商品服务暂时不可用");
    }

    @Override
    public Result<Void> restoreStock(Long productId, Integer quantity) {
        log.error("ProductService调用失败: restoreStock, productId={}, quantity={}", productId, quantity);
        return Result.error("商品服务暂时不可用");
    }
}

