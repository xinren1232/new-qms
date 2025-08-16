<template>
  <div class="mcp-integration">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <img :src="mcpLogo" alt="MCP" />
        </div>
        <div class="header-text">
          <h1>MCP服务连接器</h1>
          <p>Model Context Protocol - 连接各种AI工具和服务的标准协议</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="refreshServices" :icon="Refresh" :loading="loading">刷新</el-button>
        <el-button @click="showAddServiceDialog = true" type="primary" :icon="Plus">添加服务</el-button>
      </div>
    </div>

    <!-- 服务概览 -->
    <div class="services-overview">
      <div class="overview-card">
        <div class="overview-icon">
          <el-icon><Connection /></el-icon>
        </div>
        <div class="overview-content">
          <div class="overview-value">{{ connectedServices.length }}</div>
          <div class="overview-label">已连接服务</div>
        </div>
      </div>
      <div class="overview-card">
        <div class="overview-icon">
          <el-icon><Tools /></el-icon>
        </div>
        <div class="overview-content">
          <div class="overview-value">{{ totalTools }}</div>
          <div class="overview-label">可用工具</div>
        </div>
      </div>
      <div class="overview-card">
        <div class="overview-icon">
          <el-icon><DataBoard /></el-icon>
        </div>
        <div class="overview-content">
          <div class="overview-value">{{ totalResources }}</div>
          <div class="overview-label">资源数量</div>
        </div>
      </div>
      <div class="overview-card">
        <div class="overview-icon">
          <el-icon><Timer /></el-icon>
        </div>
        <div class="overview-content">
          <div class="overview-value">{{ avgResponseTime }}ms</div>
          <div class="overview-label">平均响应</div>
        </div>
      </div>
    </div>

    <!-- 主要内容 -->
    <div class="main-content">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 服务管理 -->
        <el-tab-pane label="服务管理" name="services">
          <div class="services-section">
            <div class="services-grid">
              <div
                v-for="service in mcpServices"
                :key="service.id"
                class="service-card"
                :class="{ 
                  'connected': service.status === 'connected',
                  'disconnected': service.status === 'disconnected',
                  'error': service.status === 'error'
                }"
              >
                <div class="service-header">
                  <div class="service-info">
                    <div class="service-icon">
                      <img v-if="service.icon" :src="service.icon" :alt="service.name" />
                      <el-icon v-else><Service /></el-icon>
                    </div>
                    <div class="service-details">
                      <h3>{{ service.name }}</h3>
                      <p>{{ service.description }}</p>
                      <el-tag :type="getStatusType(service.status)" size="small">
                        {{ getStatusText(service.status) }}
                      </el-tag>
                    </div>
                  </div>
                  <div class="service-actions">
                    <el-switch
                      v-model="service.enabled"
                      @change="toggleService(service)"
                      :disabled="loading"
                    />
                  </div>
                </div>

                <div class="service-stats">
                  <div class="stat-item">
                    <span class="stat-label">工具:</span>
                    <span class="stat-value">{{ service.tools?.length || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">资源:</span>
                    <span class="stat-value">{{ service.resources?.length || 0 }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">延迟:</span>
                    <span class="stat-value">{{ service.latency || '--' }}ms</span>
                  </div>
                </div>

                <div class="service-capabilities">
                  <div class="capability-item" :class="{ active: service.capabilities?.tools }">
                    <el-icon><Tools /></el-icon>
                    <span>工具调用</span>
                  </div>
                  <div class="capability-item" :class="{ active: service.capabilities?.resources }">
                    <el-icon><DataBoard /></el-icon>
                    <span>资源访问</span>
                  </div>
                  <div class="capability-item" :class="{ active: service.capabilities?.prompts }">
                    <el-icon><ChatDotRound /></el-icon>
                    <span>提示模板</span>
                  </div>
                </div>

                <div class="service-footer">
                  <el-button @click="testService(service)" size="small" :loading="service.testing">
                    测试连接
                  </el-button>
                  <el-button @click="configureService(service)" size="small" type="primary">
                    配置
                  </el-button>
                  <el-button @click="viewServiceDetails(service)" size="small" text>
                    详情
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 工具调用 -->
        <el-tab-pane label="工具调用" name="tools">
          <div class="tools-section">
            <div class="section-header">
              <h3>可用工具</h3>
              <el-input
                v-model="toolSearchQuery"
                placeholder="搜索工具..."
                :prefix-icon="Search"
                style="width: 300px"
              />
            </div>

            <div class="tools-grid">
              <div
                v-for="tool in filteredTools"
                :key="`${tool.serviceId}-${tool.name}`"
                class="tool-card"
              >
                <div class="tool-header">
                  <div class="tool-info">
                    <h4>{{ tool.name }}</h4>
                    <p>{{ tool.description }}</p>
                    <el-tag size="small">{{ tool.serviceName }}</el-tag>
                  </div>
                </div>

                <div class="tool-schema">
                  <h5>参数:</h5>
                  <div class="schema-properties">
                    <div
                      v-for="(prop, key) in tool.inputSchema?.properties"
                      :key="key"
                      class="property-item"
                    >
                      <span class="property-name">{{ key }}</span>
                      <span class="property-type">{{ prop.type }}</span>
                      <span v-if="tool.inputSchema?.required?.includes(key)" class="property-required">*</span>
                    </div>
                  </div>
                </div>

                <div class="tool-actions">
                  <el-button @click="callTool(tool)" size="small" type="primary">
                    调用工具
                  </el-button>
                  <el-button @click="viewToolSchema(tool)" size="small" text>
                    查看Schema
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 资源浏览 -->
        <el-tab-pane label="资源浏览" name="resources">
          <div class="resources-section">
            <div class="section-header">
              <h3>可用资源</h3>
              <el-select v-model="selectedServiceFilter" placeholder="筛选服务" style="width: 200px">
                <el-option label="全部服务" value="" />
                <el-option
                  v-for="service in connectedServices"
                  :key="service.id"
                  :label="service.name"
                  :value="service.id"
                />
              </el-select>
            </div>

            <div class="resources-tree">
              <el-tree
                :data="filteredResources"
                :props="{ children: 'children', label: 'name' }"
                node-key="uri"
                :expand-on-click-node="false"
                @node-click="handleResourceClick"
              >
                <template #default="{ node, data }">
                  <div class="resource-node">
                    <el-icon>
                      <Folder v-if="data.type === 'directory'" />
                      <Document v-else-if="data.mimeType?.includes('text')" />
                      <Picture v-else-if="data.mimeType?.includes('image')" />
                      <VideoPlay v-else-if="data.mimeType?.includes('video')" />
                      <Files v-else />
                    </el-icon>
                    <span class="resource-name">{{ data.name }}</span>
                    <span class="resource-size" v-if="data.size">{{ formatSize(data.size) }}</span>
                  </div>
                </template>
              </el-tree>
            </div>
          </div>
        </el-tab-pane>

        <!-- 提示模板 -->
        <el-tab-pane label="提示模板" name="prompts">
          <div class="prompts-section">
            <div class="section-header">
              <h3>提示模板</h3>
              <el-button @click="createPrompt" type="primary" :icon="Plus">创建模板</el-button>
            </div>

            <div class="prompts-grid">
              <div
                v-for="prompt in availablePrompts"
                :key="`${prompt.serviceId}-${prompt.name}`"
                class="prompt-card"
              >
                <div class="prompt-header">
                  <h4>{{ prompt.name }}</h4>
                  <el-tag size="small">{{ prompt.serviceName }}</el-tag>
                </div>
                
                <div class="prompt-description">
                  <p>{{ prompt.description }}</p>
                </div>

                <div class="prompt-arguments" v-if="prompt.arguments?.length">
                  <h5>参数:</h5>
                  <div class="arguments-list">
                    <el-tag
                      v-for="arg in prompt.arguments"
                      :key="arg.name"
                      size="small"
                      :type="arg.required ? 'primary' : 'info'"
                    >
                      {{ arg.name }}
                    </el-tag>
                  </div>
                </div>

                <div class="prompt-actions">
                  <el-button @click="usePrompt(prompt)" size="small" type="primary">
                    使用模板
                  </el-button>
                  <el-button @click="previewPrompt(prompt)" size="small" text>
                    预览
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 添加服务对话框 -->
    <el-dialog v-model="showAddServiceDialog" title="添加MCP服务" width="600px">
      <el-form :model="newService" label-width="120px">
        <el-form-item label="服务名称" required>
          <el-input v-model="newService.name" placeholder="输入服务名称" />
        </el-form-item>
        <el-form-item label="服务类型" required>
          <el-select v-model="newService.type" placeholder="选择服务类型">
            <el-option label="文件系统" value="filesystem" />
            <el-option label="数据库" value="database" />
            <el-option label="API服务" value="api" />
            <el-option label="Git仓库" value="git" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="连接地址" required>
          <el-input v-model="newService.endpoint" placeholder="例如: stdio://path/to/server" />
        </el-form-item>
        <el-form-item label="认证方式">
          <el-select v-model="newService.authType" placeholder="选择认证方式">
            <el-option label="无认证" value="none" />
            <el-option label="API密钥" value="api_key" />
            <el-option label="OAuth2" value="oauth2" />
            <el-option label="基础认证" value="basic" />
          </el-select>
        </el-form-item>
        <el-form-item label="认证信息" v-if="newService.authType !== 'none'">
          <el-input
            v-model="newService.authCredentials"
            type="password"
            placeholder="输入认证信息"
            show-password
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="newService.description"
            type="textarea"
            :rows="3"
            placeholder="输入服务描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddServiceDialog = false">取消</el-button>
        <el-button @click="testNewService" :loading="testingNewService">测试连接</el-button>
        <el-button @click="addService" type="primary" :loading="addingService">添加</el-button>
      </template>
    </el-dialog>

    <!-- 工具调用对话框 -->
    <el-dialog v-model="showToolCallDialog" :title="`调用工具: ${selectedTool?.name}`" width="700px">
      <div v-if="selectedTool" class="tool-call-form">
        <el-form :model="toolCallParams" label-width="120px">
          <el-form-item
            v-for="(prop, key) in selectedTool.inputSchema?.properties"
            :key="key"
            :label="key"
            :required="selectedTool.inputSchema?.required?.includes(key)"
          >
            <el-input
              v-if="prop.type === 'string'"
              v-model="toolCallParams[key]"
              :placeholder="prop.description"
            />
            <el-input-number
              v-else-if="prop.type === 'number'"
              v-model="toolCallParams[key]"
              :placeholder="prop.description"
            />
            <el-switch
              v-else-if="prop.type === 'boolean'"
              v-model="toolCallParams[key]"
            />
            <el-input
              v-else
              v-model="toolCallParams[key]"
              :placeholder="prop.description"
            />
          </el-form-item>
        </el-form>

        <div class="tool-call-result" v-if="toolCallResult">
          <h4>执行结果:</h4>
          <pre>{{ JSON.stringify(toolCallResult, null, 2) }}</pre>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showToolCallDialog = false">取消</el-button>
        <el-button @click="executeToolCall" type="primary" :loading="callingTool">
          执行
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import mcpLogo from '@/../public/icons/mcp-logo.png'
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Refresh, Connection, Tools, DataBoard, Timer,
  Service, Search, ChatDotRound, Folder, Document,
  Picture, VideoPlay, Files
} from '@element-plus/icons-vue'

// 响应式数据
const activeTab = ref('services')
const loading = ref(false)
const testingNewService = ref(false)
const addingService = ref(false)
const callingTool = ref(false)

const showAddServiceDialog = ref(false)
const showToolCallDialog = ref(false)

const toolSearchQuery = ref('')
const selectedServiceFilter = ref('')
const selectedTool = ref(null)

// MCP服务列表
const mcpServices = ref([
  {
    id: 'filesystem',
    name: '文件系统服务',
    description: '访问本地文件系统，读取和写入文件',
    status: 'connected',
    enabled: true,
    icon: '/icons/filesystem-icon.png',
    latency: 45,
    capabilities: {
      tools: true,
      resources: true,
      prompts: false
    },
    tools: [
      {
        name: 'read_file',
        description: '读取文件内容',
        inputSchema: {
          type: 'object',
          properties: {
            path: { type: 'string', description: '文件路径' }
          },
          required: ['path']
        }
      },
      {
        name: 'write_file',
        description: '写入文件内容',
        inputSchema: {
          type: 'object',
          properties: {
            path: { type: 'string', description: '文件路径' },
            content: { type: 'string', description: '文件内容' }
          },
          required: ['path', 'content']
        }
      }
    ],
    resources: [
      {
        uri: 'file:///home/user/documents',
        name: 'Documents',
        type: 'directory',
        children: [
          { uri: 'file:///home/user/documents/readme.txt', name: 'readme.txt', type: 'file', size: 1024 }
        ]
      }
    ]
  },
  {
    id: 'database',
    name: '数据库服务',
    description: '连接和查询数据库',
    status: 'disconnected',
    enabled: false,
    icon: '/icons/database-icon.png',
    latency: null,
    capabilities: {
      tools: true,
      resources: true,
      prompts: true
    },
    tools: [],
    resources: []
  },
  {
    id: 'git',
    name: 'Git仓库服务',
    description: '访问Git仓库信息和操作',
    status: 'error',
    enabled: false,
    icon: '/icons/git-icon.png',
    latency: null,
    capabilities: {
      tools: true,
      resources: true,
      prompts: false
    },
    tools: [],
    resources: []
  }
])

// 新服务表单
const newService = reactive({
  name: '',
  type: '',
  endpoint: '',
  authType: 'none',
  authCredentials: '',
  description: ''
})

// 工具调用参数
const toolCallParams = reactive({})
const toolCallResult = ref(null)

// 可用提示模板
const availablePrompts = ref([
  {
    serviceId: 'filesystem',
    serviceName: '文件系统服务',
    name: 'analyze_code',
    description: '分析代码文件的结构和质量',
    arguments: [
      { name: 'file_path', required: true },
      { name: 'language', required: false }
    ]
  }
])

// 计算属性
const connectedServices = computed(() => mcpServices.value.filter(s => s.status === 'connected'))
const totalTools = computed(() => mcpServices.value.reduce((sum, s) => sum + (s.tools?.length || 0), 0))
const totalResources = computed(() => mcpServices.value.reduce((sum, s) => sum + (s.resources?.length || 0), 0))
const avgResponseTime = computed(() => {
  const connected = connectedServices.value.filter(s => s.latency)
  if (connected.length === 0) return 0
  return Math.round(connected.reduce((sum, s) => sum + s.latency, 0) / connected.length)
})

const filteredTools = computed(() => {
  const allTools = []
  mcpServices.value.forEach(service => {
    if (service.tools && service.status === 'connected') {
      service.tools.forEach(tool => {
        allTools.push({
          ...tool,
          serviceId: service.id,
          serviceName: service.name
        })
      })
    }
  })
  
  if (!toolSearchQuery.value) return allTools
  return allTools.filter(tool => 
    tool.name.toLowerCase().includes(toolSearchQuery.value.toLowerCase()) ||
    tool.description.toLowerCase().includes(toolSearchQuery.value.toLowerCase())
  )
})

const filteredResources = computed(() => {
  const allResources = []
  mcpServices.value.forEach(service => {
    if (service.resources && service.status === 'connected') {
      if (!selectedServiceFilter.value || service.id === selectedServiceFilter.value) {
        allResources.push(...service.resources)
      }
    }
  })
  return allResources
})

// 方法定义
const refreshServices = async () => {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('服务列表已刷新')
  } catch (error) {
    ElMessage.error('刷新失败')
  } finally {
    loading.value = false
  }
}

const toggleService = async (service) => {
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    if (service.enabled) {
      service.status = 'connected'
      service.latency = Math.floor(Math.random() * 100) + 50
    } else {
      service.status = 'disconnected'
      service.latency = null
    }
    ElMessage.success(`${service.name} 已${service.enabled ? '启用' : '禁用'}`)
  } catch (error) {
    service.enabled = !service.enabled
    ElMessage.error('操作失败')
  }
}

