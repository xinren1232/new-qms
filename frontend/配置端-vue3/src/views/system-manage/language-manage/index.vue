<template>
  <div class="language-manage">
    <div class="page-header">
      <h2>语言管理</h2>
      <p class="text-muted">管理系统多语言配置</p>
    </div>
    <div class="content">
      <el-empty description="语言管理功能开发中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
// 语言管理组件
</script>

<style scoped>
.language-manage {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.text-muted {
  color: #909399;
  margin: 0;
}

.content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}
</style>
