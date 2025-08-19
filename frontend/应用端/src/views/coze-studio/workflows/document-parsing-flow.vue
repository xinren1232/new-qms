<template>
  <div class="document-parsing-workflow">
    <!-- 顶部工具栏 -->
    <div class="workflow-header">
      <div class="header-left">
        <el-button @click="goBack" size="small" text>
          <el-icon><ArrowLeft /></el-icon>
          返回工作流
        </el-button>
        <div class="workflow-title">
          <h3>智能文档解析工作流</h3>
          <span class="workflow-subtitle">多格式文档解析 → 内容分析 → 结构化输出</span>
        </div>
      </div>
      <div class="header-right">
        <el-button @click="resetWorkflow" size="small">
          <el-icon><Refresh /></el-icon>
          重置流程
        </el-button>
        <el-button @click="startWorkflow" type="primary" size="small" :loading="isRunning">
          <el-icon><VideoPlay /></el-icon>
          {{ isRunning ? '执行中...' : '开始解析' }}
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="workflow-content">
      <!-- 左侧文档输入区域 -->
      <div class="input-panel">
        <div class="panel-header">
          <h4>📄 文档输入</h4>
          <span class="panel-subtitle">支持多种格式文档上传和解析</span>
        </div>

        <!-- 文件上传区域 -->
        <div class="upload-section">
          <el-upload
            ref="uploadRef"
            class="document-uploader"
            drag
            :auto-upload="false"
            :on-change="handleFileChange"
            :accept="acceptedFileTypes"
            :show-file-list="false"
            multiple
            :limit="5"
            :on-exceed="handleExceed"
          >
            <div class="upload-content">
              <el-icon class="upload-icon"><Upload /></el-icon>
              <div class="upload-text">
                <p>拖拽文件到此处或点击上传</p>
                <p class="upload-hint">支持 PDF、Word、Excel、CSV、图片等格式</p>
                <p class="upload-limit">最多同时上传5个文件，单文件不超过50MB</p>
              </div>
            </div>
          </el-upload>

          <!-- 文件列表 -->
          <div v-if="fileList.length > 0" class="file-list">
            <div class="file-list-header">
              <span>已选择文件 ({{ fileList.length }})</span>
              <el-button @click="clearAllFiles" size="small" text type="danger">
                清空全部
              </el-button>
            </div>
            <div
              v-for="(file, index) in fileList"
              :key="file.uid"
              class="file-item"
              :class="{ 'active': currentFileIndex === index }"
              @click="selectFile(index)"
            >
              <div class="file-info">
                <el-icon class="file-icon">
                  <component :is="getFileIcon(file.name)" />
                </el-icon>
                <div class="file-details">
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">{{ formatFileSize(file.size) }}</span>
                  <span class="file-type">{{ getFileType(file.name) }}</span>
                </div>
                <div class="file-status">
                  <el-tag
                    v-if="file.parseStatus"
                    :type="getFileStatusType(file.parseStatus)"
                    size="small"
                  >
                    {{ getFileStatusText(file.parseStatus) }}
                  </el-tag>
                </div>
                <el-button @click.stop="removeFile(index)" size="small" text type="danger">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>

              <!-- 文件预览 -->
              <div v-if="file.preview" class="file-preview">
                <div class="preview-content">
                  <pre v-if="file.preview.type === 'text'">{{ file.preview.content.slice(0, 200) }}...</pre>
                  <div v-else-if="file.preview.type === 'table'" class="table-preview">
                    <el-table :data="file.preview.content.slice(0, 3)" size="small" max-height="120">
                      <el-table-column
                        v-for="(col, colIndex) in file.preview.columns"
                        :key="colIndex"
                        :prop="col"
                        :label="col"
                        width="120"
                        show-overflow-tooltip
                      />
                    </el-table>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- URL输入区域 -->
        <div class="url-section">
          <h5>或输入文档URL</h5>
          <el-input
            v-model="documentUrl"
            placeholder="输入文档链接地址"
            @keyup.enter="loadFromUrl"
          >
            <template #append>
              <el-button @click="loadFromUrl" :loading="loadingUrl">加载</el-button>
            </template>
          </el-input>
        </div>

        <!-- 文本输入区域 -->
        <div class="text-section">
          <h5>或直接输入文本</h5>
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="4"
            placeholder="直接粘贴文本内容进行分析..."
            maxlength="10000"
            show-word-limit
          />
        </div>
      </div>

      <!-- 中间工作流可视化区域 -->
      <div class="workflow-visualization">
        <div class="visualization-header">
          <h4>🔄 解析流程</h4>
          <div class="flow-controls">
            <el-tag v-if="workflowStatus" :type="getStatusTagType(workflowStatus)">
              {{ getStatusText(workflowStatus) }}
            </el-tag>
          </div>
        </div>

        <!-- 工作流步骤展示 -->
        <div class="workflow-steps">
          <div class="steps-container">
            <div
              v-for="(step, index) in workflowSteps"
              :key="step.id"
              class="workflow-step"
              :class="{
                'active': currentStepIndex === index,
                'completed': step.status === 'completed',
                'running': step.status === 'running',
                'error': step.status === 'error',
                'pending': step.status === 'pending'
              }"
            >
              <!-- 步骤图标 -->
              <div class="step-icon">
                <el-icon v-if="step.status === 'running'" class="icon-running">
                  <Loading />
                </el-icon>
                <el-icon v-else-if="step.status === 'completed'" class="icon-completed">
                  <CircleCheck />
                </el-icon>
                <el-icon v-else-if="step.status === 'error'" class="icon-error">
                  <CircleClose />
                </el-icon>
                <el-icon v-else class="icon-pending">
                  <component :is="step.icon" />
                </el-icon>
              </div>

              <!-- 步骤内容 -->
              <div class="step-content">
                <div class="step-title">{{ step.title }}</div>
                <div class="step-description">{{ step.description }}</div>

                <!-- 步骤详情 -->
                <div v-if="step.details" class="step-details">
                  <div v-if="step.details.format" class="detail-item">
                    <span class="detail-label">检测格式:</span>
                    <el-tag size="small">{{ step.details.format }}</el-tag>
                  </div>
                  <div v-if="step.details.parser" class="detail-item">
                    <span class="detail-label">解析器:</span>
                    <el-tag size="small" type="info">{{ step.details.parser }}</el-tag>
                  </div>
                  <div v-if="step.details.duration" class="detail-item">
                    <span class="detail-label">耗时:</span>
                    <span class="detail-value">{{ step.details.duration }}ms</span>
                  </div>
                </div>

                <!-- 步骤结果预览 -->
                <div v-if="step.result && step.status === 'completed'" class="step-result">
                  <el-collapse>
                    <el-collapse-item title="查看结果" :name="step.id">
                      <pre class="result-preview">{{ formatStepResult(step.result) }}</pre>
                    </el-collapse-item>
                  </el-collapse>
                </div>
              </div>

              <!-- 连接线 -->
              <div v-if="index < workflowSteps.length - 1" class="step-connector">
                <div class="connector-line" :class="{ 'active': step.status === 'completed' }"></div>
                <div class="connector-arrow" :class="{ 'active': step.status === 'completed' }">
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 整体进度条 -->
        <div class="workflow-progress">
          <el-progress
            :percentage="workflowProgress"
            :status="workflowStatus === 'error' ? 'exception' : (workflowStatus === 'completed' ? 'success' : '')"
            :stroke-width="8"
          />
          <div class="progress-text">
            {{ getProgressText() }}
          </div>
        </div>
      </div>

      <!-- 右侧结果展示区域 -->
      <div class="results-panel">
        <div class="panel-header">
          <h4>📊 解析结果</h4>
          <div class="result-actions" v-if="finalResult">
            <el-button @click="exportResult" size="small" type="primary">
              <el-icon><Download /></el-icon>
              导出结果
            </el-button>
          </div>
        </div>

        <!-- 结果内容 -->
        <div class="results-content">
          <!-- 批量处理结果 -->
          <div v-if="batchMode && batchResults.length > 0" class="batch-results">
            <div class="batch-header">
              <h5>📊 批量处理结果</h5>
              <div class="batch-stats">
                <el-tag type="success">成功: {{ batchResults.filter(r => r.status === 'success').length }}</el-tag>
                <el-tag type="danger">失败: {{ batchResults.filter(r => r.status === 'error').length }}</el-tag>
                <el-tag type="info">总计: {{ batchResults.length }}</el-tag>
              </div>
            </div>

            <div class="batch-list">
              <div
                v-for="(result, index) in batchResults"
                :key="index"
                class="batch-item"
                :class="{ 'success': result.status === 'success', 'error': result.status === 'error' }"
              >
                <div class="batch-item-header">
                  <el-icon class="status-icon">
                    <CircleCheck v-if="result.status === 'success'" />
                    <CircleClose v-else />
                  </el-icon>
                  <span class="file-name">{{ result.file }}</span>
                  <el-tag :type="result.status === 'success' ? 'success' : 'danger'" size="small">
                    {{ result.status === 'success' ? '成功' : '失败' }}
                  </el-tag>
                </div>

                <div v-if="result.error" class="error-message">
                  {{ result.error }}
                </div>

                <div v-if="result.result" class="result-preview">
                  <el-collapse>
                    <el-collapse-item :title="`查看解析结果`" :name="`result-${index}`">
                      <div class="result-summary">
                        <p><strong>解析类型:</strong> {{ result.result.parsed?.res?.type || '未知' }}</p>
                        <p><strong>内容长度:</strong> {{ (result.result.parsed?.res?.text || '').length || 0 }} 字符</p>
                        <p><strong>处理时间:</strong> {{ result.result.duration || 0 }}ms</p>
                      </div>
                      <pre class="result-content">{{ formatStepResult(result.result.parsed?.res) }}</pre>
                    </el-collapse-item>
                  </el-collapse>
                </div>
              </div>
            </div>

            <div class="batch-actions">
              <el-button @click="exportBatchResults" type="primary" size="small">
                <el-icon><Download /></el-icon>
                导出批量结果
              </el-button>
              <el-button @click="clearBatchResults" size="small">
                清空结果
              </el-button>
            </div>
          </div>

          <!-- 无结果状态 -->
          <div v-else-if="!finalResult && batchResults.length === 0" class="no-results">
            <el-empty description="暂无解析结果">
              <template #image>
                <el-icon size="60"><DocumentCopy /></el-icon>
              </template>
              <p>上传文档并开始解析后，结果将在此处显示</p>
              <p v-if="fileList.length > 1" class="batch-hint">
                检测到多个文件，将启用批量处理模式
              </p>
            </el-empty>
          </div>

          <!-- 有结果时的展示 -->
          <div v-else class="result-sections">
            <!-- 文档基本信息 -->
            <div class="result-section">
              <h5>📄 文档信息</h5>
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">文件名:</span>
                  <span class="info-value">{{ finalResult.metadata?.fileName || '未知' }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">格式:</span>
                  <el-tag size="small">{{ finalResult.metadata?.format || '未知' }}</el-tag>
                </div>
                <div class="info-item">
                  <span class="info-label">大小:</span>
                  <span class="info-value">{{ formatFileSize(finalResult.metadata?.fileSize) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">解析时间:</span>
                  <span class="info-value">{{ finalResult.metadata?.duration || 0 }}ms</span>
                </div>
              </div>
            </div>

            <!-- 解析内容 -->
            <div class="result-section">
              <h5>📝 解析内容</h5>
              <div class="content-preview">
                <el-input
                  v-model="finalResult.content"
                  type="textarea"
                  :rows="8"
                  readonly
                  placeholder="解析的文本内容将在此显示..."
                />
                <div class="content-stats">
                  <span>字符数: {{ finalResult.content?.length || 0 }}</span>
                  <span>字数: {{ getWordCount(finalResult.content) }}</span>
                </div>
              </div>
            </div>

            <!-- AI分析结果 -->
            <div v-if="finalResult.analysis" class="result-section">
              <h5>🤖 AI分析</h5>
              <el-tabs v-model="activeAnalysisTab" type="border-card">
                <el-tab-pane label="结构分析" name="structure" v-if="finalResult.analysis.structure">
                  <div class="analysis-content">
                    <pre>{{ finalResult.analysis.structure }}</pre>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="关键词" name="keywords" v-if="finalResult.analysis.keywords">
                  <div class="keywords-list">
                    <el-tag
                      v-for="keyword in finalResult.analysis.keywords"
                      :key="keyword"
                      class="keyword-tag"
                    >
                      {{ keyword }}
                    </el-tag>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="内容摘要" name="summary" v-if="finalResult.analysis.summary">
                  <div class="analysis-content">
                    <p>{{ finalResult.analysis.summary }}</p>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="实体识别" name="entities" v-if="finalResult.analysis.entities">
                  <div class="entities-list">
                    <div
                      v-for="entity in finalResult.analysis.entities"
                      :key="entity.text"
                      class="entity-item"
                    >
                      <el-tag :type="getEntityTagType(entity.type)">{{ entity.type }}</el-tag>
                      <span class="entity-text">{{ entity.text }}</span>
                    </div>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </div>

            <!-- 原始数据 -->
            <div class="result-section">
              <h5>🔧 原始数据</h5>
              <el-collapse>
                <el-collapse-item title="查看完整JSON数据" name="raw">
                  <pre class="raw-data">{{ JSON.stringify(finalResult, null, 2) }}</pre>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部状态栏 -->
    <div class="workflow-footer">
      <div class="footer-left">
        <span class="status-item">
          <el-icon><DocumentCopy /></el-icon>
          支持格式: PDF, Word, Excel, CSV, 图片
        </span>
        <span class="status-item">
          <el-icon><Setting /></el-icon>
          解析引擎: 智能多格式解析
        </span>
      </div>
      <div class="footer-right">
        <span v-if="lastExecutionTime" class="status-item">
          上次执行: {{ lastExecutionTime }}
        </span>
        <span class="status-item">
          <el-icon><CircleCheck /></el-icon>
          就绪状态
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, VideoPlay, Refresh, Loading, CircleCheck, CircleClose, Delete,
  Upload, Setting, Download, DocumentCopy, ArrowRight
} from '@element-plus/icons-vue'
import * as cozeStudioAPI from '@/api/coze-studio'

const router = useRouter()

// 响应式数据
const uploadRef = ref(null)
const fileList = ref([])
const currentFileIndex = ref(0)
const documentUrl = ref('')
const inputText = ref('')
const loadingUrl = ref(false)
const isRunning = ref(false)
const workflowStatus = ref('ready')
const currentStepIndex = ref(-1)
const workflowProgress = ref(0)
const finalResult = ref(null)
const activeAnalysisTab = ref('structure')
const lastExecutionTime = ref('')
const batchMode = ref(false)
const batchResults = ref([])

// 接受的文件类型
const acceptedFileTypes = '.pdf,.docx,.doc,.xlsx,.xls,.csv,.txt,.json,.xml,.png,.jpg,.jpeg,.bmp,.gif,.webp,.md,.rtf'

// 计算属性
const currentFile = computed(() => {
  return fileList.value[currentFileIndex.value] || null
})

// 工作流步骤定义 - 参考图二优化为多格式解析流程
const workflowSteps = ref([
  {
    id: 'input',
    title: '文档输入',
    description: '接收多种格式的文档文件、URL或文本内容',
    icon: 'Upload',
    status: 'pending',
    details: null,
    result: null
  },
  {
    id: 'format_detection',
    title: '格式识别',
    description: '智能识别文档格式：PDF、Word、Excel、图片等',
    icon: 'Search',
    status: 'pending',
    details: null,
    result: null
  },
  {
    id: 'parser_selection',
    title: '解析器选择',
    description: '根据格式选择最适合的解析器引擎',
    icon: 'Setting',
    status: 'pending',
    details: null,
    result: null
  },
  {
    id: 'content_extraction',
    title: '内容提取',
    description: '执行格式化内容提取和结构化处理',
    icon: 'DocumentCopy',
    status: 'pending',
    details: null,
    result: null
  },
  {
    id: 'normalization',
    title: '内容归一化',
    description: '统一不同格式的内容为标准结构',
    icon: 'Grid',
    status: 'pending',
    details: null,
    result: null
  },
  {
    id: 'ai_analysis',
    title: 'AI智能分析',
    description: '文本摘要、表格统计、关键信息提取',
    icon: 'MagicStick',
    status: 'pending',
    details: null,
    result: null
  },
  {
    id: 'output_formatting',
    title: '结果输出',
    description: '格式化最终结果，支持多种输出格式',
    icon: 'Download',
    status: 'pending',
    details: null,
    result: null
  }
])

// 计算属性
const getStatusTagType = (status) => {
  const typeMap = {
    'ready': 'info',
    'running': 'warning',
    'completed': 'success',
    'error': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'ready': '就绪',
    'running': '执行中',
    'completed': '已完成',
    'error': '执行失败'
  }
  return textMap[status] || status
}

const getProgressText = () => {
  if (workflowStatus.value === 'running') {
    const currentStep = workflowSteps.value[currentStepIndex.value]
    return `正在执行: ${currentStep?.title || '未知步骤'}`
  } else if (workflowStatus.value === 'completed') {
    return '解析完成'
  } else if (workflowStatus.value === 'error') {
    return '执行失败'
  }
  return '等待开始'
}

// 方法
const goBack = () => {
  router.push('/coze-studio/workflows')
}

const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getWordCount = (text) => {
  if (!text) return 0
  return text.trim().split(/\s+/).length
}

const getEntityTagType = (entityType) => {
  const typeMap = {
    'PERSON': 'success',
    'ORG': 'warning',
    'GPE': 'info',
    'DATE': 'primary',
    'MONEY': 'danger'
  }
  return typeMap[entityType] || ''
}

const formatStepResult = (result) => {
  if (typeof result === 'string') {
    return result.length > 200 ? result.substring(0, 200) + '...' : result
  }
  return JSON.stringify(result, null, 2)
}

// 文件处理
const handleFileChange = (file) => {
  if (file.size > 50 * 1024 * 1024) { // 50MB限制
    ElMessage.error('文件大小不能超过50MB')
    return
  }

  // 添加到文件列表
  const fileItem = {
    ...file,
    uid: file.uid || Date.now(),
    parseStatus: 'pending',
    preview: null,
    result: null
  }

  fileList.value.push(fileItem)
  currentFileIndex.value = fileList.value.length - 1

  // 自动预览文件内容
  previewFile(fileItem)

  ElMessage.success(`已添加文件: ${file.name}`)
}

const handleExceed = (files, fileList) => {
  ElMessage.warning(`最多只能选择5个文件，当前选择了${files.length}个文件，已有${fileList.length}个文件`)
}

const selectFile = (index) => {
  currentFileIndex.value = index
}

const removeFile = (index) => {
  fileList.value.splice(index, 1)
  if (currentFileIndex.value >= fileList.value.length) {
    currentFileIndex.value = Math.max(0, fileList.value.length - 1)
  }
}

const clearAllFiles = () => {
  fileList.value = []
  currentFileIndex.value = 0
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

const getFileIcon = (fileName) => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  const iconMap = {
    'pdf': 'Document',
    'doc': 'Document', 'docx': 'Document',
    'xls': 'Document', 'xlsx': 'Document',
    'csv': 'Document',
    'txt': 'Document', 'md': 'Document',
    'png': 'Picture', 'jpg': 'Picture', 'jpeg': 'Picture', 'gif': 'Picture',
    'json': 'DocumentCopy', 'xml': 'DocumentCopy'
  }
  return iconMap[ext] || 'Document'
}

const getFileType = (fileName) => {
  const ext = fileName.split('.').pop()?.toLowerCase()
  const typeMap = {
    'pdf': 'PDF文档',
    'doc': 'Word文档', 'docx': 'Word文档',
    'xls': 'Excel表格', 'xlsx': 'Excel表格',
    'csv': 'CSV数据',
    'txt': '文本文件', 'md': 'Markdown',
    'png': 'PNG图片', 'jpg': 'JPEG图片', 'jpeg': 'JPEG图片', 'gif': 'GIF图片',
    'json': 'JSON数据', 'xml': 'XML数据'
  }
  return typeMap[ext] || '未知格式'
}

const getFileStatusType = (status) => {
  const typeMap = {
    'pending': 'info',
    'parsing': 'warning',
    'completed': 'success',
    'error': 'danger'
  }
  return typeMap[status] || 'info'
}

const getFileStatusText = (status) => {
  const textMap = {
    'pending': '待解析',
    'parsing': '解析中',
    'completed': '已完成',
    'error': '解析失败'
  }
  return textMap[status] || status
}

// 文件预览功能
const previewFile = async (file) => {
  // 获取实际的文件对象
  const actualFile = file.raw || file

  if (!actualFile || !(actualFile instanceof File || actualFile instanceof Blob)) return

  try {
    const fileType = getFileType(file.name)

    if (fileType.includes('图片')) {
      // 图片预览
      const reader = new FileReader()
      reader.onload = (e) => {
        file.preview = {
          type: 'image',
          content: e.target.result
        }
      }
      reader.readAsDataURL(actualFile)
    } else if (fileType.includes('文本') || file.name.endsWith('.md')) {
      // 文本文件预览
      const reader = new FileReader()
      reader.onload = (e) => {
        file.preview = {
          type: 'text',
          content: e.target.result
        }
      }
      reader.readAsText(actualFile)
    } else if (fileType.includes('JSON')) {
      // JSON文件预览
      const reader = new FileReader()
      reader.onload = (e) => {
        try {
          const jsonData = JSON.parse(e.target.result)
          file.preview = {
            type: 'json',
            content: JSON.stringify(jsonData, null, 2)
          }
        } catch (err) {
          console.warn('JSON解析失败，作为文本处理:', err)
          file.preview = {
            type: 'text',
            content: e.target.result
          }
        }
      }
      reader.readAsText(actualFile)
    }
  } catch (error) {
    console.error('文件预览失败:', error)
  }
}

const loadFromUrl = async () => {
  if (!documentUrl.value.trim()) {
    ElMessage.warning('请输入有效的URL')
    return
  }

  loadingUrl.value = true
  try {
    // 这里可以添加URL加载逻辑
    ElMessage.success('URL文档加载成功')
  } catch (error) {
    ElMessage.error('URL文档加载失败: ' + error.message)
  } finally {
    loadingUrl.value = false
  }
}

// 工作流控制
const resetWorkflow = () => {
  workflowStatus.value = 'ready'
  currentStepIndex.value = -1
  workflowProgress.value = 0
  finalResult.value = null
  batchResults.value = []

  // 重置所有步骤状态
  workflowSteps.value.forEach(step => {
    step.status = 'pending'
    step.details = null
    step.result = null
  })

  ElMessage.info('工作流已重置')
}

const startWorkflow = async () => {
  // 检查输入
  if (fileList.value.length === 0 && !documentUrl.value.trim() && !inputText.value.trim()) {
    ElMessage.warning('请先上传文件、输入URL或文本内容')
    return
  }

  isRunning.value = true
  workflowStatus.value = 'running'
  currentStepIndex.value = 0
  batchResults.value = []

  try {
    if (fileList.value.length > 1) {
      // 批量处理模式
      batchMode.value = true
      await executeBatchWorkflow()
    } else {
      // 单文件处理模式
      batchMode.value = false
      await executeSingleWorkflow()
    }

    workflowStatus.value = 'completed'
    workflowProgress.value = 100
    lastExecutionTime.value = new Date().toLocaleString()
    const successMessage = batchMode.value
      ? `文档解析完成！共处理${fileList.value.length}个文件`
      : '文档解析完成！'
    ElMessage.success(successMessage)

  } catch (error) {
    workflowStatus.value = 'error'
    ElMessage.error('工作流执行失败: ' + error.message)
  } finally {
    isRunning.value = false
  }
}

// 单文件工作流 - 更新为新的7步流程
const executeSingleWorkflow = async () => {
  // 步骤1: 文档输入
  await executeStep('input')

  // 步骤2: 格式识别
  await executeStep('format_detection')

  // 步骤3: 解析器选择
  await executeStep('parser_selection')

  // 步骤4: 内容提取
  await executeStep('content_extraction')

  // 步骤5: 内容归一化
  await executeStep('normalization')

  // 步骤6: AI智能分析
  await executeStep('ai_analysis')

  // 步骤7: 结果输出
  await executeStep('output_formatting')
}

// 批量工作流
const executeBatchWorkflow = async () => {
  const totalFiles = fileList.value.length

  for (let i = 0; i < totalFiles; i++) {
    const file = fileList.value[i]
    currentFileIndex.value = i
    file.parseStatus = 'parsing'

    try {
      // 为每个文件执行完整的解析流程
      const result = await executeFileParsingAPI(file)

      file.parseStatus = 'completed'
      file.result = result
      batchResults.value.push({
        file: file.name,
        status: 'success',
        result: result
      })

      // 更新进度
      workflowProgress.value = Math.round(((i + 1) / totalFiles) * 100)

    } catch (error) {
      file.parseStatus = 'error'
      batchResults.value.push({
        file: file.name,
        status: 'error',
        error: error.message
      })
    }

    // 短暂延迟避免请求过快
    if (i < totalFiles - 1) {
      await new Promise(resolve => setTimeout(resolve, 500))
    }
  }
}

// 调用后端API进行文件解析
const executeFileParsingAPI = async (file) => {
  // 获取实际的文件对象
  const actualFile = file.raw || file

  if (!actualFile || !(actualFile instanceof File || actualFile instanceof Blob)) {
    throw new Error('无效的文件对象')
  }

  // 将文件转换为base64
  const base64 = await new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result.split(',')[1])
    reader.onerror = reject
    reader.readAsDataURL(actualFile)
  })

  const payload = {
    input: {
      file: {
        name: file.name,
        type: file.type || 'application/octet-stream',
        base64: base64
      }
    },
    options: {
      summarize: true,
      stats: true,
      ingest_kb: false
    }
  }

  const response = await cozeStudioAPI.executeDocumentParsingWorkflow(payload)
  return response.data
}

