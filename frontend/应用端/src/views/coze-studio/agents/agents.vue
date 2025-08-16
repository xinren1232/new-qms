<template>
  <div class="agents-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>Agent 管理</h1>
        <p class="description">创建和管理您的 AI Agent</p>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="showCreateDialog = true">
          创建 Agent
        </el-button>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filters">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input
            v-model="searchQuery"
            placeholder="搜索 Agent..."
            :prefix-icon="Search"
            clearable
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="已发布" value="published" />
            <el-option label="草稿" value="draft" />
            <el-option label="已归档" value="archived" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="categoryFilter" placeholder="分类" clearable>
            <el-option label="全部" value="" />
            <el-option label="客服助手" value="customer-service" />
            <el-option label="内容创作" value="content-creation" />
            <el-option label="数据分析" value="data-analysis" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="sortBy" placeholder="排序">
            <el-option label="最近更新" value="updated" />
            <el-option label="创建时间" value="created" />
            <el-option label="使用次数" value="usage" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <div class="view-controls">
            <el-button-group>
              <el-button
                :type="viewMode === 'grid' ? 'primary' : ''"
                :icon="Grid"
                @click="viewMode = 'grid'"
              />
              <el-button
                :type="viewMode === 'list' ? 'primary' : ''"
                :icon="List"
                @click="viewMode = 'list'"
              />
            </el-button-group>
          </div>
        </el-col>
      </el-row>

    <!-- 开源Agent模板推荐 -->
    <el-card class="plugins-card" style="margin-bottom:16px;">
      <div class="card-header" style="display:flex;justify-content:space-between;align-items:center;">
        <span>开源Agent模板推荐</span>
        <small style="color:#909399;">一键创建并可立即测试</small>
      </div>
      <el-row :gutter="16">
        <el-col :span="8" v-for="tpl in curatedAgents" :key="tpl.id">
          <el-card>
            <div style="font-weight:600;margin-bottom:4px;">{{ tpl.name }}</div>
            <div style="color:#909399;margin-bottom:8px;">{{ tpl.description }}</div>
            <el-button size="small" type="primary" @click="createFromTemplate(tpl)">一键创建</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    </div>

    <!-- Agent 列表 -->
    <div class="agents-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>

      <!-- 空状态 -->
      <div v-else-if="agents.length === 0" class="empty-container">
        <el-empty description="暂无Agent数据">
          <el-button type="primary" @click="showCreateDialog = true">创建第一个Agent</el-button>
        </el-empty>
      </div>

      <!-- Agent网格视图 -->
      <div v-else-if="viewMode === 'grid'" class="agents-grid">
        <div
          v-for="agent in filteredAgents"
          :key="agent.id"
          class="agent-card"
          @click="selectAgent(agent)"
        >
          <div class="card-header">
            <div class="agent-avatar">
              <el-avatar :size="40" :src="agent.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
            </div>
            <div class="agent-status">
              <el-tag
                :type="getStatusType(agent.status)"
                size="small"
              >
                {{ getStatusText(agent.status) }}
              </el-tag>
            </div>
          </div>

          <div class="card-content">
            <h3 class="agent-name">{{ agent.name }}</h3>
            <p class="agent-description">{{ agent.description }}</p>

            <div class="agent-stats">
              <div class="stat-item">
                <el-icon><ChatDotRound /></el-icon>
                <span>{{ agent.conversationCount || 0 }} 对话</span>
              </div>
              <div class="stat-item">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatDate(agent.updatedAt) }}</span>
              </div>
            </div>
          </div>

          <div class="card-actions">
            <el-button size="small" @click.stop="testAgent(agent)">
              <el-icon><VideoPlay /></el-icon>
              测试
            </el-button>
            <el-button size="small" @click.stop="editAgent(agent)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-dropdown @command="handleAgentAction" trigger="click">
              <el-button size="small">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="{action: 'duplicate', agent}">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'archive', agent}">
                    <el-icon><Box /></el-icon>
                    归档
                  </el-dropdown-item>
                  <el-dropdown-item :command="{action: 'delete', agent}" divided>
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建 Agent 对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建 Agent"
      width="600px"
    >
      <el-form :model="newAgent" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="newAgent.name" placeholder="请输入 Agent 名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="newAgent.description"
            type="textarea"
            :rows="3"
            placeholder="请输入 Agent 描述"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="newAgent.category" placeholder="请选择分类">
            <el-option label="客服助手" value="customer-service" />
            <el-option label="内容创作" value="content-creation" />
            <el-option label="数据分析" value="data-analysis" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createAgent">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getAgents, createAgent as apiCreateAgent, executePlugin as apiExecutePlugin } from '@/api/coze-studio'
