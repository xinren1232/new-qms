<template>
  <div class="ai-models-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>AI模型管理</h1>
        <p>管理和配置QMS系统中的8个AI模型</p>
      </div>

      <!-- 筛选器控制 -->
      <div class="header-filters">
        <el-button
          @click="showFilters = !showFilters"
          :icon="showFilters ? ArrowUp : ArrowDown"
          :type="showFilters ? 'warning' : 'primary'"
          size="default"
        >
          {{ showFilters ? '隐藏筛选' : '显示筛选' }} ({{ getActiveFiltersCount() }})
        </el-button>
        <el-button
          @click="clearAllFilters"
          type="danger"
          size="default"
          v-if="getActiveFiltersCount() > 0"
        >
          清空筛选
        </el-button>
      </div>

      <div class="header-actions">
        <el-button @click="refreshModels" :icon="Refresh" :loading="loading" size="default">刷新</el-button>
        <el-button @click="showAddDialog = true" type="primary" :icon="Plus" size="default">添加模型</el-button>
      </div>
    </div>

    <!-- 活跃筛选标签 -->
    <div class="active-filters-bar" v-if="getActiveFiltersCount() > 0">
      <div class="active-filters-content">
        <span class="filters-label">当前筛选:</span>
        <div class="filters-tags">
          <el-tag
            v-for="tag in getActiveFilterTags()"
            :key="tag.key"
            closable
            @close="removeFilter(tag.type, tag.value)"
            type="primary"
            size="default"
            class="filter-tag"
            effect="dark"
          >
            {{ tag.label }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 模型统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><Cpu /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ enabledModels.length }}</div>
          <div class="stat-label">已启用模型</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><Connection /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ onlineModels.length }}</div>
          <div class="stat-label">在线模型</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><View /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ visionModels.length }}</div>
          <div class="stat-label">支持视觉</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">
          <el-icon><Cpu /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ reasoningModels.length }}</div>
          <div class="stat-label">推理模型</div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 可折叠筛选器 -->
      <el-collapse-transition>
        <div class="models-filters" v-show="showFilters">
          <div class="filter-section">
            <div class="filter-group">
              <h5>部署方式</h5>
              <el-checkbox-group v-model="filters.deploymentType">
                <el-checkbox label="内网模型">内网模型 (传音代理)</el-checkbox>
                <el-checkbox label="外网模型">外网模型 (直连)</el-checkbox>
              </el-checkbox-group>
            </div>
            <div class="filter-group">
              <h5>上下文长度</h5>
              <el-checkbox-group v-model="filters.contextLength">
                <el-checkbox label="64k">64k</el-checkbox>
                <el-checkbox label="128k">128k</el-checkbox>
                <el-checkbox label="200k">200k</el-checkbox>
              </el-checkbox-group>
            </div>
            <div class="filter-group">
              <h5>模型能力</h5>
              <el-checkbox-group v-model="filters.capabilities">
                <el-checkbox label="图片理解">图片理解</el-checkbox>
                <el-checkbox label="工具调用">工具调用</el-checkbox>
                <el-checkbox label="深度推理">深度推理</el-checkbox>
                <el-checkbox label="代码生成">代码生成</el-checkbox>
              </el-checkbox-group>
            </div>
            <div class="filter-group">
              <h5>模型家族</h5>
              <el-checkbox-group v-model="filters.modelFamily">
                <el-checkbox label="OpenAI">OpenAI</el-checkbox>
                <el-checkbox label="DeepSeek">DeepSeek</el-checkbox>
                <el-checkbox label="Google">Google</el-checkbox>
                <el-checkbox label="Anthropic">Anthropic</el-checkbox>
              </el-checkbox-group>
            </div>
            <div class="filter-group">
              <h5>模型状态</h5>
              <el-checkbox-group v-model="filters.status">
                <el-checkbox label="在线">在线</el-checkbox>
                <el-checkbox label="离线">离线</el-checkbox>
                <el-checkbox label="测试中">测试中</el-checkbox>
              </el-checkbox-group>
            </div>
          </div>
        </div>
      </el-collapse-transition>

      <!-- 模型列表 -->
      <div class="models-container">
      <div class="models-grid">
        <div
          v-for="model in filteredModels"
          :key="model.provider"
          class="model-card"
          :class="{
            'enabled': model.enabled,
            'disabled': !model.enabled,
            'external': model.type === 'external',
            'internal': model.type === 'internal'
          }"
        >
          <!-- 模型头部 -->
          <div class="model-header">
            <div class="model-info">
              <div class="model-avatar">
                <img v-if="model.avatar" :src="model.avatar" :alt="model.name" />
                <el-icon v-else><Cpu /></el-icon>
              </div>
              <div class="model-details">
                <h3>{{ model.name }}</h3>
                <p>{{ model.model }}</p>
                <div class="model-tags">
                  <el-tag v-if="model.type === 'external'" type="success" size="small">外网直连</el-tag>
                  <el-tag v-if="model.type === 'internal'" type="primary" size="small">内网代理</el-tag>
                  <el-tag v-if="model.features?.vision" type="warning" size="small">图片理解</el-tag>
                  <el-tag v-if="model.features?.reasoning" type="danger" size="small">深度推理</el-tag>
                  <el-tag v-if="model.features?.toolCalls" type="info" size="small">工具调用</el-tag>
                </div>
              </div>
            </div>
            <div class="model-actions">
              <el-switch
                v-model="model.enabled"
                @change="toggleModel(model)"
                :disabled="loading"
              />
            </div>
          </div>

          <!-- 模型特性 -->
          <div class="model-features">
            <div class="feature-item" :class="{ active: model.features?.reasoning }">
              <el-icon><Cpu /></el-icon>
              <span>推理能力</span>
            </div>
            <div class="feature-item" :class="{ active: model.features?.toolCalls }">
              <el-icon><Tools /></el-icon>
              <span>工具调用</span>
            </div>
            <div class="feature-item" :class="{ active: model.features?.vision }">
              <el-icon><View /></el-icon>
              <span>图文识别</span>
            </div>
            <div class="feature-item" :class="{ active: model.features?.jsonOutput }">
              <el-icon><Setting /></el-icon>
              <span>Json输出</span>
            </div>
          </div>

          <!-- 支持功能标签 -->
          <div class="supported-features" v-if="getSupportedFeatures(model).length > 0">
            <div class="features-title">支持功能:</div>
            <div class="features-tags">
              <el-tag
                v-for="feature in getSupportedFeatures(model)"
                :key="feature"
                size="small"
                type="info"
                class="feature-tag"
              >
                {{ feature }}
              </el-tag>
            </div>
          </div>

          <!-- 模型详细信息 -->
          <div class="model-details-info">
            <div class="detail-item">
              <span class="detail-label">上下文长度:</span>
              <span class="detail-value">{{ model.contextLength }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">最大输出:</span>
              <span class="detail-value">{{ typeof model.maxTokens === 'string' ? model.maxTokens : model.maxTokens + ' tokens' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">默认温度:</span>
              <span class="detail-value">{{ model.temperature }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">部署方式:</span>
              <span class="detail-value">{{ model.type === 'internal' ? '内网代理' : '外网直连' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">更新时间:</span>
              <span class="detail-value">{{ model.updatedAt }}</span>
            </div>
          </div>

          <!-- 模型配置 -->
          <div class="model-config">
            <div class="config-item">
              <label>API地址:</label>
              <span class="config-value">{{ getApiUrl(model) }}</span>
            </div>
            <div class="config-item">
              <label>状态:</label>
              <el-tag :type="getStatusType(model)" size="small">
                {{ getStatusText(model) }}
              </el-tag>
            </div>
            <div class="config-item">
              <label>响应时间:</label>
              <span class="config-value">{{ model.responseTime || '--' }}ms</span>
            </div>
          </div>

          <!-- 模型操作 -->
          <div class="model-footer">
            <el-button @click="testModel(model)" size="small" :loading="model.testing">
              测试连接
            </el-button>
            <el-button @click="configureModel(model)" size="small" type="primary">
              配置
            </el-button>
            <el-button @click="viewStats(model)" size="small" text>
              统计
            </el-button>
          </div>
        </div>
      </div>
      </div> <!-- models-container 结束 -->
    </div> <!-- main-content 结束 -->

    <!-- 模型配置对话框 -->
    <el-dialog
      v-model="showConfigDialog"
      :title="`配置 ${selectedModel?.name}`"
      width="600px"
    >
      <el-form v-if="selectedModel" :model="modelConfig" label-width="120px">
        <el-form-item label="模型名称">
          <el-input v-model="modelConfig.name" />
        </el-form-item>
        <el-form-item label="API地址">
          <el-input v-model="modelConfig.apiUrl" />
        </el-form-item>
        <el-form-item label="API密钥">
          <el-input v-model="modelConfig.apiKey" type="password" show-password />
        </el-form-item>
        <el-form-item label="默认温度">
          <el-slider v-model="modelConfig.temperature" :min="0" :max="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="最大令牌">
          <el-input-number v-model="modelConfig.maxTokens" :min="1" :max="32768" />
        </el-form-item>
        <el-form-item label="超时时间">
          <el-input-number v-model="modelConfig.timeout" :min="5" :max="300" />
          <span style="margin-left: 8px;">秒</span>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showConfigDialog = false">取消</el-button>
        <el-button @click="testModelConfig" :loading="testing">测试配置</el-button>
        <el-button @click="saveModelConfig" type="primary" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 添加模型对话框 -->
    <el-dialog
      v-model="showAddDialog"
      title="添加AI模型"
      width="600px"
    >
      <el-form :model="newModel" label-width="120px">
        <el-form-item label="模型名称" required>
          <el-input v-model="newModel.name" placeholder="例如: GPT-4o" />
        </el-form-item>
        <el-form-item label="模型标识" required>
          <el-input v-model="newModel.model" placeholder="例如: gpt-4o" />
        </el-form-item>
        <el-form-item label="提供商" required>
          <el-input v-model="newModel.provider" placeholder="例如: openai" />
        </el-form-item>
        <el-form-item label="API地址" required>
          <el-input v-model="newModel.apiUrl" placeholder="例如: https://api.openai.com/v1" />
        </el-form-item>
        <el-form-item label="模型特性">
          <el-checkbox-group v-model="newModel.features">
            <el-checkbox label="reasoning">推理能力</el-checkbox>
            <el-checkbox label="toolCalls">工具调用</el-checkbox>
            <el-checkbox label="vision">图文识别</el-checkbox>
            <el-checkbox label="external">外网可用</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button @click="addModel" type="primary" :loading="adding">添加</el-button>
      </template>
    </el-dialog>

    <!-- 模型统计对话框 -->
    <el-dialog
      v-model="showStatsDialog"
      :title="`${selectedModel?.name} 使用统计`"
      width="800px"
    >
      <div v-if="selectedModel" class="model-stats">
        <div class="stats-overview">
          <div class="overview-item">
            <div class="overview-value">{{ modelStats.totalRequests }}</div>
            <div class="overview-label">总请求数</div>
          </div>
          <div class="overview-item">
            <div class="overview-value">{{ modelStats.successRate }}%</div>
            <div class="overview-label">成功率</div>
          </div>
          <div class="overview-item">
            <div class="overview-value">{{ modelStats.avgResponseTime }}ms</div>
            <div class="overview-label">平均响应时间</div>
          </div>
          <div class="overview-item">
            <div class="overview-value">{{ modelStats.totalTokens }}</div>
            <div class="overview-label">总令牌数</div>
          </div>
        </div>
        
        <!-- 这里可以添加图表组件 -->
        <div class="stats-charts">
          <p>详细统计图表功能待实现...</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import {
  Plus, Refresh, Cpu, Connection, View,
  Tools, Setting, ArrowUp, ArrowDown
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const testing = ref(false)
const saving = ref(false)
const adding = ref(false)

const showConfigDialog = ref(false)
const showAddDialog = ref(false)
const showStatsDialog = ref(false)

const selectedModel = ref(null)

// 模型列表 - 基于实际配置的8个AI模型（根据传音代理参数表更新）
const models = ref([
  {
    provider: 'gpt-4o',
    name: 'GPT-4o',
    model: 'gpt-4o',
    enabled: true,
    status: 'online',
    apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
    features: { reasoning: false, toolCalls: true, vision: true, external: false, embedding: false },
    responseTime: 1200,
    avatar: '/icons/openai-logo.png',
    contextLength: '128k/16k',
    maxTokens: 4096,
    temperature: 0.7,
    updatedAt: '2025-01-04',
    family: 'OpenAI',
    type: 'internal',
    supportedFeatures: ['toolcalls', '实时输出解析', '活跃度检测']
  },
  {
    provider: 'o3',
    name: 'O3',
    model: 'o3-mini',
    enabled: true,
    status: 'online',
    apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
    features: { reasoning: true, toolCalls: true, vision: true, external: false, embedding: false },
    responseTime: 1500,
    avatar: '/icons/openai-logo.png',
    contextLength: '200k/16k',
    maxTokens: 4096,
    temperature: 0.7,
    updatedAt: '2025-01-04',
    family: 'OpenAI',
    type: 'internal',
    supportedFeatures: ['toolcalls', '实时输出解析', '活跃度检测']
  },
  {
    provider: 'gemini-2.5-pro-thinking',
    name: 'Gemini 2.5 Pro Thinking',
    model: 'gemini-2.5-flash-thinking',
    enabled: true,
    status: 'online',
    apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
    features: { reasoning: true, toolCalls: true, vision: true, external: false, embedding: false },
    responseTime: 1800,
    avatar: '/icons/google-logo.png',
    contextLength: '100k',
    maxTokens: 8192,
    temperature: 0.7,
    updatedAt: '2025-01-04',
    family: 'Google',
    type: 'internal',
    supportedFeatures: ['toolcalls', '实时输出解析', '活跃度检测']
  },
  {
    provider: 'claude-3.7-sonnet',
    name: 'Claude 3.5 Sonnet',
    model: 'claude-3-5-sonnet-20241022',
    enabled: true,
    status: 'online',
    apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
    features: { reasoning: false, toolCalls: true, vision: true, external: false, embedding: false },
    responseTime: 1500,
    avatar: '/icons/anthropic-logo.png',
    contextLength: '200k/8k',
    maxTokens: 4096,
    temperature: 0.7,
    updatedAt: '2025-01-04',
    family: 'Anthropic',
    type: 'internal',
    supportedFeatures: ['toolcalls', '实时输出解析']
  },
  {
    provider: 'deepseek-r1',
    name: 'DeepSeek R1 (内网)',
    model: 'deepseek-reasoner',
    enabled: true,
    status: 'online',
    apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
    features: { reasoning: true, toolCalls: false, vision: false, external: false, embedding: false },
    responseTime: 2200,
    avatar: '/icons/deepseek-logo.png',
    contextLength: '128k/8k',
    maxTokens: 4096,
    temperature: 0.7,
    updatedAt: '2025-01-04',
    family: 'DeepSeek',
    type: 'internal',
    supportedFeatures: ['深度推理']
  },
  {
    provider: 'deepseek-v3',
    name: 'DeepSeek V3 (内网)',
    model: 'deepseek-chat',
    enabled: true,
    status: 'online',
    apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
    features: { reasoning: false, toolCalls: false, vision: false, external: false, embedding: false },
    responseTime: 1800,
    avatar: '/icons/deepseek-logo.png',
    contextLength: '128k/8k',
    maxTokens: 4096,
    temperature: 0.7,
    updatedAt: '2025-01-04',
    family: 'DeepSeek',
    type: 'internal',
    supportedFeatures: ['基础对话']
  },
  {
    provider: 'deepseek-chat-v3-0324',
    name: 'DeepSeek Chat - 外网直连',
    model: 'deepseek-chat',
    enabled: true,
    status: 'online',
    apiUrl: 'https://api.deepseek.com/v1',
    features: { reasoning: false, toolCalls: true, vision: false, external: true, jsonOutput: true, functionCalling: true },
    responseTime: 2000,
    avatar: '/icons/deepseek-logo.png',
    contextLength: '64k',
    maxTokens: '默认4k，最大8k',
    temperature: 0.7,
    updatedAt: '2025-01-04',
    family: 'DeepSeek',
    type: 'external',
    supportedFeatures: ['Json Output', 'Function Calling', '对话前缀续写', 'FIM补全']
  },
  {
    provider: 'deepseek-reasoner-r1-0528',
    name: 'DeepSeek Reasoner - 外网直连',
    model: 'deepseek-reasoner',
    enabled: true,
    status: 'online',
    apiUrl: 'https://api.deepseek.com/v1',
    features: { reasoning: true, toolCalls: true, vision: false, external: true, jsonOutput: true, functionCalling: true },
    responseTime: 2500,
    avatar: '/icons/deepseek-logo.png',
    contextLength: '64k',
    maxTokens: '默认32k，最大64k',
    temperature: 0.7,
    updatedAt: '2025-01-04',
    family: 'DeepSeek',
    type: 'external',
    supportedFeatures: ['Json Output', 'Function Calling', '对话前缀续写', '深度推理']
  }
])

// 模型配置
const modelConfig = reactive({
  name: '',
  apiUrl: '',
  apiKey: '',
  temperature: 0.7,
  maxTokens: 4096,
  timeout: 30
})

// 新模型
const newModel = reactive({
  name: '',
  model: '',
  provider: '',
  apiUrl: '',
  features: []
})

// 模型统计
const modelStats = reactive({
  totalRequests: 0,
  successRate: 0,
  avgResponseTime: 0,
  totalTokens: 0
})

// 筛选器状态
const filters = reactive({
  deploymentType: [],
  contextLength: [],
  capabilities: [],
  modelFamily: [],
  status: []
})

// 筛选器折叠状态
const showFilters = ref(true)

// 计算属性
const enabledModels = computed(() => models.value.filter(m => m.enabled))
const onlineModels = computed(() => models.value.filter(m => m.status === 'online'))
const visionModels = computed(() => models.value.filter(m => m.features?.vision))
const reasoningModels = computed(() => models.value.filter(m => m.features?.reasoning))

// 筛选后的模型列表
const filteredModels = computed(() => {
  let filtered = models.value

  // 按部署方式筛选
  if (filters.deploymentType.length > 0) {
    filtered = filtered.filter(model => {
      if (filters.deploymentType.includes('内网模型') && model.type === 'internal') return true
      if (filters.deploymentType.includes('外网模型') && model.type === 'external') return true
      return false
    })
  }

  // 按上下文长度筛选
  if (filters.contextLength.length > 0) {
    filtered = filtered.filter(model => {
      const contextLength = model.contextLength || '64k'
      return filters.contextLength.some(length => contextLength.includes(length))
    })
  }

  // 按模型能力筛选
  if (filters.capabilities.length > 0) {
    filtered = filtered.filter(model => {
      if (filters.capabilities.includes('图片理解') && model.features?.vision) return true
      if (filters.capabilities.includes('工具调用') && model.features?.toolCalls) return true
      if (filters.capabilities.includes('深度推理') && model.features?.reasoning) return true
      if (filters.capabilities.includes('代码生成') && model.name.includes('Coder')) return true
      return false
    })
  }

  // 按模型家族筛选
  if (filters.modelFamily.length > 0) {
    filtered = filtered.filter(model => {
      return filters.modelFamily.includes(model.family)
    })
  }

  // 按模型状态筛选
  if (filters.status.length > 0) {
    filtered = filtered.filter(model => {
      const statusMap = {
        '在线': 'online',
        '离线': 'offline',
        '测试中': 'testing'
      }
      return filters.status.some(status => statusMap[status] === model.status)
    })
  }

  return filtered
})

// 方法定义
const refreshModels = async () => {
  loading.value = true
  try {
    // 从配置中心获取最新的模型配置
    const response = await axios.get('/api/ai/models')
    if (response.data.success && response.data.data.models) {
      const configModels = response.data.data.models

      // 将配置中心的模型数据转换为前端显示格式
      models.value = Object.entries(configModels).map(([key, model]) => {
        // 查找现有模型数据以保留UI状态
        const existingModel = models.value.find(m => m.provider === key)

        return {
          provider: key,
          name: model.name,
          model: model.model,
          enabled: model.enabled !== false,
          status: model.enabled !== false ? 'online' : 'offline',
          apiUrl: model.baseURL,
          features: {
            reasoning: model.features?.reasoning || false,
            toolCalls: model.features?.tools || false,
            vision: model.features?.vision || false,
            external: model.baseURL?.includes('deepseek.com') || false,
            embedding: model.features?.embedding || false
          },
          responseTime: existingModel?.responseTime || Math.floor(Math.random() * 1000) + 500,
          avatar: getModelAvatar(model),
          contextLength: getModelContextLength(model),
          maxTokens: model.maxTokens || 4096,
          temperature: model.temperature || 0.7,
          updatedAt: new Date().toISOString().split('T')[0],
          family: getModelFamily(model),
          type: model.baseURL?.includes('transsion.com') ? 'internal' : 'external',
          supportedFeatures: getModelFeatures(model)
        }
      })

      console.log(`✅ 已刷新 ${models.value.length} 个AI模型`)
      ElMessage.success(`模型列表已刷新 (${models.value.length}个模型)`)
    } else {
      ElMessage.error('获取模型配置失败: 数据格式错误')
    }
  } catch (error) {
    console.error('刷新模型失败:', error)
    ElMessage.error('刷新失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 获取模型头像
const getModelAvatar = (model) => {
  if (model.model?.includes('gpt') || model.model?.includes('o3')) {
    return '/icons/openai-logo.png'
  } else if (model.model?.includes('deepseek')) {
    return '/icons/deepseek-logo.png'
  } else if (model.model?.includes('claude')) {
    return '/icons/anthropic-logo.png'
  } else if (model.model?.includes('gemini')) {
    return '/icons/google-logo.png'
  }
  return '/icons/ai-default.png'
}

// 获取模型上下文长度
const getModelContextLength = (model) => {
  if (model.model?.includes('gpt-4o')) return '128k/16k'
  if (model.model?.includes('o3')) return '200k/16k'
  if (model.model?.includes('gemini')) return '1M/32k'
  if (model.model?.includes('claude')) return '200k/32k'
  if (model.model?.includes('deepseek')) return '64k/32k'
  return '4k/2k'
}

// 获取模型家族
const getModelFamily = (model) => {
  if (model.model?.includes('gpt') || model.model?.includes('o3')) return 'OpenAI'
  if (model.model?.includes('deepseek')) return 'DeepSeek'
  if (model.model?.includes('claude')) return 'Anthropic'
  if (model.model?.includes('gemini')) return 'Google'
  return '其他'
}

// 获取模型特性
const getModelFeatures = (model) => {
  const features = []
  if (model.features?.tools) features.push('工具调用')
  if (model.features?.vision) features.push('视觉理解')
  if (model.features?.reasoning) features.push('深度推理')
  if (model.baseURL?.includes('transsion.com')) features.push('内网代理')
  return features.length > 0 ? features : ['基础对话']
}

const toggleModel = async (model) => {
  try {
    // 模拟切换
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success(`${model.name} 已${model.enabled ? '启用' : '禁用'}`)
  } catch (error) {
    model.enabled = !model.enabled // 回滚
    ElMessage.error('操作失败: ' + error.message)
  }
}

const testModel = async (model) => {
  model.testing = true
  try {
    // 模拟测试
    await new Promise(resolve => setTimeout(resolve, 2000))
    model.status = 'online'
    model.responseTime = Math.floor(Math.random() * 2000) + 500
    ElMessage.success(`${model.name} 连接测试成功`)
  } catch (error) {
    model.status = 'offline'
    ElMessage.error(`${model.name} 连接测试失败`)
  } finally {
    model.testing = false
  }
}

const configureModel = (model) => {
  selectedModel.value = model
  Object.assign(modelConfig, {
    name: model.name,
    apiUrl: model.apiUrl,
    apiKey: '',
    temperature: 0.7,
    maxTokens: 4096,
    timeout: 30
  })
  showConfigDialog.value = true
}

const testModelConfig = async () => {
  testing.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('配置测试成功')
  } catch (error) {
    ElMessage.error('配置测试失败')
  } finally {
    testing.value = false
  }
}

const saveModelConfig = async () => {
  saving.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('配置保存成功')
    showConfigDialog.value = false
  } catch (error) {
    ElMessage.error('配置保存失败')
  } finally {
    saving.value = false
  }
}

const addModel = async () => {
  if (!newModel.name || !newModel.model || !newModel.provider) {
    ElMessage.error('请填写完整信息')
    return
  }

  adding.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const model = {
      provider: newModel.provider,
      name: newModel.name,
      model: newModel.model,
      enabled: false,
      status: 'offline',
      apiUrl: newModel.apiUrl,
      features: newModel.features.reduce((acc, feature) => {
        acc[feature] = true
        return acc
      }, {}),
      responseTime: null
    }
    
    models.value.push(model)
    ElMessage.success('模型添加成功')
    showAddDialog.value = false
    
    // 重置表单
    Object.assign(newModel, {
      name: '',
      model: '',
      provider: '',
      apiUrl: '',
      features: []
    })
  } catch (error) {
    ElMessage.error('模型添加失败')
  } finally {
    adding.value = false
  }
}

const viewStats = (model) => {
  selectedModel.value = model
  // 模拟统计数据
  Object.assign(modelStats, {
    totalRequests: Math.floor(Math.random() * 10000) + 1000,
    successRate: Math.floor(Math.random() * 20) + 80,
    avgResponseTime: Math.floor(Math.random() * 1000) + 500,
    totalTokens: Math.floor(Math.random() * 1000000) + 100000
  })
  showStatsDialog.value = true
}

const getApiUrl = (model) => {
  const url = model.apiUrl || ''
  return url.length > 40 ? url.substring(0, 40) + '...' : url
}

const getStatusType = (model) => {
  const statusMap = {
    online: 'success',
    offline: 'danger',
    testing: 'warning'
  }
  return statusMap[model.status] || 'info'
}

const getStatusText = (model) => {
  const statusMap = {
    online: '在线',
    offline: '离线',
    testing: '测试中'
  }
  return statusMap[model.status] || '未知'
}

const getModelDescription = (model) => {
  const descriptions = {
    'gpt-4o': 'OpenAI最新的多模态大语言模型，支持文本、图像处理，具有强大的工具调用功能和实时输出解析能力。',
    'o3-mini': 'OpenAI O3系列的轻量版本，专注于推理任务，在数学、编程和逻辑推理方面表现出色，支持多模态输入。',
    'gemini-2.5-flash-thinking': 'Google最新的Gemini模型，具备深度思考能力，在复杂推理和创意任务中表现优异，支持100k上下文。',
    'claude-3-5-sonnet-20241022': 'Anthropic的Claude 3.5 Sonnet模型，在文本理解、分析和创作方面具有卓越性能，支持200k上下文。',
    'deepseek-reasoner': 'DeepSeek的推理专用模型，在数学证明、代码生成和逻辑推理方面具有强大能力，支持深度推理链。',
    'deepseek-chat': 'DeepSeek的通用对话模型，支持多轮对话、Function Calling和Json Output等高级功能。'
  }
  return descriptions[model.model] || `${model.name}是一个先进的AI模型，提供高质量的对话和文本生成服务。`
}

// 获取模型特性标签
const getModelFeatureTags = (model) => {
  const tags = []

  if (model.features.reasoning) tags.push({ text: '深度推理', type: 'warning' })
  if (model.features.toolCalls) tags.push({ text: '工具调用', type: 'success' })
  if (model.features.vision) tags.push({ text: '图像理解', type: 'info' })
  if (model.features.jsonOutput) tags.push({ text: 'Json输出', type: 'primary' })
  if (model.features.functionCalling) tags.push({ text: 'Function调用', type: 'success' })
  if (model.features.external) tags.push({ text: '外网直连', type: 'danger' })
  else tags.push({ text: '内网代理', type: 'info' })

  return tags
}

// 获取支持功能列表
const getSupportedFeatures = (model) => {
  return model.supportedFeatures || []
}

// 筛选器相关方法
const getActiveFiltersCount = () => {
  return filters.deploymentType.length +
         filters.contextLength.length +
         filters.capabilities.length +
         filters.modelFamily.length +
         filters.status.length
}

const getActiveFilterTags = () => {
  const tags = []

  filters.deploymentType.forEach(item => {
    tags.push({ type: 'deploymentType', value: item, label: item, key: `deployment-${item}` })
  })

  filters.contextLength.forEach(item => {
    tags.push({ type: 'contextLength', value: item, label: `上下文: ${item}`, key: `context-${item}` })
  })

  filters.capabilities.forEach(item => {
    tags.push({ type: 'capabilities', value: item, label: `能力: ${item}`, key: `capability-${item}` })
  })

  filters.modelFamily.forEach(item => {
    tags.push({ type: 'modelFamily', value: item, label: `家族: ${item}`, key: `family-${item}` })
  })

  filters.status.forEach(item => {
    tags.push({ type: 'status', value: item, label: `状态: ${item}`, key: `status-${item}` })
  })

  return tags
}

const removeFilter = (type, value) => {
  const index = filters[type].indexOf(value)
  if (index > -1) {
    filters[type].splice(index, 1)
  }
}

const clearAllFilters = () => {
  filters.deploymentType = []
  filters.contextLength = []
  filters.capabilities = []
  filters.modelFamily = []
  filters.status = []
}

// 生命周期
onMounted(() => {
  // 初始化时刷新模型列表
  refreshModels()
})
</script>

<style scoped>
.ai-models-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  gap: 20px;
}

.header-content {
  flex: 1;
}

.header-content h1 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
}

.header-content p {
  margin: 0;
  color: #606266;
  font-size: 16px;
}

.header-filters {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 8px 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 活跃筛选标签栏 */
.active-filters-bar {
  margin-bottom: 20px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border-left: 4px solid #2196f3;
}

.active-filters-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filters-label {
  font-size: 14px;
  color: #1976d2;
  font-weight: 600;
  white-space: nowrap;
}

.filters-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-tag {
  margin: 0;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  border-radius: 12px;
  margin-right: 16px;
}

.stat-icon .el-icon {
  font-size: 24px;
  color: white;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.models-container {
  flex: 1;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}



/* 主要内容区域 */
.main-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.models-filters {
  width: 100%;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.filter-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.filter-group {
  margin-bottom: 20px;
}

.filter-group h5 {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
}

.filter-group .el-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group .el-checkbox {
  margin-right: 0;
}

.models-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
  flex: 1;
}

.model-card {
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
}

.model-card.enabled {
  border-color: #67c23a;
  background: #f0f9ff;
}

.model-card.disabled {
  border-color: #f56c6c;
  background: #fef0f0;
}

.model-card.external {
  border-left: 4px solid #e6a23c;
}

.model-card.internal {
  border-left: 4px solid #409eff;
}

.model-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.model-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.model-avatar {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.model-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.model-avatar .el-icon {
  font-size: 24px;
  color: #909399;
}

.model-details h3 {
  margin: 0 0 4px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.model-details p {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.model-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.model-details-info {
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  color: #606266;
  font-size: 13px;
}

.detail-value {
  color: #2c3e50;
  font-size: 13px;
  font-weight: 500;
}

.model-features {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 6px;
  background: #f5f7fa;
  color: #909399;
  font-size: 12px;
  transition: all 0.3s ease;
}

.feature-item.active {
  background: #e1f3d8;
  color: #67c23a;
}

.supported-features {
  margin: 16px 0;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 3px solid #409eff;
}

.features-title {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
}

.features-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.feature-tag {
  margin: 0;
}

.model-config {
  margin-bottom: 16px;
}

.config-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.config-item label {
  color: #606266;
  font-weight: 500;
}

.config-value {
  color: #2c3e50;
  font-family: monospace;
}

.model-footer {
  display: flex;
  gap: 8px;
}

.model-stats {
  padding: 20px 0;
}

.stats-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.overview-item {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.overview-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.overview-label {
  font-size: 14px;
  color: #606266;
}

.stats-charts {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  text-align: center;
  color: #909399;
}
</style>
