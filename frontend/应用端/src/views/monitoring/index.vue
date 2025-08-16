<template>
  <div class="monitoring-dashboard">
    <div class="dashboard-header">
      <h1>🔍 系统监控仪表板</h1>
      <div class="status-indicator" :class="healthStatus.status.toLowerCase()">
        <span class="status-dot"></span>
        {{ healthStatus.status === 'HEALTHY' ? '系统正常' : '系统异常' }}
      </div>
    </div>

    <!-- 系统指标卡片 -->
    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-header">
          <h3>💻 CPU使用率</h3>
          <span class="metric-value" :class="{ warning: metrics.system.cpu > 80 }">
            {{ metrics.system.cpu }}%
          </span>
        </div>
        <div class="metric-progress">
          <div class="progress-bar" :style="{ width: metrics.system.cpu + '%' }"></div>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-header">
          <h3>🧠 内存使用率</h3>
          <span class="metric-value" :class="{ warning: metrics.system.memory > 85 }">
            {{ metrics.system.memory }}%
          </span>
        </div>
        <div class="metric-progress">
          <div class="progress-bar" :style="{ width: metrics.system.memory + '%' }"></div>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-header">
          <h3>📊 请求总数</h3>
          <span class="metric-value">{{ metrics.application.totalRequests }}</span>
        </div>
        <div class="metric-detail">
          成功: {{ metrics.application.successfulRequests }} | 
          失败: {{ metrics.application.failedRequests }}
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-header">
          <h3>⚡ 平均响应时间</h3>
          <span class="metric-value" :class="{ warning: metrics.application.averageResponseTime > 5000 }">
            {{ Math.round(metrics.application.averageResponseTime) }}ms
          </span>
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-header">
          <h3>🤖 AI对话数</h3>
          <span class="metric-value">{{ metrics.ai.totalConversations }}</span>
        </div>
        <div class="metric-detail">
          消息数: {{ metrics.ai.totalMessages }}
        </div>
      </div>

      <div class="metric-card">
        <div class="metric-header">
          <h3>❌ 错误率</h3>
          <span class="metric-value" :class="{ warning: metrics.application.errorRate > 10 }">
            {{ metrics.application.errorRate.toFixed(2) }}%
          </span>
        </div>
      </div>
    </div>

    <!-- 告警列表 -->
    <div class="alerts-section">
      <div class="section-header">
        <h2>⚠️ 系统告警</h2>
        <el-button @click="clearAlerts" size="small" type="danger" v-if="alerts.length > 0">
          清除告警
        </el-button>
      </div>
      
      <div v-if="alerts.length === 0" class="no-alerts">
        ✅ 暂无告警信息
      </div>
      
      <div v-else class="alerts-list">
        <div 
          v-for="alert in alerts" 
          :key="alert.timestamp"
          class="alert-item"
          :class="alert.level.toLowerCase()"
        >
          <div class="alert-icon">
            {{ alert.level === 'ERROR' ? '🚨' : '⚠️' }}
          </div>
          <div class="alert-content">
            <div class="alert-message">{{ alert.message }}</div>
            <div class="alert-time">{{ formatTime(alert.timestamp) }}</div>
          </div>
          <div class="alert-value">
            {{ alert.value }} / {{ alert.threshold }}
          </div>
        </div>
      </div>
    </div>

    <!-- 模型使用统计 -->
    <div class="model-usage-section" v-if="Object.keys(metrics.ai.modelUsage).length > 0">
      <h2>🤖 AI模型使用统计</h2>
      <div class="model-usage-grid">
        <div 
          v-for="(count, model) in metrics.ai.modelUsage" 
          :key="model"
          class="model-usage-item"
        >
          <div class="model-name">{{ model }}</div>
          <div class="model-count">{{ count }} 次</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElButton } from 'element-plus'

