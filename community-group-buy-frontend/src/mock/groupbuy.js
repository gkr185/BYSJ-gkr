// 拼团活动Mock数据 - 严格遵守数据库设计文档字段标准
// 数据库表: group_buy, group_buy_join

// 模拟拼团活动数据
export const mockGroupBuyActivities = [
  {
    // group_buy 表字段
    activity_id: 1,
    product_id: 1,
    leader_id: 1001, // 团长ID (可为null，系统默认团长)
    group_price: 19.90, // 拼团价
    required_num: 3, // 成团人数
    start_time: '2025-10-21 00:00:00',
    end_time: '2025-10-23 23:59:59',
    status: 1, // 0:未开始 1:进行中 2:已结束 3:异常
    qrcode_url: 'https://via.placeholder.com/200/667eea/FFFFFF?text=拼团二维码',
    link: '/group/1/abc123', // 拼团专属链接
    
    // 关联商品信息（从 product 表）
    product_name: '新鲜草莓',
    product_img: 'https://via.placeholder.com/300x300/FF6B6B/FFFFFF?text=新鲜草莓',
    original_price: 25.00, // product.price
    
    // 拼团参与记录（从 group_buy_join 表）
    current_num: 2,
    members: [
      { 
        join_id: 1,
        activity_id: 1,
        user_id: 1, 
        user_name: '张三', 
        avatar: 'https://via.placeholder.com/50/FF6B6B/FFFFFF?text=张', 
        join_time: '2025-10-21 09:00:00', 
        is_launcher: 1, // 是否发起者 (0-否；1-是)
        status: 1 // 0:已退出 1:参与中 2:成团
      },
      { 
        join_id: 2,
        activity_id: 1,
        user_id: 2, 
        user_name: '李四', 
        avatar: 'https://via.placeholder.com/50/4ECDC4/FFFFFF?text=李', 
        join_time: '2025-10-21 09:15:00', 
        is_launcher: 0,
        status: 1
      }
    ]
  },
  {
    activity_id: 2,
    product_id: 6,
    leader_id: 1002,
    group_price: 12.90,
    required_num: 5,
    start_time: '2025-10-20 00:00:00',
    end_time: '2025-10-22 23:59:59',
    status: 2, // 已成团
    qrcode_url: 'https://via.placeholder.com/200/667eea/FFFFFF?text=拼团二维码',
    link: '/group/2/def456',
    
    product_name: '鲜牛奶',
    product_img: 'https://via.placeholder.com/300x300/4D96FF/FFFFFF?text=鲜牛奶',
    original_price: 15.00,
    
    current_num: 5,
    members: [
      { join_id: 3, activity_id: 2, user_id: 3, user_name: '王五', avatar: 'https://via.placeholder.com/50/FFD93D/333333?text=王', join_time: '2025-10-20 10:00:00', is_launcher: 1, status: 2 },
      { join_id: 4, activity_id: 2, user_id: 4, user_name: '赵六', avatar: 'https://via.placeholder.com/50/A29BFE/FFFFFF?text=赵', join_time: '2025-10-20 10:05:00', is_launcher: 0, status: 2 },
      { join_id: 5, activity_id: 2, user_id: 5, user_name: '孙七', avatar: 'https://via.placeholder.com/50/74B9FF/FFFFFF?text=孙', join_time: '2025-10-20 10:10:00', is_launcher: 0, status: 2 },
      { join_id: 6, activity_id: 2, user_id: 6, user_name: '周八', avatar: 'https://via.placeholder.com/50/6BCB77/FFFFFF?text=周', join_time: '2025-10-20 10:15:00', is_launcher: 0, status: 2 },
      { join_id: 7, activity_id: 2, user_id: 7, user_name: '吴九', avatar: 'https://via.placeholder.com/50/FF6B9D/FFFFFF?text=吴', join_time: '2025-10-20 10:20:00', is_launcher: 0, status: 2 }
    ]
  },
  {
    activity_id: 3,
    product_id: 10,
    leader_id: null, // 系统默认团长
    group_price: 9.90,
    required_num: 2,
    start_time: '2025-10-21 08:00:00',
    end_time: '2025-10-22 23:59:59',
    status: 1,
    qrcode_url: 'https://via.placeholder.com/200/667eea/FFFFFF?text=拼团二维码',
    link: '/group/3/ghi789',
    
    product_name: '面包',
    product_img: 'https://via.placeholder.com/300x300/DFE4EA/333333?text=面包',
    original_price: 12.00,
    
    current_num: 1,
    members: [
      { join_id: 8, activity_id: 3, user_id: 8, user_name: '郑十', avatar: 'https://via.placeholder.com/50/FFEAA7/333333?text=郑', join_time: '2025-10-21 08:30:00', is_launcher: 1, status: 1 }
    ]
  }
]

// 获取商品的拼团活动列表
export function getProductGroupBuyActivities(productId) {
  return mockGroupBuyActivities.filter(activity => 
    activity.product_id === parseInt(productId) && activity.status === 1
  )
}

// 获取拼团活动详情
export function getGroupBuyDetail(activityId) {
  return mockGroupBuyActivities.find(activity => 
    activity.activity_id === parseInt(activityId)
  )
}

// 计算剩余时间（返回小时和分钟）
export function calculateRemainingTime(endTime) {
  const end = new Date(endTime).getTime()
  const now = new Date().getTime()
  const diff = end - now
  
  if (diff <= 0) {
    return { hours: 0, minutes: 0, seconds: 0, expired: true }
  }
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  return { hours, minutes, seconds, expired: false }
}

