<template>
  <div class="app-container">
    <!-- 顶部导航栏 -->
    <div class="app-header">
      <div class="header-left">
        <div class="logo-section">
          <div class="app-logo">
            <el-icon><ChatDotRound /></el-icon>
            <span class="logo-text">QMS AI</span>
          </div>
        </div>
      </div>
      
      <div class="header-center">
        <div class="search-bar">
          <el-input
            v-model="searchQuery"
            placeholder="搜索AI助手、工作流或知识..."
            :prefix-icon="Search"
            size="large"
            @keyup.enter="handleSearch"
          />
        </div>
      </div>
      
      <div class="header-right">
        <el-button :icon="Bell" circle />
        <el-dropdown @command="handleUserAction">
          <el-avatar :size="32" src="/avatar.jpg" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人资料</el-dropdown-item>
              <el-dropdown-item command="settings">设置</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="app-main">
      <!-- 侧边栏 -->
      <div class="app-sidebar">
        <!-- 导航菜单 -->
        <div class="nav-menu">
          <div
            v-for="item in navItems"
            :key="item.key"
            :class="['nav-item', { active: activeNav === item.key }]"
            @click="activeNav = item.key"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </div>
        </div>
        
        <!-- 最近使用 -->
        <div class="recent-section">
          <h4>最近使用</h4>
          <div class="recent-list">
            <div
              v-for="item in recentItems"
              :key="item.id"
              class="recent-item"
              @click="openRecentItem(item)"
            >
              <div class="item-avatar">
                <el-icon><component :is="item.icon" /></el-icon>
              </div>
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-type">{{ item.type }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 主内容区 -->
      <div class="app-content">
        <!-- 首页 -->
        <div v-if="activeNav === 'home'" class="content-panel">
          <HomePage @select-item="handleSelectItem" />
        </div>
        
        <!-- AI助手 -->
        <div v-if="activeNav === 'agents'" class="content-panel">
          <AgentList @select-agent="handleSelectAgent" />
        </div>
        
        <!-- 工作流 -->
        <div v-if="activeNav === 'workflows'" class="content-panel">
          <WorkflowList @select-workflow="handleSelectWorkflow" />
        </div>
        
        <!-- 知识库 -->
        <div v-if="activeNav === 'knowledge'" class="content-panel">
          <KnowledgeList @select-knowledge="handleSelectKnowledge" />
        </div>
        
        <!-- 历史记录 -->
        <div v-if="activeNav === 'history'" class="content-panel">
          <HistoryList @select-conversation="handleSelectConversation" />
        </div>
      </div>
      
      <!-- 聊天面板 -->
      <div v-if="showChatPanel" class="chat-panel">
        <ChatInterface
          :agent="selectedAgent"
          :conversation="selectedConversation"
          @close="closeChatPanel"
        />
      </div>
    </div>
    
    <!-- 快速启动浮动按钮 -->
    <el-button
      class="quick-start-btn"
      type="primary"
      :icon="Plus"
      circle
      size="large"
      @click="showQuickStart = true"
    />
    
    <!-- 快速启动对话框 -->
    <QuickStartDialog
      v-model="showQuickStart"
      @start-chat="handleQuickStart"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  ChatDotRound, Search, Bell, Plus, User, Connection, 
  Document, Clock, House 
} from '@element-plus/icons-vue'
import HomePage from './components/HomePage.vue'
import AgentList from './components/AgentList.vue'
import WorkflowList from './components/WorkflowList.vue'
import KnowledgeList from './components/KnowledgeList.vue'
import HistoryList from './components/HistoryList.vue'
import ChatInterface from './components/ChatInterface.vue'
import QuickStartDialog from './components/QuickStartDialog.vue'

// 响应式数据
const activeNav = ref('home')
const searchQuery = ref('')
const showChatPanel = ref(false)
const showQuickStart = ref(false)
const selectedAgent = ref(null)
const selectedConversation = ref(null)

// 导航菜单
const navItems = ref([
  { key: 'home', label: '首页', icon: 'House' },
  { key: 'agents', label: 'AI助手', icon: 'User' },
  { key: 'workflows', label: '工作流', icon: 'Connection' },
  { key: 'knowledge', label: '知识库', icon: 'Document' },
  { key: 'history', label: '历史记录', icon: 'Clock' }
])

// 最近使用项目
const recentItems = ref([
  {
    id: '1',
    name: 'QMS质量管理助手',
    type: 'AI助手',
    icon: 'User',
    lastUsed: new Date()
  },
  {
    id: '2',
    name: 'ISO标准查询',
    type: '知识库',
    icon: 'Document',
    lastUsed: new Date()
  },
  {
    id: '3',
    name: '质量检查流程',
    type: '工作流',
    icon: 'Connection',
    lastUsed: new Date()
  }
])

