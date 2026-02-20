<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onMounted(async () => {
  // 应用启动时检查认证状态
  await authStore.checkAuthStatus()
})
</script>

<style lang="scss">
// 全局样式
* {
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  background-color: #f5f7fa;
}

#app {
  min-height: 100vh;
}

// 滚动条样式
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

// 自定义类
.page-container {
  padding: 20px;
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
}

.page-header {
  margin-bottom: 20px;
  
  .page-title {
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 8px;
  }
  
  .page-description {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
  }
}

// 卡片样式增强
.ivu-card {
  border-radius: 8px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06);
  
  .ivu-card-head {
    border-bottom: 1px solid #f0f0f0;
    background: #fafafa;
    
    .ivu-card-head-title {
      font-weight: 600;
      font-size: 16px;
      color: #1f2937;
    }
  }
}

// 表格样式增强
.ivu-table {
  .ivu-table-header {
    th {
      background-color: #fafafa;
      font-weight: 600;
      color: #374151;
    }
  }
  
  .ivu-table-body {
    td {
      border-bottom: 1px solid #f0f0f0;
    }
  }
}

// 状态标签样式增强
.status-tag {
  &.success {
    background-color: #f0f9ff;
    color: #0ea5e9;
    border: 1px solid #bae6fd;
  }
  
  &.error {
    background-color: #fef2f2;
    color: #ef4444;
    border: 1px solid #fecaca;
  }
  
  &.warning {
    background-color: #fffbeb;
    color: #f59e0b;
    border: 1px solid #fed7aa;
  }
  
  &.running {
    background-color: #f0fdf4;
    color: #16a34a;
    border: 1px solid #bbf7d0;
  }
  
  &.paused {
    background-color: #f9fafb;
    color: #6b7280;
    border: 1px solid #e5e7eb;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .page-container {
    padding: 12px;
  }
  
  .ivu-card {
    margin: 0 -8px;
    border-radius: 0;
  }
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-right-enter-active {
  transition: all 0.3s ease;
}

.slide-right-enter-from {
  transform: translateX(100%);
  opacity: 0;
}
</style>