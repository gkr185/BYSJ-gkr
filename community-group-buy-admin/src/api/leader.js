import request from '../utils/request'

// ==================== 社区管理 API ====================

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
 */
export const getCommunityById = (communityId) => {
  return request({
    url: `/api/community/${communityId}`,
    method: 'GET'
  })
}

/**
 * 【管理员】创建社区
 */
export const createCommunity = (data) => {
  return request({
    url: '/api/admin/community',
    method: 'POST',
    data
  })
}

/**
 * 【管理员】更新社区
 */
export const updateCommunity = (communityId, data) => {
  return request({
    url: `/api/admin/community/${communityId}`,
    method: 'PUT',
    data
  })
}

/**
 * 【管理员】删除社区（软删除）
 */
export const deleteCommunity = (communityId) => {
  return request({
    url: `/api/admin/community/${communityId}`,
    method: 'DELETE'
  })
}

/**
 * 【管理员】根据状态查询社区列表
 * @param {Number} status 0-待审核 1-正常运营 2-已关闭
 */
export const getCommunitiesByStatus = (status) => {
  return request({
    url: '/api/admin/community/all',
    method: 'GET',
    params: { status }
  })
}

// ==================== 社区申请审核 API ====================

/**
 * 【管理员】查询待审核的社区申请
 */
export const getPendingCommunityApplications = () => {
  return request({
    url: '/api/community-application/pending',
    method: 'GET'
  })
}

/**
 * 【管理员】根据状态查询社区申请
 * @param {Number} status 0-待审核 1-审核通过 2-审核拒绝
 */
export const getCommunityApplicationsByStatus = (status) => {
  return request({
    url: '/api/community-application/list',
    method: 'GET',
    params: { status }
  })
}

/**
 * 查询社区申请详情
 */
export const getCommunityApplicationById = (applicationId) => {
  return request({
    url: `/api/community-application/${applicationId}`,
    method: 'GET'
  })
}

/**
 * 【管理员】补充社区申请的经纬度信息（审核前调用）
 * @param {Number} applicationId 申请ID
 * @param {Number} latitude 纬度
 * @param {Number} longitude 经度
 */
export const updateCommunityApplicationCoordinates = (applicationId, latitude, longitude) => {
  return request({
    url: `/api/community-application/${applicationId}/coordinates`,
    method: 'POST',
    params: {
      latitude,
      longitude
    }
  })
}

/**
 * 【管理员】审核社区申请
 * @param {Number} applicationId 申请ID
 * @param {Number} reviewerId 审核人ID
 * @param {Boolean} approved 是否通过
 * @param {String} reviewComment 审核意见
 */
export const reviewCommunityApplication = (applicationId, reviewerId, approved, reviewComment) => {
  return request({
    url: `/api/community-application/${applicationId}/review`,
    method: 'POST',
    params: {
      reviewerId,
      approved,
      reviewComment
    }
  })
}

// ==================== 团长管理 API ====================

/**
 * 【管理员】查询待审核的团长申请
 */
export const getPendingLeaderApplications = () => {
  return request({
    url: '/api/leader/pending',
    method: 'GET'
  })
}

/**
 * 【管理员】根据状态查询团长列表
 * @param {Number} status 0-待审核 1-正常运营 2-已停用
 */
export const getLeadersByStatus = (status) => {
  return request({
    url: '/api/leader/list',
    method: 'GET',
    params: { status }
  })
}

/**
 * 查询团长详情
 */
export const getLeaderById = (storeId) => {
  return request({
    url: `/api/leader/${storeId}`,
    method: 'GET'
  })
}

/**
 * 【管理员】补充团点的经纬度信息（审核前调用）
 * @param {Number} storeId 团点ID
 * @param {Number} latitude 纬度
 * @param {Number} longitude 经度
 */
export const updateLeaderStoreCoordinates = (storeId, latitude, longitude) => {
  return request({
    url: `/api/leader/${storeId}/coordinates`,
    method: 'POST',
    params: {
      latitude,
      longitude
    }
  })
}

/**
 * 【管理员】审核团长申请
 * @param {Number} storeId 团点ID
 * @param {Number} reviewerId 审核人ID
 * @param {Boolean} approved 是否通过
 * @param {String} reviewComment 审核意见
 */
export const reviewLeaderApplication = (storeId, reviewerId, approved, reviewComment) => {
  return request({
    url: `/api/leader/${storeId}/review`,
    method: 'POST',
    params: {
      reviewerId,
      approved,
      reviewComment
    }
  })
}

/**
 * 【团长/管理员】更新团点信息
 */
export const updateLeaderStore = (storeId, data) => {
  return request({
    url: `/api/leader/${storeId}`,
    method: 'PUT',
    data
  })
}

/**
 * 【管理员】启用团长
 */
export const enableLeader = (storeId) => {
  return request({
    url: `/api/leader/${storeId}/enable`,
    method: 'POST'
  })
}

/**
 * 【管理员】停用团长
 */
export const disableLeader = (storeId) => {
  return request({
    url: `/api/leader/${storeId}/disable`,
    method: 'POST'
  })
}

/**
 * 查询社区的团长列表
 */
export const getLeadersByCommunity = (communityId) => {
  return request({
    url: `/api/leader/community/${communityId}`,
    method: 'GET'
  })
}

// ==================== 佣金管理 API ====================

/**
 * 【管理员】查询所有待结算佣金
 */
export const getPendingCommissions = () => {
  return request({
    url: '/api/commission/pending',
    method: 'GET'
  })
}

/**
 * 【管理员】查询所有已结算佣金
 */
export const getSettledCommissions = () => {
  return request({
    url: '/api/commission/settled',
    method: 'GET'
  })
}

/**
 * 【管理员】根据结算批次号查询佣金记录
 */
export const getCommissionsByBatch = (settlementBatch) => {
  return request({
    url: `/api/commission/batch/${settlementBatch}`,
    method: 'GET'
  })
}

/**
 * 【管理员】手动触发佣金结算
 */
export const settleCommissions = () => {
  return request({
    url: '/api/commission/settle',
    method: 'POST'
  })
}

/**
 * 查询团长的佣金记录
 */
export const getLeaderCommissions = (leaderId) => {
  return request({
    url: '/api/commission/my',
    method: 'GET',
    params: { leaderId }
  })
}

/**
 * 查询团长的佣金统计
 */
export const getLeaderCommissionSummary = (leaderId) => {
  return request({
    url: '/api/commission/my/summary',
    method: 'GET',
    params: { leaderId }
  })
}

