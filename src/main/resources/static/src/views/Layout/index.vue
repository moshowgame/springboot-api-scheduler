<template>
  <div class="layout">
    <layout>
      <!-- 侧边栏 -->
      <sider
        ref="sider"
        v-model="collapsed"
        :width="sidebarWidth"
        :collapsed-width="sidebarCollapsedWidth"
        breakpoint="md"
        collapsible
        class="layout-sider"
      >
        <div class="layout-logo">
          <div class="logo-content">
            <Icon type="ios-speedometer" :size="collapsed ? 24 : 32" />
            <span v-show="!collapsed" class="logo-text">API监控系统</span>
          </div>
        </div>
        
        <Menu
          v-model:selectedKeys="selectedKeys"
          v-model:open-keys="openKeys"
          :active-name="activeName"
          theme="dark"
          width="auto"
          @on-select="handleMenuSelect"
        >
          <template v-for="route in menuRoutes" :key="route.name">
            <MenuItem v-if="!route.children || route.children.length === 0" :name="route.name">
              <Icon :type="route.meta.icon" />
              <span>{{ route.meta.title }}</span>
            </MenuItem>
            
            <Submenu v-else :name="route.name">
              <template #title>
                <Icon :type="route.meta.icon" />
                <span>{{ route.meta.title }}</span>
              </template>
              <MenuItem
                v-for="child in route.children"
                :key="child.name"
                :name="child.name"
              >
                <Icon :type="child.meta.icon" />
                <span>{{ child.meta.title }}</span>
              </MenuItem>
            </Submenu>
          </template>
        </Menu>
      </sider>

      <layout>
        <!-- 顶部导航 -->
        <header class="layout-header">
          <div class="header-left">
            <Icon
              :type="collapsed ? 'md-menu' : 'md-menu'"
              :size="24"
              class="collapse-trigger"
              @click="toggleCollapse"
            />
            <Breadcrumb class="header-breadcrumb">
              <BreadcrumbItem v-for="item in breadcrumbs" :key="item.name">
                <Icon :type="item.icon" v-if="item.icon" />
                {{ item.title }}
              </BreadcrumbItem>
            </Breadcrumb>
          </div>
          
          <div class="header-right">
            <!-- 系统状态 -->
            <div class="system-status">
              <Badge :count="alertCount" :offset="[10, 0]">
                <Icon type="ios-notifications" :size="20" />
              </Badge>
            </div>
            
            <!-- 用户菜单 -->
            <Dropdown trigger="click" placement="bottom-end" @on-click="handleUserMenuClick">
              <div class="user-info">
                <Avatar :src="userAvatar" icon="ios-person" size="small" />
                <span class="user-name">{{ currentUser?.username || '用户' }}</span>
                <Icon type="ios-arrow-down" />
              </div>
              
              <template #dropdown>
                <DropdownMenu>
                  <DropdownItem name="profile">
                    <Icon type="ios-person-outline" />
                    个人中心
                  </DropdownItem>
                  <DropdownItem name="settings">
                    <Icon type="ios-settings-outline" />
                    系统设置
                  </DropdownItem>
                  <DropdownItem divided name="logout">
                    <Icon type="ios-log-out" />
                    退出登录
                  </DropdownItem>
                </DropdownMenu>
              </template>
            </Dropdown>
          </div>
        </header>

        <!-- 主内容区 -->
        <Content class="layout-content">
          <div class="content-wrapper">
            <router-view v-slot="{ Component }">
              <transition name="fade" mode="out-in">
                <component :is="Component" />
              </transition>
            </router-view>
          </div>
        </Content>
      </layout>
    </layout>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Modal } from 'view-ui-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const collapsed = ref(false)
const selectedKeys = ref([])
const openKeys = ref([])
const alertCount = ref(0)

// 布局配置
const sidebarWidth = 240
const sidebarCollapsedWidth = 64

