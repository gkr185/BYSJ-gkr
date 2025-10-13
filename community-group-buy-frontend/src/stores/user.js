import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getUserInfo as getUserInfoApi } from '../api/user'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('user_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('user_info') || 'null'))

  // 是否已登录
  const isLogin = ref(!!token.value)

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
    login,
    logout,
    updateUserInfo
  }
})

