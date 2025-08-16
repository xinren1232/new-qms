<template>
  <div class="app-control">
    <div class="page-header">
      <h2>应用端管控中心</h2>
      <p class="text-muted">实时监控和管理所有应用</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon online">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">在线用户</div>
                <div class="stat-value">{{ stats.onlineUsers }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon response">
                <el-icon><Timer /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">响应时间</div>
                <div class="stat-value">{{ stats.responseTime }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon requests">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">今日请求</div>
                <div class="stat-value">{{ stats.todayRequests }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon success">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">成功率</div>
                <div class="stat-value">{{ stats.successRate }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 应用端配置 -->
    <div class="app-config-section">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>应用端</span>
            <el-button type="primary" size="small" @click="handleConfig">
              配置
            </el-button>
          </div>
        </template>

        <div class="app-info">
          <div class="app-url">
            <label>应用端地址:</label>
            <span>http://localhost:8080</span>
          </div>

          <div class="app-status">
            <label>状态:</label>
            <el-tag type="success">运行中</el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 配置推送控制 -->
    <div class="config-push-section">
      <el-card>
        <template #header>
          <span>配置推送控制</span>
        </template>

        <el-form :model="configForm" label-width="120px">
          <el-form-item label="目标应用:">
            <el-select v-model="configForm.targetApp" placeholder="选择目标应用">
              <el-option label="QMS主应用" value="qms-main" />
              <el-option label="移动端应用" value="qms-mobile" />
              <el-option label="第三方集成" value="third-party" />
            </el-select>
          </el-form-item>

          <el-form-item label="配置内容:">
            <el-input
              v-model="configForm.content"
              type="textarea"
              :rows="4"
              placeholder="请输入配置内容..."
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handlePushConfig">
              推送配置
            </el-button>
            <el-button @click="handleResetConfig">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Monitor, Timer, DataAnalysis, SuccessFilled } from '@element-plus/icons-vue'

// 统计数据
const stats = reactive({
  onlineUsers: 15,
  responseTime: '120ms',
  todayRequests: '2,456',
  successRate: '99.8%'
})

// 配置表单
const configForm = reactive({
  targetApp: '',
  content: ''
})

// 方法
const handleConfig = () => {
  ElMessage.info('配置功能开发中...')
}

const handlePushConfig = () => {
  if (!configForm.targetApp) {
    ElMessage.warning('请选择目标应用')
    return
  }
  if (!configForm.content) {
    ElMessage.warning('请输入配置内容')
    return
  }

  ElMessage.success('配置推送成功')
}

const handleResetConfig = () => {
  configForm.targetApp = ''
  configForm.content = ''
}

// 模拟实时数据更新
const updateStats = () => {
  setInterval(() => {
    stats.onlineUsers = Math.floor(Math.random() * 10) + 10
    stats.responseTime = Math.floor(Math.random() * 50) + 100 + 'ms'
    stats.todayRequests = (Math.floor(Math.random() * 1000) + 2000).toLocaleString()
  }, 5000)
}

onMounted(() => {
  updateStats()
})
</script>

<style scoped>
.app-control {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.text-muted {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

/* 统计卡片样式 */
.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stat-icon.online {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.response {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.requests {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.success {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

/* 应用配置区域 */
.app-config-section {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-info {
  display: flex;
  gap: 30px;
  align-items: center;
}

.app-info label {
  font-weight: 500;
  color: #606266;
  margin-right: 8px;
}

.app-info span {
  color: #303133;
}

/* 配置推送区域 */
.config-push-section {
  margin-bottom: 20px;
}

:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-card__header) {
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}
</style>
