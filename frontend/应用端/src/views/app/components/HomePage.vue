<template>
  <div class="home-page">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="banner-content">
        <h1>欢迎使用 QMS AI</h1>
        <p>智能质量管理助手，让工作更高效</p>
        <div class="quick-actions">
          <el-button type="primary" size="large" @click="quickChat">
            <el-icon><ChatDotRound /></el-icon>
            开始对话
          </el-button>
          <el-button size="large" @click="exploreAgents">
            <el-icon><User /></el-icon>
            探索助手
          </el-button>
        </div>
      </div>
      <div class="banner-image">
        <el-icon size="120"><Cpu /></el-icon>
      </div>
    </div>
    
    <!-- 推荐内容 -->
    <div class="content-sections">
      <!-- 推荐AI助手 -->
      <div class="section">
        <div class="section-header">
          <h2>推荐AI助手</h2>
          <el-button text @click="viewAllAgents">查看全部</el-button>
        </div>
        <div class="agent-grid">
          <div
            v-for="agent in recommendedAgents"
            :key="agent.id"
            class="agent-card"
            @click="selectAgent(agent)"
          >
            <div class="agent-avatar">
              <img v-if="agent.avatar" :src="agent.avatar" :alt="agent.name" />
              <el-icon v-else><User /></el-icon>
            </div>
            <div class="agent-info">
              <h3>{{ agent.name }}</h3>
              <p>{{ agent.description }}</p>
              <div class="agent-stats">
                <span><el-icon><ChatDotRound /></el-icon> {{ agent.conversations }}次对话</span>
                <span><el-icon><Star /></el-icon> {{ agent.rating }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 热门工作流 -->
      <div class="section">
        <div class="section-header">
          <h2>热门工作流</h2>
          <el-button text @click="viewAllWorkflows">查看全部</el-button>
        </div>
        <div class="workflow-grid">
          <div
            v-for="workflow in popularWorkflows"
            :key="workflow.id"
            class="workflow-card"
            @click="selectWorkflow(workflow)"
          >
            <div class="workflow-icon">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="workflow-info">
              <h3>{{ workflow.name }}</h3>
              <p>{{ workflow.description }}</p>
              <div class="workflow-stats">
                <span><el-icon><Timer /></el-icon> {{ workflow.avgTime }}</span>
                <span><el-icon><Check /></el-icon> {{ workflow.successRate }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 知识库 -->
      <div class="section">
        <div class="section-header">
          <h2>知识库</h2>
          <el-button text @click="viewAllKnowledge">查看全部</el-button>
        </div>
        <div class="knowledge-grid">
          <div
            v-for="knowledge in knowledgeBases"
            :key="knowledge.id"
            class="knowledge-card"
            @click="selectKnowledge(knowledge)"
          >
            <div class="knowledge-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="knowledge-info">
              <h3>{{ knowledge.name }}</h3>
              <p>{{ knowledge.description }}</p>
              <div class="knowledge-stats">
                <span><el-icon><Files /></el-icon> {{ knowledge.documents }}个文档</span>
                <span><el-icon><View /></el-icon> {{ knowledge.views }}次查看</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { 
  ChatDotRound, User, Cpu, Star, Connection, Timer, 
  Check, Document, Files, View 
} from '@element-plus/icons-vue'

const emit = defineEmits(['select-item'])

// 推荐AI助手
const recommendedAgents = ref([
  {
    id: '1',
    name: 'QMS质量管理助手',
    description: '专业的质量管理AI助手，帮助您解决ISO标准、质量控制等问题',
    avatar: '',
    conversations: 1234,
    rating: 4.8,
    type: 'agent'
  },
  {
    id: '2',
    name: '文档分析专家',
    description: '智能分析质量文档，提取关键信息和改进建议',
    avatar: '',
    conversations: 856,
    rating: 4.6,
    type: 'agent'
  },
  {
    id: '3',
    name: '流程优化顾问',
    description: '分析业务流程，提供优化建议和最佳实践',
    avatar: '',
    conversations: 642,
    rating: 4.7,
    type: 'agent'
  }
])

// 热门工作流
const popularWorkflows = ref([
  {
    id: '1',
    name: '质量检查流程',
    description: '自动化质量检查，生成检查报告',
    avgTime: '5分钟',
    successRate: 95,
    type: 'workflow'
  },
  {
    id: '2',
    name: '文档审核流程',
    description: '智能审核质量文档，标记问题点',
    avgTime: '3分钟',
    successRate: 92,
    type: 'workflow'
  },
  {
    id: '3',
    name: '数据分析流程',
    description: '分析质量数据，生成可视化报告',
    avgTime: '8分钟',
    successRate: 88,
    type: 'workflow'
  }
])

// 知识库
const knowledgeBases = ref([
  {
    id: '1',
    name: 'ISO标准库',
    description: '包含各种ISO质量管理标准文档',
    documents: 156,
    views: 2341,
    type: 'knowledge'
  },
  {
    id: '2',
    name: '质量管理手册',
    description: '企业质量管理制度和操作手册',
    documents: 89,
    views: 1876,
    type: 'knowledge'
  },
  {
    id: '3',
    name: '最佳实践案例',
    description: '行业质量管理最佳实践和案例分析',
    documents: 234,
    views: 3456,
    type: 'knowledge'
  }
])

// 方法
const quickChat = () => {
  // 快速开始对话
  emit('select-item', {
    type: 'agent',
    id: 'quick-chat',
    name: '快速对话'
  })
}

const exploreAgents = () => {
  // 探索AI助手
  emit('select-item', {
    type: 'explore',
    category: 'agents'
  })
}

const selectAgent = (agent) => {
  emit('select-item', agent)
}

const selectWorkflow = (workflow) => {
  emit('select-item', workflow)
}

const selectKnowledge = (knowledge) => {
  emit('select-item', knowledge)
}

const viewAllAgents = () => {
  emit('select-item', {
    type: 'view-all',
    category: 'agents'
  })
}

const viewAllWorkflows = () => {
  emit('select-item', {
    type: 'view-all',
    category: 'workflows'
  })
}

const viewAllKnowledge = () => {
  emit('select-item', {
    type: 'view-all',
    category: 'knowledge'
  })
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.home-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 40px;
}

.banner-content h1 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 600;
}

.banner-content p {
  margin: 0 0 24px 0;
  font-size: 16px;
  opacity: 0.9;
}

.quick-actions {
  display: flex;
  gap: 16px;
}

.banner-image {
  opacity: 0.3;
}

.content-sections {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.agent-grid,
.workflow-grid,
.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.agent-card,
.workflow-card,
.knowledge-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  gap: 12px;
}

.agent-card:hover,
.workflow-card:hover,
.knowledge-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.agent-avatar,
.workflow-icon,
.knowledge-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.agent-avatar {
  background: #409eff;
  color: white;
}

.workflow-icon {
  background: #67c23a;
  color: white;
}

.knowledge-icon {
  background: #e6a23c;
  color: white;
}

.agent-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  object-fit: cover;
}

.agent-info,
.workflow-info,
.knowledge-info {
  flex: 1;
  min-width: 0;
}

.agent-info h3,
.workflow-info h3,
.knowledge-info h3 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.agent-info p,
.workflow-info p,
.knowledge-info p {
  margin: 0 0 8px 0;
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.agent-stats,
.workflow-stats,
.knowledge-stats {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #909399;
}

.agent-stats span,
.workflow-stats span,
.knowledge-stats span {
  display: flex;
  align-items: center;
  gap: 2px;
}

@media (max-width: 768px) {
  .welcome-banner {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .agent-grid,
  .workflow-grid,
  .knowledge-grid {
    grid-template-columns: 1fr;
  }
}
</style>
