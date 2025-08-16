<template>
  <div class="monitoring-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>AI使用监控</h2>
      <p>实时监控AI模型使用情况、性能指标和成本统计</p>
    </div>

    <!-- 实时指标卡片 -->
    <el-row :gutter="20" class="metrics-cards">
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon">
              <el-icon size="32" color="#409EFF"><ChatDotRound /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ realTimeMetrics.total_conversations }}</div>
              <div class="metric-label">今日对话数</div>
              <div class="metric-trend positive">
                <el-icon><TrendCharts /></el-icon>
                +12.5%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon">
              <el-icon size="32" color="#67C23A"><Timer /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ realTimeMetrics.avg_response_time }}ms</div>
              <div class="metric-label">平均响应时间</div>
              <div class="metric-trend negative">
                <el-icon><TrendCharts /></el-icon>
                +5.2%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon">
              <el-icon size="32" color="#E6A23C"><Money /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">${{ realTimeMetrics.today_cost }}</div>
              <div class="metric-label">今日成本</div>
              <div class="metric-trend positive">
                <el-icon><TrendCharts /></el-icon>
                -3.1%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon">
              <el-icon size="32" color="#F56C6C"><Warning /></el-icon>
            </div>
            <div class="metric-info">
              <div class="metric-value">{{ realTimeMetrics.error_rate }}%</div>
              <div class="metric-label">错误率</div>
              <div class="metric-trend" :class="realTimeMetrics.error_rate > 5 ? 'negative' : 'positive'">
                <el-icon><TrendCharts /></el-icon>
                {{ realTimeMetrics.error_rate > 5 ? '+' : '-' }}2.1%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <!-- 使用趋势图 -->
      <el-col :span="16">
        <el-card title="使用趋势" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>使用趋势</span>
              <div class="chart-controls">
                <el-radio-group v-model="trendTimeRange" size="small" @change="updateTrendChart">
                  <el-radio-button label="1h">1小时</el-radio-button>
                  <el-radio-button label="24h">24小时</el-radio-button>
                  <el-radio-button label="7d">7天</el-radio-button>
                  <el-radio-button label="30d">30天</el-radio-button>
                </el-radio-group>
              </div>
            </div>
          </template>
          <div ref="trendChartRef" class="chart" style="height: 350px;" />
        </el-card>
      </el-col>
      
      <!-- 模型使用分布 -->
      <el-col :span="8">
        <el-card title="模型使用分布" class="chart-card">
          <div ref="modelDistributionRef" class="chart" style="height: 350px;" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细监控表格 -->
    <el-row :gutter="20" class="tables-section">
      <!-- 模型性能监控 -->
      <el-col :span="12">
        <el-card title="模型性能监控" class="table-card">
          <template #header>
            <div class="card-header">
              <span>模型性能监控</span>
              <el-button size="small" @click="refreshModelMetrics">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <el-table :data="modelMetrics" style="width: 100%" size="small">
            <el-table-column prop="model_name" label="模型" width="120">
              <template #default="{ row }">
                <div class="model-info">
                  <span class="model-name">{{ row.model_name }}</span>
                  <el-tag :type="getModelStatusType(row.status)" size="small">
                    {{ row.status }}
                  </el-tag>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="requests_count" label="请求数" width="80" />
            <el-table-column prop="avg_response_time" label="响应时间" width="90">
              <template #default="{ row }">
                <span :class="getResponseTimeClass(row.avg_response_time)">
                  {{ row.avg_response_time }}ms
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="success_rate" label="成功率" width="80">
              <template #default="{ row }">
                <span :class="getSuccessRateClass(row.success_rate)">
                  {{ row.success_rate }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="cost" label="成本" width="80">
              <template #default="{ row }">
                ${{ row.cost.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <!-- 用户活跃度 -->
      <el-col :span="12">
        <el-card title="用户活跃度" class="table-card">
          <template #header>
            <div class="card-header">
              <span>用户活跃度 (Top 10)</span>
              <el-button size="small" @click="refreshUserActivity">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          
          <el-table :data="userActivity" style="width: 100%" size="small">
            <el-table-column prop="rank" label="排名" width="60" />
            <el-table-column prop="username" label="用户" width="100" />
            <el-table-column prop="conversations" label="对话数" width="80" />
            <el-table-column prop="tokens_used" label="Token使用" width="100">
              <template #default="{ row }">
                {{ formatNumber(row.tokens_used) }}
              </template>
            </el-table-column>
            <el-table-column prop="cost" label="成本" width="80">
              <template #default="{ row }">
                ${{ row.cost.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quota_usage" label="配额使用" width="100">
              <template #default="{ row }">
                <el-progress 
                  :percentage="row.quota_usage" 
                  :color="getQuotaColor(row.quota_usage)"
                  :show-text="false"
                  style="width: 60px;"
                />
                <span style="margin-left: 8px; font-size: 12px;">{{ row.quota_usage }}%</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 告警和异常 -->
    <el-row :gutter="20" class="alerts-section">
      <el-col :span="24">
        <el-card title="实时告警" class="alerts-card">
          <template #header>
            <div class="card-header">
              <span>实时告警</span>
              <div class="alert-controls">
                <el-badge :value="activeAlerts.length" :hidden="activeAlerts.length === 0">
                  <el-button size="small" type="danger" @click="showAllAlerts">
                    <el-icon><Bell /></el-icon>
                    查看所有告警
                  </el-button>
                </el-badge>
                <el-button size="small" @click="refreshAlerts">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
            </div>
          </template>
          
          <div v-if="activeAlerts.length > 0" class="alerts-list">
            <div 
              v-for="alert in activeAlerts.slice(0, 5)" 
              :key="alert.id" 
              class="alert-item"
              :class="getAlertClass(alert.severity)"
            >
              <div class="alert-icon">
                <el-icon><component :is="getAlertIcon(alert.severity)" /></el-icon>
              </div>
              <div class="alert-content">
                <div class="alert-title">{{ alert.title }}</div>
                <div class="alert-description">{{ alert.description }}</div>
                <div class="alert-time">{{ formatTime(alert.created_at) }}</div>
              </div>
              <div class="alert-actions">
                <el-button size="small" @click="acknowledgeAlert(alert.id)">确认</el-button>
                <el-button size="small" type="primary" @click="resolveAlert(alert.id)">解决</el-button>
              </div>
            </div>
          </div>
          
          <el-empty v-else description="暂无活跃告警" :image-size="100" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, getCurrentInstance } from 'vue'
// 使用Vue 2的Element UI消息组件
// import { ElMessage } from 'element-plus'
// import {
//   ChatDotRound, Timer, Money, Warning, TrendCharts, Refresh, Bell
// } from '@element-plus/icons-vue'

export default {
  name: 'Monitoring',
  setup() {
    const { proxy } = getCurrentInstance()

    // 响应式数据
    const realTimeMetrics = ref({
      total_conversations: 1247,
      avg_response_time: 1850,
      today_cost: 23.45,
      error_rate: 2.1
    })

    const trendTimeRange = ref('24h')

    const modelMetrics = ref([
      {
        model_name: 'GPT-4o',
        status: 'healthy',
        requests_count: 456,
        avg_response_time: 1650,
        success_rate: 98.5,
        cost: 12.34
      },
      {
        model_name: 'GPT-4o Mini',
        status: 'healthy',
        requests_count: 789,
        avg_response_time: 1200,
        success_rate: 99.2,
        cost: 5.67
      },
      {
        model_name: 'DeepSeek R1',
        status: 'degraded',
        requests_count: 234,
        avg_response_time: 2800,
        success_rate: 95.1,
        cost: 1.23
      },
      {
        model_name: 'Claude 3.5',
        status: 'offline',
        requests_count: 0,
        avg_response_time: 0,
        success_rate: 0,
        cost: 0
      }
    ])

    const userActivity = ref([
      { rank: 1, username: 'admin', conversations: 45, tokens_used: 125000, cost: 3.75, quota_usage: 62 },
      { rank: 2, username: 'developer', conversations: 38, tokens_used: 98000, cost: 2.94, quota_usage: 49 },
      { rank: 3, username: 'quality', conversations: 32, tokens_used: 87000, cost: 2.61, quota_usage: 43 },
      { rank: 4, username: 'manager', conversations: 28, tokens_used: 76000, cost: 2.28, quota_usage: 38 },
      { rank: 5, username: 'analyst', conversations: 24, tokens_used: 65000, cost: 1.95, quota_usage: 32 }
    ])

    const activeAlerts = ref([
      {
        id: 'alert_001',
        title: '模型响应时间过长',
        description: 'DeepSeek R1 模型平均响应时间超过 2.5 秒',
        severity: 'warning',
        created_at: '2024-01-15 14:30:00'
      },
      {
        id: 'alert_002', 
        title: '用户配额即将耗尽',
        description: '用户 admin 的日配额使用率已达 90%',
        severity: 'info',
        created_at: '2024-01-15 14:25:00'
      }
    ])

    // 图表引用
    const _trendChartRef = ref(null)
    const _modelDistributionRef = ref(null)

    // 定时器
    let metricsTimer = null

    // 方法
    const _getModelStatusType = (status) => {
      const types = {
        'healthy': 'success',
        'degraded': 'warning',
        'offline': 'danger'
      }

      return types[status] || 'info'
    }

    const _getResponseTimeClass = (time) => {
      if (time > 3000) return 'metric-danger'
      if (time > 2000) return 'metric-warning'

      return 'metric-normal'
    }

    const _getSuccessRateClass = (rate) => {
      if (rate < 95) return 'metric-danger'
      if (rate < 98) return 'metric-warning'

      return 'metric-normal'
    }

    const _getQuotaColor = (usage) => {
      if (usage >= 90) return '#f56c6c'
      if (usage >= 70) return '#e6a23c'

      return '#67c23a'
    }

    const _getAlertClass = (severity) => {
      return `alert-${severity}`
    }

    const _getAlertIcon = (severity) => {
      const icons = {
        'critical': 'CircleCloseFilled',
        'warning': 'WarningFilled',
        'info': 'InfoFilled'
      }

      return icons[severity] || 'InfoFilled'
    }

    const _formatNumber = (num) => {
      if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
      if (num >= 1000) return (num / 1000).toFixed(1) + 'K'

      return num.toString()
    }

    const _formatTime = (timeStr) => {
      return new Date(timeStr).toLocaleString()
    }

    // 事件处理
    const updateTrendChart = () => {
      // 更新趋势图表
      proxy.$message.info(`切换到 ${trendTimeRange.value} 时间范围`)
    }

    const refreshModelMetrics = async () => {
      proxy.$message.success('模型指标已刷新')
    }

    const refreshUserActivity = async () => {
      proxy.$message.success('用户活跃度已刷新')
    }

    const refreshAlerts = async () => {
      proxy.$message.success('告警信息已刷新')
    }

    const showAllAlerts = () => {
      proxy.$message.info('查看所有告警功能开发中...')
    }

    const acknowledgeAlert = (alertId) => {
      const index = activeAlerts.value.findIndex(alert => alert.id === alertId)

      if (index > -1) {
        activeAlerts.value.splice(index, 1)
        proxy.$message.success('告警已确认')
      }
    }

    const resolveAlert = (alertId) => {
      const index = activeAlerts.value.findIndex(alert => alert.id === alertId)

      if (index > -1) {
        activeAlerts.value.splice(index, 1)
        proxy.$message.success('告警已解决')
      }
    }

    // 实时数据更新
    const updateRealTimeMetrics = () => {
      // 模拟实时数据更新
      realTimeMetrics.value.total_conversations += Math.floor(Math.random() * 3)
      realTimeMetrics.value.avg_response_time += Math.floor(Math.random() * 100 - 50)
      realTimeMetrics.value.today_cost += Math.random() * 0.1
      realTimeMetrics.value.error_rate = Math.max(0, realTimeMetrics.value.error_rate + Math.random() * 0.2 - 0.1)
    }

    // 生命周期
    onMounted(() => {
      // 启动实时数据更新
      metricsTimer = setInterval(updateRealTimeMetrics, 5000)
  
      // 初始化图表
      updateTrendChart()
    })

    onUnmounted(() => {
      if (metricsTimer) {
        clearInterval(metricsTimer)
      }
    })

    // 返回所有响应式数据和方法
    return {
      // 响应式数据
      realTimeMetrics,
      modelMetrics,
      userActivity,
      activeAlerts,
      trendTimeRange,

      // 方法
      updateTrendChart,
      refreshModelMetrics,
      refreshUserActivity,
      refreshAlerts,
      showAllAlerts,
      acknowledgeAlert,
      resolveAlert
    }
  }
}
</script>

<style scoped>
.monitoring-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.metrics-cards {
  margin-bottom: 24px;
}

.metric-card {
  transition: all 0.3s;
}

.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.metric-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.metric-info {
  flex: 1;
}

.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.metric-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.metric-trend.positive {
  color: #67c23a;
}

.metric-trend.negative {
  color: #f56c6c;
}

.charts-section,
.tables-section,
.alerts-section {
  margin-bottom: 24px;
}

.chart-card,
.table-card,
.alerts-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-controls {
  display: flex;
  gap: 8px;
}

.model-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.model-name {
  font-weight: 500;
}

.metric-normal {
  color: #67c23a;
}

.metric-warning {
  color: #e6a23c;
}

.metric-danger {
  color: #f56c6c;
}

.alerts-list {
  max-height: 300px;
  overflow-y: auto;
}

.alert-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 12px;
  border-left: 4px solid;
}

.alert-item.alert-critical {
  background: #fef0f0;
  border-left-color: #f56c6c;
}

.alert-item.alert-warning {
  background: #fdf6ec;
  border-left-color: #e6a23c;
}

.alert-item.alert-info {
  background: #f4f4f5;
  border-left-color: #909399;
}

.alert-icon {
  margin-top: 2px;
}

.alert-content {
  flex: 1;
}

.alert-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.alert-description {
  color: #606266;
  font-size: 14px;
  margin-bottom: 4px;
}

.alert-time {
  color: #909399;
  font-size: 12px;
}

.alert-actions {
  display: flex;
  gap: 8px;
}

.alert-controls {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