// 计算属性
const currentUser = computed(() => authStore.currentUser)
const userAvatar = computed(() => currentUser.value?.avatar || '')

// 获取菜单路由
const menuRoutes = computed(() => {
  return router.options.routes
    .find(route => route.path === '/')
    ?.children.filter(route => !route.meta?.hideInMenu) || []
})

// 获取面包屑
const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  return matched.map(item => ({
    name: item.name,
    title: item.meta.title,
    icon: item.meta.icon
  }))
})

// 获取当前激活的菜单项
const activeName = computed(() => {
  return route.name || 'Dashboard'
})

// 监听路由变化更新选中菜单
watch(
  () => route.name,
  (newName) => {
    selectedKeys.value = [newName]
  },
  { immediate: true }
)

// 切换侧边栏折叠状态
const toggleCollapse = () => {
  collapsed.value = !collapsed.value
}

// 处理菜单选择
const handleMenuSelect = (name) => {
  router.push({ name })
}

// 处理用户菜单点击
const handleUserMenuClick = (name) => {
  switch (name) {
    case 'profile':
      router.push({ name: 'Profile' })
      break
    case 'settings':
      // TODO: 跳转到系统设置页面
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 处理退出登录
const handleLogout = () => {
  Modal.confirm({
    title: '确认退出',
    content: '您确定要退出登录吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await authStore.logout()
      router.push('/login')
    }
  })
}

// 获取系统警报数量
const fetchAlertCount = async () => {
  try {
    // TODO: 调用API获取警报数量
    // alertCount.value = response.data.count
  } catch (error) {
    console.error('Failed to fetch alert count:', error)
  }
}

// 初始化
onMounted(() => {
  fetchAlertCount()
  
  // 设置初始展开的菜单项
  const currentRoute = route
  if (currentRoute.meta && currentRoute.meta.parentName) {
    openKeys.value = [currentRoute.meta.parentName]
  }
})
</script>

<style lang="scss" scoped>
.layout {
  height: 100vh;
  overflow: hidden;
}

.layout-sider {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  overflow-y: auto;
  z-index: 1000;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

.layout-logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  margin-bottom: 16px;
  
  .logo-content {
    display: flex;
    align-items: center;
    gap: 12px;
    color: #fff;
    
    .logo-text {
      font-size: 16px;
      font-weight: 600;
      white-space: nowrap;
    }
  }
}

.layout-header {
  height: 64px;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: fixed;
  top: 0;
  left: 240px;
  right: 0;
  z-index: 999;
  transition: left 0.3s;
  
  &.collapsed {
    left: 64px;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .collapse-trigger {
    cursor: pointer;
    color: #666;
    transition: color 0.3s;
    
    &:hover {
      color: #1890ff;
    }
  }
  
  .header-breadcrumb {
    :deep(.ivu-breadcrumb) {
      font-size: 14px;
      
      .ivu-breadcrumb-item-link {
        color: #666;
        
        &:hover {
          color: #1890ff;
        }
      }
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
  
  .system-status {
    cursor: pointer;
    color: #666;
    transition: color 0.3s;
    
    &:hover {
      color: #1890ff;
    }
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: 4px;
    transition: background-color 0.3s;
    
    &:hover {
      background-color: #f5f5f5;
    }
    
    .user-name {
      font-size: 14px;
      color: #333;
      max-width: 100px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.layout-content {
  margin-left: 240px;
  margin-top: 64px;
  transition: margin-left 0.3s;
  
  &.collapsed {
    margin-left: 64px;
  }
}

.content-wrapper {
  padding: 24px;
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
}

// 响应式设计
@media (max-width: 768px) {
  .layout-header {
    left: 64px;
    
    &.collapsed {
      left: 64px;
    }
  }
  
  .layout-content {
    margin-left: 64px;
    
    &.collapsed {
      margin-left: 64px;
    }
  }
  
  .content-wrapper {
    padding: 16px;
  }
  
  .header-right {
    .user-name {
      display: none;
    }
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
</style>