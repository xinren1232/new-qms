<template>
  <div class="ai-conversation-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">AI对话记录管理</h2>
      <p class="page-description">智能分析AI助手对话质量，提供数据洞察和优化建议</p>
    </div>

    <!-- 智能分析概览 -->
    <el-row :gutter="20" class="analytics-overview">
      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon success">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ totalConversations }}</h3>
              <p>总对话数</p>
              <span class="metric-trend positive">+12.5%</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon warning">
              <el-icon><Star /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ averageSatisfaction }}</h3>
              <p>平均满意度</p>
              <span class="metric-trend positive">+0.3</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon info">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ averageResponseTime }}</h3>
              <p>平均响应时间</p>
              <span class="metric-trend negative">+0.2s</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="metric-card">
          <div class="metric-content">
            <div class="metric-icon primary">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="metric-info">
              <h3>{{ completionRate }}%</h3>
              <p>对话完成率</p>
              <span class="metric-trend positive">+2.1%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 配置状态显示 -->
    <el-card class="config-status-card" shadow="never">
      <div class="config-status">
        <div class="status-item">
          <el-icon><Setting /></el-icon>
          <span>配置状态: {{ configStore.loading ? '加载中...' : '已加载' }}</span>
        </div>
        <div class="status-item">
          <el-icon><Clock /></el-icon>
          <span>最后同步: {{ configStore.lastUpdateTime ? formatTime(configStore.lastUpdateTime) : '未同步' }}</span>
        </div>
        <div class="status-item">
          <el-icon><DataBoard /></el-icon>
          <span>数据源: 配置端驱动</span>
        </div>
      </div>
    </el-card>

    <!-- 智能搜索表单 -->
    <el-card class="search-card" shadow="never">
      <template #header>
        <div class="search-header">
          <span>智能搜索与筛选</span>
          <el-button type="text" @click="toggleAdvancedSearch">
            {{ showAdvancedSearch ? '收起高级搜索' : '展开高级搜索' }}
            <el-icon><ArrowDown v-if="!showAdvancedSearch" /><ArrowUp v-else /></el-icon>
          </el-button>
        </div>
      </template>

      <el-form :model="searchForm" inline>
        <!-- 基础搜索 -->
        <el-form-item label="用户ID">
          <el-input v-model="searchForm.userId" placeholder="请输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="对话状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已中断" value="已中断" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <!-- 高级搜索 -->
        <div v-show="showAdvancedSearch" class="advanced-search">
          <el-form-item label="满意度">
            <el-select v-model="searchForm.satisfaction" placeholder="满意度筛选" clearable>
              <el-option label="全部" value="" />
              <el-option label="5星 (优秀)" value="5" />
              <el-option label="4星 (良好)" value="4" />
              <el-option label="3星 (一般)" value="3" />
              <el-option label="2星 (较差)" value="2" />
              <el-option label="1星 (很差)" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="问题类型">
            <el-select v-model="searchForm.questionType" placeholder="问题类型" clearable>
              <el-option label="全部" value="" />
              <el-option label="技术咨询" value="technical" />
              <el-option label="产品询问" value="product" />
              <el-option label="售后服务" value="service" />
              <el-option label="投诉建议" value="complaint" />
            </el-select>
          </el-form-item>
          <el-form-item label="响应时间">
            <el-select v-model="searchForm.responseTime" placeholder="响应时间" clearable>
              <el-option label="全部" value="" />
              <el-option label="< 1秒" value="fast" />
              <el-option label="1-3秒" value="normal" />
              <el-option label="> 3秒" value="slow" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading" icon="Search">
            智能搜索
          </el-button>
          <el-button @click="handleReset" icon="Refresh">重置</el-button>
          <el-button type="success" @click="refreshConfig" icon="Setting">刷新配置</el-button>
          <el-button type="warning" @click="showAnalysisDialog = true" icon="DataAnalysis">
            智能分析
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 工具栏 -->
    <el-card class="toolbar-card" shadow="never">
      <div class="toolbar-content">
        <div class="toolbar-left">
          <span class="data-count">共 {{ pagination.total }} 条数据</span>
          <el-tag v-if="selectedRows.length > 0" type="info" size="small">
            已选择 {{ selectedRows.length }} 项
          </el-tag>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" @click="loadData" :loading="loading" icon="Refresh">刷新</el-button>
          <el-button type="success" icon="Download">导出</el-button>
          <el-button type="warning" icon="DataAnalysis" :disabled="selectedRows.length === 0">批量分析</el-button>
        </div>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="conversationId" label="对话ID" width="150" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="userQuestion" label="用户问题" min-width="300" show-overflow-tooltip />
        <el-table-column prop="conversationStatus" label="对话状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.conversationStatus)" size="small">
              {{ row.conversationStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="satisfactionScore" label="满意度" width="120">
          <template #default="{ row }">
            <el-rate
              :model-value="row.satisfactionScore"
              disabled
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column prop="conversationTime" label="对话时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="text" @click="viewDetail(row)" icon="View">查看</el-button>
            <el-button type="text" @click="analyzeConversation(row)" icon="DataAnalysis">分析</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="对话详情"
      width="800px"
    >
      <div v-if="currentConversation" class="conversation-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="对话ID">
            {{ currentConversation.conversationId }}
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ currentConversation.userId }}
          </el-descriptions-item>
          <el-descriptions-item label="对话状态">
            <el-tag :type="getStatusType(currentConversation.conversationStatus)">
              {{ currentConversation.conversationStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="满意度评分">
            <el-rate
              :model-value="currentConversation.satisfactionScore"
              disabled
              show-score
              text-color="#ff9900"
            />
          </el-descriptions-item>
          <el-descriptions-item label="对话时间" :span="2">
            {{ currentConversation.conversationTime }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="conversation-content">
          <h4>用户问题</h4>
          <div class="question-content">
            {{ currentConversation.userQuestion }}
          </div>

          <h4>AI回答</h4>
          <div class="answer-content">
            {{ currentConversation.aiResponse || '暂无AI回答记录' }}
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button type="primary" @click="analyzeConversation(currentConversation)">分析对话</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 智能分析对话框 -->
    <el-dialog
      v-model="showAnalysisDialog"
      title="AI对话智能分析"
      width="1000px"
      :before-close="handleAnalysisClose"
    >
      <div class="analysis-content">
        <el-tabs v-model="analysisTab" type="border-card">
          <!-- 质量分析 -->
          <el-tab-pane label="对话质量分析" name="quality">
            <div class="analysis-section">
              <h4>质量评估概览</h4>
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="quality-metric">
                    <div class="metric-label">整体质量评分</div>
                    <div class="metric-value excellent">4.2/5.0</div>
                    <div class="metric-desc">基于用户满意度和AI响应质量</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="quality-metric">
                    <div class="metric-label">问题解决率</div>
                    <div class="metric-value good">87.5%</div>
                    <div class="metric-desc">用户问题得到有效解决的比例</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="quality-metric">
                    <div class="metric-label">响应准确率</div>
                    <div class="metric-value excellent">92.3%</div>
                    <div class="metric-desc">AI回答准确性评估</div>
                  </div>
                </el-col>
              </el-row>

              <h4>质量趋势分析</h4>
              <div class="chart-placeholder">
                <el-empty description="质量趋势图表 (集成图表库后显示)" />
              </div>
            </div>
          </el-tab-pane>

          <!-- 用户行为分析 -->
          <el-tab-pane label="用户行为分析" name="behavior">
            <div class="analysis-section">
              <h4>用户活跃度分析</h4>
              <el-row :gutter="20">
                <el-col :span="12">
                  <div class="behavior-metric">
                    <h5>高频用户 (Top 10)</h5>
                    <el-table :data="topUsers" size="small">
                      <el-table-column prop="userId" label="用户ID" width="100" />
                      <el-table-column prop="conversations" label="对话次数" width="80" />
                      <el-table-column prop="satisfaction" label="满意度" width="80" />
                    </el-table>
                  </div>
                </el-col>
                <el-col :span="12">
                  <div class="behavior-metric">
                    <h5>问题类型分布</h5>
                    <div class="chart-placeholder">
                      <el-empty description="问题类型饼图 (集成图表库后显示)" />
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>

          <!-- 优化建议 -->
          <el-tab-pane label="智能优化建议" name="suggestions">
            <div class="analysis-section">
              <h4>AI助手优化建议</h4>
              <div class="suggestions-list">
                <el-alert
                  title="响应时间优化"
                  description="检测到部分对话响应时间较长，建议优化AI模型推理速度或增加缓存机制"
                  type="warning"
                  :closable="false"
                  show-icon
                />
                <el-alert
                  title="知识库完善"
                  description="发现用户在'产品功能'类问题上满意度较低，建议补充相关知识库内容"
                  type="info"
                  :closable="false"
                  show-icon
                />
                <el-alert
                  title="个性化推荐"
                  description="基于用户行为分析，建议为高频用户提供个性化的对话体验"
                  type="success"
                  :closable="false"
                  show-icon
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAnalysisDialog = false">关闭</el-button>
          <el-button type="primary" @click="exportAnalysisReport">导出分析报告</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useConfigStore } from '@/stores/config'
import { getBusinessData } from '@/api/config'
import { formatDate } from '@/utils/format'

const route = useRoute()
const configStore = useConfigStore()

// 模块配置key
const moduleKey = 'aiConversation'

// 响应式数据
const loading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const currentConversation = ref(null)
const detailVisible = ref(false)

// 搜索表单数据
const searchForm = ref({
  userId: '',
  status: '',
  dateRange: [],
  satisfaction: '',
  questionType: '',
  responseTime: ''
})

// 界面控制
const showAdvancedSearch = ref(false)
const showAnalysisDialog = ref(false)
const analysisTab = ref('quality')

// 分页数据
const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

// 智能分析数据
const totalConversations = ref(1234)
const averageSatisfaction = ref(4.2)
const averageResponseTime = ref('1.8s')
const completionRate = ref(87.5)

// 分析数据
const topUsers = ref([
  { userId: 'user001', conversations: 45, satisfaction: 4.8 },
  { userId: 'user002', conversations: 38, satisfaction: 4.5 },
  { userId: 'user003', conversations: 32, satisfaction: 4.2 },
  { userId: 'user004', conversations: 28, satisfaction: 4.6 },
  { userId: 'user005', conversations: 25, satisfaction: 4.1 }
])

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知'
  return formatDate(timeStr, 'MM-DD HH:mm')
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    '进行中': 'warning',
    '已完成': 'success',
    '已中断': 'info'
  }
  return typeMap[status] || 'info'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.value.current,
      size: pagination.value.size,
      ...searchForm.value
    }

    const response = await getBusinessData(moduleKey, params)

    if (response && response.success) {
      tableData.value = response.data?.records || []
      pagination.value.total = response.data?.total || 0
      ElMessage.success(`加载了 ${tableData.value.length} 条数据`)
    } else {
      ElMessage.warning('暂无数据')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 刷新配置
const refreshConfig = async () => {
  try {
    await configStore.initializeConfig()
    ElMessage.success('配置刷新成功')
    loadData()
  } catch (error) {
    console.error('刷新配置失败:', error)
    ElMessage.error('刷新配置失败')
  }
}

// 搜索处理
const handleSearch = () => {
  pagination.value.current = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    userId: '',
    status: '',
    dateRange: [],
    satisfaction: '',
    questionType: '',
    responseTime: ''
  }
  pagination.value.current = 1
  loadData()
}

