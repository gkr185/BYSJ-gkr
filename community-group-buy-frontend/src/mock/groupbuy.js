// 拼团Mock数据 - v2.0 优化版
// 严格遵守拼团逻辑优化方案的三层架构设计
// 数据库表: group_buy, group_buy_team, group_buy_member

// ==========================================
// 枚举定义
// ==========================================

// 活动状态 (group_buy.status)
export const ACTIVITY_STATUS = {
  NOT_STARTED: 0,  // 未开始
  ONGOING: 1,      // 进行中
  ENDED: 2,        // 已结束
  ABNORMAL: 3      // 异常
}

// 团状态 (group_buy_team.team_status)
export const TEAM_STATUS = {
  JOINING: 0,   // 拼团中
  SUCCESS: 1,   // 已成团
  FAILED: 2     // 已失败
}

// 参团状态 (group_buy_member.status)
export const MEMBER_STATUS = {
  UNPAID: 0,     // 待支付
  PAID: 1,       // 已支付
  SUCCESS: 2,    // 已成团
  CANCELLED: 3   // 已取消
}

// ==========================================
// 1. 拼团活动表 (group_buy) - 活动模板
// ==========================================

export const mockActivities = [
  {
    activity_id: 1,
    product_id: 1,
    group_price: 19.90,
    required_num: 3,
    max_num: null,  // 无最大人数限制
    start_time: '2025-10-21 00:00:00',
    end_time: '2025-10-28 23:59:59',
    status: ACTIVITY_STATUS.ONGOING,
    qrcode_url: 'https://via.placeholder.com/200/667eea/FFFFFF?text=拼团二维码',
    link: '/groupbuy/activity/1',
    create_time: '2025-10-20 10:00:00',
    
    // 关联商品信息（从 product 表）
    product_name: '新鲜草莓',
    product_img: 'https://via.placeholder.com/300x300/FF6B6B/FFFFFF?text=新鲜草莓',
    original_price: 25.00
  },
  {
    activity_id: 2,
    product_id: 6,
    group_price: 12.90,
    required_num: 5,
    max_num: 10,  // 最多10人
    start_time: '2025-10-20 00:00:00',
    end_time: '2025-10-26 23:59:59',
    status: ACTIVITY_STATUS.ONGOING,
    qrcode_url: 'https://via.placeholder.com/200/667eea/FFFFFF?text=拼团二维码',
    link: '/groupbuy/activity/2',
    create_time: '2025-10-19 15:00:00',
    
    product_name: '鲜牛奶',
    product_img: 'https://via.placeholder.com/300x300/4D96FF/FFFFFF?text=鲜牛奶',
    original_price: 15.00
  },
  {
    activity_id: 3,
    product_id: 10,
    group_price: 9.90,
    required_num: 2,
    max_num: null,
    start_time: '2025-10-21 08:00:00',
    end_time: '2025-10-25 23:59:59',
    status: ACTIVITY_STATUS.ONGOING,
    qrcode_url: 'https://via.placeholder.com/200/667eea/FFFFFF?text=拼团二维码',
    link: '/groupbuy/activity/3',
    create_time: '2025-10-21 07:00:00',
    
    product_name: '面包',
    product_img: 'https://via.placeholder.com/300x300/DFE4EA/333333?text=面包',
    original_price: 12.00
  }
]

// ==========================================
// 2. 团实例表 (group_buy_team) - 具体的团
// ==========================================

