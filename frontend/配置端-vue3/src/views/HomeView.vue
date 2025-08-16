<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <el-card class="welcome-card">
        <div class="welcome-content">
          <div class="welcome-text">
            <h1>欢迎使用 QMS-AI配置中心</h1>
            <p>智能质量管理系统配置平台，让AI赋能质量管理</p>
          </div>
          <div class="welcome-actions">
            <el-button type="primary" size="large" @click="handleQuickStart">
              <el-icon><Rocket /></el-icon>
              快速开始
            </el-button>
            <el-button size="large" @click="handleViewDocs">
              <el-icon><Document /></el-icon>
              查看文档
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 系统概览 -->
    <div class="overview-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="overview-icon ai">
                <el-icon><Cpu /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-title">AI模型</div>
                <div class="overview-value">{{ overview.aiModels }}</div>
                <div class="overview-desc">个可用模型</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="overview-icon users">
                <el-icon><User /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-title">活跃用户</div>
                <div class="overview-value">{{ overview.activeUsers }}</div>
                <div class="overview-desc">今日活跃</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="overview-icon conversations">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-title">对话次数</div>
                <div class="overview-value">{{ overview.conversations }}</div>
                <div class="overview-desc">今日对话</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="overview-card">
            <div class="overview-item">
              <div class="overview-icon success">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="overview-info">
                <div class="overview-title">成功率</div>
                <div class="overview-value">{{ overview.successRate }}</div>
                <div class="overview-desc">系统稳定性</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions-section">
      <el-card>
        <template #header>
          <span>快捷操作</span>
        </template>
        <el-row :gutter="20">
          <el-col :span="4" v-for="action in quickActions" :key="action.id">
            <div class="quick-action-item" @click="handleQuickAction(action)">
              <div class="action-icon" :class="action.type">
                <el-icon>
                  <component :is="action.icon" />
                </el-icon>
              </div>
              <div class="action-title">{{ action.title }}</div>
              <div class="action-desc">{{ action.description }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activities-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>最近活动</span>
            </template>
            <div class="activities-list">
              <div
                v-for="activity in recentActivities"
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-icon" :class="activity.type">
                  <el-icon>
                    <component :is="activity.icon" />
                  </el-icon>
                </div>
                <div class="activity-content">
                  <div class="activity-title">{{ activity.title }}</div>
                  <div class="activity-time">{{ activity.time }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>系统状态</span>
            </template>
            <div class="system-status">
              <div
                v-for="status in systemStatus"
                :key="status.name"
                class="status-item"
              >
                <div class="status-name">{{ status.name }}</div>
                <div class="status-value" :class="status.status">
                  <el-tag :type="getStatusType(status.status)">
                    {{ status.value }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Rocket,
  Document,
  Cpu,
  User,
  ChatDotRound,
  SuccessFilled,
  Setting,
  DataAnalysis,
  Monitor,
  Files,
  Connection,
  Lock
} from '@element-plus/icons-vue'

const router = useRouter()

// 系统概览数据
const overview = reactive({
  aiModels: 8,
  activeUsers: 156,
  conversations: '2.3K',
  successRate: '99.8%'
})

// 快捷操作
const quickActions = ref([
  {
    id: 1,
    title: '模型配置',
    description: '配置AI模型',
    icon: 'Cpu',
    type: 'primary',
    route: '/ai-management/model-config'
  },
  {
    id: 2,
    title: '对话配置',
    description: '设置对话参数',
    icon: 'ChatDotRound',
    type: 'success',
    route: '/ai-management/conversation-config'
  },
  {
    id: 3,
    title: '数据源配置',
    description: '管理数据源',
    icon: 'DataAnalysis',
    type: 'warning',
    route: '/ai-management/data-source-config'
  },
  {
    id: 4,
    title: '应用管控',
    description: '监控应用状态',
    icon: 'Monitor',
    type: 'info',
    route: '/ai-management/app-control'
  },
  {
    id: 5,
    title: '用户权限',
    description: '管理用户权限',
    icon: 'Lock',
    type: 'danger',
    route: '/ai-management/user-control'
  },
  {
    id: 6,
    title: '聊天测试',
    description: '测试AI对话',
    icon: 'Connection',
    type: 'primary',
    route: '/ai-management/chat-test'
  }
])

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    title: '用户张三配置了新的AI模型',
    time: '5分钟前',
    type: 'success',
    icon: 'Cpu'
  },
  {
    id: 2,
    title: '系统自动备份完成',
    time: '1小时前',
    type: 'info',
    icon: 'Files'
  },
  {
    id: 3,
    title: '检测到异常访问，已自动拦截',
    time: '2小时前',
    type: 'warning',
    icon: 'Lock'
  },
  {
    id: 4,
    title: '新用户李四注册成功',
    time: '3小时前',
    type: 'success',
    icon: 'User'
  }
])

