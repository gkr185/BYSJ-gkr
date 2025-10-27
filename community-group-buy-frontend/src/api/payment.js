/**
 * 支付相关API
 */

import { mockPayOrder } from '@/mock/payment'

/**
 * 支付订单
 * @param {Object} data - 支付数据
 * @param {Number} data.order_id - 订单ID
 * @param {Number} data.pay_type - 支付类型（1-微信/2-支付宝/3-余额）
 * @param {Number} data.pay_amount - 支付金额
 * @returns {Promise<Object>}
 */
export function apiPayOrder(data) {
  // Mock版本：直接调用Mock函数
  return mockPayOrder(data)
  
  // 真实API版本：
  // return request({
  //   url: '/api/payment/pay',
  //   method: 'post',
  //   data
  // })
}

/**
 * 获取支付状态
 * @param {Number} orderId - 订单ID
 * @returns {Promise<Object>}
 */
export function apiGetPaymentStatus(orderId) {
  // Mock版本
  return new Promise(resolve => {
    setTimeout(() => {
      resolve({
        pay_status: 1,
        pay_time: new Date().toISOString()
      })
    }, 300)
  })
  
  // 真实API版本：
  // return request({
  //   url: `/api/payment/status/${orderId}`,
  //   method: 'get'
  // })
}

/**
 * 获取用户余额
 * @returns {Promise<Number>}
 */
export function apiGetUserBalance() {
  // Mock版本
  return new Promise(resolve => {
    setTimeout(() => {
      resolve(158.00)
    }, 300)
  })
  
  // 真实API版本：
  // return request({
  //   url: '/api/user/account/balance',
  //   method: 'get'
  // })
}

