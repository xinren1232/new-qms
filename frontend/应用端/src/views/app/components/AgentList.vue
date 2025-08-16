<template>
  <div class="agent-list">
    <div v-for="agent in agents" :key="agent.id" class="agent-item" @click="$emit('select-agent', agent)">
      <el-card shadow="hover">
        <div class="title">{{ agent.name }}</div>
        <div class="desc">{{ agent.description }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const agents = reactive([
  { id: 'a1', name: '质量助手', description: '质量管理与问答助手' },
  { id: 'a2', name: '流程顾问', description: '流程规范与合规建议' }
])
</script>

<style scoped>
.agent-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 12px; }
.title { font-weight: 600; margin-bottom: 6px; }
.desc { color: #888; font-size: 12px; }
</style>

