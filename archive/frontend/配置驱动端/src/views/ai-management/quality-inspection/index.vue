<template>
  <div class="quality-inspection">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>AI质量检测</h2>
      <p>基于配置驱动的智能质量检测系统</p>
      <div class="header-actions">
        <el-button type="primary" @click="startInspection" :loading="inspecting">
          <el-icon><VideoPlay /></el-icon>
          开始检测
        </el-button>
        <el-button @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button @click="exportReport">
          <el-icon><Download /></el-icon>
          导出报告
        </el-button>
      </div>
    </div>

    <!-- 配置驱动的搜索组件 -->
    <ConfigDrivenSearch
      v-if="searchConfig"
      :config="searchConfig"
      @search="handleSearch"
      class="search-section"
    />

    <!-- 实时状态卡片 -->
    <el-row :gutter="20" class="status-cards">
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-item">
            <div class="status-icon success">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="status-content">
              <div class="status-value">{{ statistics.passedCount }}</div>
              <div class="status-label">检测通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-item">
            <div class="status-icon warning">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="status-content">
              <div class="status-value">{{ statistics.warningCount }}</div>
              <div class="status-label">需要关注</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-item">
            <div class="status-icon error">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="status-content">
              <div class="status-value">{{ statistics.failedCount }}</div>
              <div class="status-label">检测失败</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="status-card">
          <div class="status-item">
            <div class="status-icon info">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="status-content">
              <div class="status-value">{{ statistics.processingCount }}</div>
              <div class="status-label">检测中</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 配置驱动的工具栏 -->
    <ConfigDrivenToolbar
      v-if="toolbarConfig"
      :config="toolbarConfig"
      @action="handleToolbarAction"
      class="toolbar-section"
    />

    <!-- 配置驱动的数据表格 -->
    <ConfigDrivenTable
      v-if="tableConfig"
      :config="tableConfig"
      :data="tableData"
      :loading="loading"
      @row-click="handleRowClick"
      @selection-change="handleSelectionChange"
      class="table-section"
    />

    <!-- 检测详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="检测详情"
      width="80%"
      :before-close="handleDetailClose"
    >
      <InspectionDetail
        v-if="selectedInspection"
        :inspection="selectedInspection"
        @update="handleInspectionUpdate"
      />
    </el-dialog>

    <!-- AI分析对话框 -->
    <el-dialog
      v-model="analysisDialogVisible"
      title="AI智能分析"
      width="70%"
    >
      <AIAnalysisPanel
        v-if="selectedInspection"
        :inspection="selectedInspection"
        @analysis-complete="handleAnalysisComplete"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useConfigStore } from '@/stores/config'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  VideoPlay,
  Refresh,
  Download,
  CircleCheck,
  Warning,
  CircleClose,
  Clock
} from '@element-plus/icons-vue'

// 导入配置驱动组件
import ConfigDrivenSearch from '@/components/ConfigDriven/Search.vue'
import ConfigDrivenToolbar from '@/components/ConfigDriven/Toolbar.vue'
import ConfigDrivenTable from '@/components/ConfigDriven/Table.vue'

// 导入业务组件
import InspectionDetail from './components/InspectionDetail.vue'
import AIAnalysisPanel from './components/AIAnalysisPanel.vue'

// 使用配置store
const configStore = useConfigStore()

// 响应式数据
const loading = ref(false)
const inspecting = ref(false)
const tableData = ref([])
const selectedInspection = ref(null)
const detailDialogVisible = ref(false)
const analysisDialogVisible = ref(false)

// 统计数据
const statistics = reactive({
  passedCount: 0,
  warningCount: 0,
  failedCount: 0,
  processingCount: 0
})

// 配置驱动的配置项
const searchConfig = computed(() => {
  return configStore.getModuleConfig('qualityInspection')?.search || null
})

const toolbarConfig = computed(() => {
  return configStore.getModuleConfig('qualityInspection')?.toolbar || null
})

const tableConfig = computed(() => {
  return configStore.getModuleConfig('qualityInspection')?.table || null
})

// 生命周期
onMounted(async () => {
  await initializePage()
})

