<template>
  <div class="app-control-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>应用端管控中心</h2>
      <p>实时监控和管理所有应用端的运行状态、配置同步</p>
    </div>

    <!-- 应用端状态监控 -->
    <el-row :gutter="20" class="status-cards">
      <el-col v-for="app in appStatus" :key="app.id" :span="8">
        <el-card class="status-card" :class="getStatusClass(app.status)">
          <div class="status-header">
            <div class="app-info">
              <h3 class="app-name">{{ app.name }}</h3>
              <span class="app-url">{{ app.url }}</span>
            </div>
            <el-tag :type="getStatusType(app.status)" size="large">
              {{ getStatusText(app.status) }}
            </el-tag>
          </div>
          
          <div class="status-metrics">
            <div class="metric-row">
              <div class="metric">
                <span class="label">在线用户</span>
                <span class="value">{{ app.online_users }}</span>
              </div>
              <div class="metric">
                <span class="label">今日对话</span>
                <span class="value">{{ app.today_conversations }}</span>
              </div>
            </div>
            <div class="metric-row">
              <div class="metric">
                <span class="label">响应时间</span>
                <span class="value">{{ app.response_time }}ms</span>
              </div>
              <div class="metric">
                <span class="label">错误率</span>
                <span class="value">{{ app.error_rate }}%</span>
              </div>
            </div>
          </div>
          
          <div class="status-actions">
            <el-button size="small" @click="viewAppDetails(app)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button size="small" type="primary" @click="configureApp(app)">
              <el-icon><Setting /></el-icon>
              配置
            </el-button>
            <el-button
              size="small"
              :type="app.status === 'running' ? 'danger' : 'success'" 
              @click="toggleAppStatus(app)"
            >
              <el-icon><component :is="app.status === 'running' ? 'VideoPause' : 'VideoPlay'" /></el-icon>
              {{ app.status === 'running' ? '停止' : '启动' }}
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 配置推送控制 -->
    <el-card title="配置推送控制" class="config-push-card">
      <template #header>
        <div class="card-header">
          <span>配置推送控制</span>
          <el-button type="primary" size="small" @click="showConfigTemplates">
            <el-icon><Document /></el-icon>
            配置模板
          </el-button>
        </div>
      </template>
      
      <div class="config-push-content">
        <el-form ref="configFormRef" :model="configPushForm" label-width="120px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="目标应用:" required>
                <el-select v-model="configPushForm.target_apps" multiple placeholder="选择目标应用">
                  <el-option 
                    v-for="app in availableApps" 
                    :key="app.id" 
                    :label="app.name" 
                    :value="app.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            
            <el-col :span="12">
              <el-form-item label="配置类型:" required>
                <el-select v-model="configPushForm.config_type" @change="onConfigTypeChange">
                  <el-option label="AI模型配置" value="ai.config" />
                  <el-option label="用户权限配置" value="permission.config" />
                  <el-option label="界面主题配置" value="app.config" />
                  <el-option label="系统API配置" value="system.api" />
                  <el-option label="监控配置" value="monitoring.config" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="配置内容:">
            <div class="config-editor-container">
              <div class="editor-toolbar">
                <el-button size="small" @click="formatConfig">
                  <el-icon><Magic /></el-icon>
                  格式化
                </el-button>
                <el-button size="small" @click="validateConfig">
                  <el-icon><Check /></el-icon>
                  验证
                </el-button>
                <el-button size="small" @click="loadCurrentConfig">
                  <el-icon><Refresh /></el-icon>
                  加载当前配置
                </el-button>
              </div>
              <textarea 
                v-model="configPushForm.config_content"
                class="config-editor"
                rows="15"
                placeholder="请输入JSON格式的配置内容..."
              />
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" :loading="pushing" @click="pushConfig">
              <el-icon><Upload /></el-icon>
              推送配置
            </el-button>
            <el-button @click="previewConfig">
              <el-icon><View /></el-icon>
              预览效果
            </el-button>
            <el-button @click="resetForm">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 实时配置同步状态 -->
    <el-card title="配置同步状态" class="sync-status-card">
      <template #header>
        <div class="card-header">
          <span>配置同步状态</span>
          <div class="header-actions">
            <el-button size="small" @click="refreshSyncStatus">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button size="small" @click="clearSyncHistory">
              <el-icon><Delete /></el-icon>
              清空历史
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table v-loading="loadingSyncStatus" :data="syncStatus" style="width: 100%">
        <el-table-column prop="target_name" label="应用名称" width="150" />
        <el-table-column prop="config_key" label="配置类型" width="180">
          <template #default="{ row }">
            <el-tag size="small">{{ getConfigTypeLabel(row.config_key) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="timestamp" label="同步时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="同步状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getSyncStatusType(row.status)">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="error" label="错误信息" min-width="200">
          <template #default="{ row }">
            <span v-if="row.error" class="error-message">{{ row.error }}</span>
            <span v-else class="success-message">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="retrySync(row)">
              <el-icon><Refresh /></el-icon>
              重试
            </el-button>
            <el-button size="small" type="danger" @click="rollbackConfig(row)">
              <el-icon><Back /></el-icon>
              回滚
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 配置预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="配置预览" width="60%">
      <div class="config-preview">
        <pre>{{ formattedPreviewConfig }}</pre>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="confirmPushFromPreview">确认推送</el-button>
      </template>
    </el-dialog>

    <!-- 应用详情对话框 -->
    <el-dialog v-model="appDetailsDialogVisible" title="应用详情" width="70%">
      <div v-if="selectedApp" class="app-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="应用名称">{{ selectedApp.name }}</el-descriptions-item>
          <el-descriptions-item label="访问地址">{{ selectedApp.url }}</el-descriptions-item>
          <el-descriptions-item label="运行状态">
            <el-tag :type="getStatusType(selectedApp.status)">
              {{ getStatusText(selectedApp.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="启动时间">{{ selectedApp.start_time }}</el-descriptions-item>
          <el-descriptions-item label="在线用户">{{ selectedApp.online_users }}</el-descriptions-item>
          <el-descriptions-item label="今日对话">{{ selectedApp.today_conversations }}</el-descriptions-item>
          <el-descriptions-item label="平均响应时间">{{ selectedApp.response_time }}ms</el-descriptions-item>
          <el-descriptions-item label="错误率">{{ selectedApp.error_rate }}%</el-descriptions-item>
        </el-descriptions>
        
        <div class="app-metrics-chart" style="margin-top: 20px;">
          <h4>性能指标趋势</h4>
          <div ref="metricsChartRef" style="height: 300px;" />
        </div>
      </div>
      <template #footer>
        <el-button @click="appDetailsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
// 使用Element UI的消息组件
import { ElMessage, ElMessageBox } from 'element-ui'
import localConfigCenter from '@/config/local-config-center'

// 响应式数据
const appStatus = ref([
  {
    id: 'app-frontend',
    name: '应用端',
    status: 'running',
    url: 'http://localhost:8080',
    online_users: 15,
    today_conversations: 234,
    response_time: 120,
    error_rate: 0.5,
    start_time: '2024-01-15 09:00:00'
  },
  {
    id: 'config-driven-frontend',
    name: '配置驱动端',
    status: 'stopped',
    url: 'http://localhost:8073',
    online_users: 0,
    today_conversations: 0,
    response_time: 0,
    error_rate: 0,
    start_time: '-'
  }
])

const configPushForm = ref({
  target_apps: [],
  config_type: '',
  config_content: ''
})

const syncStatus = ref([])
const pushing = ref(false)
const loadingSyncStatus = ref(false)
const previewDialogVisible = ref(false)
const appDetailsDialogVisible = ref(false)
const selectedApp = ref(null)

// 计算属性
const availableApps = computed(() => {
  return appStatus.value.filter(app => app.status === 'running')
})

const formattedPreviewConfig = computed(() => {
  try {
    return JSON.stringify(JSON.parse(configPushForm.value.config_content), null, 2)
  } catch {
    return configPushForm.value.config_content
  }
})

// 方法
const getStatusClass = (status) => {
  return {
    'status-running': status === 'running',
    'status-stopped': status === 'stopped',
    'status-error': status === 'error'
  }
}

const getStatusType = (status) => {
  const types = {
    'running': 'success',
    'stopped': 'info', 
    'error': 'danger'
  }

  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    'running': '运行中',
    'stopped': '已停止',
    'error': '错误'
  }

  return texts[status] || '未知'
}

const getSyncStatusType = (status) => {
  return status === 'success' ? 'success' : 'danger'
}

const getConfigTypeLabel = (configKey) => {
  const labels = {
    'ai.config': 'AI模型配置',
    'permission.config': '权限配置',
    'app.config': '应用配置',
    'system.api': 'API配置',
    'monitoring.config': '监控配置'
  }

  return labels[configKey] || configKey
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleString()
}

// 事件处理
const onConfigTypeChange = async () => {
  if (configPushForm.value.config_type) {
    await loadCurrentConfig()
  }
}

const loadCurrentConfig = async () => {
  try {
    const config = await localConfigCenter.getConfig(configPushForm.value.config_type)

    if (config) {
      configPushForm.value.config_content = JSON.stringify(config, null, 2)
    }
  } catch (error) {
    ElMessage.error('加载配置失败: ' + error.message)
  }
}

const formatConfig = () => {
  try {
    const parsed = JSON.parse(configPushForm.value.config_content)

    configPushForm.value.config_content = JSON.stringify(parsed, null, 2)
    ElMessage.success('配置格式化成功')
  } catch (error) {
    ElMessage.error('配置格式错误，无法格式化')
  }
}

const validateConfig = () => {
  try {
    JSON.parse(configPushForm.value.config_content)
    ElMessage.success('配置格式验证通过')
  } catch (error) {
    ElMessage.error('配置格式错误: ' + error.message)
  }
}

const pushConfig = async () => {
  if (!configPushForm.value.target_apps.length) {
    ElMessage.error('请选择目标应用')

    return
  }
  
  if (!configPushForm.value.config_type) {
    ElMessage.error('请选择配置类型')

    return
  }
  
  try {
    JSON.parse(configPushForm.value.config_content)
  } catch (error) {
    ElMessage.error('配置格式错误，请检查JSON格式')

    return
  }
  
  pushing.value = true
  
  try {
    const configValue = JSON.parse(configPushForm.value.config_content)
    
    // 先更新本地配置
    await localConfigCenter.setConfig(configPushForm.value.config_type, configValue)
    
    // 同步到应用端
    const results = await localConfigCenter.syncConfigToApps(
      configPushForm.value.config_type,
      configValue,
      configPushForm.value.target_apps
    )
    
    const successCount = results.filter(r => r.status === 'success').length
    const failCount = results.filter(r => r.status === 'failed').length
    
    if (successCount > 0) {
      ElMessage.success(`配置推送完成：成功 ${successCount} 个，失败 ${failCount} 个`)
    } else {
      ElMessage.error('配置推送失败，请检查目标应用状态')
    }
    
    // 刷新同步状态
    await refreshSyncStatus()
    
  } catch (error) {
    ElMessage.error('配置推送失败: ' + error.message)
  } finally {
    pushing.value = false
  }
}

const previewConfig = () => {
  if (!configPushForm.value.config_content) {
    ElMessage.error('请输入配置内容')

    return
  }
  
  try {
    JSON.parse(configPushForm.value.config_content)
    previewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('配置格式错误，无法预览')
  }
}

const confirmPushFromPreview = () => {
  previewDialogVisible.value = false
  pushConfig()
}

const resetForm = () => {
  configPushForm.value = {
    target_apps: [],
    config_type: '',
    config_content: ''
  }
}

const refreshSyncStatus = async () => {
  loadingSyncStatus.value = true
  try {
    syncStatus.value = localConfigCenter.getSyncStatus()
  } finally {
    loadingSyncStatus.value = false
  }
}

const clearSyncHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空同步历史吗？', '确认', {
      type: 'warning'
    })
    
    localConfigCenter.syncHistory = []
    await refreshSyncStatus()
    ElMessage.success('同步历史已清空')
  } catch {
    // 用户取消
  }
}

