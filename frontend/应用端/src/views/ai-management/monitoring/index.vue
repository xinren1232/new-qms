<template>
  <div class="ai-monitoring">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h2>AI问答监控</h2>
        <p>实时监控AI问答系统的使用情况和质量指标</p>
      </div>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="YYYY-MM-DD HH:mm:ss"
          value-format="YYYY-MM-DD HH:mm:ss"
          @change="handleDateRangeChange"
          class="date-picker"
        />
        <el-button type="primary" @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><ChatDotRound /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.total_chats || 0 }}</div>
          <div class="stat-label">总问答数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon users">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ overallStats.unique_users || 0 }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon success">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ successRate }}%</div>
          <div class="stat-label">成功率</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon satisfaction">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ satisfactionRate }}%</div>
          <div class="stat-label">满意度</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3>使用趋势</h3>
            </div>
            <div class="chart-content">
              <div ref="usageTrendChart" class="chart"></div>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3>满意度趋势</h3>
            </div>
            <div class="chart-content">
              <div ref="satisfactionChart" class="chart"></div>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="8">
          <div class="chart-card">
            <div class="chart-header">
              <h3>模型使用分布</h3>
            </div>
            <div class="chart-content">
              <div ref="modelDistributionChart" class="chart"></div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="chart-card">
            <div class="chart-header">
              <h3>响应时间分布</h3>
            </div>
            <div class="chart-content">
              <div ref="responseTimeChart" class="chart"></div>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="chart-card">
            <div class="chart-header">
              <h3>反馈类型分布</h3>
            </div>
            <div class="chart-content">
              <div ref="feedbackChart" class="chart"></div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 详细数据表格 -->
    <div class="tables-section">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="问答记录" name="chat-records">
          <div class="table-card">
            <div class="table-header">
              <div class="table-filters">
                <el-input
                  v-model="chatFilters.userId"
                  placeholder="用户ID"
                  clearable
                  style="width: 150px; margin-right: 10px;"
                />
                <el-select
                  v-model="chatFilters.modelProvider"
                  placeholder="模型提供商"
                  clearable
                  style="width: 150px; margin-right: 10px;"
                >
                  <el-option label="Transsion AI" value="transsion" />
                  <el-option label="OpenAI" value="openai" />
                  <el-option label="Claude" value="claude" />
                </el-select>
                <el-select
                  v-model="chatFilters.chatStatus"
                  placeholder="状态"
                  clearable
                  style="width: 120px; margin-right: 10px;"
                >
                  <el-option label="成功" value="SUCCESS" />
                  <el-option label="失败" value="FAILED" />
                  <el-option label="超时" value="TIMEOUT" />
                </el-select>
                <el-button type="primary" @click="loadChatRecords">查询</el-button>
              </div>
            </div>
            <el-table
              :data="chatRecords"
              v-loading="chatLoading"
              style="width: 100%"
              max-height="400"
            >
              <el-table-column prop="userId" label="用户ID" width="120" />
              <el-table-column prop="userMessage" label="用户问题" min-width="200" show-overflow-tooltip />
              <el-table-column prop="modelProvider" label="模型" width="120" />
              <el-table-column prop="responseTime" label="响应时间" width="100">
                <template #default="{ row }">
                  {{ row.responseTime }}ms
                </template>
              </el-table-column>
              <el-table-column prop="chatStatus" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.chatStatus)">
                    {{ getStatusText(row.chatStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdTime" label="时间" width="160" />
              <el-table-column label="操作" width="120">
                <template #default="{ row }">
                  <el-button type="text" @click="viewChatDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="table-pagination">
              <el-pagination
                v-model:current-page="chatPagination.current"
                v-model:page-size="chatPagination.size"
                :total="chatPagination.total"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="loadChatRecords"
                @current-change="loadChatRecords"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="用户统计" name="user-stats">
          <div class="table-card">
            <el-table
              :data="userStats"
              v-loading="userStatsLoading"
              style="width: 100%"
              max-height="400"
            >
              <el-table-column prop="user_id" label="用户ID" width="120" />
              <el-table-column prop="count" label="问答次数" width="100" />
              <el-table-column prop="like_count" label="点赞数" width="80" />
              <el-table-column prop="dislike_count" label="点踩数" width="80" />
              <el-table-column prop="total_feedback" label="总反馈" width="80" />
              <el-table-column label="满意度" width="100">
                <template #default="{ row }">
                  {{ calculateSatisfactionRate(row.like_count, row.dislike_count) }}%
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="模型统计" name="model-stats">
          <div class="table-card">
            <el-table
              :data="modelStats"
              v-loading="modelStatsLoading"
              style="width: 100%"
              max-height="400"
            >
              <el-table-column prop="model_provider" label="提供商" width="120" />
              <el-table-column prop="model_name" label="模型名称" width="150" />
              <el-table-column prop="count" label="使用次数" width="100" />
              <el-table-column prop="like_count" label="点赞数" width="80" />
              <el-table-column prop="dislike_count" label="点踩数" width="80" />
              <el-table-column label="满意度" width="100">
                <template #default="{ row }">
                  {{ calculateSatisfactionRate(row.like_count, row.dislike_count) }}%
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 聊天详情对话框 -->
    <el-dialog
      v-model="chatDetailVisible"
      title="问答详情"
      width="60%"
      :before-close="closeChatDetail"
    >
      <div v-if="selectedChat" class="chat-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户ID">{{ selectedChat.userId }}</el-descriptions-item>
            <el-descriptions-item label="会话ID">{{ selectedChat.conversationId }}</el-descriptions-item>
            <el-descriptions-item label="模型">{{ selectedChat.modelProvider }} - {{ selectedChat.modelName }}</el-descriptions-item>
            <el-descriptions-item label="响应时间">{{ selectedChat.responseTime }}ms</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(selectedChat.chatStatus)">
                {{ getStatusText(selectedChat.chatStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="时间">{{ selectedChat.createdTime }}</el-descriptions-item>
          </el-descriptions>
        </div>
        <div class="detail-section">
          <h4>用户问题</h4>
          <div class="message-content">{{ selectedChat.userMessage }}</div>
        </div>
        <div class="detail-section">
          <h4>AI回答</h4>
          <div class="message-content">{{ selectedChat.aiResponse }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  User,
  CircleCheck,
  CopyDocument,
  Refresh,
  Monitor
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import {
  getOverallStatistics,
  getRequestTrend,
  getResponseTimeTrend,
  getSystemResources,
  getErrorAnalysis,
  getApiPerformance
} from '@/api/monitoring'

// 响应式数据
const loading = ref(false)
const dateRange = ref([])
const activeTab = ref('chat-records')

// 总体统计数据
const overallStats = ref({})

// 图表实例
const charts = reactive({
  usageTrend: null,
  satisfaction: null,
  modelDistribution: null,
  responseTime: null,
  feedback: null
})

// 问答记录相关
const chatRecords = ref([])
const chatLoading = ref(false)
const chatFilters = reactive({
  userId: '',
  modelProvider: '',
  chatStatus: ''
})
const chatPagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 监控数据
const requestTrendData = ref([])
const systemResources = ref({})
const apiPerformanceData = ref([])
const errorAnalysisData = ref([])

// 用户统计（保留用于兼容性）
const userStats = ref([])
const userStatsLoading = ref(false)

// 模型统计（保留用于兼容性）
const modelStats = ref([])
const modelStatsLoading = ref(false)

// 聊天详情
const chatDetailVisible = ref(false)
const selectedChat = ref(null)

// 计算属性
const successRate = computed(() => {
  const total = overallStats.value.total_chats || 0
  const successful = overallStats.value.successful_chats || 0
  return total > 0 ? Math.round((successful / total) * 100) : 0
})

const satisfactionRate = computed(() => {
  const likes = overallStats.value.total_likes || 0
  const dislikes = overallStats.value.total_dislikes || 0
  const total = likes + dislikes
  return total > 0 ? Math.round((likes / total) * 100) : 0
})

// 方法
const handleDateRangeChange = () => {
  refreshData()
}

const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadOverallStats(),
      loadRequestTrend(),
      loadSystemResources(),
      loadApiPerformance(),
      loadErrorAnalysis()
    ])
    await nextTick()
    initCharts()
  } catch (error) {
    console.error('刷新数据失败:', error)
    ElMessage.error('刷新数据失败')
  } finally {
    loading.value = false
  }
}

const loadOverallStats = async () => {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const response = await getOverallStatistics(params)
    if (response.success) {
      overallStats.value = response.data
    } else {
      throw new Error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取总体统计失败:', error)
    // 使用模拟数据作为后备
    overallStats.value = {
      total_chats: 1250,
      unique_users: 85,
      successful_chats: 1180,
      total_likes: 890,
      total_dislikes: 120
    }
  }
}

const loadRequestTrend = async () => {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const response = await getRequestTrend(params)
    if (response.success) {
      requestTrendData.value = response.data
    } else {
      throw new Error(response.message || '获取请求趋势失败')
    }
  } catch (error) {
    console.error('获取请求趋势失败:', error)
    // 使用模拟数据作为后备
    requestTrendData.value = []
  }
}

const loadSystemResources = async () => {
  try {
    const response = await getSystemResources()
    if (response.success) {
      systemResources.value = response.data
    } else {
      throw new Error(response.message || '获取系统资源失败')
    }
  } catch (error) {
    console.error('获取系统资源失败:', error)
    // 使用模拟数据作为后备
    systemResources.value = {
      cpu: '45.2',
      memory: '68.5',
      disk: '32.1',
      network: '125.8'
    }
  }
}

const loadApiPerformance = async () => {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const response = await getApiPerformance(params)
    if (response.success) {
      apiPerformanceData.value = response.data
    } else {
      throw new Error(response.message || '获取API性能失败')
    }
  } catch (error) {
    console.error('获取API性能失败:', error)
    // 使用模拟数据作为后备
    apiPerformanceData.value = []
  }
}

const loadErrorAnalysis = async () => {
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const response = await getErrorAnalysis(params)
    if (response.success) {
      errorAnalysisData.value = response.data
    } else {
      throw new Error(response.message || '获取错误分析失败')
    }
  } catch (error) {
    console.error('获取错误分析失败:', error)
    // 使用模拟数据作为后备
    errorAnalysisData.value = []
  }
}

