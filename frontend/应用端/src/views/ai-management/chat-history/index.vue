<template>
  <div class="chat-history-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <h1>💬 AI问答记录</h1>
        <p>查看和管理您的AI对话历史记录</p>
      </div>
      <div class="header-right">
        <el-button type="success" @click="showExportDialog">
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
        <el-button type="primary" @click="refreshConversations">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计信息卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon conversations">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ userStats.total_conversations || 0 }}</div>
              <div class="stat-label">总对话数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon messages">
              <el-icon><ChatLineRound /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ userStats.total_messages || 0 }}</div>
              <div class="stat-label">总消息数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon rating">
              <el-icon><Star /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ (userStats.avg_rating || 0).toFixed(1) }}</div>
              <div class="stat-label">平均评分</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon time">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ formatLastChatTime(userStats.last_chat_time) }}</div>
              <div class="stat-label">最近对话</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-select v-model="filterModel" placeholder="筛选模型" clearable @change="loadConversations">
            <el-option label="全部模型" value="" />
            <el-option label="GPT-4o" value="GPT-4o" />
            <el-option label="DeepSeek Chat" value="DeepSeek Chat (V3-0324)" />
            <el-option label="DeepSeek Reasoner" value="DeepSeek Reasoner (R1-0528)" />
            <el-option label="Claude 3.7 Sonnet" value="Claude 3.7 Sonnet" />
            <el-option label="Gemini 2.5 Pro" value="Gemini 2.5 Pro Thinking" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadConversations"
          />
        </el-col>
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索对话内容..."
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
      </el-row>
    </div>

    <!-- 对话列表 -->
    <div class="conversations-list">
      <el-card v-if="loading" class="loading-card">
        <div class="loading-content">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在加载对话记录...</span>
        </div>
      </el-card>

      <div v-else-if="filteredConversations.length === 0" class="empty-state">
        <el-empty description="暂无对话记录">
          <el-button type="primary" @click="$router.push('/ai-management/chat')">
            开始新对话
          </el-button>
        </el-empty>
      </div>

      <div v-else class="conversation-cards">
        <el-card 
          v-for="conversation in filteredConversations" 
          :key="conversation.id"
          class="conversation-card"
          @click="viewConversation(conversation)"
        >
          <div class="conversation-header">
            <div class="conversation-title">
              <h3>{{ conversation.title }}</h3>
              <div class="conversation-meta">
                <el-tag :type="getModelTagType(conversation.model_provider)" size="small">
                  {{ conversation.model_provider }}
                </el-tag>
                <span class="message-count">{{ conversation.message_count }} 条消息</span>
                <span class="conversation-time">{{ formatTime(conversation.updated_at) }}</span>
              </div>
            </div>
            <div class="conversation-actions">
              <el-button 
                type="text" 
                size="small" 
                @click.stop="viewConversation(conversation)"
              >
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button 
                type="text" 
                size="small" 
                @click.stop="deleteConversation(conversation)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
          
          <div class="conversation-preview">
            <p>{{ conversation.first_message || '暂无预览' }}</p>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalConversations"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 对话详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="selectedConversation?.title || '对话详情'"
      width="80%"
      top="5vh"
      class="conversation-dialog"
    >
      <div v-if="selectedConversation" class="conversation-detail">
        <div class="detail-header">
          <div class="detail-meta">
            <el-tag :type="getModelTagType(selectedConversation.model_provider)">
              {{ selectedConversation.model_provider }}
            </el-tag>
            <span>{{ selectedConversation.model_name }}</span>
            <span>{{ formatTime(selectedConversation.created_at) }}</span>
          </div>
        </div>
        
        <div class="messages-container">
          <div 
            v-for="message in selectedConversation.messages" 
            :key="message.id"
            :class="['message-item', message.message_type]"
          >
            <div class="message-avatar">
              <el-icon v-if="message.message_type === 'user'"><User /></el-icon>
              <el-icon v-else><Cpu /></el-icon>
            </div>
            <div class="message-content">
              <div class="message-header">
                <span class="message-role">
                  {{ message.message_type === 'user' ? '用户' : 'AI助手' }}
                </span>
                <span class="message-time">{{ formatTime(message.created_at) }}</span>
              </div>
              <div class="message-text">{{ message.content }}</div>
              <div v-if="message.message_type === 'assistant' && message.response_time" class="message-meta">
                <span>响应时间: {{ message.response_time }}ms</span>
                <span v-if="message.token_usage && message.token_usage.total_tokens">
                  Token使用: {{ message.token_usage.total_tokens }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 导出对话框 -->
    <ExportDialog
      v-model:visible="exportDialogVisible"
      :conversations="conversations"
      :total-conversations="totalConversations"
      @export-success="handleExportSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh, ChatDotRound, ChatLineRound, Star, Clock,
  Search, Loading, View, Delete, User, Cpu, Download
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import request from '@/utils/request'
import ExportDialog from '@/components/ExportDialog.vue'

// 认证store
const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const conversations = ref([])
const userStats = ref({})
const filterModel = ref('')
const dateRange = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalConversations = ref(0)
const dialogVisible = ref(false)
const selectedConversation = ref(null)
const exportDialogVisible = ref(false)

// 计算属性
const filteredConversations = computed(() => {
  let filtered = conversations.value

  // 按关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(conv => 
      conv.title.toLowerCase().includes(keyword) ||
      (conv.first_message && conv.first_message.toLowerCase().includes(keyword))
    )
  }

  return filtered
})

