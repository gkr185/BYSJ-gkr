package com.bcu.edu.service;

import com.bcu.edu.dto.ProductStatisticsVO;
import com.bcu.edu.entity.Product;
import com.bcu.edu.entity.ProductCategory;
import com.bcu.edu.repository.ProductCategoryRepository;
import com.bcu.edu.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品统计服务
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final StockService stockService;
    
    /**
     * 获取商品统计数据
     */
    public ProductStatisticsVO getProductStatistics() {
        ProductStatisticsVO stats = new ProductStatisticsVO();
        
        // 1. 商品总数
        stats.setTotalProducts(productRepository.count());
        
        // 2. 上架商品数
        stats.setOnSaleProducts(productRepository.countByStatus(1));
        
        // 3. 下架商品数
        stats.setOffSaleProducts(productRepository.countByStatus(0));
        
        // 4. 库存预警商品数（库存≤10）
        List<Product> lowStockProducts = stockService.getLowStockProducts(10);
        stats.setLowStockProducts((long) lowStockProducts.size());
        
        // 5. 今日新增商品数
        stats.setTodayNewProducts(getTodayNewProductCount());
        
        // 6. 分类统计
        // stats.setCategoryStats(getCategoryStatistics());
        
        return stats;
    }
    
    /**
     * 获取分类统计
     */
    /*
    private List<CategoryStatVO> getCategoryStatistics() {
        // 查询所有分类
        List<ProductCategory> categories = categoryRepository.findByStatusOrderBySortAsc(1);
        
        // 查询各分类商品数量
        List<Object[]> categoryCounts = productRepository.countByCategory();
        Map<Long, Long> countMap = categoryCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));
        
        // 构建统计结果
        return categories.stream()
                .map(category -> new CategoryStatVO(
                        category.getCategoryId(),
                        category.getCategoryName(),
                        countMap.getOrDefault(category.getCategoryId(), 0L)
                ))
                .collect(Collectors.toList());
    }
    */
    
    /**
     * 统计今日新增商品数
     */
    private Long getTodayNewProductCount() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return productRepository.countByCreateTimeBetween(startOfDay, endOfDay);
    }
    
    /**
     * 获取低库存商品列表
     */
    public List<Product> getLowStockProducts(Integer threshold) {
        return stockService.getLowStockProducts(threshold);
    }
}

