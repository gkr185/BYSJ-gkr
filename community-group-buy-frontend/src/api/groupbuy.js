import request from '../utils/request'

// ==================== 拼团活动 API ====================

/**
 * 获取拼团活动列表
 * @param {object} params - 查询参数 { page, size, status, communityId }
 */
export const getGroupBuyActivities = (params) => {
  return request({
    url: '/api/groupbuy/activity/list',
    method: 'GET',
    params
  })
}

/**
 * 获取拼团活动详情
 * @param {number} activityId - 活动ID
 */
export const getGroupBuyActivityDetail = (activityId) => {
  return request({
    url: `/api/groupbuy/activity/${activityId}`,
    method: 'GET'
  })
}

/**
 * 创建拼团活动（团长专属）
 * v3.0规则：只有团长可以创建拼团活动
 * @param {object} data - 活动数据
 */
export const createGroupBuyActivity = (data) => {
  return request({
    url: '/api/groupbuy/activity',
    method: 'POST',
    data
  })
}

/**
 * 更新拼团活动（团长）
 * @param {number} activityId - 活动ID
 * @param {object} data - 活动数据
 */
export const updateGroupBuyActivity = (activityId, data) => {
  return request({
    url: `/api/groupbuy/activity/${activityId}`,
    method: 'PUT',
    data
  })
}

/**
 * 结束拼团活动（团长）
 * @param {number} activityId - 活动ID
 */
export const endGroupBuyActivity = (activityId) => {
  return request({
    url: `/api/groupbuy/activity/${activityId}/end`,
    method: 'POST'
  })
}

/**
 * 获取社区的拼团活动（推荐）
 * @param {number} communityId - 社区ID
 * @param {object} params - 查询参数 { page, size }
 */
export const getCommunityGroupBuyActivities = (communityId, params) => {
  return request({
    url: `/api/groupbuy/activity/community/${communityId}`,
    method: 'GET',
    params
  })
}

// ==================== 拼团团队 API ====================

/**
 * 获取活动下的拼团团队列表
 * @param {number} activityId - 活动ID
 * @param {object} params - 查询参数 { page, size, status }
 */
export const getGroupBuyTeams = (activityId, params) => {
  return request({
    url: `/api/groupbuy/team/activity/${activityId}`,
    method: 'GET',
    params
  })
}

/**
 * 获取拼团团队详情
 * @param {number} teamId - 团队ID
 */
export const getGroupBuyTeamDetail = (teamId) => {
  return request({
    url: `/api/groupbuy/team/${teamId}`,
    method: 'GET'
  })
}

/**
 * 发起拼团（团长开团）
 * v3.0规则：只有团长可以发起拼团
 * @param {object} data - { activityId, leaderId, leaderName, targetCount, endTime }
 */
export const launchGroupBuyTeam = (data) => {
  return request({
    url: '/api/groupbuy/team/launch',
    method: 'POST',
    data
  })
}

/**
 * 查询团长发起的拼团（团长）
 * @param {number} leaderId - 团长ID
 * @param {object} params - 查询参数 { page, size, status }
 */
export const getLeaderGroupBuyTeams = (leaderId, params) => {
  return request({
    url: `/api/groupbuy/team/leader/${leaderId}`,
    method: 'GET',
    params
  })
}

// ==================== 拼团成员 API ====================

/**
 * 加入拼团
 * v3.0规则：加入时立即支付
 * @param {object} data - { teamId, userId, userName, phone, quantity, paymentAmount }
 */
export const joinGroupBuy = (data) => {
  return request({
    url: '/api/groupbuy/member/join',
    method: 'POST',
    data
  })
}

/**
 * 获取团队成员列表
 * @param {number} teamId - 团队ID
 */
export const getGroupBuyMembers = (teamId) => {
  return request({
    url: `/api/groupbuy/member/team/${teamId}`,
    method: 'GET'
  })
}

/**
 * 获取用户参与的拼团记录
 * @param {number} userId - 用户ID
 * @param {object} params - 查询参数 { page, size, status }
 */
export const getMyGroupBuys = (userId, params) => {
  return request({
    url: `/api/groupbuy/member/user/${userId}`,
    method: 'GET',
    params
  })
}

/**
 * 获取拼团成员详情
 * @param {number} memberId - 成员ID
 */
export const getGroupBuyMemberDetail = (memberId) => {
  return request({
    url: `/api/groupbuy/member/${memberId}`,
    method: 'GET'
  })
}

/**
 * 取消拼团（仅限未成团）
 * @param {number} memberId - 成员ID
 */
export const cancelGroupBuyMember = (memberId) => {
  return request({
    url: `/api/groupbuy/member/${memberId}/cancel`,
    method: 'POST'
  })
}

// ==================== 拼团订单 API ====================

/**
 * 创建拼团订单
 * v3.0规则：加入拼团时立即支付并创建订单
 * @param {object} data - 订单数据
 */
export const createGroupBuyOrder = (data) => {
  return request({
    url: '/api/groupbuy/order',
    method: 'POST',
    data
  })
}

/**
 * 获取拼团订单详情
 * @param {number} orderId - 订单ID
 */
export const getGroupBuyOrderDetail = (orderId) => {
  return request({
    url: `/api/groupbuy/order/${orderId}`,
    method: 'GET'
  })
}

/**
 * 获取用户的拼团订单列表
 * @param {number} userId - 用户ID
 * @param {object} params - 查询参数 { page, size, status }
 */
export const getMyGroupBuyOrders = (userId, params) => {
  return request({
    url: `/api/groupbuy/order/user/${userId}`,
    method: 'GET',
    params
  })
}

/**
 * 获取团队的拼团订单列表（团长）
 * @param {number} teamId - 团队ID
 */
export const getTeamGroupBuyOrders = (teamId) => {
  return request({
    url: `/api/groupbuy/order/team/${teamId}`,
    method: 'GET'
  })
}

// ==================== 拼团状态查询 ====================

/**
 * 检查拼团状态
 * @param {number} teamId - 团队ID
 * @returns {object} { status, currentCount, targetCount, remainingTime }
 */
export const checkGroupBuyStatus = (teamId) => {
  return request({
    url: `/api/groupbuy/team/${teamId}/status`,
    method: 'GET'
  })
}

/**
 * 检查用户是否已参与拼团
 * @param {number} userId - 用户ID
 * @param {number} teamId - 团队ID
 */
export const checkUserInTeam = (userId, teamId) => {
  return request({
    url: `/api/groupbuy/member/check`,
    method: 'GET',
    params: { userId, teamId }
  })
}

// ==================== 拼团统计（团长） ====================

/**
 * 获取团长的拼团统计
 * @param {number} leaderId - 团长ID
 * @returns {object} { totalTeams, successTeams, failedTeams, totalMembers, totalRevenue }
 */
export const getLeaderGroupBuyStatistics = (leaderId) => {
  return request({
    url: `/api/groupbuy/statistics/leader/${leaderId}`,
    method: 'GET'
  })
}

/**
 * 获取活动统计
 * @param {number} activityId - 活动ID
 * @returns {object} { totalTeams, successTeams, totalMembers, totalRevenue }
 */
export const getActivityStatistics = (activityId) => {
  return request({
    url: `/api/groupbuy/statistics/activity/${activityId}`,
    method: 'GET'
  })
}

