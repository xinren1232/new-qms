<template>
  <div class="file-library">
    <div class="page-header">
      <h2>文件库管理</h2>
      <p class="text-muted">管理系统文件库，包括文件上传、分类、权限等</p>
    </div>

    <!-- 文件统计 -->
    <div class="file-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><Files /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">总文件数</div>
                <div class="stat-value">{{ stats.totalFiles }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon size">
                <el-icon><Coin /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">总大小</div>
                <div class="stat-value">{{ stats.totalSize }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon today">
                <el-icon><Calendar /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">今日上传</div>
                <div class="stat-value">{{ stats.todayUploads }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon categories">
                <el-icon><FolderOpened /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-title">分类数量</div>
                <div class="stat-value">{{ stats.categories }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" @click="handleUpload">
          <el-icon><Upload /></el-icon>
          上传文件
        </el-button>
        <el-button @click="handleCreateFolder">
          <el-icon><FolderAdd /></el-icon>
          新建文件夹
        </el-button>
        <el-button @click="handleBatchDelete" :disabled="!selectedFiles.length">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-input
          v-model="searchText"
          placeholder="搜索文件..."
          style="width: 200px;"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="viewMode" style="width: 120px;">
          <el-option label="列表视图" value="list" />
          <el-option label="网格视图" value="grid" />
        </el-select>
      </div>
    </div>

    <!-- 文件夹导航 -->
    <div class="breadcrumb">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item @click="handleNavigate('/')">根目录</el-breadcrumb-item>
        <el-breadcrumb-item
          v-for="(folder, index) in currentPath"
          :key="index"
          @click="handleNavigate(folder.path)"
        >
          {{ folder.name }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 文件列表 -->
    <div class="file-container">
      <el-card>
        <!-- 列表视图 -->
        <div v-if="viewMode === 'list'" class="list-view">
          <vxe-table
            ref="fileTableRef"
            :data="filteredFiles"
            :loading="loading"
            border
            stripe
            resizable
            height="500"
            :checkbox-config="{ checkField: 'checked' }"
            @checkbox-change="handleSelectionChange"
            @checkbox-all="handleSelectionChange"
          >
            <vxe-column type="checkbox" width="60" />
            <vxe-column field="name" title="文件名" min-width="200">
              <template #default="{ row }">
                <div class="file-name-cell" @click="handleFileClick(row)">
                  <el-icon class="file-icon" :class="getFileIconClass(row.type)">
                    <component :is="getFileIcon(row.type)" />
                  </el-icon>
                  <span class="file-name">{{ row.name }}</span>
                </div>
              </template>
            </vxe-column>
            <vxe-column field="type" title="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getFileTypeTagType(row.type)">
                  {{ getFileTypeText(row.type) }}
                </el-tag>
              </template>
            </vxe-column>
            <vxe-column field="size" title="大小" width="100" />
            <vxe-column field="uploadTime" title="上传时间" width="160" />
            <vxe-column field="uploader" title="上传者" width="120" />
            <vxe-column title="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" size="small" @click="handleDownload(row)">
                  下载
                </el-button>
                <el-button size="small" @click="handlePreview(row)">
                  预览
                </el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">
                  删除
                </el-button>
              </template>
            </vxe-column>
          </vxe-table>
        </div>

        <!-- 网格视图 -->
        <div v-else class="grid-view">
          <el-row :gutter="20">
            <el-col :span="4" v-for="file in filteredFiles" :key="file.id">
              <div class="file-card" @click="handleFileClick(file)">
                <div class="file-preview">
                  <el-icon class="file-icon-large" :class="getFileIconClass(file.type)">
                    <component :is="getFileIcon(file.type)" />
                  </el-icon>
                </div>
                <div class="file-info">
                  <div class="file-name" :title="file.name">{{ file.name }}</div>
                  <div class="file-meta">
                    <span class="file-size">{{ file.size }}</span>
                    <span class="file-time">{{ file.uploadTime }}</span>
                  </div>
                </div>
                <div class="file-actions">
                  <el-button type="primary" size="small" @click.stop="handleDownload(file)">
                    <el-icon><Download /></el-icon>
                  </el-button>
                  <el-button size="small" @click.stop="handlePreview(file)">
                    <el-icon><View /></el-icon>
                  </el-button>
                  <el-button type="danger" size="small" @click.stop="handleDelete(file)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="600px">
      <el-upload
        ref="uploadRef"
        :action="uploadUrl"
        :file-list="fileList"
        :auto-upload="false"
        multiple
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持jpg/png/gif/pdf/doc/xls等格式，单个文件不超过100MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmUpload">确认上传</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Files,
  Coin,
  Calendar,
  FolderOpened,
  Upload,
  FolderAdd,
  Delete,
  Search,
  Download,
  View,
  Document,
  Picture,
  VideoPlay,
  Headset,
  Files as FilesIcon,
  UploadFilled
} from '@element-plus/icons-vue'

// 响应式数据
const fileTableRef = ref()
const uploadRef = ref()
const loading = ref(false)
const searchText = ref('')
const viewMode = ref('list')
const uploadDialogVisible = ref(false)
const selectedFiles = ref([])
const fileList = ref([])
const uploadUrl = '/api/upload'

// 统计数据
const stats = reactive({
  totalFiles: '2,456',
  totalSize: '15.6GB',
  todayUploads: 23,
  categories: 8
})

// 当前路径
const currentPath = ref([
  { name: '文档', path: '/documents' },
  { name: 'AI训练数据', path: '/documents/ai-training' }
])

// 文件数据
const files = ref([
  {
    id: 1,
    name: 'QMS系统需求文档.pdf',
    type: 'pdf',
    size: '2.5MB',
    uploadTime: '2024-01-01 10:30',
    uploader: '张三',
    checked: false
  },
  {
    id: 2,
    name: '产品图片.jpg',
    type: 'image',
    size: '1.2MB',
    uploadTime: '2024-01-01 09:15',
    uploader: '李四',
    checked: false
  },
  {
    id: 3,
    name: '培训视频.mp4',
    type: 'video',
    size: '156MB',
    uploadTime: '2024-01-01 08:45',
    uploader: '王五',
    checked: false
  },
  {
    id: 4,
    name: '数据表格.xlsx',
    type: 'excel',
    size: '856KB',
    uploadTime: '2024-01-01 08:20',
    uploader: '赵六',
    checked: false
  }
])

// 计算属性
const filteredFiles = computed(() => {
  if (!searchText.value) return files.value
  return files.value.filter(file =>
    file.name.toLowerCase().includes(searchText.value.toLowerCase())
  )
})

// 方法
const getFileIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    'pdf': Document,
    'doc': Document,
    'docx': Document,
    'image': Picture,
    'jpg': Picture,
    'png': Picture,
    'gif': Picture,
    'video': VideoPlay,
    'mp4': VideoPlay,
    'avi': VideoPlay,
    'audio': Headset,
    'mp3': Headset,
    'wav': Headset,
    'excel': FilesIcon,
    'xlsx': FilesIcon,
    'xls': FilesIcon
  }
  return iconMap[type] || Document
}

const getFileIconClass = (type: string) => {
  const classMap: Record<string, string> = {
    'pdf': 'pdf',
    'doc': 'doc',
    'docx': 'doc',
    'image': 'image',
    'jpg': 'image',
    'png': 'image',
    'gif': 'image',
    'video': 'video',
    'mp4': 'video',
    'avi': 'video',
    'audio': 'audio',
    'mp3': 'audio',
    'wav': 'audio',
    'excel': 'excel',
    'xlsx': 'excel',
    'xls': 'excel'
  }
  return classMap[type] || 'default'
}

const getFileTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'pdf': 'danger',
    'doc': 'primary',
    'docx': 'primary',
    'image': 'success',
    'jpg': 'success',
    'png': 'success',
    'gif': 'success',
    'video': 'warning',
    'mp4': 'warning',
    'avi': 'warning',
    'audio': 'info',
    'mp3': 'info',
    'wav': 'info',
    'excel': 'success',
    'xlsx': 'success',
    'xls': 'success'
  }
  return typeMap[type] || 'default'
}

const getFileTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    'pdf': 'PDF',
    'doc': 'Word',
    'docx': 'Word',
    'image': '图片',
    'jpg': '图片',
    'png': '图片',
    'gif': '图片',
    'video': '视频',
    'mp4': '视频',
    'avi': '视频',
    'audio': '音频',
    'mp3': '音频',
    'wav': '音频',
    'excel': 'Excel',
    'xlsx': 'Excel',
    'xls': 'Excel'
  }
  return textMap[type] || type.toUpperCase()
}

const handleUpload = () => {
  uploadDialogVisible.value = true
}

const handleCreateFolder = () => {
  ElMessage.info('新建文件夹功能开发中...')
}

const handleBatchDelete = () => {
  if (!selectedFiles.value.length) {
    ElMessage.warning('请选择要删除的文件')
    return
  }

  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedFiles.value.length} 个文件吗？`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('批量删除成功')
  })
}

const handleNavigate = (path: string) => {
  ElMessage.info(`导航到: ${path}`)
}

const handleFileClick = (file: any) => {
  if (file.type === 'folder') {
    // 进入文件夹
    ElMessage.info(`进入文件夹: ${file.name}`)
  } else {
    // 预览文件
    handlePreview(file)
  }
}

const handleDownload = (file: any) => {
  ElMessage.success(`开始下载: ${file.name}`)
}

const handlePreview = (file: any) => {
  ElMessage.info(`预览文件: ${file.name}`)
}

const handleDelete = (file: any) => {
  ElMessageBox.confirm(
    `确定要删除文件 "${file.name}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    const index = files.value.findIndex(f => f.id === file.id)
    if (index > -1) {
      files.value.splice(index, 1)
      ElMessage.success('删除成功')
    }
  })
}