// 方法
const handleSearch = () => {
  if (!searchQuery.value.trim()) return
  
  // 执行搜索逻辑
  ElMessage.info(`搜索: ${searchQuery.value}`)
}

const handleUserAction = (command) => {
  switch (command) {
    case 'profile':
      // 跳转到个人资料页面
      break
    case 'settings':
      // 跳转到设置页面
      break
    case 'logout':
      // 退出登录
      break
  }
}

const handleSelectItem = (item) => {
  // 处理选择项目
  if (item.type === 'agent') {
    handleSelectAgent(item)
  } else if (item.type === 'workflow') {
    handleSelectWorkflow(item)
  } else if (item.type === 'knowledge') {
    handleSelectKnowledge(item)
  }
}

const handleSelectAgent = (agent) => {
  selectedAgent.value = agent
  selectedConversation.value = null
  showChatPanel.value = true
  
  // 添加到最近使用
  addToRecent({
    id: agent.id,
    name: agent.name,
    type: 'AI助手',
    icon: 'User'
  })
}

const handleSelectWorkflow = (workflow) => {
  // 执行工作流
  ElMessage.info(`启动工作流: ${workflow.name}`)
  
  // 添加到最近使用
  addToRecent({
    id: workflow.id,
    name: workflow.name,
    type: '工作流',
    icon: 'Connection'
  })
}

const handleSelectKnowledge = (knowledge) => {
  // 打开知识库
  ElMessage.info(`打开知识库: ${knowledge.name}`)
  
  // 添加到最近使用
  addToRecent({
    id: knowledge.id,
    name: knowledge.name,
    type: '知识库',
    icon: 'Document'
  })
}

const handleSelectConversation = (conversation) => {
  selectedConversation.value = conversation
  selectedAgent.value = conversation.agent
  showChatPanel.value = true
}

const closeChatPanel = () => {
  showChatPanel.value = false
  selectedAgent.value = null
  selectedConversation.value = null
}

const handleQuickStart = (config) => {
  if (config.type === 'agent') {
    handleSelectAgent(config.agent)
  } else if (config.type === 'workflow') {
    handleSelectWorkflow(config.workflow)
  }
}

const openRecentItem = (item) => {
  // 根据类型打开对应项目
  if (item.type === 'AI助手') {
    // 查找对应的Agent并打开
    handleSelectAgent({ id: item.id, name: item.name })
  } else if (item.type === '工作流') {
    // 查找对应的Workflow并执行
    handleSelectWorkflow({ id: item.id, name: item.name })
  } else if (item.type === '知识库') {
    // 查找对应的Knowledge并打开
    handleSelectKnowledge({ id: item.id, name: item.name })
  }
}

const addToRecent = (item) => {
  // 移除已存在的项目
  const index = recentItems.value.findIndex(i => i.id === item.id)
  if (index > -1) {
    recentItems.value.splice(index, 1)
  }
  
  // 添加到开头
  recentItems.value.unshift({
    ...item,
    lastUsed: new Date()
  })
  
  // 限制最大数量
  if (recentItems.value.length > 10) {
    recentItems.value = recentItems.value.slice(0, 10)
  }
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.app-header {
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

.app-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  max-width: 600px;
  margin: 0 40px;
}

.search-bar {
  width: 100%;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.app-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  position: relative;
}

.app-sidebar {
  width: 260px;
  background: white;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.nav-menu {
  padding: 20px 16px;
  border-bottom: 1px solid #e4e7ed;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
  font-size: 14px;
  color: #606266;
}

.nav-item:hover {
  background: #f0f9ff;
  color: #409eff;
}

.nav-item.active {
  background: #e6f7ff;
  color: #409eff;
  font-weight: 500;
}

.recent-section {
  flex: 1;
  padding: 20px 16px;
  overflow-y: auto;
}

.recent-section h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
}

.recent-item:hover {
  background: #f5f7fa;
}

.item-avatar {
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

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-type {
  font-size: 12px;
  color: #909399;
}

.app-content {
  flex: 1;
  background: white;
  overflow: hidden;
}

.content-panel {
  height: 100%;
  overflow-y: auto;
}

.chat-panel {
  width: 400px;
  border-left: 1px solid #e4e7ed;
  background: white;
  display: flex;
  flex-direction: column;
}

.quick-start-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  z-index: 1000;
}

@media (max-width: 768px) {
  .app-sidebar {
    width: 200px;
  }
  
  .header-center {
    margin: 0 20px;
  }
  
  .chat-panel {
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    z-index: 100;
  }
}
</style>
