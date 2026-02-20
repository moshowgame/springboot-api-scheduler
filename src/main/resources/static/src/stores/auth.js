import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { storage } from '@/utils/storage'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
    isAuthenticated: false,
    permissions: []
  }),

  getters: {
    currentUser: (state) => state.user,
    isLoggedIn: (state) => state.isAuthenticated,
    userPermissions: (state) => state.permissions
  },

  actions: {
    // 登录
    async login(credentials) {
      try {
        const response = await authApi.login(credentials)
        
        if (response.success) {
          const { user, token } = response.data
          
          // 保存用户信息和token
          this.user = user
          this.token = token
          this.isAuthenticated = true
          
          // 保存到本地存储
          storage.set('token', token)
          storage.set('user', user)
          
          // 设置API请求头
          this.setAuthHeader(token)
          
          return { success: true, data: user }
        } else {
          return { success: false, message: response.message || '登录失败' }
        }
      } catch (error) {
        console.error('Login error:', error)
        return { 
          success: false, 
          message: error.response?.data?.message || '登录失败，请稍后重试' 
        }
      }
    },

    // 退出登录
    async logout() {
      try {
        await authApi.logout()
      } catch (error) {
        console.error('Logout error:', error)
      } finally {
        this.clearUserData()
      }
    },

    // 检查认证状态
    async checkAuthStatus() {
      // 首先检查本地存储
      const token = storage.get('token')
      const user = storage.get('user')
      
      if (!token || !user) {
        this.clearUserData()
        return false
      }
      
      // 恢复状态
      this.token = token
      this.user = user
      this.isAuthenticated = true
      this.setAuthHeader(token)
      
      try {
        // 验证token有效性
        const response = await authApi.check()
        if (response.success) {
          return true
        } else {
          this.clearUserData()
          return false
        }
      } catch (error) {
        console.error('Auth check error:', error)
        this.clearUserData()
        return false
      }
    },

    // 更新用户信息
    updateUserInfo(userInfo) {
      this.user = { ...this.user, ...userInfo }
      storage.set('user', this.user)
    },

    // 设置认证头
    setAuthHeader(token) {
      if (typeof window !== 'undefined') {
        window.axios = window.axios || require('axios')
        window.axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
      }
    },

    // 清除用户数据
    clearUserData() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      this.permissions = []
      
      // 清除本地存储
      storage.remove('token')
      storage.remove('user')
      
      // 清除API请求头
      if (typeof window !== 'undefined' && window.axios) {
        delete window.axios.defaults.headers.common['Authorization']
      }
    },

    // 刷新token
    async refreshToken() {
      try {
        const response = await authApi.refreshToken()
        
        if (response.success) {
          const { token } = response.data
          this.token = token
          storage.set('token', token)
          this.setAuthHeader(token)
          return true
        } else {
          this.clearUserData()
          return false
        }
      } catch (error) {
        console.error('Token refresh error:', error)
        this.clearUserData()
        return false
      }
    }
  }
})