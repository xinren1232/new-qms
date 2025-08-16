<template>
  <div class="advanced-chat-container">
    <!-- 左侧面板 -->
    <div class="left-panel">
      <!-- 模型管理 -->
      <div class="model-management">
        <div class="panel-header">
          <el-icon><Cpu /></el-icon>
          <span>模型管理</span>
        </div>
        <div class="model-selector">
          <el-select 
            v-model="currentModel" 
            @change="switchModel"
            placeholder="选择AI模型"
            style="width: 100%"
          >
            <el-option
              v-for="model in availableModels"
              :key="model.provider"
              :label="getModelDisplayName(model)"
              :value="model.provider"
            >
              <div class="model-option">
                <span class="model-name">{{ model.name }}</span>
                <span class="model-version">{{ model.model }}</span>
                <el-tag
                  v-if="model.features && model.features.external"
                  type="success"
                  size="small"
                  class="external-tag"
                >
                  外网可用
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </div>
        <div class="model-status">
          <el-tag :type="modelStatus.status === 'online' ? 'success' : 'danger'" size="small">
            {{ modelStatus.status === 'online' ? '在线' : '离线' }}
          </el-tag>
          <span class="status-text">{{ modelStatus.provider }}</span>
        </div>

        <!-- 模型特性指标 -->
        <div class="model-features">
          <div class="feature-item">
            <div class="feature-indicator" :class="{ active: currentModelFeatures.reasoning }"></div>
            <span class="feature-text">推理能力</span>
          </div>
          <div class="feature-item">
            <div class="feature-indicator" :class="{ active: currentModelFeatures.toolCalls }"></div>
            <span class="feature-text">工具调用</span>
          </div>
          <div class="feature-item">
            <div class="feature-indicator" :class="{ active: currentModelFeatures.vision }"></div>
            <span class="feature-text">图文识别</span>
          </div>
        </div>
      </div>

      <!-- 对话记录 -->
      <div class="conversation-history">
        <div class="panel-header">
          <el-icon><ChatDotRound /></el-icon>
          <span>对话记录</span>
          <el-button @click="clearHistory" size="small" text type="danger">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        <div class="history-list">
          <div 
            v-for="conversation in conversationList" 
            :key="conversation.id"
            class="history-item"
            :class="{ active: conversation.id === currentConversationId }"
            @click="loadConversation(conversation.id)"
          >
            <div class="conversation-title">{{ conversation.title }}</div>
            <div class="conversation-time">{{ formatTime(conversation.lastTime) }}</div>
            <div class="conversation-preview">{{ conversation.preview }}</div>
          </div>
        </div>
      </div>

      <!-- 功能展示 -->
      <div class="feature-showcase">
        <div class="panel-header">
          <el-icon><Tools /></el-icon>
          <span>功能展示</span>
        </div>
        <div class="feature-list">
          <div class="feature-item" @click="sendQuickQuestion('请介绍ISO 9001质量管理体系')">
            <el-icon><Document /></el-icon>
            <span>质量体系咨询</span>
          </div>
          <div class="feature-item" @click="sendQuickQuestion('如何进行SPC统计过程控制？')">
            <el-icon><TrendCharts /></el-icon>
            <span>SPC分析</span>
          </div>
          <div class="feature-item" @click="sendQuickQuestion('请分析产品缺陷的根本原因')">
            <el-icon><Warning /></el-icon>
            <span>缺陷分析</span>
          </div>
          <div class="feature-item" @click="sendQuickQuestion('FMEA失效模式分析怎么做？')">
            <el-icon><DataAnalysis /></el-icon>
            <span>FMEA分析</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 中间问答区域 -->
    <div class="chat-area">
      <div class="chat-header">
        <h3>AI智能问答</h3>
        <div class="chat-actions">
          <el-button @click="exportChat" size="small" text>
            <el-icon><Download /></el-icon>
            导出对话
          </el-button>
          <el-button @click="newConversation" size="small" text type="primary">
            <el-icon><Plus /></el-icon>
            新对话
          </el-button>
        </div>
      </div>

      <div class="messages-container" ref="messagesContainer">
        <div v-if="messages.length === 0" class="welcome-message">
          <el-icon><ChatDotRound /></el-icon>
          <h4>欢迎使用QMS AI智能助手</h4>
          <p>我是您的质量管理专家，可以帮助您解决质量管理相关问题</p>
        </div>

        <div 
          v-for="(message, index) in messages" 
          :key="index" 
          class="message-item"
          :class="message.role"
        >
          <div class="message-avatar">
            <el-avatar v-if="message.role === 'user'" :size="32">
              <el-icon><User /></el-icon>
            </el-avatar>
            <el-avatar v-else :size="32" style="background: linear-gradient(135deg, #409EFF, #66b1ff)">
              <el-icon><Cpu /></el-icon>
            </el-avatar>
          </div>
          
          <div class="message-content">
            <div class="message-header">
              <span class="sender-name">{{ message.role === 'user' ? '您' : 'AI助手' }}</span>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
            
            <div class="message-text" v-html="formatMessage(message.content)"></div>
            
            <div v-if="message.role === 'assistant' && message.model_info" class="message-meta">
              <el-tag size="small">{{ message.model_info.provider }}</el-tag>
              <span class="response-time">{{ message.response_time }}ms</span>
            </div>

            <!-- 点赞点踩功能 -->
            <div v-if="message.role === 'assistant' && !message.loading" class="message-actions">
              <el-button
                size="small"
                text
                :type="message.feedback === 'like' ? 'success' : ''"
                @click="handleFeedback(message, 'like')"
                :disabled="message.feedback && message.feedback !== 'like'"
              >
                <el-icon><Like /></el-icon>
                有帮助
              </el-button>
              <el-button
                size="small"
                text
                :type="message.feedback === 'dislike' ? 'danger' : ''"
                @click="handleFeedback(message, 'dislike')"
                :disabled="message.feedback && message.feedback !== 'dislike'"
              >
                <el-icon><Delete /></el-icon>
                没帮助
              </el-button>
              <el-button size="small" text @click="copyMessage(message.content)">
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
            </div>

            <div v-if="message.loading" class="message-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
              <span>AI正在思考中...</span>
            </div>

            <!-- 评分组件 - 只对AI消息显示 -->
            <MessageRating
              v-if="message.role === 'assistant' && !message.loading && message.id"
              :message-id="message.id"
              :rating="message.rating"
              :feedback="message.ratingFeedback"
              :conversation-id="currentConversationId"
              @rated="handleMessageRated"
              @rating-updated="handleRatingUpdated"
            />
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题..."
            @keydown.ctrl.enter="sendMessage"
            :disabled="isLoading"
          />
          <div class="input-actions">
            <el-button @click="sendMessage" type="primary" :loading="isLoading">
              <el-icon><Promotion /></el-icon>
              发送 (Ctrl+Enter)
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧执行和思考过程 -->
    <div class="right-panel">
      <div class="thinking-process">
        <div class="panel-header">
          <el-icon><View /></el-icon>
          <span>思考过程</span>
        </div>
        <div class="process-content">
          <div v-if="currentThinking.length === 0" class="no-thinking">
            <el-icon><BrainFilled /></el-icon>
            <p>AI思考过程将在这里显示</p>
          </div>
          
          <div v-for="(step, index) in currentThinking" :key="index" class="thinking-step">
            <div class="step-header">
              <el-icon><Right /></el-icon>
              <span>步骤 {{ index + 1 }}</span>
            </div>
            <div class="step-content">{{ step.content }}</div>
            <div class="step-time">{{ step.timestamp }}</div>
          </div>
        </div>
      </div>

      <div class="execution-details">
        <div class="panel-header">
          <el-icon><Monitor /></el-icon>
          <span>执行详情</span>
        </div>
        <div class="details-content">
          <div class="detail-item">
            <label>当前模型:</label>
            <span>{{ modelStatus.provider }}</span>
          </div>
          <div class="detail-item">
            <label>模型版本:</label>
            <span>{{ modelStatus.model }}</span>
          </div>
          <div class="detail-item">
            <label>响应时间:</label>
            <span>{{ lastResponseTime }}ms</span>
          </div>
          <div class="detail-item">
            <label>Token使用:</label>
            <span>{{ tokenUsage.total || 0 }}</span>
          </div>
          <div class="detail-item">
            <label>对话轮次:</label>
            <span>{{ messages.length / 2 || 0 }}</span>
          </div>
        </div>
      </div>

      <div class="performance-monitor">
        <div class="panel-header">
          <el-icon><DataLine /></el-icon>
          <span>性能监控</span>
        </div>
        <div class="monitor-content">
          <div class="performance-chart">
            <!-- 这里可以添加图表组件 -->
            <div class="chart-placeholder">
              <el-icon><TrendCharts /></el-icon>
              <p>响应时间趋势图</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { rateMessage } from '@/api/chat'
