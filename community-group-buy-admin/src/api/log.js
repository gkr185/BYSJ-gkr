import request from '@/utils/request'

/**
 * 分页查询操作日志
 * @param {Object} params - 查询参数
 * @param {Number} params.page - 页码
 * @param {Number} params.size - 每页大小
 * @param {String} params.username - 操作人用户名（可选）
 * @param {String} params.module - 操作模块（可选）
 * @param {String} params.startDate - 开始时间（可选）
 * @param {String} params.endDate - 结束时间（可选）
 * @param {String} params.keyword - 关键词（可选）
 */
export function getOperationLogs(params) {
  return request({
    url: '/api/admin/logs/operations',
    method: 'get',
    params
  })
}

/**
 * 导出操作日志为Excel
 * @param {Object} params - 导出条件
 * @param {String} params.module - 操作模块（可选）
 * @param {String} params.startDate - 开始时间（可选）
 * @param {String} params.endDate - 结束时间（可选）
 */
export function exportOperationLogs(params) {
  return request({
    url: '/api/admin/logs/export',
    method: 'get',
    params,
    responseType: 'blob' // 重要：接收二进制文件流
  })
}

/**
 * 获取操作模块列表
 * 用于筛选下拉框
 */
export function getLogModules() {
  return request({
    url: '/api/admin/logs/modules',
    method: 'get'
  })
}

