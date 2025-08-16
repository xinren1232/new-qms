<template>
  <div class="rating-stats-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>⭐ AI回答评分统计</h1>
        <p>查看和分析AI回答的用户评分数据</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="refreshStats">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 总体统计卡片 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon rating">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ (overallStats.avg_rating || 0).toFixed(1) }}</div>
              <div class="stat-label">平均评分</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon messages">
              <el-icon><ChatLineRound /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ overallStats.total_rated || 0 }}</div>
              <div class="stat-label">已评分消息</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon rate">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ (overallStats.rating_rate || 0).toFixed(1) }}%</div>
              <div class="stat-label">评分率</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon total">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ overallStats.total_messages || 0 }}</div>
              <div class="stat-label">总消息数</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 评分分布图表 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>评分分布</span>
              </div>
            </template>
            <div class="rating-distribution">
              <div v-for="(count, rating) in overallStats.rating_distribution" :key="rating" class="rating-bar">
                <div class="rating-label">
                  <el-rate :model-value="parseInt(rating)" :max="5" disabled size="small" />
                  <span class="rating-text">{{ rating }}星</span>
                </div>
                <div class="rating-progress">
                  <el-progress 
                    :percentage="getPercentage(count, overallStats.total_rated)" 
                    :color="getRatingColor(rating)"
                    :show-text="false"
                  />
                  <span class="rating-count">{{ count }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <span>按模型统计</span>
              </div>
            </template>
            <div class="model-stats">
              <div v-if="modelStats.length === 0" class="no-data">
                <el-empty description="暂无数据" />
              </div>
              <div v-else>
                <div v-for="model in modelStats" :key="model.model_provider" class="model-item">
                  <div class="model-header">
                    <el-tag :type="getModelTagType(model.model_provider)">
                      {{ model.model_provider }}
                    </el-tag>
                    <div class="model-rating">
                      <el-rate :model-value="model.avg_rating || 0" :max="5" disabled show-score />
                    </div>
                  </div>
                  <div class="model-details">
                    <span>已评分: {{ model.total_rated }}</span>
                    <span>总消息: {{ model.total_messages }}</span>
                    <span>评分率: {{ ((model.total_rated / model.total_messages) * 100).toFixed(1) }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选和详细数据 -->
    <div class="detailed-stats">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>详细统计</span>
            <div class="header-filters">
              <el-select v-model="filterModel" placeholder="筛选模型" clearable @change="loadStats">
                <el-option label="全部模型" value="" />
                <el-option label="GPT-4o" value="GPT-4o" />
                <el-option label="DeepSeek Chat" value="DeepSeek Chat (V3-0324)" />
                <el-option label="DeepSeek Reasoner" value="DeepSeek Reasoner (R1-0528)" />
                <el-option label="Claude 3.7 Sonnet" value="Claude 3.7 Sonnet" />
                <el-option label="Gemini 2.5 Pro" value="Gemini 2.5 Pro Thinking" />
              </el-select>
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="loadStats"
              />
            </div>
          </div>
        </template>
        
        <div class="daily-stats">
          <el-table :data="dailyStats" style="width: 100%" v-loading="loading">
            <el-table-column prop="rating_date" label="日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.rating_date) }}
              </template>
            </el-table-column>
            <el-table-column prop="model_provider" label="模型" width="200">
              <template #default="{ row }">
                <el-tag :type="getModelTagType(row.model_provider)">
                  {{ row.model_provider }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="avg_rating" label="平均评分" width="120">
              <template #default="{ row }">
                <el-rate :model-value="row.avg_rating || 0" :max="5" disabled show-score />
              </template>
            </el-table-column>
            <el-table-column prop="total_rated" label="已评分" width="100" />
            <el-table-column prop="total_messages" label="总消息" width="100" />
            <el-table-column label="评分率" width="100">
              <template #default="{ row }">
                {{ ((row.total_rated / row.total_messages) * 100).toFixed(1) }}%
              </template>
            </el-table-column>
            <el-table-column label="评分分布">
              <template #default="{ row }">
                <div class="rating-mini-distribution">
                  <span v-for="i in 5" :key="i" class="mini-rating">
                    {{ i }}⭐: {{ row[`rating_${i}_count`] || 0 }}
                  </span>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, Star, ChatLineRound, TrendCharts, Document 
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

// 认证store
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const overallStats = ref({
  avg_rating: 0,
  rating_distribution: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 },
  total_rated: 0,
  total_messages: 0,
  rating_rate: 0
})
const modelStats = ref([])
const dailyStats = ref([])
const filterModel = ref('')
const dateRange = ref([])

// 计算属性
const ratingColors = ['#F56C6C', '#E6A23C', '#F7BA2A', '#67C23A', '#67C23A']

// 页面加载时初始化
onMounted(() => {
  loadStats()
})

// 加载统计数据
const loadStats = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    
    if (filterModel.value) {
      params.append('model_provider', filterModel.value)
    }
    
    if (dateRange.value && dateRange.value.length === 2) {
      params.append('date_range', `${dateRange.value[0].toISOString().split('T')[0]},${dateRange.value[1].toISOString().split('T')[0]}`)
    }

    const headers = {}
    const user = authStore.user
    if (user && user.id) {
      headers['user-id'] = user.id
      headers['user-name'] = user.username || ''
      headers['user-email'] = user.email || ''
      headers['user-department'] = user.department || ''
      headers['user-role'] = user.role || ''
    }

    const response = await fetch(`/api/chat/ratings/stats?${params}`, {
      headers: headers
    })
    const result = await response.json()
    
    if (result.success) {
      overallStats.value = result.data.overall
      modelStats.value = result.data.by_model
      dailyStats.value = result.data.daily_stats
    } else {
      ElMessage.error('加载统计数据失败')
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 刷新统计数据
const refreshStats = () => {
  loadStats()
}

// 获取百分比
const getPercentage = (count, total) => {
  return total > 0 ? (count / total) * 100 : 0
}

// 获取评分颜色
const getRatingColor = (rating) => {
  const colors = {
    1: '#F56C6C',
    2: '#E6A23C', 
    3: '#F7BA2A',
    4: '#67C23A',
    5: '#67C23A'
  }
  return colors[rating] || '#909399'
}

// 获取模型标签类型
const getModelTagType = (modelProvider) => {
  if (modelProvider.includes('DeepSeek')) return 'success'
  if (modelProvider.includes('GPT')) return 'primary'
  if (modelProvider.includes('Claude')) return 'warning'
  if (modelProvider.includes('Gemini')) return 'info'
  return 'default'
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '未知'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}
</script>

<style lang="scss" scoped>
.rating-stats-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

// 页面头部
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .header-left {
    h1 {
      margin: 0 0 8px 0;
      color: #303133;
      font-size: 24px;
    }

    p {
      margin: 0;
      color: #606266;
      font-size: 14px;
    }
  }
}

// 统计卡片
.stats-overview {
  margin-bottom: 20px;

  .stat-card {
    background: white;
    border-radius: 8px;
    padding: 20px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      font-size: 24px;

      &.rating {
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        color: #e67e22;
      }

      &.messages {
        background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
        color: #2c3e50;
      }

      &.rate {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }

      &.total {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: white;
      }
    }

    .stat-content {
      .stat-number {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
        line-height: 1;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

// 图表区域
.charts-section {
  margin-bottom: 20px;

  .chart-card {
    height: 400px;

    .card-header {
      font-weight: 600;
      color: #303133;
    }

    .rating-distribution {
      .rating-bar {
        display: flex;
        align-items: center;
        margin-bottom: 16px;

        .rating-label {
          width: 120px;
          display: flex;
          align-items: center;
          gap: 8px;

          .rating-text {
            font-size: 14px;
            color: #606266;
          }
        }

        .rating-progress {
          flex: 1;
          display: flex;
          align-items: center;
          gap: 12px;

          .rating-count {
            font-weight: 600;
            color: #303133;
            min-width: 30px;
          }
        }
      }
    }

    .model-stats {
      .model-item {
        padding: 16px;
        border: 1px solid #e4e7ed;
        border-radius: 6px;
        margin-bottom: 12px;

        .model-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
        }

        .model-details {
          display: flex;
          gap: 16px;
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}

// 详细统计
.detailed-stats {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-filters {
      display: flex;
      gap: 12px;
    }
  }

  .daily-stats {
    .rating-mini-distribution {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .mini-rating {
        font-size: 11px;
        color: #909399;
        background: #f5f7fa;
        padding: 2px 6px;
        border-radius: 3px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .rating-stats-container {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .stats-overview {
    .el-col {
      margin-bottom: 12px;
    }
  }

  .charts-section {
    .el-col {
      margin-bottom: 20px;
    }
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;

    .header-filters {
      width: 100%;
      flex-direction: column;
    }
  }
}
</style>
