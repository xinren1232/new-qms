<template>
  <div class="evaluation-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>效果评测</h1>
        <p>欢迎来到效果评测，让我们开始吧！</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="startNewEvaluation">
          新建评测
        </el-button>
      </div>
    </div>

    <!-- 评测流程展示 -->
    <div class="evaluation-process" v-if="!currentEvaluation">
      <div class="process-header">
        <h2>评测流程</h2>
        <p>4个简单步骤，完成AI模型效果评测</p>
      </div>
      
      <div class="process-steps">
        <div class="step-card" v-for="(step, index) in evaluationSteps" :key="index">
          <div class="step-icon">
            <el-icon :size="32" :color="step.color">
              <component :is="step.icon" />
            </el-icon>
          </div>
          <div class="step-content">
            <h3>第{{ index + 1 }}步：{{ step.title }}</h3>
            <p>{{ step.description }}</p>
          </div>
          <div class="step-arrow" v-if="index < evaluationSteps.length - 1">
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <div class="quick-start">
        <el-button type="primary" size="large" @click="startNewEvaluation">
          立即评测
        </el-button>
        <p>新手必看</p>
      </div>
    </div>

    <!-- 评测进行中 -->
    <div class="evaluation-active" v-if="currentEvaluation">
      <!-- 步骤指示器 -->
      <div class="steps-indicator">
        <el-steps :active="currentStep" align-center>
          <el-step 
            v-for="(step, index) in evaluationSteps" 
            :key="index"
            :title="step.title"
            :icon="step.icon"
          />
        </el-steps>
      </div>

      <!-- 步骤内容 -->
      <div class="step-content-area">
        <!-- 第一步：挑选训练集 -->
        <div v-if="currentStep === 0" class="step-panel">
          <div class="panel-header">
            <h3>挑选训练集</h3>
            <p>选择合适的训练数据集进行模型评测</p>
          </div>
          <div class="dataset-selection">
            <div class="dataset-grid">
              <div 
                v-for="dataset in availableDatasets" 
                :key="dataset.id"
                class="dataset-card"
                :class="{ selected: selectedDataset?.id === dataset.id }"
                @click="selectDataset(dataset)"
              >
                <div class="dataset-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="dataset-info">
                  <h4>{{ dataset.name }}</h4>
                  <p>{{ dataset.description }}</p>
                  <div class="dataset-meta">
                    <span>{{ dataset.size }} 条数据</span>
                    <span>{{ dataset.type }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 第二步：上传评测数据 -->
        <div v-if="currentStep === 1" class="step-panel">
          <div class="panel-header">
            <h3>上传评测数据</h3>
            <p>上传您的评测数据文件，支持多种格式</p>
          </div>
          <div class="upload-area">
            <el-upload
              class="upload-dragger"
              drag
              :auto-upload="false"
              :on-change="handleFileChange"
              :show-file-list="true"
              accept=".txt,.csv,.json,.xlsx"
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 txt/csv/json/xlsx 格式文件，文件大小不超过 10MB
                </div>
              </template>
            </el-upload>
          </div>
        </div>

        <!-- 第三步：定制评测规则 -->
        <div v-if="currentStep === 2" class="step-panel">
          <div class="panel-header">
            <h3>定制评测规则</h3>
            <p>配置评测指标和规则，定制化您的评测方案</p>
          </div>
          <div class="rules-config">
            <el-form :model="evaluationRules" label-width="120px">
              <el-form-item label="评测指标">
                <el-checkbox-group v-model="evaluationRules.metrics">
                  <el-checkbox label="accuracy">准确率</el-checkbox>
                  <el-checkbox label="response_time">响应时间</el-checkbox>
                  <el-checkbox label="relevance">相关性</el-checkbox>
                  <el-checkbox label="fluency">流畅度</el-checkbox>
                  <el-checkbox label="safety">安全性</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="评测模型">
                <el-select v-model="evaluationRules.models" multiple placeholder="选择要评测的模型">
                  <el-option
                    v-for="model in availableModels"
                    :key="model.id"
                    :label="model.name"
                    :value="model.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="评测轮数">
                <el-input-number v-model="evaluationRules.rounds" :min="1" :max="10" />
              </el-form-item>
              <el-form-item label="并发数">
                <el-input-number v-model="evaluationRules.concurrency" :min="1" :max="5" />
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 第四步：查看评测结果 -->
        <div v-if="currentStep === 3" class="step-panel">
          <div class="panel-header">
            <h3>查看评测结果</h3>
            <p>评测完成，查看详细的评测报告和分析</p>
          </div>
          <div class="results-display">
            <div class="results-summary">
              <div class="summary-card" v-for="metric in resultSummary" :key="metric.name">
                <div class="metric-name">{{ metric.name }}</div>
                <div class="metric-value">{{ metric.value }}</div>
                <div class="metric-trend" :class="metric.trend">
                  <el-icon><component :is="metric.trend === 'up' ? 'TrendCharts' : 'Bottom'" /></el-icon>
                  {{ metric.change }}
                </div>
              </div>
            </div>
            
            <div class="results-charts">
              <div class="chart-container">
                <div id="accuracyChart" style="width: 100%; height: 300px;"></div>
              </div>
              <div class="chart-container">
                <div id="responseTimeChart" style="width: 100%; height: 300px;"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 步骤操作按钮 -->
      <div class="step-actions">
        <el-button v-if="currentStep > 0" @click="previousStep">上一步</el-button>
        <el-button 
          type="primary" 
          @click="nextStep"
          :disabled="!canProceed"
          :loading="processing"
        >
          {{ currentStep === 3 ? '完成评测' : '下一步' }}
        </el-button>
      </div>
    </div>

    <!-- 历史评测记录 -->
    <div class="evaluation-history" v-if="!currentEvaluation">
      <div class="history-header">
        <h3>评测历史</h3>
        <el-button text @click="refreshHistory">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
      
      <el-table :data="evaluationHistory" style="width: 100%">
        <el-table-column prop="name" label="评测名称" />
        <el-table-column prop="dataset" label="数据集" />
        <el-table-column prop="models" label="评测模型" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button size="small" @click="viewResults(scope.row)">查看结果</el-button>
            <el-button size="small" type="danger" @click="deleteEvaluationRecord(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  ArrowRight,
  Document,
  UploadFilled,
  Refresh,
  TrendCharts,
  Bottom,
  DataAnalysis,
  Upload,
  Setting,
  View
} from '@element-plus/icons-vue'
import {
  getDatasets,
  getEvaluationModels,
  uploadEvaluationFile,
  createEvaluation,
  startEvaluation,
  getEvaluationList,
  deleteEvaluation
} from '@/api/evaluation'