import {
  Plus,
  Search,
  User,
  Calendar,
  ChatDotRound,
  VideoPlay,
  Edit,
  MoreFilled,
  CopyDocument,
  Box,
  Delete,
  Grid,
  List
} from '@element-plus/icons-vue'

// 响应式数据
const searchQuery = ref('')
const statusFilter = ref('')
const categoryFilter = ref('')
const sortBy = ref('updated')
const viewMode = ref('grid')
const showCreateDialog = ref(false)
const showEditDialog = ref(false)
const selectedAgent = ref(null)


// 开源模板
const curatedAgents = ref([
  { id: 'data-analyst', name: '数据分析Agent', description: '读取Excel→统计→SPC→结论总结（无模型版示例）' },
  { id: 'file-parser', name: '文件解析Agent', description: 'PDF/Excel解析→结构化输出' }
])

// 增加更多模板
curatedAgents.value.push(
  { id: 'data-cleaner-agent', name: '数据清洗Agent', description: '基于数据清洗器(data_cleaner)快速清洗数据' },
  { id: 'anomaly-detector-agent', name: '异常检测Agent', description: '基于异常检测器(anomaly_detector)快速检测异常点' },
  { id: 'text-summarizer-agent', name: '文本摘要Agent', description: '基于文本摘要器(text_summarizer)快速生成摘要' }
)

async function createFromTemplate(tpl){
  try{
    const payload = {
      name: tpl.name,
      description: tpl.description,
      category: tpl.id === 'data-analyst' ? 'data-analysis' : 'general',
      config: {
        template: tpl.id,
        steps: tpl.id === 'data-analyst'
          ? ['excel_analyzer','statistical_analyzer','spc_controller']
          : tpl.id === 'data-cleaner-agent' ? ['data_cleaner']
          : tpl.id === 'anomaly-detector-agent' ? ['anomaly_detector']
          : tpl.id === 'text-summarizer-agent' ? ['text_summarizer']
          : ['pdf_parser']
      },
      metadata: { source: 'curated_template' }
    }
    const resp = await apiCreateAgent(payload)
    if(resp.success){
      ElMessage.success('模板已创建，可在列表中测试或编辑配置')
      await loadAgents()
    }
  } catch(e){
    ElMessage.error('创建失败：'+e.message)
  }
}

// 新建 Agent 表单
const newAgent = reactive({
  name: '',
  description: '',
  category: ''
})

// Agent 列表数据
const agents = ref([])
const loading = ref(false)

// 计算属性
const filteredAgents = computed(() => {
  let filtered = agents.filter(agent => {
    const matchesSearch = !searchQuery.value ||
      agent.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      agent.description.toLowerCase().includes(searchQuery.value.toLowerCase())

    const matchesStatus = !statusFilter.value || agent.status === statusFilter.value
    const matchesCategory = !categoryFilter.value || agent.category === categoryFilter.value

    return matchesSearch && matchesStatus && matchesCategory
  })

  // 排序
  filtered.sort((a, b) => {
    switch (sortBy.value) {
      case 'created':
        return new Date(b.createdAt) - new Date(a.createdAt)
      case 'usage':
        return b.conversationCount - a.conversationCount
      default: // updated
        return new Date(b.updatedAt) - new Date(a.updatedAt)
    }
  })

  return filtered
})

// 方法
const selectAgent = (agent) => {
  selectedAgent.value = agent
}

