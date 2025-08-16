<template>
  <div class="coze-studio-container">
    <!-- 顶部导航栏 -->
    <div class="coze-header">
      <div class="header-left">
        <div class="logo-section">
          <div class="coze-logo" @click="navigateTo('/coze-studio')">
            <el-icon class="logo-icon"><Grid /></el-icon>
            <span class="logo-text">Coze Studio</span>
          </div>

          <!-- 面包屑导航 -->
          <div class="breadcrumb-section">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>
                <el-icon><House /></el-icon>
                {{ currentWorkspace.name }}
              </el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentPageTitle">
                {{ currentPageTitle }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
        </div>
      </div>

      <div class="header-center">
        <!-- 搜索框 -->
        <div class="global-search">
          <el-input
            v-model="searchQuery"
            placeholder="搜索 Agent、工作流、知识库..."
            :prefix-icon="Search"
            size="default"
            style="width: 300px;"
            @keyup.enter="handleGlobalSearch"
          />
        </div>
      </div>

      <div class="header-right">
        <!-- 创建下拉菜单 -->
        <el-dropdown @command="handleCreateCommand" trigger="click">
          <el-button type="primary" :icon="Plus">
            创建
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="agent">
                <el-icon><User /></el-icon>
                创建 Agent
              </el-dropdown-item>
              <el-dropdown-item command="workflow">
                <el-icon><Connection /></el-icon>
                创建工作流
              </el-dropdown-item>
              <el-dropdown-item command="knowledge">
                <el-icon><Document /></el-icon>
                创建知识库
              </el-dropdown-item>
              <el-dropdown-item command="plugin">
                <el-icon><Tools /></el-icon>
                创建插件
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 通知 -->
        <el-badge :value="notificationCount" :hidden="notificationCount === 0">
          <el-button :icon="Bell" circle @click="showNotifications = true" />
        </el-badge>

        <!-- 帮助 -->
        <el-button :icon="QuestionFilled" circle @click="showHelp = true" />

        <!-- 用户菜单 -->
        <el-dropdown @command="handleUserCommand" trigger="click">
          <div class="user-avatar">
            <el-avatar :size="32" :src="userInfo.avatar">
              {{ userInfo.name?.charAt(0) }}
            </el-avatar>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人资料
              </el-dropdown-item>
              <el-dropdown-item command="workspace">
                <el-icon><OfficeBuilding /></el-icon>
                工作空间设置
              </el-dropdown-item>
              <el-dropdown-item command="settings">
                <el-icon><Setting /></el-icon>
                系统设置
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="coze-main">
      <!-- 侧边栏导航 -->
      <div class="coze-sidebar">
        <!-- 个人空间信息 -->
        <div class="workspace-info">
          <div class="workspace-avatar">
            <el-icon><User /></el-icon>
          </div>
          <div class="workspace-details">
            <div class="workspace-name">{{ currentWorkspace.name }}</div>
            <div class="workspace-type">个人空间</div>
          </div>
        </div>

        <!-- 导航菜单 -->
        <div class="nav-menu">
          <!-- 插件建设 -->
          <div class="nav-section">
            <div class="section-title">插件建设</div>

            <div class="nav-item" @click="setActiveComponent('PluginEcosystemManager')">
              <div class="nav-icon">
                <el-icon><Tools /></el-icon>
              </div>
              <span class="nav-text">插件中心</span>
            </div>

            <div class="nav-item" @click="navigateTo('/coze-studio/validation')">
              <div class="nav-icon">
                <el-icon><Document /></el-icon>
              </div>
              <span class="nav-text">功能验证/巡检</span>
            </div>
            <div class="nav-submenu">
              <div class="nav-subitem" @click="navigateTo('/coze-studio/validation')">多格式解析验证</div>
              <div class="nav-subitem" @click="navigateTo('/coze-studio/validation')">插件批量验证</div>
              <div class="nav-subitem" @click="navigateTo('/coze-studio/validation')">场景质量分析</div>
            </div>

            <div class="nav-item" @click="navigateTo('/coze-studio/monitoring')">
              <div class="nav-icon">
                <el-icon><Monitor /></el-icon>
              </div>
              <span class="nav-text">执行监控</span>
            </div>
            <div class="nav-submenu">
              <div class="nav-subitem" @click="navigateTo('/coze-studio/monitoring?view=plugins')">插件执行</div>
              <div class="nav-subitem" @click="navigateTo('/coze-studio/monitoring?view=scenarios')">场景执行</div>
            </div>
          </div>

          <!-- 业务编排 -->
          <div class="nav-section">
            <div class="section-title">业务编排</div>

            <div class="nav-item" @click="navigateTo('/coze-studio/projects')">
              <div class="nav-icon">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <span class="nav-text">项目管理</span>
            </div>

            <div class="nav-item" @click="navigateTo('/coze-studio/agents')">
              <div class="nav-icon">
                <el-icon><User /></el-icon>
              </div>
              <span class="nav-text">Agent开发</span>
            </div>

            <div class="nav-item" @click="navigateTo('/coze-studio/workflows')">
              <div class="nav-icon">
                <el-icon><Connection /></el-icon>
              </div>
              <span class="nav-text">工作流设计</span>
            </div>
          </div>

          <!-- AI能力 -->
          <div class="nav-section">
            <div class="section-title">AI能力</div>

            <div class="nav-item" @click="navigateTo('/coze-studio/models')">
              <div class="nav-icon">
                <el-icon><Cpu /></el-icon>
              </div>
              <span class="nav-text">AI模型</span>
            </div>

            <div class="nav-item" @click="navigateTo('/coze-studio/knowledge')">
              <div class="nav-icon">
                <el-icon><Document /></el-icon>
              </div>
              <span class="nav-text">知识库</span>
            </div>

            <div class="nav-item" @click="setActiveComponent('MemoryManager')">
              <div class="nav-icon">
                <el-icon><DataBoard /></el-icon>
              </div>
              <span class="nav-text">Memory记忆</span>
            </div>
          </div>

	          <!-- 验证与监控 -->
	          <div class="nav-section">
	            <div class="section-title">验证与监控</div>
	            <div class="nav-item" @click="navigateTo('/coze-studio/validation')">
	              <div class="nav-icon">
	                <el-icon><Document /></el-icon>
	              </div>
	              <span class="nav-text">功能验证/巡检</span>
	            </div>
	            <div class="nav-item" @click="navigateTo('/coze-studio/monitoring')">
	              <div class="nav-icon">
	                <el-icon><Monitor /></el-icon>
	              </div>
	              <span class="nav-text">执行监控</span>
	            </div>
	          </div>

          <!-- 智能协作 -->
          <div class="nav-section">
            <div class="section-title">智能协作</div>

            <div class="nav-item" @click="setActiveComponent('AutoGPTManager')">
              <div class="nav-icon">
                <el-icon><MagicStick /></el-icon>
              </div>
              <span class="nav-text">AutoGPT规划</span>
            </div>

            <div class="nav-item" @click="setActiveComponent('CrewAIManager')">
              <div class="nav-icon">
                <el-icon><UserFilled /></el-icon>
              </div>
              <span class="nav-text">CrewAI协作</span>
            </div>

            <div class="nav-item" @click="navigateTo('/coze-studio/monitoring')">
              <div class="nav-icon">
                <el-icon><Monitor /></el-icon>
              </div>
              <span class="nav-text">执行监控</span>
            </div>
          </div>
        </div>

        <!-- 收藏区域 -->
        <div class="favorites-section">
          <div class="section-title">收藏</div>
          <div class="empty-state">
            <div class="empty-text">还没有收藏任何内容</div>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="coze-content">
        <!-- 动态组件显示 -->
        <component
          v-if="activeComponent"
          :is="activeComponent"
          @back="activeComponent = null"
        />
        <!-- 首页内容 -->
        <div v-else-if="$route.path === '/coze-studio'" class="home-content">
          <!-- 欢迎区域 -->
          <div class="welcome-section">
            <h1 class="welcome-title">欢迎使用 Coze Studio</h1>
            <p class="welcome-subtitle">AI开发平台 - 构建智能Agent、工作流和插件生态</p>
          </div>

          <!-- 统计卡片 -->
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon agent">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.agents }}</div>
                <div class="stat-label">AI Agent</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon workflow">
                <el-icon><Connection /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.workflows }}</div>
                <div class="stat-label">工作流</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon plugin">
                <el-icon><Tools /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.plugins }}</div>
                <div class="stat-label">插件</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon project">
                <el-icon><OfficeBuilding /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-number">{{ stats.projects }}</div>
                <div class="stat-label">项目</div>
              </div>
            </div>
          </div>

          <!-- 快速开始 -->
          <div class="quick-start-section">
            <h2 class="section-title">快速开始</h2>
            <div class="quick-actions">
              <div class="action-card" @click="navigateTo('/coze-studio/agents')">
                <div class="action-icon">
                  <el-icon><User /></el-icon>
                </div>
                <div class="action-content">
                  <h3>创建Agent</h3>
                  <p>构建智能AI助手</p>
                </div>
              </div>
              <div class="action-card" @click="navigateTo('/coze-studio/workflows')">
                <div class="action-icon">
                  <el-icon><Connection /></el-icon>
                </div>
                <div class="action-content">
                  <h3>设计工作流</h3>
                  <p>可视化流程编排</p>
                </div>
              </div>
              <div class="action-card" @click="setActiveComponent('PluginEcosystemManager')">
                <div class="action-icon">
                  <el-icon><Tools /></el-icon>
                </div>
                <div class="action-content">
                  <h3>安装插件</h3>
                  <p>扩展平台能力</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 默认路由视图 -->
        <router-view v-else />
      </div>
    </div>

    <!-- 创建项目对话框 -->
    <el-dialog
      v-model="showCreateDialog"
      title="创建新项目"
      width="600px"
      :before-close="handleCloseCreate"
    >
      <CozeProjectCreator
        @create="handleCreateProject"
        @cancel="showCreateDialog = false"
      />
    </el-dialog>

    <!-- 设置对话框 -->
    <el-dialog
      v-model="showSettings"
      title="Coze Studio 设置"
      width="800px"
    >
      <CozeSettings
        @save="handleSaveSettings"
        @cancel="showSettings = false"
      />
    </el-dialog>

    <!-- 快捷键帮助 -->
    <ShortcutHelp ref="shortcutHelpRef" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Cpu,
  User,
  Plus,
  Setting,
  ArrowDown,
  Monitor,
  Document,
  Connection,
  DataBoard,
  Grid,
  House,
  Search,
  Bell,
  QuestionFilled,
  OfficeBuilding,
  SwitchButton,
  Tools,
  MagicStick,
  UserFilled
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 导入API函数
import {
  getAgents,
  getWorkflows,
  getPlugins,
  getProjects
} from '@/api/coze-studio'

