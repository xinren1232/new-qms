<template>
  <div class="coze-knowledge-manager">
    <!-- 工具栏 -->
    <div class="knowledge-toolbar">
      <div class="toolbar-left">
        <h3>知识库管理</h3>
        <el-tag type="info">{{ knowledgeItems.length }} 个文档</el-tag>
      </div>
      <div class="toolbar-right">
        <el-button :icon="Upload" @click="showUploadDialog = true">上传文档</el-button>
        <el-button :icon="Refresh" @click="rebuildIndex">重建索引</el-button>
        <el-button :icon="Setting" @click="showSettings = true">设置</el-button>
      </div>
    </div>
    
    <!-- 主要内容 -->
    <div class="knowledge-main">
      <!-- 左侧文档列表 -->
      <div class="documents-panel">
        <div class="panel-header">
          <el-input
            v-model="searchQuery"
            placeholder="搜索文档..."
            :prefix-icon="Search"
            size="small"
          />
        </div>
        
        <div class="documents-list">
          <div
            v-for="item in filteredKnowledgeItems"
            :key="item.id"
            :class="['document-item', { selected: selectedItem?.id === item.id }]"
            @click="selectItem(item)"
          >
            <div class="document-icon">
              <el-icon>
                <component :is="getDocumentIcon(item.type)" />
              </el-icon>
            </div>
            <div class="document-info">
              <div class="document-name">{{ item.name }}</div>
              <div class="document-meta">
                <span class="document-size">{{ formatFileSize(item.size) }}</span>
                <span class="document-status">{{ getStatusText(item.status) }}</span>
              </div>
            </div>
            <div class="document-actions">
              <el-dropdown @command="handleDocumentAction">
                <el-button :icon="MoreFilled" size="small" circle />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'preview', item }">预览</el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'download', item }">下载</el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'reindex', item }">重新索引</el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', item }" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧详情面板 -->
      <div class="details-panel">
        <div v-if="selectedItem" class="document-details">
          <!-- 文档信息 -->
          <div class="details-section">
            <h4>文档信息</h4>
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="文件名">{{ selectedItem.name }}</el-descriptions-item>
              <el-descriptions-item label="文件类型">{{ selectedItem.type.toUpperCase() }}</el-descriptions-item>
              <el-descriptions-item label="文件大小">{{ formatFileSize(selectedItem.size) }}</el-descriptions-item>
              <el-descriptions-item label="上传时间">{{ formatDate(selectedItem.uploadTime) }}</el-descriptions-item>
              <el-descriptions-item label="索引状态">
                <el-tag :type="getStatusType(selectedItem.status)">
                  {{ getStatusText(selectedItem.status) }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 文档预览 -->
          <div class="details-section">
            <h4>文档预览</h4>
            <div class="document-preview">
              <div v-if="selectedItem.type === 'txt' || selectedItem.type === 'md'" class="text-preview">
                <pre>{{ selectedItem.preview || '正在加载预览...' }}</pre>
              </div>
              <div v-else-if="selectedItem.type === 'pdf'" class="pdf-preview">
                <el-button @click="openPdfViewer">打开PDF查看器</el-button>
              </div>
              <div v-else class="unsupported-preview">
                <el-empty description="不支持预览此文件类型" />
              </div>
            </div>
          </div>
          
          <!-- 索引信息 -->
          <div class="details-section">
            <h4>索引信息</h4>
            <el-descriptions :column="1" size="small">
              <el-descriptions-item label="分块数量">{{ selectedItem.chunks || 0 }}</el-descriptions-item>
              <el-descriptions-item label="向量维度">{{ selectedItem.vectorDim || 1536 }}</el-descriptions-item>
              <el-descriptions-item label="索引时间">{{ formatDate(selectedItem.indexTime) }}</el-descriptions-item>
            </el-descriptions>
            
            <div class="index-actions">
              <el-button size="small" @click="testSearch">测试搜索</el-button>
              <el-button size="small" @click="viewChunks">查看分块</el-button>
            </div>
          </div>
        </div>
        
        <div v-else class="no-selection">
          <el-empty description="请选择一个文档查看详情" />
        </div>
      </div>
    </div>
    
    <!-- 上传对话框 -->
    <el-dialog v-model="showUploadDialog" title="上传文档" width="600px">
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        drag
        :action="uploadAction"
        :multiple="true"
        :file-list="fileList"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        :before-upload="beforeUpload"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持 txt、md、pdf、doc、docx 格式，单个文件不超过 10MB
          </div>
        </template>
      </el-upload>
      
      <template #footer>
        <el-button @click="showUploadDialog = false">取消</el-button>
        <el-button type="primary" @click="startUpload">开始上传</el-button>
      </template>
    </el-dialog>
    
    <!-- 设置对话框 -->
    <el-dialog v-model="showSettings" title="知识库设置" width="500px">
      <el-form :model="settings" label-width="120px">
        <el-form-item label="分块大小">
          <el-input-number v-model="settings.chunkSize" :min="100" :max="2000" />
        </el-form-item>
        <el-form-item label="分块重叠">
          <el-input-number v-model="settings.chunkOverlap" :min="0" :max="500" />
        </el-form-item>
        <el-form-item label="向量模型">
          <el-select v-model="settings.embeddingModel">
            <el-option label="text-embedding-ada-002" value="ada-002" />
            <el-option label="text-embedding-3-small" value="3-small" />
            <el-option label="text-embedding-3-large" value="3-large" />
          </el-select>
        </el-form-item>
        <el-form-item label="自动索引">
          <el-switch v-model="settings.autoIndex" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showSettings = false">取消</el-button>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import {
  Upload,
  Refresh,
  Setting,
  Search,
  MoreFilled,
  UploadFilled,
  Document,
  Picture,
  VideoPlay
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Props
const props = defineProps({
  project: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['upload', 'index'])

// 响应式数据
const searchQuery = ref('')
const selectedItem = ref(null)
const showUploadDialog = ref(false)
const showSettings = ref(false)
const uploadRef = ref(null)
const fileList = ref([])
const uploadAction = '/api/knowledge/upload'

// 知识库项目
const knowledgeItems = reactive([
  {
    id: 1,
    name: 'ISO 9001质量管理体系标准.pdf',
    type: 'pdf',
    size: 2048576,
    status: 'indexed',
    uploadTime: new Date('2024-01-15'),
    indexTime: new Date('2024-01-15'),
    chunks: 156,
    vectorDim: 1536,
    preview: null
  },
  {
    id: 2,
    name: '质量管理手册.docx',
    type: 'docx',
    size: 1024000,
    status: 'indexing',
    uploadTime: new Date('2024-01-20'),
    indexTime: null,
    chunks: 0,
    vectorDim: 1536,
    preview: null
  },
  {
    id: 3,
    name: '质量控制流程.md',
    type: 'md',
    size: 51200,
    status: 'indexed',
    uploadTime: new Date('2024-01-18'),
    indexTime: new Date('2024-01-18'),
    chunks: 23,
    vectorDim: 1536,
    preview: '# 质量控制流程\n\n## 1. 概述\n质量控制是确保产品和服务符合规定要求的过程...'
  }
])

// 设置
const settings = reactive({
  chunkSize: 1000,
  chunkOverlap: 200,
  embeddingModel: 'ada-002',
  autoIndex: true
})

// 计算属性
const filteredKnowledgeItems = computed(() => {
  if (!searchQuery.value) return knowledgeItems
  
  return knowledgeItems.filter(item =>
    item.name.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 方法
const selectItem = (item) => {
  selectedItem.value = item
  loadPreview(item)
}

const loadPreview = async (item) => {
  if (item.type === 'txt' || item.type === 'md') {
    // 模拟加载预览
    if (!item.preview) {
      item.preview = '正在加载预览...'
      setTimeout(() => {
        item.preview = `这是 ${item.name} 的预览内容...\n\n文档内容会在这里显示。`
      }, 1000)
    }
  }
}

const getDocumentIcon = (type) => {
  const iconMap = {
    pdf: Document,
    doc: Document,
    docx: Document,
    txt: Document,
    md: Document,
    jpg: Picture,
    png: Picture,
    mp4: VideoPlay
  }
  return iconMap[type] || Document
}

const getStatusType = (status) => {
  const typeMap = {
    indexed: 'success',
    indexing: 'warning',
    failed: 'danger',
    pending: 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    indexed: '已索引',
    indexing: '索引中',
    failed: '索引失败',
    pending: '待索引'
  }
  return textMap[status] || '未知'
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

const handleDocumentAction = ({ action, item }) => {
  switch (action) {
    case 'preview':
      selectItem(item)
      break
    case 'download':
      downloadDocument(item)
      break
    case 'reindex':
      reindexDocument(item)
      break
    case 'delete':
      deleteDocument(item)
      break
  }
}

const downloadDocument = (item) => {
  ElMessage.success(`下载 ${item.name}`)
}

const reindexDocument = (item) => {
  item.status = 'indexing'
  ElMessage.success(`重新索引 ${item.name}`)
  
  // 模拟索引过程
  setTimeout(() => {
    item.status = 'indexed'
    item.indexTime = new Date()
    ElMessage.success('索引完成')
  }, 3000)
}

const deleteDocument = async (item) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文档 "${item.name}" 吗？此操作不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const index = knowledgeItems.findIndex(i => i.id === item.id)
    if (index > -1) {
      knowledgeItems.splice(index, 1)
      if (selectedItem.value?.id === item.id) {
        selectedItem.value = null
      }
      ElMessage.success('文档删除成功')
    }
  } catch {
    // 用户取消删除
  }
}

const beforeUpload = (file) => {
  const allowedTypes = ['application/pdf', 'text/plain', 'text/markdown', 
                       'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
  const isAllowedType = allowedTypes.includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isAllowedType) {
    ElMessage.error('只能上传 PDF、TXT、MD、DOC、DOCX 格式的文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response, file) => {
  const newItem = {
    id: Date.now(),
    name: file.name,
    type: file.name.split('.').pop().toLowerCase(),
    size: file.size,
    status: 'pending',
    uploadTime: new Date(),
    indexTime: null,
    chunks: 0,
    vectorDim: 1536,
    preview: null
  }
  
  knowledgeItems.push(newItem)
  
  if (settings.autoIndex) {
    setTimeout(() => {
      newItem.status = 'indexing'
      setTimeout(() => {
        newItem.status = 'indexed'
        newItem.indexTime = new Date()
        newItem.chunks = Math.floor(Math.random() * 100) + 10
        ElMessage.success(`${file.name} 索引完成`)
      }, 2000)
    }, 1000)
  }
  
  ElMessage.success(`${file.name} 上传成功`)
  emit('upload', [newItem])
}

const handleUploadError = (error, file) => {
  ElMessage.error(`${file.name} 上传失败`)
}

const startUpload = () => {
  uploadRef.value?.submit()
  showUploadDialog.value = false
}

const rebuildIndex = () => {
  ElMessage.success('开始重建索引...')
  emit('index', knowledgeItems)
}

const testSearch = () => {
  ElMessage.success('测试搜索功能')
}

const viewChunks = () => {
  ElMessage.success('查看文档分块')
}

const openPdfViewer = () => {
  ElMessage.success('打开PDF查看器')
}

const saveSettings = () => {
  ElMessage.success('设置保存成功')
  showSettings.value = false
}
</script>

<style lang="scss" scoped>
.coze-knowledge-manager {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .knowledge-toolbar {
    height: 60px;
    padding: 0 20px;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: white;
    
    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 12px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        color: #1f2937;
      }
    }
    
    .toolbar-right {
      display: flex;
      gap: 8px;
    }
  }
  
  .knowledge-main {
    flex: 1;
    display: flex;
    overflow: hidden;
    
    .documents-panel {
      width: 400px;
      border-right: 1px solid #e4e7ed;
      background: #f9fafb;
      display: flex;
      flex-direction: column;
      
      .panel-header {
        padding: 16px;
        border-bottom: 1px solid #e4e7ed;
        background: white;
      }
      
      .documents-list {
        flex: 1;
        overflow-y: auto;
        padding: 16px;
        
        .document-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px;
          background: white;
          border-radius: 8px;
          margin-bottom: 8px;
          cursor: pointer;
          transition: all 0.2s ease;
          border: 1px solid #e5e7eb;
          
          &:hover {
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }
          
          &.selected {
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
          }
          
          .document-icon {
            width: 32px;
            height: 32px;
            border-radius: 6px;
            background: #f3f4f6;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #6b7280;
          }
          
          .document-info {
            flex: 1;
            
            .document-name {
              font-size: 14px;
              font-weight: 500;
              color: #1f2937;
              margin-bottom: 4px;
              word-break: break-all;
            }
            
            .document-meta {
              display: flex;
              gap: 8px;
              font-size: 12px;
              color: #6b7280;
            }
          }
          
          .document-actions {
            opacity: 0;
            transition: opacity 0.2s ease;
          }
          
          &:hover .document-actions {
            opacity: 1;
          }
        }
      }
    }
    
    .details-panel {
      flex: 1;
      background: white;
      overflow-y: auto;
      
      .document-details {
        padding: 20px;
        
        .details-section {
          margin-bottom: 32px;
          
          h4 {
            margin: 0 0 16px 0;
            font-size: 16px;
            font-weight: 600;
            color: #1f2937;
          }
          
          .document-preview {
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            padding: 16px;
            background: #f9fafb;
            
            .text-preview {
              pre {
                margin: 0;
                font-family: 'Monaco', 'Menlo', monospace;
                font-size: 12px;
                line-height: 1.5;
                color: #374151;
                white-space: pre-wrap;
                word-break: break-word;
              }
            }
          }
          
          .index-actions {
            margin-top: 16px;
            display: flex;
            gap: 8px;
          }
        }
      }
      
      .no-selection {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }
}

.upload-demo {
  :deep(.el-upload-dragger) {
    width: 100%;
  }
}
</style>