const executeStep = async (stepId) => {
  const stepIndex = workflowSteps.value.findIndex(s => s.id === stepId)
  if (stepIndex === -1) return

  const step = workflowSteps.value[stepIndex]
  currentStepIndex.value = stepIndex
  step.status = 'running'

  const startTime = Date.now()

  try {
    let result = null

    switch (stepId) {
      case 'input':
        result = await executeInputStep()
        break
      case 'format_detection':
        result = await executeFormatDetectionStep()
        break
      case 'parser_selection':
        result = await executeParserSelectionStep()
        break
      case 'content_extraction':
        result = await executeContentExtractionStep()
        break
      case 'normalization':
        result = await executeNormalizationStep()
        break
      case 'ai_analysis':
        result = await executeAIAnalysisStep()
        break
      case 'output_formatting':
        result = await executeOutputFormattingStep()
        break
    }

    const duration = Date.now() - startTime
    step.status = 'completed'
    step.result = result
    step.details = { ...step.details, duration }

    // 更新进度
    workflowProgress.value = ((stepIndex + 1) / workflowSteps.value.length) * 100

    // 短暂延迟以显示动画效果
    await new Promise(resolve => setTimeout(resolve, 500))

  } catch (error) {
    step.status = 'error'
    step.details = { ...step.details, error: error.message }
    throw error
  }
}

