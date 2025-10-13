import request from '../utils/request'

/**
 * 查询所有反馈（管理员）
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
 */
export const getFeedbackByStatus = (status, params) => {
  return request({
    url: `/api/feedback/status/${status}`,
    method: 'GET',
    params
  })
}

/**
 * 获取反馈详情
 */
export const getFeedbackDetail = (feedbackId) => {
  return request({
    url: `/api/feedback/${feedbackId}`,
    method: 'GET'
  })
}

/**
 * 管理员回复反馈
 */
export const replyFeedback = (data) => {
  return request({
    url: '/api/feedback/reply',
    method: 'POST',
    data
  })
}

