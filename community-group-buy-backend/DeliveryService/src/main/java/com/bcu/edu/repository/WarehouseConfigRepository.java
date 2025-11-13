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
 * 仓库配置数据访问层
 * 
 * @author 耿康瑞
 * @since 2025-11-13
 */
@Repository
public interface WarehouseConfigRepository extends JpaRepository<WarehouseConfig, Long> {

    /**
     * 查询默认仓库
     */
    @Query("SELECT w FROM WarehouseConfig w WHERE w.isDefault = 1 AND w.status = 1")
    Optional<WarehouseConfig> findDefaultWarehouse();

    /**
     * 查询所有启用的仓库
     */
    @Query("SELECT w FROM WarehouseConfig w WHERE w.status = 1 ORDER BY w.isDefault DESC, w.createTime ASC")
    List<WarehouseConfig> findEnabledWarehouses();

    /**
     * 根据名称查询仓库
     */
    Optional<WarehouseConfig> findByWarehouseName(String warehouseName);

    /**
     * 检查仓库名称是否已存在（排除指定ID）
     */
    @Query("SELECT COUNT(w) FROM WarehouseConfig w WHERE w.warehouseName = :name AND w.id != :excludeId")
    Long countByWarehouseNameAndIdNot(@Param("name") String warehouseName, @Param("excludeId") Long excludeId);

    /**
     * 检查仓库名称是否已存在
     */
    boolean existsByWarehouseName(String warehouseName);

    /**
     * 清除所有默认仓库标记
     */
    @Modifying
    @Query("UPDATE WarehouseConfig w SET w.isDefault = 0")
    void clearAllDefaultFlags();

    /**
     * 设置指定仓库为默认仓库
     */
    @Modifying
    @Query("UPDATE WarehouseConfig w SET w.isDefault = CASE WHEN w.id = :id THEN 1 ELSE 0 END")
    void setDefaultWarehouse(@Param("id") Long id);

    /**
     * 统计启用的仓库数量
     */
    @Query("SELECT COUNT(w) FROM WarehouseConfig w WHERE w.status = 1")
    Long countEnabledWarehouses();

    /**
     * 根据状态查询仓库列表
     */
    List<WarehouseConfig> findByStatusOrderByCreateTimeDesc(Integer status);
}
