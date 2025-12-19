<template>
  <div class="login-container">
    <div class="login-background">
      <div class="background-overlay"></div>
    </div>
    
    <div class="login-content">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <Icon type="ios-speedometer" :size="48" color="#2563eb" />
          </div>
          <h1 class="login-title">API监控系统</h1>
          <p class="login-subtitle">企业级API监控调度解决方案</p>
        </div>
        
        <Form
          ref="loginForm"
          :model="formData"
          :rules="formRules"
          class="login-form"
          @submit.prevent="handleSubmit"
        >
          <FormItem prop="username">
            <Input
              v-model="formData.username"
              type="text"
              size="large"
              placeholder="请输入用户名"
              prefix="ios-person-outline"
              :disabled="loading"
              @on-enter="handleSubmit"
            />
          </FormItem>
          
          <FormItem prop="password">
            <Input
              v-model="formData.password"
              type="password"
              size="large"
              placeholder="请输入密码"
              prefix="ios-lock-outline"
              :disabled="loading"
              @on-enter="handleSubmit"
            />
          </FormItem>
          
          <FormItem>
            <Checkbox v-model="formData.rememberMe">记住我</Checkbox>
          </FormItem>
          
          <FormItem>
            <Button
              type="primary"
              size="large"
              long
              :loading="loading"
              @click="handleSubmit"
            >
              {{ loading ? '登录中...' : '登录' }}
            </Button>
          </FormItem>
        </Form>
        
        <div class="login-footer">
          <p class="copyright">
            © 2024 API监控系统. 保留所有权利.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Message } from 'view-ui-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 响应式数据
const loginForm = ref(null)
const loading = ref(false)

// 表单数据
const formData = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在2到50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在6到100个字符', trigger: 'blur' }
  ]
}

// 处理登录提交
const handleSubmit = async () => {
  if (!loginForm.value) return
  
  try {
    const valid = await loginForm.value.validate()
    if (!valid) return
    
    loading.value = true
    
    const result = await authStore.login({
      username: formData.username,
      password: formData.password,
      rememberMe: formData.rememberMe
    })
    
    if (result.success) {
      Message.success({
        content: '登录成功',
        duration: 2
      })
      
      // 获取重定向地址
      const redirect = route.query.redirect || '/dashboard'
      router.push(redirect)
    } else {
      Message.error({
        content: result.message || '登录失败',
        duration: 3
      })
    }
  } catch (error) {
    console.error('Login error:', error)
    Message.error({
      content: '登录失败，请稍后重试',
      duration: 3
    })
  } finally {
    loading.value = false
  }
}

// 初始化
onMounted(() => {
  // 如果已经登录，重定向到首页
  if (authStore.isAuthenticated) {
    router.push('/dashboard')
  }
  
  // 尝试从本地存储恢复用户名
  const savedUsername = localStorage.getItem('saved_username')
  if (savedUsername) {
    formData.username = savedUsername
    formData.rememberMe = true
  }
})

// 保存用户名到本地存储
const saveUsername = () => {
  if (formData.rememberMe) {
    localStorage.setItem('saved_username', formData.username)
  } else {
    localStorage.removeItem('saved_username')
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 1;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  }
}

.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.2);
}

.login-content {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 400px;
  padding: 0 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.2);
  padding: 48px 40px;
  text-align: center;
}

.login-header {
  margin-bottom: 40px;
  
  .logo {
    margin-bottom: 16px;
  }
  
  .login-title {
    font-size: 28px;
    font-weight: 700;
    color: #1f2937;
    margin: 0 0 8px 0;
  }
  
  .login-subtitle {
    font-size: 14px;
    color: #6b7280;
    margin: 0;
  }
}

.login-form {
  text-align: left;
  margin-bottom: 32px;
  
  :deep(.ivu-form-item) {
    margin-bottom: 24px;
  }
  
  :deep(.ivu-input) {
    height: 48px;
    font-size: 14px;
    
    &::-webkit-input-placeholder {
      color: #9ca3af;
    }
  }
  
  :deep(.ivu-btn) {
    height: 48px;
    font-size: 16px;
    font-weight: 500;
  }
}

.login-footer {
  .copyright {
    font-size: 12px;
    color: #9ca3af;
    margin: 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-content {
    padding: 0 16px;
  }
  
  .login-card {
    padding: 32px 24px;
  }
  
  .login-title {
    font-size: 24px !important;
  }
}

// 动画效果
.login-card {
  animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 表单项动画
:deep(.ivu-form-item) {
  animation: fadeIn 0.8s ease-out;
  animation-fill-mode: both;
  
  &:nth-child(1) { animation-delay: 0.1s; }
  &:nth-child(2) { animation-delay: 0.2s; }
  &:nth-child(3) { animation-delay: 0.3s; }
  &:nth-child(4) { animation-delay: 0.4s; }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>