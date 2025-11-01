import request from '../utils/request'

/**
 * 拼团服务 API
 * 版本: v1.0.0
 * 对应后端: GroupBuyService
 * API文档: docs/社区团购系统/二级文档（参考）/API_GroupBuyService.md
 */

// ==================== 拼团活动管理 API ====================

/**
 * 获取所有拼团活动列表
 */
export const getActivities = () => {
  return request({
    url: '/api/groupbuy/activities',
    method: 'GET'
  })
}

/**
 * 获取进行中的拼团活动
 * 说明：返回 status=1 且在时间范围内的活动
 */
export const getOngoingActivities = () => {
  return request({
    url: '/api/groupbuy/activities/ongoing',
    method: 'GET'
  })
}

/**
 * 获取进行中的拼团活动（包含商品信息）
 * 说明：返回 status=1 且在时间范围内的活动，包含商品名称、图片等详细信息
 * 用于团长发起拼团页面显示商品详情
 */
export const getOngoingActivitiesWithProduct = () => {
  return request({
    url: '/api/groupbuy/activities/ongoing-with-product',
    method: 'GET'
  })
}

/**
 * 获取拼团活动详情
 * @param {number} activityId - 活动ID
 */
export const getActivityDetail = (activityId) => {
  return request({
    url: `/api/groupbuy/activity/${activityId}`,
    method: 'GET'
  })
}

/**
 * 创建拼团活动（管理员）
 * @param {object} data - { productId, groupPrice, requiredNum, maxNum, startTime, endTime }
 */
export const createActivity = (data) => {
  return request({
    url: '/api/groupbuy/activity',
    method: 'POST',
    data
  })
}

/**
 * 更新拼团活动（管理员）
 * @param {number} activityId - 活动ID
 * @param {object} data - 活动数据
 */