import { useAuthStore } from '@/stores/auth'
import MessageRating from '@/components/MessageRating.vue'

// 认证store
const authStore = useAuthStore()

// 响应式数据
const currentModel = ref('transsion')
const availableModels = ref([])
const modelStatus = ref({})
const inputMessage = ref('')
const isLoading = ref(false)
const messages = ref([])
const currentConversationId = ref(null)
const conversationList = ref([])
const currentThinking = ref([])
const lastResponseTime = ref(0)
const tokenUsage = ref({})

// 模型特性配置
const modelFeaturesConfig = {
  'gpt-4o': {
    reasoning: false,
    toolCalls: true,
    vision: true
  },
  'o3': {
    reasoning: true,
    toolCalls: true,
    vision: true
  },
  'gemini-2.5-pro-thinking': {
    reasoning: true,
    toolCalls: true,
    vision: true
  },
  'claude-3-7-sonnet': {
    reasoning: false,
    toolCalls: true,
    vision: true
  },
  'deepseek-v3': {
    reasoning: false,
    toolCalls: false,
    vision: false
  },
  'deepseek-r1': {
    reasoning: true,
    toolCalls: true,
    vision: false
  },
  'deepseek-chat': {
    reasoning: false,
    toolCalls: true,
    vision: false,
    external: true
  },
  'deepseek-reasoner': {
    reasoning: true,
    toolCalls: true,
    vision: false,
    external: true
  }
}