// 页面加载时初始化
onMounted(() => {
  loadUserStats()
  loadConversations()
})

// 加载用户统计信息
const loadUserStats = async () => {
  try {
    const result = await request({
      url: '/api/chat/stats',
      method: 'get'
    })
    if (result.success) {
      userStats.value = result.data
    }
  } catch (error) {
    console.error('加载用户统计失败:', error)
  }
}

// 加载对话列表
const loadConversations = async () => {
  loading.value = true
  try {
    const params = {
      limit: pageSize.value,
      offset: (currentPage.value - 1) * pageSize.value
    }

    if (filterModel.value) {
      params.model_provider = filterModel.value
    }

    const result = await request({
      url: '/api/chat/conversations',
      method: 'get',
      params
    })

    if (result.success) {
      conversations.value = result.data.conversations
      totalConversations.value = result.data.total
    } else {
      ElMessage.error('加载对话列表失败')
    }
  } catch (error) {
    console.error('加载对话列表失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 刷新对话列表
const refreshConversations = () => {
  loadUserStats()
  loadConversations()
}

// 查看对话详情
const viewConversation = async (conversation) => {
  try {
    const result = await request({
      url: `/api/chat/conversations/${conversation.id}`,
      method: 'get'
    })

    if (result.success) {
      selectedConversation.value = result.data
      dialogVisible.value = true
    } else {
      ElMessage.error('加载对话详情失败')
    }
  } catch (error) {
    console.error('加载对话详情失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  }
}

// 删除对话
const deleteConversation = async (conversation) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除对话"${conversation.title}"吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const result = await request({
      url: `/api/chat/conversations/${conversation.id}`,
      method: 'delete'
    })

    if (result.success) {
      ElMessage.success('对话删除成功')
      loadConversations()
      loadUserStats()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除对话失败:', error)
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 处理搜索
const handleSearch = () => {
  // 搜索通过计算属性实现，这里可以添加防抖逻辑
}

// 处理分页大小变化
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadConversations()
}

// 处理页码变化
const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
  loadConversations()
}

// 获取当前用户ID - 集成认证系统
const getCurrentUserId = () => {
  const user = authStore.user
  if (user && user.id) {
    return user.id
  }
  // 兼容旧版本，从localStorage获取
  return localStorage.getItem('qms_user_id') || 'anonymous_user'
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return '未知'
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

// 格式化最近对话时间
const formatLastChatTime = (timeString) => {
  if (!timeString) return '无'
  const date = new Date(timeString)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${Math.floor(diff / 86400000)}天前`
}

// 显示导出对话框
const showExportDialog = () => {
  exportDialogVisible.value = true
}

// 处理导出成功
const handleExportSuccess = (exportData) => {
  ElMessage.success(`导出成功！文件: ${exportData.filename}`)
  console.log('导出成功:', exportData)
}

// 获取模型标签类型
const getModelTagType = (modelProvider) => {
  if (modelProvider.includes('DeepSeek')) return 'success'
  if (modelProvider.includes('GPT')) return 'primary'
  if (modelProvider.includes('Claude')) return 'warning'
  if (modelProvider.includes('Gemini')) return 'info'
  return 'default'
}
</script>

<style lang="scss" scoped>
.chat-history-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 120px);
}

// 页面头部
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .header-left {
    h1 {
      margin: 0 0 8px 0;
      color: #303133;
      font-size: 24px;
    }

    p {
      margin: 0;
      color: #606266;
      font-size: 14px;
    }
  }
}

// 统计卡片
.stats-cards {
  margin-bottom: 20px;

  .stat-card {
    background: white;
    border-radius: 8px;
    padding: 20px;
    display: flex;
    align-items: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 16px;
      font-size: 24px;

      &.conversations {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }

      &.messages {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        color: white;
      }

      &.rating {
        background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
        color: #e67e22;
      }

      &.time {
        background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%);
        color: #2c3e50;
      }
    }

    .stat-content {
      .stat-number {
        font-size: 28px;
        font-weight: bold;
        color: #303133;
        line-height: 1;
      }

      .stat-label {
        font-size: 14px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

// 筛选区域
.filter-section {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

// 对话列表
.conversations-list {
  .loading-card {
    text-align: center;
    padding: 40px;

    .loading-content {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      color: #606266;
      font-size: 16px;
    }
  }

  .empty-state {
    background: white;
    border-radius: 8px;
    padding: 60px 20px;
    text-align: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }

  .conversation-cards {
    display: grid;
    gap: 16px;

    .conversation-card {
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
      }

      .conversation-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 12px;

        .conversation-title {
          flex: 1;

          h3 {
            margin: 0 0 8px 0;
            color: #303133;
            font-size: 16px;
            font-weight: 600;
          }

          .conversation-meta {
            display: flex;
            align-items: center;
            gap: 12px;
            font-size: 12px;
            color: #909399;
          }
        }

        .conversation-actions {
          display: flex;
          gap: 8px;
        }
      }

      .conversation-preview {
        p {
          margin: 0;
          color: #606266;
          font-size: 14px;
          line-height: 1.5;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
      }
    }
  }

  .pagination-wrapper {
    margin-top: 20px;
    text-align: center;
  }
}

// 对话详情弹窗
.conversation-dialog {
  .conversation-detail {
    .detail-header {
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #e4e7ed;

      .detail-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 14px;
        color: #606266;
      }
    }

    .messages-container {
      max-height: 60vh;
      overflow-y: auto;

      .message-item {
        display: flex;
        margin-bottom: 20px;

        &.user {
          flex-direction: row-reverse;

          .message-content {
            background: #409eff;
            color: white;
            margin-right: 12px;
            margin-left: 0;
          }
        }

        &.assistant {
          .message-content {
            background: #f5f7fa;
            color: #303133;
            margin-left: 12px;
          }
        }

        .message-avatar {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          background: #e4e7ed;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;

          .el-icon {
            font-size: 20px;
            color: #606266;
          }
        }

        .message-content {
          max-width: 70%;
          border-radius: 12px;
          padding: 12px 16px;

          .message-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            font-size: 12px;
            opacity: 0.8;
          }

          .message-text {
            line-height: 1.6;
            word-break: break-word;
          }

          .message-meta {
            margin-top: 8px;
            font-size: 11px;
            opacity: 0.7;
            display: flex;
            gap: 16px;
          }
        }
      }
    }
  }
}
</style>