// 方法
const initializePage = async () => {
  try {
    loading.value = true
    
    // 确保配置已加载
    if (!configStore.configs.qualityInspection) {
      await configStore.initializeConfig()
    }
    
    // 加载数据
    await loadInspectionData()
    updateStatistics()
    
  } catch (error) {
    console.error('页面初始化失败:', error)
    ElMessage.error('页面初始化失败')
  } finally {
    loading.value = false
  }
}

const loadInspectionData = async () => {
  // 模拟加载检测数据
  const mockData = [
    {
      id: 'INS001',
      productName: '智能手机屏幕',
      batchNumber: 'B20240130001',
      inspectionTime: '2024-01-30 10:30:00',
      status: 'passed',
      qualityScore: 0.96,
      inspector: 'AI检测系统',
      issues: []
    },
    {
      id: 'INS002',
      productName: '电路板组件',
      batchNumber: 'B20240130002',
      inspectionTime: '2024-01-30 11:15:00',
      status: 'warning',
      qualityScore: 0.87,
      inspector: 'AI检测系统',
      issues: ['尺寸偏差', '表面瑕疵']
    },
    {
      id: 'INS003',
      productName: '金属外壳',
      batchNumber: 'B20240130003',
      inspectionTime: '2024-01-30 12:00:00',
      status: 'failed',
      qualityScore: 0.72,
      inspector: 'AI检测系统',
      issues: ['材料不合格', '尺寸超差', '表面缺陷']
    }
  ]
  
  tableData.value = mockData
}

const updateStatistics = () => {
  const data = tableData.value
  statistics.passedCount = data.filter(item => item.status === 'passed').length
  statistics.warningCount = data.filter(item => item.status === 'warning').length
  statistics.failedCount = data.filter(item => item.status === 'failed').length
  statistics.processingCount = data.filter(item => item.status === 'processing').length
}

const startInspection = async () => {
  try {
    inspecting.value = true
    ElMessage.info('正在启动AI质量检测...')
    
    // 模拟检测过程
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    ElMessage.success('AI质量检测已启动')
    await loadInspectionData()
    updateStatistics()
    
  } catch (error) {
    console.error('启动检测失败:', error)
    ElMessage.error('启动检测失败')
  } finally {
    inspecting.value = false
  }
}

const refreshData = async () => {
  await loadInspectionData()
  updateStatistics()
  ElMessage.success('数据已刷新')
}

const exportReport = () => {
  ElMessage.info('正在生成检测报告...')
  // 实现报告导出逻辑
}

const handleSearch = (searchParams) => {
  console.log('搜索参数:', searchParams)
  // 实现搜索逻辑
}

const handleToolbarAction = (action) => {
  console.log('工具栏操作:', action)
  // 实现工具栏操作逻辑
}

const handleRowClick = (row) => {
  selectedInspection.value = row
  detailDialogVisible.value = true
}

const handleSelectionChange = (selection) => {
  console.log('选择变更:', selection)
}

const handleDetailClose = () => {
  detailDialogVisible.value = false
  selectedInspection.value = null
}

const handleInspectionUpdate = (updatedInspection) => {
  // 更新检测数据
  const index = tableData.value.findIndex(item => item.id === updatedInspection.id)
  if (index !== -1) {
    tableData.value[index] = updatedInspection
    updateStatistics()
  }
}

const handleAnalysisComplete = (analysisResult) => {
  console.log('AI分析完成:', analysisResult)
  analysisDialogVisible.value = false
  ElMessage.success('AI分析完成')
}
</script>

<style scoped>
.quality-inspection {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.search-section {
  margin-bottom: 20px;
}

.status-cards {
  margin-bottom: 20px;
}

.status-card {
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.status-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.status-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.status-icon.success {
  background: #f0f9ff;
  color: #67c23a;
}

.status-icon.warning {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-icon.error {
  background: #fef0f0;
  color: #f56c6c;
}

.status-icon.info {
  background: #f4f4f5;
  color: #909399;
}

.status-content {
  flex: 1;
}

.status-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.status-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.toolbar-section {
  margin-bottom: 20px;
}

.table-section {
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>
