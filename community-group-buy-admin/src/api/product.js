import request from '../utils/request'

/**
 * ====== 分类管理（管理端） ======
 */

/**
 * 获取分类列表
 */
export const getCategoryList = () => {
  return request({
    url: '/api/product/category/list',
    method: 'GET'
  })
}

/**
 * 获取分类树
 */
export const getCategoryTree = () => {
  return request({
    url: '/api/product/category/tree',
    method: 'GET'
  })
}

/**
 * 获取分类详情
 */
export const getCategoryDetail = (id) => {
  return request({
    url: `/api/product/category/${id}`,
    method: 'GET'
  })
}

/**
 * 创建分类
 */
export const createCategory = (data) => {
  return request({
    url: '/api/product/admin/category',
    method: 'POST',
    data
  })
}

/**
 * 更新分类
 */
export const updateCategory = (id, data) => {
  return request({
    url: `/api/product/admin/category/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除分类
 */
export const deleteCategory = (id) => {
  return request({
    url: `/api/product/admin/category/${id}`,
    method: 'DELETE'
  })
}

/**
 * 调整分类排序
 */
export const updateCategorySort = (id, sort) => {
  return request({
    url: `/api/product/admin/category/${id}/sort`,
    method: 'PUT',
    params: { sort }
  })
}

/**
 * ====== 商品管理（管理端） ======
 */

/**
 * 获取商品列表（管理端）
 */
export const getAdminProductList = (params) => {
  return request({
    url: '/api/product/admin/product/list',
    method: 'GET',
    params
  })
}

/**
 * 获取商品详情
 */
export const getProductDetail = (id) => {
  return request({
    url: `/api/product/${id}`,
    method: 'GET'
  })
}

/**
 * 创建商品
 */
export const createProduct = (data) => {
  return request({
    url: '/api/product/admin/product',
    method: 'POST',
    data
  })
}

/**
 * 更新商品
 */
export const updateProduct = (id, data) => {
  return request({
    url: `/api/product/admin/product/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除商品
 */
export const deleteProduct = (id) => {
  return request({
    url: `/api/product/admin/product/${id}`,
    method: 'DELETE'
  })
}

/**
 * 更新商品状态（上下架）
 */
export const updateProductStatus = (id, status) => {
  return request({
    url: `/api/product/admin/product/${id}/status`,
    method: 'PUT',
    params: { status }
  })
}

/**
 * 调整商品库存
 */
export const adjustProductStock = (id, quantity) => {
  return request({
    url: `/api/product/admin/product/${id}/stock`,
    method: 'PUT',
    params: { quantity }
  })
}

/**
 * 上传商品图片
 */
export const uploadProductImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/product/admin/upload',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 获取商品统计数据
 */
export const getProductStatistics = () => {
  return request({
    url: '/api/product/admin/statistics',
    method: 'GET'
  })
}

