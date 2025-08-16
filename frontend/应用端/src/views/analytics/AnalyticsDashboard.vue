<template>
  <div class="analytics-dashboard">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><DataAnalysis /></el-icon>
          智能分析
        </h1>
        <p class="page-description">深度分析您的对话数据，发现质量管理洞察</p>
      </div>
      <div class="header-actions">
        <el-button @click="refreshData" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="primary" @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
    </div>

    <!-- 概览指标 -->
    <el-row :gutter="20" class="overview-metrics">
      <el-col :xs="12" :sm="6" v-for="metric in overviewMetrics" :key="metric.key">
        <div class="metric-card">
          <div class="metric-icon" :style="{ backgroundColor: metric.color }">
            <el-icon :size="24"><component :is="metric.icon" /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ metric.value }}</div>
            <div class="metric-label">{{ metric.label }}</div>
            <div class="metric-trend" :class="metric.trend">
              <el-icon><component :is="metric.trendIcon" /></el-icon>
              {{ metric.change }}
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 分析图表区域 -->
    <el-row :gutter="20" class="analysis-charts">
      <!-- 主题分析 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card" v-loading="analytics.loading">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><PieChart /></el-icon>
                主题分析
              </span>
              <el-dropdown @command="handleTopicAction">
                <el-button text>
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="export">导出数据</el-dropdown-item>
                    <el-dropdown-item command="detail">查看详情</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </template>
          
          <div class="chart-container">
            <div ref="topicChartRef" class="chart" style="height: 300px;"></div>
            
            <!-- 主题列表 -->
            <div class="topic-list">
              <div 
                v-for="(topic, index) in analytics.topics.slice(0, 6)" 
                :key="topic.topic"
                class="topic-item"
              >
                <div class="topic-rank">{{ index + 1 }}</div>
                <div class="topic-info">
                  <div class="topic-name">{{ topic.topic }}</div>
                  <div class="topic-count">{{ topic.count }}个对话</div>
                </div>
                <div class="topic-progress">
                  <el-progress 
                    :percentage="(topic.count / analytics.topics[0]?.count * 100) || 0" 
                    :show-text="false"
                    :stroke-width="6"
                  />
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 情感分析 -->
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card" v-loading="analytics.loading">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Sunny /></el-icon>
                情感分析
              </span>
            </div>
          </template>
          
          <div class="sentiment-analysis">
            <div class="sentiment-overview">
              <div class="sentiment-item positive">
                <div class="sentiment-icon">😊</div>
                <div class="sentiment-data">
                  <div class="sentiment-percentage">{{ analytics.sentiment.positive }}%</div>
                  <div class="sentiment-label">积极</div>
                </div>
              </div>
              
              <div class="sentiment-item neutral">
                <div class="sentiment-icon">😐</div>
                <div class="sentiment-data">
                  <div class="sentiment-percentage">{{ analytics.sentiment.neutral }}%</div>
                  <div class="sentiment-label">中性</div>
                </div>
              </div>
              
              <div class="sentiment-item negative">
                <div class="sentiment-icon">😔</div>
                <div class="sentiment-data">
                  <div class="sentiment-percentage">{{ analytics.sentiment.negative }}%</div>
                  <div class="sentiment-label">消极</div>
                </div>
              </div>
            </div>
            
            <div class="sentiment-chart">
              <div ref="sentimentChartRef" style="height: 200px;"></div>
            </div>
            
            <div class="sentiment-insight">
              <el-alert
                :title="getSentimentInsight()"
                type="info"
                :closable="false"
                show-icon
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户行为分析 -->
    <el-row :gutter="20" class="behavior-analysis">
      <el-col :span="24">
        <el-card class="chart-card" v-loading="analytics.loading">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><User /></el-icon>
                用户行为分析
              </span>
            </div>
          </template>
          
          <el-row :gutter="20">
            <!-- 行为统计 -->
            <el-col :xs="24" :md="8">
              <div class="behavior-stats">
                <div class="stat-grid">
                  <div class="stat-item">
                    <div class="stat-icon">💬</div>
                    <div class="stat-info">
                      <div class="stat-value">{{ analytics.behavior.total_conversations || 0 }}</div>
                      <div class="stat-label">总对话数</div>
                    </div>
                  </div>
                  
                  <div class="stat-item">
                    <div class="stat-icon">📝</div>
                    <div class="stat-info">
                      <div class="stat-value">{{ analytics.behavior.total_messages || 0 }}</div>
                      <div class="stat-label">总消息数</div>
                    </div>
                  </div>
                  
                  <div class="stat-item">
                    <div class="stat-icon">⭐</div>
                    <div class="stat-info">
                      <div class="stat-value">{{ (analytics.behavior.rating_patterns?.avg_rating || 0).toFixed(1) }}</div>
                      <div class="stat-label">平均评分</div>
                    </div>
                  </div>
                  
                  <div class="stat-item">
                    <div class="stat-icon">🕐</div>
                    <div class="stat-info">
                      <div class="stat-value">{{ analytics.behavior.peak_hour || 0 }}:00</div>
                      <div class="stat-label">活跃时间</div>
                    </div>
                  </div>
                </div>
              </div>
            </el-col>
            
            <!-- 兴趣分析 -->
            <el-col :xs="24" :md="16">
              <div class="interest-analysis">
                <h4>兴趣主题分布</h4>
                <div class="interest-list">
                  <div 
                    v-for="(count, topic) in analytics.behavior.topics_interest" 
                    :key="topic"
                    class="interest-item"
                  >
                    <div class="interest-topic">{{ topic }}</div>
                    <div class="interest-bar">
                      <div 
                        class="interest-fill"
                        :style="{ width: getInterestPercentage(count) + '%' }"
                      ></div>
                    </div>
                    <div class="interest-count">{{ count }}次</div>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { analyticsAPI } from '@/api/advanced-features'