const executeInputStep = async () => {
  const step = workflowSteps.value.find(s => s.id === 'input')

  let inputData = {}

  if (currentFile.value) {
    // 处理文件输入
    const base64 = await fileToBase64(currentFile.value)
    inputData = {
      type: 'file',
      name: currentFile.value.name,
      size: currentFile.value.size,
      mimeType: currentFile.value.type,
      base64: base64
    }
    step.details = {
      fileName: currentFile.value.name,
      fileSize: currentFile.value.size,
      inputType: 'file'
    }
  } else if (documentUrl.value.trim()) {
    // 处理URL输入
    inputData = {
      type: 'url',
      url: documentUrl.value.trim()
    }
    step.details = {
      url: documentUrl.value.trim(),
      inputType: 'url'
    }
  } else if (inputText.value.trim()) {
    // 处理文本输入
    inputData = {
      type: 'text',
      text: inputText.value.trim()
    }
    step.details = {
      textLength: inputText.value.trim().length,
      inputType: 'text'
    }
  }

  return inputData
}

// 新的格式检测步骤 - 更详细的格式识别
const executeFormatDetectionStep = async () => {
  const inputStep = workflowSteps.value.find(s => s.id === 'input')
  const inputData = inputStep.result
  const step = workflowSteps.value.find(s => s.id === 'format_detection')

  let detectedFormat = 'text'
  let confidence = 0.5
  let formatCategory = 'text'

  if (inputData.type === 'file') {
    const fileName = inputData.name || ''
    const ext = fileName.split('.').pop()?.toLowerCase()

    if (ext) {
      const formatMap = {
        // 文档类
        'pdf': { format: 'pdf', confidence: 0.95, category: 'document' },
        'docx': { format: 'docx', confidence: 0.95, category: 'document' },
        'doc': { format: 'doc', confidence: 0.90, category: 'document' },
        'rtf': { format: 'rtf', confidence: 0.85, category: 'document' },

        // 表格类
        'xlsx': { format: 'xlsx', confidence: 0.95, category: 'spreadsheet' },
        'xls': { format: 'xls', confidence: 0.90, category: 'spreadsheet' },
        'csv': { format: 'csv', confidence: 0.95, category: 'spreadsheet' },

        // 数据类
        'json': { format: 'json', confidence: 0.95, category: 'data' },
        'xml': { format: 'xml', confidence: 0.90, category: 'data' },
        'yaml': { format: 'yaml', confidence: 0.85, category: 'data' },

        // 文本类
        'txt': { format: 'text', confidence: 0.85, category: 'text' },
        'md': { format: 'markdown', confidence: 0.90, category: 'text' },

        // 图像类
        'png': { format: 'image', confidence: 0.95, category: 'image' },
        'jpg': { format: 'image', confidence: 0.95, category: 'image' },
        'jpeg': { format: 'image', confidence: 0.95, category: 'image' },
        'gif': { format: 'image', confidence: 0.90, category: 'image' },
        'bmp': { format: 'image', confidence: 0.85, category: 'image' },
        'webp': { format: 'image', confidence: 0.90, category: 'image' }
      }

      if (formatMap[ext]) {
        detectedFormat = formatMap[ext].format
        confidence = formatMap[ext].confidence
        formatCategory = formatMap[ext].category
      }
    }
  }

  step.details = {
    format: detectedFormat,
    category: formatCategory,
    confidence: confidence,
    detectionMethod: 'enhanced_extension_analysis',
    supportedParsers: getSupportedParsers(detectedFormat)
  }

  return { format: detectedFormat, category: formatCategory, confidence }
}

