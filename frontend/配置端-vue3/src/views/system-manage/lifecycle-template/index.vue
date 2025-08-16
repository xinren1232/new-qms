<template>
  <div class="lifecycle-template-manage">
    <div class="page-header">
      <h2>生命周期模板</h2>
      <p class="text-muted">管理对象生命周期模板配置</p>
    </div>
    <div class="content">
      <el-empty description="生命周期模板管理功能开发中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
// 生命周期模板管理组件
</script>

<style scoped>
.lifecycle-template-manage {
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
