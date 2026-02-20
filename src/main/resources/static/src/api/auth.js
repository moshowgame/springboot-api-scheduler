import request from '@/utils/request'

// 认证相关API
export const authApi = {
  // 用户登录
  login: (credentials) => {
    return request({
      url: '/auth/login',
      method: 'post',
      data: credentials
    })
  },

  // 用户退出
  logout: () => {
    return request({
      url: '/auth/logout',
      method: 'post'
    })
  },

  // 检查认证状态
  check: () => {
    return request({
      url: '/auth/check',
      method: 'get'
    })
  },

  // 刷新token
  refreshToken: () => {
    return request({
      url: '/auth/refresh',
      method: 'post'
    })
  },

  // 获取用户信息
  getUserInfo: () => {
    return request({
      url: '/auth/user',
      method: 'get'
    })
  },

  // 更新用户信息
  updateUserInfo: (data) => {
    return request({
      url: '/auth/user',
      method: 'put',
      data
    })
  },

  // 修改密码
  changePassword: (data) => {
    return request({
      url: '/auth/password',
      method: 'put',
      data
    })
  }
}