// 响应式数据
const metrics = ref({
  system: { cpu: 0, memory: 0, disk: 0, uptime: 0 },
  application: { 
    totalRequests: 0, 
    successfulRequests: 0, 
    failedRequests: 0, 
    averageResponseTime: 0,
    errorRate: 0 
  },
  ai: { 
    totalConversations: 0, 
    totalMessages: 0, 
    modelUsage: {}, 
    averageResponseTime: 0 
  }
})

const alerts = ref([])
const healthStatus = ref({ status: 'HEALTHY', issues: [] })

let refreshInterval = null

// 加载监控数据
const loadMetrics = async () => {
  try {
    const response = await fetch('/api/monitoring/metrics')
    const result = await response.json()
    
    if (result.success) {
      metrics.value = result.data
    }
  } catch (error) {
    console.error('❌ 加载监控指标失败:', error)
  }
}

// 加载告警数据
const loadAlerts = async () => {
  try {
    const response = await fetch('/api/monitoring/alerts?limit=20')
    const result = await response.json()
    
    if (result.success) {
      alerts.value = result.data
    }
  } catch (error) {
    console.error('❌ 加载告警数据失败:', error)
  }
}

// 加载健康状态
const loadHealthStatus = async () => {
  try {
    const response = await fetch('/api/monitoring/health')
    const result = await response.json()
    
    if (result.success) {
      healthStatus.value = result.data
    }
  } catch (error) {
    console.error('❌ 加载健康状态失败:', error)
  }
}

// 清除告警
const clearAlerts = async () => {
  try {
    const response = await fetch('/api/monitoring/alerts/clear', {
      method: 'POST'
    })
    const result = await response.json()
    
    if (result.success) {
      alerts.value = []
      ElMessage.success('告警已清除')
    }
  } catch (error) {
    console.error('❌ 清除告警失败:', error)
    ElMessage.error('清除告警失败')
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString('zh-CN')
}

// 加载所有数据
const loadAllData = async () => {
  await Promise.all([
    loadMetrics(),
    loadAlerts(),
    loadHealthStatus()
  ])
}

// 页面挂载时启动
onMounted(() => {
  loadAllData()
  
  // 每30秒刷新一次数据
  refreshInterval = setInterval(loadAllData, 30000)
})

// 页面卸载时清理
onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.monitoring-dashboard {
  padding: 20px;
  background: #f5f5f5;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.dashboard-header h1 {
  margin: 0;
  color: #333;
}

.status-indicator {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-radius: 20px;
  font-weight: bold;
}

.status-indicator.healthy {
  background: #e8f5e8;
  color: #52c41a;
}

.status-indicator.warning {
  background: #fff7e6;
  color: #fa8c16;
}

.status-indicator.error {
  background: #fff2f0;
  color: #ff4d4f;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  margin-right: 8px;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.metric-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.metric-header h3 {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.metric-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.metric-value.warning {
  color: #ff4d4f;
}

.metric-progress {
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #52c41a, #1890ff);
  transition: width 0.3s ease;
}

.metric-detail {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
}

.alerts-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  color: #333;
}

.no-alerts {
  text-align: center;
  color: #52c41a;
  padding: 40px;
  font-size: 16px;
}

.alerts-list {
  max-height: 400px;
  overflow-y: auto;
}

.alert-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-left: 4px solid;
  margin-bottom: 10px;
  border-radius: 4px;
}

.alert-item.warning {
  background: #fff7e6;
  border-color: #fa8c16;
}

.alert-item.error {
  background: #fff2f0;
  border-color: #ff4d4f;
}

.alert-icon {
  font-size: 20px;
  margin-right: 12px;
}

.alert-content {
  flex: 1;
}

.alert-message {
  font-weight: bold;
  color: #333;
}

.alert-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.alert-value {
  font-size: 12px;
  color: #666;
}

.model-usage-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.model-usage-section h2 {
  margin: 0 0 20px 0;
  color: #333;
}

.model-usage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.model-usage-item {
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  text-align: center;
}

.model-name {
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.model-count {
  color: #1890ff;
  font-size: 18px;
  font-weight: bold;
}
</style>
