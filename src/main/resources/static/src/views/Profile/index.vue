<template>
  <div class="profile">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
      <p class="page-description">管理个人信息和系统设置</p>
    </div>

    <Row :gutter="24">
      <Col :span="8">
        <Card title="个人信息" class="profile-card">
          <div class="profile-info">
            <div class="avatar-section">
              <Avatar :size="80" :src="userInfo.avatar" icon="ios-person" />
              <div class="avatar-actions">
                <Button size="small" type="primary">更换头像</Button>
              </div>
            </div>
            
            <div class="info-section">
              <div class="info-item">
                <label>用户名：</label>
                <span>{{ userInfo.username }}</span>
              </div>
              <div class="info-item">
                <label>邮箱：</label>
                <span>{{ userInfo.email || '未设置' }}</span>
              </div>
              <div class="info-item">
                <label>角色：</label>
                <Tag color="primary">{{ userInfo.role || '管理员' }}</Tag>
              </div>
              <div class="info-item">
                <label>创建时间：</label>
                <span>{{ formatDateTime(userInfo.createTime) }}</span>
              </div>
            </div>
          </div>
        </Card>
      </Col>
      
      <Col :span="16">
        <Card title="修改密码" class="password-card">
          <Form
            ref="passwordForm"
            :model="passwordForm"
            :rules="passwordRules"
            label-position="top"
          >
            <FormItem label="当前密码" prop="oldPassword">
              <Input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
              />
            </FormItem>
            
            <FormItem label="新密码" prop="newPassword">
              <Input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码"
              />
            </FormItem>
            
            <FormItem label="确认新密码" prop="confirmPassword">
              <Input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
              />
            </FormItem>
            
            <FormItem>
              <Button type="primary" :loading="passwordLoading" @click="handleChangePassword">
                修改密码
              </Button>
            </FormItem>
          </Form>
        </Card>
        
        <Card title="系统设置" class="settings-card">
          <div class="settings-list">
            <div class="setting-item">
              <div class="setting-info">
                <h4>邮件通知</h4>
                <p>接收系统警报和任务状态变化通知</p>
              </div>
              <Switch v-model="settings.emailNotification" />
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <h4>桌面通知</h4>
                <p>在浏览器中显示桌面通知</p>
              </div>
              <Switch v-model="settings.desktopNotification" />
            </div>
            
            <div class="setting-item">
              <div class="setting-info">
                <h4>深色模式</h4>
                <p>启用深色主题界面</p>
              </div>
              <Switch v-model="settings.darkMode" />
            </div>
          </div>
          
          <div class="settings-actions">
            <Button type="primary" @click="handleSaveSettings">保存设置</Button>
          </div>
        </Card>
      </Col>
    </Row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { Message } from 'view-ui-plus'
import dayjs from 'dayjs'

const authStore = useAuthStore()

// 响应式数据
const passwordForm = ref(null)
const passwordLoading = ref(false)

// 用户信息
const userInfo = reactive({
  username: '',
  email: '',
  role: '',
  avatar: '',
  createTime: null
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 密码表单验证规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度在6到100个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 系统设置
const settings = reactive({
  emailNotification: false,
  desktopNotification: false,
  darkMode: false
})

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '未知'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 处理修改密码
const handleChangePassword = async () => {
  try {
    const valid = await passwordForm.value.validate()
    if (!valid) return
    
    passwordLoading.value = true
    
    // TODO: 调用修改密码API
    // const response = await authApi.changePassword({
    //   oldPassword: passwordForm.oldPassword,
    //   newPassword: passwordForm.newPassword
    // })
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    Message.success('密码修改成功')
    
    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } catch (error) {
    console.error('Change password error:', error)
    Message.error('密码修改失败，请检查当前密码是否正确')
  } finally {
    passwordLoading.value = false
  }
}

// 保存设置
const handleSaveSettings = () => {
  // TODO: 保存设置到后端
  Message.success('设置保存成功')
}

// 初始化用户信息
const initUserInfo = () => {
  const currentUser = authStore.currentUser
  if (currentUser) {
    Object.assign(userInfo, {
      username: currentUser.username || 'admin',
      email: currentUser.email || '',
      role: currentUser.role || '管理员',
      avatar: currentUser.avatar || '',
      createTime: currentUser.createTime || new Date().toISOString()
    })
  }
}

// 初始化设置
const initSettings = () => {
  // TODO: 从后端获取用户设置
  const savedSettings = localStorage.getItem('user_settings')
  if (savedSettings) {
    try {
      const parsed = JSON.parse(savedSettings)
      Object.assign(settings, parsed)
    } catch (error) {
      console.error('Failed to parse settings:', error)
    }
  }
}

// 保存设置到本地存储
const saveSettingsToLocal = () => {
  localStorage.setItem('user_settings', JSON.stringify(settings))
}

// 监听设置变化
watch(
  () => settings,
  () => {
    saveSettingsToLocal()
  },
  { deep: true }
)

// 初始化
onMounted(() => {
  initUserInfo()
  initSettings()
})
</script>

<style lang="scss" scoped>
.profile {
  .profile-card {
    margin-bottom: 24px;
    
    .profile-info {
      .avatar-section {
        text-align: center;
        margin-bottom: 24px;
        
        .avatar-actions {
          margin-top: 12px;
        }
      }
      
      .info-section {
        .info-item {
          display: flex;
          align-items: center;
          margin-bottom: 12px;
          
          label {
            min-width: 80px;
            font-weight: 500;
            color: #6b7280;
          }
          
          span {
            color: #374151;
          }
        }
      }
    }
  }
  
  .password-card {
    margin-bottom: 24px;
  }
  
  .settings-card {
    .settings-list {
      .setting-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .setting-info {
          h4 {
            margin: 0 0 4px 0;
            font-size: 14px;
            font-weight: 500;
            color: #374151;
          }
          
          p {
            margin: 0;
            font-size: 12px;
            color: #9ca3af;
          }
        }
      }
    }
    
    .settings-actions {
      margin-top: 24px;
      text-align: center;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .profile {
    :deep(.ivu-col) {
      margin-bottom: 16px;
    }
    
    .settings-card {
      .settings-list {
        .setting-item {
          flex-direction: column;
          align-items: flex-start;
          gap: 8px;
        }
      }
    }
  }
}
</style>