import request from '../utils/request'

/**
 * 支付服务 API
 * 版本: v1.0.0
 * 对应后端: PaymentService
 */

// ==================== 支付接口 ====================

/**
 * 创建支付
 * @param {object} data - 支付数据
 * @param {number} data.orderId - 订单ID
 * @param {number} data.payType - 支付方式（3-余额支付）
 * @param {number} data.amount - 支付金额
 */
export const createPayment = (data) => {
  return request({
    url: '/api/payment/create',
    method: 'POST',
    data
  })
}

/**
 * 查询支付记录
 * @param {number} payId - 支付ID
 */
export const getPaymentDetail = (payId) => {
  return request({
    url: `/api/payment/${payId}`,
    method: 'GET'
  })
}

/**
 * 根据订单ID查询支付记录
 * @param {number} orderId - 订单ID
 */
export const getPaymentByOrderId = (orderId) => {
  return request({
    url: `/api/payment/order/${orderId}`,
    method: 'GET'
  })
}

/**
 * 获取用户的支付记录列表（交易记录）
 * @param {object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 * @param {string} params.type - 交易类型（可选）
 * @returns {Promise} 支付记录列表
 */
export const getPaymentRecords = (params) => {
  return request({
    url: '/api/payment/records',
    method: 'GET',
    params
  })
}

/**
 * 账户充值
 * @param {object} data - 充值数据
 * @param {number} data.amount - 充值金额
 * @param {number} data.payType - 支付方式（3-余额支付，1-微信，2-支付宝）
 */
export const recharge = (data) => {
  return request({
    url: '/api/payment/recharge',
    method: 'POST',
    data
  })
}

/**
 * 退款
 * @param {object} data - 退款数据
 * @param {number} data.orderId - 订单ID
 * @param {number} data.amount - 退款金额
 * @param {string} data.reason - 退款原因
 */
export const refund = (data) => {
  return request({
    url: '/api/payment/refund',
    method: 'POST',
    data
  })
}

// ==================== 支付常量 ====================

/**
 * 支付类型
 */
export const PAY_TYPE = {
  WECHAT: 1,      // 微信支付
  ALIPAY: 2,      // 支付宝支付
  BALANCE: 3      // 余额支付
}

/**
 * 支付类型文本
 */
export const PAY_TYPE_TEXT = {
  [PAY_TYPE.WECHAT]: '微信支付',
  [PAY_TYPE.ALIPAY]: '支付宝支付',
  [PAY_TYPE.BALANCE]: '余额支付'
}

/**
 * 支付状态
 */
export const PAY_STATUS = {
  UNPAID: 0,      // 待支付
  PAID: 1,        // 已支付
  REFUNDING: 2,   // 退款中
  REFUNDED: 3,    // 已退款
  FAILED: 4       // 支付失败
}

/**
 * 支付状态文本
 */
export const PAY_STATUS_TEXT = {
  [PAY_STATUS.UNPAID]: '待支付',
  [PAY_STATUS.PAID]: '已支付',
  [PAY_STATUS.REFUNDING]: '退款中',
  [PAY_STATUS.REFUNDED]: '已退款',
  [PAY_STATUS.FAILED]: '支付失败'
}

/**
 * 支付状态标签类型
 */
export const PAY_STATUS_TAG_TYPE = {
  [PAY_STATUS.UNPAID]: 'warning',
  [PAY_STATUS.PAID]: 'success',
  [PAY_STATUS.REFUNDING]: 'info',
  [PAY_STATUS.REFUNDED]: 'info',
  [PAY_STATUS.FAILED]: 'danger'
}
