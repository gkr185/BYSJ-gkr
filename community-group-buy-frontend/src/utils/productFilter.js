// 商品筛选和排序工具函数

/**
 * 筛选商品
 * @param {Array} products - 商品列表
 * @param {Object} filters - 筛选条件
 * @param {Number} filters.category_id - 分类ID
 * @param {String} filters.keyword - 搜索关键词
 * @param {Number} filters.minPrice - 最低价格
 * @param {Number} filters.maxPrice - 最高价格
 * @param {String} filters.sortBy - 排序方式: default, sales, priceAsc, priceDesc
 * @returns {Array} 筛选后的商品列表
 */
export function filterProducts(products, filters = {}) {
  let result = [...products]
  
  // 只显示上架商品
  result = result.filter(p => p.status === 1)
  
  // 分类筛选
  if (filters.category_id) {
    result = result.filter(p => p.category_id === filters.category_id)
  }
  
  // 关键词搜索
  if (filters.keyword) {
    const keyword = filters.keyword.toLowerCase()
    result = result.filter(p => 
      p.product_name.toLowerCase().includes(keyword) ||
      (p.detail && p.detail.toLowerCase().includes(keyword))
    )
  }
  
  // 价格区间筛选
  if (filters.minPrice !== undefined || filters.maxPrice !== undefined) {
    result = result.filter(p => {
      const price = p.group_price || p.price
      const min = filters.minPrice !== undefined ? filters.minPrice : 0
      const max = filters.maxPrice !== undefined ? filters.maxPrice : Infinity
      return price >= min && price <= max
    })
  }
  
  // 排序
  if (filters.sortBy) {
    switch (filters.sortBy) {
      case 'sales':
        // 按销量降序
        result.sort((a, b) => (b.sales || 0) - (a.sales || 0))
        break
      case 'priceAsc':
        // 按价格升序
        result.sort((a, b) => {
          const priceA = a.group_price || a.price
          const priceB = b.group_price || b.price
          return priceA - priceB
        })
        break
      case 'priceDesc':
        // 按价格降序
        result.sort((a, b) => {
          const priceA = a.group_price || a.price
          const priceB = b.group_price || b.price
          return priceB - priceA
        })
        break
      case 'default':
      default:
        // 默认排序（按创建时间）
        result.sort((a, b) => {
          return new Date(b.create_time) - new Date(a.create_time)
        })
        break
    }
  }
  
  return result
}

/**
 * 分页
 * @param {Array} items - 数据列表
 * @param {Number} page - 当前页码（从1开始）
 * @param {Number} pageSize - 每页数量
 * @returns {Object} { items, total, totalPages }
 */
export function paginate(items, page = 1, pageSize = 12) {
  const total = items.length
  const totalPages = Math.ceil(total / pageSize)
  const start = (page - 1) * pageSize
  const end = start + pageSize
  const paginatedItems = items.slice(start, end)
  
  return {
    items: paginatedItems,
    total,
    totalPages,
    currentPage: page,
    pageSize
  }
}

