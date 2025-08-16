<template>
  <div class="recommendation-panel">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><MagicStick /></el-icon>
          智能推荐
        </h1>
        <p class="page-description">基于您的使用习惯和热门话题，为您推荐相关问题</p>
      </div>
      <div class="header-actions">
        <el-button @click="refreshRecommendations" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新推荐
        </el-button>
        <el-button type="primary" @click="clearCache">
          <el-icon><Delete /></el-icon>
          清除缓存
        </el-button>
      </div>
    </div>

    <!-- 推荐统计 -->
    <el-row :gutter="20" class="recommendation-stats">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon personalized">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ personalizedRecs.length }}</div>
            <div class="stat-label">个性化推荐</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon popular">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ popularRecs.length }}</div>
            <div class="stat-label">热门推荐</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon total">
            <el-icon><Collection /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ totalRecommendations }}</div>
            <div class="stat-label">总推荐数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 推荐内容 -->
    <el-tabs v-model="activeTab" class="recommendation-tabs">
      <!-- 个性化推荐 -->
      <el-tab-pane label="个性化推荐" name="personalized">
        <div class="tab-header">
          <div class="tab-description">
            <el-icon><User /></el-icon>
            基于您的对话历史和兴趣偏好生成的个性化推荐
          </div>
          <div class="tab-actions">
            <el-select v-model="personalizedFilter" placeholder="筛选主题" clearable>
              <el-option
                v-for="topic in availableTopics"
                :key="topic"
                :label="topic"
                :value="topic"
              />
            </el-select>
          </div>
        </div>

        <div v-loading="recommendations.loading" class="recommendation-grid">
          <div 
            v-for="rec in filteredPersonalizedRecs" 
            :key="rec.id || rec.question"
            class="recommendation-card personalized-card"
            @click="handleRecommendationClick(rec)"
          >
            <div class="rec-header">
              <el-tag :type="getTopicTagType(rec.topic)" size="small">
                {{ rec.topic }}
              </el-tag>
              <div class="rec-score">
                <el-rate 
                  v-model="rec.displayScore" 
                  disabled 
                  show-score 
                  text-color="#ff9900"
                  :max="5"
                />
              </div>
            </div>
            
            <div class="rec-content">
              <h4 class="rec-question">{{ rec.question }}</h4>
              <p class="rec-reason">
                <el-icon><InfoFilled /></el-icon>
                {{ rec.reason }}
              </p>
              <div class="rec-meta">
                <span class="rec-difficulty" v-if="rec.difficulty">
                  <el-icon><Star /></el-icon>
                  {{ rec.difficulty }}
                </span>
                <span class="rec-relevance">
                  相关度: {{ (rec.score * 20).toFixed(1) }}%
                </span>
              </div>
            </div>
            
            <div class="rec-actions">
              <el-button size="small" type="primary" @click.stop="startChatWithQuestion(rec.question)">
                <el-icon><ChatDotRound /></el-icon>
                开始对话
              </el-button>
              <el-button size="small" @click.stop="saveRecommendation(rec)">
                <el-icon><Star /></el-icon>
                收藏
              </el-button>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="!recommendations.loading && filteredPersonalizedRecs.length === 0" class="empty-state">
            <el-empty description="暂无个性化推荐">
              <el-button type="primary" @click="refreshRecommendations">
                刷新推荐
              </el-button>
            </el-empty>
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 热门推荐 -->
      <el-tab-pane label="热门推荐" name="popular">
        <div class="tab-header">
          <div class="tab-description">
            <el-icon><TrendCharts /></el-icon>
            基于全平台用户询问频率的热门问题推荐
          </div>
          <div class="tab-actions">
            <el-select v-model="popularSort" placeholder="排序方式">
              <el-option label="按热度排序" value="popularity" />
              <el-option label="按时间排序" value="time" />
              <el-option label="按主题排序" value="topic" />
            </el-select>
          </div>
        </div>

        <div v-loading="recommendations.loading" class="popular-list">
          <div 
            v-for="(rec, index) in sortedPopularRecs" 
            :key="rec.id || rec.question"
            class="popular-item"
            @click="handleRecommendationClick(rec)"
          >
            <div class="popular-rank">
              <div class="rank-number" :class="getRankClass(index)">
                {{ index + 1 }}
              </div>
            </div>
            
            <div class="popular-content">
              <div class="popular-header">
                <h4 class="popular-question">{{ rec.question }}</h4>
                <div class="popular-meta">
                  <el-tag :type="getTopicTagType(rec.topic)" size="small">
                    {{ rec.topic }}
                  </el-tag>
                  <span class="popularity-count">
                    <el-icon><View /></el-icon>
                    {{ rec.popularity }}次询问
                  </span>
                </div>
              </div>
              
              <p class="popular-reason" v-if="rec.reason">{{ rec.reason }}</p>
              
              <div class="popular-actions">
                <el-button size="small" type="primary" @click.stop="startChatWithQuestion(rec.question)">
                  <el-icon><ChatDotRound /></el-icon>
                  立即询问
                </el-button>
                <el-button size="small" @click.stop="viewSimilarQuestions(rec)">
                  <el-icon><Search /></el-icon>
                  相似问题
                </el-button>
              </div>
            </div>
            
            <div class="popular-trend">
              <div class="trend-indicator" :class="getTrendClass(rec.trend)">
                <el-icon><component :is="getTrendIcon(rec.trend)" /></el-icon>
              </div>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="!recommendations.loading && sortedPopularRecs.length === 0" class="empty-state">
            <el-empty description="暂无热门推荐">
              <el-button type="primary" @click="refreshRecommendations">
                刷新推荐
              </el-button>
            </el-empty>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { recommendationAPI } from '@/api/advanced-features'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const activeTab = ref('personalized')