const editAgent = (agent) => {
  selectedAgent.value = { ...agent }
  showEditDialog.value = true
}

const testAgent = (agent) => {
  selectedAgent.value = agent
  ElMessage.success(`开始测试 ${agent.name}`)
}

const createAgent = () => {
  if (!newAgent.name || !newAgent.description || !newAgent.category) {
    ElMessage.warning('请填写完整信息')
    return
  }

  const agent = {
    id: Date.now(),
    ...newAgent,
    status: 'draft',
    conversationCount: 0,
    createdAt: new Date(),
    updatedAt: new Date()
  }

  agents.push(agent)
  showCreateDialog.value = false

  // 重置表单
  Object.assign(newAgent, {
    name: '',
    description: '',
    category: ''
  })

  ElMessage.success('Agent 创建成功')
}

const handleAgentAction = ({ action, agent }) => {
  switch (action) {
    case 'duplicate':
      ElMessage.success(`复制 ${agent.name}`)
      break
    case 'archive':
      agent.status = 'archived'
      ElMessage.success(`${agent.name} 已归档`)
      break
    case 'delete':
      const index = agents.findIndex(a => a.id === agent.id)
      if (index > -1) {
        agents.splice(index, 1)
        ElMessage.success(`${agent.name} 已删除`)
      }
      break
  }
}

const getStatusType = (status) => {
  const types = {
    published: 'success',
    draft: 'warning',
    archived: 'info'
  }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = {
    published: '已发布',
    draft: '草稿',
    archived: '已归档'
  }
  return texts[status] || status
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString()
}

// 加载Agent数据
const loadAgents = async () => {
  loading.value = true
  try {
    const response = await getAgents({
      page: 1,
      limit: 100
    })

    if (response.data?.items) {
      agents.value = response.data.items.map(agent => ({
        id: agent.id,
        name: agent.name,
        description: agent.description,
        avatar: agent.avatar || '',
        status: agent.status,
        category: agent.category,
        conversationCount: 0, // 暂时设为0，后续可以从对话表获取
        createdAt: new Date(agent.created_at),
        updatedAt: new Date(agent.updated_at)
      }))
    }
  } catch (error) {
    console.error('加载Agent失败:', error)
    ElMessage.error('加载Agent列表失败')
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadAgents()
})
</script>

<style scoped lang="scss">
.agents-page {
  padding: 24px;
  min-height: calc(100vh - 60px);

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;

    .header-left {
      h1 {
        margin: 0 0 8px 0;
        font-size: 28px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }

      .description {
        margin: 0;
        color: var(--el-text-color-regular);
        font-size: 14px;
      }
    }
  }

  .filters {
    margin-bottom: 24px;
    padding: 20px;
    background: var(--el-bg-color);
    border-radius: 8px;
    border: 1px solid var(--el-border-color-light);

    .view-controls {
      display: flex;
      justify-content: flex-end;
    }
  }

  .agents-container {
    .agents-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;

      .agent-card {
        background: var(--el-bg-color);
        border: 1px solid var(--el-border-color-light);
        border-radius: 12px;
        padding: 20px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: var(--el-color-primary);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
          transform: translateY(-2px);
        }

        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 16px;
        }

        .card-content {
          .agent-name {
            margin: 0 0 8px 0;
            font-size: 18px;
            font-weight: 600;
            color: var(--el-text-color-primary);
          }

          .agent-description {
            margin: 0 0 16px 0;
            color: var(--el-text-color-regular);
            font-size: 14px;
            line-height: 1.5;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
          }

          .agent-stats {
            display: flex;
            gap: 16px;
            margin-bottom: 16px;

            .stat-item {
              display: flex;
              align-items: center;
              gap: 4px;
              font-size: 12px;
              color: var(--el-text-color-regular);

              .el-icon {
                font-size: 14px;
              }
            }
          }
        }

        .card-actions {
          display: flex;
          gap: 8px;
          justify-content: flex-end;
        }
      }
    }
  }
}
</style>
