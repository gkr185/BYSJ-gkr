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
 * 搜索用户
 */
export const searchUsers = (params) => {
  return request({
    url: '/api/user/search',
    method: 'GET',
    params
  })
}

/**
 * 按角色查询用户
 */
export const getUsersByRole = (role) => {
  return request({
    url: `/api/user/role/${role}`,
    method: 'GET'
  })
}

/**
 * 获取用户信息
 */
export const getUserInfo = (userId) => {
  return request({
    url: `/api/user/info/${userId}`,
    method: 'GET'
  })
}

/**
 * 更改用户状态
 */
export const changeUserStatus = (userId, status) => {
  return request({
    url: `/api/user/status/${userId}`,
    method: 'PUT',
    params: { status }
  })
}

/**
 * 更改用户角色
 */
export const changeUserRole = (userId, role) => {
  return request({
    url: `/api/user/role/${userId}`,
    method: 'PUT',
    params: { role }
  })
}

/**
 * 用户统计
 */
export const getUserStats = () => {
  return request({
    url: '/api/user/statistics',
    method: 'GET'
  })
}

/**
 * 管理员创建用户
 */
export const adminCreateUser = (data) => {
  return request({
    url: '/api/user/admin/create',
    method: 'POST',
    data
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
export const getUserAddresses = (userId) => {
  return request({
    url: `/api/address/list/${userId}`,
    method: 'GET'
  })
}

/**
 * 获取用户账户信息
 */
export const getUserAccount = (userId) => {
  return request({
    url: `/api/account/${userId}`,
    method: 'GET'
  })
}

// ==================== 文件上传 ====================

/**
 * 上传用户头像
 * @param {File} file - 头像文件
 */
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/upload/avatar',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传反馈图片
 * @param {File} file - 图片文件
 */
export const uploadFeedbackImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/upload/feedback',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传通用文件
 * @param {File} file - 文件
 */
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/upload/file',
    method: 'POST',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

