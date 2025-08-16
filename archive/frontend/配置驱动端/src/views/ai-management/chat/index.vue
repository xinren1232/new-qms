<template>
  <div class="ai-chat-container">
    <!-- 聊天头部 -->
    <div class="chat-header">
      <div class="header-left">
        <el-icon class="chat-icon"><ChatDotRound /></el-icon>
        <div class="header-info">
          <h3>QMS智能助手</h3>
          <p class="status-text">
            <el-icon class="status-icon" :class="{ 'online': isOnline }">
              <CircleFilled />
            </el-icon>
            {{ isOnline ? 'AI助手在线' : '连接中...' }}
          </p>
        </div>
      </div>
      <div class="header-actions">
        <el-button @click="clearChat" size="small" type="danger" plain>
          <el-icon><Delete /></el-icon>
          清空对话
        </el-button>
        <el-button @click="exportChat" size="small" type="primary" plain>
          <el-icon><Download /></el-icon>
          导出对话
        </el-button>
      </div>
    </div>

    <!-- 聊天内容区域 -->
    <div class="chat-content" ref="chatContentRef">
      <div class="welcome-message" v-if="messages.length === 0">
        <div class="welcome-card">
          <el-icon class="welcome-icon"><Robot /></el-icon>
          <h4>欢迎使用QMS智能助手</h4>
          <p>我可以帮助您解答质量管理相关问题，提供专业建议和数据分析</p>
          <div class="quick-questions">
            <h5>快速提问：</h5>
            <div class="question-tags">
              <el-tag 
                v-for="question in quickQuestions" 
                :key="question"
                @click="sendQuickQuestion(question)"
                class="question-tag"
                type="primary"
                effect="plain"
              >
                {{ question }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="messages-list">
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          class="message-item"
          :class="{ 'user-message': message.role === 'user', 'ai-message': message.role === 'assistant' }"
        >
          <div class="message-avatar">
            <el-avatar v-if="message.role === 'user'" :size="36">
              <el-icon><User /></el-icon>
            </el-avatar>
            <el-avatar v-else :size="36" class="ai-avatar">
              <el-icon><Robot /></el-icon>
            </el-avatar>
          </div>
          
          <div class="message-content">
            <div class="message-header">
              <span class="sender-name">
                {{ message.role === 'user' ? '您' : 'QMS智能助手' }}
              </span>
              <span class="message-time">{{ formatTime(message.timestamp) }}</span>
            </div>
            
            <div class="message-text" v-if="message.content">
              <div v-if="message.role === 'assistant'" class="ai-response">
                <div v-html="formatAIResponse(message.content)"></div>
                <div class="message-actions" v-if="message.role === 'assistant'">
                  <el-button size="small" text @click="copyMessage(message.content)">
                    <el-icon><CopyDocument /></el-icon>
                    复制
                  </el-button>
                  <el-button size="small" text @click="likeMessage(message, true)">
                    <el-icon><Like /></el-icon>
                    有用
                  </el-button>
                  <el-button size="small" text @click="likeMessage(message, false)">
                    <el-icon><DisLike /></el-icon>
                    无用
                  </el-button>
                </div>
              </div>
              <div v-else class="user-message-text">
                {{ message.content }}
              </div>
            </div>
            
            <!-- 加载状态 -->
            <div v-if="message.loading" class="loading-indicator">
              <el-icon class="loading-icon"><Loading /></el-icon>
              <span>AI正在思考中...</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <div class="input-container">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="请输入您的问题..."
          @keydown.ctrl.enter="sendMessage"
          :disabled="isLoading"
          class="message-input"
          resize="none"
        />
        <div class="input-actions">
          <div class="input-tips">
            <span>Ctrl + Enter 发送</span>
          </div>
          <div class="action-buttons">
            <el-button 
              @click="sendMessage" 
              type="primary" 
              :loading="isLoading"
              :disabled="!inputMessage.trim()"
            >
              <el-icon v-if="!isLoading"><Promotion /></el-icon>
              {{ isLoading ? '发送中...' : '发送' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ChatDotRound, 
  Robot, 
  User, 
  Delete, 
  Download, 
  CopyDocument, 
  Like, 
  DisLike, 
  Loading, 
  Promotion,
  CircleFilled
} from '@element-plus/icons-vue'
import { sendChatMessage, getChatHistory } from '@/api/chat'

// 响应式数据
const chatContentRef = ref()
const inputMessage = ref('')
const isLoading = ref(false)
const isOnline = ref(true)
const messages = ref([])

// 快速问题
const quickQuestions = [
  '如何提升产品质量？',
  '质量管理体系如何建立？',
  '缺陷分析方法有哪些？',
  '质量检测标准是什么？',
  '如何进行质量改进？'
]

// 组件挂载
onMounted(async () => {
  await loadChatHistory()
  scrollToBottom()
})

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  nextTick(() => {
    scrollToBottom()
  })
}, { deep: true })