const testService = async (service) => {
  service.testing = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    service.status = 'connected'
    service.latency = Math.floor(Math.random() * 100) + 50
    ElMessage.success(`${service.name} 连接测试成功`)
  } catch (error) {
    service.status = 'error'
    ElMessage.error(`${service.name} 连接测试失败`)
  } finally {
    service.testing = false
  }
}

const configureService = (service) => {
  ElMessage.info(`配置服务: ${service.name}`)
}

const viewServiceDetails = (service) => {
  ElMessage.info(`查看服务详情: ${service.name}`)
}

const callTool = (tool) => {
  selectedTool.value = tool
  Object.keys(toolCallParams).forEach(key => delete toolCallParams[key])
  toolCallResult.value = null
  showToolCallDialog.value = true
}

const executeToolCall = async () => {
  callingTool.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    toolCallResult.value = {
      success: true,
      result: `工具 ${selectedTool.value.name} 执行成功`,
      data: { ...toolCallParams }
    }
    ElMessage.success('工具调用成功')
  } catch (error) {
    toolCallResult.value = {
      success: false,
      error: '工具调用失败'
    }
    ElMessage.error('工具调用失败')
  } finally {
    callingTool.value = false
  }
}

const viewToolSchema = (tool) => {
  ElMessage.info(`查看工具Schema: ${tool.name}`)
}

