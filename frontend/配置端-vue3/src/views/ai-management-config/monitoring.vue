<template>
  <div class="ai-monitoring">
    <div class="page-header">
      <h2>AI使用监控</h2>
      <p class="text-muted">监控AI功能的使用情况和性能指标</p>
    </div>

    <!-- 实时监控指标 -->
    <div class="monitoring-metrics">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon requests">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="metric-info">
                <div class="metric-title">今日对话</div>
                <div class="metric-value">{{ metrics.todayChats }}</div>
                <div class="metric-trend up">
                  <el-icon><ArrowUp /></el-icon>
                  +12.5%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon tokens">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="metric-info">
                <div class="metric-title">Token消耗</div>
                <div class="metric-value">{{ metrics.tokensUsed }}</div>
                <div class="metric-trend up">
                  <el-icon><ArrowUp /></el-icon>
                  +8.3%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon response">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="metric-info">
                <div class="metric-title">平均响应</div>
                <div class="metric-value">{{ metrics.avgResponse }}</div>
                <div class="metric-trend down">
                  <el-icon><ArrowDown /></el-icon>
                  -5.2%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon success">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="metric-info">
                <div class="metric-title">成功率</div>
                <div class="metric-value">{{ metrics.successRate }}</div>
                <div class="metric-trend stable">
                  <el-icon><Minus /></el-icon>
                  0.0%
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>使用趋势</span>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon><TrendCharts /></el-icon>
                <p>使用趋势图表</p>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>模型分布</span>
            </template>
            <div class="chart-container">
              <div class="chart-placeholder">
                <el-icon><PieChart /></el-icon>
                <p>模型使用分布</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 实时日志 -->
    <div class="logs-section">
      <el-card>
        <template #header>
          <div class="logs-header">
            <span>实时日志</span>
            <el-button size="small" @click="handleClearLogs">清空日志</el-button>
          </div>
        </template>
        <div class="logs-container">
          <div
            v-for="log in logs"
            :key="log.id"
            :class="['log-item', log.level]"
          >
            <span class="log-time">{{ log.timestamp }}</span>
            <span class="log-level">{{ log.level.toUpperCase() }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  Cpu,
  Timer,
  SuccessFilled,
  ArrowUp,
  ArrowDown,
  Minus,
  TrendCharts,
  PieChart
} from '@element-plus/icons-vue'

// 监控指标
const metrics = reactive({
  todayChats: '1,234',
  tokensUsed: '45.6K',
  avgResponse: '1.2s',
  successRate: '99.8%'
})

// 日志数据
const logs = ref([
  {
    id: 1,
    timestamp: '2024-01-01 10:30:15',
    level: 'info',
    message: '用户发起AI对话请求'
  },
  {
    id: 2,
    timestamp: '2024-01-01 10:30:16',
    level: 'success',
    message: 'AI响应成功，耗时1.2秒'
  },
  {
    id: 3,
    timestamp: '2024-01-01 10:30:20',
    level: 'warning',
    message: 'Token使用量接近限制'
  },
  {
    id: 4,
    timestamp: '2024-01-01 10:30:25',
    level: 'error',
    message: '模型调用失败，正在重试'
  }
])

let logInterval: NodeJS.Timeout | null = null

// 方法
const handleClearLogs = () => {
  logs.value = []
  ElMessage.success('日志已清空')
}

// 模拟实时日志更新
const updateLogs = () => {
  const levels = ['info', 'success', 'warning', 'error']
  const messages = [
    '用户发起AI对话请求',
    'AI响应成功',
    'Token使用量更新',
    '模型切换完成',
    '配置更新推送',
    '用户权限验证通过'
  ]

  logInterval = setInterval(() => {
    const newLog = {
      id: Date.now(),
      timestamp: new Date().toLocaleString(),
      level: levels[Math.floor(Math.random() * levels.length)],
      message: messages[Math.floor(Math.random() * messages.length)]
    }

    logs.value.unshift(newLog)

    // 保持最多50条日志
    if (logs.value.length > 50) {
      logs.value = logs.value.slice(0, 50)
    }
  }, 3000)
}

// 模拟指标更新
const updateMetrics = () => {
  setInterval(() => {
    metrics.todayChats = (Math.floor(Math.random() * 500) + 1000).toLocaleString()
    metrics.tokensUsed = (Math.floor(Math.random() * 20) + 40).toFixed(1) + 'K'
    metrics.avgResponse = (Math.random() * 2 + 0.5).toFixed(1) + 's'
  }, 5000)
}

onMounted(() => {
  updateLogs()
  updateMetrics()
})

onUnmounted(() => {
  if (logInterval) {
    clearInterval(logInterval)
  }
})
</script>

<style scoped>
.ai-monitoring {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.text-muted {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

/* 监控指标样式 */
.monitoring-metrics {
  margin-bottom: 20px;
}

.metric-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.metric-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.metric-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 20px;
  color: white;
}

.metric-icon.requests {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.metric-icon.tokens {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.metric-icon.response {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.metric-icon.success {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.metric-info {
  flex: 1;
}

.metric-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.metric-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.metric-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 2px;
}

.metric-trend.up {
  color: #67c23a;
}

.metric-trend.down {
  color: #f56c6c;
}

.metric-trend.stable {
  color: #909399;
}

/* 图表区域 */
.charts-section {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  text-align: center;
  color: #909399;
}

.chart-placeholder .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}

/* 日志区域 */
.logs-section {
  margin-bottom: 20px;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logs-container {
  max-height: 400px;
  overflow-y: auto;
  background: #fafafa;
  border-radius: 4px;
  padding: 10px;
}

.log-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: #909399;
  margin-right: 10px;
  min-width: 140px;
}

.log-level {
  margin-right: 10px;
  min-width: 60px;
  font-weight: 600;
}

.log-item.info .log-level {
  color: #409eff;
}

.log-item.success .log-level {
  color: #67c23a;
}

.log-item.warning .log-level {
  color: #e6a23c;
}

.log-item.error .log-level {
  color: #f56c6c;
}

.log-message {
  flex: 1;
  color: #303133;
}

:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-card__header) {
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}
</style>