// 系统状态
const systemStatus = ref([
  { name: 'AI服务', status: 'normal', value: '正常' },
  { name: '数据库', status: 'normal', value: '正常' },
  { name: '缓存服务', status: 'normal', value: '正常' },
  { name: '消息队列', status: 'warning', value: '警告' },
  { name: '文件存储', status: 'normal', value: '正常' },
  { name: '监控服务', status: 'normal', value: '正常' }
])

// 方法
const handleQuickStart = () => {
  router.push('/ai-management/model-config')
}

const handleViewDocs = () => {
  ElMessage.info('文档功能开发中...')
}

const handleQuickAction = (action: any) => {
  router.push(action.route)
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    normal: 'success',
    warning: 'warning',
    error: 'danger'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  // 模拟数据更新
  setInterval(() => {
    overview.activeUsers = Math.floor(Math.random() * 50) + 150
    overview.conversations = (Math.floor(Math.random() * 500) + 2000).toLocaleString() + 'K'
  }, 10000)
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 欢迎区域 */
.welcome-section {
  margin-bottom: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  color: white;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.welcome-text h1 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

.welcome-text p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.welcome-actions {
  display: flex;
  gap: 15px;
}

/* 系统概览 */
.overview-section {
  margin-bottom: 20px;
}

.overview-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-2px);
}

.overview-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.overview-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.overview-icon.ai {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.overview-icon.users {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.overview-icon.conversations {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.overview-icon.success {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.overview-info {
  flex: 1;
}

.overview-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.overview-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.overview-desc {
  font-size: 12px;
  color: #c0c4cc;
}

/* 快捷操作 */
.quick-actions-section {
  margin-bottom: 20px;
}

.quick-action-item {
  text-align: center;
  padding: 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.quick-action-item:hover {
  background: #f8f9fa;
  border-color: #409eff;
  transform: translateY(-2px);
}

.action-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  font-size: 20px;
  color: white;
}

.action-icon.primary {
  background: linear-gradient(135deg, #409eff 0%, #3a8ee6 100%);
}

.action-icon.success {
  background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
}

.action-icon.warning {
  background: linear-gradient(135deg, #e6a23c 0%, #cf9236 100%);
}

.action-icon.info {
  background: linear-gradient(135deg, #909399 0%, #82848a 100%);
}

.action-icon.danger {
  background: linear-gradient(135deg, #f56c6c 0%, #f45656 100%);
}

.action-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 5px;
}

.action-desc {
  font-size: 12px;
  color: #909399;
}

/* 最近活动 */
.recent-activities-section {
  margin-bottom: 20px;
}

.activities-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 16px;
  color: white;
}

.activity-icon.success {
  background: #67c23a;
}

.activity-icon.info {
  background: #409eff;
}

.activity-icon.warning {
  background: #e6a23c;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.activity-time {
  font-size: 12px;
  color: #909399;
}

/* 系统状态 */
.system-status {
  max-height: 300px;
  overflow-y: auto;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #ebeef5;
}

.status-item:last-child {
  border-bottom: none;
}

.status-name {
  font-size: 14px;
  color: #303133;
}

:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-card__header) {
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.welcome-card .el-card__body) {
  background: transparent;
}
</style>