const handleResourceClick = (data) => {
  ElMessage.info(`访问资源: ${data.name}`)
}

const formatSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const createPrompt = () => {
  ElMessage.info('创建提示模板功能')
}

const usePrompt = (prompt) => {
  ElMessage.info(`使用提示模板: ${prompt.name}`)
}

const previewPrompt = (prompt) => {
  ElMessage.info(`预览提示模板: ${prompt.name}`)
}

const testNewService = async () => {
  if (!newService.name || !newService.endpoint) {
    ElMessage.error('请填写完整信息')
    return
  }

  testingNewService.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    ElMessage.success('连接测试成功')
  } catch (error) {
    ElMessage.error('连接测试失败')
  } finally {
    testingNewService.value = false
  }
}

const addService = async () => {
  if (!newService.name || !newService.type || !newService.endpoint) {
    ElMessage.error('请填写完整信息')
    return
  }

  addingService.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    const service = {
      id: Date.now().toString(),
      name: newService.name,
      description: newService.description,
      status: 'disconnected',
      enabled: false,
      capabilities: {
        tools: true,
        resources: true,
        prompts: false
      },
      tools: [],
      resources: []
    }
    
    mcpServices.value.push(service)
    ElMessage.success('服务添加成功')
    showAddServiceDialog.value = false
    
    // 重置表单
    Object.assign(newService, {
      name: '',
      type: '',
      endpoint: '',
      authType: 'none',
      authCredentials: '',
      description: ''
    })
  } catch (error) {
    ElMessage.error('服务添加失败')
  } finally {
    addingService.value = false
  }
}

