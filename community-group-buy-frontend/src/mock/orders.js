// 订单Mock数据 - 严格遵守数据库设计文档字段标准
// 数据库表: order_main, order_item

// 订单状态枚举 (order_main.order_status)
export const ORDER_STATUS = {
  UNPAID: 0,        // 待支付
  TO_DELIVER: 1,    // 待发货
  DELIVERING: 2,    // 配送中
  DELIVERED: 3,     // 已送达
  CANCELLED: 4,     // 已取消
  REFUNDING: 5,     // 退款中
  REFUNDED: 6       // 已退款
}

// 支付状态枚举 (order_main.pay_status)
export const PAY_STATUS = {
  UNPAID: 0,   // 未支付
  PAID: 1      // 已支付
}

// 支付类型枚举 (payment_record.pay_type)
export const PAY_TYPE = {
  WECHAT: 1,   // 微信支付
  ALIPAY: 2,   // 支付宝支付
  BALANCE: 3   // 余额支付
}

// 模拟订单列表数据
export const mockOrders = [
  {
    // order_main 表字段
    order_id: 1001,
    user_id: 1,
    leader_id: 1001, // 取货团长ID
    order_sn: '2025102114253612345', // 订单编号：日期+随机数
    total_amount: 45.80, // 商品总金额
    discount_amount: 0.00, // 优惠金额
    pay_amount: 45.80, // 实付金额 (total_amount - discount_amount)
    order_status: 1, // 待发货
    pay_status: 1, // 已支付
    pay_time: '2025-10-21 14:30:00',
    receive_address_id: 1,
    dispatch_group: 'leader_1001_20251021_01', // 分单组标识
    delivery_id: null,
    create_time: '2025-10-21 14:25:36',
    update_time: '2025-10-21 14:30:00',
    
    // 关联收货地址信息
    address: {
      receiver: '张三',
      phone: '138****5678',
      province: '北京市',
      city: '北京市',
      district: '朝阳区',
      detail: '朝阳路123号'
    },
    
    // 关联订单项 (order_item)
    items: [
      {
        item_id: 1,
        order_id: 1001,
        product_id: 1,
        activity_id: null, // 非拼团商品为null
        team_id: null, // ⭐新增：拼团团ID
        product_name: '新鲜草莓', // 快照
        product_img: 'https://via.placeholder.com/100/FF6B6B/FFFFFF?text=草莓',
        price: 25.00, // 购买单价
        quantity: 1,
        total_price: 25.00 // price × quantity
      },
      {
        item_id: 2,
        order_id: 1001,
        product_id: 2,
        activity_id: 1, // 拼团商品
        team_id: 5001, // ⭐新增：拼团团ID
        team_no: 'T20251027001', // ⭐团号（用于显示）
        product_name: '苹果（拼团）',
        product_img: 'https://via.placeholder.com/100/FFB6C1/FFFFFF?text=苹果',
        price: 19.90, // 拼团价
        quantity: 1,
        total_price: 19.90
      }
    ]
  },
  {
    order_id: 1002,
    user_id: 1,
    leader_id: 1002,
    order_sn: '2025102015103498765',
    total_amount: 38.90,
    discount_amount: 5.00,
    pay_amount: 33.90,
    order_status: 3, // 已送达
    pay_status: 1,
    pay_time: '2025-10-20 15:12:00',
    receive_address_id: 1,
    dispatch_group: 'leader_1002_20251020_01',
    delivery_id: 1,
    create_time: '2025-10-20 15:10:34',
    update_time: '2025-10-21 10:00:00',
    
    address: {
      receiver: '张三',
      phone: '138****5678',
      province: '北京市',
      city: '北京市',
      district: '朝阳区',
      detail: '朝阳路123号'
    },
    
    items: [
      {
        item_id: 3,
        order_id: 1002,
        product_id: 6,
        activity_id: 2,
        team_id: 5005, // ⭐新增：拼团团ID
        team_no: 'T20251026002', // ⭐团号
        product_name: '鲜牛奶（拼团）',
        product_img: 'https://via.placeholder.com/100/4D96FF/FFFFFF?text=牛奶',
        price: 12.90,
        quantity: 3,
        total_price: 38.90
      }
    ]
  },
  {
    order_id: 1003,
    user_id: 1,
    leader_id: 1001,
    order_sn: '2025102109452378912',
    total_amount: 58.50,
    discount_amount: 0.00,
    pay_amount: 58.50,
    order_status: 0, // 待支付
    pay_status: 0, // 未支付
    pay_time: null,
    receive_address_id: 2,
    dispatch_group: null,
    delivery_id: null,
    create_time: '2025-10-21 09:45:23',
    update_time: '2025-10-21 09:45:23',
    
    address: {
      receiver: '李四',
      phone: '139****1234',
      province: '北京市',
      city: '北京市',
      district: '海淀区',
      detail: '中关村大街456号'
    },
    
    items: [
      {
        item_id: 4,
        order_id: 1003,
        product_id: 3,
        activity_id: null,
        team_id: null, // ⭐非拼团
        product_name: '西瓜',
        product_img: 'https://via.placeholder.com/100/98D8C8/FFFFFF?text=西瓜',
        price: 18.50,
        quantity: 1,
        total_price: 18.50
      },
      {
        item_id: 5,
        order_id: 1003,
        product_id: 5,
        activity_id: null,
        team_id: null, // ⭐非拼团
        product_name: '鸡蛋',
        product_img: 'https://via.placeholder.com/100/FAD390/333333?text=鸡蛋',
        price: 20.00,
        quantity: 2,
        total_price: 40.00
      }
    ]
  }
]

