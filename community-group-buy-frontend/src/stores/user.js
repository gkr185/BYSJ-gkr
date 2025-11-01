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

  // 是否已登录
  const isLogin = ref(!!token.value)
  
  // 是否为团长（role=2）
  const isLeader = computed(() => userInfo.value?.role === 2)

  /**
   * 登录
   */
  const login = async (loginForm) => {
    try {
      const data = await loginApi(loginForm)
      
      token.value = data.token
      userInfo.value = data.userInfo
      isLogin.value = true
      
      // 保存到localStorage
      localStorage.setItem('user_token', data.token)
      localStorage.setItem('user_info', JSON.stringify(data.userInfo))
      
      return data
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
    isLogin.value = false
    
    localStorage.removeItem('user_token')
    localStorage.removeItem('user_info')
  }

  /**
   * 更新用户信息
   */
  const updateUserInfo = async () => {
    if (!userInfo.value?.userId) return
    
    try {
      const data = await getUserInfoApi(userInfo.value.userId)
      userInfo.value = data
      localStorage.setItem('user_info', JSON.stringify(data))
    } catch (error) {
      console.error('Update user info failed:', error)
    }
  }

  return {
    token,
    userInfo,
    isLogin,
    isLeader,
    login,
    logout,
    updateUserInfo
  }
})