// 响应式数据
const loading = ref(false)
const topicChartRef = ref()
const sentimentChartRef = ref()

const analytics = reactive({
  topics: [],
  behavior: {},
  sentiment: {},
  loading: false
})

// 概览指标
const overviewMetrics = ref([
  {
    key: 'conversations',
    label: '分析对话',
    value: '0',
    icon: 'ChatDotRound',
    color: '#409EFF',
    trend: 'up',
    trendIcon: 'ArrowUp',
    change: '+12%'
  },
  {
    key: 'topics',
    label: '识别主题',
    value: '0',
    icon: 'Collection',
    color: '#67C23A',
    trend: 'up',
    trendIcon: 'ArrowUp',
    change: '+8%'
  },
  {
    key: 'keywords',
    label: '关键词',
    value: '0',
    icon: 'Key',
    color: '#E6A23C',
    trend: 'stable',
    trendIcon: 'Minus',
    change: '0%'
  },
  {
    key: 'satisfaction',
    label: '满意度',
    value: '0%',
    icon: 'Star',
    color: '#F56C6C',
    trend: 'up',
    trendIcon: 'ArrowUp',
    change: '+5%'
  }
])

// 生命周期
onMounted(() => {
  loadAnalyticsData()
})

// 方法
const loadAnalyticsData = async () => {
  analytics.loading = true
  try {
    const [topicsRes, behaviorRes, sentimentRes] = await Promise.all([
      analyticsAPI.getTopics(),
      analyticsAPI.getBehavior(),
      analyticsAPI.getSentiment()
    ])
    
    if (topicsRes.success) {
      analytics.topics = topicsRes.data.topics || []
      updateOverviewMetrics('conversations', topicsRes.data.total_analyzed || 0)
      updateOverviewMetrics('topics', topicsRes.data.topics?.length || 0)
      updateOverviewMetrics('keywords', topicsRes.data.keywords?.length || 0)
    }
    
    if (behaviorRes.success) {
      analytics.behavior = behaviorRes.data
    }
    
    if (sentimentRes.success) {
      analytics.sentiment = sentimentRes.data
      const positiveRate = parseFloat(sentimentRes.data.positive || 0)
      updateOverviewMetrics('satisfaction', `${positiveRate.toFixed(1)}%`)
    }
    
    // 渲染图表
    await nextTick()
    renderTopicChart()
    renderSentimentChart()
    
  } catch (error) {
    console.error('加载分析数据失败:', error)
    ElMessage.error('加载分析数据失败')
  } finally {
    analytics.loading = false
  }
}

const updateOverviewMetrics = (key, value) => {
  const metric = overviewMetrics.value.find(m => m.key === key)
  if (metric) {
    metric.value = value
  }
}

const renderTopicChart = () => {
  if (!topicChartRef.value || !analytics.topics.length) return
  
  const chart = echarts.init(topicChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [{
      name: '主题分布',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center'
      },
      emphasis: {
        label: {
          show: true,
          fontSize: '18',
          fontWeight: 'bold'
        }
      },
      labelLine: {
        show: false
      },
      data: analytics.topics.slice(0, 8).map(topic => ({
        value: topic.count,
        name: topic.topic
      }))
    }]
  }
  
  chart.setOption(option)
}

