import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getErrorMessage, isSuccess, needLogin, noPermission } from './errorCodes'

// 创建axios实例
const request = axios.create({
  baseURL: import.meta.env.DEV ? '' : 'http://localhost:9000', // 开发环境使用proxy，生产环境使用网关
  timeout: 15000 // 增加超时时间到15秒
})

// 请求拦截器：添加token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('user_token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    ElMessage.error('请求发送失败')
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 后端统一返回格式：{ code, message, data, timestamp }
    // 返回完整的response对象，让业务代码自行判断
    // 这样可以统一处理 res.code === 200 的判断
    
    // 如果是成功响应，直接返回完整response
    if (isSuccess(res.code)) {
      return res
    }
    
    // 业务失败，显示错误消息
    const errorMsg = getErrorMessage(res.code, res.message)
    ElMessage.error(errorMsg)
    
    // 返回Promise.reject，但附带完整的响应数据
    return Promise.reject({
      code: res.code,
      message: errorMsg,
      data: res.data,
      response: res
    })
  },
  error => {
    console.error('响应错误:', error)
    
    // 处理HTTP错误
    if (error.response) {
      const { status, data } = error.response
      
      // 401 未授权 - 需要重新登录
      if (status === 401) {
        // 避免重复弹窗
        if (!window.__loginExpiredShown__) {
          window.__loginExpiredShown__ = true
          ElMessageBox.alert('登录已过期，请重新登录', '提示', {
            confirmButtonText: '确定',
            type: 'warning'
          }).then(() => {
            localStorage.removeItem('user_token')
            localStorage.removeItem('user_info')
            window.location.href = '/login'
          }).finally(() => {
            window.__loginExpiredShown__ = false
          })
        }
      } 
      // 403 无权限
      else if (status === 403) {
        ElMessage.error('您没有权限执行此操作')
      } 
      // 404 资源不存在
      else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } 
      // 500 服务器错误
      else if (status === 500) {
        ElMessage.error(data?.message || '服务器内部错误，请稍后重试')
      }
      // 502 网关错误
      else if (status === 502) {
        ElMessage.error('服务暂时不可用，请稍后重试')
      }
      // 503 服务不可用
      else if (status === 503) {
        ElMessage.error('服务维护中，请稍后重试')
      }
      // 504 网关超时
      else if (status === 504) {
        ElMessage.error('请求超时，请稍后重试')
      }
      // 其他错误
      else {
        const errorMsg = data?.message || `请求失败 (${status})`
        ElMessage.error(errorMsg)
      }
    } 
    // 请求超时
    else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    }
    // 网络错误
    else if (error.message === 'Network Error') {
      ElMessage.error('网络连接失败，请检查网络')
    }
    // 其他错误
    else {
      ElMessage.error(error.message || '未知错误')
    }
    
    return Promise.reject(error)
  }
)

export default request

