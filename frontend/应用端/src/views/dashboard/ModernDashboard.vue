<template>
  <div class="modern-dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1 class="welcome-title">
            欢迎回来，{{ userInfo.name || '用户' }}
            <span class="welcome-emoji">👋</span>
          </h1>
          <p class="welcome-subtitle">
            今天是 {{ currentDate }}，让我们开始高效的质量管理工作吧！
          </p>
        </div>
        <div class="quick-actions">
          <el-button type="primary" size="large" @click="startNewChat" class="action-btn">
            <el-icon><ChatDotRound /></el-icon>
            开始对话
          </el-button>
          <el-button size="large" @click="viewAnalytics" class="action-btn">
            <el-icon><DataAnalysis /></el-icon>
            查看分析
          </el-button>
          <el-button size="large" @click="viewRecommendations" class="action-btn">
            <el-icon><MagicStick /></el-icon>
            智能推荐
          </el-button>
        </div>
      </div>
      <div class="welcome-illustration">
        <div class="illustration-card">
          <div class="illustration-icon">
            <el-icon :size="64"><TrendCharts /></el-icon>
          </div>
          <div class="illustration-text">
            <h3>智能质量管理</h3>
            <p>AI驱动的质量管理解决方案</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 数据概览 -->
    <el-row :gutter="20" class="dashboard-metrics">
      <el-col :xs="12" :sm="6" v-for="metric in dashboardMetrics" :key="metric.key">
        <div class="metric-card" :class="`metric-${metric.key}`">
          <div class="metric-icon" :style="{ background: metric.gradient }">
            <el-icon :size="28"><component :is="metric.icon" /></el-icon>
          </div>
          <div class="metric-data">
            <div class="metric-value">{{ metric.value }}</div>
            <div class="metric-label">{{ metric.label }}</div>
            <div class="metric-trend" :class="metric.trend">
              <el-icon><component :is="metric.trendIcon" /></el-icon>
              <span>{{ metric.change }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 功能区域 -->
    <el-row :gutter="20" class="dashboard-sections">
      <!-- 最近对话 -->
      <el-col :xs="24" :lg="12">
        <div class="section-card">
          <div class="section-header">
            <h3 class="section-title">
              <el-icon><ChatDotRound /></el-icon>
              最近对话
            </h3>
            <el-button text @click="viewAllConversations">
              查看全部
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="section-content" v-loading="loading.conversations">
            <div v-if="recentConversations.length === 0" class="empty-state">
              <el-empty description="暂无对话记录" :image-size="80">
                <el-button type="primary" @click="startNewChat">开始第一次对话</el-button>
              </el-empty>
            </div>
            <div v-else class="conversation-list">
              <div 
                v-for="conversation in recentConversations" 
                :key="conversation.id"
                class="conversation-item"
                @click="openConversation(conversation)"
              >
                <div class="conversation-avatar">
                  <el-icon><User /></el-icon>
                </div>
                <div class="conversation-content">
                  <div class="conversation-title">{{ conversation.title }}</div>
                  <div class="conversation-preview">{{ conversation.lastMessage }}</div>
                  <div class="conversation-meta">
                    <span class="conversation-time">{{ formatTime(conversation.updatedAt) }}</span>
                    <el-tag size="small" :type="getConversationTagType(conversation.status)">
                      {{ conversation.status }}
                    </el-tag>
                  </div>
                </div>
                <div class="conversation-actions">
                  <el-button size="small" text @click.stop="continueConversation(conversation)">
                    <el-icon><Right /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 智能推荐 -->
      <el-col :xs="24" :lg="12">
        <div class="section-card">
          <div class="section-header">
            <h3 class="section-title">
              <el-icon><MagicStick /></el-icon>
              智能推荐
            </h3>
            <el-button text @click="viewAllRecommendations">
              查看全部
              <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="section-content" v-loading="loading.recommendations">
            <div v-if="quickRecommendations.length === 0" class="empty-state">
              <el-empty description="暂无推荐内容" :image-size="80">
                <el-button type="primary" @click="refreshRecommendations">刷新推荐</el-button>
              </el-empty>
            </div>
            <div v-else class="recommendation-list">
              <div 
                v-for="rec in quickRecommendations" 
                :key="rec.id"
                class="recommendation-item"
                @click="askRecommendedQuestion(rec)"
              >
                <div class="recommendation-icon">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="recommendation-content">
                  <div class="recommendation-question">{{ rec.question }}</div>
                  <div class="recommendation-meta">
                    <el-tag size="small" :type="getTopicTagType(rec.topic)">
                      {{ rec.topic }}
                    </el-tag>
                    <span class="recommendation-score">
                      相关度 {{ (rec.score * 20).toFixed(0) }}%
                    </span>
                  </div>
                </div>
                <div class="recommendation-actions">
                  <el-button size="small" text @click.stop="askRecommendedQuestion(rec)">
                    <el-icon><ChatDotRound /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 系统状态 -->
    <el-row :gutter="20" class="system-status">
      <el-col :span="24">
        <div class="section-card">
          <div class="section-header">
            <h3 class="section-title">
              <el-icon><Monitor /></el-icon>
              系统状态
            </h3>
            <div class="status-actions">
              <el-button size="small" @click="refreshSystemStatus">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
              <el-button size="small" type="primary" @click="viewSystemDetails">
                <el-icon><View /></el-icon>
                详情
              </el-button>
            </div>
          </div>
          <div class="section-content">
            <el-row :gutter="16">
              <el-col :xs="12" :sm="6" v-for="status in systemStatus" :key="status.name">
                <div class="status-item" :class="{ 'status-healthy': status.healthy, 'status-error': !status.healthy }">
                  <div class="status-indicator">
                    <div class="status-dot" :class="{ 'dot-green': status.healthy, 'dot-red': !status.healthy }"></div>
                  </div>
                  <div class="status-info">
                    <div class="status-name">{{ status.name }}</div>
                    <div class="status-value">{{ status.value }}</div>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { dashboardAPI, recommendationAPI } from '@/api/advanced-features'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 响应式数据
const loading = reactive({
  conversations: false,
  recommendations: false,
  systemStatus: false
})

const userInfo = computed(() => authStore.userInfo || {})
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 仪表板指标
const dashboardMetrics = ref([
  {
    key: 'conversations',
    label: '今日对话',
    value: '0',
    icon: 'ChatDotRound',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    trend: 'up',
    trendIcon: 'ArrowUp',
    change: '+12%'
  },
  {
    key: 'recommendations',
    label: '智能推荐',
    value: '0',
    icon: 'MagicStick',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    trend: 'up',
    trendIcon: 'ArrowUp',
    change: '+8%'
  },
  {
    key: 'analytics',
    label: '分析报告',
    value: '0',
    icon: 'DataAnalysis',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    trend: 'stable',
    trendIcon: 'Minus',
    change: '0%'
  },
  {
    key: 'satisfaction',
    label: '满意度',
    value: '0%',
    icon: 'Star',
    gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    trend: 'up',
    trendIcon: 'ArrowUp',
    change: '+5%'
  }
])

const recentConversations = ref([])
const quickRecommendations = ref([])
const systemStatus = ref([
  { name: '聊天服务', value: '正常', healthy: true },
  { name: '分析服务', value: '正常', healthy: true },
  { name: '推荐服务', value: '正常', healthy: true },
  { name: '数据库', value: '正常', healthy: true }
])

// 生命周期
onMounted(() => {
  loadDashboardData()
})

// 方法
const loadDashboardData = async () => {
  await Promise.all([
    loadQuickStats(),
    loadRecentConversations(),
    loadQuickRecommendations(),
    loadSystemStatus()
  ])
}

const loadQuickStats = async () => {
  try {
    const response = await dashboardAPI.getQuickStats()
    if (response.success) {
      const stats = response.data
      updateMetric('conversations', stats.todayConversations || 0)
      updateMetric('recommendations', stats.totalRecommendations || 0)
      updateMetric('analytics', stats.totalReports || 0)
      updateMetric('satisfaction', `${(stats.avgSatisfaction || 0).toFixed(1)}%`)
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecentConversations = async () => {
  loading.conversations = true
  try {
    const response = await dashboardAPI.getRecentActivities(5)
    if (response.success) {
      recentConversations.value = response.data.map(item => ({
        id: item.id,
        title: item.title || '未命名对话',
        lastMessage: item.lastMessage || '暂无消息',
        updatedAt: item.updatedAt,
        status: item.status || '进行中'
      }))
    }
  } catch (error) {
    console.error('加载最近对话失败:', error)
  } finally {
    loading.conversations = false
  }
}

const loadQuickRecommendations = async () => {
  loading.recommendations = true
  try {
    const response = await recommendationAPI.getPersonalized(4)
    if (response.success) {
      quickRecommendations.value = response.data
    }
  } catch (error) {
    console.error('加载推荐数据失败:', error)
  } finally {
    loading.recommendations = false
  }
}

const loadSystemStatus = async () => {
  loading.systemStatus = true
  try {
    // 这里可以调用系统健康检查API
    // const response = await systemAPI.getHealthStatus()
    // 暂时使用模拟数据
    setTimeout(() => {
      loading.systemStatus = false
    }, 500)
  } catch (error) {
    console.error('加载系统状态失败:', error)
    loading.systemStatus = false
  }
}

const updateMetric = (key, value) => {
  const metric = dashboardMetrics.value.find(m => m.key === key)
  if (metric) {
    metric.value = value
  }
}

// 导航方法
const startNewChat = () => {
  router.push('/ai-management/chat')
}

const viewAnalytics = () => {
  router.push('/ai-management/analytics')
}

const viewRecommendations = () => {
  router.push('/ai-management/recommendations')
}

const viewAllConversations = () => {
  router.push('/ai-management/chat-history')
}

const viewAllRecommendations = () => {
  router.push('/ai-management/recommendations')
}

const viewSystemDetails = () => {
  router.push('/ai-management/monitoring')
}

// 交互方法
const openConversation = (conversation) => {
  router.push(`/ai-management/chat?conversationId=${conversation.id}`)
}

const continueConversation = (conversation) => {
  router.push(`/ai-management/chat?conversationId=${conversation.id}`)
}

const askRecommendedQuestion = (recommendation) => {
  router.push({
    path: '/ai-management/chat',
    query: { question: encodeURIComponent(recommendation.question) }
  })
}

const refreshRecommendations = () => {
  loadQuickRecommendations()
}

const refreshSystemStatus = () => {
  loadSystemStatus()
}

// 工具方法
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return date.toLocaleDateString()
}

const getConversationTagType = (status) => {
  const statusTypes = {
    '进行中': 'primary',
    '已完成': 'success',
    '已暂停': 'warning',
    '已结束': 'info'
  }
  return statusTypes[status] || ''
}

const getTopicTagType = (topic) => {
  const topicTypes = {
    '质量体系': 'primary',
    '质量控制': 'success',
    '质量改进': 'warning',
    '供应商管理': 'info'
  }
  return topicTypes[topic] || ''
}
</script>

<style lang="scss" scoped>
.modern-dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.welcome-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  color: white;
  
  .welcome-content {
    flex: 1;
    
    .welcome-text {
      margin-bottom: 24px;
      
      .welcome-title {
        font-size: 28px;
        font-weight: 600;
        margin: 0 0 8px 0;
        display: flex;
        align-items: center;
        
        .welcome-emoji {
          margin-left: 12px;
          font-size: 32px;
        }
      }
      
      .welcome-subtitle {
        font-size: 16px;
        opacity: 0.9;
        margin: 0;
      }
    }
    
    .quick-actions {
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      
      .action-btn {
        background: rgba(255, 255, 255, 0.2);
        border: 1px solid rgba(255, 255, 255, 0.3);
        color: white;
        backdrop-filter: blur(10px);
        
        &:hover {
          background: rgba(255, 255, 255, 0.3);
          border-color: rgba(255, 255, 255, 0.5);
        }
        
        &.el-button--primary {
          background: rgba(255, 255, 255, 0.9);
          color: #667eea;
          
          &:hover {
            background: white;
          }
        }
      }
    }
  }
  
  .welcome-illustration {
    .illustration-card {
      background: rgba(255, 255, 255, 0.1);
      border-radius: 12px;
      padding: 24px;
      text-align: center;
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.2);
      
      .illustration-icon {
        margin-bottom: 16px;
        opacity: 0.8;
      }
      
      .illustration-text {
        h3 {
          font-size: 18px;
          margin: 0 0 8px 0;
        }
        
        p {
          font-size: 14px;
          margin: 0;
          opacity: 0.8;
        }
      }
    }
  }
}

.dashboard-metrics {
  margin-bottom: 24px;
  
  .metric-card {
    background: white;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    display: flex;
    align-items: center;
    transition: all 0.3s ease;
    border: 1px solid #f0f0f0;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
    }
    
    .metric-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      color: white;
    }
    
    .metric-data {
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
        margin-bottom: 8px;
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

.dashboard-sections {
  margin-bottom: 24px;
}

.section-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f0f0f0;
  overflow: hidden;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-bottom: 1px solid #f0f0f0;
    background: #fafafa;
    
    .section-title {
      display: flex;
      align-items: center;
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin: 0;
      
      .el-icon {
        margin-right: 8px;
        color: #409EFF;
      }
    }
    
    .status-actions {
      display: flex;
      gap: 8px;
    }
  }
  
  .section-content {
    padding: 24px;
    min-height: 200px;
    
    .empty-state {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 200px;
    }
  }
}

