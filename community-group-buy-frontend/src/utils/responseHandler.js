/**
 * 统一的API响应处理工具
 */
import { ElMessage } from 'element-plus'
import { getErrorMessage, isSuccess } from './errorCodes'

/**
 * 处理API响应
 * @param {Object} response - API响应对象
 * @param {Object} options - 配置选项
 * @param {boolean} options.showSuccess - 是否显示成功提示，默认false
 * @param {string} options.successMsg - 成功提示消息
 * @param {boolean} options.showError - 是否显示错误提示，默认true
 * @param {Function} options.onSuccess - 成功回调
 * @param {Function} options.onError - 失败回调
 * @returns {boolean} 是否成功
 */
export function handleResponse(response, options = {}) {
  const {
    showSuccess = false,
    successMsg = '操作成功',
    showError = true,
    onSuccess,
    onError
  } = options

  // 检查响应格式
  if (!response || typeof response.code === 'undefined') {
    console.error('无效的响应格式:', response)
    if (showError) {
      ElMessage.error('响应格式错误')
    }
    return false
  }

  // 成功处理
  if (isSuccess(response.code)) {
    if (showSuccess) {
      ElMessage.success(response.message || successMsg)
    }
    if (typeof onSuccess === 'function') {
      onSuccess(response.data)
    }
    return true
  }

  // 失败处理
  const errorMsg = getErrorMessage(response.code, response.message)
  if (showError) {
    ElMessage.error(errorMsg)
  }
  if (typeof onError === 'function') {
    onError(response.code, errorMsg, response.data)
  }
  return false
}

/**
 * 标准化分页数据
 * 后端使用PageHelper返回的数据结构
 * @param {Object} pageData - 分页数据
 * @returns {Object} 标准化后的分页数据
 */
export function normalizePagination(pageData) {
  if (!pageData) {
    return {
      records: [],
      total: 0,
      current: 1,
      size: 10,
      pages: 0
    }
  }

  // PageHelper格式
  if (pageData.list) {
    return {
      records: pageData.list || [],
      total: pageData.total || 0,
      current: pageData.pageNum || 1,
      size: pageData.pageSize || 10,
      pages: pageData.pages || 0
    }
  }

  // MyBatis Plus格式或其他格式
  return {
    records: pageData.records || pageData.data || [],
    total: pageData.total || 0,
    current: pageData.current || pageData.pageNum || 1,
    size: pageData.size || pageData.pageSize || 10,
    pages: pageData.pages || 0
  }
}

/**
 * 安全的数据获取
 * @param {Object} response - API响应
 * @param {any} defaultValue - 默认值
 * @returns {any} 数据或默认值
 */
export function getData(response, defaultValue = null) {
  if (!response || !isSuccess(response.code)) {
    return defaultValue
  }
  return response.data ?? defaultValue
}

/**
 * 检查响应是否成功
 * @param {Object} response - API响应
 * @returns {boolean}
 */
export function checkSuccess(response) {
  return response && isSuccess(response.code)
}

/**
 * 获取错误信息
 * @param {Object} response - API响应
 * @returns {string} 错误信息
 */
export function getError(response) {
  if (!response) return '未知错误'
  return getErrorMessage(response.code, response.message)
}

/**
 * 批量处理API调用
 * @param {Array<Promise>} promises - Promise数组
 * @param {Object} options - 配置选项
 * @returns {Promise<Object>} 处理结果
 */
export async function handleBatch(promises, options = {}) {
  const {
    showSuccess = false,
    successMsg = '操作完成',
    showError = true
  } = options

  try {
    const results = await Promise.allSettled(promises)
    const succeeded = results.filter(r => r.status === 'fulfilled' && checkSuccess(r.value))
    const failed = results.filter(r => r.status === 'rejected' || !checkSuccess(r.value))

    if (failed.length === 0 && showSuccess) {
      ElMessage.success(successMsg)
    } else if (failed.length > 0 && showError) {
      ElMessage.error(`${failed.length} 个操作失败`)
    }

    return {
      total: results.length,
      succeeded: succeeded.length,
      failed: failed.length,
      results
    }
  } catch (error) {
    console.error('批量处理错误:', error)
    if (showError) {
      ElMessage.error('批量操作失败')
    }
    throw error
  }
}

/**
 * 带重试的API调用
 * @param {Function} apiCall - API调用函数
 * @param {Object} options - 配置选项
 * @returns {Promise<Object>} API响应
 */
export async function withRetry(apiCall, options = {}) {
  const {
    maxRetries = 3,
    retryDelay = 1000,
    onRetry
  } = options

  let lastError
  for (let i = 0; i < maxRetries; i++) {
    try {
      const response = await apiCall()
      if (checkSuccess(response)) {
        return response
      }
      lastError = response
    } catch (error) {
      lastError = error
      if (i < maxRetries - 1) {
        if (typeof onRetry === 'function') {
          onRetry(i + 1, maxRetries)
        }
        await new Promise(resolve => setTimeout(resolve, retryDelay * (i + 1)))
      }
    }
  }

  throw lastError
}

export default {
  handleResponse,
  normalizePagination,
  getData,
  checkSuccess,
  getError,
  handleBatch,
  withRetry
}

