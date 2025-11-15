package com.bcu.edu.repository;

import com.bcu.edu.entity.WarehouseConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 仓库配置Repository
 * 
 * @author 耿康瑞
 * @since 2025-11-15
 */
@Repository
public interface WarehouseConfigRepository extends JpaRepository<WarehouseConfig, Long> {

    /**
     * 查询默认仓库
     */
    Optional<WarehouseConfig> findByIsDefault(Integer isDefault);

    /**
     * 查询所有启用的仓库
     */
    List<WarehouseConfig> findByStatus(Integer status);

    /**
     * 根据仓库名称查询
     */
    Optional<WarehouseConfig> findByWarehouseName(String warehouseName);

    /**
     * 检查仓库名称是否存在
     */
    boolean existsByWarehouseName(String warehouseName);

    /**
     * 检查是否存在默认仓库
     */
    @Query("SELECT COUNT(w) > 0 FROM WarehouseConfig w WHERE w.isDefault = 1")
    boolean existsDefaultWarehouse();

    /**
     * 取消所有默认仓库（设置新默认仓库前调用）
     */
    @Modifying
    @Query("UPDATE WarehouseConfig w SET w.isDefault = 0 WHERE w.isDefault = 1")
    void clearAllDefaultWarehouse();

    /**
     * 设置默认仓库
     */
    @Modifying
    @Query("UPDATE WarehouseConfig w SET w.isDefault = 1 WHERE w.id = :id")
    void setDefaultWarehouse(@Param("id") Long id);

    /**
     * 统计启用的仓库数量
     */
    Long countByStatus(Integer status);
}

