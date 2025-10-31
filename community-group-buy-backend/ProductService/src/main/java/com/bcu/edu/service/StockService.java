package com.bcu.edu.service;

import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.entity.Product;
import com.bcu.edu.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 库存管理服务
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {
    
    private final ProductRepository productRepository;
    
    /**
     * 扣减库存（乐观锁）
     * 
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    @Transactional
    public boolean deductStock(Long productId, Integer quantity) {
        log.info("扣减库存: productId={}, quantity={}", productId, quantity);
        
        // 使用乐观锁扣减库存
        int affectedRows = productRepository.deductStockOptimistic(productId, quantity);
        
        if (affectedRows == 0) {
            log.error("库存扣减失败: productId={}, quantity={}", productId, quantity);
            throw new BusinessException("库存不足或商品已下架");
        }
        
        log.info("库存扣减成功: productId={}, quantity={}", productId, quantity);
        return true;
    }
    
    /**
     * 恢复库存（取消订单/退款）
     * 
     * @param productId 商品ID
     * @param quantity 恢复数量
     * @return 是否成功
     */
    @Transactional
    public boolean restoreStock(Long productId, Integer quantity) {
        log.info("恢复库存: productId={}, quantity={}", productId, quantity);
        
        int affectedRows = productRepository.restoreStock(productId, quantity);
        
        if (affectedRows == 0) {
            log.error("库存恢复失败: productId={}, quantity={}", productId, quantity);
            throw new BusinessException("商品不存在");
        }
        
        log.info("库存恢复成功: productId={}, quantity={}", productId, quantity);
        return true;
    }
    
    /**
     * 检查商品是否可售
     * 
     * @param productId 商品ID
     * @return 是否可售
     */
    public boolean checkProductAvailable(Long productId) {
        return productRepository.findById(productId)
                .map(product -> product.getStatus() == 1 && product.getStock() > 0)
                .orElse(false);
    }
    
    /**
     * 获取低库存商品列表
     * 
     * @param threshold 库存阈值（默认10）
     * @return 低库存商品列表
     */
    public List<Product> getLowStockProducts(Integer threshold) {
        if (threshold == null) {
            threshold = 10;
        }
        return productRepository.findByStockLessThanEqualAndStatus(threshold, 1);
    }
}

