<template>
  <div class="personal-space">
    <!-- 顶部导航 -->
    <div class="space-header">
      <div class="header-left">
        <el-select v-model="currentSpace" placeholder="个人空间" size="small">
          <el-option label="个人空间" value="personal" />
          <el-option label="团队空间" value="team" />
        </el-select>
      </div>
      <div class="header-right">
        <el-button :icon="Plus" type="primary" @click="showCreateDialog = true">
          添加
        </el-button>
        <el-button :icon="Setting" circle />
      </div>
    </div>

    <!-- 侧边栏 -->
    <div class="space-content">
      <div class="space-sidebar">
        <div class="sidebar-menu">
          <div
            v-for="menu in sidebarMenus"
            :key="menu.key"
            :class="['menu-item', { active: activeMenu === menu.key }]"
            @click="activeMenu = menu.key"
          >
            <el-icon><component :is="menu.icon" /></el-icon>
            <span>{{ menu.label }}</span>
          </div>
        </div>

        <!-- 收藏区域 -->
        <div class="favorites-section">
          <h4>收藏</h4>
          <div class="favorites-list">
            <div class="favorite-item" v-for="item in favorites" :key="item.id">
              <el-icon><Star /></el-icon>
              <span>{{ item.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="space-main">
        <!-- 资源库页面 -->
        <div v-if="activeMenu === 'resources'" class="resources-view">
          <div class="view-header">
            <h2>资源库</h2>
            <div class="view-controls">
              <el-select v-model="resourceType" placeholder="所有类型" size="small">
                <el-option label="所有类型" value="all" />
                <el-option label="Agent" value="agent" />
                <el-option label="工作流" value="workflow" />
                <el-option label="插件" value="plugin" />
                <el-option label="知识库" value="knowledge" />
              </el-select>
              <el-select v-model="resourceStatus" placeholder="全部" size="small">
                <el-option label="全部" value="all" />
                <el-option label="已发布" value="published" />
                <el-option label="草稿" value="draft" />
              </el-select>
              <el-input
                v-model="searchKeyword"
                placeholder="搜索资源"
                :prefix-icon="Search"
                size="small"
                style="width: 200px;"
              />
            </div>
          </div>

          <div class="resources-grid">
            <div
              v-for="resource in filteredResources"
              :key="resource.id"
              class="resource-card"
              @click="openResource(resource)"
            >
              <div class="resource-icon">
                <el-icon><component :is="getResourceIcon(resource.type)" /></el-icon>
              </div>
              <div class="resource-info">
                <h4>{{ resource.name }}</h4>
                <p>{{ resource.description }}</p>
                <div class="resource-meta">
                  <el-tag :type="getStatusType(resource.status)" size="small">
                    {{ resource.status }}
                  </el-tag>
                  <span class="update-time">{{ formatTime(resource.updatedAt) }}</span>
                </div>
              </div>
              <div class="resource-actions">
                <el-button size="small" text @click.stop="editResource(resource)">
                  编辑
                </el-button>
                <el-button size="small" text @click.stop="shareResource(resource)">
                  分享
                </el-button>
                <el-dropdown @command="handleResourceAction">
                  <el-button size="small" text>
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="`duplicate-${resource.id}`">
                        复制
                      </el-dropdown-item>
                      <el-dropdown-item :command="`favorite-${resource.id}`">
                        收藏
                      </el-dropdown-item>
                      <el-dropdown-item :command="`delete-${resource.id}`" divided>
                        删除
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="filteredResources.length === 0" class="empty-state">
            <el-empty description="未能找到相关结果" />
          </div>
        </div>

        <!-- 项目开发页面 -->
        <div v-if="activeMenu === 'projects'" class="projects-view">
          <div class="view-header">
            <h2>项目开发</h2>
            <el-button :icon="Plus" type="primary" @click="createProject">
              新建项目
            </el-button>
          </div>
          <div class="projects-grid">
            <div
              v-for="project in projects"
              :key="project.id"
              class="project-card"
            >
              <div class="project-header">
                <h4>{{ project.name }}</h4>
                <el-tag :type="getStatusType(project.status)" size="small">
                  {{ project.status }}
                </el-tag>
              </div>
              <p>{{ project.description }}</p>
              <div class="project-progress">
                <span>进度</span>
                <el-progress :percentage="project.progress" :show-text="false" />
                <span>{{ project.progress }}%</span>
              </div>
              <div class="project-actions">
                <el-button size="small" @click="openProject(project)">
                  打开
                </el-button>
                <el-button size="small" text @click="editProject(project)">
                  编辑
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 发布管理页面 -->
        <div v-if="activeMenu === 'publish'" class="publish-view">
          <div class="view-header">
            <h2>发布管理</h2>
          </div>
          <el-table :data="publishedItems" style="width: 100%">
            <el-table-column prop="name" label="名称" />
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="publishedAt" label="发布时间" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" text>查看</el-button>
                <el-button size="small" text>更新</el-button>
                <el-button size="small" text type="danger">下线</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 效果评测页面 -->
        <div v-if="activeMenu === 'evaluation'" class="evaluation-view">
          <div class="view-header">
            <h2>效果评测</h2>
          </div>
          <div class="evaluation-cards">
            <div class="eval-card">
              <h4>Agent性能</h4>
              <div class="eval-metrics">
                <div class="metric">
                  <span>响应时间</span>
                  <span class="metric-value">1.2s</span>
                </div>
                <div class="metric">
                  <span>准确率</span>
                  <span class="metric-value">95.6%</span>
                </div>
                <div class="metric">
                  <span>用户满意度</span>
                  <span class="metric-value">4.8/5</span>
                </div>
              </div>
            </div>
            <div class="eval-card">
              <h4>使用统计</h4>
              <div class="eval-chart">
                <!-- 这里可以集成图表组件 -->
                <div class="chart-placeholder">图表区域</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建资源对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建资源" width="40%">
      <div class="create-options">
        <div
          v-for="option in createOptions"
          :key="option.type"
          class="create-option"
          @click="createResource(option.type)"
        >
          <div class="option-icon">
            <el-icon><component :is="option.icon" /></el-icon>
          </div>
          <div class="option-info">
            <h4>{{ option.name }}</h4>
            <p>{{ option.description }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus, Setting, Star, Search, MoreFilled,
  User, Connection, Tools, Document, FolderOpened,
  DataAnalysis, Share
} from '@element-plus/icons-vue'

// 响应式数据
const currentSpace = ref('personal')
const activeMenu = ref('resources')
const resourceType = ref('all')
const resourceStatus = ref('all')
const searchKeyword = ref('')
const showCreateDialog = ref(false)

// 侧边栏菜单
const sidebarMenus = ref([
  { key: 'projects', label: '项目开发', icon: 'FolderOpened' },
  { key: 'resources', label: '资源库', icon: 'Document' },
  { key: 'publish', label: '发布管理', icon: 'Share' },
  { key: 'evaluation', label: '效果评测', icon: 'DataAnalysis' }
])

// 收藏列表
const favorites = ref([
  { id: 1, name: '智能客服Agent' },
  { id: 2, name: '数据分析工作流' },
  { id: 3, name: '文档处理插件' }
])

// 资源列表
const resources = ref([
  {
    id: 1,
    name: '智能客服Agent',
    type: 'agent',
    description: '专业的客服AI助手，能够处理常见问题',
    status: 'published',
    updatedAt: new Date('2024-01-15')
  },
  {
    id: 2,
    name: '数据分析工作流',
    type: 'workflow',
    description: '自动化数据处理和分析流程',
    status: 'draft',
    updatedAt: new Date('2024-01-14')
  },
  {
    id: 3,
    name: '文档处理插件',
    type: 'plugin',
    description: '处理各种格式的文档文件',
    status: 'published',
    updatedAt: new Date('2024-01-13')
  }
])

// 项目列表
const projects = ref([
  {
    id: 1,
    name: '智能客服系统',
    description: '基于AI的客服解决方案',
    status: 'in-progress',
    progress: 75
  },
  {
    id: 2,
    name: '数据分析平台',
    description: '企业数据分析和可视化平台',
    status: 'planning',
    progress: 25
  }
])

// 发布项目列表
const publishedItems = ref([
  {
    id: 1,
    name: '智能客服Agent',
    type: 'Agent',
    status: 'online',
    publishedAt: '2024-01-15 10:30'
  }
])

// 创建选项
const createOptions = ref([
  {
    type: 'agent',
    name: 'Agent',
    description: '创建智能对话助手',
    icon: 'User'
  },
  {
    type: 'workflow',
    name: '工作流',
    description: '创建自动化流程',
    icon: 'Connection'
  },
  {
    type: 'plugin',
    name: '插件',
    description: '创建功能插件',
    icon: 'Tools'
  },
  {
    type: 'knowledge',
    name: '知识库',
    description: '创建知识库',
    icon: 'Document'
  }
])

// 计算属性
const filteredResources = computed(() => {
  return resources.value.filter(resource => {
    const typeMatch = resourceType.value === 'all' || resource.type === resourceType.value
    const statusMatch = resourceStatus.value === 'all' || resource.status === resourceStatus.value
    const keywordMatch = !searchKeyword.value || 
      resource.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
      resource.description.toLowerCase().includes(searchKeyword.value.toLowerCase())
    
    return typeMatch && statusMatch && keywordMatch
  })
})

// 方法
const getResourceIcon = (type) => {
  const iconMap = {
    agent: 'User',
    workflow: 'Connection',
    plugin: 'Tools',
    knowledge: 'Document'
  }
  return iconMap[type] || 'Document'
}

const getStatusType = (status) => {
  const typeMap = {
    published: 'success',
    draft: 'warning',
    'in-progress': 'primary',
    planning: 'info',
    online: 'success'
  }
  return typeMap[status] || 'info'
}

const formatTime = (date) => {
  return date.toLocaleDateString('zh-CN')
}

const openResource = (resource) => {
  ElMessage.info(`打开${resource.name}`)
}

const editResource = (resource) => {
  ElMessage.info(`编辑${resource.name}`)
}

const shareResource = (resource) => {
  ElMessage.info(`分享${resource.name}`)
}

const handleResourceAction = (command) => {
  const [action, id] = command.split('-')
  ElMessage.info(`执行${action}操作`)
}

const createResource = (type) => {
  showCreateDialog.value = false
  ElMessage.success(`开始创建${type}`)
}

const createProject = () => {
  ElMessage.info('创建新项目')
}

const openProject = (project) => {
  ElMessage.info(`打开项目：${project.name}`)
}

const editProject = (project) => {
  ElMessage.info(`编辑项目：${project.name}`)
}
</script>

<style scoped>
.personal-space {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.space-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.space-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.space-sidebar {
  width: 240px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.sidebar-menu {
  padding: 16px 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.3s;
  color: #606266;
}

.menu-item:hover {
  background: #f5f7fa;
  color: #409eff;
}

.menu-item.active {
  background: #e6f7ff;
  color: #409eff;
  border-right: 2px solid #409eff;
}

.favorites-section {
  margin-top: auto;
  padding: 16px 24px;
  border-top: 1px solid #e4e7ed;
}

.favorites-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #909399;
}

.favorite-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
}

.favorite-item:hover {
  color: #409eff;
}

.space-main {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.view-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.view-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.view-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.resources-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.resource-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s;
}

.resource-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.resource-icon {
  width: 48px;
  height: 48px;
  background: #f0f9ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  color: #409eff;
  font-size: 24px;
}

.resource-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #303133;
}

.resource-info p {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.resource-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.update-time {
  font-size: 12px;
  color: #909399;
}

.resource-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.project-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e4e7ed;
}

.project-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.project-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.project-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 16px 0;
  font-size: 14px;
  color: #606266;
}

.project-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}

.evaluation-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.eval-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #e4e7ed;
}

.eval-card h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #303133;
}

.eval-metrics {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.metric {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-value {
  font-weight: 600;
  color: #409eff;
}

.chart-placeholder {
  height: 200px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.create-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.create-option {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.create-option:hover {
  border-color: #409eff;
  background: #f0f9ff;
}

.option-icon {
  width: 48px;
  height: 48px;
  background: #f0f9ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #409eff;
  font-size: 24px;
}

.option-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #303133;
}

.option-info p {
  margin: 0;
  font-size: 14px;
  color: #606266;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}
</style>
