import request from '@/utils/request'

/**
 * 订单相关API
 */

/**
 * 查询我的订单列表（分页）
 * @param {Number} page - 页码（从0开始）
 * @param {Number} size - 每页数量
 */
export const getMyOrders = (page = 0, size = 10) => {
  return request({
    url: '/api/order/my',
    method: 'GET',
    params: { page, size }
  })
}

/**
 * 查询订单详情
 * @param {Number} orderId - 订单ID
 */
export const getOrderDetail = (orderId) => {
  return request({
    url: `/api/order/${orderId}`,
    method: 'GET'
  })
}

/**
 * 取消订单
 * @param {Number} orderId - 订单ID
 */
export const cancelOrder = (orderId) => {
  return request({
    url: `/api/order/cancel/${orderId}`,
    method: 'POST'
  })
}

/**
 * 确认收货
 * @param {Number} orderId - 订单ID
 */
export const confirmReceipt = (orderId) => {
  return request({
    url: `/api/order/confirm/${orderId}`,
    method: 'POST'
  })
}

/**
 * 订单状态枚举
 */
export const ORDER_STATUS = {
  PENDING_PAYMENT: 0,    // 待支付
  PENDING_DELIVERY: 1,   // 待发货
  IN_DELIVERY: 2,        // 配送中
  DELIVERED: 3,          // 已送达
  CANCELLED: 4,          // 已取消
  REFUNDING: 5,          // 退款中
  REFUNDED: 6            // 已退款
}

/**
 * 订单状态文本映射
 */
export const ORDER_STATUS_TEXT = {
  [ORDER_STATUS.PENDING_PAYMENT]: '待支付',
  [ORDER_STATUS.PENDING_DELIVERY]: '待发货',
  [ORDER_STATUS.IN_DELIVERY]: '配送中',
  [ORDER_STATUS.DELIVERED]: '已送达',
  [ORDER_STATUS.CANCELLED]: '已取消',
  [ORDER_STATUS.REFUNDING]: '退款中',
  [ORDER_STATUS.REFUNDED]: '已退款'
}

/**
 * 订单状态标签类型映射（Element Plus）
 */
export const ORDER_STATUS_TAG_TYPE = {
  [ORDER_STATUS.PENDING_PAYMENT]: 'warning',
  [ORDER_STATUS.PENDING_DELIVERY]: 'primary',
  [ORDER_STATUS.IN_DELIVERY]: 'info',
  [ORDER_STATUS.DELIVERED]: 'success',
  [ORDER_STATUS.CANCELLED]: 'info',
  [ORDER_STATUS.REFUNDING]: 'warning',
  [ORDER_STATUS.REFUNDED]: 'danger'
}

/**
 * 支付状态枚举
 */
export const PAY_STATUS = {
  UNPAID: 0,  // 未支付
  PAID: 1     // 已支付
}

/**
 * 支付状态文本映射
 */
export const PAY_STATUS_TEXT = {
  [PAY_STATUS.UNPAID]: '未支付',
  [PAY_STATUS.PAID]: '已支付'
}

/**
 * 查询团长订单列表（团长端）
 * @param {Object} params - 查询参数
 * @param {Number} params.leaderId - 团长ID
 * @param {Number} params.page - 页码（从0开始）
 * @param {Number} params.size - 每页数量
 * @param {Number} params.orderStatus - 订单状态（可选）
 * @returns {Promise<Object>} { total, items }
 */
export const getLeaderOrders = (params) => {
  return request({
    url: '/api/order/leader/my',
    method: 'GET',
    params
  })
}

/**
 * 查询团长订单统计（团长端）
 * @param {Number} leaderId - 团长ID
 * @returns {Promise<Object>} { totalCount, todayCount, pendingCount, deliveringCount }
 */
export const getLeaderOrdersSummary = (leaderId) => {
  return request({
    url: '/api/order/leader/summary',
    method: 'GET',
    params: { leaderId }
  })
}

/**
 * 查询团长的所有订单（用于按活动分组）
 * @param {Object} params - 查询参数
 * @returns {Promise<Object>} 团长订单列表
 */
export const getAllLeaderOrders = (params = {}) => {
  return request({
    url: '/api/order/leader/my',
    method: 'GET',
    params: {
      ...params,
      size: 1000 // 获取足够多的数据用于前端分组
    }
  })
}

/**
 * 查询团订单列表（⭐精确按团查询）
 * @param {Number} teamId - 团ID
 * @returns {Promise<Object>} 团订单列表
 */
export const getTeamOrders = (teamId) => {
  return request({
    url: `/api/order/leader/team/${teamId}/orders`,
    method: 'GET'
  })
}

