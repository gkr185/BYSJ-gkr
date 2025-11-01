import request from '../utils/request'

/**
 * 获取订单列表（分页）
 */
export const getOrderList = (params) => {
  return request({
    url: '/api/order/admin/list',
    method: 'GET',
    params
  })
}

/**
 * 获取订单详情
 */
export const getOrderDetail = (orderId) => {
  return request({
    url: `/api/order/${orderId}`,
    method: 'GET'
  })
}

/**
 * 更新订单状态
 */
export const updateOrderStatus = (orderId, status) => {
  return request({
    url: `/api/order/admin/status/${orderId}`,
    method: 'PUT',
    params: { status }
  })
}

/**
 * 批量更新订单状态
 */
export const batchUpdateOrderStatus = (orderIds, status) => {
  return request({
    url: '/api/order/admin/batchUpdateStatus',
    method: 'POST',
    data: orderIds,
    params: { status }
  })
}

/**
 * 取消订单
 */
export const cancelOrder = (orderId) => {
  return request({
    url: `/api/order/cancel/${orderId}`,
    method: 'POST'
  })
}

/**
 * 订单统计
 */
export const getOrderStatistics = () => {
  return request({
    url: '/api/order/admin/statistics',
    method: 'GET'
  })
}

/**
 * 按状态查询订单
 */
export const getOrdersByStatus = (status, params) => {
  return request({
    url: `/api/order/admin/status/${status}`,
    method: 'GET',
    params
  })
}

/**
 * 搜索订单
 */
export const searchOrders = (keyword, params) => {
  return request({
    url: '/api/order/admin/search',
    method: 'GET',
    params: { keyword, ...params }
  })
}

/**
 * 导出订单
 */
export const exportOrders = (params) => {
  return request({
    url: '/api/order/admin/export',
    method: 'GET',
    params,
    responseType: 'blob'
  })
}

/**
 * 获取用户的订单列表
 */
export const getUserOrders = (userId, params) => {
  return request({
    url: `/api/order/admin/user/${userId}`,
    method: 'GET',
    params
  })
}

/**
 * 获取团长的订单列表
 */
export const getLeaderOrders = (leaderId, params) => {
  return request({
    url: `/api/order/admin/leader/${leaderId}`,
    method: 'GET',
    params
  })
}

