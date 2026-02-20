import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 路由组件懒加载
const Layout = () => import('@/views/Layout/index.vue')
const Login = () => import('@/views/Login/index.vue')
const Dashboard = () => import('@/views/Dashboard/index.vue')
const TaskManagement = () => import('@/views/TaskManagement/index.vue')
const ResponseMonitoring = () => import('@/views/ResponseMonitoring/index.vue')
const AlertManagement = () => import('@/views/AlertManagement/index.vue')
const AssertionConfig = () => import('@/views/AssertionConfig/index.vue')
const ApiTest = () => import('@/views/ApiTest/index.vue')
const Profile = () => import('@/views/Profile/index.vue')
const NotFound = () => import('@/views/NotFound/index.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '用户登录',
      requiresAuth: false,
      hideInMenu: true
    }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: {
      title: '首页',
      requiresAuth: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: {
          title: '仪表板',
          icon: 'ios-speedometer',
          requiresAuth: true
        }
      },
      {
        path: 'tasks',
        name: 'TaskManagement',
        component: TaskManagement,
        meta: {
          title: '任务管理',
          icon: 'ios-list-box',
          requiresAuth: true
        }
      },
      {
        path: 'monitoring',
        name: 'ResponseMonitoring',
        component: ResponseMonitoring,
        meta: {
          title: '响应监控',
          icon: 'ios-stats',
          requiresAuth: true
        }
      },
      {
        path: 'alerts',
        name: 'AlertManagement',
        component: AlertManagement,
        meta: {
          title: '警报管理',
          icon: 'ios-notifications',
          requiresAuth: true
        }
      },
      {
        path: 'assertions',
        name: 'AssertionConfig',
        component: AssertionConfig,
        meta: {
          title: '断言配置',
          icon: 'ios-checkmark-circle',
          requiresAuth: true
        }
      },
      {
        path: 'test',
        name: 'ApiTest',
        component: ApiTest,
        meta: {
          title: 'API测试',
          icon: 'ios-flask',
          requiresAuth: true
        }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: {
          title: '个人中心',
          icon: 'ios-person',
          requiresAuth: true
        }
      }
    ]
  },
  {
    path: '/404',
    name: 'NotFound',
    component: NotFound,
    meta: {
      title: '页面不存在',
      requiresAuth: false,
      hideInMenu: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - API监控系统` : 'API监控系统'
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const isAuthenticated = await authStore.checkAuthStatus()
    
    if (!isAuthenticated) {
      // 未登录，重定向到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
  }
  
  // 如果已登录访问登录页，重定向到首页
  if (to.path === '/login' && authStore.isAuthenticated) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router