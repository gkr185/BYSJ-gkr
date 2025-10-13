import { defineStore } from 'pinia'
import { login } from '../api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    userInfo: JSON.parse(localStorage.getItem('admin_info') || '{}'),
    isLoggedIn: !!localStorage.getItem('admin_token')
  }),

  getters: {
    userId: (state) => state.userInfo.userId,
    username: (state) => state.userInfo.username,
    userRole: (state) => state.userInfo.role,
    isAdmin: (state) => state.userInfo.role === 3
  },

  actions: {
    async login(loginForm) {
      try {
        const res = await login(loginForm)
        
        // 验证是否为管理员
        if (res.data.userInfo.role !== 3) {
          throw new Error('只有管理员才能登录此系统')
        }
        
        this.token = res.data.token
        this.userInfo = res.data.userInfo
        this.isLoggedIn = true
        
        localStorage.setItem('admin_token', res.data.token)
        localStorage.setItem('admin_info', JSON.stringify(res.data.userInfo))
        
        return res
      } catch (error) {
        throw error
      }
    },

    logout() {
      this.token = ''
      this.userInfo = {}
      this.isLoggedIn = false
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_info')
    }
  }
})

