<template>
  <div class="copy-rule">
    <div class="page-header">
      <h2>复制规则</h2>
      <p class="text-muted">管理文件复制规则配置</p>
    </div>
    <div class="content">
      <el-empty description="复制规则功能开发中..." />
    </div>
  </div>
</template>

<script setup lang="ts">
// 复制规则组件
</script>

<style scoped>
.copy-rule {
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