// 导入子组件
import CozeProjectCreator from './components/CozeProjectCreator.vue'
import CozeSettings from './components/CozeSettings.vue'
import ShortcutHelp from '@/components/ShortcutHelp.vue'

// 导入新的AI功能组件
import AutoGPTManager from './components/AutoGPTManager.vue'
import CrewAIManager from './components/CrewAIManager.vue'
import MemoryManager from './components/MemoryManager.vue'
import PluginEcosystemManager from './components/PluginEcosystemManager.vue'

const router = useRouter()

// 响应式数据
const showCreateDialog = ref(false)
const showSettings = ref(false)
const showNotifications = ref(false)
const showHelp = ref(false)
const shortcutHelpRef = ref(null)
const activeComponent = ref(null)
const searchQuery = ref('')
const notificationCount = ref(0)
const currentPageTitle = ref('')

// 用户信息
const userInfo = reactive({
  name: '用户',
  avatar: ''
})

// 当前工作空间
const currentWorkspace = reactive({
  id: 'personal',
  name: '个人空间'
})

// 工作空间列表（预留：如需启用工作空间切换可恢复使用）
// const workspaces = reactive([
//   { id: 'personal', name: '个人空间' },
//   { id: 'team', name: '团队空间' },
//   { id: 'enterprise', name: '企业空间' }
// ])