// 获取用户订单列表
export function getUserOrders(userId, status = null) {
  let orders = mockOrders.filter(order => order.user_id === parseInt(userId))
  
  if (status !== null) {
    orders = orders.filter(order => order.order_status === parseInt(status))
  }
  
  // 按创建时间倒序
  return orders.sort((a, b) => new Date(b.create_time) - new Date(a.create_time))
}

// 获取订单详情
export function getOrderDetail(orderId) {
  return mockOrders.find(order => order.order_id === parseInt(orderId))
}

// 获取订单状态文本
export function getOrderStatusText(status) {
  const statusMap = {
    [ORDER_STATUS.UNPAID]: '待支付',
    [ORDER_STATUS.TO_DELIVER]: '待发货',
    [ORDER_STATUS.DELIVERING]: '配送中',
    [ORDER_STATUS.DELIVERED]: '已送达',
    [ORDER_STATUS.CANCELLED]: '已取消',
    [ORDER_STATUS.REFUNDING]: '退款中',
    [ORDER_STATUS.REFUNDED]: '已退款'
  }
  return statusMap[status] || '未知状态'
}

// 获取订单状态类型（用于Element Plus Tag）
export function getOrderStatusType(status) {
  const typeMap = {
    [ORDER_STATUS.UNPAID]: 'warning',
    [ORDER_STATUS.TO_DELIVER]: 'primary',
    [ORDER_STATUS.DELIVERING]: 'info',
    [ORDER_STATUS.DELIVERED]: 'success',
    [ORDER_STATUS.CANCELLED]: 'info',
    [ORDER_STATUS.REFUNDING]: 'warning',
    [ORDER_STATUS.REFUNDED]: 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取支付方式文本
export function getPayTypeText(payType) {
  const typeMap = {
    [PAY_TYPE.WECHAT]: '微信支付',
    [PAY_TYPE.ALIPAY]: '支付宝支付',
    [PAY_TYPE.BALANCE]: '余额支付'
  }
  return typeMap[payType] || '未知支付方式'
}

// 模拟创建订单
export function createOrder(orderData) {
  const newOrderId = Math.max(...mockOrders.map(o => o.order_id)) + 1
  const now = new Date()
  const orderSn = now.toISOString().replace(/[-:T.Z]/g, '').slice(0, 14) + Math.floor(Math.random() * 1000000).toString().padStart(6, '0')
  
  const newOrder = {
    order_id: newOrderId,
    user_id: orderData.user_id,
    leader_id: orderData.leader_id || 1001, // 默认团长
    order_sn: orderSn,
    total_amount: orderData.total_amount,
    discount_amount: orderData.discount_amount || 0.00,
    pay_amount: orderData.total_amount - (orderData.discount_amount || 0.00),
    order_status: ORDER_STATUS.UNPAID,
    pay_status: PAY_STATUS.UNPAID,
    pay_time: null,
    receive_address_id: orderData.receive_address_id,
    dispatch_group: null,
    delivery_id: null,
    create_time: now.toISOString().slice(0, 19).replace('T', ' '),
    update_time: now.toISOString().slice(0, 19).replace('T', ' '),
    address: orderData.address,
    items: orderData.items
  }
  
  mockOrders.push(newOrder)
  return newOrder
}

// 模拟支付订单
export function payOrder(orderId, payType = PAY_TYPE.WECHAT) {
  const order = mockOrders.find(o => o.order_id === parseInt(orderId))
  if (order) {
    order.pay_status = PAY_STATUS.PAID
    order.pay_time = new Date().toISOString().slice(0, 19).replace('T', ' ')
    order.order_status = ORDER_STATUS.TO_DELIVER
    order.update_time = new Date().toISOString().slice(0, 19).replace('T', ' ')
    return { success: true, order }
  }
  return { success: false, message: '订单不存在' }
}

// 模拟取消订单
export function cancelOrder(orderId) {
  const order = mockOrders.find(o => o.order_id === parseInt(orderId))
  if (order && order.order_status === ORDER_STATUS.UNPAID) {
    order.order_status = ORDER_STATUS.CANCELLED
    order.update_time = new Date().toISOString().slice(0, 19).replace('T', ' ')
    return { success: true }
  }
  return { success: false, message: '订单状态不允许取消' }
}

// 格式化日期时间
export function formatDateTime(dateTime) {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * 获取订单的拼团信息（如果是拼团订单）
 * @param {Object} order - 订单对象
 * @returns {Object|null} 拼团信息 { team_id, team_no, team_status, team_progress }
 */
export function getOrderTeamInfo(order) {
  // 检查订单是否包含拼团商品
  const groupBuyItem = order.items?.find(item => item.team_id)
  if (!groupBuyItem) return null
  
  // 从 groupbuy.js 动态获取团状态（实际项目中应该从API获取）
  // 这里简化处理，返回订单项中的团信息
  return {
    team_id: groupBuyItem.team_id,
    team_no: groupBuyItem.team_no,
    // 注意：实际应该从 mockTeams 中查询团的实时状态
    // 这里仅用于展示
    has_team: true
  }
}

/**
 * 根据订单ID获取订单
 * @param {Number} orderId - 订单ID
 * @returns {Object|null} 订单对象
 */
export function getOrderById(orderId) {
  const order = mockOrders.find(o => o.order_id === parseInt(orderId))
  
  if (!order) return null
  
  // 检查是否有拼团信息
  const teamInfo = getOrderTeamInfo(order)
  
  return {
    ...order,
    team_info: teamInfo
  }
}