export const mockTeams = [
  // 活动1的团
  {
    team_id: 5001,
    team_no: 'T20251027001',
    activity_id: 1,
    launcher_id: 1,
    leader_id: 1001,
    required_num: 3,
    current_num: 2,
    team_status: TEAM_STATUS.JOINING,
    success_time: null,
    expire_time: '2025-10-28 14:00:00',
    create_time: '2025-10-27 14:00:00'
  },
  {
    team_id: 5002,
    team_no: 'T20251027002',
    activity_id: 1,
    launcher_id: 3,
    leader_id: 1001,
    required_num: 3,
    current_num: 3,
    team_status: TEAM_STATUS.SUCCESS,
    success_time: '2025-10-27 16:30:00',
    expire_time: '2025-10-28 15:00:00',
    create_time: '2025-10-27 15:00:00'
  },
  {
    team_id: 5003,
    team_no: 'T20251026001',
    activity_id: 1,
    launcher_id: 5,
    leader_id: 1002,
    required_num: 3,
    current_num: 1,
    team_status: TEAM_STATUS.FAILED,
    success_time: null,
    expire_time: '2025-10-27 10:00:00',
    create_time: '2025-10-26 10:00:00'
  },
  
  // 活动2的团
  {
    team_id: 5004,
    team_no: 'T20251027003',
    activity_id: 2,
    launcher_id: 2,
    leader_id: 1001,
    required_num: 5,
    current_num: 4,
    team_status: TEAM_STATUS.JOINING,
    success_time: null,
    expire_time: '2025-10-28 10:00:00',
    create_time: '2025-10-27 10:00:00'
  },
  {
    team_id: 5005,
    team_no: 'T20251026002',
    activity_id: 2,
    launcher_id: 4,
    leader_id: 1002,
    required_num: 5,
    current_num: 5,
    team_status: TEAM_STATUS.SUCCESS,
    success_time: '2025-10-26 18:00:00',
    expire_time: '2025-10-27 12:00:00',
    create_time: '2025-10-26 12:00:00'
  },
  
  // 活动3的团
  {
    team_id: 5006,
    team_no: 'T20251027004',
    activity_id: 3,
    launcher_id: 6,
    leader_id: 1001,
    required_num: 2,
    current_num: 1,
    team_status: TEAM_STATUS.JOINING,
    success_time: null,
    expire_time: '2025-10-28 08:30:00',
    create_time: '2025-10-27 08:30:00'
  }
]

// ==========================================
// 3. 参团记录表 (group_buy_member) - 用户参团记录
// ==========================================