const renderSentimentChart = () => {
  if (!sentimentChartRef.value) return
  
  const chart = echarts.init(sentimentChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    series: [{
      type: 'pie',
      radius: '60%',
      data: [
        { value: parseFloat(analytics.sentiment.positive || 0), name: '积极', itemStyle: { color: '#67C23A' } },
        { value: parseFloat(analytics.sentiment.neutral || 0), name: '中性', itemStyle: { color: '#909399' } },
        { value: parseFloat(analytics.sentiment.negative || 0), name: '消极', itemStyle: { color: '#F56C6C' } }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  
  chart.setOption(option)
}

const refreshData = () => {
  loadAnalyticsData()
}

const exportReport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleTopicAction = (command) => {
  if (command === 'export') {
    ElMessage.info('导出主题数据功能开发中...')
  } else if (command === 'detail') {
    ElMessage.info('查看详情功能开发中...')
  }
}

const getSentimentInsight = () => {
  const positive = parseFloat(analytics.sentiment.positive || 0)
  const negative = parseFloat(analytics.sentiment.negative || 0)
  
  if (positive > 70) {
    return '用户情感倾向非常积极，系统表现优秀！'
  } else if (positive > 50) {
    return '用户情感倾向整体积极，继续保持！'
  } else if (negative > 30) {
    return '发现较多消极情感，建议关注用户体验改进'
  } else {
    return '用户情感倾向相对平和，可适当提升服务质量'
  }
}

const getInterestPercentage = (count) => {
  if (!analytics.behavior.topics_interest) return 0
  const maxCount = Math.max(...Object.values(analytics.behavior.topics_interest))
  return maxCount > 0 ? (count / maxCount * 100) : 0
}
</script>

<style lang="scss" scoped>
.analytics-dashboard {
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

.overview-metrics {
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
      flex: 1;
      
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
        margin-bottom: 4px;
      }
      
      .metric-trend {
        display: flex;
        align-items: center;
        font-size: 12px;
        
        &.up {
          color: #67C23A;
        }
        
        &.down {
          color: #F56C6C;
        }
        
        &.stable {
          color: #909399;
        }
        
        .el-icon {
          margin-right: 4px;
        }
      }
    }
  }
}

.analysis-charts {
  margin-bottom: 24px;
}

.chart-card {
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
  }
  
  .chart-container {
    .topic-list {
      margin-top: 20px;
      
      .topic-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .topic-rank {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: #409EFF;
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 12px;
          font-weight: 600;
          margin-right: 12px;
        }
        
        .topic-info {
          flex: 1;
          margin-right: 12px;
          
          .topic-name {
            font-weight: 500;
            color: #303133;
            margin-bottom: 2px;
          }
          
          .topic-count {
            font-size: 12px;
            color: #909399;
          }
        }
        
        .topic-progress {
          width: 100px;
        }
      }
    }
  }
}

.sentiment-analysis {
  .sentiment-overview {
    display: flex;
    justify-content: space-around;
    margin-bottom: 24px;
    
    .sentiment-item {
      text-align: center;
      
      .sentiment-icon {
        font-size: 32px;
        margin-bottom: 8px;
      }
      
      .sentiment-percentage {
        font-size: 20px;
        font-weight: 600;
        margin-bottom: 4px;
      }
      
      .sentiment-label {
        font-size: 14px;
        color: #606266;
      }
      
      &.positive .sentiment-percentage {
        color: #67C23A;
      }
      
      &.neutral .sentiment-percentage {
        color: #909399;
      }
      
      &.negative .sentiment-percentage {
        color: #F56C6C;
      }
    }
  }
  
  .sentiment-insight {
    margin-top: 16px;
  }
}

.behavior-analysis {
  .behavior-stats {
    .stat-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
      
      .stat-item {
        display: flex;
        align-items: center;
        padding: 16px;
        background: #f8f9fa;
        border-radius: 8px;
        
        .stat-icon {
          font-size: 24px;
          margin-right: 12px;
        }
        
        .stat-info {
          .stat-value {
            font-size: 18px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 2px;
          }
          
          .stat-label {
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }
  }
  
  .interest-analysis {
    h4 {
      margin: 0 0 16px 0;
      color: #303133;
    }
    
    .interest-list {
      .interest-item {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        
        .interest-topic {
          width: 100px;
          font-size: 14px;
          color: #606266;
        }
        
        .interest-bar {
          flex: 1;
          height: 8px;
          background: #f0f0f0;
          border-radius: 4px;
          margin: 0 12px;
          overflow: hidden;
          
          .interest-fill {
            height: 100%;
            background: linear-gradient(90deg, #409EFF, #67C23A);
            border-radius: 4px;
            transition: width 0.3s ease;
          }
        }
        
        .interest-count {
          font-size: 12px;
          color: #909399;
          min-width: 40px;
          text-align: right;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .analytics-dashboard {
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
  
  .overview-metrics {
    .metric-card {
      margin-bottom: 12px;
    }
  }
  
  .behavior-stats .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
