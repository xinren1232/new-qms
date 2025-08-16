<template>
  <div class="monitoring-page">
    <div class="page-header">
      <div class="header-left">
        <h2>执行监控</h2>
        <p>查看任务执行状态、耗时指标与失败明细</p>
      </div>
      <div class="header-right">
        <el-button @click="$router.push('/coze-studio/validation')" type="primary">
          前往功能验证
        </el-button>
      </div>
    </div>

    <el-card class="panel">
      <template #header><div class="card-header"><span>近期任务</span></div></template>
      <el-table :data="recentExecutions" border size="small">
        <el-table-column prop="time" label="时间" width="180"/>
        <el-table-column prop="type" label="类型" width="120"/>
        <el-table-column prop="id" label="ID" width="200"/>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status==='success' ? 'success' : (row.status==='running' ? 'warning' : 'danger')">{{ row.status }}</el-tag>

          </template>
        </el-table-column>
        <el-table-column prop="durationMs" label="耗时(ms)" width="120"/>
        <el-table-column prop="message" label="摘要" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import TestPage from '@/views/coze-studio/test/index.vue'
</script>

<style scoped>
.monitoring-page { padding: 16px; }
.page-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.panel .card-header{ display:flex; align-items:center; justify-content:space-between; }
</style>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api, { getPlugins } from '@/api/coze-studio'

const recentExecutions = ref([])

async function loadRecentExecutions(){
  try {
    // 先尝试真实接口（若后端暂未提供，可回退到模拟）
    // const res = await api.getRecentExecutions()
    // recentExecutions.value = res.data.records

    // 临时模拟：结合插件列表，构造最近执行示例
    const plugins = await getPlugins().then(r => r.data.plugins).catch(()=>[])
    const now = new Date().toLocaleString()
    recentExecutions.value = [
      { time: now, type:'plugin', id: plugins[0]?.id || 'csv_parser', status:'success', durationMs: 126, message:'行=3 列=2' },
      { time: now, type:'plugin', id: plugins[1]?.id || 'xlsx_parser', status:'success', durationMs: 248, message:'行=20 列=5' },
      { time: now, type:'scenario', id:'quality-analysis', status:'running', durationMs: 0, message:'执行中...' }
    ]
  } catch (e) {
    ElMessage.error('加载近期任务失败：' + (e.message||'未知错误'))
  }
}

onMounted(loadRecentExecutions)


