import request from '../utils/request'

// ==================== 商品分类（C端） ====================

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
 * @param {number} categoryId - 分类ID
 */
export const getCategoryDetail = (categoryId) => {
  return request({
    url: `/api/product/category/${categoryId}`,
    method: 'GET'
  })
}

/**
 * 获取分类下的商品
 * @param {number} categoryId - 分类ID
 * @param {object} params - 查询参数 { page, size }
 */
export const getCategoryProducts = (categoryId, params) => {
  return request({
    url: `/api/product/category/${categoryId}/products`,
    method: 'GET',
    params
  })
}

// ==================== 商品（C端） ====================

/**
 * 获取商品列表
 * @param {object} params - 查询参数 { page, size, sort, keyword, categoryId }
 */
export const getProductList = (params) => {
  return request({
    url: '/api/product/list',
    method: 'GET',
    params
  })
}

/**
 * 获取商品详情
 * @param {number} productId - 商品ID
 */
export const getProductDetail = (productId) => {
  return request({
    url: `/api/product/${productId}`,
    method: 'GET'
  })
}

/**
 * 商品搜索
 * @param {object} params - 查询参数 { keyword, categoryId, page, size }
 */
export const searchProducts = (params) => {
  return request({
    url: '/api/product/search',
    method: 'GET',
    params
  })
}

/**
 * 热门商品推荐
 * @param {number} limit - 返回数量，默认10
 */
export const getHotProducts = (limit = 10) => {
  return request({
    url: '/api/product/hot',
    method: 'GET',
    params: { limit }
  })
}

/**
 * 推荐商品
 * @param {object} params - 查询参数 { categoryId, limit }
 */
export const getRecommendProducts = (params) => {
  return request({
    url: '/api/product/recommend',
    method: 'GET',
    params
  })
}

/**
 * 查询商品库存
 * @param {number} productId - 商品ID
 */
export const getProductStock = (productId) => {
  return request({
    url: `/api/product/${productId}/stock`,
    method: 'GET'
  })
}

/**
 * 按分类查询商品
 * @param {number} categoryId - 分类ID
 * @param {object} params - 查询参数 { page, size }
 */
export const getProductsByCategory = (categoryId, params) => {
  return request({
    url: `/api/product/category/${categoryId}/list`,
    method: 'GET',
    params
  })
}

// ==================== 商品管理（管理端） ====================

/**
 * 创建商品（管理员）
 * @param {object} data - 商品数据
 */
export const createProduct = (data) => {
  return request({
    url: '/api/product/admin/product',
    method: 'POST',
    data
  })
}

/**
 * 更新商品（管理员）
 * @param {number} productId - 商品ID
 * @param {object} data - 商品数据
 */
export const updateProduct = (productId, data) => {
  return request({
    url: `/api/product/admin/product/${productId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除商品（管理员）
 * @param {number} productId - 商品ID
 */
export const deleteProduct = (productId) => {
  return request({
    url: `/api/product/admin/product/${productId}`,
    method: 'DELETE'
  })
}

/**
 * 更新商品状态（管理员）
 * @param {number} productId - 商品ID
 * @param {number} status - 状态（0-下架；1-上架）
 */
export const updateProductStatus = (productId, status) => {
  return request({
    url: `/api/product/admin/product/${productId}/status`,
    method: 'PUT',
    params: { status }
  })
}

/**
 * 调整商品库存（管理员）
 * @param {number} productId - 商品ID
 * @param {number} quantity - 调整数量（正数增加，负数减少）
 */
export const adjustProductStock = (productId, quantity) => {
  return request({
    url: `/api/product/admin/product/${productId}/stock`,
    method: 'PUT',
    params: { quantity }
  })
}

/**
 * 上传商品图片（管理员）
 * @param {File} file - 图片文件
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
 * 获取商品列表（管理端）
 * @param {object} params - 查询参数 { page, size }
 */
export const getAdminProductList = (params) => {
  return request({
    url: '/api/product/admin/product/list',
    method: 'GET',
    params
  })
}

/**
 * 获取商品统计数据（管理员）
 */
export const getProductStatistics = () => {
  return request({
    url: '/api/product/admin/statistics',
    method: 'GET'
  })
}

// ==================== 分类管理（管理端） ====================

/**
 * 创建分类（管理员）
 * @param {object} data - 分类数据
 */
export const createCategory = (data) => {
  return request({
    url: '/api/product/admin/category',
    method: 'POST',
    data
  })
}

/**
 * 更新分类（管理员）
 * @param {number} categoryId - 分类ID
 * @param {object} data - 分类数据
 */
export const updateCategory = (categoryId, data) => {
  return request({
    url: `/api/product/admin/category/${categoryId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除分类（管理员）
 * @param {number} categoryId - 分类ID
 */
export const deleteCategory = (categoryId) => {
  return request({
    url: `/api/product/admin/category/${categoryId}`,
    method: 'DELETE'
  })
}

/**
 * 调整分类排序（管理员）
 * @param {number} categoryId - 分类ID
 * @param {number} sort - 排序值
 */
export const updateCategorySort = (categoryId, sort) => {
  return request({
    url: `/api/product/admin/category/${categoryId}/sort`,
    method: 'PUT',
    params: { sort }
  })
}

