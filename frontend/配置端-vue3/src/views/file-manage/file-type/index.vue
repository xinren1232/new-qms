<template>
  <div class="file-type">
    <div class="page-header">
      <h2>文件类型</h2>
      <p class="text-muted">管理系统支持的文件类型配置</p>
    </div>
    <div class="content">
      <el-empty description="文件类型管理功能开发中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
// 文件类型组件
</script>

<style scoped>
.file-type {
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
