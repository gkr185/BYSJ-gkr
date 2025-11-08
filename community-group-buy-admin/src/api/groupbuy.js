import request from '../utils/request'

/**
 * ====== 拼团活动管理（管理端） ======
 */

/**
 * 获取活动列表
 */
export const getActivityList = (params) => {
  return request({
    url: '/api/groupbuy/activities',
    method: 'GET',
    params
  })
}

/**
 * 获取进行中的活动
 */
export const getOngoingActivities = () => {
  return request({
    url: '/api/groupbuy/activities/ongoing',
    method: 'GET'
  })
}

/**
 * 获取活动详情
 */
export const getActivityDetail = (id) => {
  return request({
    url: `/api/groupbuy/activity/${id}`,
    method: 'GET'
  })
}

/**
 * 创建拼团活动
 */
export const createActivity = (data) => {
  return request({
    url: '/api/groupbuy/activity',
    method: 'POST',
    data
  })
}

/**
 * 更新拼团活动
 */
export const updateActivity = (id, data) => {
  return request({
    url: `/api/groupbuy/activity/${id}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除拼团活动
 */
export const deleteActivity = (id) => {
  return request({
    url: `/api/groupbuy/activity/${id}`,
    method: 'DELETE'
  })
}

/**
 * ====== 团管理（查询接口） ======
 */

/**
 * 获取团详情
 */
export const getTeamDetail = (teamId) => {
  return request({
    url: `/api/groupbuy/team/${teamId}/detail`,
    method: 'GET'
  })
}

/**
 * 获取活动的团列表
 * @param {number} activityId - 活动ID
 * @param {object} params - 查询参数
 * @param {number} params.communityId - 社区ID（可选）
 * @param {number} params.status - 团状态（0-拼团中, 1-已成团, 2-已失败，null-默认拼团中）
 * @param {boolean} params.includeExpired - 是否包含已过期的团（默认false）
 */
export const getActivityTeams = (activityId, params = {}) => {
  return request({
    url: `/api/groupbuy/activity/${activityId}/teams`,
    method: 'GET',
    params
  })
}

