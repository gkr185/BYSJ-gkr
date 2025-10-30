import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:9000', // ⭐ API Gateway 统一入口
  timeout: 10000
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
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 后端统一返回格式：{ code, message, data }
    if (res.code === 200) {
      return res.data // 返回data部分
    } else {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
  },
  error => {
    console.error('Response Error:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      if (status === 401) {
        ElMessageBox.alert('登录已过期，请重新登录', '提示', {
          confirmButtonText: '确定'
        }).then(() => {
          localStorage.removeItem('user_token')
          localStorage.removeItem('user_info')
          window.location.href = '/login'
        })
      } else if (status === 403) {
        ElMessage.error('没有权限访问')
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else {
        ElMessage.error(data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

export default request

