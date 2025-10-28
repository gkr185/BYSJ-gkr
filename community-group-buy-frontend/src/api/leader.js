/**
 * 团长API接口
 * 原型阶段使用Mock数据
 */

import { 
  mockLeaderDashboard,
  mockLeaderTeams,
  mockTeamMembers,
  mockDeliveryOrders,
  mockDeliveryRoute,
  mockCommission,
  mockCommunityApplications,
  mockLeaderInfo,
  mockGroupBuyActivities
} from '@/mock/leader'

export default {
  /**
   * 获取团长信息
   */
  getLeaderInfo() {
    return Promise.resolve({
      code: 200,
      msg: '获取成功',
      data: mockLeaderInfo
    })
  },

  /**
   * 获取工作台数据
   */
  getDashboard() {
    return Promise.resolve({
      code: 200,
      msg: '获取成功',
      data: mockLeaderDashboard
    })
  },

  /**
   * 获取拼团活动列表
   */
  getActivities() {
    return Promise.resolve({
      code: 200,
      msg: '获取成功',
      data: mockGroupBuyActivities
    })
  },

  /**
   * 发起拼团
   * @param {Object} data - 发起参数
   * @param {Number} data.activityId - 活动ID
   * @param {Boolean} data.joinTeam - 是否参团
   */
  launchTeam(data) {
    // 模拟生成团号
    const timestamp = Date.now()
    const teamNo = `T${new Date().toISOString().slice(0, 10).replace(/-/g, '')}${String(timestamp).slice(-3)}`
    
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          msg: '发起成功',
          data: { 
            teamId: timestamp,
            teamNo,
            shareLink: `${window.location.origin}/groupbuy/team/${timestamp}`
          }
        })
      }, 500)
    })
  },

  /**
   * 获取发起的团列表
   * @param {Object} params - 查询参数
   * @param {Number} params.status - 团状态（可选）
   */
  getTeams(params = {}) {
    let teams = [...mockLeaderTeams]
    
    // 按状态筛选
    if (params.status !== undefined) {
      teams = teams.filter(t => t.teamStatus === params.status)
    }
    
    return Promise.resolve({
      code: 200,
      msg: '获取成功',
      data: teams
    })
  },

  /**
   * 获取团成员列表
   * @param {Number} teamId - 团ID
   */
  getMembers(teamId) {
    const members = mockTeamMembers[teamId] || []
    
    return Promise.resolve({
      code: 200,
      msg: '获取成功',
      data: members
    })
  },

  /**
   * 获取配送订单列表
   * @param {Object} params - 查询参数
   */
  getDeliveryOrders(params = {}) {
    return Promise.resolve({
      code: 200,
      msg: '获取成功',
      data: mockDeliveryOrders
    })
  },

  /**
   * 生成配送路径
   * @param {Array} orderIds - 订单ID数组
   */
  generateRoute(orderIds) {
    // 原型阶段返回Mock路径
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          msg: '生成成功',
          data: mockDeliveryRoute
        })
      }, 800)
    })
  },

  /**
   * 获取佣金数据
   */
  getCommission() {
    return Promise.resolve({
      code: 200,
      msg: '获取成功',
      data: mockCommission
    })
  },

  /**
   * 申请社区
   * @param {Object} data - 申请数据
   */
  applyCommunity(data) {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({
          code: 200,
          msg: '申请提交成功，等待管理员审核',
          data: null
        })
      }, 500)
    })
  },

  /**
   * 获取社区申请记录
   */
  getApplications() {
    return Promise.resolve({
      code: 200,
      msg: '获取成功',
      data: mockCommunityApplications
    })
  }
}