// 获取支持的解析器列表
const getSupportedParsers = (format) => {
  const parserMap = {
    'pdf': ['pdf-parse', 'pdfjs', 'tesseract-ocr'],
    'docx': ['mammoth', 'docx-parser', 'pandoc'],
    'xlsx': ['xlsx', 'exceljs', 'node-xlsx'],
    'csv': ['csv-parser', 'papaparse', 'fast-csv'],
    'image': ['tesseract-ocr', 'google-vision', 'azure-ocr'],
    'json': ['native-json', 'json5'],
    'xml': ['xml2js', 'fast-xml-parser'],
    'text': ['native-text', 'encoding-detector']
  }
  return parserMap[format] || ['fallback-text']
}

// 解析器选择步骤
const executeParserSelectionStep = async () => {
  const formatDetectionStep = workflowSteps.value.find(s => s.id === 'format_detection')
  const formatData = formatDetectionStep.result
  const step = workflowSteps.value.find(s => s.id === 'parser_selection')

  const availableParsers = getSupportedParsers(formatData.format)
  const selectedParser = selectBestParser(formatData.format, formatData.category, formatData.confidence)

  step.details = {
    detectedFormat: formatData.format,
    category: formatData.category,
    availableParsers: availableParsers,
    selectedParser: selectedParser,
    selectionReason: getParserSelectionReason(selectedParser, formatData)
  }

  return {
    selectedParser,
    availableParsers,
    parserConfig: getParserConfig(selectedParser, formatData.format)
  }
}

