<template>
  <div class="enhanced-search">
    <div class="search-container">
      <!-- 主搜索框 -->
      <el-autocomplete
        v-model="searchQuery"
        :fetch-suggestions="fetchSuggestions"
        :trigger-on-focus="true"
        :select-when-unmatched="true"
        placeholder="搜索历史对话、知识库..."
        class="search-input"
        size="large"
        @select="handleSelect"
        @keyup.enter="handleSearch"
        @input="handleInput"
        clearable
      >
        <template #prefix>
          <el-icon class="search-icon"><Search /></el-icon>
        </template>
        
        <template #suffix>
          <div class="search-actions">
            <el-button 
              :icon="Filter" 
              circle 
              size="small" 
              @click="showFilters = !showFilters"
              :type="hasActiveFilters ? 'primary' : ''"
            />
            <el-button 
              :icon="Search" 
              circle 
              size="small" 
              type="primary"
              @click="handleSearch"
              :loading="searching"
            />
          </div>
        </template>
        
        <template #default="{ item }">
          <div class="suggestion-item">
            <div class="suggestion-content">
              <el-icon class="suggestion-icon">
                <component :is="getSuggestionIcon(item.type)" />
              </el-icon>
              <div class="suggestion-text">
                <div class="suggestion-title" v-html="highlightMatch(item.title, searchQuery)"></div>
                <div class="suggestion-meta">
                  <span class="suggestion-type">{{ getSuggestionTypeText(item.type) }}</span>
                  <span class="suggestion-time">{{ formatTime(item.time) }}</span>
                </div>
              </div>
            </div>
            <div class="suggestion-score" v-if="item.score">
              {{ Math.round(item.score * 100) }}%
            </div>
          </div>
        </template>
      </el-autocomplete>
      
      <!-- 搜索过滤器 -->
      <el-collapse-transition>
        <div v-show="showFilters" class="search-filters">
          <el-row :gutter="16">
            <el-col :span="6">
              <el-select v-model="filters.type" placeholder="内容类型" clearable size="small">
                <el-option label="全部" value="" />
                <el-option label="对话记录" value="conversation" />
                <el-option label="知识库" value="knowledge" />
                <el-option label="文档" value="document" />
                <el-option label="配置" value="config" />
              </el-select>
            </el-col>
            
            <el-col :span="6">
              <el-select v-model="filters.timeRange" placeholder="时间范围" clearable size="small">
                <el-option label="今天" value="today" />
                <el-option label="本周" value="week" />
                <el-option label="本月" value="month" />
                <el-option label="全部" value="" />
              </el-select>
            </el-col>
            
            <el-col :span="6">
              <el-select v-model="filters.model" placeholder="AI模型" clearable size="small">
                <el-option label="全部模型" value="" />
                <el-option label="GPT-4o" value="gpt-4o" />
                <el-option label="Claude-3" value="claude-3" />
                <el-option label="DeepSeek" value="deepseek" />
              </el-select>
            </el-col>
            
            <el-col :span="6">
              <div class="filter-actions">
                <el-button size="small" @click="applyFilters">应用</el-button>
                <el-button size="small" @click="clearFilters">清除</el-button>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-collapse-transition>
    </div>
    
    <!-- 搜索结果 -->
    <div v-if="showResults" class="search-results">
      <div class="results-header">
        <h4>搜索结果 ({{ searchResults.length }})</h4>
        <el-button size="small" text @click="showResults = false">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
      
      <div class="results-list">
        <div
          v-for="result in paginatedResults"
          :key="result.id"
          class="result-item"
          @click="selectResult(result)"
        >
          <div class="result-content">
            <div class="result-header">
              <el-icon class="result-icon">
                <component :is="getSuggestionIcon(result.type)" />
              </el-icon>
              <h5 class="result-title" v-html="highlightMatch(result.title, searchQuery)"></h5>
              <div class="result-score">{{ Math.round(result.score * 100) }}%</div>
            </div>
            
            <div class="result-snippet" v-html="highlightMatch(result.snippet, searchQuery)"></div>
            
            <div class="result-meta">
              <el-tag :type="getTypeTagType(result.type)" size="small">
                {{ getSuggestionTypeText(result.type) }}
              </el-tag>
              <span class="result-time">{{ formatTime(result.time) }}</span>
              <span v-if="result.model" class="result-model">{{ result.model }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div v-if="searchResults.length > pageSize" class="results-pagination">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="searchResults.length"
          layout="prev, pager, next"
          small
        />
      </div>
    </div>
    
    <!-- 搜索历史 -->
    <div v-if="showSearchHistory && searchHistory.length > 0" class="search-history">
      <div class="history-header">
        <h5>搜索历史</h5>
        <el-button size="small" text @click="clearSearchHistory">清除</el-button>
      </div>
      <div class="history-list">
        <el-tag
          v-for="(query, index) in searchHistory.slice(0, 10)"
          :key="index"
          class="history-tag"
          @click="searchQuery = query; handleSearch()"
          closable
          @close="removeFromHistory(index)"
        >
          {{ query }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { 
  Search, 
  Filter, 
  Close, 
  ChatDotRound, 
  Document, 
  Setting, 
  Star,
  Clock
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// Props
const props = defineProps({
  placeholder: {
    type: String,
    default: '搜索历史对话、知识库...'
  }
})

// Emits
const emit = defineEmits(['search', 'select'])

// 响应式数据
const searchQuery = ref('')
const searching = ref(false)
const showFilters = ref(false)
const showResults = ref(false)
const showSearchHistory = ref(false)
const searchResults = ref([])
const searchHistory = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

// 过滤器
const filters = ref({
  type: '',
  timeRange: '',
  model: ''
})

// 计算属性
const hasActiveFilters = computed(() => {
  return Object.values(filters.value).some(value => value !== '')
})

const paginatedResults = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return searchResults.value.slice(start, end)
})

// 模拟搜索建议数据
const mockSuggestions = [
  {
    id: 1,
    title: '如何建立ISO 9001质量管理体系？',
    type: 'conversation',
    time: new Date(Date.now() - 2 * 60 * 60 * 1000),
    score: 0.95,
    snippet: '建立ISO 9001质量管理体系需要遵循PDCA循环...'
  },
  {
    id: 2,
    title: '质量控制流程优化方案',
    type: 'document',
    time: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
    score: 0.88,
    snippet: '通过统计过程控制(SPC)方法可以有效提升质量控制效率...'
  },
  {
    id: 3,
    title: 'AI模型配置参数',
    type: 'config',
    time: new Date(Date.now() - 30 * 60 * 1000),
    score: 0.82,
    snippet: 'GPT-4o模型的温度参数设置为0.7，最大令牌数为4096...'
  }
]

// 方法
const fetchSuggestions = async (queryString, callback) => {
  if (!queryString) {
    callback([])
    return
  }
  
  // 模拟API调用延迟
  setTimeout(() => {
    const suggestions = mockSuggestions.filter(item =>
      item.title.toLowerCase().includes(queryString.toLowerCase()) ||
      item.snippet.toLowerCase().includes(queryString.toLowerCase())
    )
    callback(suggestions)
  }, 200)
}

const handleInput = (value) => {
  showSearchHistory.value = !value && searchHistory.value.length > 0
}

const handleSelect = (item) => {
  searchQuery.value = item.title
  emit('select', item)
  addToSearchHistory(item.title)
}

const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  searching.value = true
  showResults.value = true
  showSearchHistory.value = false
  
  try {
    // 模拟搜索API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 模拟搜索结果
    searchResults.value = mockSuggestions.filter(item =>
      item.title.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      item.snippet.toLowerCase().includes(searchQuery.value.toLowerCase())
    )
    
    addToSearchHistory(searchQuery.value)
    emit('search', {
      query: searchQuery.value,
      filters: filters.value,
      results: searchResults.value
    })
    
  } catch (error) {
    ElMessage.error('搜索失败，请重试')
  } finally {
    searching.value = false
  }
}

