<template>
  <div class="assertion-config">
    <div class="page-header">
      <h1 class="page-title">断言配置</h1>
      <p class="page-description">配置API响应断言验证规则</p>
    </div>

    <Card>
      <div class="coming-soon">
        <Icon type="ios-time" :size="64" color="#9ca3af" />
        <h2>功能开发中</h2>
        <p>断言配置功能正在开发中，敬请期待...</p>
      </div>
    </Card>
  </div>
</template>

<script setup>
// 断言配置页面占位
</script>

<style lang="scss" scoped>
.assertion-config {
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