// 切换高级搜索
const toggleAdvancedSearch = () => {
  showAdvancedSearch.value = !showAdvancedSearch.value
}

// 关闭分析对话框
const handleAnalysisClose = () => {
  showAnalysisDialog.value = false
}

// 导出分析报告
const exportAnalysisReport = () => {
  ElMessage.success('分析报告导出中...')
  // 这里可以调用导出API
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 分页变化
const handlePageChange = (page) => {
  pagination.value.current = page
  loadData()
}

// 页面大小变化
const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.current = 1
  loadData()
}

// 查看详情
const viewDetail = (row) => {
  currentConversation.value = row
  detailVisible.value = true
}

// 分析对话
const analyzeConversation = (row) => {
  console.log('分析对话:', row)
  ElMessage.success(`开始分析对话 ${row.conversationId}...`)
}

// 监听配置变化
watch(
  () => configStore.configs[moduleKey],
  (newConfig) => {
    if (newConfig) {
      console.log('🔄 AI对话记录页面配置已更新:', newConfig)
      // 配置更新后重新加载数据
      loadData()
    }
  },
  { deep: true }
)

// 组件挂载
onMounted(() => {
  // 初始化配置
  configStore.initializeConfig()

  // 等待配置加载完成后再加载数据
  const unwatch = watch(
    () => configStore.loading,
    (loading) => {
      if (!loading) {
        loadData()
        unwatch() // 取消监听
      }
    },
    { immediate: true }
  )
})
</script>