const handleSelectionChange = () => {
  selectedFiles.value = files.value.filter(file => file.checked)
}

const handleConfirmUpload = () => {
  if (!fileList.value.length) {
    ElMessage.warning('请选择要上传的文件')
    return
  }

  ElMessage.success('文件上传成功')
  uploadDialogVisible.value = false
  fileList.value = []
}

onMounted(() => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 1000)
})
</script>

<style scoped>
.file-library {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.text-muted {
  color: #909399;
  margin: 0;
  font-size: 14px;
}

/* 文件统计样式 */
.file-stats {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 20px;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.size {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.today {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.categories {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

/* 工具栏样式 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.toolbar-left {
  display: flex;
  gap: 10px;
}

.toolbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* 面包屑样式 */
.breadcrumb {
  margin-bottom: 20px;
  padding: 10px 15px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 文件容器样式 */
.file-container {
  margin-bottom: 20px;
}

/* 列表视图样式 */
.list-view {
  min-height: 500px;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.file-icon {
  font-size: 18px;
}

.file-icon.pdf {
  color: #f56c6c;
}

.file-icon.doc {
  color: #409eff;
}

.file-icon.image {
  color: #67c23a;
}

.file-icon.video {
  color: #e6a23c;
}

.file-icon.audio {
  color: #909399;
}

.file-icon.excel {
  color: #67c23a;
}

.file-name {
  font-weight: 500;
  color: #303133;
}

/* 网格视图样式 */
.grid-view {
  min-height: 500px;
  padding: 20px;
}

.file-card {
  background: white;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  margin-bottom: 20px;
}

.file-card:hover {
  border-color: #409eff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.file-preview {
  margin-bottom: 10px;
}

.file-icon-large {
  font-size: 48px;
}

.file-icon-large.pdf {
  color: #f56c6c;
}

.file-icon-large.doc {
  color: #409eff;
}

.file-icon-large.image {
  color: #67c23a;
}

.file-icon-large.video {
  color: #e6a23c;
}

.file-icon-large.audio {
  color: #909399;
}

.file-icon-large.excel {
  color: #67c23a;
}

.file-info {
  margin-bottom: 10px;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-meta {
  font-size: 12px;
  color: #909399;
}

.file-size {
  margin-right: 10px;
}

.file-actions {
  display: flex;
  justify-content: center;
  gap: 5px;
}

:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-card__header) {
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-breadcrumb__item) {
  cursor: pointer;
}

:deep(.el-breadcrumb__item:hover) {
  color: #409eff;
}

:deep(.el-upload-dragger) {
  border-radius: 8px;
}

:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-dialog__header) {
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}
</style>