// 统计数据
const stats = reactive({
  agents: 0,
  workflows: 0,
  plugins: 0,
  projects: 0
})

// 方法
const navigateTo = (path) => {
  activeComponent.value = null // 清除活跃组件
  router.push(path)
}

// 组件映射
const componentMap = {
  'AutoGPTManager': AutoGPTManager,
  'CrewAIManager': CrewAIManager,
  'MemoryManager': MemoryManager,
  'PluginEcosystemManager': PluginEcosystemManager
}

const setActiveComponent = (componentName) => {
  activeComponent.value = componentMap[componentName]
  // 更新页面标题
  const titles = {
    'AutoGPTManager': 'AutoGPT 自主规划',
    'CrewAIManager': 'CrewAI 团队协作',
    'MemoryManager': 'Memory 记忆管理',
    'PluginEcosystemManager': '插件生态系统'
  }
  currentPageTitle.value = titles[componentName] || ''
}

const handleGlobalSearch = () => {
  if (searchQuery.value.trim()) {
    ElMessage.info(`搜索: ${searchQuery.value}`)
    // TODO: 实现全局搜索功能
  }
}

const handleCreateCommand = (command) => {
  switch (command) {
    case 'agent':
      navigateTo('/coze-studio/agents')
      break
    case 'workflow':
      navigateTo('/coze-studio/workflows')
      break
    case 'knowledge':
      navigateTo('/coze-studio/knowledge')
      break
    case 'plugin':
      setActiveComponent('PluginEcosystemManager')
      break
    default:
      ElMessage.info(`创建 ${command}`)
  }
}

const handleUserCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料')
      break
    case 'workspace':
      ElMessage.info('工作空间设置')
      break
    case 'settings':
      showSettings.value = true
      break
    case 'logout':
      ElMessage.info('退出登录')
      break
    default:
      ElMessage.info(command)
  }
}



const handleCloseCreate = () => {
  showCreateDialog.value = false
}

const handleCreateProject = () => {
  showCreateDialog.value = false
  ElMessage.success('项目创建成功')
}

const handleSaveSettings = () => {
  showSettings.value = false
  ElMessage.success('设置保存成功')
}

// 获取统计数据
const loadStats = async () => {
  try {
    // 获取Agent数量
    const agentsResponse = await getAgents({ limit: 1 })
    stats.agents = agentsResponse.data?.total || 0

    // 获取工作流数量
    const workflowsResponse = await getWorkflows({ limit: 1 })
    stats.workflows = workflowsResponse.data?.total || 0

    // 获取插件数量
    const pluginsResponse = await getPlugins({ limit: 1 })
    stats.plugins = pluginsResponse.data?.total || 0

    // 获取项目数量
    const projectsResponse = await getProjects({ limit: 1 })
    stats.projects = projectsResponse.data?.total || 0
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 使用默认值
    stats.agents = 0
    stats.workflows = 0
    stats.plugins = 0
    stats.projects = 0
  }
}

// 初始化
onMounted(() => {
  // 加载统计数据
  loadStats()
})


</script>