const retrySync = async (row) => {
  try {
    const config = await localConfigCenter.getConfig(row.config_key)

    if (config) {
      const results = await localConfigCenter.syncConfigToApps(
        row.config_key,
        config,
        [row.target_name]
      )
      
      if (results[0]?.status === 'success') {
        ElMessage.success('重试同步成功')
        await refreshSyncStatus()
      } else {
        ElMessage.error('重试同步失败: ' + results[0]?.message)
      }
    }
  } catch (error) {
    ElMessage.error('重试失败: ' + error.message)
  }
}

const rollbackConfig = async (_row) => {
  ElMessage.info('回滚功能开发中...')
}

const viewAppDetails = (app) => {
  selectedApp.value = app
  appDetailsDialogVisible.value = true
}

const configureApp = (_app) => {
  configPushForm.value.target_apps = [_app.id]
  ElMessage.info(`已选择 ${_app.name} 作为配置目标`)
}

const toggleAppStatus = async (_app) => {
  ElMessage.info('应用启停功能开发中...')
}

const showConfigTemplates = () => {
  ElMessage.info('配置模板功能开发中...')
}

// 生命周期
onMounted(async () => {
  await refreshSyncStatus()
})
</script>

<style scoped>
.app-control-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.status-cards {
  margin-bottom: 24px;
}

.status-card {
  transition: all 0.3s;
}

.status-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.status-card.status-running {
  border-left: 4px solid #67c23a;
}

.status-card.status-stopped {
  border-left: 4px solid #909399;
}

.status-card.status-error {
  border-left: 4px solid #f56c6c;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.app-info h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #303133;
}

.app-url {
  font-size: 12px;
  color: #909399;
}

.status-metrics {
  margin-bottom: 16px;
}

.metric-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.metric {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.metric .label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.metric .value {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.status-actions {
  display: flex;
  gap: 8px;
}

.config-push-card,
.sync-status-card {
  margin-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.editor-toolbar {
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  gap: 8px;
}

.config-editor {
  width: 100%;
  border: none;
  outline: none;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  resize: vertical;
}

.config-preview pre {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
}

.success-message {
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 8px;
}
</style>
