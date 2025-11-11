import request from '@/utils/request'

/**
 * 购物车相关API
 * 对应后端 OrderService (端口8065)
 * API文档: docs/社区团购系统/二级文档（参考）/API_OrderService.md
 */

/**
 * 查询购物车列表
 * @param {Number} userId - 用户ID
 * @returns {Promise}
 */
export const getCartList = (userId) => {
  return request({
    url: `/api/cart/${userId}`,
    method: 'GET'
  })
}

/**
 * 添加商品到购物车
 * @param {Object} data - 购物车数据
 * @param {Number} data.userId - 用户ID
 * @param {Number} data.productId - 商品ID
 * @param {Number} data.quantity - 数量
 * @param {Number} data.activityId - 拼团活动ID（可选）
 * @returns {Promise}
 */
export const addToCart = (data) => {
  return request({
    url: `/api/cart/${data.userId}`,
    method: 'POST',
    data
  })
}

/**
 * 更新购物车商品数量
 * @param {Number} userId - 用户ID
 * @param {Number} cartId - 购物车ID
 * @param {Number} quantity - 新数量
 * @returns {Promise}
 */
export const updateCartQuantity = (userId, cartId, quantity) => {
  return request({
    url: `/api/cart/${userId}/${cartId}`,
    method: 'PUT',
    params: { quantity }
  })
}

/**
 * 删除购物车商品
 * @param {Number} userId - 用户ID
 * @param {Number} cartId - 购物车ID
 * @returns {Promise}
 */
export const removeFromCart = (userId, cartId) => {
  return request({
    url: `/api/cart/${userId}/${cartId}`,
    method: 'DELETE'
  })
}

/**
 * 清空购物车
 * @param {Number} userId - 用户ID
 * @returns {Promise}
 */
export const clearCart = (userId) => {
  return request({
    url: `/api/cart/${userId}/clear`,
    method: 'DELETE'
  })
}

/**
 * 批量删除购物车商品
 * @param {Number} userId - 用户ID
 * @param {Array<Number>} cartIds - 购物车ID数组
 * @returns {Promise}
 */
export const batchRemoveCart = (userId, cartIds) => {
  return request({
    url: `/api/cart/${userId}/batch`,
    method: 'DELETE',
    data: cartIds
  })
}

/**
 * 统计购物车数量
 * @param {Number} userId - 用户ID
 * @returns {Promise<Number>} 商品总数量
 */
export const getCartCount = (userId) => {
  return request({
    url: `/api/cart/${userId}/count`,
    method: 'GET'
  })
}

/**
 * 购物车结算（批量创建订单）
 * @param {Object} data - 结算数据
 * @param {Array<Number>} data.cartIds - 购物车ID数组
 * @param {Number} data.addressId - 收货地址ID
 * @param {Number} data.leaderId - 团长ID
 * @returns {Promise<Array<Number>>} 创建的订单ID数组
 */
export const checkoutCart = (data) => {
  return request({
    url: '/api/cart/checkout',
    method: 'POST',
    data
  })
}

