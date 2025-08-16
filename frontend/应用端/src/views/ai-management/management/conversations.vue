<template>
  <div class="conversation-history-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <el-button @click="$router.back()" text>
          <el-icon><ArrowLeft /></el-icon>
          返回聊天
        </el-button>
        <h1>对话历史记录</h1>
      </div>
      <div class="header-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索对话内容..."
          style="width: 300px; margin-right: 16px;"
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="exportAllConversations">
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filters-bar">
      <div class="filter-group">
        <label>时间范围:</label>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="handleDateRangeChange"
        />
      </div>
      <div class="filter-group">
        <label>AI模型:</label>
        <el-select v-model="selectedModel" placeholder="选择模型" clearable @change="handleModelChange">
          <el-option label="全部模型" value="" />
          <el-option
            v-for="model in availableModels"
            :key="model.value"
            :label="model.label"
            :value="model.value"
          />
        </el-select>
      </div>
      <div class="filter-group">
        <label>对话状态:</label>
        <el-select v-model="selectedStatus" placeholder="选择状态" clearable @change="handleStatusChange">
          <el-option label="全部状态" value="" />
          <el-option label="进行中" value="active" />
          <el-option label="已完成" value="completed" />
          <el-option label="已归档" value="archived" />
        </el-select>
      </div>
    </div>

    <!-- 按日期分组的对话列表 -->
    <div class="conversations-container">
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="groupedConversations.length === 0" class="empty-state">
        <el-empty description="暂无对话记录" />
      </div>

      <div v-else class="conversation-groups">
        <div
          v-for="group in groupedConversations"
          :key="group.date"
          class="conversation-group"
        >
          <div class="group-header">
            <h3>{{ formatGroupDate(group.date) }}</h3>
            <span class="conversation-count">{{ group.conversations.length }} 条对话</span>
          </div>
          
          <div class="conversation-list">
            <div
              v-for="conversation in group.conversations"
              :key="conversation.id"
              class="conversation-card"
              @click="openConversation(conversation)"
            >
              <div class="conversation-header">
                <div class="conversation-title">
                  {{ conversation.title || '新对话' }}
                </div>
                <div class="conversation-actions">
                  <el-button size="small" text @click.stop="renameConversation(conversation)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button size="small" text @click.stop="exportConversation(conversation)">
                    <el-icon><Download /></el-icon>
                  </el-button>
                  <el-button size="small" text type="danger" @click.stop="deleteConversation(conversation)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
              
              <div class="conversation-meta">
                <div class="meta-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(conversation.updated_at) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon><ChatDotRound /></el-icon>
                  <span>{{ conversation.message_count || 0 }} 条消息</span>
                </div>
                <div class="meta-item">
                  <el-icon><Monitor /></el-icon>
                  <span>{{ conversation.model || 'GPT-4o' }}</span>
                </div>
                <div class="meta-item">
                  <el-tag
                    :type="getStatusType(conversation.status)"
                    size="small"
                  >
                    {{ getStatusText(conversation.status) }}
                  </el-tag>
                </div>
              </div>
              
              <div class="conversation-preview">
                {{ conversation.first_message || '暂无消息预览' }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
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

    <!-- 重命名对话对话框 -->
    <el-dialog
      v-model="renameDialogVisible"
      title="重命名对话"
      width="400px"
    >
      <el-form>
        <el-form-item label="对话标题">
          <el-input
            v-model="newConversationTitle"
            placeholder="请输入新的对话标题"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRename">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Search,
  Download,
  Edit,
  Delete,
  Clock,
  ChatDotRound,
  Monitor
} from '@element-plus/icons-vue'
import { getConversationList, deleteConversation as deleteConversationAPI, updateConversation } from '@/api/chat'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const searchKeyword = ref('')
const dateRange = ref([])
const selectedModel = ref('')
const selectedStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalConversations = ref(0)
const conversations = ref([])

// 重命名对话
const renameDialogVisible = ref(false)
const currentConversation = ref(null)
const newConversationTitle = ref('')

// 可用模型列表
const availableModels = ref([
  { label: 'GPT-4o', value: 'GPT-4o' },
  { label: 'GPT-4', value: 'GPT-4' },
  { label: 'Claude-3.5', value: 'Claude-3.5' },
  { label: 'DeepSeek', value: 'DeepSeek' },
  { label: 'Qwen', value: 'Qwen' },
  { label: 'GLM-4', value: 'GLM-4' }
])