const loadChatRecords = async () => {
  chatLoading.value = true
  try {
    const params = {
      current: chatPagination.current,
      size: chatPagination.size,
      ...chatFilters
    }

    if (dateRange.value && dateRange.value.length === 2) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }

    const response = await getChatRecords(params)
    if (response.success) {
      chatRecords.value = response.data.records || []
      chatPagination.total = response.data.total || 0
    } else {
      throw new Error(response.message || '获取问答记录失败')
    }
  } catch (error) {
    console.error('获取问答记录失败:', error)
    // 使用模拟数据作为后备
    chatRecords.value = [
      {
        id: '1',
        userId: 'user001',
        userMessage: '如何提升产品质量？',
        aiResponse: '提升产品质量可以从以下几个方面入手...',
        modelProvider: 'transsion',
        modelName: 'gemini-2.5-pro',
        responseTime: 1250,
        chatStatus: 'SUCCESS',
        createdTime: '2025-01-31 14:30:00'
      }
    ]
    chatPagination.total = 1250
  } finally {
    chatLoading.value = false
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'chat-records') {
    // 暂时移除，因为我们专注于监控数据
    console.log('切换到问答记录标签')
  } else if (tabName === 'user-stats') {
    loadRequestTrend()
  } else if (tabName === 'model-stats') {
    loadApiPerformance()
  }
}

