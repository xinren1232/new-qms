<template>
  <div class="workflows-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>工作流设计</h1>
        <p>创建和管理可视化工作流程</p>
      </div>
      <div class="header-right">
        <!-- 创建按钮组 -->
        <el-dropdown @command="handleCreateCommand" trigger="click">
          <el-button type="primary" :icon="Plus">
            创建工作流
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="blank">
                <el-icon><DocumentAdd /></el-icon>
                从空白开始
              </el-dropdown-item>
              <el-dropdown-item command="template">
                <el-icon><Collection /></el-icon>
                从模板创建
              </el-dropdown-item>
              <el-dropdown-item command="import">
                <el-icon><Upload /></el-icon>
                导入工作流
              </el-dropdown-item>
              <el-dropdown-item command="ai-generate">
                <el-icon><MagicStick /></el-icon>
                AI 生成工作流
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchQuery"
            placeholder="搜索工作流..."
            :prefix-icon="Search"
            clearable
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable>
            <el-option label="全部" value="" />
            <el-option label="运行中" value="running" />
            <el-option label="已停止" value="stopped" />
            <el-option label="草稿" value="draft" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="categoryFilter" placeholder="分类筛选" clearable>
            <el-option label="全部" value="" />
            <el-option label="数据处理" value="data-processing" />
            <el-option label="内容生成" value="content-generation" />
            <el-option label="自动化" value="automation" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-col>
      </el-row>
    </div>

    <!-- 工作流模板推荐 -->
    <div v-if="showTemplates" class="templates-section">
      <div class="section-header">
        <h2>工作流模板</h2>
        <el-button text @click="showTemplates = false">
          <el-icon><Close /></el-icon>
          收起
        </el-button>
      </div>
      <div class="templates-grid">
        <div
          v-for="template in workflowTemplates"
          :key="template.id"
          class="template-card"
          @click="createFromTemplate(template)"
        >
          <div class="template-preview">
            <div class="preview-canvas">
              <svg width="100%" height="80" viewBox="0 0 200 80">
                <defs>
                  <marker id="arrowhead" markerWidth="10" markerHeight="7"
                    refX="9" refY="3.5" orient="auto">
                    <polygon points="0 0, 10 3.5, 0 7" fill="#409EFF" />
                  </marker>
                </defs>
                <!-- 简化的工作流预览图 -->
                <circle cx="30" cy="40" r="8" fill="#67C23A" />
                <circle cx="100" cy="40" r="8" fill="#409EFF" />
                <circle cx="170" cy="40" r="8" fill="#E6A23C" />
                <line x1="38" y1="40" x2="92" y2="40" stroke="#409EFF"
                  stroke-width="2" marker-end="url(#arrowhead)" />
                <line x1="108" y1="40" x2="162" y2="40" stroke="#409EFF"
                  stroke-width="2" marker-end="url(#arrowhead)" />
              </svg>
            </div>
          </div>
          <div class="template-info">
            <h4>{{ template.name }}</h4>
            <p>{{ template.description }}</p>
            <div class="template-tags">
              <el-tag v-for="tag in template.tags" :key="tag" size="small">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 工作流列表 -->
    <div class="workflows-grid">
      <div
        v-for="workflow in filteredWorkflows"
        :key="workflow.id"
        class="workflow-card"
        @click="openWorkflow(workflow)"
      >
        <div class="workflow-header">
          <div class="workflow-preview">
            <!-- 可视化预览缩略图 -->
            <div class="preview-canvas">
              <svg width="100%" height="60" viewBox="0 0 200 60">
                <defs>
                  <marker id="arrow" markerWidth="10" markerHeight="7"
                    refX="9" refY="3.5" orient="auto">
                    <polygon points="0 0, 10 3.5, 0 7" fill="#409EFF" />
                  </marker>
                </defs>
                <!-- 根据节点数生成简化预览 -->
                <template v-for="(node, index) in Math.min(workflow.nodeCount, 4)" :key="index">
                  <circle
                    :cx="30 + index * 45"
                    cy="30"
                    r="6"
                    :fill="getNodeColor(index)"
                  />
                  <line
                    v-if="index < Math.min(workflow.nodeCount, 4) - 1"
                    :x1="36 + index * 45"
                    y1="30"
                    :x2="69 + index * 45"
                    y2="30"
                    stroke="#409EFF"
                    stroke-width="2"
                    marker-end="url(#arrow)"
                  />
                </template>
                <text v-if="workflow.nodeCount > 4" x="185" y="35"
                  font-size="12" fill="#909399">+{{ workflow.nodeCount - 4 }}</text>
              </svg>
            </div>
          </div>
          <div class="workflow-status">
            <el-tag :type="getStatusType(workflow.status)" size="small">
              {{ getStatusText(workflow.status) }}
            </el-tag>
          </div>
        </div>

        <div class="workflow-content">
          <div class="workflow-title">
            <h3>{{ workflow.name }}</h3>
            <div class="version-info">
              <el-tag size="small" type="info">v{{ workflow.version || '1.0' }}</el-tag>
            </div>
          </div>
          <p>{{ workflow.description }}</p>

          <div class="workflow-stats">
            <div class="stat-item">
              <span class="stat-label">节点数</span>
              <span class="stat-value">{{ workflow.nodeCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">执行次数</span>
              <span class="stat-value">{{ workflow.executionCount }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">成功率</span>
              <span class="stat-value">{{ workflow.successRate || '95%' }}</span>
            </div>
          </div>

          <div class="workflow-meta">
            <div class="meta-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(workflow.updatedAt) }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Timer /></el-icon>
              <span>{{ workflow.avgExecutionTime }}ms</span>
            </div>
            <div class="meta-item" v-if="workflow.collaborators">
              <el-icon><User /></el-icon>
              <span>{{ workflow.collaborators.length }} 协作者</span>
            </div>
          </div>
        </div>

        <div class="workflow-actions">
          <el-button size="small" @click.stop="runWorkflow(workflow)">
            <el-icon><VideoPlay /></el-icon>
            运行
          </el-button>
          <el-button size="small" @click.stop="editWorkflow(workflow)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-dropdown @command="handleWorkflowAction" trigger="click" @click.stop>
            <el-button size="small" :icon="MoreFilled" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="`duplicate-${workflow.id}`">
                  <el-icon><CopyDocument /></el-icon>
                  复制
                </el-dropdown-item>
                <el-dropdown-item :command="`version-${workflow.id}`">
                  <el-icon><FolderOpened /></el-icon>
                  版本管理
                </el-dropdown-item>
                <el-dropdown-item :command="`share-${workflow.id}`">
                  <el-icon><Share /></el-icon>
                  分享协作
                </el-dropdown-item>
                <el-dropdown-item :command="`export-${workflow.id}`">
                  <el-icon><Download /></el-icon>
                  导出
                </el-dropdown-item>
                <el-dropdown-item :command="`delete-${workflow.id}`" divided>
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
    <div v-if="filteredWorkflows.length === 0" class="empty-state">
      <el-empty description="暂无工作流">
        <el-button type="primary" @click="showCreateDialog = true">
          创建第一个工作流
        </el-button>
      </el-empty>
    </div>

    <!-- 模板选择对话框 -->
    <el-dialog
      v-model="showTemplateDialog"
      title="选择工作流模板"
      width="800px"
    >
      <div class="template-selector">
        <div class="template-categories">
          <el-radio-group v-model="selectedTemplateCategory">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="data-processing">数据处理</el-radio-button>
            <el-radio-button label="content-generation">内容生成</el-radio-button>
            <el-radio-button label="automation">自动化</el-radio-button>
          </el-radio-group>
        </div>

        <div class="template-list">
          <div
            v-for="template in filteredTemplates"
            :key="template.id"
            class="template-item"
            :class="{ active: selectedTemplate?.id === template.id }"
            @click="selectedTemplate = template"
          >
            <div class="template-preview-small">
              <svg width="60" height="40" viewBox="0 0 60 40">
                <circle cx="15" cy="20" r="4" fill="#67C23A" />
                <circle cx="30" cy="20" r="4" fill="#409EFF" />
                <circle cx="45" cy="20" r="4" fill="#E6A23C" />
                <line x1="19" y1="20" x2="26" y2="20" stroke="#409EFF" stroke-width="1" />
                <line x1="34" y1="20" x2="41" y2="20" stroke="#409EFF" stroke-width="1" />
              </svg>
            </div>
            <div class="template-details">
              <h4>{{ template.name }}</h4>
              <p>{{ template.description }}</p>
              <div class="template-meta">
                <el-tag size="small">{{ template.nodeCount }} 节点</el-tag>
                <el-tag size="small" type="success">{{ template.usageCount }} 使用</el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="showTemplateDialog = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!selectedTemplate"
          @click="createFromSelectedTemplate"
        >
          使用此模板
        </el-button>
      </template>
    </el-dialog>

    <!-- 版本管理对话框 -->
    <el-dialog
      v-model="showVersionDialog"
      title="版本管理"
      width="700px"
    >
      <div class="version-manager">
        <div class="version-header">
          <el-button type="primary" size="small" @click="createNewVersion">
            <el-icon><Plus /></el-icon>
            创建新版本
          </el-button>
        </div>

        <el-table :data="workflowVersions" style="width: 100%">
          <el-table-column prop="version" label="版本" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isCurrent ? 'success' : ''">
                v{{ row.version }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="author" label="作者" width="120" />
          <el-table-column prop="createdAt" label="创建时间" width="150">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button
                v-if="!row.isCurrent"
                size="small"
                @click="switchToVersion(row)"
              >
                切换
              </el-button>
              <el-button size="small" @click="compareVersion(row)">
                对比
              </el-button>
              <el-button
                v-if="!row.isCurrent"
                size="small"
                type="danger"
                @click="deleteVersion(row)"
              >
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- 协作分享对话框 -->
    <el-dialog
      v-model="showShareDialog"
      title="分享与协作"
      width="600px"
    >
      <div class="share-manager">
        <el-tabs v-model="shareActiveTab">
          <el-tab-pane label="团队协作" name="collaborate">
            <div class="collaborate-section">
              <div class="add-collaborator">
                <el-input
                  v-model="newCollaboratorEmail"
                  placeholder="输入邮箱地址邀请协作者"
                  style="width: 300px"
                >
                  <template #append>
                    <el-button @click="addCollaborator">邀请</el-button>
                  </template>
                </el-input>
              </div>

              <div class="collaborator-list">
                <div
                  v-for="collaborator in currentCollaborators"
                  :key="collaborator.id"
                  class="collaborator-item"
                >
                  <el-avatar :size="32" :src="collaborator.avatar">
                    {{ collaborator.name.charAt(0) }}
                  </el-avatar>
                  <div class="collaborator-info">
                    <div class="name">{{ collaborator.name }}</div>
                    <div class="email">{{ collaborator.email }}</div>
                  </div>
                  <el-select v-model="collaborator.role" size="small">
                    <el-option label="查看者" value="viewer" />
                    <el-option label="编辑者" value="editor" />
                    <el-option label="管理员" value="admin" />
                  </el-select>
                  <el-button size="small" type="danger" text @click="removeCollaborator(collaborator)">
                    移除
                  </el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="公开分享" name="share">
            <div class="share-section">
              <el-form label-width="100px">
                <el-form-item label="分享链接">
                  <el-input
                    v-model="shareLink"
                    readonly
                    style="width: 400px"
                  >
                    <template #append>
                      <el-button @click="copyShareLink">复制</el-button>
                    </template>
                  </el-input>
                </el-form-item>
                <el-form-item label="访问权限">
                  <el-radio-group v-model="sharePermission">
                    <el-radio label="view">仅查看</el-radio>
                    <el-radio label="copy">可复制</el-radio>
                    <el-radio label="edit">可编辑</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="有效期">
                  <el-select v-model="shareExpiry">
                    <el-option label="永久有效" value="never" />
                    <el-option label="7天" value="7d" />
                    <el-option label="30天" value="30d" />
                    <el-option label="90天" value="90d" />
                  </el-select>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 创建工作流对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建工作流"
      width="600px"
    >
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="createForm.name" placeholder="请输入工作流名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入工作流描述"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="createForm.category" placeholder="选择分类">
            <el-option label="数据处理" value="data-processing" />
            <el-option label="内容生成" value="content-generation" />
            <el-option label="自动化" value="automation" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateWorkflow">创建</el-button>
      </template>
    </el-dialog>

    <!-- 运行结果对话框 -->
    <el-dialog
      v-model="showRunDialog"
      title="工作流执行结果"
      width="800px"
    >
      <div class="run-result">
        <div class="result-header">
          <div class="result-status">
            <el-tag :type="runResult.success ? 'success' : 'danger'">
              {{ runResult.success ? '执行成功' : '执行失败' }}
            </el-tag>
          </div>
          <div class="result-time">
            执行时间: {{ runResult.executionTime }}ms
          </div>
        </div>
        
        <div class="result-content">
          <el-tabs>
            <el-tab-pane label="执行日志" name="logs">
              <div class="logs-container">
                <div
                  v-for="log in runResult.logs"
                  :key="log.id"
                  :class="['log-item', log.level]"
                >
                  <span class="log-time">{{ log.time }}</span>
                  <span class="log-message">{{ log.message }}</span>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="输出结果" name="output">
              <pre class="output-content">{{ runResult.output }}</pre>
            </el-tab-pane>
          </el-tabs>
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
  Connection,
  Calendar,
  Timer,
  VideoPlay,
  Edit,
  MoreFilled,
  CopyDocument,
  Download,
  Delete,
  Close,
  FolderOpened,
  Share,
  User,
  ArrowDown,
  DocumentAdd,
  Collection,
  Upload,
  MagicStick
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const statusFilter = ref('')
const categoryFilter = ref('')
const showCreateDialog = ref(false)
const showRunDialog = ref(false)
const showTemplateDialog = ref(false)
const showVersionDialog = ref(false)
const showShareDialog = ref(false)
const showTemplates = ref(true)
const selectedWorkflow = ref(null)
const selectedTemplate = ref(null)
const selectedTemplateCategory = ref('all')
const shareActiveTab = ref('collaborate')
const newCollaboratorEmail = ref('')
const shareLink = ref('https://coze.example.com/workflow/share/abc123')
const sharePermission = ref('view')
const shareExpiry = ref('never')

// 创建表单
const createForm = reactive({
  name: '',
  description: '',
  category: ''
})

// 运行结果
const runResult = reactive({
  success: false,
  executionTime: 0,
  logs: [],
  output: ''
})

// 工作流模板数据
const workflowTemplates = reactive([
  {
    id: 1,
    name: '数据处理模板',
    description: '标准的数据清洗和处理流程模板',
    category: 'data-processing',
    nodeCount: 5,
    usageCount: 156,
    tags: ['数据清洗', '验证', '转换']
  },
  {
    id: 2,
    name: '内容生成模板',
    description: 'AI驱动的内容创作和优化流程',
    category: 'content-generation',
    nodeCount: 4,
    usageCount: 89,
    tags: ['AI生成', '内容优化', '多语言']
  },
  {
    id: 3,
    name: '自动化审批模板',
    description: '智能审批和通知流程模板',
    category: 'automation',
    nodeCount: 6,
    usageCount: 234,
    tags: ['审批', '通知', '条件判断']
  }
])

// 版本管理数据
const workflowVersions = reactive([
  {
    id: 1,
    version: '2.1',
    description: '优化数据验证逻辑，提升处理效率',
    author: '张三',
    isCurrent: true,
    createdAt: new Date('2024-01-20')
  },
  {
    id: 2,
    version: '2.0',
    description: '重构工作流架构，增加错误处理',
    author: '李四',
    isCurrent: false,
    createdAt: new Date('2024-01-15')
  },
  {
    id: 3,
    version: '1.0',
    description: '初始版本',
    author: '张三',
    isCurrent: false,
    createdAt: new Date('2024-01-10')
  }
])

// 当前协作者数据
const currentCollaborators = reactive([
  {
    id: 1,
    name: '张三',
    email: 'zhang@example.com',
    avatar: '',
    role: 'admin'
  },
  {
    id: 2,
    name: '李四',
    email: 'li@example.com',
    avatar: '',
    role: 'editor'
  },
  {
    id: 3,
    name: '王五',
    email: 'wang@example.com',
    avatar: '',
    role: 'viewer'
  }
])

// 工作流列表
const workflows = reactive([
  {
    id: 1,
    name: '订单处理流程',
    description: '自动化处理订单信息，包括验证、库存检查和发货通知',
    status: 'running',
    category: 'automation',
    nodeCount: 8,
    executionCount: 245,
    avgExecutionTime: 1250,
    version: '2.1',
    successRate: '98%',
    collaborators: [
      { id: 1, name: '张三', email: 'zhang@example.com', role: 'editor' },
      { id: 2, name: '李四', email: 'li@example.com', role: 'viewer' }
    ],
    createdAt: new Date('2024-01-15'),
    updatedAt: new Date('2024-01-20')
  },
  {
    id: 2,
    name: '内容生成工作流',
    description: '基于模板和数据自动生成营销内容和社交媒体文案',
    status: 'draft',
    category: 'content-generation',
    nodeCount: 5,
    executionCount: 12,
    avgExecutionTime: 2100,
    version: '1.3',
    successRate: '95%',
    collaborators: [
      { id: 3, name: '王五', email: 'wang@example.com', role: 'admin' }
    ],
    createdAt: new Date('2024-01-18'),
    updatedAt: new Date('2024-01-19')
  },
  {
    id: 3,
    name: '数据清洗流程',
    description: '清洗和标准化客户数据，去除重复项和无效信息',
    status: 'stopped',
    category: 'data-processing',
    nodeCount: 12,
    executionCount: 89,
    avgExecutionTime: 3500,
    version: '1.0',
    successRate: '92%',
    collaborators: [],
    createdAt: new Date('2024-01-10'),
    updatedAt: new Date('2024-01-22')
  }
])

// 计算属性
const filteredWorkflows = computed(() => {
  return workflows.filter(workflow => {
    const matchesSearch = !searchQuery.value ||
      workflow.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      workflow.description.toLowerCase().includes(searchQuery.value.toLowerCase())

    const matchesStatus = !statusFilter.value || workflow.status === statusFilter.value
    const matchesCategory = !categoryFilter.value || workflow.category === categoryFilter.value

    return matchesSearch && matchesStatus && matchesCategory
  })
})

const filteredTemplates = computed(() => {
  if (selectedTemplateCategory.value === 'all') {
    return workflowTemplates
  }
  return workflowTemplates.filter(template =>
    template.category === selectedTemplateCategory.value
  )
})

// 方法
const openWorkflow = (workflow) => {
  router.push(`/coze-plugins/workflows/${workflow.id}`)
}

const editWorkflow = (workflow) => {
  router.push(`/coze-plugins/workflows/${workflow.id}/edit`)
}

const runWorkflow = async (workflow) => {
  selectedWorkflow.value = workflow
  
  // 模拟工作流执行
  const startTime = Date.now()
  
  try {
    // 模拟执行过程
    runResult.logs = [
      { id: 1, time: '00:00:01', level: 'info', message: '开始执行工作流' },
      { id: 2, time: '00:00:02', level: 'info', message: '初始化节点...' },
      { id: 3, time: '00:00:03', level: 'info', message: '执行数据处理节点' },
      { id: 4, time: '00:00:05', level: 'success', message: '工作流执行完成' }
    ]
    
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    runResult.success = true
    runResult.executionTime = Date.now() - startTime
    runResult.output = JSON.stringify({
      result: 'success',
      processedItems: 156,
      outputData: {
        summary: '工作流执行成功',
        details: '处理了156个数据项'
      }
    }, null, 2)
    
    workflow.executionCount++
    showRunDialog.value = true
    
  } catch (error) {
    runResult.success = false
    runResult.executionTime = Date.now() - startTime
    runResult.logs.push({
      id: Date.now(),
      time: new Date().toLocaleTimeString(),
      level: 'error',
      message: `执行失败: ${error.message}`
    })
    showRunDialog.value = true
  }
}

const handleWorkflowAction = async (command) => {
  const [action, workflowId] = command.split('-')
  const workflow = workflows.find(w => w.id === parseInt(workflowId))

  if (!workflow) return

  switch (action) {
    case 'edit':
      router.push(`/coze-studio/workflows/edit/${workflowId}`)
      break
    case 'run':
      await runWorkflow(workflow)
      break
    case 'duplicate':
      duplicateWorkflow(workflow)
      break
    case 'version':
      selectedWorkflow.value = workflow
      showVersionDialog.value = true
      break
    case 'share':
      selectedWorkflow.value = workflow
      showShareDialog.value = true
      break
    case 'export':
      exportWorkflow(workflow)
      break
    case 'delete':
      await deleteWorkflow(workflow)
      break
  }
}

const duplicateWorkflow = (workflow) => {
  const newWorkflow = {
    ...workflow,
    id: Date.now(),
    name: `${workflow.name} (副本)`,
    status: 'draft',
    executionCount: 0,
    createdAt: new Date(),
    updatedAt: new Date()
  }
  workflows.push(newWorkflow)
  ElMessage.success('工作流复制成功')
}

const exportWorkflow = (workflow) => {
  // 模拟导出功能
  const data = JSON.stringify(workflow, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${workflow.name}.json`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('工作流导出成功')
}

const deleteWorkflow = async (workflow) => {
  try {
    await ElMessageBox.confirm('确定要删除这个工作流吗？此操作不可恢复。', '确认删除', {
      type: 'warning'
    })
    
    const index = workflows.findIndex(w => w.id === workflow.id)
    if (index > -1) {
      workflows.splice(index, 1)
      ElMessage.success('工作流已删除')
    }
  } catch {
    // 用户取消
  }
}

const handleCreateWorkflow = () => {
  if (!createForm.name.trim()) {
    ElMessage.warning('请输入工作流名称')
    return
  }
  
  const newWorkflow = {
    id: Date.now(),
    name: createForm.name,
    description: createForm.description,
    category: createForm.category,
    status: 'draft',
    nodeCount: 0,
    executionCount: 0,
    avgExecutionTime: 0,
    createdAt: new Date(),
    updatedAt: new Date()
  }
  
  workflows.push(newWorkflow)
  showCreateDialog.value = false
  
  // 重置表单
  Object.assign(createForm, {
    name: '',
    description: '',
    category: ''
  })
  
  ElMessage.success('工作流创建成功')
  
  // 跳转到编辑页面
  router.push(`/coze-plugins/workflows/${newWorkflow.id}/edit`)
}

const getStatusType = (status) => {
  const typeMap = {
    running: 'success',
    stopped: 'danger',
    draft: 'warning'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    running: '运行中',
    stopped: '已停止',
    draft: '草稿'
  }
  return textMap[status] || status
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString()
}

// 模板相关方法
const createFromTemplate = (template) => {
  selectedTemplate.value = template
  showTemplateDialog.value = true
}

const createFromSelectedTemplate = () => {
  if (!selectedTemplate.value) return

  const newWorkflow = {
    id: Date.now(),
    name: `基于${selectedTemplate.value.name}的工作流`,
    description: selectedTemplate.value.description,
    status: 'draft',
    category: selectedTemplate.value.category,
    nodeCount: selectedTemplate.value.nodeCount,
    executionCount: 0,
    avgExecutionTime: 0,
    version: '1.0',
    successRate: '0%',
    collaborators: [],
    createdAt: new Date(),
    updatedAt: new Date()
  }

  workflows.push(newWorkflow)
  showTemplateDialog.value = false
  selectedTemplate.value = null

  ElMessage.success('工作流创建成功')
  router.push(`/coze-plugins/workflows/${newWorkflow.id}/edit`)
}

// 节点颜色生成
const getNodeColor = (index) => {
  const colors = ['#67C23A', '#409EFF', '#E6A23C', '#F56C6C']
  return colors[index % colors.length]
}

// 版本管理方法
const createNewVersion = () => {
  ElMessage.info('创建新版本功能开发中...')
}

const switchToVersion = (version) => {
  ElMessage.success(`已切换到版本 v${version.version}`)
}

const compareVersion = (version) => {
  ElMessage.info('版本对比功能开发中...')
}

const deleteVersion = async (version) => {
  try {
    await ElMessageBox.confirm('确定要删除这个版本吗？', '确认删除', {
      type: 'warning'
    })

    const index = workflowVersions.findIndex(v => v.id === version.id)
    if (index > -1) {
      workflowVersions.splice(index, 1)
      ElMessage.success('版本已删除')
    }
  } catch {
    // 用户取消
  }
}

// 协作分享方法
const addCollaborator = () => {
  if (!newCollaboratorEmail.value.trim()) {
    ElMessage.warning('请输入邮箱地址')
    return
  }

  const newCollaborator = {
    id: Date.now(),
    name: newCollaboratorEmail.value.split('@')[0],
    email: newCollaboratorEmail.value,
    avatar: '',
    role: 'viewer'
  }

  currentCollaborators.push(newCollaborator)
  newCollaboratorEmail.value = ''
  ElMessage.success('协作者邀请已发送')
}

const removeCollaborator = (collaborator) => {
  const index = currentCollaborators.findIndex(c => c.id === collaborator.id)
  if (index > -1) {
    currentCollaborators.splice(index, 1)
    ElMessage.success('协作者已移除')
  }
}

const copyShareLink = () => {
  navigator.clipboard.writeText(shareLink.value)
  ElMessage.success('分享链接已复制')
}



// 更新创建命令处理
const handleCreateCommand = (command) => {
  switch (command) {
    case 'blank':
      showCreateDialog.value = true
      break
    case 'template':
      showTemplateDialog.value = true
      break
    case 'import':
      ElMessage.info('导入功能开发中...')
      break
    case 'ai-generate':
      ElMessage.info('AI 生成功能开发中...')
      break
  }
}

onMounted(() => {
  // 页面初始化
})
</script>

<style lang="scss" scoped>
@import "@/styles/variables.scss";
@import "@/styles/responsive.scss";
@import "@/styles/modern-theme.scss";

.workflows-page {
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

  .templates-section {
    margin-bottom: 24px;
    padding: 20px;
    background: var(--el-bg-color);
    border-radius: 8px;
    border: 1px solid var(--el-border-color-light);

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      h2 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: var(--el-text-color-primary);
      }
    }

    .templates-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 16px;

      .template-card {
        background: var(--el-bg-color-page);
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 8px;
        padding: 16px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: var(--el-color-primary);
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }

        .template-preview {
          margin-bottom: 12px;

          .preview-canvas {
            background: #f8f9fa;
            border-radius: 4px;
            padding: 8px;
          }
        }

        .template-info {
          h4 {
            margin: 0 0 8px 0;
            font-size: 14px;
            font-weight: 600;
            color: var(--el-text-color-primary);
          }

          p {
            margin: 0 0 12px 0;
            font-size: 12px;
            color: var(--el-text-color-regular);
            line-height: 1.4;
          }

          .template-tags {
            display: flex;
            gap: 4px;
            flex-wrap: wrap;
          }
        }
      }
    }
  }

  .workflows-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
    gap: 20px;
    margin-bottom: 24px;

    .workflow-card {
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

      .workflow-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 16px;

        .workflow-preview {
          flex: 1;
          margin-right: 12px;

          .preview-canvas {
            background: #f8f9fa;
            border-radius: 6px;
            padding: 8px;
            border: 1px solid var(--el-border-color-lighter);
          }
        }
      }

      .workflow-content {
        margin-bottom: 16px;

        .workflow-title {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          h3 {
            margin: 0;
            font-size: 16px;
            font-weight: 600;
            color: var(--el-text-color-primary);
          }

          .version-info {
            margin-left: 8px;
          }
        }

        p {
          margin: 0 0 16px 0;
          font-size: 14px;
          color: var(--el-text-color-regular);
          line-height: 1.5;
        }

        .workflow-stats {
          display: flex;
          gap: 20px;
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
              font-size: 16px;
              font-weight: 600;
              color: var(--el-text-color-primary);
            }
          }
        }

        .workflow-meta {
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

      .workflow-actions {
        display: flex;
        gap: 8px;
      }
    }
  }

  .run-result {
    .result-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      padding-bottom: 16px;
      border-bottom: 1px solid var(--el-border-color-light);

      .result-time {
        font-size: 14px;
        color: var(--el-text-color-regular);
      }
    }

    .logs-container {
      max-height: 300px;
      overflow-y: auto;

      .log-item {
        display: flex;
        gap: 12px;
        padding: 8px 0;
        border-bottom: 1px solid var(--el-border-color-lighter);

        .log-time {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          min-width: 60px;
        }

        .log-message {
          font-size: 14px;
        }

        &.info .log-message {
          color: var(--el-text-color-primary);
        }

        &.success .log-message {
          color: var(--el-color-success);
        }

        &.error .log-message {
          color: var(--el-color-danger);
        }
      }
    }

    .output-content {
      background: var(--el-fill-color-light);
      padding: 16px;
      border-radius: 8px;
      font-size: 14px;
      line-height: 1.5;
      max-height: 300px;
      overflow-y: auto;
    }
  }

  // 模板选择对话框样式
  .template-selector {
    .template-categories {
      margin-bottom: 20px;
    }

    .template-list {
      max-height: 400px;
      overflow-y: auto;

      .template-item {
        display: flex;
        align-items: center;
        padding: 12px;
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 6px;
        margin-bottom: 8px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          border-color: var(--el-color-primary-light-7);
          background: var(--el-color-primary-light-9);
        }

        &.active {
          border-color: var(--el-color-primary);
          background: var(--el-color-primary-light-9);
        }

        .template-preview-small {
          margin-right: 12px;
          background: #f8f9fa;
          border-radius: 4px;
          padding: 4px;
        }

        .template-details {
          flex: 1;

          h4 {
            margin: 0 0 4px 0;
            font-size: 14px;
            font-weight: 600;
            color: var(--el-text-color-primary);
          }

          p {
            margin: 0 0 8px 0;
            font-size: 12px;
            color: var(--el-text-color-regular);
            line-height: 1.4;
          }

          .template-meta {
            display: flex;
            gap: 8px;
          }
        }
      }
    }
  }

  // 版本管理对话框样式
  .version-manager {
    .version-header {
      margin-bottom: 16px;
      text-align: right;
    }
  }

  // 协作分享对话框样式
  .share-manager {
    .collaborate-section {
      .add-collaborator {
        margin-bottom: 20px;
      }

      .collaborator-list {
        .collaborator-item {
          display: flex;
          align-items: center;
          padding: 12px;
          border: 1px solid var(--el-border-color-lighter);
          border-radius: 6px;
          margin-bottom: 8px;

          .collaborator-info {
            flex: 1;
            margin-left: 12px;

            .name {
              font-weight: 600;
              color: var(--el-text-color-primary);
              margin-bottom: 2px;
            }

            .email {
              font-size: 12px;
              color: var(--el-text-color-regular);
            }
          }

          .el-select {
            width: 100px;
            margin-right: 8px;
          }
        }
      }
    }

    .share-section {
      .el-form-item {
        margin-bottom: 20px;
      }
    }
  }
}
</style>
