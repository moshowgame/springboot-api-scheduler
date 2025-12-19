import axios from 'axios'
import { Message } from 'view-ui-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const authStore = useAuthStore()
    
    // 添加认证token
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    
    // 添加请求时间戳
    config.metadata = { startTime: new Date() }
    
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const config = response.config
    const duration = new Date() - config.metadata.startTime
    
    // 记录请求耗时（开发环境）
    if (import.meta.env.DEV) {
      console.log(`API ${config.method?.toUpperCase()} ${config.url} - ${duration}ms`)
    }
    
    const res = response.data
    
    // 根据后端API规范处理响应
    if (res.success !== undefined) {
      if (res.success) {
        return res
      } else {
        Message.error({
          content: res.message || '请求失败',
          duration: 3
        })
        return Promise.reject(new Error(res.message || '请求失败'))
      }
    } else {
      // 兼容其他响应格式
      return res
    }
  },
  async error => {
    const { response } = error
    
    if (response) {
      const status = response.status
      const authStore = useAuthStore()
      
      switch (status) {
        case 401:
          // 未授权，清除认证信息并重定向到登录页
          Message.error({
            content: '登录已过期，请重新登录',
            duration: 3
          })
          authStore.clearUserData()
          router.push('/login')
          break
          
        case 403:
          Message.error({
            content: '没有权限访问该资源',
            duration: 3
          })
          break
          
        case 404:
          Message.error({
            content: '请求的资源不存在',
            duration: 3
          })
          break
          
        case 500:
          Message.error({
            content: '服务器内部错误',
            duration: 3
          })
          break
          
        default:
          const errorMessage = response.data?.message || '网络错误，请稍后重试'
          Message.error({
            content: errorMessage,
            duration: 3
          })
      }
    } else if (error.code === 'ECONNABORTED') {
      Message.error({
        content: '请求超时，请稍后重试',
        duration: 3
      })
    } else {
      Message.error({
        content: '网络连接失败，请检查网络设置',
        duration: 3
      })
    }
    
    return Promise.reject(error)
  }
)

export default service