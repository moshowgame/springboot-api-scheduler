// 本地存储工具类
export const storage = {
  // 设置存储项
  set(key, value, expire = null) {
    try {
      const item = {
        value: JSON.stringify(value),
        timestamp: Date.now(),
        expire: expire ? Date.now() + expire * 1000 : null
      }
      localStorage.setItem(key, JSON.stringify(item))
    } catch (error) {
      console.error('Storage set error:', error)
    }
  },

  // 获取存储项
  get(key, defaultValue = null) {
    try {
      const itemStr = localStorage.getItem(key)
      if (!itemStr) return defaultValue

      const item = JSON.parse(itemStr)
      
      // 检查是否过期
      if (item.expire && Date.now() > item.expire) {
        localStorage.removeItem(key)
        return defaultValue
      }
      
      return JSON.parse(item.value)
    } catch (error) {
      console.error('Storage get error:', error)
      return defaultValue
    }
  },

  // 删除存储项
  remove(key) {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      console.error('Storage remove error:', error)
    }
  },

  // 清除所有存储
  clear() {
    try {
      localStorage.clear()
    } catch (error) {
      console.error('Storage clear error:', error)
    }
  },

  // 获取所有键
  keys() {
    try {
      return Object.keys(localStorage)
    } catch (error) {
      console.error('Storage keys error:', error)
      return []
    }
  },

  // 检查键是否存在
  has(key) {
    try {
      return localStorage.getItem(key) !== null
    } catch (error) {
      console.error('Storage has error:', error)
      return false
    }
  }
}

// SessionStorage 工具类
export const sessionStorage = {
  set(key, value) {
    try {
      sessionStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error('SessionStorage set error:', error)
    }
  },

  get(key, defaultValue = null) {
    try {
      const itemStr = sessionStorage.getItem(key)
      if (!itemStr) return defaultValue
      return JSON.parse(itemStr)
    } catch (error) {
      console.error('SessionStorage get error:', error)
      return defaultValue
    }
  },

  remove(key) {
    try {
      sessionStorage.removeItem(key)
    } catch (error) {
      console.error('SessionStorage remove error:', error)
    }
  },

  clear() {
    try {
      sessionStorage.clear()
    } catch (error) {
      console.error('SessionStorage clear error:', error)
    }
  },

  has(key) {
    try {
      return sessionStorage.getItem(key) !== null
    } catch (error) {
      console.error('SessionStorage has error:', error)
      return false
    }
  }
}