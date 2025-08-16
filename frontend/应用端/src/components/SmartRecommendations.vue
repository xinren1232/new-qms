<template>
  <div class="smart-recommendations">
    <div class="recommendations-header">
      <h4>
        <el-icon><StarFilled /></el-icon>
        智能推荐
      </h4>
      <el-button 
        size="small" 
        text 
        @click="refreshRecommendations"
        :loading="loading"
      >
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>
    
    <div class="recommendations-content">
      <!-- 个性化推荐 -->
      <div v-if="personalizedRecommendations.length > 0" class="recommendation-section">
        <h5>
          <el-icon><User /></el-icon>
          为您推荐
        </h5>
        <div class="recommendation-list">
          <div
            v-for="item in personalizedRecommendations"
            :key="item.id"
            class="recommendation-item personalized"
            @click="selectRecommendation(item)"
          >
            <div class="item-content">
              <span class="item-text">{{ item.question }}</span>
              <el-tag :type="getCategoryType(item.category)" size="small">
                {{ item.category }}
              </el-tag>
            </div>
            <div class="item-meta">
              <span class="item-reason">{{ item.reason }}</span>
              <span class="item-score">匹配度: {{ item.score }}%</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 热门问题 -->
      <div class="recommendation-section">
        <h5>
          <el-icon><TrendCharts /></el-icon>
          热门问题
        </h5>
        <div class="recommendation-list">
          <div
            v-for="item in popularRecommendations"
            :key="item.id"
            class="recommendation-item popular"
            @click="selectRecommendation(item)"
          >
            <div class="item-content">
              <span class="item-text">{{ item.question }}</span>
              <el-tag :type="getCategoryType(item.category)" size="small">
                {{ item.category }}
              </el-tag>
            </div>
            <div class="item-meta">
              <span class="item-popularity">
                <el-icon><View /></el-icon>
                {{ item.popularity }}次询问
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 快速入门 -->
      <div class="recommendation-section">
        <h5>
          <el-icon><QuestionFilled /></el-icon>
          快速入门
        </h5>
        <div class="recommendation-list">
          <div
            v-for="item in quickStartRecommendations"
            :key="item.id"
            class="recommendation-item quick-start"
            @click="selectRecommendation(item)"
          >
            <div class="item-content">
              <span class="item-text">{{ item.question }}</span>
              <el-tag type="info" size="small">入门</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { 
  StarFilled, 
  Refresh, 
  User, 
  TrendCharts, 
  View, 
  QuestionFilled 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  userId: {
    type: String,
    default: 'default-user'
  }
})

// Emits
const emit = defineEmits(['select-recommendation'])

// 响应式数据
const loading = ref(false)
const personalizedRecommendations = ref([])
const popularRecommendations = ref([])
const quickStartRecommendations = ref([])

// 推荐数据
const recommendationData = {
  personalized: [
    {
      id: 'p1',
      question: '如何建立ISO 9001质量管理体系？',
      category: '质量体系',
      reason: '基于您的浏览历史',
      score: 95
    },
    {
      id: 'p2',
      question: '统计过程控制(SPC)如何实施？',
      category: '质量控制',
      reason: '相似用户推荐',
      score: 88
    },
    {
      id: 'p3',
      question: '供应商质量管理最佳实践',
      category: '供应商管理',
      reason: '热门相关问题',
      score: 82
    }
  ],
  popular: [
    {
      id: 'h1',
      question: '质量手册应该包含哪些内容？',
      category: '质量体系',
      popularity: 156
    },
    {
      id: 'h2',
      question: '如何进行内部质量审核？',
      category: '审核管理',
      popularity: 142
    },
    {
      id: 'h3',
      question: '不合格品如何处理？',
      category: '质量控制',
      popularity: 128
    },
    {
      id: 'h4',
      question: 'FMEA分析方法详解',
      category: '质量改进',
      popularity: 115
    }
  ],
  quickStart: [
    {
      id: 'q1',
      question: 'QMS系统基本功能介绍',
      category: '系统使用'
    },
    {
      id: 'q2',
      question: '如何开始第一次质量对话？',
      category: '系统使用'
    },
    {
      id: 'q3',
      question: '常用质量管理术语解释',
      category: '基础知识'
    }
  ]
}

