package com.bcu.edu.service;

import com.bcu.edu.common.exception.BusinessException;
import com.bcu.edu.dto.CategoryRequest;
import com.bcu.edu.dto.CategoryTreeNode;
import com.bcu.edu.entity.Product;
import com.bcu.edu.entity.ProductCategory;
import com.bcu.edu.repository.ProductCategoryRepository;
import com.bcu.edu.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务
 * 
 * @author 耿康瑞
 * @version 1.0.0
 * @since 2025-10-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    
    /**
     * 获取所有启用的分类列表（扁平）
     */
    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findByStatusOrderBySortAsc(1);
    }
    
    /**
     * 获取分类树（树形结构）
     */
    public List<CategoryTreeNode> getCategoryTree() {
        List<ProductCategory> allCategories = categoryRepository.findByStatusOrderBySortAsc(1);
        return buildTree(allCategories, 0L);
    }
    
    /**
     * 获取分类详情
     */
    public ProductCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("分类不存在: " + id));
    }
    
    /**
     * 获取分类下的商品
     */
    public Page<Product> getProductsByCategoryId(Long categoryId, Pageable pageable) {
        // 验证分类存在
        getCategoryById(categoryId);
        return productRepository.findByCategoryIdAndStatusOrderByCreateTimeDesc(categoryId, 1, pageable);
    }
    
    /**
     * 创建分类
     */
    @Transactional
    public ProductCategory createCategory(CategoryRequest request) {
        // 检查分类名称是否已存在
        if (categoryRepository.existsByCategoryName(request.getCategoryName())) {
            throw new BusinessException("分类名称已存在: " + request.getCategoryName());
        }
        
        // 如果有父分类，验证父分类存在
        if (request.getParentId() != 0) {
            getCategoryById(request.getParentId());
        }
        
        ProductCategory category = new ProductCategory();
        BeanUtils.copyProperties(request, category);
        category.setStatus(1); // 默认启用
        
        return categoryRepository.save(category);
    }
    
    /**
     * 更新分类
     */
    @Transactional
    public ProductCategory updateCategory(Long id, CategoryRequest request) {
        ProductCategory category = getCategoryById(id);
        
        // 检查分类名称是否与其他分类重复
        if (categoryRepository.existsByCategoryNameAndCategoryIdNot(request.getCategoryName(), id)) {
            throw new BusinessException("分类名称已存在: " + request.getCategoryName());
        }
        
        // 如果有父分类，验证父分类存在
        if (request.getParentId() != 0) {
            getCategoryById(request.getParentId());
        }
        
        // 不能将分类设置为自己的子分类
        if (request.getParentId().equals(id)) {
            throw new BusinessException("不能将分类设置为自己的子分类");
        }
        
        BeanUtils.copyProperties(request, category);
        return categoryRepository.save(category);
    }
    
    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        ProductCategory category = getCategoryById(id);
        
        // 检查是否有子分类
        List<ProductCategory> children = categoryRepository.findByParentIdAndStatusOrderBySortAsc(id, 1);
        if (!children.isEmpty()) {
            throw new BusinessException("该分类下还有" + children.size() + "个子分类，无法删除");
        }
        
        // 检查是否有商品关联
        Long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new BusinessException("该分类下还有" + productCount + "个商品，无法删除");
        }
        
        categoryRepository.deleteById(id);
    }
    
    /**
     * 更新分类排序
     */
    @Transactional
    public ProductCategory updateCategorySort(Long id, Integer sort) {
        ProductCategory category = getCategoryById(id);
        category.setSort(sort);
        return categoryRepository.save(category);
    }
    
    /**
     * 递归构建分类树
     */
    private List<CategoryTreeNode> buildTree(List<ProductCategory> categories, Long parentId) {
        return categories.stream()
                .filter(c -> c.getParentId().equals(parentId))
                .map(c -> {
                    CategoryTreeNode node = new CategoryTreeNode();
                    BeanUtils.copyProperties(c, node);
                    // 递归获取子分类
                    node.setChildren(buildTree(categories, c.getCategoryId()));
                    return node;
                })
                .sorted(Comparator.comparing(CategoryTreeNode::getSort))
                .collect(Collectors.toList());
    }
}

