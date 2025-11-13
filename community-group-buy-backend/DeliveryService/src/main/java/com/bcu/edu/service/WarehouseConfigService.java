package com.bcu.edu.service;

import com.bcu.edu.common.annotation.OperationLog;
import com.bcu.edu.common.enums.ResultCode;
import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.entity.WarehouseConfig;
import com.bcu.edu.repository.WarehouseConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 仓库配置服务
 * 
 * <p>主要功能：
 * <ul>
 *   <li>仓库信息管理</li>
 *   <li>默认仓库设置</li>
 *   <li>仓库状态管理</li>
 *   <li>仓库坐标验证</li>
 * </ul>
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Slf4j
@Service
@Transactional
public class WarehouseConfigService {

    @Autowired
    private WarehouseConfigRepository warehouseConfigRepository;

    @Value("${warehouse.default.name:中心仓库}")
    private String defaultWarehouseName;

    @Value("${warehouse.default.address:北京市朝阳区示例地址}")
    private String defaultWarehouseAddress;

    @Value("${warehouse.default.longitude:116.397128}")
    private String defaultLongitude;

    @Value("${warehouse.default.latitude:39.916527}")
    private String defaultLatitude;

    /**
     * 获取默认仓库
     */
    public WarehouseConfig getDefaultWarehouse() {
        return warehouseConfigRepository.findDefaultWarehouse()
                .orElseGet(this::createDefaultWarehouse);
    }

    /**
     * 创建默认仓库配置
     */
    private WarehouseConfig createDefaultWarehouse() {
        log.info("创建默认仓库配置");
        
        WarehouseConfig warehouse = new WarehouseConfig();
        warehouse.setWarehouseName(defaultWarehouseName);
        warehouse.setAddress(defaultWarehouseAddress);
        warehouse.setLongitude(new BigDecimal(defaultLongitude));
        warehouse.setLatitude(new BigDecimal(defaultLatitude));
        warehouse.setAsDefault();
        warehouse.enable();
        warehouse.setDescription("系统默认仓库，用于配送路径规划起点");
        
        return warehouseConfigRepository.save(warehouse);
    }

    /**
     * 获取所有启用的仓库
     */
    public List<WarehouseConfig> getEnabledWarehouses() {
        return warehouseConfigRepository.findEnabledWarehouses();
    }

