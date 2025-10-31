package com.bcu.edu.controller;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.CategoryRequest;
import com.bcu.edu.entity.ProductCategory;
import com.bcu.edu.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 分类Controller - 管理端接口
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Tag(name = "商品分类管理", description = "商品分类管理接口（管理员）")
@RestController
@RequestMapping("/api/product/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {
    
    private final CategoryService categoryService;
    
    @OperationLog(value = "创建分类", module = "商品分类")
    @Operation(summary = "创建分类")
    @PostMapping
    public Result<ProductCategory> createCategory(@Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.createCategory(request));
    }
    
    @OperationLog(value = "更新分类", module = "商品分类")
    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<ProductCategory> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return Result.success(categoryService.updateCategory(id, request));
    }
    
    @OperationLog(value = "删除分类", module = "商品分类")
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
    
    @OperationLog(value = "调整排序", module = "商品分类")
    @Operation(summary = "调整分类排序")
    @PutMapping("/{id}/sort")
    public Result<ProductCategory> updateCategorySort(
            @PathVariable Long id,
            @RequestParam Integer sort) {
        return Result.success(categoryService.updateCategorySort(id, sort));
    }
}