// 当前模型特性
const currentModelFeatures = ref({
  reasoning: false,
  toolCalls: false,
  vision: false
})

// DOM引用
const messagesContainer = ref(null)

// 初始化
onMounted(async () => {
  await loadAvailableModels()
  await loadModelStatus()
  await loadConversationHistory()
  generateNewConversationId()
})

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  nextTick(() => {
    scrollToBottom()
  })
}, { deep: true })

// 加载可用模型
const loadAvailableModels = async () => {
  try {
    const response = await fetch('/api/chat/models')
    const result = await response.json()
    if (result.success) {
      // 处理模型数据结构
      availableModels.value = result.data.models || result.data
      // 开发环境下显示模型列表
      if (process.env.NODE_ENV === 'development') {
        console.log('加载的模型列表:', availableModels.value)
      }
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
    ElMessage.error('加载模型列表失败')
  }
}

// 获取模型显示名称
const getModelDisplayName = (model) => {
  if (model.features && model.features.external) {
    return `${model.name} (外网可用)`
  }
  return model.name
}

// 更新模型特性
const updateModelFeatures = (providerKey) => {
  const features = modelFeaturesConfig[providerKey]
  if (features) {
    currentModelFeatures.value = { ...features }
  } else {
    // 默认特性
    currentModelFeatures.value = {
      reasoning: false,
      toolCalls: false,
      vision: false
    }
  }
}

// 加载模型状态
const loadModelStatus = async () => {
  try {
    const response = await fetch('/api/chat/model-status')
    const result = await response.json()
    if (result.success) {
      modelStatus.value = result.data
      // 根据模型名称找到对应的provider key
      const modelData = availableModels.value.find(m => m.name === result.data.provider)
      if (modelData) {
        currentModel.value = modelData.provider
        // 更新模型特性
        updateModelFeatures(modelData.provider)
      }
    }
  } catch (error) {
    console.error('加载模型状态失败:', error)
  }
}

// 切换模型
const switchModel = async (provider) => {
  try {
    const response = await fetch('/api/chat/switch-model', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ provider })
    })
    const result = await response.json()
    if (result.success) {
      // 更新模型状态
      modelStatus.value = {
        status: 'online',
        provider: result.data.provider,
        model: result.data.current_model,
        api_status: 'connected'
      }
      currentModel.value = provider
      // 更新模型特性
      updateModelFeatures(provider)
      ElMessage.success(`已切换到 ${result.data.provider}`)

      // 添加思考过程
      addThinkingStep(`切换到模型: ${result.data.provider}`)
    } else {
      ElMessage.error('模型切换失败')
    }
  } catch (error) {
    console.error('模型切换失败:', error)
    ElMessage.error('模型切换失败')
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = {
    role: 'user',
    content: inputMessage.value,
    timestamp: new Date()
  }

  messages.value.push(userMessage)

  // 添加加载中的AI消息
  const loadingMessage = {
    role: 'assistant',
    content: '',
    loading: true,
    timestamp: new Date()
  }
  messages.value.push(loadingMessage)

  const messageToSend = inputMessage.value
  inputMessage.value = ''
  isLoading.value = true

  // 添加思考过程
  addThinkingStep('接收用户问题')
  addThinkingStep('分析问题类型和上下文')
  addThinkingStep('调用AI模型生成回答')

  try {
    const startTime = Date.now()
    const response = await fetch('/api/chat/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        message: messageToSend,
        conversation_id: currentConversationId.value,
        user_id: getCurrentUserId() || 'anonymous', // 确保用户ID传递
        user_info: {
          id: getCurrentUserId(),
          username: getCurrentUsername(),
          email: getCurrentUserEmail(),
          department: getCurrentUserDepartment(),
          role: getCurrentUserRole()
        }
      })
    })

    const result = await response.json()
    const endTime = Date.now()
    const responseTime = endTime - startTime

    // 移除加载消息
    messages.value.pop()

    if (result.success) {
      const aiMessage = {
        id: result.data.message_id || generateMessageId(),
        role: 'assistant',
        content: result.data.response,
        timestamp: new Date(),
        model_info: {
          provider: modelStatus.value.name,
          model: modelStatus.value.model
        },
        response_time: responseTime,
        rating: null,
        ratingFeedback: null
      }

      messages.value.push(aiMessage)
      lastResponseTime.value = responseTime

      // 更新token使用情况
      if (result.data.usage) {
        tokenUsage.value = result.data.usage
      }

      // 添加思考过程
      addThinkingStep(`生成回答完成 (${responseTime}ms)`)

      // 更新对话记录
      updateConversationHistory(userMessage, aiMessage)

    } else {
      ElMessage.error('发送失败: ' + result.message)
      addThinkingStep('发送失败: ' + result.message)
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送消息失败')

    // 移除加载消息
    messages.value.pop()
    addThinkingStep('发送失败: 网络错误')
  } finally {
    isLoading.value = false
  }
}

