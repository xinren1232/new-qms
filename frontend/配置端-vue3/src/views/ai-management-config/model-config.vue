<template>
  <div class="model-config">
    <div class="page-header">
      <h2>AI模型配置管理</h2>
      <p class="text-muted">管理和配置可用的AI模型，支持多种模型提供商</p>
    </div>

    <!-- 当前模型状态 -->
    <div class="current-model-card">
      <el-card class="model-status-card">
        <template #header>
          <div class="card-header">
            <span>当前活跃模型</span>
            <el-button type="primary" size="small" @click="refreshModels">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>
        <div v-if="currentModel" class="model-info">
          <div class="model-name">{{ currentModel.name }}</div>
          <div class="model-details">
            <el-tag type="info">{{ currentModel.provider }}</el-tag>
            <el-tag type="success">{{ currentModel.contextLength }}</el-tag>
          </div>
          <div class="model-features">
            <el-tag v-if="currentModel.features.multimodal" type="success" size="small">
              多模态
            </el-tag>
            <el-tag v-if="currentModel.features.vision" type="primary" size="small">
              视觉识别
            </el-tag>
            <el-tag v-if="currentModel.features.toolCalls" type="warning" size="small">
              工具调用
            </el-tag>
            <el-tag v-if="currentModel.features.streaming" type="info" size="small">
              流式响应
            </el-tag>
            <el-tag v-if="currentModel.features.reasoning" type="danger" size="small">
              推理能力
            </el-tag>
            <el-tag v-if="currentModel.features.external" type="info" size="small">
              外网可用
            </el-tag>
            <!-- 显示自定义标签 -->
            <el-tag
              v-for="tag in currentModel.tags"
              :key="tag"
              type="info"
              size="small"
              effect="plain"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 模型列表 -->
    <div class="models-list">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>可用模型列表</span>
            <div class="header-actions">
              <el-input
                v-model="searchText"
                placeholder="搜索模型..."
                size="small"
                style="width: 200px;"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <el-button type="primary" @click="handleAddModel">
                <el-icon><Plus /></el-icon>
                添加模型
              </el-button>
            </div>
          </div>
        </template>

        <!-- 模型表格 -->
        <vxe-table
          ref="modelTableRef"
          :data="filteredModels"
          :loading="loading"
          border
          stripe
          resizable
          height="500"
        >
          <vxe-column type="seq" width="60" title="序号" />
          <vxe-column field="name" title="模型名称" min-width="150">
            <template #default="{ row }">
              <div class="model-name-cell">
                <el-avatar :size="32" :src="row.avatar" />
                <span class="model-name">{{ row.name }}</span>
              </div>
            </template>
          </vxe-column>
          <vxe-column field="provider" title="提供商" width="120">
            <template #default="{ row }">
              <el-tag :type="getProviderTagType(row.provider)">
                {{ row.provider }}
              </el-tag>
            </template>
          </vxe-column>
          <vxe-column field="contextLength" title="上下文长度" width="120" />
          <vxe-column field="features" title="特性" min-width="250">
            <template #default="{ row }">
              <div class="features-tags">
                <el-tag v-if="row.features.multimodal" size="small" type="success">多模态</el-tag>
                <el-tag v-if="row.features.vision" size="small" type="primary">视觉</el-tag>
                <el-tag v-if="row.features.toolCalls" size="small" type="warning">工具</el-tag>
                <el-tag v-if="row.features.reasoning" size="small" type="danger">推理</el-tag>
                <el-tag v-if="row.features.external" size="small" type="info">外网可用</el-tag>
                <!-- 显示自定义标签 -->
                <el-tag
                  v-for="tag in row.tags"
                  :key="tag"
                  size="small"
                  type="info"
                  effect="plain"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </template>
          </vxe-column>
          <vxe-column field="status" title="状态" width="100">
            <template #default="{ row }">
              <el-switch
                v-model="row.enabled"
                @change="handleStatusChange(row)"
              />
            </template>
          </vxe-column>
          <vxe-column title="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleSetDefault(row)">
                设为默认
              </el-button>
              <el-button size="small" @click="handleConfig(row)">
                配置
              </el-button>
              <el-button type="danger" size="small" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </vxe-column>
        </vxe-table>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Search, Plus } from '@element-plus/icons-vue'

// 响应式数据
const modelTableRef = ref()
const loading = ref(false)
const searchText = ref('')

const currentModel = ref(null)
const currentModelId = ref('')
const models = ref([])

// 计算属性
const filteredModels = computed(() => {
  if (!searchText.value) return models.value
  return models.value.filter(model =>
    model.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
    model.provider.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

// 获取模型列表
const fetchModels = async () => {
  try {
    loading.value = true
    const response = await fetch('http://localhost:3004/api/models')
    const data = await response.json()

    if (data.success) {
      models.value = data.data.models.map(model => ({
        ...model,
        enabled: true,
        avatar: `/avatars/${model.provider.toLowerCase()}.png`
      }))
      currentModelId.value = data.data.current_model
      currentModel.value = models.value.find(m => m.id === currentModelId.value) || models.value[0]
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
    ElMessage.error('获取模型列表失败')
  } finally {
    loading.value = false
  }
}

// 方法
const getProviderTagType = (provider: string) => {
  const typeMap: Record<string, string> = {
    'OpenAI': 'success',
    'DeepSeek': 'primary',
    'Anthropic': 'warning',
    'Google': 'info'
  }
  return typeMap[provider] || 'default'
}

const refreshModels = () => {
  fetchModels()
  ElMessage.success('模型列表已刷新')
}

const handleAddModel = () => {
  ElMessage.info('添加模型功能开发中...')
}

const handleSetDefault = (row: any) => {
  ElMessageBox.confirm(
    `确定要将 "${row.name}" 设置为默认模型吗？`,
    '确认操作',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    currentModel.value = { ...row }
    ElMessage.success('默认模型设置成功')
  })
}

const handleConfig = (row: any) => {
  ElMessage.info(`配置模型: ${row.name}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除模型 "${row.name}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = models.value.findIndex(m => m.id === row.id)
    if (index > -1) {
      models.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}

const handleStatusChange = (row: any) => {
  const status = row.enabled ? '启用' : '禁用'
  ElMessage.success(`${status}模型成功`)
}

// 生命周期
onMounted(() => {
  refreshModels()
})
</script>

<style scoped>
.model-config {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.text-muted {
  color: #909399;
  margin: 0;
}

.current-model-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.model-info {
  text-align: center;
}

.model-name {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 10px;
}

.model-details {
  margin-bottom: 10px;
}

.model-details .el-tag {
  margin: 0 5px;
}

.model-features .el-tag {
  margin: 2px;
}

.model-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.features-tags .el-tag {
  margin: 2px;
}
</style>