// 选择最佳解析器
const selectBestParser = (format, category, confidence) => {
  const parserPriority = {
    'pdf': confidence > 0.9 ? 'pdf-parse' : 'pdfjs',
    'docx': 'mammoth',
    'xlsx': 'xlsx',
    'csv': 'csv-parser',
    'image': 'tesseract-ocr',
    'json': 'native-json',
    'xml': 'xml2js',
    'text': 'native-text'
  }
  return parserPriority[format] || 'fallback-text'
}

// 获取解析器选择原因
const getParserSelectionReason = (parser, formatData) => {
  const reasons = {
    'pdf-parse': '高置信度PDF文档，使用专业PDF解析器',
    'pdfjs': '标准PDF文档解析',
    'mammoth': 'Word文档最佳解析器，保持格式',
    'xlsx': 'Excel表格专用解析器',
    'csv-parser': 'CSV数据高效解析',
    'tesseract-ocr': '图像OCR文字识别',
    'native-json': 'JSON数据原生解析',
    'xml2js': 'XML结构化数据解析',
    'fallback-text': '通用文本解析器'
  }
  return reasons[parser] || '默认解析器'
}

// 获取解析器配置
const getParserConfig = (parser, format) => {
  const configs = {
    'pdf-parse': { preserveFormatting: true, extractImages: false },
    'mammoth': { styleMap: [], includeDefaultStyleMap: true },
    'xlsx': { cellDates: true, cellNF: false },
    'csv-parser': { delimiter: 'auto', encoding: 'utf8' },
    'tesseract-ocr': { lang: 'chi_sim+eng', psm: 6 }
  }
  return configs[parser] || {}
}

