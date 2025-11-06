/**
 * 图片工具函数
 */

// 图片服务器基础URL（ProductService端口8086）
const IMAGE_BASE_URL = import.meta.env.DEV 
  ? 'http://localhost:8086'  // 开发环境直连ProductService
  : 'http://localhost:9000'  // 生产环境通过网关

/**
 * 获取完整的图片URL
 * @param {string} imagePath - 图片路径（可能是相对路径或完整URL）
 * @returns {string} 完整的图片URL
 */
export const getImageUrl = (imagePath) => {
  // 如果没有图片路径，返回占位图
  if (!imagePath) {
    return '/placeholder-product.png'
  }

  // 如果已经是完整的URL（http://或https://开头），直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }

  // 如果是相对路径，拼接基础URL
  // 确保路径以/开头
  const path = imagePath.startsWith('/') ? imagePath : `/${imagePath}`
  return `${IMAGE_BASE_URL}${path}`
}

/**
 * 获取商品图片URL
 * @param {object} product - 商品对象
 * @returns {string} 完整的图片URL
 */
export const getProductImageUrl = (product) => {
  if (!product) {
    return '/placeholder-product.png'
  }

  // 兼容不同的字段名
  const imagePath = product.coverImg || product.image || product.imageUrl
  return getImageUrl(imagePath)
}

/**
 * 获取用户头像URL
 * @param {object} user - 用户对象
 * @returns {string} 完整的头像URL
 */
export const getUserAvatarUrl = (user) => {
  if (!user || !user.avatar) {
    return '/placeholder-avatar.png'
  }
  return getImageUrl(user.avatar)
}

