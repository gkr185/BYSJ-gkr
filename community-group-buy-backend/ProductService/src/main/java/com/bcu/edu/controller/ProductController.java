package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.ProductQueryRequest;
import com.bcu.edu.entity.Product;
import com.bcu.edu.service.ProductManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品Controller - C端接口
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Tag(name = "商品", description = "商品相关接口")
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductManagementService productService;
    
    @Operation(summary = "获取商品列表")
    @GetMapping("/list")
    public Result<Page<Product>> getProductList(ProductQueryRequest request) {
        return Result.success(productService.getProductList(request));
    }
    
    @Operation(summary = "获取商品详情")
    @GetMapping("/{id}")
    public Result<Product> getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }
    
    @Operation(summary = "商品搜索")
    @GetMapping("/search")
    public Result<Page<Product>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(productService.searchProducts(keyword, categoryId, PageRequest.of(page, size)));
    }
    
    @Operation(summary = "热门商品推荐")
    @GetMapping("/hot")
    public Result<List<Product>> getHotProducts(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getHotProducts(limit));
    }
    
    @Operation(summary = "推荐商品")
    @GetMapping("/recommend")
    public Result<List<Product>> getRecommendProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getRecommendProducts(categoryId, limit));
    }
    
    @Operation(summary = "查询商品库存")
    @GetMapping("/{id}/stock")
    public Result<Integer> getProductStock(@PathVariable Long id) {
        return Result.success(productService.getProductStock(id));
    }
    
    @Operation(summary = "按分类查询商品")
    @GetMapping("/category/{categoryId}/list")
    public Result<Page<Product>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(productService.getProductsByCategory(categoryId, PageRequest.of(page, size)));
    }
}

