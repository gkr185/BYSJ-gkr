import request from '../utils/request'

// ==================== 社区管理 API ====================

/**
 * 匹配最近的社区（核心接口）
 * 使用Haversine公式计算距离
 * @param {object} params - { latitude, longitude }
 */
export const getNearestCommunity = (params) => {
  return request({
    url: '/api/community/nearest',
    method: 'GET',
    params
  })
}

/**
 * 查询所有社区
 */
export const getCommunityList = () => {
  return request({
    url: '/api/community/list',
    method: 'GET'
  })
}

/**
 * 查询社区详情
 * @param {number} communityId - 社区ID
 */
export const getCommunityDetail = (communityId) => {
  return request({
    url: `/api/community/${communityId}`,
    method: 'GET'
  })
}

/**
 * 创建社区（管理员）
 * @param {object} data - 社区数据
 */
export const createCommunity = (data) => {
  return request({
    url: '/api/admin/community',
    method: 'POST',
    data
  })
}

/**
 * 更新社区（管理员）
 * @param {number} communityId - 社区ID
 * @param {object} data - 社区数据
 */
export const updateCommunity = (communityId, data) => {
  return request({
    url: `/api/admin/community/${communityId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除社区（管理员）
 * @param {number} communityId - 社区ID
 */
export const deleteCommunity = (communityId) => {
  return request({
    url: `/api/admin/community/${communityId}`,
    method: 'DELETE'
  })
}

// ==================== 社区申请审核 API ====================

/**
 * 提交社区申请
 * @param {object} data - 申请数据
 */
export const submitCommunityApplication = (data) => {
  return request({
    url: '/api/community-application',
    method: 'POST',
    data
  })
}

/**
 * 查询我的申请记录
 * @param {number} userId - 用户ID
 */
export const getMyCommunityApplications = (userId) => {
  return request({
    url: '/api/community-application/my',
    method: 'GET',
    params: { userId }
  })
}

/**
 * 查询申请详情
 * @param {number} applicationId - 申请ID
 */
export const getCommunityApplicationDetail = (applicationId) => {
  return request({
    url: `/api/community-application/${applicationId}`,
    method: 'GET'
  })
}

/**
 * 审核申请（管理员）
 * 审核通过后自动创建Community + GroupLeaderStore
 * @param {number} applicationId - 申请ID
 * @param {object} data - { reviewerId, approved, reviewComment }
 */
export const reviewCommunityApplication = (applicationId, data) => {
  return request({
    url: `/api/community-application/${applicationId}/review`,
    method: 'POST',
    data
  })
}

/**
 * 查询待审核申请（管理员）
 */
export const getPendingCommunityApplications = () => {
  return request({
    url: '/api/community-application/pending',
    method: 'GET'
  })
}

// ==================== 团长管理 API ====================

/**
 * 提交团长申请
 * @param {object} data - 申请数据
 */
export const submitLeaderApplication = (data) => {
  return request({
    url: '/api/leader/apply',
    method: 'POST',
    data
  })
}

/**
 * 查询我的团长信息
 * @param {number} userId - 用户ID
 */
export const getMyLeaderInfo = (userId) => {
  return request({
    url: '/api/leader/my',
    method: 'GET',
    params: { userId }
  })
}

/**
 * 查询社区的团长列表
 * @param {number} communityId - 社区ID
 */
export const getCommunityLeaders = (communityId) => {
  return request({
    url: `/api/leader/community/${communityId}`,
    method: 'GET'
  })
}

/**
 * 查询团长详情
 * @param {number} storeId - 团点ID
 */
export const getLeaderDetail = (storeId) => {
  return request({
    url: `/api/leader/${storeId}`,
    method: 'GET'
  })
}

/**
 * 更新团点信息（团长）
 * @param {number} storeId - 团点ID
 * @param {object} data - 团点数据
 */
export const updateLeaderStore = (storeId, data) => {
  return request({
    url: `/api/leader/${storeId}`,
    method: 'PUT',
    data
  })
}

/**
 * 审核团长申请（管理员）
 * 审核通过后自动调用UserService更新用户角色为2（团长）
 * @param {number} storeId - 团点ID
 * @param {object} data - { reviewerId, approved, reviewComment }
 */
export const reviewLeaderApplication = (storeId, data) => {
  return request({
    url: `/api/leader/${storeId}/review`,
    method: 'POST',
    data
  })
}

/**
 * 查询待审核申请（管理员）
 */
export const getPendingLeaderApplications = () => {
  return request({
    url: '/api/leader/pending',
    method: 'GET'
  })
}

/**
 * 停用团长（管理员）
 * @param {number} storeId - 团点ID
 */
export const disableLeader = (storeId) => {
  return request({
    url: `/api/leader/${storeId}/disable`,
    method: 'POST'
  })
}

// ==================== 佣金管理 API ====================

/**
 * 查询我的佣金记录（团长）
 * @param {object} params - 查询参数
 * @param {number} params.leaderId - 团长ID
 * @param {number} params.status - 结算状态 (0-未结算, 1-已结算)
 * @param {number} params.page - 页码
 * @param {number} params.limit - 每页数量
 * @returns {Promise<Object>} { list, total }
 */
export const getMyCommissionRecords = (params) => {
  return request({
    url: '/api/commission/my',
    method: 'GET',
    params
  })
}

/**
 * 查询佣金统计（团长）
 * @param {number} leaderId - 团长ID
 * @returns {object} { pendingCommission, settledCommission, totalCommission }
 */
export const getMyCommissionSummary = (leaderId) => {
  return request({
    url: '/api/commission/my/summary',
    method: 'GET',
    params: { leaderId }
  })
}

/**
 * 查询待结算佣金（管理员）
 */
export const getPendingCommissions = () => {
  return request({
    url: '/api/commission/pending',
    method: 'GET'
  })
}

/**
 * 查询结算批次（管理员）
 * @param {string} settlementBatch - 结算批次号（格式：YYYYMMDD）
 */
export const getCommissionBatch = (settlementBatch) => {
  return request({
    url: `/api/commission/batch/${settlementBatch}`,
    method: 'GET'
  })
}

/**
 * 手动结算佣金（管理员）
 */
export const settleCommissions = () => {
  return request({
    url: '/api/commission/settle',
    method: 'POST'
  })
}

