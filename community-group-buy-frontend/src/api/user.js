import request from '../utils/request'

/**
 * 用户登录
 */
export const login = (data) => {
  return request({
    url: '/api/user/login',
    method: 'POST',
    data
  })
}

/**
 * 用户注册
 */
export const register = (data) => {
  return request({
    url: '/api/user/register',
    method: 'POST',
    data
  })
}

/**
 * 获取当前用户信息
 */
export const getUserInfo = (userId) => {
  return request({
    url: `/api/user/info/${userId}`,
    method: 'GET'
  })
}

/**
 * 更新用户信息
 */
export const updateUserInfo = (userId, data) => {
  return request({
    url: `/api/user/update/${userId}`,
    method: 'PUT',
    data
  })
}

/**
 * 获取用户地址列表
 */
export const getAddressList = (userId) => {
  return request({
    url: `/api/address/list/${userId}`,
    method: 'GET'
  })
}

/**
 * 获取用户地址列表（别名，兼容性）
 */
export const getUserAddressList = getAddressList

/**
 * 添加收货地址
 */
export const addAddress = (data) => {
  return request({
    url: `/api/address/add/${data.userId}`,
    method: 'POST',
    data
  })
}

/**
 * 更新收货地址
 */
export const updateAddress = (addressId, data) => {
  return request({
    url: `/api/address/update/${data.userId}/${addressId}`,
    method: 'PUT',
    data
  })
}

/**
 * 删除收货地址
 */
export const deleteAddress = (userId, addressId) => {
  return request({
    url: `/api/address/delete/${userId}/${addressId}`,
    method: 'DELETE'
  })
}

/**
 * 设置默认地址
 */
export const setDefaultAddress = (userId, addressId) => {
  return request({
    url: `/api/address/default/${userId}/${addressId}`,
    method: 'PUT'
  })
}

/**
 * 获取账户信息
 */
export const getAccountInfo = (userId) => {
  return request({
    url: `/api/account/${userId}`,
    method: 'GET'
  })
}

/**
 * 提交意见反馈
 */
export const submitFeedback = (data) => {
  return request({
    url: `/api/feedback/submit/${data.userId}`,
    method: 'POST',
    data
  })
}

/**
 * 获取我的反馈列表
 */
export const getMyFeedback = (userId) => {
  return request({
    url: `/api/feedback/user/${userId}`,
    method: 'GET'
  })
}

/**
 * 分页查询用户反馈
 * @param {number} userId - 用户ID
 * @param {object} params - 查询参数 { page, size }
 */
export const getMyFeedbackPage = (userId, params) => {
  return request({
    url: `/api/feedback/user/${userId}/page`,
    method: 'GET',
    params
  })
}

/**
 * 获取反馈详情
 * @param {number} feedbackId - 反馈ID
 */
export const getFeedbackDetail = (feedbackId) => {
  return request({
    url: `/api/feedback/${feedbackId}`,
    method: 'GET'
  })
}

// ==================== 账户操作 ====================

/**
 * 账户充值
 * @param {number} userId - 用户ID
 * @param {number} amount - 充值金额
 */
export const recharge = (userId, amount) => {
  return request({
    url: `/api/account/recharge/${userId}`,
    method: 'POST',
    params: { amount }
  })
}

/**
 * 账户扣款
 * @param {number} userId - 用户ID
 * @param {number} amount - 扣款金额
 */
export const deduct = (userId, amount) => {
  return request({
    url: `/api/account/deduct/${userId}`,
    method: 'POST',
    params: { amount }
  })
}

/**
 * 冻结金额
 * @param {number} userId - 用户ID
 * @param {number} amount - 冻结金额
 */
export const freezeAmount = (userId, amount) => {
  return request({
    url: `/api/account/freeze/${userId}`,
    method: 'POST',
    params: { amount }
  })
}

/**
 * 解冻金额
 * @param {number} userId - 用户ID
 * @param {number} amount - 解冻金额
 */
export const unfreezeAmount = (userId, amount) => {
  return request({
    url: `/api/account/unfreeze/${userId}`,
    method: 'POST',
    params: { amount }
  })
}

// ==================== 管理员功能 ====================

/**
 * 搜索用户（管理员）
 * @param {string} keyword - 搜索关键词
 */
export const searchUsers = (keyword) => {
  return request({
    url: '/api/user/search',
    method: 'GET',
    params: { keyword }
  })
}

/**
 * 按角色查询用户（管理员）
 * @param {number} role - 角色（1-普通用户；2-团长；3-管理员）
 */
export const getUsersByRole = (role) => {
  return request({
    url: `/api/user/role/${role}`,
    method: 'GET'
  })
}

/**
 * 更改用户角色（管理员）
 * @param {number} userId - 用户ID
 * @param {number} role - 新角色（1-普通用户；2-团长；3-管理员）
 */
export const updateUserRole = (userId, role) => {
  return request({
    url: `/api/user/role/${userId}`,
    method: 'PUT',
    params: { role }
  })
}

/**
 * 更改用户状态（管理员）
 * @param {number} userId - 用户ID
 * @param {number} status - 状态（0-禁用；1-正常）
 */
export const updateUserStatus = (userId, status) => {
  return request({
    url: `/api/user/status/${userId}`,
    method: 'PUT',
    params: { status }
  })
}

/**
 * 用户统计（管理员）
 */
export const getUserStatistics = () => {
  return request({
    url: '/api/user/statistics',
    method: 'GET'
  })
}

/**
 * 管理员创建用户（管理员）
 * @param {object} data - 用户数据
 */
export const adminCreateUser = (data) => {
  return request({
    url: '/api/user/admin/create',
    method: 'POST',
    data
  })
}

/**
 * 删除用户（管理员）
 * @param {number} userId - 用户ID
 */
export const deleteUser = (userId) => {
  return request({
    url: `/api/user/delete/${userId}`,
    method: 'DELETE'
  })
}

/**
 * 管理员回复反馈（管理员）
 * @param {object} data - { feedbackId, reply, status }
 */
export const replyFeedback = (data) => {
  return request({
    url: '/api/feedback/reply',
    method: 'POST',
    data
  })
}

/**
 * 查询所有反馈（管理员）
 * @param {object} params - 查询参数 { page, size }
 */
export const getAllFeedback = (params) => {
  return request({
    url: '/api/feedback/all',
    method: 'GET',
    params
  })
}

/**
 * 按状态查询反馈（管理员）
 * @param {number} status - 状态（0-待处理；1-处理中；2-已解决；3-已关闭）
 * @param {object} params - 查询参数 { page, size }
 */
export const getFeedbackByStatus = (status, params) => {
  return request({
    url: `/api/feedback/status/${status}`,
    method: 'GET',
    params
  })
}

/**
 * 删除反馈（管理员）
 * @param {number} feedbackId - 反馈ID
 */
export const deleteFeedback = (feedbackId) => {
  return request({
    url: `/api/feedback/delete/${feedbackId}`,
    method: 'DELETE'
  })
}