export const updateActivity = (activityId, data) => {
  return request({
    url: `/api/groupbuy/activity/${activityId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除拼团活动（管理员）
 * @param {number} activityId - 活动ID
 */
export const deleteActivity = (activityId) => {
  return request({
    url: `/api/groupbuy/activity/${activityId}`,
    method: 'DELETE'
  })
}

// ==================== 团管理 API ====================

/**
 * 团长发起拼团（⭐v3.0核心接口）
 * 权限：仅团长可调用（role=2）
 * 说明：
 * - 仅团长可发起拼团
 * - 自动关联团长的社区
 * - 团长可选择是否立即参与
 * 
 * @param {object} data - 发起拼团请求
 * @param {number} data.activityId - 活动ID（必填）
 * @param {boolean} data.joinImmediately - 是否立即参与（可选，默认false）
 * @param {number} data.addressId - 收货地址ID（joinImmediately=true时必填）
 * @param {number} data.quantity - 购买数量（可选，默认1）
 */
export const launchTeam = (data) => {
  return request({
    url: '/api/groupbuy/team/launch',
    method: 'POST',
    data
  })
}

/**
 * 用户参与拼团（⭐核心接口）
 * 权限：已登录用户
 * 说明：
 * - 行锁检查团状态
 * - 防重复参团
 * - Feign调用OrderService创建订单
 * 
 * @param {object} data - 参团请求
 * @param {number} data.teamId - 团ID（必填）
 * @param {number} data.addressId - 收货地址ID（必填）
 * @param {number} data.quantity - 购买数量（必填，最小为1）
 */
export const joinTeam = (data) => {
  return request({
    url: '/api/groupbuy/team/join',
    method: 'POST',
    data
  })
}

/**
 * 获取团详情
 * 权限：无需登录
 * @param {number} teamId - 团ID
 */
export const getTeamDetail = (teamId) => {
  return request({
    url: `/api/groupbuy/team/${teamId}/detail`,
    method: 'GET'
  })
}

/**
 * 获取活动的团列表（⭐社区优先排序）
 * 权限：无需登录
 * 说明：
 * - v3.0社区优先推荐
 * - 优先显示本社区的团
 * - SQL ORDER BY CASE实现
 * 
 * @param {number} activityId - 活动ID
 * @param {object} params - 查询参数
 * @param {number} params.communityId - 用户的社区ID（可选，用于社区优先排序）
 */
export const getActivityTeams = (activityId, params) => {
  return request({
    url: `/api/groupbuy/activity/${activityId}/teams`,
    method: 'GET',
    params
  })
}

/**
 * 退出拼团
 * 权限：已登录用户
 * 说明：成团前可退，自动退款
 * @param {number} teamId - 团ID
 */
export const quitTeam = (teamId) => {
  return request({
    url: '/api/groupbuy/team/quit',
    method: 'POST',
    params: { teamId }
  })
}

/**
 * 支付回调（内部接口）
 * 权限：无需Token
 * 说明：由PaymentService支付成功后回调
 * @param {number} orderId - 订单ID
 */
export const paymentCallback = (orderId) => {
  return request({
    url: '/api/groupbuy/payment/callback',
    method: 'POST',
    params: { orderId }
  })
}

/**
 * 获取我的拼团记录
 * 权限：需要登录
 * 说明：查询用户参与的所有拼团，按参团时间倒序
 * @returns {Promise<Array>} 拼团记录列表
 */
export const getMyTeams = () => {
  return request({
    url: '/api/groupbuy/teams/my',
    method: 'GET'
  })
}

/**
 * 获取团长发起的团列表
 * 权限：需要团长权限
 * 说明：查询团长发起的所有拼团
 * @param {object} params - 查询参数
 * @param {number} params.leaderId - 团长ID
 * @param {number} params.status - 团状态 (0-拼团中, 1-已成团, 2-已失败)
 * @param {number} params.page - 页码
 * @param {number} params.limit - 每页数量
 * @returns {Promise<Object>} { list, total }
 */
export const getLeaderTeams = (params) => {
  return request({
    url: '/api/groupbuy/teams/leader',
    method: 'GET',
    params
  })
}

/**
 * 获取拼团活动列表（带筛选）
 * 权限：无需登录
 * @param {object} params - 查询参数
 * @param {number} params.status - 活动状态 (0-未开始, 1-进行中, 2-已结束)
 * @param {number} params.page - 页码
 * @param {number} params.limit - 每页数量
 * @returns {Promise<Array>} 活动列表
 */
export const getGroupBuyActivities = (params) => {
  return request({
    url: '/api/groupbuy/activities',
    method: 'GET',
    params
  })
}

/**
 * 根据商品ID获取拼团活动（包含团列表）⭐商品详情页专用
 * 权限：无需登录
 * 说明：
 * - 返回该商品的所有进行中拼团活动
 * - 每个活动包含进行中的团列表（最多10个）
 * - 支持社区优先排序
 * 
 * @param {number} productId - 商品ID
 * @param {object} params - 查询参数
 * @param {number} params.communityId - 用户社区ID（可选，用于社区优先排序）
 * @returns {Promise<Array>} 活动列表（每个活动包含teams字段）
 * 
 * @example
 * // 返回数据结构
 * [
 *   {
 *     activityId: 1,
 *     productId: 101,
 *     groupPrice: 29.9,
 *     requiredNum: 3,
 *     teams: [
 *       {
 *         teamId: 5001,
 *         teamNo: "T20251101001",
 *         leaderId: 2001,
 *         leaderName: "张团长",
 *         communityId: 3001,
 *         communityName: "阳光小区",
 *         currentNum: 2,
 *         requiredNum: 3,
 *         teamStatus: 0,
 *         expireTime: "2025-11-02T12:00:00"
 *       }
 *     ]
 *   }
 * ]
 */
export const getProductGroupBuyActivities = (productId, params) => {
  return request({
    url: `/api/groupbuy/product/${productId}/activities`,
    method: 'GET',
    params
  })
}