const selectResult = (result) => {
  emit('select', result)
  showResults.value = false
}

const applyFilters = () => {
  handleSearch()
}

const clearFilters = () => {
  filters.value = {
    type: '',
    timeRange: '',
    model: ''
  }
}

const addToSearchHistory = (query) => {
  if (!query || searchHistory.value.includes(query)) return
  
  searchHistory.value.unshift(query)
  if (searchHistory.value.length > 20) {
    searchHistory.value = searchHistory.value.slice(0, 20)
  }
  
  // 保存到本地存储
  localStorage.setItem('qms-search-history', JSON.stringify(searchHistory.value))
}

const removeFromHistory = (index) => {
  searchHistory.value.splice(index, 1)
  localStorage.setItem('qms-search-history', JSON.stringify(searchHistory.value))
}

const clearSearchHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('qms-search-history')
  showSearchHistory.value = false
}

const getSuggestionIcon = (type) => {
  const iconMap = {
    conversation: ChatDotRound,
    document: Document,
    config: Setting,
    knowledge: Star
  }
  return iconMap[type] || Search
}

const getSuggestionTypeText = (type) => {
  const typeMap = {
    conversation: '对话记录',
    document: '文档',
    config: '配置',
    knowledge: '知识库'
  }
  return typeMap[type] || '未知'
}

const getTypeTagType = (type) => {
  const typeMap = {
    conversation: 'primary',
    document: 'success',
    config: 'warning',
    knowledge: 'info'
  }
  return typeMap[type] || ''
}