// 加载聊天历史
const loadChatHistory = async () => {
  try {
    const response = await getChatHistory()
    if (response.success && response.data) {
      messages.value = response.data.map(msg => ({
        ...msg,
        timestamp: new Date(msg.timestamp)
      }))
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  const userMessage = {
    role: 'user',
    content: inputMessage.value.trim(),
    timestamp: new Date()
  }
  
  messages.value.push(userMessage)
  
  // 添加AI加载消息
  const aiMessage = {
    role: 'assistant',
    content: '',
    loading: true,
    timestamp: new Date()
  }
  messages.value.push(aiMessage)
  
  const currentInput = inputMessage.value
  inputMessage.value = ''
  isLoading.value = true
  
  try {
    const response = await sendChatMessage({
      message: currentInput,
      conversation_id: generateConversationId()
    })
    
    if (response.success && response.data) {
      // 移除加载状态
      aiMessage.loading = false
      aiMessage.content = response.data.response
      aiMessage.model_info = response.data.model_info
    } else {
      throw new Error(response.message || '发送失败')
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    aiMessage.loading = false
    aiMessage.content = '抱歉，我暂时无法回答您的问题，请稍后再试。'
    aiMessage.error = true
    ElMessage.error('发送失败: ' + error.message)
  } finally {
    isLoading.value = false
  }
}

// 发送快速问题
const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 清空对话
const clearChat = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有对话记录吗？此操作不可恢复。',
      '确认清空',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    messages.value = []
    ElMessage.success('对话已清空')
  } catch {
    // 用户取消
  }
}

// 导出对话
const exportChat = () => {
  if (messages.value.length === 0) {
    ElMessage.warning('暂无对话记录可导出')
    return
  }
  
  const chatData = messages.value.map(msg => ({
    角色: msg.role === 'user' ? '用户' : 'AI助手',
    内容: msg.content,
    时间: formatTime(msg.timestamp)
  }))
  
  const jsonStr = JSON.stringify(chatData, null, 2)
  const blob = new Blob([jsonStr], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  
  const a = document.createElement('a')
  a.href = url
  a.download = `QMS对话记录_${new Date().toISOString().split('T')[0]}.json`
  a.click()
  
  URL.revokeObjectURL(url)
  ElMessage.success('对话记录已导出')
}

// 复制消息
const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

// 点赞/点踩
const likeMessage = (message, isLike) => {
  message.feedback = isLike ? 'like' : 'dislike'
  ElMessage.success(isLike ? '感谢您的反馈' : '我们会继续改进')
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return date.toLocaleString('zh-CN', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化AI响应
const formatAIResponse = (content) => {
  if (!content) return ''
  
  // 简单的markdown格式化
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`(.*?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatContentRef.value) {
    chatContentRef.value.scrollTop = chatContentRef.value.scrollHeight
  }
}

// 生成对话ID
const generateConversationId = () => {
  return 'conv_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}
</script>

<style scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-icon {
  font-size: 24px;
  color: #409eff;
}

.header-info h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.status-text {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-icon {
  font-size: 8px;
  color: #f56c6c;
}

.status-icon.online {
  color: #67c23a;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.welcome-card {
  text-align: center;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  max-width: 500px;
}

.welcome-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

.welcome-card h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.welcome-card p {
  margin: 0 0 24px 0;
  color: #606266;
  line-height: 1.6;
}

.quick-questions h5 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 14px;
}

.question-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.question-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.question-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
}

.message-item {
  display: flex;
  margin-bottom: 24px;
  animation: fadeInUp 0.3s ease;
}

.message-item.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 12px;
}

.ai-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.message-content {
  flex: 1;
  max-width: 70%;
}

.user-message .message-content {
  text-align: right;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #909399;
}

.user-message .message-header {
  flex-direction: row-reverse;
}

.sender-name {
  font-weight: 500;
}

.message-text {
  background: white;
  padding: 12px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  line-height: 1.6;
}

.user-message .message-text {
  background: #409eff;
  color: white;
}

.ai-response {
  color: #303133;
}

.message-actions {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  gap: 8px;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
  font-size: 14px;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

.chat-input-area {
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 16px 20px;
}

.input-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-input {
  resize: none;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.input-tips {
  font-size: 12px;
  color: #909399;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