.conversation-list, .recommendation-list {
  .conversation-item, .recommendation-item {
    display: flex;
    align-items: center;
    padding: 16px 0;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
    transition: all 0.2s ease;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      background: #f8f9fa;
      margin: 0 -16px;
      padding-left: 16px;
      padding-right: 16px;
      border-radius: 8px;
    }
    
    .conversation-avatar, .recommendation-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: #f0f0f0;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      color: #909399;
    }
    
    .conversation-content, .recommendation-content {
      flex: 1;
      
      .conversation-title, .recommendation-question {
        font-weight: 500;
        color: #303133;
        margin-bottom: 4px;
        font-size: 14px;
      }
      
      .conversation-preview {
        font-size: 12px;
        color: #909399;
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .conversation-meta, .recommendation-meta {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 12px;
        
        .conversation-time, .recommendation-score {
          color: #909399;
        }
      }
    }
    
    .conversation-actions, .recommendation-actions {
      opacity: 0;
      transition: opacity 0.2s ease;
    }
    
    &:hover {
      .conversation-actions, .recommendation-actions {
        opacity: 1;
      }
    }
  }
}

.system-status {
  .status-item {
    display: flex;
    align-items: center;
    padding: 16px;
    background: #f8f9fa;
    border-radius: 8px;
    border-left: 4px solid #e4e7ed;
    
    &.status-healthy {
      border-left-color: #67C23A;
      background: #f0f9ff;
    }
    
    &.status-error {
      border-left-color: #F56C6C;
      background: #fef2f2;
    }
    
    .status-indicator {
      margin-right: 12px;
      
      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        
        &.dot-green {
          background: #67C23A;
        }
        
        &.dot-red {
          background: #F56C6C;
        }
      }
    }
    
    .status-info {
      .status-name {
        font-size: 14px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 2px;
      }
      
      .status-value {
        font-size: 12px;
        color: #606266;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .modern-dashboard {
    padding: 12px;
  }
  
  .welcome-section {
    flex-direction: column;
    text-align: center;
    
    .welcome-content {
      margin-bottom: 24px;
      
      .quick-actions {
        justify-content: center;
      }
    }
  }
  
  .dashboard-metrics {
    .el-col {
      margin-bottom: 12px;
    }
  }
  
  .dashboard-sections {
    .el-col {
      margin-bottom: 20px;
    }
  }
}
</style>
