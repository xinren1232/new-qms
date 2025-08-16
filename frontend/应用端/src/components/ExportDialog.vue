<template>
  <el-dialog
    v-model="dialogVisible"
    title="导出对话记录"
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="export-dialog">
      <!-- 导出格式选择 -->
      <div class="form-section">
        <h4>📄 选择导出格式</h4>
        <el-radio-group v-model="exportFormat" class="format-group">
          <el-radio value="pdf" class="format-option">
            <div class="format-content">
              <el-icon><Document /></el-icon>
              <div class="format-info">
                <div class="format-name">PDF文档</div>
                <div class="format-desc">适合打印和阅读，保持格式完整</div>
              </div>
            </div>
          </el-radio>
          
          <el-radio value="word" class="format-option">
            <div class="format-content">
              <el-icon><Edit /></el-icon>
              <div class="format-info">
                <div class="format-name">Word文档</div>
                <div class="format-desc">可编辑的文档格式，支持进一步修改</div>
              </div>
            </div>
          </el-radio>
          
          <el-radio value="excel" class="format-option">
            <div class="format-content">
              <el-icon><Grid /></el-icon>
              <div class="format-info">
                <div class="format-name">Excel表格</div>
                <div class="format-desc">结构化数据，便于统计分析</div>
              </div>
            </div>
          </el-radio>
          
          <el-radio value="markdown" class="format-option">
            <div class="format-content">
              <el-icon><Memo /></el-icon>
              <div class="format-info">
                <div class="format-name">Markdown</div>
                <div class="format-desc">纯文本格式，兼容性好</div>
              </div>
            </div>
          </el-radio>
          
          <el-radio value="json" class="format-option">
            <div class="format-content">
              <el-icon><DataBoard /></el-icon>
              <div class="format-info">
                <div class="format-name">JSON数据</div>
                <div class="format-desc">结构化数据，便于程序处理</div>
              </div>
            </div>
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 导出范围选择 -->
      <div class="form-section">
        <h4>📋 选择导出范围</h4>
        <el-radio-group v-model="exportScope">
          <el-radio value="all">导出所有对话 ({{ totalConversations }} 个)</el-radio>
          <el-radio value="selected">导出选中对话</el-radio>
        </el-radio-group>
        
        <!-- 对话选择列表 -->
        <div v-if="exportScope === 'selected'" class="conversation-selector">
          <el-checkbox-group v-model="selectedConversations" class="conversation-list">
            <el-checkbox 
              v-for="conversation in conversations" 
              :key="conversation.id" 
              :value="conversation.id"
              class="conversation-item"
            >
              <div class="conversation-info">
                <div class="conversation-title">{{ conversation.title }}</div>
                <div class="conversation-meta">
                  <el-tag size="small" :type="getModelTagType(conversation.model_provider)">
                    {{ conversation.model_provider }}
                  </el-tag>
                  <span class="conversation-time">{{ formatTime(conversation.updated_at) }}</span>
                  <span class="message-count">{{ conversation.message_count }} 条消息</span>
                </div>
              </div>
            </el-checkbox>
          </el-checkbox-group>
          
          <div class="selection-actions">
            <el-button size="small" @click="selectAll">全选</el-button>
            <el-button size="small" @click="selectNone">清空</el-button>
            <span class="selection-count">已选择 {{ selectedConversations.length }} 个对话</span>
          </div>
        </div>
      </div>

      <!-- 导出选项 -->
      <div class="form-section">
        <h4>⚙️ 导出选项</h4>
        <el-checkbox v-model="includeRatings">包含用户评分和反馈</el-checkbox>
        <el-checkbox v-model="includeMetadata">包含技术元数据（响应时间、Token使用等）</el-checkbox>
        <el-checkbox v-model="includeStats">包含统计信息</el-checkbox>
      </div>

      <!-- 预览信息 -->
      <div class="preview-info">
        <el-alert
          :title="getPreviewText()"
          type="info"
          :closable="false"
          show-icon
        />
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="startExport"
          :loading="exporting"
          :disabled="!canExport"
        >
          <el-icon v-if="!exporting"><Download /></el-icon>
          {{ exporting ? '导出中...' : '开始导出' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Document, Edit, Grid, Memo, DataBoard, Download 
} from '@element-plus/icons-vue'

// Props
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  conversations: {
    type: Array,
    default: () => []
  },
  totalConversations: {
    type: Number,
    default: 0
  }
})

// Emits
const emit = defineEmits(['update:visible', 'export-success'])

// 响应式数据
const dialogVisible = ref(false)
const exportFormat = ref('pdf')
const exportScope = ref('all')
const selectedConversations = ref([])
const includeRatings = ref(true)
const includeMetadata = ref(false)
const includeStats = ref(true)
const exporting = ref(false)

