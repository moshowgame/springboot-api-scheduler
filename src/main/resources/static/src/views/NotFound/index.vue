<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1 class="error-title">页面不存在</h1>
      <p class="error-description">
        抱歉，您访问的页面不存在或已被移除。
      </p>
      
      <div class="error-actions">
        <Button type="primary" @click="goHome">
          <Icon type="ios-home" />
          返回首页
        </Button>
        
        <Button @click="goBack">
          <Icon type="ios-arrow-back" />
          返回上页
        </Button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style lang="scss" scoped>
.not-found {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
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

.not-found-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 0 20px;
  
  .error-code {
    font-size: 120px;
    font-weight: 700;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 20px;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
  }
  
  .error-title {
    font-size: 32px;
    font-weight: 600;
    color: rgba(255, 255, 255, 0.9);
    margin-bottom: 16px;
    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
  }
  
  .error-description {
    font-size: 16px;
    color: rgba(255, 255, 255, 0.8);
    margin-bottom: 32px;
    max-width: 400px;
    margin-left: auto;
    margin-right: auto;
  }
  
  .error-actions {
    display: flex;
    gap: 16px;
    justify-content: center;
    
    :deep(.ivu-btn) {
      height: 44px;
      padding: 0 24px;
      font-size: 16px;
      border-radius: 8px;
      transition: transform 0.3s ease, box-shadow 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 25px rgba(0, 0, 0, 0.2);
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .not-found-content {
    .error-code {
      font-size: 80px;
    }
    
    .error-title {
      font-size: 24px;
    }
    
    .error-description {
      font-size: 14px;
    }
    
    .error-actions {
      flex-direction: column;
      align-items: center;
      
      :deep(.ivu-btn) {
        width: 200px;
      }
    }
  }
}

// 动画效果
.not-found-content {
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>