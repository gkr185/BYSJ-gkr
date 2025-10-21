// 格式化工具函数

/**
 * 格式化价格
 * @param {Number} price - 价格
 * @param {Number} digits - 小数位数
 * @returns {String} 格式化后的价格
 */
export function formatPrice(price, digits = 2) {
  return Number(price).toFixed(digits)
}

/**
 * 格式化日期时间
 * @param {String|Date} date - 日期
 * @param {String} format - 格式: date, time, datetime
 * @returns {String} 格式化后的日期时间
 */
export function formatDate(date, format = 'datetime') {
  if (!date) return ''
  
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  switch (format) {
    case 'date':
      return `${year}-${month}-${day}`
    case 'time':
      return `${hours}:${minutes}:${seconds}`
    case 'datetime':
    default:
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
  }
}

/**
 * 格式化订单状态
 * @param {Number} status - 状态码
 * @returns {Object} { text, type }
 */
export function formatOrderStatus(status) {
  const statusMap = {
    0: { text: '待支付', type: 'warning' },
    1: { text: '待发货', type: 'primary' },
    2: { text: '配送中', type: 'info' },
    3: { text: '待自提', type: 'primary' },
    4: { text: '已完成', type: 'success' },
    5: { text: '已取消', type: 'info' },
    6: { text: '退款中', type: 'warning' },
    7: { text: '已退款', type: 'info' }
  }
  return statusMap[status] || { text: '未知', type: 'info' }
}

/**
 * 格式化拼团活动状态
 * @param {Number} status - 状态码
 * @returns {Object} { text, type }
 */
export function formatGroupBuyStatus(status) {
  const statusMap = {
    1: { text: '拼团中', type: 'warning' },
    2: { text: '已成团', type: 'success' },
    3: { text: '已结束', type: 'info' }
  }
  return statusMap[status] || { text: '未知', type: 'info' }
}

/**
 * 格式化手机号（脱敏）
 * @param {String} phone - 手机号
 * @returns {String} 脱敏后的手机号
 */
export function formatPhone(phone) {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 计算折扣
 * @param {Number} originalPrice - 原价
 * @param {Number} currentPrice - 现价
 * @returns {String} 折扣（如：8.5折）
 */
export function calculateDiscount(originalPrice, currentPrice) {
  if (!originalPrice || !currentPrice) return ''
  const discount = (currentPrice / originalPrice * 10).toFixed(1)
  return `${discount}折`
}