const getStatusType = (status) => {
  const statusMap = {
    connected: 'success',
    disconnected: 'info',
    error: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    connected: '已连接',
    disconnected: '未连接',
    error: '连接错误'
  }
  return statusMap[status] || '未知'
}

// 生命周期
onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.mcp-integration {
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
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon img {
  width: 48px;
  height: 48px;
  border-radius: 8px;
}

.header-text h1 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
}

.header-text p {
  margin: 0;
  color: #606266;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.services-overview {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.overview-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.overview-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409eff;
  border-radius: 12px;
  margin-right: 16px;
}

.overview-icon .el-icon {
  font-size: 24px;
  color: white;
}

.overview-value {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 4px;
}

.overview-label {
  font-size: 14px;
  color: #606266;
}

.main-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.services-section,
.tools-section,
.resources-section,
.prompts-section {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
}

.services-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 24px;
}

.service-card {
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
}

.service-card.connected {
  border-color: #67c23a;
  background: #f0f9ff;
}

.service-card.disconnected {
  border-color: #909399;
  background: #f8f9fa;
}

.service-card.error {
  border-color: #f56c6c;
  background: #fef0f0;
}

.service-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.service-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.service-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.service-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.service-icon .el-icon {
  font-size: 24px;
  color: #909399;
}

