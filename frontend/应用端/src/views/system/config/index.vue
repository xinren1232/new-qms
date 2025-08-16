<template>
  <div class="system-config-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">系统配置管理</h2>
      <p class="page-description">管理配置驱动系统的各项配置，实时同步到应用端</p>
    </div>

    <!-- 配置概览 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-item">
            <div class="overview-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="overview-content">
              <h3>AI对话记录</h3>
              <p>{{ getModuleStatus('aiConversation') }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-item">
            <div class="overview-icon">
              <el-icon><Database /></el-icon>
            </div>
            <div class="overview-content">
              <h3>数据源配置</h3>
              <p>{{ getModuleStatus('dataSource') }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-item">
            <div class="overview-icon">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="overview-content">
              <h3>AI规则配置</h3>
              <p>{{ getModuleStatus('aiRule') }}</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="overview-card">
          <div class="overview-item">
            <div class="overview-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="overview-content">
              <h3>用户管理</h3>
              <p>{{ getModuleStatus('userManagement') }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 配置详情 -->
    <el-card class="config-detail-card">
      <template #header>
        <div class="card-header">
          <span>配置详情</span>
          <div class="header-actions">
            <el-button type="primary" @click="refreshConfig" :loading="loading" icon="Refresh">
              刷新配置
            </el-button>
            <el-button type="success" @click="syncConfig" icon="Upload">
              同步到应用端
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane
          v-for="(config, key) in configStore.configs"
          :key="key"
          :label="getModuleName(key)"
          :name="key"
        >
          <div class="config-content">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="模块状态">
                <el-tag :type="config ? 'success' : 'danger'">
                  {{ config ? '已配置' : '未配置' }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="最后更新">
                {{ config?.lastSync ? formatTime(config.lastSync) : '未知' }}
              </el-descriptions-item>
              <el-descriptions-item label="数据数量">
                {{ config?.dataCount || 0 }} 条
              </el-descriptions-item>
              <el-descriptions-item label="权限配置">
                {{ config?.permissions ? Object.keys(config.permissions).length : 0 }} 项
              </el-descriptions-item>
            </el-descriptions>

            <div v-if="config" class="config-json">
              <h4>配置详情 (JSON)</h4>
              <el-input
                type="textarea"
                :rows="15"
                :model-value="JSON.stringify(config, null, 2)"
                readonly
                class="json-textarea"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 配置历史 -->
    <el-card class="config-history-card">
      <template #header>
        <span>配置同步历史</span>
      </template>

      <el-table :data="syncHistory" stripe>
        <el-table-column prop="time" label="同步时间" width="180" />
        <el-table-column prop="module" label="模块" width="150" />
        <el-table-column prop="action" label="操作" width="120">
          <template #default="{ row }">
            <el-tag :type="row.action === 'sync' ? 'success' : 'info'" size="small">
              {{ row.action === 'sync' ? '同步' : '刷新' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
              {{ row.status === 'success' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useConfigStore } from '@/stores/config'
import { formatDate } from '@/utils/format'

const configStore = useConfigStore()

// 响应式数据
const loading = ref(false)
const activeTab = ref('aiConversation')

// 同步历史记录
const syncHistory = ref([
  {
    time: new Date().toISOString(),
    module: 'aiConversation',
    action: 'sync',
    status: 'success',
    description: '成功同步AI对话记录配置'
  },
  {
    time: new Date(Date.now() - 300000).toISOString(),
    module: 'dataSource',
    action: 'refresh',
    status: 'success',
    description: '刷新数据源配置'
  }
])

// 获取模块名称
const getModuleName = (key) => {
  const nameMap = {
    aiConversation: 'AI对话记录',
    dataSource: '数据源配置',
    aiRule: 'AI规则配置',
    userManagement: '用户管理'
  }
  return nameMap[key] || key
}

// 获取模块状态
const getModuleStatus = (key) => {
  const config = configStore.configs[key]
  if (!config) return '未配置'
  return `${config.dataCount || 0} 条数据`
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '未知'
  return formatDate(timeStr, 'MM-DD HH:mm:ss')
}

// 刷新配置
const refreshConfig = async () => {
  loading.value = true
  try {
    await configStore.initializeConfig()

    // 添加历史记录
    syncHistory.value.unshift({
      time: new Date().toISOString(),
      module: 'all',
      action: 'refresh',
      status: 'success',
      description: '刷新所有模块配置'
    })

    ElMessage.success('配置刷新成功')
  } catch (error) {
    console.error('刷新配置失败:', error)
    ElMessage.error('刷新配置失败')

    // 添加失败记录
    syncHistory.value.unshift({
      time: new Date().toISOString(),
      module: 'all',
      action: 'refresh',
      status: 'failed',
      description: '刷新配置失败: ' + error.message
    })
  } finally {
    loading.value = false
  }
}

// 同步配置到应用端
const syncConfig = async () => {
  try {
    // 这里可以调用配置推送API
    console.log('同步配置到应用端:', configStore.configs)

    // 添加历史记录
    syncHistory.value.unshift({
      time: new Date().toISOString(),
      module: 'all',
      action: 'sync',
      status: 'success',
      description: '成功同步所有配置到应用端'
    })

    ElMessage.success('配置同步成功')
  } catch (error) {
    console.error('同步配置失败:', error)
    ElMessage.error('同步配置失败')
  }
}

// 组件挂载
onMounted(() => {
  configStore.initializeConfig()
})
</script>

<style lang="scss" scoped>
.system-config-page {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;

  .page-title {
    margin: 0 0 8px 0;
    font-size: 24px;
    font-weight: 500;
    color: #303133;
  }

  .page-description {
    margin: 0;
    font-size: 14px;
    color: #606266;
    line-height: 1.5;
  }
}

.overview-card {
  margin-bottom: 20px;

  .overview-item {
    display: flex;
    align-items: center;
    gap: 16px;

    .overview-icon {
      width: 48px;
      height: 48px;
      border-radius: 8px;
      background: linear-gradient(135deg, #409EFF, #66b1ff);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 24px;
    }

    .overview-content {
      h3 {
        margin: 0 0 4px 0;
        font-size: 16px;
        color: #303133;
      }

      p {
        margin: 0;
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.config-detail-card,
.config-history-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.config-content {
  .config-json {
    margin-top: 24px;

    h4 {
      margin: 0 0 12px 0;
      color: #303133;
    }

    .json-textarea {
      font-family: 'Courier New', monospace;
      font-size: 12px;
    }
  }
}

@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .overview-item {
    flex-direction: column;
    text-align: center;
  }
}
</style>