export const mockMembers = [
  // 团5001的成员（拼团中，2/3人）
  {
    member_id: 1,
    team_id: 5001,
    user_id: 1,
    order_id: 8001,
    is_launcher: 1,
    pay_amount: 19.90,
    join_time: '2025-10-27 14:00:00',
    status: MEMBER_STATUS.PAID,
    user_name: '张三',
    avatar: 'https://via.placeholder.com/50/FF6B6B/FFFFFF?text=张'
  },
  {
    member_id: 2,
    team_id: 5001,
    user_id: 2,
    order_id: 8002,
    is_launcher: 0,
    pay_amount: 19.90,
    join_time: '2025-10-27 14:15:00',
    status: MEMBER_STATUS.PAID,
    user_name: '李四',
    avatar: 'https://via.placeholder.com/50/4ECDC4/FFFFFF?text=李'
  },
  
  // 团5002的成员（已成团，3/3人）
  {
    member_id: 3,
    team_id: 5002,
    user_id: 3,
    order_id: 8003,
    is_launcher: 1,
    pay_amount: 19.90,
    join_time: '2025-10-27 15:00:00',
    status: MEMBER_STATUS.SUCCESS,
    user_name: '王五',
    avatar: 'https://via.placeholder.com/50/FFD93D/333333?text=王'
  },
  {
    member_id: 4,
    team_id: 5002,
    user_id: 4,
    order_id: 8004,
    is_launcher: 0,
    pay_amount: 19.90,
    join_time: '2025-10-27 15:20:00',
    status: MEMBER_STATUS.SUCCESS,
    user_name: '赵六',
    avatar: 'https://via.placeholder.com/50/A29BFE/FFFFFF?text=赵'
  },
  {
    member_id: 5,
    team_id: 5002,
    user_id: 5,
    order_id: 8005,
    is_launcher: 0,
    pay_amount: 19.90,
    join_time: '2025-10-27 16:30:00',
    status: MEMBER_STATUS.SUCCESS,
    user_name: '孙七',
    avatar: 'https://via.placeholder.com/50/74B9FF/FFFFFF?text=孙'
  },
  
  // 团5003的成员（已失败，1/3人）
  {
    member_id: 6,
    team_id: 5003,
    user_id: 5,
    order_id: 8006,
    is_launcher: 1,
    pay_amount: 19.90,
    join_time: '2025-10-26 10:00:00',
    status: MEMBER_STATUS.CANCELLED,
    user_name: '周八',
    avatar: 'https://via.placeholder.com/50/6BCB77/FFFFFF?text=周'
  },
  
  // 团5004的成员（拼团中，4/5人）
  {
    member_id: 7,
    team_id: 5004,
    user_id: 2,
    order_id: 8007,
    is_launcher: 1,
    pay_amount: 12.90,
    join_time: '2025-10-27 10:00:00',
    status: MEMBER_STATUS.PAID,
    user_name: '李四',
    avatar: 'https://via.placeholder.com/50/4ECDC4/FFFFFF?text=李'
  },
  {
    member_id: 8,
    team_id: 5004,
    user_id: 6,
    order_id: 8008,
    is_launcher: 0,
    pay_amount: 12.90,
    join_time: '2025-10-27 10:15:00',
    status: MEMBER_STATUS.PAID,
    user_name: '吴九',
    avatar: 'https://via.placeholder.com/50/FF6B9D/FFFFFF?text=吴'
  },
  {
    member_id: 9,
    team_id: 5004,
    user_id: 7,
    order_id: 8009,
    is_launcher: 0,
    pay_amount: 12.90,
    join_time: '2025-10-27 10:30:00',
    status: MEMBER_STATUS.PAID,
    user_name: '郑十',
    avatar: 'https://via.placeholder.com/50/FFEAA7/333333?text=郑'
  },
  {
    member_id: 10,
    team_id: 5004,
    user_id: 8,
    order_id: 8010,
    is_launcher: 0,
    pay_amount: 12.90,
    join_time: '2025-10-27 10:45:00',
    status: MEMBER_STATUS.PAID,
    user_name: '陈十一',
    avatar: 'https://via.placeholder.com/50/DFE6E9/333333?text=陈'
  },
  
  // 团5005的成员（已成团，5/5人）
  {
    member_id: 11,
    team_id: 5005,
    user_id: 4,
    order_id: 8011,
    is_launcher: 1,
    pay_amount: 12.90,
    join_time: '2025-10-26 12:00:00',
    status: MEMBER_STATUS.SUCCESS,
    user_name: '赵六',
    avatar: 'https://via.placeholder.com/50/A29BFE/FFFFFF?text=赵'
  },
  {
    member_id: 12,
    team_id: 5005,
    user_id: 9,
    order_id: 8012,
    is_launcher: 0,
    pay_amount: 12.90,
    join_time: '2025-10-26 12:15:00',
    status: MEMBER_STATUS.SUCCESS,
    user_name: '黄十二',
    avatar: 'https://via.placeholder.com/50/FD79A8/FFFFFF?text=黄'
  },
  {
    member_id: 13,
    team_id: 5005,
    user_id: 10,
    order_id: 8013,
    is_launcher: 0,
    pay_amount: 12.90,
    join_time: '2025-10-26 12:30:00',
    status: MEMBER_STATUS.SUCCESS,
    user_name: '林十三',
    avatar: 'https://via.placeholder.com/50/A29BFE/FFFFFF?text=林'
  },
  {
    member_id: 14,
    team_id: 5005,
    user_id: 11,
    order_id: 8014,
    is_launcher: 0,
    pay_amount: 12.90,
    join_time: '2025-10-26 13:00:00',
    status: MEMBER_STATUS.SUCCESS,
    user_name: '何十四',
    avatar: 'https://via.placeholder.com/50/FDCB6E/333333?text=何'
  },
  {
    member_id: 15,
    team_id: 5005,
    user_id: 12,
    order_id: 8015,
    is_launcher: 0,
    pay_amount: 12.90,
    join_time: '2025-10-26 18:00:00',
    status: MEMBER_STATUS.SUCCESS,
    user_name: '宋十五',
    avatar: 'https://via.placeholder.com/50/E17055/FFFFFF?text=宋'
  },
  
  // 团5006的成员（拼团中，1/2人）
  {
    member_id: 16,
    team_id: 5006,
    user_id: 6,
    order_id: 8016,
    is_launcher: 1,
    pay_amount: 9.90,
    join_time: '2025-10-27 08:30:00',
    status: MEMBER_STATUS.PAID,
    user_name: '吴九',
    avatar: 'https://via.placeholder.com/50/FF6B9D/FFFFFF?text=吴'
  }
]

// ==========================================
// API 函数
// ==========================================

/**
 * 获取拼团活动详情
 * @param {Number} activityId - 活动ID
 * @returns {Object|null} 活动详情
 */
export function getActivityDetail(activityId) {
  return mockActivities.find(activity => activity.activity_id === parseInt(activityId)) || null
}

/**
 * 获取活动的所有团列表
 * @param {Number} activityId - 活动ID
 * @param {String} statusFilter - 团状态筛选 ('all', 'joining', 'success', 'failed')
 * @returns {Array} 团列表（包含活动和成员信息）
 */
