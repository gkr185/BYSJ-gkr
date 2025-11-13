package com.bcu.edu.controller;

import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.WarehouseConfig;
import com.bcu.edu.service.WarehouseConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 仓库配置管理控制器
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@RestController
@RequestMapping("/api/delivery/warehouse")
@Tag(name = "仓库管理", description = "配送起点仓库配置管理")
@Validated
public class WarehouseConfigController {

    @Autowired
    private WarehouseConfigService warehouseConfigService;

    /**
     * 获取默认仓库
     */
    @GetMapping("/default")
    @Operation(summary = "获取默认仓库", description = "获取当前设置的默认仓库配置")
    public Result<WarehouseConfig> getDefaultWarehouse() {
        WarehouseConfig warehouse = warehouseConfigService.getDefaultWarehouse();
        return Result.success(warehouse);
    }

    /**
     * 获取所有启用的仓库
     */
    @GetMapping("/enabled")
    @Operation(summary = "获取启用的仓库列表", description = "获取所有状态为启用的仓库配置")
    public Result<List<WarehouseConfig>> getEnabledWarehouses() {
        List<WarehouseConfig> warehouses = warehouseConfigService.getEnabledWarehouses();
        return Result.success(warehouses);
    }

    /**
     * 根据ID查询仓库
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询仓库详情", description = "根据仓库ID查询详细配置")
    public Result<WarehouseConfig> getWarehouse(
            @Parameter(description = "仓库ID", required = true)
            @PathVariable @NotNull Long id) {
        
        WarehouseConfig warehouse = warehouseConfigService.getWarehouseById(id);
        return Result.success(warehouse);
    }

    /**
     * 创建仓库配置
     */
    @PostMapping
    @Operation(summary = "创建仓库", description = "创建新的仓库配置")
    public Result<WarehouseConfig> createWarehouse(@Valid @RequestBody WarehouseConfig warehouse) {
        log.info("创建仓库配置: {}", warehouse.getWarehouseName());
        
        WarehouseConfig created = warehouseConfigService.createWarehouse(warehouse);
        return Result.success("仓库创建成功", created);
    }

    /**
     * 更新仓库配置
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新仓库", description = "更新仓库配置信息")
    public Result<WarehouseConfig> updateWarehouse(
            @Parameter(description = "仓库ID", required = true)
            @PathVariable @NotNull Long id,
            @Valid @RequestBody WarehouseConfig warehouse) {
        
        log.info("更新仓库配置: {}", id);
        
        WarehouseConfig updated = warehouseConfigService.updateWarehouse(id, warehouse);
        return Result.success("仓库更新成功", updated);
    }

    /**
     * 删除仓库配置
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除仓库", description = "删除仓库配置（不能删除默认仓库）")
    public Result<Void> deleteWarehouse(
            @Parameter(description = "仓库ID", required = true)
            @PathVariable @NotNull Long id) {
        
        log.info("删除仓库配置: {}", id);
        
        warehouseConfigService.deleteWarehouse(id);
        return Result.success("仓库删除成功");
    }

    /**
     * 设置默认仓库
     */
    @PutMapping("/{id}/default")
    @Operation(summary = "设置默认仓库", description = "将指定仓库设置为默认仓库")
    public Result<Void> setDefaultWarehouse(
            @Parameter(description = "仓库ID", required = true)
            @PathVariable @NotNull Long id) {
        
        log.info("设置默认仓库: {}", id);
        
        warehouseConfigService.setDefaultWarehouse(id);
        return Result.success("默认仓库设置成功");
    }

    /**
     * 切换仓库状态
     */
    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "切换仓库状态", description = "启用或禁用仓库（不能禁用默认仓库）")
    public Result<WarehouseConfig> toggleWarehouseStatus(
            @Parameter(description = "仓库ID", required = true)
            @PathVariable @NotNull Long id) {
        
        log.info("切换仓库状态: {}", id);
        
        WarehouseConfig warehouse = warehouseConfigService.toggleWarehouseStatus(id);
        return Result.success("仓库状态切换成功", warehouse);
    }

    /**
     * 获取仓库统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取仓库统计", description = "获取仓库配置的统计信息")
    public Result<WarehouseConfigService.WarehouseStatistics> getWarehouseStatistics() {
        WarehouseConfigService.WarehouseStatistics statistics = 
                warehouseConfigService.getWarehouseStatistics();
        return Result.success(statistics);
    }
}
