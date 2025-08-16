<template>
  <div class="file-list">
    <div class="page-header">
      <h2>文件列表</h2>
      <p class="text-muted">查看和管理系统中的所有文件</p>
    </div>
    <div class="content">
      <el-empty description="文件列表功能开发中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
// 文件列表组件
</script>

<style scoped>
.file-list {
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
