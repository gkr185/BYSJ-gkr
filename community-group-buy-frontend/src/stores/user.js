import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getUserInfo as getUserInfoApi } from '../api/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('user_token') || '')
  
  // 安全解析 localStorage 中的用户信息
  const getUserInfoFromStorage = () => {
    try {
      const stored = localStorage.getItem('user_info')
      if (!stored || stored === 'undefined' || stored === 'null') {
        return null
      }
      return JSON.parse(stored)
    } catch (error) {
      console.warn('Failed to parse user_info from localStorage:', error)
      localStorage.removeItem('user_info')
      return null
    }
  }
  
  const userInfo = ref(getUserInfoFromStorage())

  // 是否已登录（computed 自动响应 token 和 userInfo 变化）
  const isLogin = computed(() => !!token.value && !!userInfo.value)

  // 调试：打印初始状态
  console.log('🔐 User Store 初始化:', {
    hasToken: !!token.value,
    hasUserInfo: !!userInfo.value,
    isLogin: isLogin.value,
    userId: userInfo.value?.userId
  })
  
  // 是否为团长（role=2）
  const isLeader = computed(() => userInfo.value?.role === 2)
  
  // 用户社区ID（用于社区优先推荐）⭐⭐⭐
  const communityId = computed(() => userInfo.value?.communityId)

  /**
   * 登录
   */
  const login = async (loginForm) => {
    try {
      const res = await loginApi(loginForm)
      
      if (res.code === 200) {
        token.value = res.data.token
        userInfo.value = res.data.userInfo
        // isLogin 是 computed，会自动更新
        
        // 保存到localStorage
        localStorage.setItem('user_token', res.data.token)
        localStorage.setItem('user_info', JSON.stringify(res.data.userInfo))
        
        return res.data
      } else {
        throw new Error(res.message || '登录失败')
      }
    } catch (error) {
      console.error('Login failed:', error)
      throw error
    }
  }

  /**
   * 登出
   */
  const logout = () => {
    token.value = ''
    userInfo.value = null
    // isLogin 是 computed，会自动更新
    
    localStorage.removeItem('user_token')
    localStorage.removeItem('user_info')
  }

  /**
   * 更新用户信息
   */
  const updateUserInfo = async () => {
    if (!userInfo.value?.userId) return
    
    try {
      const res = await getUserInfoApi(userInfo.value.userId)
      if (res.code === 200) {
        userInfo.value = res.data
        localStorage.setItem('user_info', JSON.stringify(res.data))
      }
    } catch (error) {
      console.error('Update user info failed:', error)
    }
  }

  return {
    token,
    userInfo,
    isLogin,
    isLeader,
    communityId, // ⭐ 导出communityId，用于社区优先推荐
    login,
    logout,
    updateUserInfo
  }
})

