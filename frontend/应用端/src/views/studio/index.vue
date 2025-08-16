<template>
  <div class="studio-container">
    <!-- 顶部导航栏 -->
    <div class="studio-header">
      <div class="header-left">
        <div class="logo-section">
          <div class="studio-logo">
            <el-icon><Tools /></el-icon>
            <span class="logo-text">QMS Studio</span>
          </div>
          <el-divider direction="vertical" />
          <div class="workspace-selector">
            <el-select v-model="currentWorkspace" placeholder="选择工作空间" size="small">
              <el-option
                v-for="workspace in workspaces"
                :key="workspace.id"
                :label="workspace.name"
                :value="workspace.id"
              />
            </el-select>
          </div>
        </div>
      </div>
      
      <div class="header-center">
        <el-tabs v-model="activeTab" class="studio-tabs">
          <el-tab-pane label="Agent" name="agent">
            <template #label>
              <span class="tab-label">
                <el-icon><User /></el-icon>
                Agent搭建
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="Workflow" name="workflow">
            <template #label>
              <span class="tab-label">
                <el-icon><Connection /></el-icon>
                工作流设计
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="Knowledge" name="knowledge">
            <template #label>
              <span class="tab-label">
                <el-icon><Document /></el-icon>
                知识库管理
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane label="Plugin" name="plugin">
            <template #label>
              <span class="tab-label">
                <el-icon><Tools /></el-icon>
                插件开发
              </span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>
      
      <div class="header-right">
        <el-button :icon="Plus" type="primary" @click="showCreateDialog = true">
          创建
        </el-button>
        <el-button :icon="View" @click="previewMode">
          预览
        </el-button>
        <el-button :icon="Upload" @click="publishProject">
          发布
        </el-button>
        <el-button :icon="Setting" circle @click="showSettings = true" />
        <el-avatar :size="32" src="/avatar.jpg" />
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="studio-main">
      <!-- 侧边栏 -->
      <div class="studio-sidebar">
        <div class="sidebar-section">
          <div class="section-header">
            <h4>我的项目</h4>
            <el-button :icon="Plus" size="small" text @click="createNewProject">
              新建
            </el-button>
          </div>
          <div class="project-list">
            <div
              v-for="project in projects"
              :key="project.id"
              :class="['project-item', { active: selectedProject?.id === project.id }]"
              @click="selectProject(project)"
            >
              <div class="project-icon">
                <el-icon>
                  <component :is="getProjectIcon(project.type)" />
                </el-icon>
              </div>
              <div class="project-info">
                <div class="project-name">{{ project.name }}</div>
                <div class="project-meta">
                  <span class="project-type">{{ getProjectTypeText(project.type) }}</span>
                  <el-tag :type="getStatusType(project.status)" size="small">
                    {{ project.status }}
                  </el-tag>
                </div>
              </div>
              <div class="project-actions">
                <el-dropdown @command="handleProjectAction">
                  <el-button :icon="MoreFilled" size="small" text />
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="{action: 'edit', project}">编辑</el-dropdown-item>
                      <el-dropdown-item :command="{action: 'test', project}">测试</el-dropdown-item>
                      <el-dropdown-item :command="{action: 'publish', project}">发布</el-dropdown-item>
                      <el-dropdown-item :command="{action: 'delete', project}" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 最近使用 -->
        <div class="sidebar-section">
          <h4>最近使用</h4>
          <div class="recent-list">
            <div
              v-for="item in recentItems"
              :key="item.id"
              class="recent-item"
              @click="openRecentItem(item)"
            >
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.name }}</span>
              <span class="recent-time">{{ formatTime(item.lastUsed) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 主内容区 -->
      <div class="studio-content">
        <!-- Agent搭建 -->
        <div v-if="activeTab === 'agent'" class="content-panel">
          <AgentBuilder 
            :project="selectedProject"
            @save="handleSave"
            @test="handleTest"
            @publish="handlePublish"
          />
        </div>
        
        <!-- 工作流设计 -->
        <div v-if="activeTab === 'workflow'" class="content-panel">
          <CozeWorkflowBuilder
            :project="selectedProject"
            @save="handleSave"
            @execute="handleTest"
          />
        </div>
        
        <!-- 知识库管理 -->
        <div v-if="activeTab === 'knowledge'" class="content-panel">
          <CozeKnowledgeManager
            :project="selectedProject"
            @save="handleSave"
          />
        </div>

        <!-- 插件开发 -->
        <div v-if="activeTab === 'plugin'" class="content-panel">
          <CozePluginManager
            :project="selectedProject"
            @save="handleSave"
            @test="handleTest"
            @publish="handlePublish"
          />
        </div>
      </div>
    </div>
    
    <!-- 创建项目对话框 -->
    <CozeProjectCreator
      v-model="showCreateDialog"
      @created="handleProjectCreated"
    />
    
    <!-- 测试面板 -->
    <TestPanel 
      v-model="showTestPanel"
      :project="testingProject"
      @close="showTestPanel = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Tools, User, Connection, Document, Plus, View, Upload, Setting, 
  MoreFilled 
} from '@element-plus/icons-vue'
import AgentBuilder from './components/AgentBuilder.vue'
import CozeWorkflowBuilder from '../coze-studio/components/CozeWorkflowBuilder.vue'
import CozeKnowledgeManager from '../coze-studio/components/CozeKnowledgeManager.vue'
import CozePluginManager from '../coze-studio/components/CozePluginManager.vue'
import CozeProjectCreator from '../coze-studio/components/CozeProjectCreator.vue'
import TestPanel from './components/TestPanel.vue'

// 响应式数据
const activeTab = ref('agent')
const currentWorkspace = ref('default')
const selectedProject = ref(null)
const showCreateDialog = ref(false)
const showTestPanel = ref(false)
const testingProject = ref(null)
const showSettings = ref(false)

// 工作空间列表
const workspaces = ref([
  { id: 'default', name: '默认工作空间' },
  { id: 'qms', name: 'QMS质量管理' },
  { id: 'test', name: '测试环境' }
])

// 项目列表
const projects = ref([
  {
    id: '1',
    name: 'QMS质量管理助手',
    type: 'agent',
    status: 'published',
    lastModified: new Date(),
    description: '基于ISO标准的质量管理AI助手'
  },
  {
    id: '2',
    name: '质量检查工作流',
    type: 'workflow',
    status: 'draft',
    lastModified: new Date(),
    description: '自动化质量检查流程'
  }
])

// 最近使用项目
const recentItems = ref([
  {
    id: '1',
    name: 'QMS质量管理助手',
    icon: 'User',
    lastUsed: new Date()
  }
])

// 方法
const selectProject = (project) => {
  selectedProject.value = project
}

const createNewProject = () => {
  showCreateDialog.value = true
}

const handleProjectCreated = (project) => {
  projects.value.unshift(project)
  selectedProject.value = project
  ElMessage.success('项目创建成功')
}

const handleProjectAction = ({ action, project }) => {
  switch (action) {
    case 'edit':
      selectedProject.value = project
      break
    case 'test':
      testingProject.value = project
      showTestPanel.value = true
      break
    case 'publish':
      handlePublish(project)
      break
    case 'delete':
      handleDeleteProject(project)
      break
  }
}

const handleDeleteProject = async (project) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目 "${project.name}" 吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const index = projects.value.findIndex(p => p.id === project.id)
    if (index > -1) {
      projects.value.splice(index, 1)
      if (selectedProject.value?.id === project.id) {
        selectedProject.value = null
      }
      ElMessage.success('项目删除成功')
    }
  } catch {
    // 用户取消删除
  }
}