    /**
     * 根据ID获取仓库
     */
    public WarehouseConfig getWarehouseById(Long id) {
        return warehouseConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND, "仓库配置不存在"));
    }

    /**
     * 创建仓库配置
     */
    @OperationLog(value = "创建仓库", module = "仓库管理")
    public WarehouseConfig createWarehouse(WarehouseConfig warehouse) {
        log.info("创建仓库配置: {}", warehouse.getWarehouseName());
        
        // 检查名称唯一性
        if (warehouseConfigRepository.existsByWarehouseName(warehouse.getWarehouseName())) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "仓库名称已存在");
        }
        
        // 如果设置为默认仓库，先清除其他默认标记
        if (warehouse.isDefaultWarehouse()) {
            warehouseConfigRepository.clearAllDefaultFlags();
        }
        
        warehouse.enable(); // 新创建的仓库默认启用
        return warehouseConfigRepository.save(warehouse);
    }

    /**
     * 更新仓库配置
     */
    @OperationLog(value = "更新仓库", module = "仓库管理")
    public WarehouseConfig updateWarehouse(Long id, WarehouseConfig warehouse) {
        log.info("更新仓库配置: {}", id);
        
        WarehouseConfig existing = getWarehouseById(id);
        
        // 检查名称唯一性
        if (!existing.getWarehouseName().equals(warehouse.getWarehouseName())) {
            Long duplicateCount = warehouseConfigRepository.countByWarehouseNameAndIdNot(
                    warehouse.getWarehouseName(), id);
            if (duplicateCount > 0) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR, "仓库名称已存在");
            }
        }
        
        // 更新字段
        existing.setWarehouseName(warehouse.getWarehouseName());
        existing.setAddress(warehouse.getAddress());
        existing.setLongitude(warehouse.getLongitude());
        existing.setLatitude(warehouse.getLatitude());
        existing.setContactPerson(warehouse.getContactPerson());
        existing.setContactPhone(warehouse.getContactPhone());
        existing.setDescription(warehouse.getDescription());
        existing.setStatus(warehouse.getStatus());
        
        // 如果设置为默认仓库，先清除其他默认标记
        if (warehouse.isDefaultWarehouse() && !existing.isDefaultWarehouse()) {
            warehouseConfigRepository.clearAllDefaultFlags();
            existing.setAsDefault();
        } else if (!warehouse.isDefaultWarehouse() && existing.isDefaultWarehouse()) {
            existing.unsetDefault();
        }
        
        return warehouseConfigRepository.save(existing);
    }

    /**
     * 删除仓库配置
     */
    @OperationLog(value = "删除仓库", module = "仓库管理")
    public void deleteWarehouse(Long id) {
        log.info("删除仓库配置: {}", id);
        
        WarehouseConfig warehouse = getWarehouseById(id);
        
        // 不能删除默认仓库
        if (warehouse.isDefaultWarehouse()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能删除默认仓库");
        }
        
        warehouseConfigRepository.delete(warehouse);
    }

    /**
     * 设置默认仓库
     */
    @OperationLog(value = "设置默认仓库", module = "仓库管理")
    public void setDefaultWarehouse(Long id) {
        log.info("设置默认仓库: {}", id);
        
        WarehouseConfig warehouse = getWarehouseById(id);
        
        if (!warehouse.isEnabled()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "禁用的仓库不能设置为默认仓库");
        }
        
        warehouseConfigRepository.setDefaultWarehouse(id);
    }

    /**
     * 启用/禁用仓库
     */
    @OperationLog(value = "切换仓库状态", module = "仓库管理")
    public WarehouseConfig toggleWarehouseStatus(Long id) {
        log.info("切换仓库状态: {}", id);
        
        WarehouseConfig warehouse = getWarehouseById(id);
        
        if (warehouse.isDefaultWarehouse() && warehouse.isEnabled()) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "不能禁用默认仓库");
        }
        
        if (warehouse.isEnabled()) {
            warehouse.disable();
        } else {
            warehouse.enable();
        }
        
        return warehouseConfigRepository.save(warehouse);
    }

    /**
     * 获取仓库统计信息
     */
    public WarehouseStatistics getWarehouseStatistics() {
        WarehouseStatistics stats = new WarehouseStatistics();
        
        List<WarehouseConfig> allWarehouses = warehouseConfigRepository.findAll();
        stats.setTotalCount(allWarehouses.size());
        
        Long enabledCount = warehouseConfigRepository.countEnabledWarehouses();
        stats.setEnabledCount(enabledCount.intValue());
        stats.setDisabledCount(allWarehouses.size() - enabledCount.intValue());
        
        boolean hasDefault = warehouseConfigRepository.findDefaultWarehouse().isPresent();
        stats.setHasDefault(hasDefault);
        
        return stats;
    }

    /**
     * 仓库统计信息
     */
    public static class WarehouseStatistics {
        private Integer totalCount = 0;
        private Integer enabledCount = 0;
        private Integer disabledCount = 0;
        private Boolean hasDefault = false;

        // Getters and Setters
        public Integer getTotalCount() { return totalCount; }
        public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

        public Integer getEnabledCount() { return enabledCount; }
        public void setEnabledCount(Integer enabledCount) { this.enabledCount = enabledCount; }

        public Integer getDisabledCount() { return disabledCount; }
        public void setDisabledCount(Integer disabledCount) { this.disabledCount = disabledCount; }

        public Boolean getHasDefault() { return hasDefault; }
        public void setHasDefault(Boolean hasDefault) { this.hasDefault = hasDefault; }
    }
}
