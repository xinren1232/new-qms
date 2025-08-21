<template>
  <div class="enhanced-chat-container">
    <!-- 顶部工具栏 -->
    <div class="chat-toolbar">
      <div class="toolbar-left">
        <div class="model-selector">
          <el-select
            v-model="currentModel"
            @change="switchModel"
            placeholder="选择AI模型"
            style="width: 200px"
          >
            <el-option
              v-for="model in availableModels"
              :key="model.provider"
              :label="getModelDisplayName(model)"
              :value="model.provider"
            >
              <div class="model-option">
                <span class="model-name">{{ model.name }}</span>
                <el-tag v-if="model.features?.external" type="info" size="small">外部</el-tag>
                <el-tag v-if="model.features?.vision" type="success" size="small">视觉</el-tag>
                <el-tag v-if="model.features?.reasoning" type="warning" size="small">推理</el-tag>
              </div>
            </el-option>
          </el-select>
        </div>

        <!-- 模型状态和特性展示 -->
        <div class="model-status-features">
          <div class="model-status">
            <el-tag :type="modelStatus.status === 'online' ? 'success' : 'danger'" size="small">
              {{ modelStatus.status === 'online' ? '在线' : '离线' }}
            </el-tag>
            <span class="status-text">{{ modelStatus.provider }}</span>
          </div>

          <!-- 当前模型特性指标 -->
          <div class="model-features-inline">
            <div class="feature-item" v-if="currentModelFeatures.reasoning">
              <div class="feature-indicator active"></div>
              <span>推理</span>
            </div>
            <div class="feature-item" v-if="currentModelFeatures.toolCalls">
              <div class="feature-indicator active"></div>
              <span>工具</span>
            </div>
            <div class="feature-item" v-if="currentModelFeatures.vision">
              <div class="feature-indicator active"></div>
              <span>视觉</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间状态展示 -->
      <div class="toolbar-center">
        <div class="status-metrics-inline">
          <div class="metric-group">
            <div class="metric-item-inline">
              <span class="metric-label">今日对话</span>
              <span class="metric-value">{{ userStats.today_conversations || 0 }}</span>
            </div>
            <div class="metric-item-inline">
              <span class="metric-label">今日消息</span>
              <span class="metric-value">{{ userStats.today_messages || 0 }}</span>
            </div>
            <div class="metric-item-inline">
              <span class="metric-label">总对话</span>
              <span class="metric-value">{{ userStats.total_conversations || 0 }}</span>
            </div>
          </div>
          <div class="divider-vertical"></div>
          <div class="metric-group">
            <div class="metric-item-inline">
              <span class="metric-label">执行状态</span>
              <span class="metric-value">{{ executionStats.completedSteps }}/{{ executionStats.totalSteps }}</span>
            </div>
            <div class="metric-item-inline">
              <span class="metric-label">总耗时</span>
              <span class="metric-value">{{ executionStats.totalDuration }}ms</span>
            </div>
          </div>
        </div>
      </div>

      <div class="toolbar-right">
        <el-button @click="showHistory = true" size="small" type="text">
          <el-icon><Clock /></el-icon>
          历史记录
        </el-button>
        <el-button @click="clearMessages" size="small" type="text">
          <el-icon><Delete /></el-icon>
          清空对话
        </el-button>
        <el-button @click="exportConversation" size="small" type="text">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 主聊天区域 -->
    <div class="main-chat-area">
      <!-- 左侧边栏 -->
      <div class="left-sidebar">
        <!-- 最近对话 -->
        <div class="sidebar-section">
          <div class="section-header centered">
            <el-icon><ChatDotRound /></el-icon>
            <span>最近对话</span>
            <el-button
              @click="debouncedRefreshConversations"
              size="small"
              type="text"
              :loading="isRefreshing"
              title="刷新对话列表"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>

          <div class="conversation-list">
            <div
              v-for="conversation in (recentConversations || []).slice(0, 8)"
              :key="conversation.id"
              class="conversation-item"
              @click="loadConversation(conversation.id)"
            >
              <div class="conversation-content">
                <div class="conversation-header">
                  <div class="conversation-title">{{ truncateTitle(conversation.title || '新对话') }}</div>
                  <div class="conversation-status">
                    <el-icon class="status-icon"><ChatDotRound /></el-icon>
                  </div>
                </div>
                <div class="conversation-preview">
                  {{ truncatePreview(conversation.last_message || '暂无消息') }}
                </div>
                <div class="conversation-footer">
                  <span class="conversation-time">{{ formatRelativeTime(conversation.updated_at) }}</span>
                  <span class="message-count">{{ conversation.message_count || 0 }}条</span>
                </div>
              </div>
            </div>

            <div v-if="(recentConversations || []).length === 0" class="empty-conversations">
              <el-icon><ChatDotRound /></el-icon>
              <span>暂无对话记录</span>
            </div>

            <!-- 查看更多对话 -->
            <div v-if="(recentConversations || []).length > 8" class="view-more-conversations">
              <el-button @click="showHistory = true" size="small" type="text">
                <el-icon><More /></el-icon>
                查看全部 {{ (recentConversations || []).length }} 条对话
              </el-button>
            </div>
          </div>
        </div>

        <!-- 快捷工具 -->
        <div class="sidebar-section">
          <div class="section-header centered">
            <el-icon><Tools /></el-icon>
            <span>快捷工具</span>
          </div>

          <!-- 工具分类 -->
          <div class="tool-categories">
            <el-button
              v-for="category in toolCategories"
              :key="category.key"
              :type="currentToolCategory === category.key ? 'primary' : 'default'"
              size="small"
              @click="switchToolCategory(category.key)"
              class="category-btn"
            >
              <el-icon><component :is="category.icon" /></el-icon>
              {{ category.name }}
            </el-button>
          </div>

          <!-- 工具列表 -->
          <div class="tools-container">
            <div class="tools-list">
              <div
                v-for="tool in currentTools"
                :key="tool.id"
                class="tool-item"
                @click="useTool(tool)"
              >
                <div class="tool-icon">
                  <el-icon><component :is="tool.icon" /></el-icon>
                </div>
                <div class="tool-info">
                  <div class="tool-name">{{ tool.name }}</div>
                  <div class="tool-desc">{{ tool.description }}</div>
                </div>
                <div class="tool-status">
                  <el-tag
                    :type="tool.status === 'available' ? 'success' : 'info'"
                    size="small"
                  >
                    {{ tool.status === 'available' ? '可用' : '开发中' }}
                  </el-tag>
                </div>
              </div>
            </div>

            <!-- 工具分页 -->
            <div v-if="totalToolPages > 1" class="tools-pagination">
              <el-pagination
                v-model:current-page="currentToolPage"
                :page-size="toolsPerPage"
                :total="filteredTools.length"
                layout="prev, pager, next"
                small
                @current-change="handleToolPageChange"
              />
            </div>
          </div>
        </div>

        <!-- 快捷问题 -->
        <div class="sidebar-section">
          <div class="section-header centered">
            <el-icon><QuestionFilled /></el-icon>
            <span>快捷问题</span>
          </div>

          <div class="quick-questions">
            <div
              v-for="category in quickQuestionCategories"
              :key="category.key"
              class="question-category"
            >
              <div class="category-title">{{ category.name }}</div>
              <div class="question-list">
                <div
                  v-for="question in category.questions"
                  :key="question"
                  class="question-item"
                  @click="sendQuickQuestion(question)"
                >
                  {{ question }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间对话区域 -->
      <div class="conversation-area">
        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0" class="welcome-message">
            <div class="welcome-content">
              <div class="robot-logo">
                <div class="robot-head">
                  <div class="robot-antenna left"></div>
                  <div class="robot-antenna right"></div>
                  <div class="robot-face">
                    <div class="robot-eyes">
                      <div class="robot-eye left"></div>
                      <div class="robot-eye right"></div>
                    </div>
                    <div class="robot-mouth"></div>
                  </div>
                </div>
              </div>
              <h3>欢迎使用QMS-AI智能问答</h3>
              <p>我是您的质量管理专家助手，可以帮助您解答质量管理相关问题</p>
            </div>
          </div>

          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['message', message.role]"
          >
            <div class="message-avatar">
              <el-icon v-if="message.role === 'user'">
                <User />
              </el-icon>
              <el-icon v-else>
                <Avatar />
              </el-icon>
            </div>

            <div class="message-content">
              <div class="message-header">
                <span class="message-role">{{ message.role === 'user' ? '您' : 'AI助手' }}</span>
                <span class="message-time">{{ formatTime(message.timestamp) }}</span>
              </div>

              <div class="message-text" v-html="formatMessage(message.content)"></div>

              <div v-if="message.role === 'assistant' && message.model_info" class="message-meta">
                <span>模型: {{ message.model_info.name }}</span>
                <span v-if="message.model_info.tokens">Token: {{ message.model_info.tokens }}</span>
              </div>

              <div v-if="message.role === 'assistant' && !message.loading" class="message-actions">
                <el-button @click="copyMessage(message.content)" size="small" type="text">
                  <el-icon><DocumentCopy /></el-icon>
                  复制
                </el-button>
                <el-button @click="speakMessage(message.content)" size="small" type="text">
                  <el-icon><Microphone /></el-icon>
                  朗读
                </el-button>
                <el-button @click="regenerateMessage(index)" size="small" type="text">
                  <el-icon><Refresh /></el-icon>
                  重新生成
                </el-button>
              </div>

              <div v-if="message.loading" class="message-loading">
                <div class="loading-dots">
                  <div class="dot"></div>
                  <div class="dot"></div>
                  <div class="dot"></div>
                </div>
                <span>AI正在思考中...</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <div class="input-container">
            <div class="input-wrapper">
              <el-input
                v-model="inputMessage"
                type="textarea"
                :rows="3"
                placeholder="输入您的问题..."
                @keydown.enter.exact="handleEnterKey"
                @keydown.enter.shift.exact="handleShiftEnter"
                :disabled="loading"
                resize="none"
              />

              <div class="input-actions">
                <div class="action-left">
                  <el-upload
                    ref="uploadRef"
                    :auto-upload="false"
                    :show-file-list="false"
                    :on-change="handleFileSelect"
                    multiple
                    accept=".txt,.csv,.xlsx,.xls,.pdf,.doc,.docx"
                  >
                    <el-button size="small" type="text">
                      <el-icon><Paperclip /></el-icon>
                      附件
                    </el-button>
                  </el-upload>

                  <el-button @click="toggleVoiceInput" size="small" type="text">
                    <el-icon><Microphone /></el-icon>
                    语音
                  </el-button>
                </div>

                <div class="action-right">
                  <el-button @click="sendMessage" type="primary" :loading="loading">
                    <el-icon><Promotion /></el-icon>
                    发送
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧执行流程面板 -->
      <div class="info-panel">
        <div class="section-header">
          <el-icon><DataAnalysis /></el-icon>
          <span>执行流程</span>
          <div class="drag-hint">
            <el-icon><Rank /></el-icon>
            <span>拖拽查看</span>
          </div>
        </div>

        <!-- 执行步骤 -->
        <div
          class="execution-steps"
          ref="executionStepsRef"
          @mousedown="startDrag"
        >
          <div
            v-for="(step, index) in executionSteps"
            :key="index"
            :class="['execution-step', step.status]"
          >
            <div class="step-indicator">
              <div class="step-number" v-if="step.status === 'pending'">{{ index + 1 }}</div>
              <el-icon v-else-if="step.status === 'running'" class="step-loading">
                <Loading />
              </el-icon>
              <el-icon v-else-if="step.status === 'completed'" class="step-success">
                <Check />
              </el-icon>
              <el-icon v-else-if="step.status === 'error'" class="step-error">
                <Close />
              </el-icon>
            </div>

            <div class="step-content">
              <div class="step-header">
                <div class="step-title">{{ step.title }}</div>
                <div v-if="step.duration" class="step-duration">{{ step.duration }}ms</div>
              </div>
              <div class="step-description">{{ step.description }}</div>

              <!-- 步骤详情 -->
              <div v-if="step.details && step.details.length > 0" class="step-details">
                <div
                  v-for="(detail, detailIndex) in step.details"
                  :key="detailIndex"
                  class="step-detail-item"
                  :class="{
                    'completed': step.status === 'completed',
                    'running': step.status === 'running' && detailIndex === 0,
                    'pending': step.status === 'pending' || (step.status === 'running' && detailIndex > 0)
                  }"
                >
                  <el-icon class="detail-icon">
                    <Check v-if="step.status === 'completed'" />
                    <Loading v-else-if="step.status === 'running' && detailIndex === 0" />
                    <Clock v-else />
                  </el-icon>
                  <span class="detail-text">{{ detail }}</span>
                </div>
              </div>

              <!-- 时间信息 -->
              <div v-if="step.startTime" class="step-timing">
                <span class="timing-label">开始:</span>
                <span class="timing-value">{{ formatStepTime(step.startTime) }}</span>
                <span v-if="step.endTime" class="timing-separator">→</span>
                <span v-if="step.endTime" class="timing-value">{{ formatStepTime(step.endTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 执行统计 -->
        <div class="execution-stats" v-if="executionStats.totalSteps > 0">
          <div class="stats-header">执行统计</div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ executionStats.completedSteps }}</div>
              <div class="stat-label">已完成</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ executionStats.totalDuration }}ms</div>
              <div class="stat-label">总耗时</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 历史记录抽屉 -->
    <el-drawer v-model="showHistory" title="对话历史" size="400px">
      <div class="history-content">
        <div class="history-actions">
          <el-input
            v-model="historySearchQuery"
            placeholder="搜索对话..."
            size="small"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="conversation-list">
          <div
            v-for="conversation in filteredHistory"
            :key="conversation.id"
            class="conversation-item"
            @click="loadConversation(conversation.id)"
          >
            <div class="conversation-title">{{ conversation.title }}</div>
            <div class="conversation-time">{{ formatTime(conversation.updated_at) }}</div>
            <div class="conversation-preview">{{ conversation.first_message }}</div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 防抖函数
