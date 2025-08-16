<template>
  <div class="file-viewer">
    <div class="page-header">
      <h2>查看器配置</h2>
      <p class="text-muted">配置文件查看器和预览功能</p>
    </div>
    <div class="content">
      <el-empty description="查看器配置功能开发中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
// 查看器配置组件
</script>

<style scoped>
.file-viewer {
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
