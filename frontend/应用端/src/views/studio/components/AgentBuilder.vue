<template>
  <div class="agent-builder">
    <div class="builder-header">
      <div class="header-left">
        <h2>{{ project?.name || '新建Agent' }}</h2>
        <el-tag v-if="project?.status" :type="getStatusType(project.status)">
          {{ project.status }}
        </el-tag>
      </div>
      <div class="header-right">
        <el-button @click="handleSave">保存</el-button>
        <el-button type="primary" @click="handleTest">测试</el-button>
        <el-button type="success" @click="handlePublish">发布</el-button>
      </div>
    </div>
    
    <div class="builder-content">
      <!-- 左侧配置面板 -->
      <div class="config-panel">
        <el-scrollbar>
          <div class="config-section">
            <h3>基本信息</h3>
            <el-form :model="agentConfig" label-width="80px">
              <el-form-item label="名称">
                <el-input v-model="agentConfig.name" placeholder="请输入Agent名称" />
              </el-form-item>
              <el-form-item label="描述">
                <el-input
                  v-model="agentConfig.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入Agent描述"
                />
              </el-form-item>
              <el-form-item label="头像">
                <el-upload
                  class="avatar-uploader"
                  action="#"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                >
                  <img v-if="agentConfig.avatar" :src="agentConfig.avatar" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
            </el-form>
          </div>
          
          <div class="config-section">
            <h3>模型配置</h3>
            <el-form :model="agentConfig" label-width="80px">
              <el-form-item label="AI模型">
                <el-select v-model="agentConfig.model" placeholder="选择AI模型">
                  <el-option
                    v-for="model in availableModels"
                    :key="model.id"
                    :label="model.name"
                    :value="model.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="温度">
                <el-slider
                  v-model="agentConfig.temperature"
                  :min="0"
                  :max="1"
                  :step="0.1"
                  show-input
                />
              </el-form-item>
              <el-form-item label="最大Token">
                <el-input-number
                  v-model="agentConfig.maxTokens"
                  :min="1"
                  :max="4096"
                  :step="1"
                />
              </el-form-item>
            </el-form>
          </div>
          
          <div class="config-section">
            <h3>提示词配置</h3>
            <el-form :model="agentConfig" label-width="80px">
              <el-form-item label="系统提示">
                <el-input
                  v-model="agentConfig.systemPrompt"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入系统提示词"
                />
              </el-form-item>
              <el-form-item label="用户提示">
                <el-input
                  v-model="agentConfig.userPrompt"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入用户提示词模板"
                />
              </el-form-item>
            </el-form>
          </div>
          
          <div class="config-section">
            <h3>技能配置</h3>
            <el-form :model="agentConfig" label-width="80px">
              <el-form-item label="插件">
                <div class="plugin-selector">
                  <el-button @click="showPluginDialog = true" :icon="Plus" size="small">
                    添加插件
                  </el-button>
                  <div v-if="agentConfig.plugins.length > 0" class="selected-plugins">
                    <el-tag
                      v-for="plugin in agentConfig.plugins"
                      :key="plugin.id"
                      closable
                      @close="removePlugin(plugin.id)"
                      style="margin: 4px;"
                    >
                      {{ plugin.name }}
                    </el-tag>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="工作流">
                <div class="workflow-selector">
                  <el-button @click="showWorkflowDialog = true" :icon="Plus" size="small">
                    添加工作流
                  </el-button>
                  <div v-if="agentConfig.workflows.length > 0" class="selected-workflows">
                    <el-tag
                      v-for="workflow in agentConfig.workflows"
                      :key="workflow.id"
                      closable
                      @close="removeWorkflow(workflow.id)"
                      style="margin: 4px;"
                    >
                      {{ workflow.name }}
                    </el-tag>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="知识库">
                <div class="knowledge-selector">
                  <el-button @click="showKnowledgeDialog = true" :icon="Plus" size="small">
                    添加知识库
                  </el-button>
                  <div v-if="agentConfig.knowledgeBases.length > 0" class="selected-knowledge">
                    <el-tag
                      v-for="kb in agentConfig.knowledgeBases"
                      :key="kb.id"
                      closable
                      @close="removeKnowledge(kb.id)"
                      style="margin: 4px;"
                    >
                      {{ kb.name }}
                    </el-tag>
                  </div>
                </div>
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h3>知识配置</h3>
            <el-form :model="agentConfig.knowledge" label-width="80px">
              <el-form-item label="变量">
                <el-button @click="showVariableDialog = true" :icon="Plus" size="small">
                  添加变量
                </el-button>
                <div v-if="agentConfig.knowledge.variables.length > 0" class="variable-list">
                  <div
                    v-for="variable in agentConfig.knowledge.variables"
                    :key="variable.id"
                    class="variable-item"
                  >
                    <span>{{ variable.name }}</span>
                    <el-button size="small" text @click="editVariable(variable)">编辑</el-button>
                    <el-button size="small" text type="danger" @click="removeVariable(variable.id)">删除</el-button>
                  </div>
                </div>
              </el-form-item>

              <el-form-item label="数据库">
                <el-switch v-model="agentConfig.knowledge.enableDatabase" />
                <span style="margin-left: 8px; font-size: 12px; color: #909399;">
                  启用后可以存储和查询结构化数据
                </span>
              </el-form-item>

              <el-form-item label="长期记忆">
                <el-switch v-model="agentConfig.knowledge.longTermMemory" />
                <span style="margin-left: 8px; font-size: 12px; color: #909399;">
                  记住用户的偏好和历史对话
                </span>
              </el-form-item>
            </el-form>
          </div>

          <div class="config-section">
            <h3>对话体验</h3>
            <el-form :model="agentConfig.conversation" label-width="80px">
              <el-form-item label="开场白">
                <el-input
                  v-model="agentConfig.conversation.openingStatement"
                  type="textarea"
                  :rows="3"
                  placeholder="设置Agent的开场白"
                />
              </el-form-item>

              <el-form-item label="用户问题建议">
                <div class="suggestion-list">
                  <div
                    v-for="(suggestion, index) in agentConfig.conversation.suggestions"
                    :key="index"
                    class="suggestion-item"
                  >
                    <el-input
                      v-model="agentConfig.conversation.suggestions[index]"
                      placeholder="输入建议问题"
                      size="small"
                    />
                    <el-button
                      size="small"
                      text
                      type="danger"
                      @click="removeSuggestion(index)"
                    >
                      删除
                    </el-button>
                  </div>
                  <el-button @click="addSuggestion" :icon="Plus" size="small" text>
                    添加建议
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item label="自动回复">
                <el-switch v-model="agentConfig.conversation.autoReply" />
              </el-form-item>
            </el-form>
          </div>
        </el-scrollbar>
      </div>
      
      <!-- 右侧预览面板 -->
      <div class="preview-panel">
        <div class="preview-header">
          <h3>预览</h3>
          <el-button size="small" @click="refreshPreview">刷新</el-button>
        </div>
        <div class="preview-content">
          <AgentPreview :config="agentConfig" />
        </div>
      </div>
    </div>

    <!-- 插件选择对话框 -->
    <el-dialog v-model="showPluginDialog" title="选择插件" width="50%">
      <div class="plugin-list">
        <div
          v-for="plugin in availablePlugins"
          :key="plugin.id"
          :class="['plugin-item', { selected: isPluginSelected(plugin.id) }]"
          @click="togglePlugin(plugin)"
        >
          <div class="plugin-icon">
            <el-icon><Tools /></el-icon>
          </div>
          <div class="plugin-info">
            <div class="plugin-name">{{ plugin.name }}</div>
            <div class="plugin-description">{{ plugin.description }}</div>
          </div>
          <div class="plugin-status">
            <el-icon v-if="isPluginSelected(plugin.id)" color="#67C23A"><Check /></el-icon>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPluginDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmPluginSelection">确定</el-button>
      </template>
    </el-dialog>

    <!-- 工作流选择对话框 -->
    <el-dialog v-model="showWorkflowDialog" title="选择工作流" width="50%">
      <div class="workflow-list">
        <div
          v-for="workflow in availableWorkflows"
          :key="workflow.id"
          :class="['workflow-item', { selected: isWorkflowSelected(workflow.id) }]"
          @click="toggleWorkflow(workflow)"
        >
          <div class="workflow-icon">
            <el-icon><Connection /></el-icon>
          </div>
          <div class="workflow-info">
            <div class="workflow-name">{{ workflow.name }}</div>
            <div class="workflow-description">{{ workflow.description }}</div>
          </div>
          <div class="workflow-status">
            <el-icon v-if="isWorkflowSelected(workflow.id)" color="#67C23A"><Check /></el-icon>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showWorkflowDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmWorkflowSelection">确定</el-button>
      </template>
    </el-dialog>

    <!-- 知识库选择对话框 -->
    <el-dialog v-model="showKnowledgeDialog" title="选择知识库" width="50%">
      <div class="knowledge-list">
        <div
          v-for="kb in availableKnowledgeBases"
          :key="kb.id"
          :class="['knowledge-item', { selected: isKnowledgeSelected(kb.id) }]"
          @click="toggleKnowledge(kb)"
        >
          <div class="knowledge-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="knowledge-info">
            <div class="knowledge-name">{{ kb.name }}</div>
            <div class="knowledge-description">{{ kb.description }}</div>
          </div>
          <div class="knowledge-status">
            <el-icon v-if="isKnowledgeSelected(kb.id)" color="#67C23A"><Check /></el-icon>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showKnowledgeDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmKnowledgeSelection">确定</el-button>
      </template>
    </el-dialog>

    <!-- 变量配置对话框 -->
    <el-dialog v-model="showVariableDialog" title="配置变量" width="40%">
      <el-form :model="currentVariable" label-width="80px">
        <el-form-item label="变量名" required>
          <el-input v-model="currentVariable.name" placeholder="输入变量名" />
        </el-form-item>
        <el-form-item label="变量类型">
          <el-select v-model="currentVariable.type" placeholder="选择类型">
            <el-option label="文本" value="text" />
            <el-option label="数字" value="number" />
            <el-option label="布尔值" value="boolean" />
            <el-option label="数组" value="array" />
            <el-option label="对象" value="object" />
          </el-select>
        </el-form-item>
        <el-form-item label="默认值">
          <el-input v-model="currentVariable.defaultValue" placeholder="输入默认值" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="currentVariable.description"
            type="textarea"
            :rows="3"
            placeholder="描述变量的用途"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showVariableDialog = false">取消</el-button>
        <el-button type="primary" @click="saveVariable">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Tools, Connection, Document, Check } from '@element-plus/icons-vue'