const viewChatDetail = (row) => {
  selectedChat.value = row
  chatDetailVisible.value = true
}

const closeChatDetail = () => {
  chatDetailVisible.value = false
  selectedChat.value = null
}

const getStatusType = (status) => {
  const types = {
    'SUCCESS': 'success',
    'FAILED': 'danger',
    'TIMEOUT': 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'TIMEOUT': '超时'
  }
  return texts[status] || '未知'
}

const calculateSatisfactionRate = (likes, dislikes) => {
  const total = (likes || 0) + (dislikes || 0)
  return total > 0 ? Math.round(((likes || 0) / total) * 100) : 0
}

const initCharts = () => {
  // 初始化图表
  initUsageTrendChart()
  initSatisfactionChart()
  initModelDistributionChart()
  initResponseTimeChart()
  initFeedbackChart()
}

const initUsageTrendChart = () => {
  const chartDom = document.querySelector('.chart-content .chart')
  if (!chartDom) return
  
  if (charts.usageTrend) {
    charts.usageTrend.dispose()
  }
  
  charts.usageTrend = echarts.init(chartDom)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: [20, 15, 45, 80, 65, 35],
      type: 'line',
      smooth: true,
      areaStyle: {}
    }]
  }
  
  charts.usageTrend.setOption(option)
}

