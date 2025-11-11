package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.StockRequest;
import com.bcu.edu.dto.response.ProductDTO;
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
@RequestMapping("/api/product/feign")
@RequiredArgsConstructor
public class FeignController {
    
    private final StockService stockService;
    private final ProductManagementService productService;

    @Operation(summary = "获取商品详情")
    @GetMapping("/{productId}")
    public Result<ProductDTO> getProduct(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setCoverImg(product.getCoverImg());
        dto.setPrice(product.getPrice());
        dto.setGroupPrice(product.getGroupPrice());
        dto.setStock(product.getStock());
        dto.setStatus(product.getStatus());

        return Result.success(dto);
    }

    @Operation(summary = "扣减库存")
    @PostMapping("/deductStock")
    public Result<Void> deductStock(@RequestParam("productId") Long productId,
                                   @RequestParam("quantity") Integer quantity) {
        stockService.deductStock(productId, quantity);
        return Result.success("库存扣减成功");
    }

    @Operation(summary = "恢复库存")
    @PostMapping("/restoreStock")
    public Result<Void> restoreStock(@RequestParam("productId") Long productId,
                                    @RequestParam("quantity") Integer quantity) {
        stockService.restoreStock(productId, quantity);
        return Result.success("库存恢复成功");
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
    public Result<ProductDTO> getProductInfo(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setCoverImg(product.getCoverImg());
        dto.setPrice(product.getPrice());
        dto.setGroupPrice(product.getGroupPrice());
        dto.setStock(product.getStock());
        dto.setStatus(product.getStatus());

        return Result.success(dto);
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

