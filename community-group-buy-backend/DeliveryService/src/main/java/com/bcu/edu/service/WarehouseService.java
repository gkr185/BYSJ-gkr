package com.bcu.edu.service;

import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.entity.WarehouseConfig;
import com.bcu.edu.repository.WarehouseConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 仓库管理服务
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseConfigRepository warehouseRepository;

    /**
     * 查询所有启用的仓库
     */
    public List<WarehouseConfig> listActiveWarehouses() {
        return warehouseRepository.findByStatus(1);
    }

    /**
     * 查询所有仓库
     */
    public List<WarehouseConfig> listAllWarehouses() {
        return warehouseRepository.findAll();
    }

    /**
     * 查询仓库详情
     */
    public WarehouseConfig getWarehouseById(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("仓库不存在，ID=" + id));
    }

    /**
     * 查询默认仓库
     */
    public WarehouseConfig getDefaultWarehouse() {
        return warehouseRepository.findByIsDefault(1)
                .orElseThrow(() -> new BusinessException("未设置默认仓库，请先配置"));
    }

    /**
     * 创建仓库
     */
    @Transactional(rollbackFor = Exception.class)
    public WarehouseConfig createWarehouse(WarehouseConfig warehouse) {
        // 1. 校验仓库名称是否重复
        if (warehouseRepository.existsByWarehouseName(warehouse.getWarehouseName())) {
            throw new BusinessException("仓库名称已存在：" + warehouse.getWarehouseName());
        }

        // 2. 如果设置为默认仓库，取消其他默认仓库
        if (warehouse.getIsDefault() != null && warehouse.getIsDefault() == 1) {
            warehouseRepository.clearAllDefaultWarehouse();
            log.info("取消所有默认仓库，新默认仓库={}", warehouse.getWarehouseName());
        }

        // 3. 如果没有默认仓库，自动设置为默认
        if (!warehouseRepository.existsDefaultWarehouse()) {
            warehouse.setIsDefault(1);
            log.info("系统无默认仓库，自动设置{}为默认仓库", warehouse.getWarehouseName());
        }

        // 4. 保存仓库
        WarehouseConfig saved = warehouseRepository.save(warehouse);
        log.info("仓库创建成功，ID={}, 名称={}", saved.getId(), saved.getWarehouseName());

        return saved;
    }

    /**
     * 更新仓库
     */
    @Transactional(rollbackFor = Exception.class)
    public WarehouseConfig updateWarehouse(Long id, WarehouseConfig warehouse) {
        // 1. 查询仓库
        WarehouseConfig existing = getWarehouseById(id);

        // 2. 校验仓库名称是否重复
        if (!existing.getWarehouseName().equals(warehouse.getWarehouseName())) {
            if (warehouseRepository.existsByWarehouseName(warehouse.getWarehouseName())) {
                throw new BusinessException("仓库名称已存在：" + warehouse.getWarehouseName());
            }
        }

        // 3. 更新字段
        existing.setWarehouseName(warehouse.getWarehouseName());
        existing.setAddress(warehouse.getAddress());
        existing.setLongitude(warehouse.getLongitude());
        existing.setLatitude(warehouse.getLatitude());
        existing.setContactPerson(warehouse.getContactPerson());
        existing.setContactPhone(warehouse.getContactPhone());
        existing.setDescription(warehouse.getDescription());
        existing.setStatus(warehouse.getStatus());

        // 4. 保存更新
        WarehouseConfig updated = warehouseRepository.save(existing);
        log.info("仓库更新成功，ID={}", id);

        return updated;
    }

    /**
     * 删除仓库
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteWarehouse(Long id) {
        // 1. 查询仓库
        WarehouseConfig warehouse = getWarehouseById(id);

        // 2. 如果是默认仓库，禁止删除
        if (warehouse.getIsDefault() == 1) {
            throw new BusinessException("默认仓库不能删除，请先设置其他仓库为默认");
        }

        // 3. 删除仓库
        warehouseRepository.deleteById(id);
        log.info("仓库删除成功，ID={}", id);
    }

    /**
     * 设置默认仓库
     */
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultWarehouse(Long id) {
        // 1. 查询仓库
        WarehouseConfig warehouse = getWarehouseById(id);

        // 2. 校验仓库状态
        if (warehouse.getStatus() == 0) {
            throw new BusinessException("禁用的仓库不能设置为默认仓库");
        }

        // 3. 取消所有默认仓库
        warehouseRepository.clearAllDefaultWarehouse();

        // 4. 设置新的默认仓库
        warehouseRepository.setDefaultWarehouse(id);
        log.info("默认仓库设置成功，ID={}, 名称={}", id, warehouse.getWarehouseName());
    }
}

