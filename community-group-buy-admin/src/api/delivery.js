import request from '../utils/request'

/**
 * 获取未配送订单列表（用于配送订单页面）
 */
export const getPendingDeliveryOrders = (params) => {
  return request({
    url: '/api/order/admin/status/2', // 待发货状态
    method: 'GET',
    params
  })
}

/**
 * 获取配送单列表（管理员）
 */
export const getDeliveryList = (params) => {
  return request({
    url: '/api/delivery/admin/list',
    method: 'GET',
    params
  })
}

/**
 * 批量发货
 */
export const batchShipOrders = (data) => {
  return request({
    url: '/api/delivery/batch/ship',
    method: 'POST',
    data
  })
}

/**
 * 创建配送单
 */
export const createDelivery = (data) => {
  return request({
    url: '/api/delivery',
    method: 'POST',
    data
  })
}

/**
 * 获取配送单详情
 */
export const getDeliveryDetail = (deliveryId) => {
  return request({
    url: `/api/delivery/${deliveryId}`,
    method: 'GET'
  })
}

/**
 * 根据分单组查询配送单
 */
export const getDeliveryByDispatchGroup = (dispatchGroup) => {
  return request({
    url: `/api/delivery/dispatch-group/${dispatchGroup}`,
    method: 'GET'
  })
}

/**
 * 查询团长配送单列表
 */
export const getLeaderDeliveries = (leaderId) => {
  return request({
    url: `/api/delivery/leader/${leaderId}`,
    method: 'GET'
  })
}

/**
 * 查询团长指定状态的配送单
 */
export const getLeaderDeliveriesByStatus = (leaderId, status) => {
  return request({
    url: `/api/delivery/leader/${leaderId}/status/${status}`,
    method: 'GET'
  })
}

/**
 * 更新配送状态
 */
export const updateDeliveryStatus = (deliveryId, status) => {
  return request({
    url: `/api/delivery/${deliveryId}/status`,
    method: 'PUT',
    params: { status }
  })
}

/**
 * 开始配送
 */
export const startDelivery = (deliveryId) => {
  return request({
    url: `/api/delivery/${deliveryId}/start`,
    method: 'PUT'
  })
}

/**
 * 完成配送
 */
export const completeDelivery = (deliveryId) => {
  return request({
    url: `/api/delivery/${deliveryId}/complete`,
    method: 'PUT'
  })
}

/**
 * 删除配送单
 */
export const deleteDelivery = (deliveryId) => {
  return request({
    url: `/api/delivery/${deliveryId}`,
    method: 'DELETE'
  })
}

/**
 * 获取配送统计信息
 */
export const getDeliveryStatistics = (params) => {
  return request({
    url: '/api/delivery/statistics',
    method: 'GET',
    params
  })
}

/**
 * 规划配送路径
 */
export const planDeliveryRoute = (data) => {
  return request({
    url: '/api/delivery/route/plan',
    method: 'POST',
    data
  })
}

/**
 * 获取算法引擎状态
 */
export const getRouteStatus = () => {
  return request({
    url: '/api/delivery/route/status',
    method: 'GET'
  })
}

/**
 * 测试算法引擎
 */
export const testAlgorithm = (algorithm) => {
  return request({
    url: `/api/delivery/route/test/${algorithm}`,
    method: 'POST'
  })
}

/**
 * 获取默认仓库
 */
export const getDefaultWarehouse = () => {
  return request({
    url: '/api/delivery/warehouse/default',
    method: 'GET'
  })
}

/**
 * 获取启用的仓库列表
 */
export const getEnabledWarehouses = () => {
  return request({
    url: '/api/delivery/warehouse/enabled',
    method: 'GET'
  })
}

/**
 * 获取仓库详情
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
    url: `/api/delivery/warehouse/${id}/default`,
    method: 'PUT'
  })
}

/**
 * 切换仓库状态
 */
export const toggleWarehouseStatus = (id) => {
  return request({
    url: `/api/delivery/warehouse/${id}/toggle-status`,
    method: 'PUT'
  })
}

/**
 * 获取仓库统计
 */
export const getWarehouseStatistics = () => {
  return request({
    url: '/api/delivery/warehouse/statistics',
    method: 'GET'
  })
}

/**
 * 健康检查
 */
export const getDeliveryHealth = () => {
  return request({
    url: '/api/delivery/monitor/health',
    method: 'GET'
  })
}

/**
 * 服务状态检查
 */
export const getDeliveryStatus = () => {
  return request({
    url: '/api/delivery/monitor/status',
    method: 'GET'
  })
}

/**
 * 版本信息
 */
export const getDeliveryVersion = () => {
  return request({
    url: '/api/delivery/monitor/version',
    method: 'GET'
  })
}
