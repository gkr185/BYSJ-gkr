import request from '../utils/request'

// ==================== 批量发货接口 ====================

/**
 * 批量发货（核心接口）
 */
export const batchShipOrders = (data) => {
  return request({
    url: '/api/delivery/batch/ship',
    method: 'POST',
    data
  })
}

// ==================== 配送单管理接口 ====================

/**
 * 查询配送单列表（分页）
 */
export const getDeliveryList = (params) => {
  return request({
    url: '/api/delivery/list',
    method: 'GET',
    params
  })
}

/**
 * 查询配送单详情
 */
export const getDeliveryDetail = (deliveryId) => {
  return request({
    url: `/api/delivery/${deliveryId}`,
    method: 'GET'
  })
}

/**
 * 重新规划路径
 */
export const replanRoute = (deliveryId) => {
  return request({
    url: `/api/delivery/${deliveryId}/replan`,
    method: 'POST'
  })
}

/**
 * 完成配送
 */
export const completeDelivery = (deliveryId) => {
  return request({
    url: `/api/delivery/${deliveryId}/complete`,
    method: 'POST'
  })
}

/**
 * 取消配送
 */
export const cancelDelivery = (deliveryId) => {
  return request({
    url: `/api/delivery/${deliveryId}/cancel`,
    method: 'POST'
  })
}

// ==================== 仓库管理接口 ====================

/**
 * 查询仓库列表
 */
export const getWarehouseList = () => {
  return request({
    url: '/api/delivery/warehouse/list',
    method: 'GET'
  })
}

/**
 * 查询启用的仓库列表
 */
export const getActiveWarehouses = () => {
  return request({
    url: '/api/delivery/warehouse/active',
    method: 'GET'
  })
}

/**
 * 查询默认仓库
 */
export const getDefaultWarehouse = () => {
  return request({
    url: '/api/delivery/warehouse/default',
    method: 'GET'
  })
}

/**
 * 查询仓库详情
 */
export const getWarehouseDetail = (id) => {
  return request({
    url: `/api/delivery/warehouse/${id}`,
    method: 'GET'
  })
}

/**
 * 创建仓库
 */
export const createWarehouse = (data) => {
  return request({
    url: '/api/delivery/warehouse',
    method: 'POST',
    data
  })
}

/**
 * 更新仓库
 */
export const updateWarehouse = (id, data) => {
  return request({
    url: `/api/delivery/warehouse/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除仓库
 */
export const deleteWarehouse = (id) => {
  return request({
    url: `/api/delivery/warehouse/${id}`,
    method: 'DELETE'
  })
}

/**
 * 设置默认仓库
 */
export const setDefaultWarehouse = (id) => {
  return request({
    url: `/api/delivery/warehouse/${id}/setDefault`,
    method: 'PUT'
  })
}

// ==================== 配送统计接口 ====================

/**
 * 配送总览统计
 */
export const getStatisticsOverview = () => {
  return request({
    url: '/api/delivery/statistics/overview',
    method: 'GET'
  })
}

/**
 * 距离统计
 */
export const getDistanceStatistics = (params) => {
  return request({
    url: '/api/delivery/statistics/distance',
    method: 'GET',
    params
  })
}

/**
 * 效率统计
 */
export const getEfficiencyStatistics = (params) => {
  return request({
    url: '/api/delivery/statistics/efficiency',
    method: 'GET',
    params
  })
}

/**
 * 团长配送统计
 */
export const getLeaderStatistics = () => {
  return request({
    url: '/api/delivery/statistics/leader',
    method: 'GET'
  })
}

// ==================== 订单管理接口（配送用）====================

/**
 * 查询待发货订单列表（管理端）
 */
export const getPendingDeliveryOrders = (params) => {
  return request({
    url: '/api/order/admin/list',
    method: 'GET',
    params: {
      ...params,
      status: 1 // 待发货
    }
  })
}
