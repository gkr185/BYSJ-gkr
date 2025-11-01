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