// 其他图表初始化方法...
const initSatisfactionChart = () => {
  // TODO: 实现满意度趋势图表
}

const initModelDistributionChart = () => {
  // TODO: 实现模型分布饼图
}

const initResponseTimeChart = () => {
  // TODO: 实现响应时间分布图表
}

const initFeedbackChart = () => {
  // TODO: 实现反馈类型分布图表
}

// 组件挂载
onMounted(() => {
  // 设置默认时间范围为最近7天
  const now = new Date()
  const sevenDaysAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
  dateRange.value = [
    sevenDaysAgo.toISOString().slice(0, 19).replace('T', ' '),
    now.toISOString().slice(0, 19).replace('T', ' ')
  ]

  refreshData()
})
</script>

<style scoped>
.ai-monitoring {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content h2 {
  margin: 0 0 8px 0;
  color: #1f2937;
  font-size: 24px;
  font-weight: 600;
}

.header-content p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.date-picker {
  width: 350px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.users {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.success {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.satisfaction {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.charts-section {
  margin-bottom: 24px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chart-header {
  padding: 20px 24px 0;
  border-bottom: 1px solid #e5e7eb;
}

.chart-header h3 {
  margin: 0 0 16px 0;
  color: #1f2937;
  font-size: 18px;
  font-weight: 600;
}

.chart-content {
  padding: 20px;
}

.chart {
  width: 100%;
  height: 300px;
}

.tables-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-card {
  padding: 0;
}

.table-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.table-filters {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.table-pagination {
  padding: 16px 24px;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  display: flex;
  justify-content: flex-end;
}

.chat-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  color: #1f2937;
  font-size: 16px;
  font-weight: 600;
}

.message-content {
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  line-height: 1.6;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .ai-monitoring {
    padding: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .date-picker {
    width: 100%;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .table-filters {
    flex-direction: column;
    align-items: stretch;
  }

  .table-filters > * {
    width: 100% !important;
  }
}

/* 深色模式支持 */
@media (prefers-color-scheme: dark) {
  .ai-monitoring {
    background-color: #111827;
  }

  .page-header,
  .stat-card,
  .chart-card,
  .tables-section {
    background: #1f2937;
    border-color: #374151;
  }

  .header-content h2,
  .stat-value,
  .chart-header h3 {
    color: #f9fafb;
  }

  .header-content p,
  .stat-label {
    color: #9ca3af;
  }

  .table-header,
  .table-pagination {
    background: #111827;
    border-color: #374151;
  }

  .message-content {
    background: #111827;
    border-color: #374151;
    color: #d1d5db;
  }
}
</style>
