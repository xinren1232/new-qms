<template>
  <div class="ai-config-center">
    <div class="config-header">
      <h1>系统配置</h1>
      <p>配置AI智能问答系统的各项参数和规则</p>
    </div>
    
    <!-- 路由出口 -->
    <router-view />
  </div>
</template>

<script setup>
// 配置中心主页面，主要用于路由导航
</script>

<style scoped>
.ai-config-center {
  padding: 20px;
}

.config-header {
  margin-bottom: 20px;
  text-align: center;
}

.config-header h1 {
  color: #303133;
  margin-bottom: 8px;
}

.config-header p {
  color: #606266;
  font-size: 14px;
}
</style>
