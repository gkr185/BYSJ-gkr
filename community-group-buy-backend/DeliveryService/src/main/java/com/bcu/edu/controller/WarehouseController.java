package com.bcu.edu.controller;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.result.Result;
import com.bcu.edu.entity.WarehouseConfig;
import com.bcu.edu.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 仓库管理Controller
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@RestController
@RequestMapping("/api/delivery/warehouse")
@RequiredArgsConstructor
@Tag(name = "仓库管理", description = "仓库配置、默认仓库管理")
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * 查询仓库列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询仓库列表", description = "查询所有仓库（包含禁用的）")
    public Result<List<WarehouseConfig>> listWarehouses() {
        List<WarehouseConfig> warehouses = warehouseService.listAllWarehouses();
        return Result.success(warehouses);
    }

    /**
     * 查询启用的仓库列表
     */
    @GetMapping("/active")
    @Operation(summary = "查询启用的仓库", description = "查询所有启用状态的仓库")
    public Result<List<WarehouseConfig>> listActiveWarehouses() {
        List<WarehouseConfig> warehouses = warehouseService.listActiveWarehouses();
        return Result.success(warehouses);
    }

    /**
     * 查询默认仓库
     */
    @GetMapping("/default")
    @Operation(summary = "查询默认仓库", description = "获取系统默认仓库配置")
    public Result<WarehouseConfig> getDefaultWarehouse() {
        WarehouseConfig warehouse = warehouseService.getDefaultWarehouse();
        return Result.success(warehouse);
    }

    /**
     * 查询仓库详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询仓库详情", description = "根据ID查询仓库详细信息")
    public Result<WarehouseConfig> getWarehouse(
            @Parameter(description = "仓库ID") @PathVariable Long id) {
        WarehouseConfig warehouse = warehouseService.getWarehouseById(id);
        return Result.success(warehouse);
    }

    /**
     * 创建仓库
     */
    @PostMapping
    @OperationLog(value = "创建仓库", module = "仓库管理")
    @Operation(summary = "创建仓库", description = "新增仓库配置")
    public Result<WarehouseConfig> createWarehouse(@Valid @RequestBody WarehouseConfig warehouse) {
        WarehouseConfig created = warehouseService.createWarehouse(warehouse);
        return Result.success("仓库创建成功", created);
    }

    /**
     * 更新仓库
     */
    @PutMapping("/{id}")
    @OperationLog(value = "更新仓库", module = "仓库管理")
    @Operation(summary = "更新仓库", description = "修改仓库配置信息")
    public Result<WarehouseConfig> updateWarehouse(
            @Parameter(description = "仓库ID") @PathVariable Long id,
            @Valid @RequestBody WarehouseConfig warehouse) {
        WarehouseConfig updated = warehouseService.updateWarehouse(id, warehouse);
        return Result.success("仓库更新成功", updated);
    }

    /**
     * 删除仓库
     */
    @DeleteMapping("/{id}")
    @OperationLog(value = "删除仓库", module = "仓库管理")
    @Operation(summary = "删除仓库", description = "删除仓库（默认仓库不能删除）")
    public Result<Void> deleteWarehouse(
            @Parameter(description = "仓库ID") @PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return Result.success("仓库删除成功");
    }

    /**
     * 设置默认仓库
     */
    @PutMapping("/{id}/setDefault")
    @OperationLog(value = "设置默认仓库", module = "仓库管理")
    @Operation(summary = "设置默认仓库", description = "将指定仓库设置为默认仓库")
    public Result<Void> setDefaultWarehouse(
            @Parameter(description = "仓库ID") @PathVariable Long id) {
        warehouseService.setDefaultWarehouse(id);
        return Result.success("默认仓库设置成功");
    }
}