export function getActivityTeams(activityId, statusFilter = 'all') {
  let teams = mockTeams.filter(team => team.activity_id === parseInt(activityId))
  
  // 状态筛选
  if (statusFilter !== 'all') {
    const statusMap = {
      'joining': TEAM_STATUS.JOINING,
      'success': TEAM_STATUS.SUCCESS,
      'failed': TEAM_STATUS.FAILED
    }
    const targetStatus = statusMap[statusFilter]
    if (targetStatus !== undefined) {
      teams = teams.filter(team => team.team_status === targetStatus)
    }
  }
  
  // 为每个团附加活动信息和成员信息
  return teams.map(team => {
    const activity = getActivityDetail(team.activity_id)
    const members = getTeamMembers(team.team_id)
    
    return {
      ...team,
      activity,
      members,
      // 计算剩余时间
      remaining_time: calculateRemainingTime(team.expire_time)
    }
  }).sort((a, b) => new Date(b.create_time) - new Date(a.create_time)) // 按创建时间倒序
}

/**
 * 获取团详情
 * @param {Number} teamId - 团ID
 * @returns {Object|null} 团详情（包含活动、成员、剩余时间）
 */
export function getTeamDetail(teamId) {
  const team = mockTeams.find(t => t.team_id === parseInt(teamId))
  if (!team) return null
  
  const activity = getActivityDetail(team.activity_id)
  const members = getTeamMembers(team.team_id)
  const remaining_time = calculateRemainingTime(team.expire_time)
  
  return {
    ...team,
    activity,
    members,
    remaining_time
  }
}

/**
 * 获取团的成员列表
 * @param {Number} teamId - 团ID
 * @returns {Array} 成员列表
 */
export function getTeamMembers(teamId) {
  return mockMembers.filter(member => member.team_id === parseInt(teamId))
}

/**
 * 发起新团
 * @param {Object} data - { activity_id, user_id, leader_id }
 * @returns {Object} 新创建的团实例
 */
export function launchTeam(data) {
  const activity = getActivityDetail(data.activity_id)
  if (!activity) {
    throw new Error('活动不存在')
  }
  
  // 生成团号
  const now = new Date()
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
  const randomNum = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
  const team_no = `T${dateStr}${randomNum}`
  
  // 创建团实例
  const newTeam = {
    team_id: Math.max(...mockTeams.map(t => t.team_id)) + 1,
    team_no,
    activity_id: data.activity_id,
    launcher_id: data.user_id,
    leader_id: data.leader_id,
    required_num: activity.required_num,
    current_num: 0,  // 初始为0，参团后更新
    team_status: TEAM_STATUS.JOINING,
    success_time: null,
    expire_time: new Date(now.getTime() + 24 * 60 * 60 * 1000).toISOString().slice(0, 19).replace('T', ' '), // 24小时后
    create_time: now.toISOString().slice(0, 19).replace('T', ' ')
  }
  
  mockTeams.push(newTeam)
  return newTeam
}

/**
 * 参与拼团
 * @param {Object} data - { team_id, user_id, order_id, pay_amount }
 * @returns {Object} 参团记录
 */
export function joinTeam(data) {
  const team = mockTeams.find(t => t.team_id === parseInt(data.team_id))
  if (!team) {
    throw new Error('团不存在')
  }
  
  if (team.team_status !== TEAM_STATUS.JOINING) {
    throw new Error('团状态不允许参与')
  }
  
  // 检查是否已参团
  const existingMember = mockMembers.find(m => 
    m.team_id === parseInt(data.team_id) && m.user_id === parseInt(data.user_id)
  )
  if (existingMember) {
    throw new Error('您已参加此团')
  }
  
  // 检查是否是发起人
  const is_launcher = team.launcher_id === parseInt(data.user_id) ? 1 : 0
  
  // 创建参团记录
  const newMember = {
    member_id: Math.max(...mockMembers.map(m => m.member_id)) + 1,
    team_id: data.team_id,
    user_id: data.user_id,
    order_id: data.order_id,
    is_launcher,
    pay_amount: data.pay_amount,
    join_time: new Date().toISOString().slice(0, 19).replace('T', ' '),
    status: MEMBER_STATUS.PAID,  // Mock场景默认已支付
    user_name: data.user_name || '用户' + data.user_id,
    avatar: data.avatar || `https://via.placeholder.com/50/667eea/FFFFFF?text=U${data.user_id}`
  }
  
  mockMembers.push(newMember)
  
  // 更新团人数
  team.current_num += 1
  
  // 检查是否成团
  if (team.current_num >= team.required_num) {
    team.team_status = TEAM_STATUS.SUCCESS
    team.success_time = new Date().toISOString().slice(0, 19).replace('T', ' ')
    
    // 更新所有成员状态为已成团
    mockMembers.forEach(member => {
      if (member.team_id === team.team_id) {
        member.status = MEMBER_STATUS.SUCCESS
      }
    })
  }
  
  return newMember
}