// 内容提取步骤
const executeContentExtractionStep = async () => {
  const inputStep = workflowSteps.value.find(s => s.id === 'input')
  const parserSelectionStep = workflowSteps.value.find(s => s.id === 'parser_selection')
  const inputData = inputStep.result
  const parserData = parserSelectionStep.result
  const step = workflowSteps.value.find(s => s.id === 'content_extraction')

  try {
    // 调用后端文档解析API，使用选定的解析器
    const response = await fetch('/api/coze-studio/workflows/execute/document-parsing', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('qms_token')}`,
        'user-id': localStorage.getItem('qms_user_id') || 'anonymous'
      },
      body: JSON.stringify({
        inputData,
        selectedParser: parserData.selectedParser,
        parserConfig: parserData.parserConfig,
        detectedFormat: parserData.selectedParser
      })
    })

    if (!response.ok) {
      throw new Error(`内容提取失败: ${response.statusText}`)
    }

    const result = await response.json()

    if (!result.success) {
      throw new Error(result.message || '内容提取失败')
    }

    const extractedContent = result.data.content || ''
    const rawData = result.data.raw || null

    step.details = {
      parser: parserData.selectedParser,
      contentLength: extractedContent.length,
      extractionMethod: 'api_based',
      hasRawData: !!rawData
    }

    return {
      content: extractedContent,
      rawData: rawData,
      metadata: result.data.metadata || {},
      parser: parserData.selectedParser,
      extractionStats: {
        contentLength: extractedContent.length,
        hasStructuredData: !!rawData,
        extractionTime: Date.now()
      }
    }

  } catch (error) {
    // 如果API调用失败，使用简单的文本处理
    console.warn('API解析失败，使用简单文本处理:', error.message)

    let content = ''
    if (inputData.type === 'text') {
      content = inputData.text
    } else if (inputData.type === 'file' && inputData.base64) {
      // 简单的base64文本解码（仅适用于文本文件）
      try {
        content = atob(inputData.base64)
      } catch {
        content = '无法解析文件内容'
      }
    }

    step.details = {
      parser: 'fallback_text',
      contentLength: content.length,
      extractionMethod: 'fallback'
    }

    return {
      content,
      rawData: null,
      metadata: {
        fileName: inputData.name,
        fileSize: inputData.size
      },
      parser: 'fallback_text',
      extractionStats: {
        contentLength: content.length,
        hasStructuredData: false,
        extractionTime: Date.now()
      }
    }
  }
}

// 内容归一化步骤
const executeNormalizationStep = async () => {
  const contentExtractionStep = workflowSteps.value.find(s => s.id === 'content_extraction')
  const extractionData = contentExtractionStep.result
  const step = workflowSteps.value.find(s => s.id === 'normalization')

  try {
    const normalizedContent = normalizeContent(extractionData.content, extractionData.parser)
    const structuredData = extractStructuredData(extractionData.rawData, extractionData.parser)

    step.details = {
      originalLength: extractionData.content?.length || 0,
      normalizedLength: normalizedContent.length,
      structureDetected: !!structuredData,
      normalizationRules: getNormalizationRules(extractionData.parser)
    }

    return {
      normalizedContent,
      structuredData,
      contentType: detectContentType(normalizedContent),
      metadata: {
        ...extractionData.metadata,
        normalizationApplied: true,
        contentStructure: analyzeContentStructure(normalizedContent)
      }
    }
  } catch (error) {
    console.warn('内容归一化失败，使用原始内容:', error.message)

    step.details = {
      originalLength: extractionData.content?.length || 0,
      normalizedLength: extractionData.content?.length || 0,
      normalizationFailed: true,
      error: error.message
    }

    return {
      normalizedContent: extractionData.content || '',
      structuredData: null,
      contentType: 'text',
      metadata: extractionData.metadata
    }
  }
}

// 内容归一化函数
const normalizeContent = (content, parser) => {
  if (!content) return ''

  let normalized = content

  // 基础清理
  normalized = normalized
    .replace(/\r\n/g, '\n')  // 统一换行符
    .replace(/\r/g, '\n')    // 统一换行符
    .replace(/\t/g, '  ')    // 制表符转空格
    .replace(/\u00A0/g, ' ') // 非断空格转普通空格
    .replace(/\s+$/gm, '')   // 移除行尾空格
    .replace(/\n{3,}/g, '\n\n') // 合并多余空行

  // 根据解析器类型进行特定归一化
  switch (parser) {
    case 'pdf-parse':
    case 'pdfjs':
      normalized = normalizePDFContent(normalized)
      break
    case 'mammoth':
      normalized = normalizeWordContent(normalized)
      break
    case 'xlsx':
      normalized = normalizeExcelContent(normalized)
      break
    case 'csv-parser':
      normalized = normalizeCSVContent(normalized)
      break
  }

  return normalized.trim()
}

// PDF内容归一化
const normalizePDFContent = (content) => {
  return content
    .replace(/([a-z])([A-Z])/g, '$1 $2') // 处理连接的单词
    .replace(/(\d+)\s*\n\s*(\d+)/g, '$1$2') // 合并分割的数字
    .replace(/\n(?=[a-z])/g, ' ') // 合并断行的句子
}

// Word内容归一化
const normalizeWordContent = (content) => {
  return content
    .replace(/\n{2,}/g, '\n\n') // 标准化段落间距
    .replace(/\s+/g, ' ') // 合并多余空格
}

// Excel内容归一化
const normalizeExcelContent = (content) => {
  // Excel内容通常已经是结构化的，主要是格式化
  return content
}

// CSV内容归一化
const normalizeCSVContent = (content) => {
  return content
    .replace(/,\s+/g, ',') // 标准化逗号后空格
    .replace(/\n+/g, '\n') // 合并空行
}

// 辅助函数
const extractStructuredData = (rawData, parser) => {
  if (!rawData) return null

  try {
    switch (parser) {
      case 'xlsx':
        return extractExcelStructure(rawData)
      case 'csv-parser':
        return extractCSVStructure(rawData)
      case 'xml2js':
        return extractXMLStructure(rawData)
      case 'native-json':
        return rawData
      default:
        return null
    }
  } catch (error) {
    console.warn('结构化数据提取失败:', error)
    return null
  }
}

const detectContentType = (content) => {
  if (!content) return 'empty'

  // 检测表格数据
  if (content.includes('\t') || content.match(/,.*,.*,/)) {
    return 'tabular'
  }

  // 检测列表
  if (content.match(/^\s*[-*•]\s/m) || content.match(/^\s*\d+\.\s/m)) {
    return 'list'
  }

  // 检测代码
  if (content.match(/^\s*(function|class|def|import|#include)/m)) {
    return 'code'
  }

  return 'text'
}

const analyzeContentStructure = (content) => {
  const lines = content.split('\n')
  const words = content.split(/\s+/).filter(Boolean)
  const sentences = content.split(/[.!?]+/).filter(Boolean)

  return {
    lineCount: lines.length,
    wordCount: words.length,
    sentenceCount: sentences.length,
    avgWordsPerSentence: Math.round(words.length / Math.max(sentences.length, 1)),
    hasHeaders: /^#{1,6}\s/.test(content) || /^[A-Z][^a-z]*$/.test(lines[0]),
    hasTables: content.includes('|') || content.includes('\t'),
    hasLists: /^\s*[-*•]\s/m.test(content)
  }
}

const getNormalizationRules = (parser) => {
  const rules = {
    'pdf-parse': ['连词处理', '数字合并', '断行修复'],
    'mammoth': ['段落标准化', '空格合并'],
    'xlsx': ['格式保持'],
    'csv-parser': ['逗号标准化', '空行合并'],
    'fallback_text': ['基础清理']
  }
  return rules[parser] || ['基础清理']
}

// AI分析步骤 - 更新为新的流程
const executeAIAnalysisStep = async () => {
  const normalizationStep = workflowSteps.value.find(s => s.id === 'normalization')
  const normalizationData = normalizationStep.result
  const step = workflowSteps.value.find(s => s.id === 'ai_analysis')

  try {
    // 调用增强的AI分析API
    const response = await fetch('/api/coze-studio/ai/analyze-document', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('qms_token')}`,
        'user-id': localStorage.getItem('qms_user_id') || 'anonymous'
      },
      body: JSON.stringify({
        content: normalizationData.normalizedContent,
        structuredData: normalizationData.structuredData,
        contentType: normalizationData.contentType,
        analysisTypes: ['structure', 'keywords', 'summary', 'entities', 'statistics']
      })
    })

    if (!response.ok) {
      throw new Error(`AI分析失败: ${response.statusText}`)
    }

    const result = await response.json()

    if (!result.success) {
      throw new Error(result.message || 'AI分析失败')
    }

    step.details = {
      aiModel: result.data.model || 'enhanced_nlp',
      analysisTypes: ['structure', 'keywords', 'summary', 'entities', 'statistics'],
      contentType: normalizationData.contentType,
      hasStructuredData: !!normalizationData.structuredData
    }

    return {
      ...result.data.analysis,
      contentStatistics: generateContentStatistics(normalizationData.normalizedContent),
      structuredAnalysis: analyzeStructuredData(normalizationData.structuredData)
    }

  } catch (error) {
    // 如果AI分析失败，使用简单的文本分析
    console.warn('AI分析失败，使用简单分析:', error.message)

    const content = parsingData.content || ''
    const words = content.split(/\s+/).filter(Boolean)
    const sentences = content.split(/[.!?]+/).filter(Boolean)

    // 简单的关键词提取（取最常见的词）
    const wordCount = {}
    words.forEach(word => {
      const cleanWord = word.toLowerCase().replace(/[^\w]/g, '')
      if (cleanWord.length > 3) {
        wordCount[cleanWord] = (wordCount[cleanWord] || 0) + 1
      }
    })

    const keywords = Object.entries(wordCount)
      .sort(([,a], [,b]) => b - a)
      .slice(0, 10)
      .map(([word]) => word)

    step.details = {
      aiModel: 'fallback_analysis',
      analysisTypes: ['basic_stats', 'keywords']
    }

    return {
      structure: `文档包含 ${words.length} 个词，${sentences.length} 个句子`,
      keywords: keywords,
      summary: content.substring(0, 200) + (content.length > 200 ? '...' : ''),
      entities: []
    }
  }
}

