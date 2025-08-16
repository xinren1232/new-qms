<template>
  <div class="workflow-list">
    <div v-for="flow in workflows" :key="flow.id" class="workflow-item" @click="$emit('select-workflow', flow)">
      <el-card shadow="hover">
        <div class="title">{{ flow.name }}</div>
        <div class="meta">节点: {{ flow.nodeCount }} • 版本: {{ flow.version }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const workflows = reactive([
  { id: 'w1', name: '不良品处理流程', nodeCount: 8, version: '1.2' },
  { id: 'w2', name: '来料检验流程', nodeCount: 5, version: '1.0' }
])
</script>

<style scoped>
.workflow-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 12px; }
.title { font-weight: 600; margin-bottom: 6px; }
.meta { color: #888; font-size: 12px; }
</style>

