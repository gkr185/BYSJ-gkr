package com.bcu.edu.repository;

import com.bcu.edu.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品Repository
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 查询商品列表（按创建时间倒序）
     * 
     * @param status 状态（1-上架）
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<Product> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);
    
    /**
     * 按分类查询商品
     * 
     * @param categoryId 分类ID
     * @param status 状态
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    Page<Product> findByCategoryIdAndStatusOrderByCreateTimeDesc(Long categoryId, Integer status, Pageable pageable);
    
    /**
     * 商品搜索（名称模糊匹配 + 分类筛选）
     * 
     * @param keyword 搜索关键词
     * @param categoryId 分类ID（可为null）
     * @param pageable 分页参数
     * @return 商品分页列表
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR p.productName LIKE %:keyword%) " +
           "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
           "AND p.status = 1")
    Page<Product> searchProducts(@Param("keyword") String keyword, 
                                  @Param("categoryId") Long categoryId, 
                                  Pageable pageable);
    
    /**
     * 统计商品数量（按状态）
     * 
     * @param status 状态
     * @return 商品数量
     */
    Long countByStatus(Integer status);
    
    /**
     * 统计各分类商品数量
     * 
     * @return 分类ID和商品数量的列表
     */
    @Query("SELECT p.categoryId, COUNT(p) FROM Product p WHERE p.status = 1 GROUP BY p.categoryId")
    List<Object[]> countByCategory();
    
    /**
     * 查询库存预警商品
     * 
     * @param stock 库存阈值
     * @param status 状态
     * @return 低库存商品列表
     */
    List<Product> findByStockLessThanEqualAndStatus(Integer stock, Integer status);
    
    /**
     * 统计今日新增商品数
     * 
     * @param start 开始时间
     * @param end 结束时间
     * @return 新增商品数
     */
    Long countByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * 扣减库存（乐观锁）
     * 
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 影响行数（0表示扣减失败）
     */
    @Modifying
    @Query(value = "UPDATE product SET stock = stock - :quantity " +
                   "WHERE product_id = :productId " +
                   "AND stock >= :quantity " +
                   "AND status = 1", 
           nativeQuery = true)
    int deductStockOptimistic(@Param("productId") Long productId, 
                              @Param("quantity") Integer quantity);
    
    /**
     * 恢复库存
     * 
     * @param productId 商品ID
     * @param quantity 恢复数量
     * @return 影响行数
     */
    @Modifying
    @Query(value = "UPDATE product SET stock = stock + :quantity " +
                   "WHERE product_id = :productId", 
           nativeQuery = true)
    int restoreStock(@Param("productId") Long productId, 
                     @Param("quantity") Integer quantity);
    
    /**
     * 统计分类下的商品数量
     * 
     * @param categoryId 分类ID
     * @return 商品数量
     */
    Long countByCategoryId(Long categoryId);
}

