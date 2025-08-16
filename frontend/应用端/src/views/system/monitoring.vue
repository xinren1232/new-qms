<template>
  <div class="monitoring-dashboard">
    <div class="dashboard-header">
      <h2>
        <el-icon><Monitor /></el-icon>
        系统监控面板
      </h2>
      <div class="header-actions">
        <el-button 
          :icon="Refresh" 
          @click="refreshAll"
          :loading="refreshing"
          type="primary"
        >
          刷新数据
        </el-button>
        <el-switch
          v-model="autoRefresh"
          active-text="自动刷新"
          @change="toggleAutoRefresh"
        />
      </div>
    </div>
    
    <!-- 系统概览 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon system-health">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="metric-info">
                <h3>系统状态</h3>
                <p :class="systemStatus.class">{{ systemStatus.text }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon active-users">
                <el-icon><User /></el-icon>
              </div>
              <div class="metric-info">
                <h3>活跃用户</h3>
                <p>{{ metrics.activeUsers }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon response-time">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="metric-info">
                <h3>平均响应时间</h3>
                <p>{{ metrics.avgResponseTime }}ms</p>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="6">
          <el-card class="metric-card">
            <div class="metric-content">
              <div class="metric-icon requests">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="metric-info">
                <h3>今日请求</h3>
                <p>{{ metrics.todayRequests }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 服务状态 -->
    <div class="services-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <h3>
              <el-icon><Setting /></el-icon>
              服务状态
            </h3>
            <el-button size="small" @click="checkAllServices">检查服务</el-button>
          </div>
        </template>
        
        <el-row :gutter="16">
          <el-col 
            v-for="service in services" 
            :key="service.name"
            :span="8"
          >
            <div class="service-item">
              <div class="service-header">
                <div class="service-info">
                  <el-icon class="service-icon">
                    <component :is="service.icon" />
                  </el-icon>
                  <div>
                    <h4>{{ service.title }}</h4>
                    <p>{{ service.description }}</p>
                  </div>
                </div>
                <el-tag 
                  :type="getStatusType(service.status)"
                  :effect="service.status === 'healthy' ? 'dark' : 'plain'"
                >
                  {{ getStatusText(service.status) }}
                </el-tag>
              </div>
              
              <div class="service-details">
                <div class="detail-item">
                  <span>端口:</span>
                  <span>{{ service.port }}</span>
                </div>
                <div class="detail-item">
                  <span>响应时间:</span>
                  <span>{{ service.responseTime }}ms</span>
                </div>
                <div class="detail-item">
                  <span>运行时间:</span>
                  <span>{{ service.uptime }}</span>
                </div>
              </div>
              
              <div class="service-actions">
                <el-button 
                  size="small" 
                  @click="checkService(service)"
                  :loading="service.checking"
                >
                  检查
                </el-button>
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="openService(service)"
                >
                  访问
                </el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
    
    <!-- 性能图表 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <h3>
                <el-icon><TrendCharts /></el-icon>
                响应时间趋势
              </h3>
            </template>
            <div class="chart-container">
              <div ref="responseTimeChart" class="chart"></div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card>
            <template #header>
              <h3>
                <el-icon><DataAnalysis /></el-icon>
                内存使用情况
              </h3>
            </template>
            <div class="chart-container">
              <div ref="memoryChart" class="chart"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="24">
          <el-card>
            <template #header>
              <h3>
                <el-icon><Monitor /></el-icon>
                请求量统计
              </h3>
            </template>
            <div class="chart-container">
              <div ref="requestChart" class="chart"></div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 系统日志 -->
    <div class="logs-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <h3>
              <el-icon><Document /></el-icon>
              系统日志
            </h3>
            <div class="log-actions">
              <el-select v-model="logLevel" size="small" style="width: 100px;">
                <el-option label="全部" value="all" />
                <el-option label="错误" value="error" />
                <el-option label="警告" value="warn" />
                <el-option label="信息" value="info" />
              </el-select>
              <el-button size="small" @click="clearLogs">清空</el-button>
            </div>
          </div>
        </template>
        
        <div class="logs-container">
          <div 
            v-for="log in filteredLogs" 
            :key="log.id"
            :class="['log-item', `log-${log.level}`]"
          >
            <span class="log-time">{{ formatTime(log.timestamp) }}</span>
            <span class="log-level">{{ log.level.toUpperCase() }}</span>
            <span class="log-service">{{ log.service }}</span>
            <span class="log-message">{{ log.message }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { 
  Monitor, 
  Refresh, 
  CircleCheck, 
  User, 
  Clock, 
  TrendCharts,
  Setting,
  DataAnalysis,
  Document
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'

// 响应式数据
const refreshing = ref(false)
const autoRefresh = ref(true)
const logLevel = ref('all')
let refreshTimer = null

// 系统指标
const metrics = reactive({
  activeUsers: 12,
  avgResponseTime: 245,
  todayRequests: 1847,
  errorRate: 0.02
})

// 系统状态
const systemStatus = computed(() => {
  const errorRate = metrics.errorRate
  if (errorRate < 0.01) {
    return { text: '健康', class: 'status-healthy' }
  } else if (errorRate < 0.05) {
    return { text: '警告', class: 'status-warning' }
  } else {
    return { text: '异常', class: 'status-error' }
  }
})

// 服务列表
const services = reactive([
  {
    name: 'api-gateway',
    title: 'API网关',
    description: '统一API入口',
    icon: 'Setting',
    port: 8080,
    url: 'http://localhost:8080',
    status: 'healthy',
    responseTime: 45,
    uptime: '2小时15分',
    checking: false
  },
  {
    name: 'chat-service',
    title: '聊天服务',
    description: 'AI对话处理',
    icon: 'ChatDotRound',
    port: 3004,
    url: 'http://localhost:3004',
    status: 'healthy',
    responseTime: 120,
    uptime: '2小时12分',
    checking: false
  },
  {
    name: 'config-service',
    title: '配置服务',
    description: '系统配置管理',
    icon: 'Tools',
    port: 3003,
    url: 'http://localhost:3003',
    status: 'healthy',
    responseTime: 35,
    uptime: '2小时10分',
    checking: false
  },
  {
    name: 'frontend-app',
    title: '应用端',
    description: '用户界面',
    icon: 'Monitor',
    port: 8082,
    url: 'http://localhost:8082',
    status: 'healthy',
    responseTime: 25,
    uptime: '2小时8分',
    checking: false
  },
  {
    name: 'frontend-config',
    title: '配置端',
    description: '管理界面',
    icon: 'Setting',
    port: 8072,
    url: 'http://localhost:8072',
    status: 'healthy',
    responseTime: 30,
    uptime: '2小时5分',
    checking: false
  }
])

// 系统日志
const logs = reactive([
  {
    id: 1,
    timestamp: new Date(Date.now() - 5 * 60 * 1000),
    level: 'info',
    service: 'API网关',
    message: '服务启动成功，监听端口 8080'
  },
  {
    id: 2,
    timestamp: new Date(Date.now() - 3 * 60 * 1000),
    level: 'info',
    service: '聊天服务',
    message: '新用户连接，当前活跃用户: 12'
  },
  {
    id: 3,
    timestamp: new Date(Date.now() - 2 * 60 * 1000),
    level: 'warn',
    service: '配置服务',
    message: '配置缓存即将过期，建议刷新'
  },
  {
    id: 4,
    timestamp: new Date(Date.now() - 1 * 60 * 1000),
    level: 'info',
    service: 'API网关',
    message: '处理请求: POST /api/chat/send'
  }
])

// 计算属性
const filteredLogs = computed(() => {
  if (logLevel.value === 'all') {
    return logs.slice().reverse()
  }
  return logs.filter(log => log.level === logLevel.value).reverse()
})

// 图表引用
const responseTimeChart = ref(null)
const memoryChart = ref(null)
const requestChart = ref(null)

// 方法
const refreshAll = async () => {
  refreshing.value = true
  try {
    await Promise.all([
      updateMetrics(),
      checkAllServices(),
      updateCharts()
    ])
    ElMessage.success('数据刷新成功')
  } catch (error) {
    ElMessage.error('数据刷新失败')
  } finally {
    refreshing.value = false
  }
}

const updateMetrics = async () => {
  // 模拟API调用
  await new Promise(resolve => setTimeout(resolve, 500))
  
  metrics.activeUsers = Math.floor(Math.random() * 20) + 10
  metrics.avgResponseTime = Math.floor(Math.random() * 100) + 200
  metrics.todayRequests += Math.floor(Math.random() * 10)
  metrics.errorRate = Math.random() * 0.05
}

const checkAllServices = async () => {
  for (const service of services) {
    await checkService(service)
  }
}

const checkService = async (service) => {
  service.checking = true
  try {
    // 模拟健康检查
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const isHealthy = Math.random() > 0.1 // 90% 概率健康
    service.status = isHealthy ? 'healthy' : 'unhealthy'
    service.responseTime = Math.floor(Math.random() * 200) + 20
    
  } catch (error) {
    service.status = 'error'
  } finally {
    service.checking = false
  }
}

const openService = (service) => {
  window.open(service.url, '_blank')
}

const getStatusType = (status) => {
  const typeMap = {
    healthy: 'success',
    warning: 'warning',
    unhealthy: 'danger',
    error: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    healthy: '健康',
    warning: '警告',
    unhealthy: '异常',
    error: '错误'
  }
  return textMap[status] || '未知'
}

const toggleAutoRefresh = (enabled) => {
  if (enabled) {
    refreshTimer = setInterval(refreshAll, 30000) // 30秒刷新一次
  } else {
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
  }
}

const clearLogs = () => {
  logs.splice(0, logs.length)
  ElMessage.success('日志已清空')
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString()
}

// 初始化图表
const initCharts = () => {
  // 响应时间趋势图
  if (responseTimeChart.value) {
    const chart1 = echarts.init(responseTimeChart.value)
    chart1.setOption({
      title: { text: '响应时间 (ms)' },
      xAxis: { type: 'category', data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'] },
      yAxis: { type: 'value' },
      series: [{
        data: [120, 200, 150, 80, 70, 110],
        type: 'line',
        smooth: true,
        areaStyle: {}
      }]
    })
  }
  
  // 内存使用图
  if (memoryChart.value) {
    const chart2 = echarts.init(memoryChart.value)
    chart2.setOption({
      title: { text: '内存使用率 (%)' },
      series: [{
        type: 'gauge',
        data: [{ value: 65, name: '内存使用率' }],
        detail: { formatter: '{value}%' }
      }]
    })
  }
  
  // 请求量统计图
  if (requestChart.value) {
    const chart3 = echarts.init(requestChart.value)
    chart3.setOption({
      title: { text: '每小时请求量' },
      xAxis: { type: 'category', data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'] },
      yAxis: { type: 'value' },
      series: [{
        data: [50, 80, 120, 200, 180, 150],
        type: 'bar',
        itemStyle: { color: '#4facfe' }
      }]
    })
  }
}

const updateCharts = async () => {
  // 更新图表数据
  initCharts()
}

// 生命周期
onMounted(() => {
  initCharts()
  if (autoRefresh.value) {
    toggleAutoRefresh(true)
  }
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
.monitoring-dashboard {
  padding: 20px;
  
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    
    h2 {
      margin: 0;
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--qms-text-primary);
    }
    
    .header-actions {
      display: flex;
      align-items: center;
      gap: 16px;
    }
  }
  
  .overview-section {
    margin-bottom: 24px;
    
    .metric-card {
      .metric-content {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .metric-icon {
          width: 48px;
          height: 48px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
          color: white;
          
          &.system-health { background: var(--qms-success); }
          &.active-users { background: var(--qms-primary); }
          &.response-time { background: var(--qms-warning); }
          &.requests { background: var(--qms-info); }
        }
        
        .metric-info {
          h3 {
            margin: 0 0 4px 0;
            font-size: 14px;
            color: var(--qms-text-secondary);
          }
          
          p {
            margin: 0;
            font-size: 24px;
            font-weight: 600;
            color: var(--qms-text-primary);
            
            &.status-healthy { color: var(--qms-success); }
            &.status-warning { color: var(--qms-warning); }
            &.status-error { color: var(--qms-error); }
          }
        }
      }
    }
  }
  
  .services-section {
    margin-bottom: 24px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
    
    .service-item {
      border: 1px solid var(--qms-border-normal);
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;
      
      .service-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12px;
        
        .service-info {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .service-icon {
            font-size: 24px;
            color: var(--qms-primary);
          }
          
          h4 {
            margin: 0 0 4px 0;
            color: var(--qms-text-primary);
          }
          
          p {
            margin: 0;
            font-size: 12px;
            color: var(--qms-text-tertiary);
          }
        }
      }
      
      .service-details {
        margin-bottom: 12px;
        
        .detail-item {
          display: flex;
          justify-content: space-between;
          margin-bottom: 4px;
          font-size: 12px;
          color: var(--qms-text-secondary);
        }
      }
      
      .service-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .charts-section {
    margin-bottom: 24px;
    
    .chart-container {
      .chart {
        height: 300px;
      }
    }
  }
  
  .logs-section {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h3 {
        margin: 0;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      
      .log-actions {
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
    
    .logs-container {
      max-height: 300px;
      overflow-y: auto;
      
      .log-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 8px 0;
        border-bottom: 1px solid var(--qms-border-light);
        font-size: 12px;
        
        &:last-child {
          border-bottom: none;
        }
        
        .log-time {
          color: var(--qms-text-tertiary);
          min-width: 80px;
        }
        
        .log-level {
          min-width: 50px;
          font-weight: 600;
          
          &.log-info { color: var(--qms-info); }
          &.log-warn { color: var(--qms-warning); }
          &.log-error { color: var(--qms-error); }
        }
        
        .log-service {
          min-width: 80px;
          color: var(--qms-text-secondary);
        }
        
        .log-message {
          flex: 1;
          color: var(--qms-text-primary);
        }
      }
    }
  }
}
</style>