// 快速发送问题
const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 添加思考步骤
const addThinkingStep = (content) => {
  currentThinking.value.push({
    content,
    timestamp: new Date().toLocaleTimeString()
  })

  // 保持最多10个步骤
  if (currentThinking.value.length > 10) {
    currentThinking.value.shift()
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleTimeString()
}

// 格式化消息内容
const formatMessage = (content) => {
  if (!content) return ''

  // 简单的markdown格式化
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}

// 生成新对话ID
const generateNewConversationId = () => {
  currentConversationId.value = 'conv_' + Date.now() + '_' + Math.random().toString(36).substring(2, 11)
}

// 新建对话
const newConversation = () => {
  messages.value = []
  currentThinking.value = []
  generateNewConversationId()
  ElMessage.success('已创建新对话')
}

// 加载对话历史
const loadConversationHistory = async () => {
  try {
    console.log('🔄 开始加载对话历史...')

    // 使用API服务而不是直接fetch
    const { getConversationList } = await import('@/api/chat')
    const result = await getConversationList({
      limit: 20,
      offset: 0
    })

    console.log('📡 API响应:', result)

    if (result.success && result.data && Array.isArray(result.data)) {
      conversationList.value = result.data.map(conv => ({
        id: conv.id,
        title: conv.title || '新对话',
        lastTime: new Date(conv.updated_at || conv.created_at),
        preview: conv.title || '新对话',
        messageCount: conv.message_count || 0
      }))
      console.log('✅ 对话历史加载成功:', conversationList.value.length, '个对话')

      // 如果没有历史对话，显示提示
      if (conversationList.value.length === 0) {
        console.log('📝 暂无历史对话记录')
      }
    } else {
      console.warn('⚠️ 对话历史加载失败，原因:', result.message || '未知错误')
      console.warn('⚠️ 使用模拟数据作为降级方案')
      loadMockConversations()
    }
  } catch (error) {
    console.error('❌ 加载对话历史异常:', error)
    ElMessage.warning('对话历史加载失败: ' + (error.message || '网络错误'))
    loadMockConversations()
  }
}

// 加载模拟对话数据
const loadMockConversations = () => {
  conversationList.value = [
    {
      id: 'conv_1',
      title: 'ISO 9001咨询',
      lastTime: new Date(Date.now() - 3600000),
      preview: '请介绍ISO 9001质量管理体系的核心要素...'
    },
    {
      id: 'conv_2',
      title: 'SPC分析讨论',
      lastTime: new Date(Date.now() - 7200000),
      preview: '如何进行SPC统计过程控制？'
    },
    {
      id: 'conv_3',
      title: '缺陷分析方法',
      lastTime: new Date(Date.now() - 10800000),
      preview: '请分析产品缺陷的根本原因分析方法...'
    }
  ]
}

// 加载指定对话
const loadConversation = async (conversationId) => {
  try {
    console.log('🔄 开始加载对话:', conversationId)

    // 设置当前对话ID
    currentConversationId.value = conversationId

    // 清空当前消息和思考过程
    messages.value = []
    currentThinking.value = []

    // 显示加载状态
    const loadingMessage = ElMessage.info({
      message: '正在加载对话历史...',
      duration: 0
    })

    // 使用getChatHistory API获取对话消息
    const { getChatHistory } = await import('@/api/chat')
    const result = await getChatHistory({
      conversation_id: conversationId,
      limit: 100
    })

    console.log('📡 对话消息API响应:', result)

    if (result.success && result.data && Array.isArray(result.data)) {
      // 转换消息格式
      const loadedMessages = result.data.map(msg => ({
        id: msg.id,
        role: msg.role || msg.message_type,
        content: msg.content,
        timestamp: msg.timestamp || msg.created_at,
        model_info: msg.model_info,
        response_time: msg.response_time,
        rating: msg.rating,
        feedback: msg.feedback
      }))

      // 按时间排序
      loadedMessages.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp))

      // 更新消息列表
      messages.value = loadedMessages

      console.log('✅ 对话加载成功:', loadedMessages.length, '条消息')

      // 关闭加载提示，显示成功消息
      loadingMessage.close()
      ElMessage.success(`已加载对话历史 (${loadedMessages.length}条消息)`)

      // 滚动到底部
      nextTick(() => {
        scrollToBottom()
      })

    } else {
      console.warn('⚠️ 对话消息加载失败:', result.message || '未知错误')
      loadingMessage.close()
      ElMessage.warning('对话历史加载失败: ' + (result.message || '数据格式错误'))
    }

  } catch (error) {
    console.error('❌ 加载对话异常:', error)
    ElMessage.error('对话历史加载失败: ' + (error.message || '网络错误'))
  }
}

