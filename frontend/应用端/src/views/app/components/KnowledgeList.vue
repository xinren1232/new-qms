<template>
  <div class="knowledge-list">
    <div v-for="k in knowledges" :key="k.id" class="knowledge-item" @click="$emit('select-knowledge', k)">
      <el-card shadow="hover">
        <div class="title">{{ k.name }}</div>
        <div class="desc">{{ k.desc }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const knowledges = reactive([
  { id: 'k1', name: 'ISO9001规范', desc: '质量管理体系基本要求' },
  { id: 'k2', name: '8D报告指南', desc: '问题解决的标准流程' }
])
</script>

<style scoped>
.knowledge-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 12px; }
.title { font-weight: 600; margin-bottom: 6px; }
.desc { color: #888; font-size: 12px; }
</style>

