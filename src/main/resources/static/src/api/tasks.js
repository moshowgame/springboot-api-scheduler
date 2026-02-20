import request from '@/utils/request'

// API任务管理相关API
export const taskApi = {
  // 获取所有任务
  getAll: () => {
    return request({
      url: '/tasks',
      method: 'get'
    })
  },

  // 获取单个任务
  getById: (id) => {
    return request({
      url: `/tasks/${id}`,
      method: 'get'
    })
  },

  // 创建任务
  create: (data) => {
    return request({
      url: '/tasks',
      method: 'post',
      data
    })
  },

  // 更新任务
  update: (id, data) => {
    return request({
      url: `/tasks/${id}`,
      method: 'put',
      data
    })
  },

  // 删除任务
  delete: (id) => {
    return request({
      url: `/tasks/${id}`,
      method: 'delete'
    })
  },

  // 启动任务
  start: (id) => {
    return request({
      url: `/tasks/${id}/start`,
      method: 'post'
    })
  },

  // 暂停任务
  pause: (id) => {
    return request({
      url: `/tasks/${id}/pause`,
      method: 'post'
    })
  },

  // 立即执行任务
  execute: (id) => {
    return request({
      url: `/tasks/${id}/execute`,
      method: 'post'
    })
  },

  // 批量操作任务
  batchStart: (ids) => {
    return request({
      url: '/tasks/batch/start',
      method: 'post',
      data: { ids }
    })
  },

  batchPause: (ids) => {
    return request({
      url: '/tasks/batch/pause',
      method: 'post',
      data: { ids }
    })
  },

  batchDelete: (ids) => {
    return request({
      url: '/tasks/batch/delete',
      method: 'post',
      data: { ids }
    })
  },

  // 复制任务
  copy: (id) => {
    return request({
      url: `/tasks/${id}/copy`,
      method: 'post'
    })
  },

  // 获取任务统计信息
  getStatistics: () => {
    return request({
      url: '/tasks/statistics',
      method: 'get'
    })
  }
}