<style lang="scss" scoped>
.ai-conversation-page {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 500;
    color: #303133;
  }

  .page-description {
    margin: 0;
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
  }
}

.analytics-overview {
  margin-bottom: 20px;
}

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
      color: white;
      font-size: 24px;

      &.success {
        background: linear-gradient(135deg, #67c23a, #85ce61);
      }

      &.warning {
        background: linear-gradient(135deg, #e6a23c, #ebb563);
      }

      &.info {
        background: linear-gradient(135deg, #909399, #a6a9ad);
      }

      &.primary {
        background: linear-gradient(135deg, #409eff, #66b1ff);
      }
    }

    .metric-info {
      h3 {
        margin: 0 0 4px 0;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
      }

      p {
        margin: 0 0 4px 0;
        font-size: 14px;
        color: #606266;
      }

      .metric-trend {
        font-size: 12px;

        &.positive {
          color: #67c23a;
        }

        &.negative {
          color: #f56c6c;
        }
      }
    }
  }
}

.config-status-card {
  margin-bottom: 16px;

  .config-status {
    display: flex;
    gap: 24px;
    align-items: center;

    .status-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: #606266;

      .el-icon {
        color: #409EFF;
      }
    }
  }
}

.search-card {
  margin-bottom: 16px;

  .search-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .advanced-search {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
  }
}