// 计算属性
const getCategoryType = (category) => {
  const typeMap = {
    '质量体系': 'primary',
    '质量控制': 'success',
    '质量改进': 'warning',
    '供应商管理': 'info',
    '审核管理': 'danger',
    '系统使用': '',
    '基础知识': 'info'
  }
  return typeMap[category] || ''
}

// 方法
const loadRecommendations = async () => {
  loading.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 加载个性化推荐
    personalizedRecommendations.value = recommendationData.personalized
    
    // 加载热门推荐
    popularRecommendations.value = recommendationData.popular
    
    // 加载快速入门
    quickStartRecommendations.value = recommendationData.quickStart
    
  } catch (error) {
    console.error('加载推荐失败:', error)
    ElMessage.error('加载推荐失败')
  } finally {
    loading.value = false
  }
}

const refreshRecommendations = () => {
  loadRecommendations()
  ElMessage.success('推荐已刷新')
}

const selectRecommendation = (item) => {
  emit('select-recommendation', item)
  ElMessage.success(`已选择: ${item.question.substring(0, 20)}...`)
}

// 生命周期
onMounted(() => {
  loadRecommendations()
})
</script>

<style lang="scss" scoped>
.smart-recommendations {
  background: var(--qms-bg-primary);
  border: 1px solid var(--qms-border-normal);
  border-radius: 8px;
  padding: 16px;
  
  .recommendations-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    h4 {
      margin: 0;
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--qms-text-primary);
      font-size: 16px;
      font-weight: 600;
    }
  }
  
  .recommendations-content {
    .recommendation-section {
      margin-bottom: 20px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      h5 {
        margin: 0 0 12px 0;
        display: flex;
        align-items: center;
        gap: 6px;
        color: var(--qms-text-secondary);
        font-size: 14px;
        font-weight: 500;
      }
      
      .recommendation-list {
        display: flex;
        flex-direction: column;
        gap: 8px;
        
        .recommendation-item {
          padding: 12px;
          border: 1px solid var(--qms-border-light);
          border-radius: 6px;
          cursor: pointer;
          transition: all 0.2s ease;
          background: var(--qms-bg-secondary);
          
          &:hover {
            border-color: var(--qms-primary);
            box-shadow: var(--qms-shadow-sm);
            transform: translateY(-1px);
          }
          
          &.personalized {
            border-left: 3px solid var(--qms-primary);
          }
          
          &.popular {
            border-left: 3px solid var(--qms-success);
          }
          
          &.quick-start {
            border-left: 3px solid var(--qms-info);
          }
          
          .item-content {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            gap: 8px;
            margin-bottom: 6px;
            
            .item-text {
              flex: 1;
              color: var(--qms-text-primary);
              font-size: 14px;
              line-height: 1.4;
            }
          }
          
          .item-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-size: 12px;
            color: var(--qms-text-tertiary);
            
            .item-reason {
              flex: 1;
            }
            
            .item-score {
              color: var(--qms-primary);
              font-weight: 500;
            }
            
            .item-popularity {
              display: flex;
              align-items: center;
              gap: 4px;
              color: var(--qms-success);
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .smart-recommendations {
    padding: 12px;
    
    .recommendations-content {
      .recommendation-section {
        .recommendation-list {
          .recommendation-item {
            padding: 10px;
            
            .item-content {
              flex-direction: column;
              align-items: flex-start;
              gap: 6px;
            }
            
            .item-meta {
              flex-direction: column;
              align-items: flex-start;
              gap: 4px;
            }
          }
        }
      }
    }
  }
}
</style>