// 更新对话历史
const updateConversationHistory = (userMessage) => {
  const existingConv = conversationList.value.find(conv => conv.id === currentConversationId.value)

  if (existingConv) {
    existingConv.lastTime = new Date()
    existingConv.preview = userMessage.content.substring(0, 30) + '...'
  } else {
    conversationList.value.unshift({
      id: currentConversationId.value,
      title: userMessage.content.substring(0, 20) + '...',
      lastTime: new Date(),
      preview: userMessage.content.substring(0, 30) + '...'
    })
  }
}

// 清空历史记录
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有对话记录吗？', '确认删除', {
      type: 'warning'
    })

    conversationList.value = []
    messages.value = []
    currentThinking.value = []
    ElMessage.success('已清空对话记录')
  } catch {
    // 用户取消
  }
}

// 导出对话
const exportChat = () => {
  if (messages.value.length === 0) {
    ElMessage.warning('当前没有对话内容可导出')
    return
  }

  const chatContent = messages.value.map(msg => {
    const role = msg.role === 'user' ? '用户' : 'AI助手'
    const time = formatTime(msg.timestamp)
    return `[${time}] ${role}: ${msg.content}`
  }).join('\n\n')

  const blob = new Blob([chatContent], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `AI对话记录_${new Date().toLocaleDateString()}.txt`
  a.click()
  URL.revokeObjectURL(url)

  ElMessage.success('对话记录已导出')
}

// 处理反馈
const handleFeedback = async (message, feedbackType) => {
  try {
    // 更新本地状态
    message.feedback = feedbackType

    // 发送反馈到后端
    await rateMessage({
      message_id: message.id || generateMessageId(),
      feedback: feedbackType,
      feedback_score: feedbackType === 'like' ? 5 : 1,
      feedback_reason: feedbackType === 'like' ? 'HELPFUL' : 'UNHELPFUL',
      comment: ''
    })

    ElMessage.success(feedbackType === 'like' ? '感谢您的反馈！' : '我们会继续改进')
  } catch (error) {
    console.error('发送反馈失败:', error)
    ElMessage.error('反馈提交失败，请稍后重试')
    // 回滚本地状态
    message.feedback = null
  }
}

// 复制消息内容
const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('内容已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动选择复制')
  }
}