.toolbar-card,
.table-card {
  margin-bottom: 16px;

  :deep(.el-card__body) {
    padding: 16px;
  }
}

.toolbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .data-count {
      font-size: 14px;
      color: #606266;
    }
  }

  .toolbar-right {
    display: flex;
    gap: 8px;
  }
}

.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.pagination-wrapper {
  padding: 16px;
  text-align: right;
  border-top: 1px solid #ebeef5;
}

.conversation-detail {
  .conversation-content {
    margin-top: 24px;

    h4 {
      margin: 16px 0 8px 0;
      color: #303133;
    }

    .question-content,
    .answer-content {
      padding: 12px;
      background-color: #f5f7fa;
      border-radius: 4px;
      line-height: 1.6;
      color: #606266;
      margin-bottom: 16px;
    }
  }
}

.analysis-content {
  .analysis-section {
    h4 {
      margin: 0 0 16px 0;
      color: #303133;
      font-size: 16px;
    }

    h5 {
      margin: 0 0 12px 0;
      color: #606266;
      font-size: 14px;
    }
  }

  .quality-metric {
    text-align: center;
    padding: 16px;
    border: 1px solid #ebeef5;
    border-radius: 4px;

    .metric-label {
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
    }

    .metric-value {
      font-size: 24px;
      font-weight: 600;
      margin-bottom: 4px;

      &.excellent {
        color: #67c23a;
      }

      &.good {
        color: #409eff;
      }

      &.average {
        color: #e6a23c;
      }

      &.poor {
        color: #f56c6c;
      }
    }

    .metric-desc {
      font-size: 12px;
      color: #909399;
    }
  }

  .behavior-metric {
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 16px;
  }

  .chart-placeholder {
    height: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #fafafa;
    border-radius: 4px;
  }

  .suggestions-list {
    .el-alert {
      margin-bottom: 12px;
    }
  }
}

@media (max-width: 768px) {
  .toolbar-content {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .config-status {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .pagination-wrapper {
    text-align: center;
  }
}
</style>