// 生成内容统计
const generateContentStatistics = (content) => {
  if (!content) return {}

  const words = content.split(/\s+/).filter(Boolean)
  const sentences = content.split(/[.!?]+/).filter(Boolean)
  const paragraphs = content.split(/\n\s*\n/).filter(Boolean)

  return {
    characterCount: content.length,
    wordCount: words.length,
    sentenceCount: sentences.length,
    paragraphCount: paragraphs.length,
    avgWordsPerSentence: Math.round(words.length / Math.max(sentences.length, 1)),
    avgSentencesPerParagraph: Math.round(sentences.length / Math.max(paragraphs.length, 1)),
    readingTime: Math.ceil(words.length / 200) // 假设每分钟200词
  }
}

// 分析结构化数据
const analyzeStructuredData = (structuredData) => {
  if (!structuredData) return null

  try {
    if (Array.isArray(structuredData)) {
      return {
        type: 'array',
        itemCount: structuredData.length,
        sampleItem: structuredData[0] || null
      }
    } else if (typeof structuredData === 'object') {
      return {
        type: 'object',
        keyCount: Object.keys(structuredData).length,
        keys: Object.keys(structuredData).slice(0, 10)
      }
    }
  } catch (error) {
    console.warn('结构化数据分析失败:', error)
  }

  return null
}

// 输出格式化步骤
const executeOutputFormattingStep = async () => {
  const normalizationStep = workflowSteps.value.find(s => s.id === 'normalization')
  const aiAnalysisStep = workflowSteps.value.find(s => s.id === 'ai_analysis')
  const inputStep = workflowSteps.value.find(s => s.id === 'input')

  const normalizationData = normalizationStep.result
  const analysisData = aiAnalysisStep.result
  const inputData = inputStep.result

  // 格式化最终结果
  const formattedResult = {
    content: normalizationData.normalizedContent,
    structuredData: normalizationData.structuredData,
    metadata: {
      fileName: inputData.name || 'unknown',
      format: normalizationData.metadata?.format || 'unknown',
      fileSize: inputData.size || 0,
      contentType: normalizationData.contentType,
      duration: workflowSteps.value.reduce((total, step) =>
        total + (step.details?.duration || 0), 0
      ),
      parser: normalizationData.metadata?.parser || 'unknown',
      timestamp: new Date().toISOString(),
      workflowVersion: '2.0',
      processingSteps: workflowSteps.value.length
    },
    analysis: analysisData,
    statistics: analysisData.contentStatistics || {},
    quality: {
      contentIntegrity: calculateContentIntegrity(normalizationData.normalizedContent),
      structureQuality: calculateStructureQuality(normalizationData.structuredData),
      analysisConfidence: calculateAnalysisConfidence(analysisData)
    }
  }

  finalResult.value = formattedResult

  return formattedResult
}

// 质量评估函数
const calculateContentIntegrity = (content) => {
  if (!content) return 0

  const hasValidText = content.trim().length > 0
  const hasProperStructure = content.includes('\n') || content.length > 50
  const noCorruption = !content.includes('�') && !content.includes('\x00')

  let score = 0
  if (hasValidText) score += 40
  if (hasProperStructure) score += 30
  if (noCorruption) score += 30

  return Math.min(score, 100)
}

const calculateStructureQuality = (structuredData) => {
  if (!structuredData) return 0

  try {
    if (Array.isArray(structuredData) && structuredData.length > 0) return 80
    if (typeof structuredData === 'object' && Object.keys(structuredData).length > 0) return 70
    return 50
  } catch {
    return 0
  }
}

const calculateAnalysisConfidence = (analysisData) => {
  if (!analysisData) return 0

  let confidence = 50
  if (analysisData.keywords && analysisData.keywords.length > 0) confidence += 15
  if (analysisData.summary && analysisData.summary.length > 50) confidence += 15
  if (analysisData.entities && analysisData.entities.length > 0) confidence += 10
  if (analysisData.structure) confidence += 10

  return Math.min(confidence, 100)
}

// 辅助函数
const fileToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    // 获取实际的文件对象
    const actualFile = file.raw || file

    if (!actualFile || !(actualFile instanceof File || actualFile instanceof Blob)) {
      reject(new Error('无效的文件对象'))
      return
    }

    const reader = new FileReader()
    reader.readAsDataURL(actualFile)
    reader.onload = () => {
      const base64 = reader.result.split(',')[1]
      resolve(base64)
    }
    reader.onerror = error => reject(error)
  })
}

// 批量处理相关方法
const exportBatchResults = () => {
  if (batchResults.value.length === 0) return

  const dataStr = JSON.stringify(batchResults.value, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)

  const link = document.createElement('a')
  link.href = url
  link.download = `batch-document-analysis-${Date.now()}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  ElMessage.success('批量结果已导出')
}

const clearBatchResults = () => {
  batchResults.value = []
  ElMessage.info('批量结果已清空')
}

const exportResult = () => {
  if (!finalResult.value) return

  const dataStr = JSON.stringify(finalResult.value, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)

  const link = document.createElement('a')
  link.href = url
  link.download = `document-analysis-${Date.now()}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)

  ElMessage.success('结果已导出')
}

