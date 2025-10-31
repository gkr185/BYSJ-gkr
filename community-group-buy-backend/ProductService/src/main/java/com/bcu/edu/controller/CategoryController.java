package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.CategoryTreeNode;
import com.bcu.edu.entity.Product;
import com.bcu.edu.entity.ProductCategory;
import com.bcu.edu.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类Controller - C端接口
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Tag(name = "商品分类", description = "商品分类相关接口")
@RestController
@RequestMapping("/api/product/category")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    @Operation(summary = "获取分类列表")
    @GetMapping("/list")
    public Result<List<ProductCategory>> getCategoryList() {
        return Result.success(categoryService.getAllCategories());
    }
    
    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public Result<List<CategoryTreeNode>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }
    
    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<ProductCategory> getCategoryById(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryById(id));
    }
    
    @Operation(summary = "获取分类下的商品")
    @GetMapping("/{id}/products")
    public Result<Page<Product>> getProductsByCategoryId(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(categoryService.getProductsByCategoryId(id, PageRequest.of(page, size)));
    }
}

