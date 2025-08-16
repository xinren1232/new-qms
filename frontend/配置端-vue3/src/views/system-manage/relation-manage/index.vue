<template>
  <div class="relation-manage">
    <div class="page-header">
      <h2>关系管理</h2>
      <p class="text-muted">管理对象间的关系定义</p>
    </div>
    <div class="content">
      <el-empty description="关系管理功能开发中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
// 关系管理组件
</script>

<style scoped>
.relation-manage {
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