const personalizedFilter = ref('')
const popularSort = ref('popularity')

const recommendations = reactive({
  loading: false
})

const personalizedRecs = ref([])
const popularRecs = ref([])

// 计算属性
const totalRecommendations = computed(() => {
  return personalizedRecs.value.length + popularRecs.value.length
})

const availableTopics = computed(() => {
  const topics = new Set()
  personalizedRecs.value.forEach(rec => {
    if (rec.topic) topics.add(rec.topic)
  })
  return Array.from(topics)
})

const filteredPersonalizedRecs = computed(() => {
  if (!personalizedFilter.value) return personalizedRecs.value
  return personalizedRecs.value.filter(rec => rec.topic === personalizedFilter.value)
})

const sortedPopularRecs = computed(() => {
  const recs = [...popularRecs.value]
  
  switch (popularSort.value) {
    case 'popularity':
      return recs.sort((a, b) => (b.popularity || 0) - (a.popularity || 0))
    case 'time':
      return recs.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
    case 'topic':
      return recs.sort((a, b) => (a.topic || '').localeCompare(b.topic || ''))
    default:
      return recs
  }
})

// 生命周期
onMounted(() => {
  loadRecommendations()
})

// 方法
const loadRecommendations = async () => {
  recommendations.loading = true
  try {
    const [personalizedRes, popularRes] = await Promise.all([
      recommendationAPI.getPersonalized(8),
      recommendationAPI.getPopular(12)
    ])
    
    if (personalizedRes.success) {
      personalizedRecs.value = personalizedRes.data.map(rec => ({
        ...rec,
        displayScore: Math.round(rec.score || 0),
        id: rec.id || `personalized_${Date.now()}_${Math.random()}`
      }))
    }
    
    if (popularRes.success) {
      popularRecs.value = popularRes.data.map((rec, index) => ({
        ...rec,
        trend: index < 3 ? 'up' : index < 6 ? 'stable' : 'down',
        id: rec.id || `popular_${Date.now()}_${Math.random()}`
      }))
    }
    
  } catch (error) {
    console.error('加载推荐数据失败:', error)
    ElMessage.error('加载推荐数据失败')
  } finally {
    recommendations.loading = false
  }
}

const refreshRecommendations = () => {
  loadRecommendations()
}

const clearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清除推荐缓存吗？这将重新生成所有推荐内容。', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await recommendationAPI.clearCache()
    ElMessage.success('缓存清除成功')
    await loadRecommendations()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清除缓存失败:', error)
      ElMessage.error('清除缓存失败')
    }
  }
}

const handleRecommendationClick = (rec) => {
  console.log('点击推荐:', rec)
  // 可以添加点击统计等逻辑
}

const startChatWithQuestion = (question) => {
  // 跳转到聊天页面并预填问题
  router.push({
    name: 'Chat',
    query: { question: encodeURIComponent(question) }
  })
}

const saveRecommendation = (rec) => {
  ElMessage.success(`已收藏推荐: ${rec.question.substring(0, 20)}...`)
  // 这里可以添加收藏逻辑
}

const viewSimilarQuestions = (rec) => {
  ElMessage.info('相似问题功能开发中...')
  // 这里可以添加查看相似问题的逻辑
}

const getTopicTagType = (topic) => {
  const topicTypes = {
    '质量体系': 'primary',
    '质量控制': 'success',
    '质量改进': 'warning',
    '供应商管理': 'info',
    '审核管理': 'danger',
    '培训管理': ''
  }
  return topicTypes[topic] || ''
}

const getRankClass = (index) => {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return 'normal'
}

const getTrendClass = (trend) => {
  return {
    'trend-up': trend === 'up',
    'trend-down': trend === 'down',
    'trend-stable': trend === 'stable'
  }
}

