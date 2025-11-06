import request from '@/utils/request'

/**
 * 支付相关API
 * 对应后端 PaymentService (端口8066)
 */

/**
 * 查询所有支付记录（管理端）✅ 已集成真实API
 * @param {Object} params - 查询参数
 * @param {Number} params.page - 页码（从0开始，默认0）
 * @param {Number} params.size - 每页数量（默认20）
 * @param {Number} params.payType - 支付方式（可选：1-微信；2-支付宝；3-余额）
 * @param {Number} params.payStatus - 支付状态（可选：0-失败；1-成功）
 * @param {String} params.recordType - 记录类型（可选：recharge/payment/refund）
 * @param {String} params.keyword - 搜索关键词（可选：订单号/交易号）
 * @param {String} params.startDate - 开始日期（可选：格式yyyy-MM-dd）
 * @param {String} params.endDate - 结束日期（可选：格式yyyy-MM-dd）
 * @returns {Promise} 返回Spring Data JPA分页数据结构
 */
export const getPaymentRecords = (params) => {
  return request({
    url: '/api/payment/admin/records',
    method: 'GET',
    params
  })
}

/**
 * 查询支付记录详情
 * @param {Number} payId - 支付记录ID
 * @returns {Promise}
 */
export const getPaymentDetail = (payId) => {
  return request({
    url: `/api/payment/record/${payId}`,
    method: 'GET'
  })
}

/**
 * 查询订单支付记录
 * @param {Number} orderId - 订单ID
 * @returns {Promise}
 */
export const getPaymentByOrderId = (orderId) => {
  return request({
    url: `/api/payment/order/${orderId}`,
    method: 'GET'
  })
}

/**
 * 查询指定用户支付记录（管理端）✅ 已集成真实API
 * @param {Number} userId - 用户ID
 * @returns {Promise} 返回用户的所有支付记录列表（按时间倒序）
 */
export const getUserPaymentRecords = (userId) => {
  return request({
    url: `/api/payment/admin/user/${userId}/records`,
    method: 'GET'
  })
}

/**
 * 查询充值记录
 * @returns {Promise}
 */
export const getRechargeRecords = () => {
  return request({
    url: '/api/payment/recharge/records',
    method: 'GET'
  })
}

/**
 * 申请退款（管理端）
 * @param {Object} data - 退款数据
 * @param {Number} data.orderId - 订单ID
 * @param {String} data.reason - 退款原因
 * @returns {Promise}
 */
export const refundPayment = (data) => {
  return request({
    url: '/api/payment/feign/refund',
    method: 'POST',
    data
  })
}

/**
 * 支付统计（管理端）✅ 已集成真实API
 * @param {Object} params - 查询参数（可选）
 * @param {String} params.startDate - 开始日期（可选：格式yyyy-MM-dd）
 * @param {String} params.endDate - 结束日期（可选：格式yyyy-MM-dd）
 * @returns {Promise} 返回统计数据（总记录数、金额、成功率、支付方式占比等）
 */
export const getPaymentStatistics = (params) => {
  return request({
    url: '/api/payment/admin/statistics',
    method: 'GET',
    params
  })
}

/**
 * 支付类型枚举
 */
export const PAY_TYPE = {
  WECHAT: 1,
  ALIPAY: 2,
  BALANCE: 3
}

/**
 * 支付状态枚举
 */
export const PAY_STATUS = {
  FAILED: 0,
  SUCCESS: 1
}

/**
 * 获取支付类型文本
 * @param {Number} type - 支付类型
 * @returns {String}
 */
export const getPayTypeText = (type) => {
  const map = {
    1: '微信支付',
    2: '支付宝支付',
    3: '余额支付'
  }
  return map[type] || '未知'
}

/**
 * 获取支付状态文本
 * @param {Number} status - 支付状态
 * @returns {String}
 */
export const getPayStatusText = (status) => {
  const map = {
    0: '失败',
    1: '成功'
  }
  return map[status] || '未知'
}

/**
 * 获取记录类型（充值/支付/退款）
 * @param {Object} record - 支付记录
 * @returns {String}
 */
export const getRecordType = (record) => {
  if (record.orderId === null && record.amount > 0) {
    return 'recharge' // 充值
  } else if (record.amount < 0) {
    return 'refund' // 退款
  } else {
    return 'payment' // 支付
  }
}

/**
 * 获取记录类型文本
 * @param {Object} record - 支付记录
 * @returns {String}
 */
export const getRecordTypeText = (record) => {
  const type = getRecordType(record)
  const map = {
    'recharge': '充值',
    'payment': '支付',
    'refund': '退款'
  }
  return map[type] || '未知'
}

