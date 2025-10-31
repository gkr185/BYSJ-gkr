package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.StockRequest;
import com.bcu.edu.entity.Product;
import com.bcu.edu.service.ProductManagementService;
import com.bcu.edu.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Feign内部接口Controller
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Tag(name = "商品Feign接口", description = "商品服务内部接口（供其他微服务调用）")
@RestController
@RequestMapping("/feign/product")
@RequiredArgsConstructor
public class FeignController {
    
    private final StockService stockService;
    private final ProductManagementService productService;
    
    @Operation(summary = "扣减库存")
    @PostMapping("/{productId}/stock/deduct")
    public Result<Boolean> deductStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockRequest request) {
        return Result.success(stockService.deductStock(productId, request.getQuantity()));
    }
    
    @Operation(summary = "恢复库存")
    @PostMapping("/{productId}/stock/restore")
    public Result<Boolean> restoreStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockRequest request) {
        return Result.success(stockService.restoreStock(productId, request.getQuantity()));
    }
    
    @Operation(summary = "检查商品是否可售")
    @GetMapping("/{productId}/check")
    public Result<Boolean> checkProductAvailable(@PathVariable Long productId) {
        return Result.success(stockService.checkProductAvailable(productId));
    }
    
    @Operation(summary = "批量检查商品状态")
    @PostMapping("/batch-check")
    public Result<Map<Long, Boolean>> batchCheckProducts(@RequestBody List<Long> productIds) {
        Map<Long, Boolean> result = productIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> stockService.checkProductAvailable(id)
                ));
        return Result.success(result);
    }
    
    @Operation(summary = "获取商品信息（快照用）")
    @GetMapping("/{productId}/info")
    public Result<Product> getProductInfo(@PathVariable Long productId) {
        return Result.success(productService.getProductById(productId));
    }
    
    @Operation(summary = "批量获取商品信息")
    @PostMapping("/batch-info")
    public Result<List<Product>> batchGetProducts(@RequestBody List<Long> productIds) {
        List<Product> products = productIds.stream()
                .map(id -> productService.getProductById(id))
                .collect(Collectors.toList());
        return Result.success(products);
    }
}

