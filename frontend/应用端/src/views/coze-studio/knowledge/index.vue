<template>
  <div class="knowledge-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>知识库管理</h1>
        <p>管理您的文档和知识资源</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="showCreateDialog = true">
          创建知识库
        </el-button>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchQuery"
            placeholder="搜索知识库..."
            :prefix-icon="Search"
            clearable
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable>
            <el-option label="全部" value="" />
            <el-option label="已索引" value="indexed" />
            <el-option label="索引中" value="indexing" />
            <el-option label="待索引" value="pending" />
            <el-option label="索引失败" value="failed" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="typeFilter" placeholder="类型筛选" clearable>
            <el-option label="全部" value="" />
            <el-option label="文档" value="document" />
            <el-option label="网页" value="webpage" />
            <el-option label="API" value="api" />
            <el-option label="数据库" value="database" />
          </el-select>
        </el-col>
      </el-row>
    </div>

    <!-- 知识库列表 -->
    <div class="knowledge-grid">
      <div
        v-for="knowledge in filteredKnowledge"
        :key="knowledge.id"
        class="knowledge-card"
        @click="openKnowledge(knowledge)"
      >
        <div class="knowledge-header">
          <div class="knowledge-icon">
            <el-icon>
              <component :is="getTypeIcon(knowledge.type)" />
            </el-icon>
          </div>
          <div class="knowledge-status">
            <el-tag :type="getStatusType(knowledge.status)" size="small">
              {{ getStatusText(knowledge.status) }}
            </el-tag>
          </div>
        </div>
        
        <div class="knowledge-content">
          <h3>{{ knowledge.name }}</h3>
          <p>{{ knowledge.description }}</p>
          
          <div class="knowledge-stats">
            <div class="stat-item">
              <span class="stat-label">文档数</span>
              <span class="stat-value">{{ knowledge.documentCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">大小</span>
              <span class="stat-value">{{ formatSize(knowledge.size) }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">查询次数</span>
              <span class="stat-value">{{ knowledge.queryCount }}</span>
            </div>
          </div>
          
          <div class="knowledge-meta">
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(knowledge.updatedAt) }}</span>
            </div>
            <div class="meta-item">
              <el-icon><DataAnalysis /></el-icon>
              <span>{{ knowledge.accuracy }}% 准确率</span>
            </div>
          </div>
        </div>
        
        <div class="knowledge-actions">
          <el-button size="small" @click.stop="queryKnowledge(knowledge)">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button size="small" @click.stop="manageKnowledge(knowledge)">
            <el-icon><Setting /></el-icon>
            管理
          </el-button>
          <el-dropdown @command="handleKnowledgeAction" trigger="click" @click.stop>
            <el-button size="small" :icon="MoreFilled" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="`reindex-${knowledge.id}`">
                  <el-icon><Refresh /></el-icon>
                  重新索引
                </el-dropdown-item>
                <el-dropdown-item :command="`export-${knowledge.id}`">
                  <el-icon><Download /></el-icon>
                  导出
                </el-dropdown-item>
                <el-dropdown-item :command="`delete-${knowledge.id}`" divided>
                  <el-icon><Delete /></el-icon>
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="filteredKnowledge.length === 0" class="empty-state">
      <el-empty description="暂无知识库">
        <el-button type="primary" @click="showCreateDialog = true">
          创建第一个知识库
        </el-button>
      </el-empty>
    </div>

    <!-- 创建知识库对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建知识库"
      width="600px"
    >
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="createForm.name" placeholder="请输入知识库名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入知识库描述"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="createForm.type" placeholder="选择知识库类型">
            <el-option label="文档知识库" value="document" />
            <el-option label="网页知识库" value="webpage" />
            <el-option label="API知识库" value="api" />
            <el-option label="数据库知识库" value="database" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateKnowledge">创建</el-button>
      </template>
    </el-dialog>

    <!-- 查询知识库对话框 -->
    <el-dialog
      v-model="showQueryDialog"
      title="知识库查询"
      width="800px"
    >
      <div class="query-panel">
        <div class="query-input">
          <el-input
            v-model="queryInput"
            placeholder="输入您的问题..."
            type="textarea"
            :rows="3"
          />
          <el-button
            type="primary"
            @click="performQuery"
            :loading="queryLoading"
            style="margin-top: 12px;"
          >
            查询
          </el-button>
        </div>
        
        <div v-if="queryResults.length > 0" class="query-results">
          <h4>查询结果</h4>
          <div
            v-for="result in queryResults"
            :key="result.id"
            class="result-item"
          >
            <div class="result-header">
              <span class="result-title">{{ result.title }}</span>
              <span class="result-score">相关度: {{ result.score }}%</span>
            </div>
            <div class="result-content">{{ result.content }}</div>
            <div class="result-source">来源: {{ result.source }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Plus,
  Search,
  Document,
  Calendar,
  DataAnalysis,
  Setting,
  MoreFilled,
  Refresh,
  Download,
  Delete,
  Link,
  Connection,
  DataBoard
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const statusFilter = ref('')
const typeFilter = ref('')
const showCreateDialog = ref(false)
const showQueryDialog = ref(false)
const selectedKnowledge = ref(null)
const queryInput = ref('')
const queryLoading = ref(false)
const queryResults = ref([])

// 创建表单
const createForm = reactive({
  name: '',
  description: '',
  type: ''
})

// 知识库列表
const knowledgeList = reactive([
  {
    id: 1,
    name: '产品文档知识库',
    description: '包含所有产品相关的技术文档、用户手册和FAQ',
    type: 'document',
    status: 'indexed',
    documentCount: 156,
    size: 25600000, // bytes
    queryCount: 1245,
    accuracy: 92,
    createdAt: new Date('2024-01-15'),
    updatedAt: new Date('2024-01-20')
  },
  {
    id: 2,
    name: '客服知识库',
    description: '客服常见问题和解决方案的知识库',
    type: 'webpage',
    status: 'indexing',
    documentCount: 89,
    size: 12800000,
    queryCount: 567,
    accuracy: 88,
    createdAt: new Date('2024-01-18'),
    updatedAt: new Date('2024-01-19')
  },
  {
    id: 3,
    name: 'API文档知识库',
    description: '系统API接口文档和开发指南',
    type: 'api',
    status: 'indexed',
    documentCount: 45,
    size: 8900000,
    queryCount: 234,
    accuracy: 95,
    createdAt: new Date('2024-01-10'),
    updatedAt: new Date('2024-01-22')
  }
])

// 计算属性
const filteredKnowledge = computed(() => {
  return knowledgeList.filter(knowledge => {
    const matchesSearch = !searchQuery.value || 
      knowledge.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      knowledge.description.toLowerCase().includes(searchQuery.value.toLowerCase())
    
    const matchesStatus = !statusFilter.value || knowledge.status === statusFilter.value
    const matchesType = !typeFilter.value || knowledge.type === typeFilter.value
    
    return matchesSearch && matchesStatus && matchesType
  })
})

// 方法
const openKnowledge = (knowledge) => {
  router.push(`/coze-plugins/knowledge/${knowledge.id}`)
}

const queryKnowledge = (knowledge) => {
  selectedKnowledge.value = knowledge
  queryResults.value = []
  queryInput.value = ''
  showQueryDialog.value = true
}

const manageKnowledge = (knowledge) => {
  router.push(`/coze-plugins/knowledge/${knowledge.id}/manage`)
}

const performQuery = async () => {
  if (!queryInput.value.trim()) {
    ElMessage.warning('请输入查询内容')
    return
  }
  
  queryLoading.value = true
  
  try {
    // 模拟查询过程
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    queryResults.value = [
      {
        id: 1,
        title: '相关文档标题1',
        content: '这是查询到的相关内容片段，包含了用户问题的答案...',
        source: '产品手册第3章',
        score: 95
      },
      {
        id: 2,
        title: '相关文档标题2',
        content: '另一个相关的内容片段，提供了补充信息...',
        source: 'FAQ文档',
        score: 87
      }
    ]
    
    selectedKnowledge.value.queryCount++
    
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    queryLoading.value = false
  }
}

const handleKnowledgeAction = async (command) => {
  const [action, knowledgeId] = command.split('-')
  const knowledge = knowledgeList.find(k => k.id === parseInt(knowledgeId))
  
  if (!knowledge) return
  
  switch (action) {
    case 'reindex':
      await reindexKnowledge(knowledge)
      break
    case 'export':
      exportKnowledge(knowledge)
      break
    case 'delete':
      await deleteKnowledge(knowledge)
      break
  }
}

const reindexKnowledge = async (knowledge) => {
  try {
    await ElMessageBox.confirm('确定要重新索引这个知识库吗？', '确认重新索引', {
      type: 'warning'
    })
    
    knowledge.status = 'indexing'
    ElMessage.success('开始重新索引')
    
    // 模拟索引过程
    setTimeout(() => {
      knowledge.status = 'indexed'
      ElMessage.success('重新索引完成')
    }, 3000)
    
  } catch {
    // 用户取消
  }
}

const exportKnowledge = (knowledge) => {
  // 模拟导出功能
  const data = JSON.stringify(knowledge, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${knowledge.name}.json`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('知识库导出成功')
}

const deleteKnowledge = async (knowledge) => {
  try {
    await ElMessageBox.confirm('确定要删除这个知识库吗？此操作不可恢复。', '确认删除', {
      type: 'warning'
    })
    
    const index = knowledgeList.findIndex(k => k.id === knowledge.id)
    if (index > -1) {
      knowledgeList.splice(index, 1)
      ElMessage.success('知识库已删除')
    }
  } catch {
    // 用户取消
  }
}

const handleCreateKnowledge = () => {
  if (!createForm.name.trim()) {
    ElMessage.warning('请输入知识库名称')
    return
  }
  
  const newKnowledge = {
    id: Date.now(),
    name: createForm.name,
    description: createForm.description,
    type: createForm.type,
    status: 'pending',
    documentCount: 0,
    size: 0,
    queryCount: 0,
    accuracy: 0,
    createdAt: new Date(),
    updatedAt: new Date()
  }
  
  knowledgeList.push(newKnowledge)
  showCreateDialog.value = false
  
  // 重置表单
  Object.assign(createForm, {
    name: '',
    description: '',
    type: ''
  })
  
  ElMessage.success('知识库创建成功')
  
  // 跳转到管理页面
  router.push(`/coze-plugins/knowledge/${newKnowledge.id}/manage`)
}

const getTypeIcon = (type) => {
  const iconMap = {
    document: Document,
    webpage: Link,
    api: Connection,
    database: DataBoard
  }
  return iconMap[type] || Document
}

const getStatusType = (status) => {
  const typeMap = {
    indexed: 'success',
    indexing: 'warning',
    pending: 'info',
    failed: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    indexed: '已索引',
    indexing: '索引中',
    pending: '待索引',
    failed: '索引失败'
  }
  return textMap[status] || status
}

const formatSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString()
}

onMounted(() => {
  // 页面初始化
})
</script>

<style lang="scss" scoped>
@import "@/styles/variables.scss";
@import "@/styles/responsive.scss";
@import "@/styles/modern-theme.scss";

.knowledge-page {
  padding: 20px;
  background: var(--el-bg-color-page);
  min-height: calc(100vh - 60px);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;

    .header-left {
      h1 {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      p {
        margin: 0;
        color: var(--el-text-color-regular);
      }
    }
  }

  .filter-section {
    margin-bottom: 24px;
  }

  .knowledge-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 20px;
    margin-bottom: 24px;

    .knowledge-card {
      background: white;
      border-radius: 12px;
      padding: 20px;
      border: 1px solid var(--el-border-color-light);
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        border-color: var(--el-color-primary);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      .knowledge-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;

        .knowledge-icon {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          background: var(--el-color-primary-light-9);
          display: flex;
          align-items: center;
          justify-content: center;
          color: var(--el-color-primary);
          font-size: 24px;
        }
      }

      .knowledge-content {
        margin-bottom: 16px;

        h3 {
          margin: 0 0 8px 0;
          font-size: 16px;
          font-weight: 600;
          color: var(--el-text-color-primary);
        }

        p {
          margin: 0 0 16px 0;
          font-size: 14px;
          color: var(--el-text-color-regular);
          line-height: 1.5;
        }

        .knowledge-stats {
          display: flex;
          gap: 16px;
          margin-bottom: 12px;

          .stat-item {
            display: flex;
            flex-direction: column;
            align-items: center;

            .stat-label {
              font-size: 12px;
              color: var(--el-text-color-secondary);
              margin-bottom: 2px;
            }

            .stat-value {
              font-size: 14px;
              font-weight: 600;
              color: var(--el-text-color-primary);
            }
          }
        }

        .knowledge-meta {
          display: flex;
          gap: 16px;

          .meta-item {
            display: flex;
            align-items: center;
            gap: 4px;
            font-size: 12px;
            color: var(--el-text-color-secondary);

            .el-icon {
              font-size: 14px;
            }
          }
        }
      }

      .knowledge-actions {
        display: flex;
        gap: 8px;
      }
    }
  }

  .query-panel {
    .query-input {
      margin-bottom: 24px;
    }

    .query-results {
      h4 {
        margin: 0 0 16px 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .result-item {
        background: var(--el-fill-color-light);
        border-radius: 8px;
        padding: 16px;
        margin-bottom: 12px;

        .result-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .result-title {
            font-weight: 600;
            color: var(--el-text-color-primary);
          }

          .result-score {
            font-size: 12px;
            color: var(--el-color-success);
          }
        }

        .result-content {
          margin-bottom: 8px;
          line-height: 1.5;
          color: var(--el-text-color-regular);
        }

        .result-source {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }
  }
}
</style>