import AgentPreview from './AgentPreview.vue'

const props = defineProps({
  project: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['save', 'test', 'publish'])

// 响应式数据
const agentConfig = reactive({
  name: '',
  description: '',
  avatar: '',
  model: 'gpt-4o',
  temperature: 0.7,
  maxTokens: 2048,
  systemPrompt: '你是一个专业的AI助手，能够帮助用户解决各种问题。',
  userPrompt: '',
  plugins: [],
  workflows: [],
  knowledgeBases: [],
  knowledge: {
    variables: [],
    enableDatabase: false,
    longTermMemory: true
  },
  conversation: {
    openingStatement: '你好！我是你的AI助手，有什么可以帮助你的吗？',
    suggestions: ['你能做什么？', '帮我分析一下这个问题', '给我一些建议'],
    autoReply: false
  }
})

// 可用模型列表
const availableModels = ref([
  { id: 'gpt-4o', name: 'GPT-4o' },
  { id: 'gpt-4o-mini', name: 'GPT-4o Mini' },
  { id: 'claude-3-5-sonnet', name: 'Claude 3.5 Sonnet' },
  { id: 'deepseek-chat', name: 'DeepSeek Chat' },
  { id: 'deepseek-coder', name: 'DeepSeek Coder' },
  { id: 'qwen-max', name: 'Qwen Max' },
  { id: 'doubao-pro', name: 'Doubao Pro' },
  { id: 'yi-large', name: 'Yi Large' }
])

// 对话框控制
const showPluginDialog = ref(false)
const showWorkflowDialog = ref(false)
const showKnowledgeDialog = ref(false)
const showVariableDialog = ref(false)

// 可用资源
const availablePlugins = ref([
  { id: 'web-search', name: '网络搜索', description: '搜索互联网获取最新信息' },
  { id: 'image-gen', name: '图像生成', description: '生成AI图像' },
  { id: 'code-exec', name: '代码执行', description: '执行Python代码' },
  { id: 'file-upload', name: '文件上传', description: '处理用户上传的文件' },
  { id: 'weather', name: '天气查询', description: '查询天气信息' }
])

const availableWorkflows = ref([
  { id: 'data-analysis', name: '数据分析流程', description: '自动化数据分析和可视化' },
  { id: 'content-gen', name: '内容生成流程', description: '批量生成营销内容' },
  { id: 'customer-service', name: '客服流程', description: '智能客服处理流程' }
])

const availableKnowledgeBases = ref([
  { id: 'company-docs', name: '公司文档', description: '公司内部文档和政策' },
  { id: 'product-manual', name: '产品手册', description: '产品使用说明和FAQ' },
  { id: 'tech-docs', name: '技术文档', description: '技术规范和API文档' }
])

// 当前编辑的变量
const currentVariable = reactive({
  id: null,
  name: '',
  type: 'text',
  defaultValue: '',
  description: ''
})

// 插件相关方法
const isPluginSelected = (pluginId) => {
  return agentConfig.plugins.some(p => p.id === pluginId)
}

const togglePlugin = (plugin) => {
  const index = agentConfig.plugins.findIndex(p => p.id === plugin.id)
  if (index > -1) {
    agentConfig.plugins.splice(index, 1)
  } else {
    agentConfig.plugins.push(plugin)
  }
}

const confirmPluginSelection = () => {
  showPluginDialog.value = false
  ElMessage.success('插件配置已更新')
}

const removePlugin = (pluginId) => {
  const index = agentConfig.plugins.findIndex(p => p.id === pluginId)
  if (index > -1) {
    agentConfig.plugins.splice(index, 1)
  }
}

// 工作流相关方法
const isWorkflowSelected = (workflowId) => {
  return agentConfig.workflows.some(w => w.id === workflowId)
}

const toggleWorkflow = (workflow) => {
  const index = agentConfig.workflows.findIndex(w => w.id === workflow.id)
  if (index > -1) {
    agentConfig.workflows.splice(index, 1)
  } else {
    agentConfig.workflows.push(workflow)
  }
}

const confirmWorkflowSelection = () => {
  showWorkflowDialog.value = false
  ElMessage.success('工作流配置已更新')
}

const removeWorkflow = (workflowId) => {
  const index = agentConfig.workflows.findIndex(w => w.id === workflowId)
  if (index > -1) {
    agentConfig.workflows.splice(index, 1)
  }
}

// 知识库相关方法
const isKnowledgeSelected = (kbId) => {
  return agentConfig.knowledgeBases.some(kb => kb.id === kbId)
}

const toggleKnowledge = (kb) => {
  const index = agentConfig.knowledgeBases.findIndex(k => k.id === kb.id)
  if (index > -1) {
    agentConfig.knowledgeBases.splice(index, 1)
  } else {
    agentConfig.knowledgeBases.push(kb)
  }
}

const confirmKnowledgeSelection = () => {
  showKnowledgeDialog.value = false
  ElMessage.success('知识库配置已更新')
}

const removeKnowledge = (kbId) => {
  const index = agentConfig.knowledgeBases.findIndex(kb => kb.id === kbId)
  if (index > -1) {
    agentConfig.knowledgeBases.splice(index, 1)
  }
}

// 变量相关方法
const editVariable = (variable) => {
  Object.assign(currentVariable, variable)
  showVariableDialog.value = true
}

const saveVariable = () => {
  if (!currentVariable.name) {
    ElMessage.warning('请输入变量名')
    return
  }

  if (currentVariable.id) {
    // 编辑现有变量
    const index = agentConfig.knowledge.variables.findIndex(v => v.id === currentVariable.id)
    if (index > -1) {
      agentConfig.knowledge.variables[index] = { ...currentVariable }
    }
  } else {
    // 添加新变量
    agentConfig.knowledge.variables.push({
      ...currentVariable,
      id: Date.now().toString()
    })
  }

  // 重置表单
  Object.assign(currentVariable, {
    id: null,
    name: '',
    type: 'text',
    defaultValue: '',
    description: ''
  })

  showVariableDialog.value = false
  ElMessage.success('变量配置已保存')
}

const removeVariable = (variableId) => {
  const index = agentConfig.knowledge.variables.findIndex(v => v.id === variableId)
  if (index > -1) {
    agentConfig.knowledge.variables.splice(index, 1)
  }
}

// 对话建议相关方法
const addSuggestion = () => {
  agentConfig.conversation.suggestions.push('')
}

const removeSuggestion = (index) => {
  agentConfig.conversation.suggestions.splice(index, 1)
}



// 主要方法
const handleSave = () => {
  emit('save', agentConfig)
  ElMessage.success('Agent配置已保存')
}

const handleTest = () => {
  emit('test', { ...props.project, config: agentConfig })
}

const handlePublish = () => {
  emit('publish', { ...props.project, config: agentConfig })
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像图片只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
    return false
  }
  
  // 这里应该上传到服务器，暂时使用本地预览
  const reader = new FileReader()
  reader.onload = (e) => {
    agentConfig.avatar = e.target.result
  }
  reader.readAsDataURL(file)
  
  return false // 阻止自动上传
}

