/**
 * 拼团相关API
 * 
 * 当前版本：Mock数据版本
 * 后端接口ready后：直接替换 import 和 函数实现，保持接口签名不变
 */

// 当前使用Mock数据
import {
  getActivityDetail,
  getActivityTeams,
  getTeamDetail,
  getTeamMembers,
  launchTeam,
  joinTeam,
  quitTeam,
  getMyTeams,
  ACTIVITY_STATUS,
  TEAM_STATUS,
  MEMBER_STATUS,
  getActivityStatusText,
  getTeamStatusText,
  getTeamStatusType,
  calculateRemainingTime,
  formatDateTime
} from '@/mock/groupbuy'

// ⚠️ 后端接口ready后替换为：
// import request from '@/utils/request'

// ==========================================
// API函数 - 保持接口签名，便于后续替换
// ==========================================

/**
 * 获取拼团活动详情
 * @param {Number} activityId - 活动ID
 * @returns {Promise<Object>} 活动详情
 */
export function apiGetActivityDetail(activityId) {
  // 当前版本：Mock数据
  return Promise.resolve(getActivityDetail(activityId))
  
  // ⚠️ 后端接口ready后替换为：
  // return request({
  //   url: `/groupbuy/activity/${activityId}`,
  //   method: 'get'
  // })
}

/**
 * 获取活动的所有团列表
 * @param {Number} activityId - 活动ID
 * @param {String} statusFilter - 团状态筛选 ('all', 'joining', 'success', 'failed')
 * @returns {Promise<Array>} 团列表
 */
export function apiGetActivityTeams(activityId, statusFilter = 'all') {
  // 当前版本：Mock数据
  return Promise.resolve(getActivityTeams(activityId, statusFilter))
  
  // ⚠️ 后端接口ready后替换为：
  // return request({
  //   url: `/groupbuy/activity/${activityId}/teams`,
  //   method: 'get',
  //   params: { status: statusFilter }
  // })
}

/**
 * 获取团详情
 * @param {Number} teamId - 团ID
 * @returns {Promise<Object>} 团详情
 */
export function apiGetTeamDetail(teamId) {
  // 当前版本：Mock数据
  return Promise.resolve(getTeamDetail(teamId))
  
  // ⚠️ 后端接口ready后替换为：
  // return request({
  //   url: `/groupbuy/team/${teamId}`,
  //   method: 'get'
  // })
}

/**
 * 获取团的成员列表
 * @param {Number} teamId - 团ID
 * @returns {Promise<Array>} 成员列表
 */
export function apiGetTeamMembers(teamId) {
  // 当前版本：Mock数据
  return Promise.resolve(getTeamMembers(teamId))
  
  // ⚠️ 后端接口ready后替换为：
  // return request({
  //   url: `/groupbuy/team/${teamId}/members`,
  //   method: 'get'
  // })
}

/**
 * 发起新团
 * @param {Object} data - { activity_id, user_id, leader_id }
 * @returns {Promise<Object>} 新创建的团实例
 */
export function apiLaunchTeam(data) {
  // 当前版本：Mock数据
  return Promise.resolve(launchTeam(data))
  
  // ⚠️ 后端接口ready后替换为：
  // return request({
  //   url: '/groupbuy/team/launch',
  //   method: 'post',
  //   data
  // })
}

/**
 * 参与拼团
 * @param {Object} data - { team_id, user_id, order_id, pay_amount }
 * @returns {Promise<Object>} 参团记录
 */
export function apiJoinTeam(data) {
  // 当前版本：Mock数据
  return Promise.resolve(joinTeam(data))
  
  // ⚠️ 后端接口ready后替换为：
  // return request({
  //   url: '/groupbuy/team/join',
  //   method: 'post',
  //   data
  // })
}

/**
 * 退出拼团（成团前）
 * @param {Number} teamId - 团ID
 * @param {Number} userId - 用户ID
 * @returns {Promise<Boolean>} 是否成功
 */
export function apiQuitTeam(teamId, userId = null) {
  // 当前版本：Mock数据
  // 如果没有传userId，从store获取（Mock默认为1）
  const finalUserId = userId || 1
  return Promise.resolve(quitTeam(teamId, finalUserId))
  
  // ⚠️ 后端接口ready后替换为：
  // return request({
  //   url: `/groupbuy/team/${teamId}/quit`,
  //   method: 'post',
  //   data: { user_id: userId }
  // })
}

/**
 * 获取我的拼团记录
 * @param {Number} userId - 用户ID（可选，从store获取）
 * @returns {Promise<Array>} 拼团记录列表
 */
export function apiGetMyTeams(userId = null) {
  // 当前版本：Mock数据
  const finalUserId = userId || 1
  return Promise.resolve(getMyTeams(finalUserId))
  
  // ⚠️ 后端接口ready后替换为：
  // return request({
  //   url: '/groupbuy/my',
  //   method: 'get'
  // })
}

// ==========================================
// 导出枚举和辅助函数（直接透传）
// ==========================================

export {
  ACTIVITY_STATUS,
  TEAM_STATUS,
  MEMBER_STATUS,
  getActivityStatusText,
  getTeamStatusText,
  getTeamStatusType,
  calculateRemainingTime,
  formatDateTime
}