// 生成消息ID
const generateMessageId = () => {
  return 'msg_' + Date.now() + '_' + Math.random().toString(36).substring(2, 11)
}

// 获取当前用户信息 - 集成认证系统
const getCurrentUserId = () => {
  const user = authStore.user
  if (user && user.id) {
    return user.id
  }
  // 兼容旧版本，从localStorage获取
  return localStorage.getItem('qms_user_id') || 'anonymous_' + Date.now()
}

const getCurrentUsername = () => {
  const user = authStore.user
  if (user && user.username) {
    return user.username
  }
  return localStorage.getItem('qms_username') || '匿名用户'
}

const getCurrentUserEmail = () => {
  const user = authStore.user
  if (user && user.email) {
    return user.email
  }
  return localStorage.getItem('qms_user_email') || 'anonymous@qms.com'
}

const getCurrentUserDepartment = () => {
  const user = authStore.user
  if (user && user.department) {
    return user.department
  }
  return localStorage.getItem('qms_user_department') || '未知部门'
}

const getCurrentUserRole = () => {
  const user = authStore.user
  if (user && user.role) {
    return user.role
  }
  return localStorage.getItem('qms_user_role') || 'USER'
}

// 处理消息评分
const handleMessageRated = (ratingData) => {
  console.log('消息已评分:', ratingData)

  // 更新本地消息的评分信息
  const message = messages.value.find(msg => msg.id === ratingData.messageId)
  if (message) {
    message.rating = ratingData.rating
    message.ratingFeedback = ratingData.feedback
  }

  ElMessage.success('感谢您的评分！')
}

// 处理评分更新
const handleRatingUpdated = (ratingData) => {
  console.log('评分已更新:', ratingData)

  // 更新本地消息的评分信息
  const message = messages.value.find(msg => msg.id === ratingData.messageId)
  if (message) {
    message.rating = ratingData.rating
    message.ratingFeedback = ratingData.feedback
  }

  ElMessage.success('评分更新成功！')
}
</script>

<style lang="scss" scoped>
.advanced-chat-container {
  display: flex;
  height: calc(100vh - 120px);
  gap: 16px;
  padding: 20px;
  background: #f5f7fa;
}

// 左侧面板样式
.left-panel {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  color: #303133;
}