const refreshPreview = () => {
  // 刷新预览
  ElMessage.success('预览已刷新')
}

const getStatusType = (status) => {
  const types = {
    draft: 'info',
    testing: 'warning',
    published: 'success',
    archived: 'danger'
  }
  return types[status] || 'info'
}

// 监听项目变化
watch(() => props.project, (newProject) => {
  if (newProject && newProject.config) {
    Object.assign(agentConfig, newProject.config)
  }
}, { immediate: true })

onMounted(() => {
  // 初始化配置
  if (props.project?.config) {
    Object.assign(agentConfig, props.project.config)
  }
})
</script>

<style scoped>
.agent-builder {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.builder-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-left h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  gap: 8px;
}

.builder-content {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow: hidden;
}

.config-panel {
  width: 400px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: white;
}

.config-section {
  padding: 20px;
  border-bottom: 1px solid #f5f7fa;
}

.config-section:last-child {
  border-bottom: none;
}

.config-section h3 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.preview-panel {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: white;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.preview-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.preview-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 60px;
  height: 60px;
  text-align: center;
  line-height: 60px;
}

.avatar {
  width: 60px;
  height: 60px;
  display: block;
  object-fit: cover;
}

/* 插件、工作流、知识库选择器样式 */
.plugin-selector, .workflow-selector, .knowledge-selector {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.selected-plugins, .selected-workflows, .selected-knowledge {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.plugin-list, .workflow-list, .knowledge-list {
  max-height: 400px;
  overflow-y: auto;
}

.plugin-item, .workflow-item, .knowledge-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.plugin-item:hover, .workflow-item:hover, .knowledge-item:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.plugin-item.selected, .workflow-item.selected, .knowledge-item.selected {
  border-color: #409eff;
  background-color: #e6f7ff;
}

.plugin-icon, .workflow-icon, .knowledge-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
  margin-right: 12px;
}

.plugin-info, .workflow-info, .knowledge-info {
  flex: 1;
}

.plugin-name, .workflow-name, .knowledge-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.plugin-description, .workflow-description, .knowledge-description {
  font-size: 12px;
  color: #666;
}

.plugin-status, .workflow-status, .knowledge-status {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 变量列表样式 */
.variable-list {
  margin-top: 8px;
}

.variable-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: white;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 4px;
}

/* 建议列表样式 */
.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
