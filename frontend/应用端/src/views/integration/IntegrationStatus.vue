<template>
  <div class="integration-status">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Connection /></el-icon>
          系统集成
        </h1>
        <p class="page-description">监控和管理与外部系统的集成状态</p>
      </div>
      <div class="header-actions">
        <el-button @click="refreshIntegrationData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新状态
        </el-button>
        <el-button type="primary" @click="testAllConnections">
          <el-icon><Connection /></el-icon>
          测试连接
        </el-button>
      </div>
    </div>

    <!-- 集成概览 -->
    <el-row :gutter="20" class="integration-overview">
      <el-col :xs="12" :sm="6" v-for="metric in integrationMetrics" :key="metric.key">
        <div class="metric-card">
          <div class="metric-icon" :style="{ backgroundColor: metric.color }">
            <el-icon :size="24"><component :is="metric.icon" /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ metric.value }}</div>
            <div class="metric-label">{{ metric.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 模块健康监控 -->
    <el-row :gutter="20" class="module-health">
      <el-col :span="24">
        <el-card class="health-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Monitor /></el-icon>
                模块健康监控
              </span>
              <div class="health-summary">
                <span class="healthy-count">{{ healthyModulesCount }}</span>
                <span class="total-count">/ {{ moduleHealth.length }}</span>
                <span class="summary-label">模块正常</span>
              </div>
            </div>
          </template>

          <div class="module-grid" v-loading="integration.loading">
            <div
              v-for="module in moduleHealth"
              :key="module.name"
              class="module-card"
              :class="{ 'healthy': module.healthy, 'unhealthy': !module.healthy }"
              @click="testModuleConnection(module)"
            >
              <div class="module-status">
                <div class="status-indicator" :class="{ 'status-green': module.healthy, 'status-red': !module.healthy }">
                  <el-icon :size="16">
                    <component :is="module.healthy ? 'CircleCheck' : 'CircleClose'" />
                  </el-icon>
                </div>
              </div>

              <div class="module-info">
                <div class="module-name">{{ module.name }}</div>
                <div class="module-description">{{ module.description }}</div>
                <div class="module-url">{{ module.url }}</div>
              </div>

              <div class="module-metrics">
                <div class="metric-item">
                  <span class="metric-label">响应时间</span>
                  <span class="metric-value">{{ module.responseTime || 'N/A' }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">最后检查</span>
                  <span class="metric-value">{{ formatTime(module.lastCheck) }}</span>
                </div>
              </div>

              <div class="module-actions">
                <el-button size="small" @click.stop="testModuleConnection(module)">
                  <el-icon><Connection /></el-icon>
                  测试
                </el-button>
                <el-button size="small" @click.stop="viewModuleLogs(module)">
                  <el-icon><Document /></el-icon>
                  日志
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 集成日志 -->
    <el-row :gutter="20" class="integration-logs">
      <el-col :span="24">
        <el-card class="logs-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Document /></el-icon>
                集成日志
              </span>
              <div class="log-filters">
                <el-select v-model="logFilter.level" placeholder="日志级别" size="small" style="width: 120px">
                  <el-option label="全部" value="" />
                  <el-option label="信息" value="info" />
                  <el-option label="警告" value="warning" />
                  <el-option label="错误" value="error" />
                </el-select>
                <el-select v-model="logFilter.module" placeholder="模块" size="small" style="width: 150px">
                  <el-option label="全部模块" value="" />
                  <el-option
                    v-for="module in moduleHealth"
                    :key="module.name"
                    :label="module.name"
                    :value="module.name"
                  />
                </el-select>
                <el-button size="small" @click="refreshLogs">
                  <el-icon><Refresh /></el-icon>
                </el-button>
              </div>
            </div>
          </template>

          <div class="logs-content" v-loading="logs.loading">
            <div v-if="filteredLogs.length === 0" class="empty-logs">
              <el-empty description="暂无日志记录" :image-size="80" />
            </div>
            <div v-else class="log-list">
              <div
                v-for="log in filteredLogs"
                :key="log.id"
                class="log-item"
                :class="`log-${log.level}`"
              >
                <div class="log-time">{{ formatTime(log.timestamp) }}</div>
                <div class="log-level">
                  <el-tag :type="getLogLevelType(log.level)" size="small">
                    {{ log.level.toUpperCase() }}
                  </el-tag>
                </div>
                <div class="log-module">{{ log.module }}</div>
                <div class="log-message">{{ log.message }}</div>
                <div class="log-actions">
                  <el-button size="small" text @click="viewLogDetails(log)">
                    详情
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div class="logs-pagination">
              <el-pagination
                v-model:current-page="logPagination.current"
                v-model:page-size="logPagination.size"
                :total="logPagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleLogPageSizeChange"
                @current-change="handleLogPageChange"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { integrationAPI } from '@/api/advanced-features'

// 响应式数据
const loading = ref(false)

const integration = reactive({
  loading: false
})

const logs = reactive({
  loading: false
})

const integrationMetrics = ref([
  {
    key: 'connected',
    label: '已连接模块',
    value: '0',
    icon: 'Connection',
    color: '#67C23A'
  },
  {
    key: 'apiCalls',
    label: '今日API调用',
    value: '0',
    icon: 'DataLine',
    color: '#409EFF'
  },
  {
    key: 'healthy',
    label: '健康模块',
    value: '0',
    icon: 'CircleCheck',
    color: '#E6A23C'
  },
  {
    key: 'errors',
    label: '错误次数',
    value: '0',
    icon: 'Warning',
    color: '#F56C6C'
  }
])

const moduleHealth = ref([
  {
    name: '配置中心',
    description: '系统配置管理服务',
    url: 'http://localhost:3003',
    healthy: true,
    responseTime: '45ms',
    lastCheck: new Date()
  },
  {
    name: '聊天服务',
    description: 'AI对话处理服务',
    url: 'http://localhost:3007',
    healthy: true,
    responseTime: '120ms',
    lastCheck: new Date()
  },
  {
    name: '导出服务',
    description: '数据导出处理服务',
    url: 'http://localhost:3008',
    healthy: false,
    responseTime: 'N/A',
    lastCheck: new Date(Date.now() - 1000 * 60 * 5)
  },
  {
    name: '高级功能服务',
    description: '智能分析和推荐服务',
    url: 'http://localhost:3009',
    healthy: true,
    responseTime: '89ms',
    lastCheck: new Date()
  }
])

const integrationLogs = ref([])

const logFilter = reactive({
  level: '',
  module: ''
})

const logPagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 计算属性
const healthyModulesCount = computed(() => {
  return moduleHealth.value.filter(module => module.healthy).length
})

const filteredLogs = computed(() => {
  let logs = integrationLogs.value

  if (logFilter.level) {
    logs = logs.filter(log => log.level === logFilter.level)
  }

  if (logFilter.module) {
    logs = logs.filter(log => log.module === logFilter.module)
  }

  return logs
})

// 生命周期
onMounted(() => {
  loadIntegrationData()
})

// 方法
const loadIntegrationData = async () => {
  await Promise.all([
    loadIntegrationStats(),
    loadModuleHealth(),
    loadIntegrationLogs()
  ])
}

const loadIntegrationStats = async () => {
  try {
    const response = await integrationAPI.getStats()
    if (response.success) {
      const stats = response.data
      updateIntegrationMetric('connected', stats.connectedModules || 0)
      updateIntegrationMetric('apiCalls', stats.apiCallsToday || 0)
      updateIntegrationMetric('healthy', stats.healthyModules || 0)
      updateIntegrationMetric('errors', stats.errorCount || 0)
    }
  } catch (error) {
    console.error('加载集成统计失败:', error)
  }
}

const loadModuleHealth = async () => {
  integration.loading = true
  try {
    const response = await integrationAPI.getModuleHealth()
    if (response.success) {
      moduleHealth.value = response.data
    }
  } catch (error) {
    console.error('加载模块健康状态失败:', error)
  } finally {
    integration.loading = false
  }
}

const loadIntegrationLogs = async () => {
  logs.loading = true
  try {
    const response = await integrationAPI.getIntegrationLogs({
      page: logPagination.current,
      size: logPagination.size,
      level: logFilter.level,
      module: logFilter.module
    })

    if (response.success) {
      integrationLogs.value = response.data.logs || []
      logPagination.total = response.data.total || 0
    }
  } catch (error) {
    console.error('加载集成日志失败:', error)
    // 使用模拟数据
    integrationLogs.value = [
      {
        id: 1,
        timestamp: new Date(),
        level: 'info',
        module: '配置中心',
        message: '配置同步成功'
      },
      {
        id: 2,
        timestamp: new Date(Date.now() - 1000 * 60 * 5),
        level: 'warning',
        module: '导出服务',
        message: '连接超时，正在重试'
      },
      {
        id: 3,
        timestamp: new Date(Date.now() - 1000 * 60 * 10),
        level: 'error',
        module: '导出服务',
        message: '服务连接失败'
      }
    ]
    logPagination.total = integrationLogs.value.length
  } finally {
    logs.loading = false
  }
}

const updateIntegrationMetric = (key, value) => {
  const metric = integrationMetrics.value.find(m => m.key === key)
  if (metric) {
    metric.value = value
  }
}

const refreshIntegrationData = () => {
  loadIntegrationData()
}

const testAllConnections = async () => {
  loading.value = true
  try {
    for (const module of moduleHealth.value) {
      await testModuleConnection(module, false)
    }
    ElMessage.success('所有连接测试完成')
  } catch (error) {
    ElMessage.error('连接测试失败')
  } finally {
    loading.value = false
  }
}

const testModuleConnection = async (module, showMessage = true) => {
  try {
    const response = await integrationAPI.testConnection(module.name)

    if (response.success) {
      module.healthy = true
      module.responseTime = response.data.responseTime
      module.lastCheck = new Date()

      if (showMessage) {
        ElMessage.success(`${module.name} 连接正常`)
      }
    } else {
      module.healthy = false
      module.responseTime = 'N/A'

      if (showMessage) {
        ElMessage.error(`${module.name} 连接失败`)
      }
    }
  } catch (error) {
    module.healthy = false
    module.responseTime = 'N/A'

    if (showMessage) {
      ElMessage.error(`${module.name} 连接测试失败`)
    }
  }
}

const viewModuleLogs = (module) => {
  logFilter.module = module.name
  refreshLogs()
}

const refreshLogs = () => {
  loadIntegrationLogs()
}

const viewLogDetails = (log) => {
  ElMessageBox.alert(log.message, `日志详情 - ${log.module}`, {
    confirmButtonText: '确定'
  })
}

const handleLogPageSizeChange = (size) => {
  logPagination.size = size
  loadIntegrationLogs()
}

const handleLogPageChange = (page) => {
  logPagination.current = page
  loadIntegrationLogs()
}

const formatTime = (time) => {
  if (!time) return 'N/A'
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleString()
}

const getLogLevelType = (level) => {
  const levelTypes = {
    'info': '',
    'warning': 'warning',
    'error': 'danger'
  }
  return levelTypes[level] || ''
}
</script>

<style lang="scss" scoped>
.integration-status {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: white;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

  .header-content {
    .page-title {
      display: flex;
      align-items: center;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      margin: 0 0 8px 0;

      .el-icon {
        margin-right: 12px;
        color: #409EFF;
      }
    }

    .page-description {
      color: #606266;
      margin: 0;
      font-size: 14px;
    }
  }

  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.integration-overview {
  margin-bottom: 24px;

  .metric-card {
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    display: flex;
    align-items: center;
    transition: transform 0.2s ease;

    &:hover {
      transform: translateY(-2px);
    }

    .metric-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      color: white;
    }

    .metric-content {
      .metric-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        line-height: 1;
        margin-bottom: 4px;
      }

      .metric-label {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.module-health {
  margin-bottom: 24px;

  .health-card {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        display: flex;
        align-items: center;
        font-weight: 600;

        .el-icon {
          margin-right: 8px;
          color: #409EFF;
        }
      }

      .health-summary {
        display: flex;
        align-items: baseline;
        gap: 4px;

        .healthy-count {
          font-size: 24px;
          font-weight: 600;
          color: #67C23A;
        }

        .total-count {
          font-size: 18px;
          color: #909399;
        }

        .summary-label {
          font-size: 14px;
          color: #606266;
          margin-left: 8px;
        }
      }
    }

    .module-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
      gap: 16px;

      .module-card {
        border: 1px solid #e4e7ed;
        border-radius: 8px;
        padding: 16px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
        }

        &.healthy {
          border-left: 4px solid #67C23A;
          background: #f0f9ff;
        }

        &.unhealthy {
          border-left: 4px solid #F56C6C;
          background: #fef2f2;
        }

        .module-status {
          display: flex;
          justify-content: flex-end;
          margin-bottom: 12px;

          .status-indicator {
            width: 24px;
            height: 24px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;

            &.status-green {
              background: #f0f9ff;
              color: #67C23A;
            }

            &.status-red {
              background: #fef2f2;
              color: #F56C6C;
            }
          }
        }

        .module-info {
          margin-bottom: 12px;

          .module-name {
            font-size: 16px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 4px;
          }

          .module-description {
            font-size: 13px;
            color: #606266;
            margin-bottom: 4px;
          }

          .module-url {
            font-size: 12px;
            color: #909399;
            font-family: monospace;
          }
        }

        .module-metrics {
          display: flex;
          justify-content: space-between;
          margin-bottom: 12px;

          .metric-item {
            text-align: center;

            .metric-label {
              display: block;
              font-size: 12px;
              color: #909399;
              margin-bottom: 2px;
            }

            .metric-value {
              font-size: 14px;
              font-weight: 500;
              color: #303133;
            }
          }
        }

        .module-actions {
          display: flex;
          gap: 8px;
        }
      }
    }
  }
}

.integration-logs {
  .logs-card {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card-title {
        display: flex;
        align-items: center;
        font-weight: 600;

        .el-icon {
          margin-right: 8px;
          color: #409EFF;
        }
      }

      .log-filters {
        display: flex;
        gap: 8px;
        align-items: center;
      }
    }

    .logs-content {
      .empty-logs {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 200px;
      }

      .log-list {
        .log-item {
          display: grid;
          grid-template-columns: 120px 80px 120px 1fr 60px;
          gap: 12px;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid #f0f0f0;
          font-size: 13px;

          &:last-child {
            border-bottom: none;
          }

          &.log-error {
            background: #fef2f2;
            margin: 0 -16px;
            padding-left: 16px;
            padding-right: 16px;
            border-radius: 4px;
          }

          &.log-warning {
            background: #fdf6ec;
            margin: 0 -16px;
            padding-left: 16px;
            padding-right: 16px;
            border-radius: 4px;
          }

          .log-time {
            color: #909399;
            font-family: monospace;
          }

          .log-module {
            color: #606266;
            font-weight: 500;
          }

          .log-message {
            color: #303133;
          }
        }
      }

      .logs-pagination {
        margin-top: 20px;
        display: flex;
        justify-content: center;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .integration-status {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;

    .header-actions {
      margin-top: 16px;
      justify-content: flex-end;
    }
  }

  .integration-overview {
    .el-col {
      margin-bottom: 12px;
    }
  }

  .module-grid {
    grid-template-columns: 1fr !important;
  }

  .log-item {
    grid-template-columns: 1fr !important;
    gap: 4px !important;

    .log-time, .log-level, .log-module {
      font-size: 12px;
    }
  }
}
</style>