.model-management {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .model-selector {
    padding: 16px;
  }
  
  .model-option {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .model-name {
      font-weight: 500;
    }

    .model-version {
      font-size: 12px;
      color: #909399;
    }

    .external-tag {
      align-self: flex-start;
      margin-top: 2px;
      font-size: 10px;
      padding: 2px 6px;
      background: linear-gradient(135deg, #67c23a, #85ce61);
      border: none;
      color: white;
      border-radius: 10px;
      font-weight: 500;
    }
  }
  
  .model-status {
    padding: 12px 16px;
    border-top: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    gap: 8px;

    .status-text {
      font-size: 12px;
      color: #606266;
    }
  }

  .model-features {
    padding: 8px 16px 12px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    flex-direction: column;
    gap: 6px;

    .feature-item {
      display: flex;
      align-items: center;
      gap: 8px;

      .feature-indicator {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background-color: #dcdfe6;
        transition: background-color 0.3s ease;

        &.active {
          background-color: #67c23a;
          box-shadow: 0 0 4px rgba(103, 194, 58, 0.4);
        }
      }

      .feature-text {
        font-size: 11px;
        color: #909399;
        user-select: none;
      }
    }
  }
}

.conversation-history {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  
  .history-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
  }
  
  .history-item {
    padding: 12px;
    border-radius: 6px;
    cursor: pointer;
    margin-bottom: 8px;
    transition: all 0.2s;
    
    &:hover {
      background: #f5f7fa;
    }
    
    &.active {
      background: #e6f7ff;
      border: 1px solid #409eff;
    }
    
    .conversation-title {
      font-weight: 500;
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .conversation-time {
      font-size: 12px;
      color: #909399;
      margin-bottom: 4px;
    }
    
    .conversation-preview {
      font-size: 12px;
      color: #606266;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
}

.feature-showcase {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .feature-list {
    padding: 16px;
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }
  
  .feature-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 16px 8px;
    border: 1px solid #e4e7ed;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s;
    text-align: center;
    
    &:hover {
      border-color: #409eff;
      background: #f0f9ff;
    }
    
    .el-icon {
      font-size: 24px;
      color: #409eff;
      margin-bottom: 8px;
    }
    
    span {
      font-size: 12px;
      color: #606266;
    }
  }
}

// 中间聊天区域样式
.chat-area {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e4e7ed;
  
  h3 {
    margin: 0;
    color: #303133;
  }
  
  .chat-actions {
    display: flex;
    gap: 8px;
  }
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome-message {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
  
  .el-icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
  
  h4 {
    margin: 0 0 8px 0;
    color: #606266;
  }
  
  p {
    margin: 0;
  }
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  
  &.assistant {
    .message-content {
      background: #f0f9ff;
      border: 1px solid #e6f7ff;
    }
  }
  
  .message-content {
    flex: 1;
    background: #f5f7fa;
    border-radius: 8px;
    padding: 12px 16px;
    
    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      .sender-name {
        font-weight: 500;
        color: #303133;
      }
      
      .message-time {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .message-text {
      line-height: 1.6;
      color: #606266;
      white-space: pre-wrap;
    }
    
    .message-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 8px;
      
      .response-time {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .message-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 8px;
      padding-top: 8px;
      border-top: 1px solid #f0f0f0;

      .el-button {
        font-size: 12px;
        padding: 4px 8px;

        &.is-disabled {
          opacity: 0.5;
        }
      }
    }

    .message-loading {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #409eff;
      font-size: 14px;
    }
  }
}

.input-area {
  padding: 20px;
  border-top: 1px solid #e4e7ed;
  
  .input-container {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .input-actions {
    display: flex;
    justify-content: flex-end;
  }
}

// 右侧面板样式
.right-panel {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.thinking-process {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  
  .process-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
  }
  
  .no-thinking {
    text-align: center;
    padding: 40px 20px;
    color: #909399;
    
    .el-icon {
      font-size: 32px;
      margin-bottom: 12px;
    }
  }
  
  .thinking-step {
    margin-bottom: 16px;
    padding: 12px;
    background: #f8f9fa;
    border-radius: 6px;
    border-left: 3px solid #409eff;
    
    .step-header {
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 500;
      color: #303133;
      margin-bottom: 8px;
    }
    
    .step-content {
      color: #606266;
      font-size: 14px;
      line-height: 1.5;
      margin-bottom: 6px;
    }
    
    .step-time {
      font-size: 12px;
      color: #909399;
    }
  }
}

.execution-details {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .details-content {
    padding: 16px;
  }
  
  .detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    label {
      font-size: 14px;
      color: #606266;
    }
    
    span {
      font-size: 14px;
      color: #303133;
      font-weight: 500;
    }
  }
}

.performance-monitor {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  
  .monitor-content {
    padding: 16px;
  }
  
  .chart-placeholder {
    text-align: center;
    padding: 30px 20px;
    color: #909399;
    border: 2px dashed #e4e7ed;
    border-radius: 6px;
    
    .el-icon {
      font-size: 24px;
      margin-bottom: 8px;
    }
    
    p {
      margin: 0;
      font-size: 12px;
    }
  }
}
</style>