const highlightMatch = (text, query) => {
  if (!query) return text
  
  const regex = new RegExp(`(${query})`, 'gi')
  return text.replace(regex, '<mark>$1</mark>')
}

const formatTime = (time) => {
  const now = new Date()
  const diff = now - new Date(time)
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  return `${days}天前`
}

// 生命周期
onMounted(() => {
  // 加载搜索历史
  const saved = localStorage.getItem('qms-search-history')
  if (saved) {
    try {
      searchHistory.value = JSON.parse(saved)
    } catch (error) {
      console.error('加载搜索历史失败:', error)
    }
  }
})

// 监听搜索框焦点
watch(searchQuery, (newValue) => {
  if (!newValue) {
    showResults.value = false
    showSearchHistory.value = searchHistory.value.length > 0
  }
})
</script>

<style lang="scss" scoped>
.enhanced-search {
  .search-container {
    .search-input {
      width: 100%;
      
      :deep(.el-input__inner) {
        border-radius: 8px;
        border: 2px solid var(--qms-border-normal);
        transition: all 0.3s ease;
        
        &:focus {
          border-color: var(--qms-primary);
          box-shadow: 0 0 0 3px rgba(79, 172, 254, 0.1);
        }
      }
    }
    
    .search-actions {
      display: flex;
      gap: 4px;
    }
    
    .search-filters {
      margin-top: 12px;
      padding: 16px;
      background: var(--qms-bg-secondary);
      border-radius: 8px;
      border: 1px solid var(--qms-border-light);
      
      .filter-actions {
        display: flex;
        gap: 8px;
        justify-content: flex-end;
      }
    }
  }
  
  .suggestion-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    
    .suggestion-content {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;
      
      .suggestion-icon {
        color: var(--qms-primary);
      }
      
      .suggestion-text {
        flex: 1;
        
        .suggestion-title {
          font-weight: 500;
          color: var(--qms-text-primary);
          margin-bottom: 4px;
          
          :deep(mark) {
            background: var(--qms-primary);
            color: white;
            padding: 1px 3px;
            border-radius: 2px;
          }
        }
        
        .suggestion-meta {
          display: flex;
          gap: 12px;
          font-size: 12px;
          color: var(--qms-text-tertiary);
        }
      }
    }
    
    .suggestion-score {
      font-size: 12px;
      color: var(--qms-primary);
      font-weight: 500;
    }
  }
  
  .search-results {
    margin-top: 16px;
    border: 1px solid var(--qms-border-normal);
    border-radius: 8px;
    background: var(--qms-bg-primary);
    
    .results-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid var(--qms-border-light);
      
      h4 {
        margin: 0;
        color: var(--qms-text-primary);
      }
    }
    
    .results-list {
      max-height: 400px;
      overflow-y: auto;
      
      .result-item {
        padding: 16px;
        border-bottom: 1px solid var(--qms-border-light);
        cursor: pointer;
        transition: background-color 0.2s ease;
        
        &:hover {
          background: var(--qms-bg-secondary);
        }
        
        &:last-child {
          border-bottom: none;
        }
        
        .result-header {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 8px;
          
          .result-icon {
            color: var(--qms-primary);
          }
          
          .result-title {
            flex: 1;
            margin: 0;
            font-size: 14px;
            font-weight: 500;
            color: var(--qms-text-primary);
            
            :deep(mark) {
              background: var(--qms-primary);
              color: white;
              padding: 1px 3px;
              border-radius: 2px;
            }
          }
          
          .result-score {
            font-size: 12px;
            color: var(--qms-primary);
            font-weight: 500;
          }
        }
        
        .result-snippet {
          font-size: 13px;
          color: var(--qms-text-secondary);
          line-height: 1.4;
          margin-bottom: 8px;
          
          :deep(mark) {
            background: var(--qms-primary);
            color: white;
            padding: 1px 3px;
            border-radius: 2px;
          }
        }
        
        .result-meta {
          display: flex;
          align-items: center;
          gap: 12px;
          font-size: 12px;
          color: var(--qms-text-tertiary);
        }
      }
    }
    
    .results-pagination {
      padding: 16px;
      text-align: center;
      border-top: 1px solid var(--qms-border-light);
    }
  }
  
  .search-history {
    margin-top: 12px;
    
    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      h5 {
        margin: 0;
        font-size: 14px;
        color: var(--qms-text-secondary);
      }
    }
    
    .history-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .history-tag {
        cursor: pointer;
        transition: all 0.2s ease;
        
        &:hover {
          transform: translateY(-1px);
          box-shadow: var(--qms-shadow-sm);
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .enhanced-search {
    .search-filters {
      .el-row {
        .el-col {
          margin-bottom: 8px;
        }
      }
    }
    
    .search-results {
      .results-list {
        max-height: 300px;
      }
    }
  }
}
</style>