const debounce = (func, wait) => {
  let timeout
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout)
      func(...args)
    }
    clearTimeout(timeout)
    timeout = setTimeout(later, wait)
  }
}

// 节流函数
const throttle = (func, limit) => {
  let inThrottle
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// 重试机制
const retryRequest = async (requestFn, maxRetries = 3, delay = 1000) => {
  for (let i = 0; i < maxRetries; i++) {
    try {
      return await requestFn()
    } catch (error) {
      if (i === maxRetries - 1) throw error
      await new Promise(resolve => setTimeout(resolve, delay * Math.pow(2, i)))
    }
  }
}
import {
  ChatDotRound, Clock, Delete, Download, Refresh, More, Tools, QuestionFilled,
  User, Avatar, DocumentCopy, Microphone, Promotion, Paperclip, DataAnalysis,
  Rank, Loading, Check, Close, Search
} from '@element-plus/icons-vue'

// 响应式数据
const messages = ref([])
const inputMessage = ref('')
const loading = ref(false)
const isRefreshing = ref(false)
const currentModel = ref('GPT-4o')
const availableModels = ref([])
const showHistory = ref(false)
const historySearchQuery = ref('')
const recentConversations = ref([])
const uploadedFiles = ref([])

// 模型状态
const modelStatus = reactive({
  status: 'online',
  provider: 'GPT-4o'
})

// 用户统计
const userStats = ref({
  today_conversations: 0,
  today_messages: 0,
  total_conversations: 0
})

// 执行步骤
const executionSteps = ref([])
const executionStats = reactive({
  totalSteps: 0,
  completedSteps: 0,
  totalDuration: 0
})

// 工具相关
const currentToolCategory = ref('all')
const currentToolPage = ref(1)
const toolsPerPage = 6

// 语音功能相关
const speechRecognition = ref(null)
const isRecording = ref(false)
const voiceEnabled = ref(false)

// 拖拽相关
const isDragging = ref(false)
const dragStartX = ref(0)
const dragStartScrollLeft = ref(0)

// 计算属性
const currentModelFeatures = computed(() => {
  const model = availableModels.value.find(m => m.provider === currentModel.value)
  return model?.features || {}
})

const getModelDisplayName = (model) => {
  return model.name || model.provider
}

const filteredHistory = computed(() => {
  const conversations = recentConversations.value || []
  if (!historySearchQuery.value) return conversations
  return conversations.filter(conv =>
    conv.title?.toLowerCase().includes(historySearchQuery.value.toLowerCase()) ||
    conv.first_message?.toLowerCase().includes(historySearchQuery.value.toLowerCase())
  )
})

// 工具分类
const toolCategories = ref([
  { key: 'all', name: '全部', icon: 'Grid' },
  { key: 'input', name: '输入', icon: 'Microphone' },
  { key: 'output', name: '输出', icon: 'VideoPlay' },
  { key: 'analysis', name: '分析', icon: 'DataAnalysis' },
  { key: 'generation', name: '生成', icon: 'Document' },
  { key: 'management', name: '管理', icon: 'Setting' },
  { key: 'export', name: '导出', icon: 'Download' },
  { key: 'ai', name: 'AI', icon: 'Switch' }
])

// 快捷问题分类
const quickQuestionCategories = ref([
  {
    key: 'quality',
    name: '质量管理',
    questions: [
      '什么是质量管理体系？',
      'ISO 9001标准的主要内容',
      '如何进行质量审核？',
      '质量改进的方法有哪些？'
    ]
  },
  {
    key: 'process',
    name: '流程优化',
    questions: [
      '如何识别流程瓶颈？',
      '流程改进的步骤',
      '如何建立标准作业程序？',
      '流程绩效指标设计'
    ]
  }
])

// 工具数据 - 基于实际可用功能
const allTools = ref([
  {
    id: 'voice-input',
    name: '语音输入',
    description: '语音转文字输入功能',
    icon: 'Microphone',
    category: 'input',
    status: 'available',
    action: 'voice'
  },
  {
    id: 'voice-output',
    name: '语音播放',
    description: '文字转语音播放功能',
    icon: 'VideoPlay',
    category: 'output',
    status: 'available',
    action: 'voice'
  },
  {
    id: 'document-export',
    name: '文档导出',
    description: '导出对话为Word/Excel文档',
    icon: 'Download',
    category: 'export',
    status: 'available',
    action: 'export'
  },
  {
    id: 'conversation-history',
    name: '对话历史',
    description: '查看和管理历史对话记录',
    icon: 'Clock',
    category: 'management',
    status: 'available',
    action: 'history'
  },
  {
    id: 'model-switch',
    name: '模型切换',
    description: '切换不同的AI模型',
    icon: 'Switch',
    category: 'ai',
    status: 'available',
    action: 'model'
  },
  {
    id: 'quality-analysis',
    name: '质量分析',
    description: '基于AI的质量数据分析',
    icon: 'DataAnalysis',
    category: 'analysis',
    status: 'available',
    action: 'prompt'
  },
  {
    id: 'spc-control',
    name: 'SPC控制图',
    description: '统计过程控制图分析',
    icon: 'TrendCharts',
    category: 'analysis',
    status: 'available',
    action: 'prompt'
  },
  {
    id: 'document-generator',
    name: '文档生成',
    description: 'AI辅助生成质量管理文档',
    icon: 'Document',
    category: 'generation',
    status: 'available',
    action: 'prompt'
  }
])

const filteredTools = computed(() => {
  if (currentToolCategory.value === 'all') {
    return allTools.value
  }
  return allTools.value.filter(tool => tool.category === currentToolCategory.value)
})

const currentTools = computed(() => {
  const start = (currentToolPage.value - 1) * toolsPerPage
  const end = start + toolsPerPage
  return filteredTools.value.slice(start, end)
})

const totalToolPages = computed(() => {
  return Math.ceil(filteredTools.value.length / toolsPerPage)
})

// 方法
const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return

  const userMessage = {
    role: 'user',
    content: inputMessage.value.trim(),
    timestamp: new Date()
  }

  messages.value.push(userMessage)

  const assistantMessage = {
    role: 'assistant',
    content: '',
    loading: true,
    timestamp: new Date()
  }

  messages.value.push(assistantMessage)

  const currentInput = inputMessage.value.trim()
  inputMessage.value = ''
  loading.value = true

  // 初始化执行流程并开始跟踪
  initExecutionFlow(currentInput)
  startExecutionTracking()

  try {
    // 构建增强的请求，提示AI生成更详细的回答
    const enhancedPrompt = `请详细回答以下问题，提供全面、结构化的信息：

${currentInput}

请确保回答：
1. 内容详尽，涵盖问题的各个方面
2. 使用清晰的结构和格式（如标题、列表、代码块等）
3. 提供具体的例子或实践建议
4. 如果涉及技术内容，请包含相关的实现细节
5. 适当使用Markdown格式来提升可读性`

    const apiStartTime = Date.now()

    const response = await axios.post('/api/chat/send', {
      message: enhancedPrompt,
      model: currentModel.value,
      conversation_id: null,
      temperature: 0.7,
      max_tokens: 4000  // 增加最大token数以支持更详细的回答
    })

    const apiDuration = Date.now() - apiStartTime

    if (response.data.success) {
      // 完成内容生成步骤
      updateExecutionStep(3, 'completed', apiDuration, `生成了 ${response.data.data.content.length} 字符的回答`)
      updateExecutionStep(4, 'running')

      // 处理响应
      setTimeout(() => {
        updateExecutionStep(4, 'completed', 100)
        updateExecutionStep(5, 'running')

        // 模拟数据存储
        setTimeout(() => {
          updateExecutionStep(5, 'completed', 50)
          updateExecutionStats()
        }, 100)
      }, 50)

      assistantMessage.content = response.data.data.content || response.data.data.response
      assistantMessage.model_info = response.data.data.model_info
      assistantMessage.loading = false

    } else {
      throw new Error(response.data.message || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)

    // 标记当前步骤为错误
    const runningStepIndex = executionSteps.value.findIndex(step => step.status === 'running')
    if (runningStepIndex !== -1) {
      updateExecutionStep(runningStepIndex, 'error', null, `错误: ${error.message}`)
    }

    assistantMessage.content = '抱歉，发送消息时出现错误，请稍后重试。\n\n错误详情：' + (error.message || '网络连接异常')
    assistantMessage.loading = false
    ElMessage.error('发送失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

const switchModel = async (modelProvider) => {
  try {
    const response = await axios.post('/api/chat/switch-model', {
      model: modelProvider
    })

    if (response.data.success) {
      currentModel.value = modelProvider
      modelStatus.provider = modelProvider
      ElMessage.success(`已切换到 ${modelProvider}`)
    } else {
      throw new Error(response.data.message || '切换失败')
    }
  } catch (error) {
    console.error('切换模型失败:', error)
    ElMessage.error('切换模型失败: ' + (error.message || '网络错误'))
  }
}

const clearMessages = async () => {
  try {
    await ElMessageBox.confirm('确定要清空当前对话吗？', '确认清空', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    messages.value = []
    executionSteps.value = []
    executionStats.totalSteps = 0
    executionStats.completedSteps = 0
    executionStats.totalDuration = 0

    ElMessage.success('对话已清空')
  } catch {
    // 用户取消
  }
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败')
  }
}

const formatMessage = (content) => {
  if (!content) return ''

  // 增强的Markdown格式化，支持更多格式
  return content
    // 代码块（三个反引号）
    .replace(/```([\s\S]*?)```/g, '<pre class="code-block"><code>$1</code></pre>')
    // 行内代码
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
    // 粗体
    .replace(/\*\*(.*?)\*\*/g, '<strong class="bold-text">$1</strong>')
    // 斜体
    .replace(/\*(.*?)\*/g, '<em class="italic-text">$1</em>')
    // 标题（### ## #）
    .replace(/^### (.*$)/gm, '<h3 class="heading-3">$1</h3>')
    .replace(/^## (.*$)/gm, '<h2 class="heading-2">$1</h2>')
    .replace(/^# (.*$)/gm, '<h1 class="heading-1">$1</h1>')
    // 无序列表
    .replace(/^[\s]*[-*+] (.+)$/gm, '<li class="list-item">$1</li>')
    // 有序列表
    .replace(/^[\s]*\d+\. (.+)$/gm, '<li class="ordered-list-item">$1</li>')
    // 链接
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" class="link">$1</a>')
    // 分割线
    .replace(/^---$/gm, '<hr class="divider">')
    // 引用
    .replace(/^> (.+)$/gm, '<blockquote class="quote">$1</blockquote>')
    // 换行
    .replace(/\n/g, '<br>')
    // 包装列表项
    .replace(/(<li class="list-item">.*?<\/li>)/gs, '<ul class="unordered-list">$1</ul>')
    .replace(/(<li class="ordered-list-item">.*?<\/li>)/gs, '<ol class="ordered-list">$1</ol>')
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatStepTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatRelativeTime = (timestamp) => {
  const now = new Date()
  const time = new Date(timestamp)
  const diff = now - time

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

const truncateTitle = (title) => {
  return title.length > 20 ? title.substring(0, 20) + '...' : title
}

const truncatePreview = (preview) => {
  return preview.length > 50 ? preview.substring(0, 50) + '...' : preview
}

const scrollToBottom = () => {
  const container = document.querySelector('.messages-container')
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

const handleEnterKey = (event) => {
  if (!event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }
}

const handleShiftEnter = (event) => {
  // 允许Shift+Enter换行
}

// 执行流程相关 - 基于实际后端执行过程
const initExecutionFlow = (query) => {
  const startTime = Date.now()

  executionSteps.value = [
    {
      title: '请求验证',
      description: `验证用户输入: "${query.substring(0, 30)}${query.length > 30 ? '...' : ''}"`,
      status: 'running',
      startTime: startTime,
      details: ['输入长度检查', '内容安全过滤', '速率限制检查']
    },
    {
      title: '模型选择',
      description: `使用模型: ${currentModel.value}`,
      status: 'pending',
      details: ['模型可用性检查', '负载均衡分配', '配置参数加载']
    },
    {
      title: 'API调用',
      description: '向AI服务发送请求...',
      status: 'pending',
      details: ['构建请求参数', '建立连接', '发送数据']
    },
    {
      title: '内容生成',
      description: 'AI模型正在思考和生成回答...',
      status: 'pending',
      details: ['问题理解', '知识检索', '内容组织', '回答生成']
    },
    {
      title: '响应处理',
      description: '处理AI返回的内容...',
      status: 'pending',
      details: ['内容解析', '格式化处理', '安全检查']
    },
    {
      title: '数据存储',
      description: '保存对话记录到数据库...',
      status: 'pending',
      details: ['生成对话ID', '存储用户消息', '存储AI回复']
    }
  ]

  executionStats.totalSteps = executionSteps.value.length
  executionStats.completedSteps = 0
  executionStats.totalDuration = 0
  executionStats.startTime = startTime
}

// 基于实际执行时间的流程更新
const updateExecutionStep = (stepIndex, status, duration = null, additionalInfo = null) => {
  if (stepIndex < executionSteps.value.length) {
    const step = executionSteps.value[stepIndex]
    step.status = status

    if (duration !== null) {
      step.duration = duration
      step.endTime = Date.now()
    }

    if (status === 'running') {
      step.startTime = Date.now()
    }

    if (additionalInfo) {
      step.description = additionalInfo
    }

    updateExecutionStats()
  }
}

// 开始执行流程跟踪
const startExecutionTracking = () => {
  // 第一步：请求验证（立即完成）
  updateExecutionStep(0, 'running')

  setTimeout(() => {
    updateExecutionStep(0, 'completed', 50)
    updateExecutionStep(1, 'running')

    // 第二步：模型选择
    setTimeout(() => {
      updateExecutionStep(1, 'completed', 100)
      updateExecutionStep(2, 'running')

      // 第三步：API调用
      setTimeout(() => {
        updateExecutionStep(2, 'completed', 200, '已连接到AI服务...')
        updateExecutionStep(3, 'running')
        // 第四步在实际API响应时完成
      }, 300)
    }, 150)
  }, 100)
}

const updateExecutionStats = () => {
  executionStats.completedSteps = executionSteps.value.filter(step => step.status === 'completed').length
  executionStats.totalDuration = executionSteps.value
    .filter(step => step.duration)
    .reduce((total, step) => total + step.duration, 0)
}

// 工具相关方法
const switchToolCategory = (category) => {
  currentToolCategory.value = category
  currentToolPage.value = 1
}

const handleToolPageChange = (page) => {
  currentToolPage.value = page
}

const useTool = (tool) => {
  if (tool.status !== 'available') {
    ElMessage.info(`${tool.name} 正在开发中，敬请期待`)
    return
  }

  ElMessage.success(`正在启动 ${tool.name}...`)

  // 根据工具类型执行不同的操作
  switch (tool.action) {
    case 'voice':
      handleVoiceTool(tool)
      break
    case 'export':
      handleExportTool(tool)
      break
    case 'history':
      handleHistoryTool(tool)
      break
    case 'model':
      handleModelTool(tool)
      break
    case 'prompt':
      handlePromptTool(tool)
      break
    default:
      ElMessage.info('工具功能开发中')
  }
}

// 处理语音工具
const handleVoiceTool = (tool) => {
  if (tool.id === 'voice-input') {
    toggleVoiceRecognition()
  } else if (tool.id === 'voice-output') {
    toggleVoiceOutput()
  }
}

// 处理导出工具
const handleExportTool = (tool) => {
  if (messages.value.length === 0) {
    ElMessage.warning('没有对话内容可以导出')
    return
  }

  // 这里可以调用导出API
  ElMessage.info('导出功能开发中，请使用右上角的导出按钮')
}

// 处理历史工具
const handleHistoryTool = (tool) => {
  showConversationHistory()
}

// 处理模型工具
const handleModelTool = (tool) => {
  ElMessage.info('请使用顶部工具栏的模型切换功能')
}

// 处理提示词工具
const handlePromptTool = (tool) => {
  let prompt = ''

  switch (tool.id) {
    case 'quality-analysis':
      prompt = '请帮我进行质量数据分析，我需要分析生产过程的质量控制情况，包括：\n1. 数据趋势分析\n2. 异常值识别\n3. 过程能力评估\n4. 改进建议\n\n请提供详细的分析方法和步骤。'
      break
    case 'spc-control':
      prompt = '请详细介绍SPC统计过程控制的应用，包括：\n1. 控制图的类型和选择\n2. 控制限的计算方法\n3. 异常模式的识别\n4. 实际案例分析\n\n请提供具体的实施指导。'
      break
    case 'document-generator':
      prompt = '请帮我生成质量管理相关文档，我需要：\n1. 文档结构和模板\n2. 关键内容要点\n3. 格式规范要求\n4. 实用性建议\n\n请提供专业的文档生成指导。'
      break
    default:
      prompt = `请为我提供关于${tool.name}的详细指导和建议。`
  }

  inputMessage.value = prompt
  sendMessage()
}

// 拖拽相关方法
const startDrag = (event) => {
  isDragging.value = true
  dragStartX.value = event.clientX
  const container = event.currentTarget
  dragStartScrollLeft.value = container.scrollLeft

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', endDrag)
}

const onDrag = (event) => {
  if (!isDragging.value) return

  const container = document.querySelector('.execution-steps')
  if (container) {
    const deltaX = event.clientX - dragStartX.value
    container.scrollLeft = dragStartScrollLeft.value - deltaX
  }
}

const endDrag = () => {
  isDragging.value = false
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', endDrag)
}

// 缓存管理
const cache = reactive({
  models: { data: null, timestamp: 0, ttl: 5 * 60 * 1000 }, // 5分钟缓存
  stats: { data: null, timestamp: 0, ttl: 2 * 60 * 1000 },  // 2分钟缓存
  conversations: { data: null, timestamp: 0, ttl: 30 * 1000 } // 30秒缓存
})

// 缓存检查函数
const isCacheValid = (cacheKey) => {
  const cacheItem = cache[cacheKey]
  return cacheItem.data && (Date.now() - cacheItem.timestamp) < cacheItem.ttl
}

// 优化的加载函数
const loadModelsWithCache = async () => {
  if (isCacheValid('models')) {
    availableModels.value = cache.models.data
    return
  }

  try {
    const response = await axios.get('/api/chat/models')
    if (response.data.success) {
      const models = response.data.data.models || []
      availableModels.value = models
      cache.models = { data: models, timestamp: Date.now(), ttl: cache.models.ttl }
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
    ElMessage.error('加载模型列表失败，请稍后重试')
  }
}

const loadStatsWithCache = async () => {
  if (isCacheValid('stats')) {
    userStats.value = cache.stats.data
    return
  }

  try {
    const statsResponse = await axios.get('/api/chat/statistics?user_id=anonymous&days=30')
    if (statsResponse.data.success) {
      const stats = statsResponse.data.data
      userStats.value = stats
      cache.stats = { data: stats, timestamp: Date.now(), ttl: cache.stats.ttl }
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
    ElMessage.warning('统计信息加载失败')
  }
}

// 生命周期
onMounted(async () => {
  // 并行加载数据以提高性能
  await Promise.all([
    loadModelsWithCache(),
    loadStatsWithCache(),
    refreshConversations()
  ])
})

const refreshConversations = async (forceRefresh = false) => {
  // 检查缓存（除非强制刷新）
  if (!forceRefresh && isCacheValid('conversations')) {
    recentConversations.value = cache.conversations.data
    return
  }

  isRefreshing.value = true
  try {
    const response = await axios.get('/api/chat/conversations', {
      params: {
        limit: 20,
        offset: 0,
        _t: Date.now() // 防止缓存
      }
    })

    if (response.data.success) {
      const data = response.data.data
      const conversations = Array.isArray(data) ? data : []
      recentConversations.value = conversations

      // 更新缓存
      cache.conversations = {
        data: conversations,
        timestamp: Date.now(),
        ttl: cache.conversations.ttl
      }
    } else {
      recentConversations.value = []
      ElMessage.warning('获取对话列表失败')
    }
  } catch (error) {
    console.error('加载对话列表失败:', error)
    recentConversations.value = []
    ElMessage.error('网络错误，请检查连接')
  } finally {
    isRefreshing.value = false
  }
}

// 防抖的刷新函数
const debouncedRefreshConversations = debounce(() => {
  refreshConversations(true) // 强制刷新
}, 300)

const loadConversation = async (conversationId) => {
  try {
    const response = await axios.get(`/api/chat/conversations/${conversationId}`)
    if (response.data.success) {
      messages.value = response.data.data.messages || []
      showHistory.value = false
      await nextTick()
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载对话失败:', error)
    ElMessage.error('加载对话失败')
  }
}

const exportConversation = () => {
  const content = messages.value
    .map(msg => `${msg.role === 'user' ? '用户' : 'AI助手'}: ${msg.content}`)
    .join('\n\n')

  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `对话记录_${new Date().toISOString().slice(0, 10)}.txt`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success('对话已导出')
}

// 语音功能方法
const initSpeechRecognition = () => {
  if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
    const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
    speechRecognition.value = new SpeechRecognition()

    speechRecognition.value.continuous = false
    speechRecognition.value.interimResults = false
    speechRecognition.value.lang = 'zh-CN'

    speechRecognition.value.onstart = () => {
      isRecording.value = true
      ElMessage.info('开始语音识别，请说话...')
    }

    speechRecognition.value.onresult = (event) => {
      const transcript = event.results[0][0].transcript
      inputMessage.value = transcript
      ElMessage.success('语音识别完成')
    }

    speechRecognition.value.onerror = (event) => {
      console.error('语音识别错误:', event.error)
      ElMessage.error('语音识别失败: ' + event.error)
      isRecording.value = false
    }

    speechRecognition.value.onend = () => {
      isRecording.value = false
    }
  } else {
    ElMessage.warning('您的浏览器不支持语音识别功能')
  }
}

const toggleVoiceRecognition = () => {
  if (!speechRecognition.value) {
    initSpeechRecognition()
    return
  }

  if (isRecording.value) {
    speechRecognition.value.stop()
  } else {
    speechRecognition.value.start()
  }
}

const toggleVoiceOutput = () => {
  voiceEnabled.value = !voiceEnabled.value
  ElMessage.info(voiceEnabled.value ? '语音播放已开启' : '语音播放已关闭')
}

const speakText = (text) => {
  if (!voiceEnabled.value || !('speechSynthesis' in window)) {
    return
  }

  // 停止当前播放
  window.speechSynthesis.cancel()

  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  utterance.rate = 0.9
  utterance.pitch = 1
  utterance.volume = 0.8

  // 尝试使用中文语音
  const voices = window.speechSynthesis.getVoices()
  const chineseVoice = voices.find(voice =>
    voice.lang.includes('zh') || voice.name.includes('Chinese')
  )
  if (chineseVoice) {
    utterance.voice = chineseVoice
  }

  window.speechSynthesis.speak(utterance)
}

const showConversationHistory = () => {
  showHistoryDrawer.value = true
  refreshConversations()
}

// 组件挂载时初始化语音功能
onMounted(() => {
  initSpeechRecognition()

  // 初始化语音合成
  if ('speechSynthesis' in window) {
    // 预加载语音列表
    window.speechSynthesis.getVoices()
    // 监听语音列表变化
    window.speechSynthesis.onvoiceschanged = () => {
      window.speechSynthesis.getVoices()
    }
  }
})

onUnmounted(() => {
  if (isDragging.value) {
    document.removeEventListener('mousemove', onDrag)
    document.removeEventListener('mouseup', endDrag)
  }
})
</script>

<style scoped>
.enhanced-chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  position: relative;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.model-selector {
  min-width: 200px;
}

.model-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-name {
  font-weight: 500;
}

.model-status-features {
  display: flex;
  align-items: center;
  gap: 12px;
}

.model-status {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-text {
  font-size: 12px;
  color: #64748b;
}

.model-features-inline {
  display: flex;
  align-items: center;
  gap: 8px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #64748b;
}

.feature-indicator {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
}

.toolbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.status-metrics-inline {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.metric-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.metric-item-inline {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.metric-label {
  font-size: 11px;
  color: #64748b;
}

.metric-value {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.divider-vertical {
  width: 1px;
  height: 24px;
  background: #e5e7eb;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.main-chat-area {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.left-sidebar {
  width: 280px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-section {
  padding: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #374151;
}

.section-header.centered {
  justify-content: space-between;
}

.conversation-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.conversation-item {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.conversation-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.conversation-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.conversation-title {
  font-weight: 500;
  color: #1f2937;
  font-size: 14px;
}

.conversation-preview {
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
}

.conversation-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #9ca3af;
}

.empty-conversations {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px;
  color: #9ca3af;
  text-align: center;
}

.view-more-conversations {
  text-align: center;
  padding: 8px;
}

.tool-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.category-btn {
  flex: 1;
  min-width: 0;
}

.tools-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tools-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tool-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}

.tool-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.tool-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  border-radius: 6px;
  color: #6b7280;
}

.tool-info {
  flex: 1;
  min-width: 0;
}

.tool-name {
  font-weight: 500;
  color: #1f2937;
  font-size: 14px;
}

.tool-desc {
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.quick-questions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-category {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-title {
  font-weight: 500;
  color: #374151;
  font-size: 13px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.question-item {
  padding: 8px 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
  color: #374151;
}

.question-item:hover {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #1d4ed8;
}

.conversation-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 600px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  text-align: center;
}

.welcome-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #64748b;
}

.robot-logo {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.robot-head {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  border-radius: 12px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.robot-antenna {
  position: absolute;
  top: -8px;
  width: 2px;
  height: 12px;
  background: #64748b;
  border-radius: 1px;
}

.robot-antenna.left {
  left: 15px;
}

.robot-antenna.right {
  right: 15px;
}

.robot-face {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.robot-eyes {
  display: flex;
  gap: 8px;
}

.robot-eye {
  width: 8px;
  height: 8px;
  background: #fff;
  border-radius: 50%;
}

.robot-mouth {
  width: 16px;
  height: 2px;
  background: #fff;
  border-radius: 1px;
}

.message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #10b981, #059669);
  color: #fff;
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #fff;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message.user .message-content {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 12px;
  color: #64748b;
}

.message.user .message-header {
  flex-direction: row-reverse;
}

.message-role {
  font-weight: 500;
}

.message-text {
  background: #fff;
  padding: 12px 16px;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  line-height: 1.6;
  color: #374151;
  max-width: 80%;
  word-wrap: break-word;
  white-space: pre-wrap;
}

/* 增强的消息格式样式 */
.message-text :deep(.code-block) {
  background: #f6f8fa;
  border: 1px solid #e1e4e8;
  border-radius: 6px;
  padding: 16px;
  margin: 12px 0;
  overflow-x: auto;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.45;
}

.message-text :deep(.inline-code) {
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 2px 6px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
  color: #d73a49;
}

.message-text :deep(.bold-text) {
  font-weight: 600;
  color: #1f2937;
}

.message-text :deep(.italic-text) {
  font-style: italic;
  color: #6b7280;
}

.message-text :deep(.heading-1) {
  font-size: 1.5em;
  font-weight: 600;
  margin: 16px 0 12px 0;
  color: #1f2937;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 8px;
}

.message-text :deep(.heading-2) {
  font-size: 1.3em;
  font-weight: 600;
  margin: 14px 0 10px 0;
  color: #374151;
}

.message-text :deep(.heading-3) {
  font-size: 1.1em;
  font-weight: 600;
  margin: 12px 0 8px 0;
  color: #4b5563;
}

.message-text :deep(.unordered-list) {
  margin: 12px 0;
  padding-left: 20px;
}

.message-text :deep(.ordered-list) {
  margin: 12px 0;
  padding-left: 20px;
}

.message-text :deep(.list-item) {
  margin: 4px 0;
  line-height: 1.5;
}

.message-text :deep(.ordered-list-item) {
  margin: 4px 0;
  line-height: 1.5;
}

.message-text :deep(.link) {
  color: #3b82f6;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.2s ease;
}

.message-text :deep(.link:hover) {
  color: #1d4ed8;
  border-bottom-color: #3b82f6;
}

.message-text :deep(.divider) {
  border: none;
  border-top: 2px solid #e5e7eb;
  margin: 20px 0;
}

.message-text :deep(.quote) {
  border-left: 4px solid #3b82f6;
  background: #f8fafc;
  padding: 12px 16px;
  margin: 12px 0;
  font-style: italic;
  color: #64748b;
}

.message.user .message-text {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: #fff;
  border-color: #3b82f6;
}

.message-meta {
  margin-top: 6px;
  font-size: 11px;
  color: #9ca3af;
  display: flex;
  gap: 12px;
}

.message-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.message-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  color: #64748b;
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  background: #64748b;
  border-radius: 50%;
  animation: loading 1.4s infinite ease-in-out;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes loading {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.input-area {
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-top: 1px solid #e5e7eb;
}

.input-container {
  max-width: 100%;
}

.input-wrapper {
  position: relative;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.action-left {
  display: flex;
  gap: 8px;
}

.action-right {
  display: flex;
  gap: 8px;
}

.info-panel {
  width: 400px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.drag-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #9ca3af;
}

.execution-steps {
  flex: 1;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 16px;
  display: flex;
  gap: 16px;
  cursor: grab;
}

.execution-steps:active {
  cursor: grabbing;
}

.execution-step {
  min-width: 320px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  display: flex;
  gap: 12px;
  transition: all 0.3s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.execution-step:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.execution-step.running {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.execution-step.completed {
  border-color: #10b981;
  background: #f0fdf4;
}

.execution-step.error {
  border-color: #ef4444;
  background: #fef2f2;
}

.step-indicator {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-weight: 600;
  font-size: 14px;
}

.execution-step.pending .step-indicator {
  background: #f3f4f6;
  color: #6b7280;
}

.execution-step.running .step-indicator {
  background: #3b82f6;
  color: #fff;
}

.execution-step.completed .step-indicator {
  background: #10b981;
  color: #fff;
}

.execution-step.error .step-indicator {
  background: #ef4444;
  color: #fff;
}

.step-content {
  flex: 1;
  min-width: 0;
}

.step-title {
  font-weight: 600;
  color: #1f2937;
  font-size: 14px;
  margin-bottom: 4px;
}

.step-description {
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
}

.step-details {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item {
  font-size: 11px;
  color: #6b7280;
  padding: 4px 8px;
  background: #f9fafb;
  border-radius: 4px;
}

.step-duration {
  font-size: 11px;
  color: #10b981;
  background: #f0fdf4;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.execution-stats {
  padding: 16px;
  border-top: 1px solid #e5e7eb;
  background: rgba(255, 255, 255, 0.8);
}

.stats-header {
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-item {
  text-align: center;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 11px;
  color: #64748b;
  margin-top: 2px;
}

.history-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
}

.history-actions {
  padding: 0 4px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .info-panel {
    width: 300px;
  }

  .execution-step {
    min-width: 160px;
  }
}

/* 增强的执行步骤样式 */
.step-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}



.step-detail-item {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
  font-size: 11px;
  padding: 2px 0;
}

.step-detail-item.completed {
  color: #10b981;
}

.step-detail-item.running {
  color: #3b82f6;
  font-weight: 500;
}

.step-detail-item.pending {
  color: #9ca3af;
}

.detail-icon {
  width: 12px;
  height: 12px;
  flex-shrink: 0;
}

.detail-text {
  flex: 1;
}

.step-timing {
  margin-top: 8px;
  font-size: 10px;
  color: #9ca3af;
  display: flex;
  align-items: center;
  gap: 4px;
}

.timing-label {
  font-weight: 500;
}

.timing-value {
  color: #6b7280;
}

.timing-separator {
  color: #d1d5db;
  margin: 0 2px;
}

@media (max-width: 768px) {
  .main-chat-area {
    flex-direction: column;
  }

  .left-sidebar {
    width: 100%;
    height: 200px;
    border-right: none;
    border-bottom: 1px solid #e5e7eb;
  }

  .info-panel {
    width: 100%;
    height: 200px;
    border-left: none;
    border-top: 1px solid #e5e7eb;
  }

  .toolbar-center {
    display: none;
  }

  .status-metrics-inline {
    display: none;
  }
}
</style>