// 响应式数据
const currentEvaluation = ref(null)
const currentStep = ref(0)
const processing = ref(false)
const selectedDataset = ref(null)
const uploadedFiles = ref([])

// 评测步骤定义
const evaluationSteps = [
  {
    title: '挑选训练集',
    description: '选择合适的训练数据集',
    icon: 'DataAnalysis',
    color: '#409EFF'
  },
  {
    title: '上传评测数据',
    description: '上传您的评测数据',
    icon: 'Upload',
    color: '#67C23A'
  },
  {
    title: '定制评测规则',
    description: '配置评测指标和规则',
    icon: 'Setting',
    color: '#E6A23C'
  },
  {
    title: '查看评测结果',
    description: '查看详细的评测报告',
    icon: 'View',
    color: '#F56C6C'
  }
]

// 可用数据集
const availableDatasets = ref([])

// 可用模型
const availableModels = ref([])

// 评测规则
const evaluationRules = reactive({
  metrics: ['accuracy', 'response_time'],
  models: [],
  rounds: 3,
  concurrency: 2
})

// 评测结果摘要
const resultSummary = ref([
  { name: '平均准确率', value: '94.2%', trend: 'up', change: '+2.1%' },
  { name: '平均响应时间', value: '1.2s', trend: 'down', change: '-0.3s' },
  { name: '用户满意度', value: '4.8/5', trend: 'up', change: '+0.2' },
  { name: '安全性评分', value: '98.5%', trend: 'up', change: '+1.2%' }
])

// 评测历史
const evaluationHistory = ref([])

// 计算属性
const canProceed = computed(() => {
  switch (currentStep.value) {
    case 0:
      return selectedDataset.value !== null
    case 1:
      return uploadedFiles.value.length > 0
    case 2:
      return evaluationRules.metrics.length > 0 && evaluationRules.models.length > 0
    case 3:
      return true
    default:
      return false
  }
})

// 方法定义
const startNewEvaluation = () => {
  currentEvaluation.value = {
    id: Date.now(),
    name: `评测-${new Date().toLocaleDateString()}`,
    createdAt: new Date()
  }
  currentStep.value = 0
}

const selectDataset = (dataset) => {
  selectedDataset.value = dataset
}

const handleFileChange = async (file) => {
  try {
    const response = await uploadEvaluationFile(file.raw, (progressEvent) => {
      const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      console.log(`上传进度: ${progress}%`)
    })

    if (response.success) {
      uploadedFiles.value = [response.data]
      ElMessage.success('文件上传成功')
    }
  } catch (error) {
    ElMessage.error('文件上传失败: ' + error.message)
  }
}

const nextStep = async () => {
  if (currentStep.value === 3) {
    // 完成评测
    await completeEvaluation()
  } else {
    currentStep.value++
  }
}

const previousStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const completeEvaluation = async () => {
  processing.value = true
  try {
    // 创建评测任务
    const evaluationData = {
      name: currentEvaluation.value.name,
      datasetId: selectedDataset.value.id,
      models: evaluationRules.models,
      metrics: evaluationRules.metrics,
      rounds: evaluationRules.rounds,
      concurrency: evaluationRules.concurrency
    }

    const createResponse = await createEvaluation(evaluationData)
    if (createResponse.success) {
      // 开始评测
      const startResponse = await startEvaluation(createResponse.data.id)
      if (startResponse.success) {
        ElMessage.success('评测已开始！')
        await loadEvaluationHistory()
        currentEvaluation.value = null
        currentStep.value = 0
      }
    }
  } catch (error) {
    ElMessage.error('评测失败：' + error.message)
  } finally {
    processing.value = false
  }
}

const refreshHistory = async () => {
  await loadEvaluationHistory()
  ElMessage.success('历史记录已刷新')
}

const viewResults = (evaluation) => {
  ElMessage.info(`查看评测结果：${evaluation.name}`)
}

const deleteEvaluationRecord = async (evaluation) => {
  try {
    await ElMessageBox.confirm('确定要删除这个评测记录吗？', '确认删除', {
      type: 'warning'
    })

    const response = await deleteEvaluation(evaluation.id)
    if (response.success) {
      await loadEvaluationHistory()
      ElMessage.success('删除成功')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + error.message)
    }
  }
}

const getStatusType = (status) => {
  const typeMap = {
    '已完成': 'success',
    '进行中': 'warning',
    '失败': 'danger'
  }
  return typeMap[status] || 'info'
}

// 数据加载函数
const loadDatasets = async () => {
  try {
    const response = await getDatasets()
    if (response.success) {
      availableDatasets.value = response.data
    }
  } catch (error) {
    console.error('加载数据集失败:', error)
  }
}

const loadModels = async () => {
  try {
    const response = await getEvaluationModels()
    if (response.success) {
      availableModels.value = response.data
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
  }
}

const loadEvaluationHistory = async () => {
  try {
    const response = await getEvaluationList()
    if (response.success) {
      evaluationHistory.value = response.data
    }
  } catch (error) {
    console.error('加载评测历史失败:', error)
  }
}

// 生命周期
onMounted(async () => {
  await Promise.all([
    loadDatasets(),
    loadModels(),
    loadEvaluationHistory()
  ])
})
</script>

<style scoped>
.evaluation-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.header-content h1 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 28px;
  font-weight: 600;
}

.header-content p {
  margin: 0;
  color: #606266;
  font-size: 16px;
}

.evaluation-process {
  background: white;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 24px;
}

.process-header h2 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 24px;
}

.process-header p {
  margin: 0 0 40px 0;
  color: #606266;
  font-size: 16px;
}

.process-steps {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 40px;
}

.step-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px;
  border-radius: 12px;
  background: #f8f9fa;
  min-width: 200px;
  position: relative;
}

.step-icon {
  margin-bottom: 16px;
  padding: 16px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.step-content h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.step-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
  text-align: center;
}

.step-arrow {
  position: absolute;
  right: -30px;
  top: 50%;
  transform: translateY(-50%);
  color: #ddd;
  font-size: 20px;
}

.quick-start {
  margin-top: 40px;
}

.quick-start p {
  margin-top: 12px;
  color: #909399;
  font-size: 14px;
}

.evaluation-active {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.steps-indicator {
  margin-bottom: 32px;
}

.step-content-area {
  min-height: 400px;
  margin-bottom: 24px;
}

.step-panel {
  max-width: 800px;
  margin: 0 auto;
}

.panel-header {
  text-align: center;
  margin-bottom: 32px;
}

.panel-header h3 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 20px;
  font-weight: 600;
}

.panel-header p {
  margin: 0;
  color: #606266;
  font-size: 16px;
}

.dataset-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.dataset-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.dataset-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
}

.dataset-card.selected {
  border-color: #409eff;
  background: #f0f9ff;
}

.dataset-icon {
  margin-right: 16px;
  padding: 12px;
  border-radius: 8px;
  background: #f5f7fa;
  color: #409eff;
}

.dataset-info h4 {
  margin: 0 0 8px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
}

.dataset-info p {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.dataset-meta {
  display: flex;
  gap: 12px;
}

.dataset-meta span {
  padding: 2px 8px;
  background: #e4e7ed;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
}

.upload-area {
  max-width: 600px;
  margin: 0 auto;
}

.rules-config {
  max-width: 600px;
  margin: 0 auto;
}

.results-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.summary-card {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  text-align: center;
}

.metric-name {
  color: #606266;
  font-size: 14px;
  margin-bottom: 8px;
}

.metric-value {
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
}

.metric-trend {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
}

.metric-trend.up {
  color: #67c23a;
}

.metric-trend.down {
  color: #f56c6c;
}

.results-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.chart-container {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
}

.step-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding-top: 24px;
  border-top: 1px solid #e4e7ed;
}

.evaluation-history {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.history-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
  font-weight: 600;
}
</style>