<style lang="scss" scoped>
.coze-studio-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;

  .coze-header {
    height: 60px;
    background: white;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;

    .header-left {
      display: flex;
      align-items: center;

      .logo-section {
        display: flex;
        align-items: center;
        gap: 16px;

        .coze-logo {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 18px;
          font-weight: 600;
          color: #1f2937;

          .el-icon {
            font-size: 24px;
            color: #3b82f6;
          }
        }

        .workspace-selector {
          .workspace-trigger {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            border-radius: 6px;
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            cursor: pointer;
            transition: all 0.2s ease;
            font-size: 14px;
            color: #475569;

            &:hover {
              background: #f1f5f9;
              border-color: #cbd5e1;
            }

            .el-icon {
              font-size: 16px;
            }
          }
        }
      }
    }

    .header-center {
      flex: 1;
      display: flex;
      justify-content: center;

      .coze-tabs {
        :deep(.el-tabs__header) {
          margin: 0;
          border-bottom: none;
        }

        :deep(.el-tabs__nav-wrap) {
          &::after {
            display: none;
          }
        }

        .tab-label {
          display: flex;
          align-items: center;
          gap: 6px;
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }

  .coze-main {
    flex: 1;
    display: flex;
    overflow: hidden;

    .coze-sidebar {
      width: 280px;
      background: #f8fafc;
      border-right: 1px solid #e2e8f0;
      padding: 20px;
      overflow-y: auto;

      .workspace-info {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        background: white;
        border-radius: 12px;
        margin-bottom: 24px;
        border: 1px solid #e2e8f0;

        .workspace-avatar {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-size: 18px;
        }

        .workspace-details {
          .workspace-name {
            font-size: 16px;
            font-weight: 600;
            color: #1e293b;
            margin-bottom: 2px;
          }

          .workspace-type {
            font-size: 12px;
            color: #64748b;
          }
        }
      }

      .nav-menu {
        .nav-section {
          margin-bottom: 24px;

          .section-title {
            font-size: 12px;
            font-weight: 600;
            color: #64748b;
            margin-bottom: 12px;
            padding: 0 16px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
          }
        }

        .nav-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px 16px;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.2s ease;
          margin-bottom: 4px;
          color: #475569;

          &:hover {
            background: white;
            color: #1e293b;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
          }

          &.active {
            background: white;
            color: #3b82f6;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
          }

          .nav-icon {
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 16px;
          }

          .nav-text {
            font-size: 14px;
            font-weight: 500;
            flex: 1;
          }
        }
      }

      .favorites-section {
        margin-top: 32px;

        .section-title {
          font-size: 14px;
          font-weight: 600;
          color: #64748b;
          margin-bottom: 16px;
          padding: 0 16px;
        }

        .empty-state {
          padding: 20px 16px;
          text-align: center;

          .empty-text {
            font-size: 13px;
            color: #94a3b8;
          }
        }
      }
    }

    .coze-content {
      flex: 1;
      padding: 20px;
      overflow-y: auto;

      .tab-content {
        height: 100%;
        background: white;
        border-radius: 8px;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .coze-studio-container {
    .coze-main {
      .coze-sidebar {
        width: 240px;
      }
    }
  }
}

@media (max-width: 768px) {
  .coze-studio-container {
    .coze-header {
      padding: 0 12px;

      .header-center {
        display: none;
      }
    }

    .coze-main {
      .coze-sidebar {
        position: fixed;
        left: -280px;
        top: 60px;
        height: calc(100vh - 60px);
        z-index: 1000;
        transition: left 0.3s ease;

        &.open {
          left: 0;
        }
      }
    }
  }
}

// 首页内容样式
.home-content {
  padding: 40px;
  max-width: 1200px;
  margin: 0 auto;

  .welcome-section {
    text-align: center;
    margin-bottom: 40px;

    .welcome-title {
      font-size: 32px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 12px;
    }

    .welcome-subtitle {
      font-size: 16px;
      color: #6b7280;
      margin: 0;
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 20px;
    margin-bottom: 40px;

    .stat-card {
      background: white;
      border-radius: 12px;
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 16px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      transition: transform 0.2s, box-shadow 0.2s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      }

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;

        &.agent {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
        }

        &.workflow {
          background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
          color: white;
        }

        &.plugin {
          background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
          color: white;
        }

        &.project {
          background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
          color: white;
        }
      }

      .stat-content {
        .stat-number {
          font-size: 28px;
          font-weight: 700;
          color: #1f2937;
          line-height: 1;
        }

        .stat-label {
          font-size: 14px;
          color: #6b7280;
          margin-top: 4px;
        }
      }
    }
  }

  .quick-start-section {
    .section-title {
      font-size: 24px;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 24px;
    }

    .quick-actions {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 20px;

      .action-card {
        background: white;
        border-radius: 12px;
        padding: 24px;
        display: flex;
        align-items: center;
        gap: 16px;
        cursor: pointer;
        transition: transform 0.2s, box-shadow 0.2s;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        }

        .action-icon {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24px;
        }

        .action-content {
          h3 {
            font-size: 18px;
            font-weight: 600;
            color: #1f2937;
            margin: 0 0 4px 0;
          }

          p {
            font-size: 14px;
            color: #6b7280;
            margin: 0;
          }
        }
      }
    }
  }
}
</style>
