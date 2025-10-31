package com.bcu.edu.service;

import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.dto.ProductQueryRequest;
import com.bcu.edu.dto.ProductRequest;
import com.bcu.edu.entity.Product;
import com.bcu.edu.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品管理服务
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductManagementService {
    
    private final ProductRepository productRepository;
    
    /**
     * 获取商品列表（分页、筛选、排序）
     */
    public Page<Product> getProductList(ProductQueryRequest request) {
        Pageable pageable = createPageable(request);
        
        if (request.getKeyword() != null || request.getCategoryId() != null) {
            // 搜索查询
            return productRepository.searchProducts(
                request.getKeyword(), 
                request.getCategoryId(), 
                pageable
            );
        } else {
            // 普通查询（仅上架商品）
            return productRepository.findByStatusOrderByCreateTimeDesc(1, pageable);
        }
    }
    
    /**
     * 管理端获取商品列表（包含下架商品）
     */
    public Page<Product> getAdminProductList(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    /**
     * 获取商品详情
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在: " + id));
    }
    
    /**
     * 商品搜索
     */
    public Page<Product> searchProducts(String keyword, Long categoryId, Pageable pageable) {
        return productRepository.searchProducts(keyword, categoryId, pageable);
    }
    
    /**
     * 热门商品推荐
     */
    public List<Product> getHotProducts(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createTime"));
        return productRepository.findByStatusOrderByCreateTimeDesc(1, pageable).getContent();
    }
    
    /**
     * 推荐商品（按分类）
     */
    public List<Product> getRecommendProducts(Long categoryId, Integer limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createTime"));
        if (categoryId != null) {
            return productRepository.findByCategoryIdAndStatusOrderByCreateTimeDesc(categoryId, 1, pageable).getContent();
        } else {
            return getHotProducts(limit);
        }
    }
    
    /**
     * 查询商品库存
     */
    public Integer getProductStock(Long id) {
        Product product = getProductById(id);
        return product.getStock();
    }
    
    /**
     * 按分类查询商品
     */
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndStatusOrderByCreateTimeDesc(categoryId, 1, pageable);
    }
    
    /**
     * 创建商品
     */
    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        product.setStatus(1); // 默认上架
        return productRepository.save(product);
    }
    
    /**
     * 更新商品
     */
    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);
        BeanUtils.copyProperties(request, product);
        product.setProductId(id); // 保持ID不变
        return productRepository.save(product);
    }
    
    /**
     * 删除商品
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.deleteById(id);
    }
    
    /**
     * 更新商品状态（上下架）
     */
    @Transactional
    public Product updateProductStatus(Long id, Integer status) {
        Product product = getProductById(id);
        product.setStatus(status);
        return productRepository.save(product);
    }
    
    /**
     * 调整库存
     */
    @Transactional
    public Product adjustStock(Long id, Integer quantity) {
        Product product = getProductById(id);
        int newStock = product.getStock() + quantity;
        if (newStock < 0) {
            throw new BusinessException("库存不足，当前库存: " + product.getStock());
        }
        product.setStock(newStock);
        return productRepository.save(product);
    }
    
    /**
     * 创建分页参数
     */
    private Pageable createPageable(ProductQueryRequest request) {
        Sort sort;
        switch (request.getSort()) {
            case "price_asc":
                sort = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "price_desc":
                sort = Sort.by(Sort.Direction.DESC, "price");
                break;
            default:
                sort = Sort.by(Sort.Direction.DESC, "createTime");
        }
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }
}

