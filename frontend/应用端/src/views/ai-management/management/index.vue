<template>
  <div class="ai-management-center">
    <div class="management-header">
      <h1>AI管理中心</h1>
      <p>管理和监控AI智能问答系统的各项功能</p>
    </div>
    
    <!-- 路由出口 -->
    <router-view />
  </div>
</template>

<script setup>
// 管理中心主页面，主要用于路由导航
</script>

<style scoped>
.ai-management-center {
  padding: 20px;
}

.management-header {
  margin-bottom: 20px;
  text-align: center;
}

.management-header h1 {
  color: #303133;
  margin-bottom: 8px;
}

.management-header p {
  color: #606266;
  font-size: 14px;
}
</style>
