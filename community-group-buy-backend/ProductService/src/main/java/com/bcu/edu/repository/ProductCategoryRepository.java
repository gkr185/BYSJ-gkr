package com.bcu.edu.repository;

import com.bcu.edu.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品分类Repository
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {
    
    /**
     * 查询所有启用的分类（按排序权重排序）
     * 
     * @param status 状态（1-启用）
     * @return 分类列表
     */
    List<ProductCategory> findByStatusOrderBySortAsc(Integer status);
    
    /**
     * 查询指定父分类下的子分类
     * 
     * @param parentId 父分类ID
     * @param status 状态
     * @return 子分类列表
     */
    List<ProductCategory> findByParentIdAndStatusOrderBySortAsc(Long parentId, Integer status);
    
    /**
     * 检查分类名称是否存在
     * 
     * @param categoryName 分类名称
     * @return 是否存在
     */
    boolean existsByCategoryName(String categoryName);
    
    /**
     * 检查分类名称是否存在（排除指定ID）
     * 
     * @param categoryName 分类名称
     * @param categoryId 要排除的分类ID
     * @return 是否存在
     */
    boolean existsByCategoryNameAndCategoryIdNot(String categoryName, Long categoryId);
    
    /**
     * 查询分类下的商品数量
     * 
     * @param categoryId 分类ID
     * @return 商品数量
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.categoryId = :categoryId")
    Long countProductsByCategoryId(@Param("categoryId") Long categoryId);
}