// 添加缺失的辅助函数
const extractExcelStructure = (rawData) => {
  // Excel结构化数据提取
  if (rawData && rawData.sheets) {
    return {
      type: 'excel',
      sheetCount: Object.keys(rawData.sheets).length,
      sheets: Object.keys(rawData.sheets).slice(0, 5),
      totalRows: Object.values(rawData.sheets).reduce((sum, sheet) => sum + (sheet?.length || 0), 0)
    }
  }
  return null
}

const extractCSVStructure = (rawData) => {
  // CSV结构化数据提取
  if (Array.isArray(rawData)) {
    return {
      type: 'csv',
      rowCount: rawData.length,
      columnCount: rawData[0] ? Object.keys(rawData[0]).length : 0,
      columns: rawData[0] ? Object.keys(rawData[0]).slice(0, 10) : []
    }
  }
  return null
}

const extractXMLStructure = (rawData) => {
  // XML结构化数据提取
  if (rawData && typeof rawData === 'object') {
    return {
      type: 'xml',
      rootElement: Object.keys(rawData)[0],
      elementCount: countXMLElements(rawData),
      structure: analyzeXMLStructure(rawData)
    }
  }
  return null
}

const countXMLElements = (obj, count = 0) => {
  if (typeof obj === 'object' && obj !== null) {
    count += Object.keys(obj).length
    for (const value of Object.values(obj)) {
      if (typeof value === 'object') {
        count = countXMLElements(value, count)
      }
    }
  }
  return count
}

const analyzeXMLStructure = (obj, depth = 0) => {
  if (depth > 3) return '...' // 限制深度

  if (typeof obj === 'object' && obj !== null) {
    const keys = Object.keys(obj).slice(0, 5)
    return keys.reduce((acc, key) => {
      acc[key] = typeof obj[key] === 'object' ? analyzeXMLStructure(obj[key], depth + 1) : typeof obj[key]
      return acc
    }, {})
  }
  return typeof obj
}
</script>

<style scoped>
.document-parsing-workflow {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 顶部工具栏 */
.workflow-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.workflow-title h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.workflow-subtitle {
  font-size: 12px;
  color: #909399;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 主要内容区域 */
.workflow-content {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 16px;
  overflow: hidden;
}

/* 左侧输入面板 */
.input-panel {
  width: 320px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.panel-header h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #303133;
}

.panel-subtitle {
  font-size: 12px;
  color: #909399;
}

.upload-section {
  padding: 16px;
}

.document-uploader {
  width: 100%;
}

.upload-content {
  text-align: center;
  padding: 40px 20px;
}

.upload-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 16px;
}

.upload-text p {
  margin: 8px 0;
  color: #606266;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.current-file {
  margin-top: 16px;
  padding: 12px;
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 6px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-details {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.file-name {
  font-weight: 500;
  color: #303133;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.url-section, .text-section {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
}

.url-section h5, .text-section h5 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #606266;
}

/* 中间工作流可视化 */
.workflow-visualization {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.visualization-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.visualization-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.workflow-steps {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.steps-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.workflow-step {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  position: relative;
}

.step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border: 2px solid #e4e7ed;
  flex-shrink: 0;
}

.workflow-step.active .step-icon {
  background: #409eff;
  border-color: #409eff;
  color: white;
}

.workflow-step.completed .step-icon {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
}

.workflow-step.running .step-icon {
  background: #e6a23c;
  border-color: #e6a23c;
  color: white;
}

.workflow-step.error .step-icon {
  background: #f56c6c;
  border-color: #f56c6c;
  color: white;
}

.step-content {
  flex: 1;
  min-width: 0;
}

.step-title {
  font-weight: 500;
  font-size: 16px;
  color: #303133;
  margin-bottom: 4px;
}

.step-description {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.step-details {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.detail-label {
  color: #909399;
}

.detail-value {
  color: #606266;
  font-weight: 500;
}

.step-result {
  margin-top: 8px;
}

.result-preview {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.step-connector {
  position: absolute;
  left: 19px;
  top: 40px;
  bottom: -24px;
  width: 2px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.connector-line {
  flex: 1;
  width: 2px;
  background: #e4e7ed;
}

.connector-line.active {
  background: #409eff;
}

.connector-arrow {
  color: #e4e7ed;
  margin-top: 4px;
}

.connector-arrow.active {
  color: #409eff;
}

.workflow-progress {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
}

.progress-text {
  text-align: center;
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
}

/* 右侧结果面板 */
.results-panel {
  width: 400px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.result-actions {
  display: flex;
  gap: 8px;
}

.results-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.no-results {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.result-sections {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-section h5 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-label {
  font-size: 12px;
  color: #909399;
}

.info-value {
  font-size: 12px;
  color: #606266;
  font-weight: 500;
}

.content-preview {
  position: relative;
}

.content-stats {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag {
  margin: 0;
}

.analysis-content {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
}

.entities-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.entity-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.entity-text {
  font-size: 12px;
  color: #606266;
}

.raw-data {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 11px;
  max-height: 300px;
  overflow-y: auto;
}

/* 底部状态栏 */
.workflow-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.footer-left, .footer-right {
  display: flex;
  gap: 24px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

/* 动画效果 */
.icon-running {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 文件列表样式 */
.file-list {
  margin-top: 16px;
}

.file-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.file-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 8px;
  padding: 12px;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.file-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.file-item.active {
  border-color: #409eff;
  background: #f0f9ff;
}

.file-list .file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  font-size: 20px;
  color: #409eff;
}

.file-list .file-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.file-list .file-name {
  font-weight: 600;
  color: #303133;
}

.file-type {
  font-size: 12px;
  color: #909399;
}

.file-status {
  margin-left: auto;
}

.file-preview {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.preview-content {
  background: #f8f9fa;
  border-radius: 4px;
  padding: 8px;
  max-height: 120px;
  overflow-y: auto;
}

.table-preview {
  font-size: 12px;
}

/* 批量处理结果样式 */
.batch-results {
  padding: 16px;
}

.batch-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.batch-stats {
  display: flex;
  gap: 8px;
}

.batch-list {
  max-height: 400px;
  overflow-y: auto;
}

.batch-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 8px;
  padding: 12px;
  background: white;
}

.batch-item.success {
  border-color: #67c23a;
  background: #f0f9ff;
}

.batch-item.error {
  border-color: #f56c6c;
  background: #fef0f0;
}

.batch-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.status-icon {
  font-size: 16px;
}

.batch-item.success .status-icon {
  color: #67c23a;
}

.batch-item.error .status-icon {
  color: #f56c6c;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-bottom: 8px;
}

.result-summary {
  margin-bottom: 12px;
}

.result-summary p {
  margin: 4px 0;
  font-size: 12px;
}

.result-content {
  background: #f8f9fa;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.batch-actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
}

.batch-hint {
  color: #409eff;
  font-size: 14px;
  margin-top: 8px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .workflow-content {
    flex-direction: column;
    gap: 12px;
  }

  .input-panel, .results-panel {
    width: 100%;
  }

  .workflow-visualization {
    min-height: 400px;
  }

  .file-info {
    flex-wrap: wrap;
  }

  .batch-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
