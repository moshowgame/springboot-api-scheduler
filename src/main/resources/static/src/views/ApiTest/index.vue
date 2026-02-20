<template>
  <div class="api-test">
    <div class="page-header">
      <h1 class="page-title">API测试</h1>
      <p class="page-description">测试API接口和验证响应结果</p>
    </div>

    <Card>
      <div class="coming-soon">
        <Icon type="ios-time" :size="64" color="#9ca3af" />
        <h2>功能开发中</h2>
        <p>API测试功能正在开发中，敬请期待...</p>
      </div>
    </Card>
  </div>
</template>

<script setup>
// API测试页面占位
</script>

<style lang="scss" scoped>
.api-test {
  .coming-soon {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    text-align: center;
    color: #9ca3af;
    
    h2 {
      margin: 16px 0 8px 0;
      color: #6b7280;
    }
    
    p {
      margin: 0;
    }
  }
}
</style>