const getTrendIcon = (trend) => {
  switch (trend) {
    case 'up': return 'ArrowUp'
    case 'down': return 'ArrowDown'
    default: return 'Minus'
  }
}
</script>

<style lang="scss" scoped>
.recommendation-panel {
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
        color: #E6A23C;
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

.recommendation-stats {
  margin-bottom: 24px;
  
  .stat-card {
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
    
    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      color: white;
      
      &.personalized {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }
      
      &.popular {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }
      
      &.total {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }
    }
    
    .stat-content {
      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        line-height: 1;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.recommendation-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  
  :deep(.el-tabs__header) {
    margin: 0;
    padding: 0 24px;
    background: #fafafa;
    border-radius: 8px 8px 0 0;
  }
  
  :deep(.el-tabs__content) {
    padding: 24px;
  }
  
  .tab-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
    
    .tab-description {
      display: flex;
      align-items: center;
      color: #606266;
      font-size: 14px;
      
      .el-icon {
        margin-right: 8px;
        color: #409EFF;
      }
    }
    
    .tab-actions {
      display: flex;
      gap: 12px;
    }
  }
}

.recommendation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  
  .recommendation-card {
    background: white;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
      border-color: #409EFF;
    }
    
    &.personalized-card {
      border-left: 4px solid #667eea;
    }
    
    .rec-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      .rec-score {
        :deep(.el-rate__text) {
          font-size: 12px;
          color: #909399;
        }
      }
    }
    
    .rec-content {
      margin-bottom: 16px;
      
      .rec-question {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin: 0 0 8px 0;
        line-height: 1.4;
      }
      
      .rec-reason {
        display: flex;
        align-items: flex-start;
        font-size: 13px;
        color: #606266;
        margin: 0 0 12px 0;
        line-height: 1.5;
        
        .el-icon {
          margin-right: 6px;
          margin-top: 2px;
          color: #909399;
          flex-shrink: 0;
        }
      }
      
      .rec-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 12px;
        color: #909399;
        
        .rec-difficulty {
          display: flex;
          align-items: center;
          
          .el-icon {
            margin-right: 4px;
          }
        }
      }
    }
    
    .rec-actions {
      display: flex;
      gap: 8px;
    }
  }
}

.popular-list {
  .popular-item {
    display: flex;
    align-items: center;
    background: white;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 12px;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      transform: translateX(4px);
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
      border-color: #409EFF;
    }
    
    .popular-rank {
      margin-right: 20px;
      
      .rank-number {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 14px;
        
        &.gold {
          background: linear-gradient(135deg, #ffd700, #ffed4e);
          color: #b8860b;
        }
        
        &.silver {
          background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
          color: #696969;
        }
        
        &.bronze {
          background: linear-gradient(135deg, #cd7f32, #daa520);
          color: #8b4513;
        }
        
        &.normal {
          background: #f0f0f0;
          color: #909399;
        }
      }
    }
    
    .popular-content {
      flex: 1;
      
      .popular-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 8px;
        
        .popular-question {
          font-size: 16px;
          font-weight: 600;
          color: #303133;
          margin: 0;
          flex: 1;
          margin-right: 16px;
        }
        
        .popular-meta {
          display: flex;
          align-items: center;
          gap: 12px;
          
          .popularity-count {
            display: flex;
            align-items: center;
            font-size: 12px;
            color: #909399;
            
            .el-icon {
              margin-right: 4px;
            }
          }
        }
      }
      
      .popular-reason {
        font-size: 13px;
        color: #606266;
        margin: 0 0 12px 0;
        line-height: 1.5;
      }
      
      .popular-actions {
        display: flex;
        gap: 8px;
      }
    }
    
    .popular-trend {
      margin-left: 16px;
      
      .trend-indicator {
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        
        &.trend-up {
          background: #f0f9ff;
          color: #67C23A;
        }
        
        &.trend-down {
          background: #fef2f2;
          color: #F56C6C;
        }
        
        &.trend-stable {
          background: #f9f9f9;
          color: #909399;
        }
      }
    }
  }
}

.empty-state {
  grid-column: 1 / -1;
  padding: 40px;
  text-align: center;
}

// 响应式设计
@media (max-width: 768px) {
  .recommendation-panel {
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
  
  .recommendation-stats {
    .el-col {
      margin-bottom: 12px;
    }
  }
  
  .recommendation-grid {
    grid-template-columns: 1fr;
  }
  
  .tab-header {
    flex-direction: column;
    align-items: stretch;
    
    .tab-actions {
      margin-top: 12px;
    }
  }
  
  .popular-item {
    flex-direction: column;
    align-items: stretch;
    
    .popular-rank {
      margin-right: 0;
      margin-bottom: 12px;
      align-self: center;
    }
    
    .popular-trend {
      margin-left: 0;
      margin-top: 12px;
      align-self: center;
    }
  }
}
</style>
