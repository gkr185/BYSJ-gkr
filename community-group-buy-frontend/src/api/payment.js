import request from '@/utils/request'

/**
 * 支付相关API
 * 对应后端 PaymentService (端口8066)
 */

/**
 * 创建支付
 * @param {Object} data - 支付请求参数
 * @param {Number} data.orderId - 订单ID
 * @param {Number} data.payType - 支付方式（1-微信；2-支付宝；3-余额）
 * @param {Number} data.amount - 支付金额
 * @returns {Promise}
 */
export const createPayment = (data) => {
  return request({
    url: '/api/payment/create',
    method: 'POST',
    data
  })
}

/**
 * 余额充值
 * @param {Object} data - 充值请求参数
 * @param {Number} data.amount - 充值金额
 * @param {Number} data.payType - 支付方式（3-余额）
 * @returns {Promise}
 */
export const recharge = (data) => {
  return request({
    url: '/api/payment/recharge',
    method: 'POST',
    data
  })
}

/**
 * 查询支付记录
 * @param {Number} payId - 支付记录ID
 * @returns {Promise}
 */
export const getPaymentRecord = (payId) => {
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
 * 查询所有支付记录
 * @returns {Promise}
 */
export const getPaymentRecords = () => {
  return request({
    url: '/api/payment/records',
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

