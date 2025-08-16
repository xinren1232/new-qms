<template>
  <div class="history-list">
    <div v-for="c in conversations" :key="c.id" class="history-item" @click="$emit('select-conversation', c)">
      <el-card shadow="hover">
        <div class="title">{{ c.title }}</div>
        <div class="meta">更新于：{{ c.updated_at }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const conversations = reactive([
  { id: 'c1', title: '质量优化讨论', updated_at: '2025-01-05' },
  { id: 'c2', title: '供应商评审记录', updated_at: '2025-01-06' }
])
</script>

<style scoped>
.history-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 12px; }
.title { font-weight: 600; margin-bottom: 6px; }
.meta { color: #888; font-size: 12px; }
</style>