// 计算属性
const canExport = computed(() => {
  if (exportScope.value === 'selected') {
    return selectedConversations.value.length > 0
  }
  return true
})

// 监听visible变化
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
})

watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
  if (!newVal) {
    resetForm()
  }
})

// 重置表单
const resetForm = () => {
  exportFormat.value = 'pdf'
  exportScope.value = 'all'
  selectedConversations.value = []
  includeRatings.value = true
  includeMetadata.value = false
  includeStats.value = true
  exporting.value = false
}

// 全选对话
const selectAll = () => {
  selectedConversations.value = props.conversations.map(conv => conv.id)
}

// 清空选择
const selectNone = () => {
  selectedConversations.value = []
}

// 获取预览文本
const getPreviewText = () => {
  const formatNames = {
    pdf: 'PDF文档',
    word: 'Word文档',
    excel: 'Excel表格',
    markdown: 'Markdown文件',
    json: 'JSON数据文件'
  }
  
  const count = exportScope.value === 'all' ? props.totalConversations : selectedConversations.value.length
  return `将导出 ${count} 个对话为 ${formatNames[exportFormat.value]} 格式`
}

// 开始导出
const startExport = async () => {
  if (!canExport.value) {
    ElMessage.warning('请选择要导出的对话')
    return
  }

  exporting.value = true
  
  try {
    const exportData = {
      format: exportFormat.value,
      conversation_ids: exportScope.value === 'selected' ? selectedConversations.value : null,
      options: {
        include_ratings: includeRatings.value,
        include_metadata: includeMetadata.value,
        include_stats: includeStats.value
      }
    }

    const response = await fetch('/api/chat/export', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(exportData)
    })

    const result = await response.json()

    if (result.success) {
      ElMessage.success('导出成功！正在下载文件...')
      
      // 触发文件下载
      const downloadUrl = result.data.download_url
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = result.data.filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      // 触发成功事件
      emit('export-success', result.data)
      
      // 关闭对话框
      dialogVisible.value = false
    } else {
      ElMessage.error(result.message || '导出失败')
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    exporting.value = false
  }
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return '未知'
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
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
.export-dialog {
  .form-section {
    margin-bottom: 24px;

    h4 {
      margin: 0 0 12px 0;
      color: #303133;
      font-size: 16px;
    }

    .format-group {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .format-option {
        border: 1px solid #e4e7ed;
        border-radius: 6px;
        padding: 12px;
        margin: 0;
        transition: all 0.2s;

        &:hover {
          border-color: #409eff;
          background: #f0f8ff;
        }

        :deep(.el-radio__input.is-checked + .el-radio__label) {
          color: #409eff;
        }

        :deep(.el-radio__input.is-checked) {
          .el-radio__inner {
            border-color: #409eff;
            background: #409eff;
          }
        }

        .format-content {
          display: flex;
          align-items: center;
          gap: 12px;

          .el-icon {
            font-size: 24px;
            color: #606266;
          }

          .format-info {
            .format-name {
              font-weight: 600;
              color: #303133;
              margin-bottom: 4px;
            }

            .format-desc {
              font-size: 12px;
              color: #909399;
              line-height: 1.4;
            }
          }
        }
      }
    }
  }

  .conversation-selector {
    margin-top: 12px;
    border: 1px solid #e4e7ed;
    border-radius: 6px;

    .conversation-list {
      max-height: 200px;
      overflow-y: auto;
      padding: 8px;

      .conversation-item {
        display: block;
        width: 100%;
        margin: 0 0 8px 0;
        padding: 8px;
        border-radius: 4px;
        transition: background 0.2s;

        &:hover {
          background: #f5f7fa;
        }

        :deep(.el-checkbox__label) {
          width: 100%;
          padding-left: 8px;
        }

        .conversation-info {
          .conversation-title {
            font-weight: 500;
            color: #303133;
            margin-bottom: 4px;
            font-size: 14px;
          }

          .conversation-meta {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 12px;
            color: #909399;
          }
        }
      }
    }

    .selection-actions {
      padding: 8px 12px;
      border-top: 1px solid #e4e7ed;
      background: #f8f9fa;
      display: flex;
      align-items: center;
      gap: 8px;

      .selection-count {
        margin-left: auto;
        font-size: 12px;
        color: #606266;
      }
    }
  }

  .preview-info {
    margin-top: 20px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

// 响应式设计
@media (max-width: 768px) {
  .export-dialog {
    .format-group {
      .format-content {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;

        .format-info {
          .format-name {
            font-size: 14px;
          }

          .format-desc {
            font-size: 11px;
          }
        }
      }
    }

    .conversation-selector {
      .conversation-list {
        max-height: 150px;
      }

      .selection-actions {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;

        .selection-count {
          margin-left: 0;
        }
      }
    }
  }
}
</style>