// 按日期分组的对话
const groupedConversations = computed(() => {
  const groups = {}
  conversations.value.forEach(conversation => {
    const date = conversation.updated_at ? conversation.updated_at.split('T')[0] : new Date().toISOString().split('T')[0]
    if (!groups[date]) {
      groups[date] = []
    }
    groups[date].push(conversation)
  })
  
  return Object.keys(groups)
    .sort((a, b) => new Date(b) - new Date(a))
    .map(date => ({
      date,
      conversations: groups[date].sort((a, b) => new Date(b.updated_at) - new Date(a.updated_at))
    }))
})

// 方法定义
const loadConversations = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      limit: pageSize.value,
      search: searchKeyword.value,
      model: selectedModel.value,
      status: selectedStatus.value,
      start_date: dateRange.value?.[0],
      end_date: dateRange.value?.[1]
    }
    
    const response = await getConversationList(params)
    if (response.success) {
      conversations.value = response.data.conversations || []
      totalConversations.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载对话列表失败:', error)
    ElMessage.error('加载对话列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadConversations()
}

const handleDateRangeChange = () => {
  currentPage.value = 1
  loadConversations()
}

const handleModelChange = () => {
  currentPage.value = 1
  loadConversations()
}

const handleStatusChange = () => {
  currentPage.value = 1
  loadConversations()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadConversations()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadConversations()
}

const formatGroupDate = (dateStr) => {
  const date = new Date(dateStr)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)
  
  if (dateStr === today.toISOString().split('T')[0]) {
    return '今天'
  } else if (dateStr === yesterday.toISOString().split('T')[0]) {
    return '昨天'
  } else {
    return date.toLocaleDateString('zh-CN', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    })
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  const statusMap = {
    active: 'success',
    completed: 'info',
    archived: 'warning'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    active: '进行中',
    completed: '已完成',
    archived: '已归档'
  }
  return statusMap[status] || '未知'
}

const openConversation = (conversation) => {
  router.push({
    path: '/ai-management/chat',
    query: { conversationId: conversation.id }
  })
}

const renameConversation = (conversation) => {
  currentConversation.value = conversation
  newConversationTitle.value = conversation.title || ''
  renameDialogVisible.value = true
}

const confirmRename = async () => {
  try {
    await updateConversation(currentConversation.value.id, {
      title: newConversationTitle.value
    })
    currentConversation.value.title = newConversationTitle.value
    ElMessage.success('重命名成功')
    renameDialogVisible.value = false
  } catch (error) {
    ElMessage.error('重命名失败')
  }
}

const deleteConversation = (conversation) => {
  ElMessageBox.confirm(
    `确定要删除对话"${conversation.title || '新对话'}"吗？此操作不可恢复。`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteConversationAPI(conversation.id)
      ElMessage.success('删除成功')
      loadConversations()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

const exportConversation = (conversation) => {
  ElMessage.info('导出功能开发中...')
}

const exportAllConversations = () => {
  ElMessage.info('批量导出功能开发中...')
}

// 生命周期
onMounted(() => {
  loadConversations()
})
</script>

<style scoped>
.conversation-history-page {
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px 24px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.filters-bar {
  display: flex;
  gap: 24px;
  align-items: center;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-group label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.conversations-container {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  padding: 24px;
  margin-bottom: 24px;
  min-height: 400px;
}

.loading-container {
  padding: 20px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

.conversation-groups {
  space-y: 24px;
}

.conversation-group {
  margin-bottom: 32px;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e4e7ed;
}

.group-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.conversation-count {
  color: #909399;
  font-size: 14px;
}

.conversation-list {
  display: grid;
  gap: 16px;
}

.conversation-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.conversation-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  transform: translateY(-2px);
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.conversation-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.conversation-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.conversation-card:hover .conversation-actions {
  opacity: 1;
}

.conversation-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 12px;
}

.conversation-preview {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.pagination-container {
  display: flex;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

@media (max-width: 768px) {
  .conversation-history-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .header-right {
    justify-content: space-between;
  }

  .filters-bar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .filter-group {
    flex-direction: column;
    align-items: stretch;
    gap: 4px;
  }

  .conversation-meta {
    gap: 8px;
  }
}
</style>