.service-details h3 {
  margin: 0 0 4px 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}

.service-details p {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.service-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  font-size: 14px;
}

.stat-item {
  display: flex;
  gap: 4px;
}

.stat-label {
  color: #606266;
}

.stat-value {
  color: #2c3e50;
  font-weight: 600;
}

.service-capabilities {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.capability-item {
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

.capability-item.active {
  background: #e1f3d8;
  color: #67c23a;
}

.service-footer {
  display: flex;
  gap: 8px;
}

.tools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.tool-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

.tool-header h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.tool-header p {
  margin: 0 0 12px 0;
  color: #606266;
  font-size: 14px;
}

.tool-schema {
  margin-bottom: 16px;
}

.tool-schema h5 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
}

.schema-properties {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.property-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.property-name {
  color: #2c3e50;
  font-weight: 600;
}

.property-type {
  color: #409eff;
  background: #ecf5ff;
  padding: 2px 6px;
  border-radius: 4px;
}

.property-required {
  color: #f56c6c;
  font-weight: bold;
}

.tool-actions {
  display: flex;
  gap: 8px;
}

.resources-tree {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
}

.resource-node {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.resource-name {
  flex: 1;
  color: #2c3e50;
}

.resource-size {
  color: #909399;
  font-size: 12px;
}

.prompts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.prompt-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  background: #fafafa;
}

.prompt-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.prompt-header h4 {
  margin: 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.prompt-description p {
  margin: 0 0 16px 0;
  color: #606266;
  font-size: 14px;
}

.prompt-arguments {
  margin-bottom: 16px;
}

.prompt-arguments h5 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 14px;
  font-weight: 600;
}

.arguments-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.prompt-actions {
  display: flex;
  gap: 8px;
}

.tool-call-form {
  max-height: 400px;
  overflow-y: auto;
}

.tool-call-result {
  margin-top: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.tool-call-result h4 {
  margin: 0 0 12px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.tool-call-result pre {
  margin: 0;
  color: #2c3e50;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