const handleSave = (data) => {
  // 保存项目数据
  ElMessage.success('保存成功')
}

const handleTest = (project) => {
  testingProject.value = project
  showTestPanel.value = true
}

const handlePublish = async (project) => {
  try {
    // 发布项目逻辑
    ElMessage.success('发布成功')
  } catch (error) {
    ElMessage.error('发布失败：' + error.message)
  }
}

const previewMode = () => {
  // 跳转到预览页面
  window.open('/app/preview', '_blank')
}

const publishProject = () => {
  if (!selectedProject.value) {
    ElMessage.warning('请先选择一个项目')
    return
  }
  handlePublish(selectedProject.value)
}

const getProjectIcon = (type) => {
  const icons = {
    agent: 'User',
    workflow: 'Connection',
    knowledge: 'Document',
    plugin: 'Tools'
  }
  return icons[type] || 'Document'
}

const getProjectTypeText = (type) => {
  const types = {
    agent: 'AI助手',
    workflow: '工作流',
    knowledge: '知识库',
    plugin: '插件'
  }
  return types[type] || '未知'
}

const getStatusType = (status) => {
  const types = {
    draft: 'info',
    testing: 'warning',
    published: 'success',
    archived: 'danger'
  }
  return types[status] || 'info'
}

const formatTime = (time) => {
  const now = new Date()
  const diff = now - time
  const minutes = Math.floor(diff / 60000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (minutes < 1440) return `${Math.floor(minutes / 60)}小时前`
  return `${Math.floor(minutes / 1440)}天前`
}

const openRecentItem = (item) => {
  const project = projects.value.find(p => p.id === item.id)
  if (project) {
    selectedProject.value = project
  }
}

onMounted(() => {
  // 初始化数据
  if (projects.value.length > 0) {
    selectedProject.value = projects.value[0]
  }
})
</script>

<style scoped>
.studio-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.studio-header {
  height: 60px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.studio-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.workspace-selector {
  min-width: 150px;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.studio-tabs {
  border-bottom: none;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.studio-main {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.studio-sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e4e7ed;
  padding: 20px;
  overflow-y: auto;
}

.sidebar-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.project-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.project-item {
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 12px;
}

.project-item:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.project-item.active {
  border-color: #409eff;
  background: #e6f7ff;
}

.project-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.project-info {
  flex: 1;
  min-width: 0;
}

.project-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.project-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.project-actions {
  flex-shrink: 0;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-item {
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.recent-item:hover {
  background: #f5f7fa;
}

.recent-time {
  margin-left: auto;
  color: #c0c4cc;
  font-size: 12px;
}

.studio-content {
  flex: 1;
  background: white;
  overflow: hidden;
}

.content-panel {
  height: 100%;
  padding: 20px;
}
</style>