/**
 * 退出拼团（成团前）
 * @param {Number} teamId - 团ID
 * @param {Number} userId - 用户ID
 * @returns {Boolean} 是否成功
 */
export function quitTeam(teamId, userId) {
  const team = mockTeams.find(t => t.team_id === parseInt(teamId))
  if (!team) {
    throw new Error('团不存在')
  }
  
  if (team.team_status !== TEAM_STATUS.JOINING) {
    throw new Error('团已成团，无法退出')
  }
  
  const memberIndex = mockMembers.findIndex(m => 
    m.team_id === parseInt(teamId) && m.user_id === parseInt(userId)
  )
  
  if (memberIndex === -1) {
    throw new Error('您未参加此团')
  }
  
  // 删除参团记录
  mockMembers.splice(memberIndex, 1)
  
  // 更新团人数
  team.current_num -= 1
  
  return true
}

/**
 * 计算剩余时间
 * @param {String} expireTime - 过期时间
 * @returns {Object} { hours, minutes, seconds, expired }
 */
export function calculateRemainingTime(expireTime) {
  const end = new Date(expireTime).getTime()
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

/**
 * 获取活动状态文本
 * @param {Number} status - 状态码
 * @returns {String} 状态文本
 */
export function getActivityStatusText(status) {
  const statusMap = {
    [ACTIVITY_STATUS.NOT_STARTED]: '未开始',
    [ACTIVITY_STATUS.ONGOING]: '进行中',
    [ACTIVITY_STATUS.ENDED]: '已结束',
    [ACTIVITY_STATUS.ABNORMAL]: '异常'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取团状态文本
 * @param {Number} status - 状态码
 * @returns {String} 状态文本
 */
export function getTeamStatusText(status) {
  const statusMap = {
    [TEAM_STATUS.JOINING]: '拼团中',
    [TEAM_STATUS.SUCCESS]: '已成团',
    [TEAM_STATUS.FAILED]: '已失败'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取团状态类型（用于Element Plus Tag）
 * @param {Number} status - 状态码
 * @returns {String} 类型
 */
export function getTeamStatusType(status) {
  const typeMap = {
    [TEAM_STATUS.JOINING]: 'warning',
    [TEAM_STATUS.SUCCESS]: 'success',
    [TEAM_STATUS.FAILED]: 'info'
  }
  return typeMap[status] || 'info'
}

/**
 * 格式化日期时间
 * @param {String} dateTime - 日期时间字符串
 * @returns {String} 格式化后的字符串
 */
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
 * 获取我的拼团记录
 * @param {Number} userId - 用户ID（可选，从store获取）
 * @returns {Array} 拼团记录列表
 */
export function getMyTeams(userId = 1) {
  // 查询用户的所有参团记录
  const myMembers = mockMembers.filter(m => m.user_id === parseInt(userId))
  
  // 组装完整信息
  const myTeams = myMembers.map(member => {
    const team = mockTeams.find(t => t.team_id === member.team_id)
    const activity = mockActivities.find(a => a.activity_id === team?.activity_id)
    
    if (!team || !activity) {
      return null
    }
    
    return {
      // 参团记录信息
      member_id: member.member_id,
      team_id: member.team_id,
      user_id: member.user_id,
      order_id: member.order_id,
      is_launcher: member.is_launcher,
      pay_amount: member.pay_amount,
      join_time: member.join_time,
      status: member.status,
      
      // 团信息
      team_no: team.team_no,
      team_status: team.team_status,
      launcher_id: team.launcher_id,
      required_num: team.required_num,
      current_num: team.current_num,
      success_time: team.success_time,
      expire_time: team.expire_time,
      create_time: team.create_time,
      
      // 活动信息
      activity_id: activity.activity_id,
      product_id: activity.product_id,
      product_name: activity.product_name,
      product_img: activity.product_img,
      group_price: activity.group_price,
      original_price: activity.original_price
    }
  }).filter(item => item !== null)
  
  // 按参团时间倒序排列（最新的在前）
  myTeams.sort((a, b) => new Date(b.join_time) - new Date(a.join_time))
  
  return myTeams
}
