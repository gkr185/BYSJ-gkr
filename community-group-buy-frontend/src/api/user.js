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

