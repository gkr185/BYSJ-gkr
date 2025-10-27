/**
 * 支付相关Mock数据
 */

import { mockOrders } from './orders'
import { mockTeams, mockMembers } from './groupbuy'

/**
 * Mock支付订单
 * @param {Object} data - 支付数据
 * @returns {Promise<Object>}
 */
export function mockPayOrder(data) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const { order_id, pay_type, pay_amount } = data
      
      // 查找订单
      const order = mockOrders.find(o => o.order_id === parseInt(order_id))
      
      if (!order) {
        reject(new Error('订单不存在'))
        return
      }
      
      // 检查订单状态
      if (order.order_status !== 0) {
        reject(new Error('订单已支付或已取消'))
        return
      }
      
      // 检查支付金额
      if (parseFloat(pay_amount) !== parseFloat(order.pay_amount)) {
        reject(new Error('支付金额不匹配'))
        return
      }
      
      // 更新订单状态
      order.pay_status = 1
      order.pay_time = new Date().toISOString()
      
      // 如果是拼团订单，更新拼团相关状态
      let teamId = null
      const groupBuyItem = order.items.find(item => item.team_id)
      
      if (groupBuyItem) {
        teamId = groupBuyItem.team_id
        
        // 更新参团记录状态
        const memberRecord = mockMembers.find(m => 
          m.team_id === teamId && 
          m.order_id === order.order_id
        )
        
        if (memberRecord) {
          memberRecord.status = 1 // 已支付
        }
        
        // 更新团人数
        const team = mockTeams.find(t => t.team_id === teamId)
        if (team) {
          team.current_num += 1
          
          // 检查是否成团
          if (team.current_num >= team.required_num) {
            team.team_status = 1 // 已成团
            team.success_time = new Date().toISOString()
            
            // 更新所有成员状态为"已成团"
            mockMembers.forEach(m => {
              if (m.team_id === teamId && m.status === 1) {
                m.status = 2 // 已成团
              }
            })
            
            // 更新所有订单状态为"待发货"
            mockOrders.forEach(o => {
              const item = o.items.find(i => i.team_id === teamId)
              if (item && o.pay_status === 1) {
                o.order_status = 1 // 待发货
              }
            })
          }
        }
      } else {
        // 普通订单直接更新为待发货
        order.order_status = 1
      }
      
      // 生成支付记录
      const paymentRecord = {
        pay_id: Date.now(),
        user_id: order.user_id,
        order_id: order.order_id,
        pay_type: pay_type,
        amount: pay_amount,
        pay_status: 1,
        transaction_id: `T${Date.now()}${Math.floor(Math.random() * 1000)}`,
        create_time: new Date().toISOString()
      }
      
      console.log('支付成功:', {
        order: order,
        payment: paymentRecord,
        team_id: teamId
      })
      
      resolve({
        success: true,
        message: '支付成功',
        data: {
          order_id: order.order_id,
          pay_id: paymentRecord.pay_id,
          team_id: teamId
        },
        team_id: teamId // 用于跳转
      })
    }, 1000) // 模拟网络延迟
  })
}

/**
 * 获取支付方式名称
 * @param {Number} payType - 支付类型
 * @returns {String}
 */
export function getPaymentTypeName(payType) {
  const typeMap = {
    1: '微信支付',
    2: '支付宝支付',
    3: '余额支付'
  }
  return typeMap[payType] || '未知'
}

