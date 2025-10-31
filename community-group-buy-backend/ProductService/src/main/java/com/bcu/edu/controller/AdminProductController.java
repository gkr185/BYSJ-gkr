package com.bcu.edu.controller;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.dto.ProductRequest;
import com.bcu.edu.dto.ProductStatisticsVO;
import com.bcu.edu.entity.Product;
import com.bcu.edu.service.FileUploadService;
import com.bcu.edu.service.ProductManagementService;
import com.bcu.edu.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商品Controller - 管理端接口
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Tag(name = "商品管理", description = "商品管理接口（管理员）")
@RestController
@RequestMapping("/api/product/admin")
@RequiredArgsConstructor
public class AdminProductController {
    
    private final ProductManagementService productService;
    private final FileUploadService fileUploadService;
    private final StatisticsService statisticsService;
    
    @OperationLog(value = "创建商品", module = "商品管理")
    @Operation(summary = "创建商品")
    @PostMapping("/product")
    public Result<Product> createProduct(@Valid @RequestBody ProductRequest request) {
        return Result.success(productService.createProduct(request));
    }
    
    @OperationLog(value = "更新商品", module = "商品管理")
    @Operation(summary = "更新商品")
    @PutMapping("/product/{id}")
    public Result<Product> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return Result.success(productService.updateProduct(id, request));
    }
    
    @OperationLog(value = "删除商品", module = "商品管理")
    @Operation(summary = "删除商品")
    @DeleteMapping("/product/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }
    
    @OperationLog(value = "上下架商品", module = "商品管理")
    @Operation(summary = "更新商品状态（上下架）")
    @PutMapping("/product/{id}/status")
    public Result<Product> updateProductStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        return Result.success(productService.updateProductStatus(id, status));
    }
    
    @OperationLog(value = "调整库存", module = "商品管理")
    @Operation(summary = "调整商品库存")
    @PutMapping("/product/{id}/stock")
    public Result<Product> adjustStock(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        return Result.success(productService.adjustStock(id, quantity));
    }
    
    @OperationLog(value = "上传图片", module = "商品管理")
    @Operation(summary = "上传商品图片", description = "返回图片访问URL")
    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileUploadService.uploadFile(file);
        // 使用带data的success方法，确保URL在data字段中
        return Result.success("上传成功", imageUrl);
    }
    
    @Operation(summary = "获取商品列表（管理端）")
    @GetMapping("/product/list")
    public Result<Page<Product>> getAdminProductList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(productService.getAdminProductList(PageRequest.of(page, size)));
    }
    
    @Operation(summary = "获取商品统计数据")
    @GetMapping("/statistics")
    public Result<ProductStatisticsVO> getStatistics() {
        return Result.success(statisticsService.getProductStatistics());
    }
}

