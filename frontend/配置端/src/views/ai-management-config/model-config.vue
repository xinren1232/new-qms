<template>
  <div class="model-config">
    <div class="page-header">
      <h2>AI模型配置管理</h2>
      <p class="text-muted">管理和配置可用的AI模型，支持多种模型提供商</p>
    </div>

    <!-- 当前模型状态 -->
    <div class="current-model-card">
      <div class="card">
        <div class="card-header">
          <h5>当前活跃模型</h5>
          <el-button type="primary" size="small" @click="refreshModels">
            <i class="el-icon-refresh" /> 刷新
          </el-button>
        </div>
        <div class="card-body">
          <div v-if="currentModel" class="model-info">
            <div class="model-name">{{ currentModel.name }}</div>
            <div class="model-details">
              <span class="provider">{{ currentModel.provider }}</span>
              <span class="context-length">{{ currentModel.contextLength }}</span>
            </div>
            <div class="model-features">
              <el-tag v-if="currentModel.features.multimodal" type="success" size="small">多模态</el-tag>
              <el-tag v-if="currentModel.features.vision" type="primary" size="small">视觉识别</el-tag>
              <el-tag v-if="currentModel.features.toolCalls" type="warning" size="small">工具调用</el-tag>
              <el-tag v-if="currentModel.features.streaming" type="info" size="small">流式响应</el-tag>
              <el-tag v-if="currentModel.features.reasoning" type="danger" size="small">推理能力</el-tag>
              <el-tag v-if="currentModel.features.external" type="info" size="small">外网可用</el-tag>
              <!-- 显示自定义标签 -->
              <el-tag
                v-for="tag in currentModel.tags"
                :key="tag"
                type="info"
                size="small"
                effect="plain"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 模型列表 -->
    <div class="models-list">
      <div class="card">
        <div class="card-header">
          <h5>可用模型列表</h5>
          <div class="header-actions">
            <el-input
              v-model="searchText"
              placeholder="搜索模型..."
              size="small"
              style="width: 200px;"
              clearable
            >
              <template #prefix>
                <i class="el-icon-search" />
              </template>
            </el-input>
          </div>
        </div>
        <div class="card-body">
          <div class="models-grid">
            <div
              v-for="model in filteredModels"
              :key="model.id"
              class="model-card"
              :class="{ active: model.id === currentModelId }"
            >
              <div class="model-header">
                <h6>{{ model.name }}</h6>
                <el-button
                  v-if="model.id !== currentModelId"
                  type="primary"
                  size="mini"
                  @click="switchModel(model.id)"
                >
                  切换
                </el-button>
                <el-tag v-else type="success" size="mini">当前</el-tag>
              </div>
              
              <div class="model-info">
                <div class="provider">{{ model.provider }}</div>
                <div class="model-id">{{ model.model }}</div>
                <div class="context-length">上下文: {{ model.contextLength }}</div>
              </div>

              <div class="model-features">
                <el-tag v-if="model.features.multimodal" type="success" size="mini">多模态</el-tag>
                <el-tag v-if="model.features.vision" type="primary" size="mini">视觉</el-tag>
                <el-tag v-if="model.features.toolCalls" type="warning" size="mini">工具</el-tag>
                <el-tag v-if="model.features.streaming" type="info" size="mini">流式</el-tag>
                <el-tag v-if="model.features.reasoning" type="danger" size="mini">推理</el-tag>
                <el-tag v-if="model.features.external" type="info" size="mini">外网可用</el-tag>
                <!-- 显示自定义标签 -->
                <el-tag
                  v-for="tag in model.tags"
                  :key="tag"
                  type="info"
                  size="mini"
                  effect="plain"
                >
                  {{ tag }}
                </el-tag>
              </div>

              <div class="model-params">
                <div class="param">
                  <span>最大Token:</span>
                  <span>{{ model.maxTokens }}</span>
                </div>
                <div class="param">
                  <span>温度:</span>
                  <span>{{ model.temperature }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 模型测试 -->
    <div class="model-test">
      <div class="card">
        <div class="card-header">
          <h5>模型测试</h5>
        </div>
        <div class="card-body">
          <div class="test-form">
            <el-form :model="testForm" label-width="100px">
              <el-form-item label="测试模型">
                <el-select v-model="testForm.modelId" placeholder="选择要测试的模型">
                  <el-option
                    v-for="model in availableModels"
                    :key="model.id"
                    :label="model.name"
                    :value="model.id"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="测试消息">
                <el-input
                  v-model="testForm.message"
                  type="textarea"
                  :rows="3"
                  placeholder="输入测试消息..."
                />
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" :loading="testing" @click="testModel">
                  测试模型
                </el-button>
                <el-button @click="clearTest">清空</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <div v-if="testResult" class="test-result">
            <h6>测试结果:</h6>
            <div class="result-content">{{ testResult.response }}</div>
            <div class="result-meta">
              <span>响应时间: {{ testResult.responseTime }}ms</span>
              <span>Token使用: {{ testResult.tokensUsed }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, getCurrentInstance } from 'vue'
// 使用Vue 2的Element UI消息组件
// import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'ModelConfig',
  setup() {
    const { proxy } = getCurrentInstance()
    const availableModels = ref([])
    const currentModel = ref(null)
    const currentModelId = ref('')
    const searchText = ref('')
    const testing = ref(false)
    
    const testForm = reactive({
      modelId: '',
      message: '你好，请介绍一下你的功能特点。'
    })
    
    const testResult = ref(null)

    // 过滤模型列表
    const filteredModels = computed(() => {
      if (!searchText.value) return availableModels.value

      return availableModels.value.filter(model =>
        model.name.toLowerCase().includes(searchText.value.toLowerCase()) ||
        model.provider.toLowerCase().includes(searchText.value.toLowerCase())
      )
    })

    // 获取模型列表
    const fetchModels = async () => {
      try {
        const response = await fetch('/api/models')
        const data = await response.json()
        
        if (data.success) {
          availableModels.value = data.data.models
          currentModelId.value = data.data.current_model
          currentModel.value = availableModels.value.find(m => m.id === currentModelId.value)
        }
      } catch (error) {
        if (process.env.NODE_ENV === 'development') {
          console.error('获取模型列表失败:', error)
        }
        proxy.$message.error('获取模型列表失败')
      }
    }

    // 刷新模型列表
    const refreshModels = () => {
      fetchModels()
      proxy.$message.success('模型列表已刷新')
    }

    // 切换模型
    const switchModel = async (modelId) => {
      try {
        const response = await fetch('/api/models/switch', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({ model_id: modelId })
        })
        
        const data = await response.json()
        
        if (data.success) {
          currentModelId.value = modelId
          currentModel.value = availableModels.value.find(m => m.id === modelId)
          proxy.$message.success(data.message)
        } else {
          proxy.$message.error(data.message)
        }
      } catch (error) {
        if (process.env.NODE_ENV === 'development') {
          console.error('切换模型失败:', error)
        }
        proxy.$message.error('切换模型失败')
      }
    }

    // 测试模型
    const testModel = async () => {
      if (!testForm.modelId || !testForm.message) {
        proxy.$message.warning('请选择模型并输入测试消息')

        return
      }

      testing.value = true
      testResult.value = null

      try {
        const response = await fetch('/api/chat', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            message: testForm.message,
            model_config: availableModels.value.find(m => m.id === testForm.modelId)
          })
        })

        const data = await response.json()

        if (data.success) {
          testResult.value = {
            response: data.data.response,
            responseTime: data.data.response_time,
            tokensUsed: data.data.model_info.tokens_used?.total_tokens || 'N/A'
          }
          proxy.$message.success('模型测试完成')
        } else {
          proxy.$message.error(data.message)
        }
      } catch (error) {
        if (process.env.NODE_ENV === 'development') {
          console.error('模型测试失败:', error)
        }
        proxy.$message.error('模型测试失败')
      } finally {
        testing.value = false
      }
    }

    // 清空测试
    const clearTest = () => {
      testForm.message = '你好，请介绍一下你的功能特点。'
      testResult.value = null
    }

    onMounted(() => {
      fetchModels()
    })

    return {
      availableModels,
      currentModel,
      currentModelId,
      searchText,
      filteredModels,
      testForm,
      testResult,
      testing,
      refreshModels,
      switchModel,
      testModel,
      clearTest
    }
  }
}
</script>

<style scoped>
.model-config {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.current-model-card,
.models-list,
.model-test {
  margin-bottom: 20px;
}

.card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;
}

.card-header {
  padding: 15px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h5 {
  margin: 0;
  color: #303133;
}

.card-body {
  padding: 20px;
}

.model-info .model-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.model-details {
  margin-bottom: 12px;
}

.model-details span {
  margin-right: 15px;
  color: #606266;
}

.model-features {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.models-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.model-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  transition: all 0.3s;
}

.model-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.model-card.active {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

.model-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.model-header h6 {
  margin: 0;
  color: #303133;
}

.model-info {
  margin-bottom: 12px;
}

.model-info div {
  margin-bottom: 4px;
  color: #606266;
  font-size: 14px;
}

.model-features {
  margin-bottom: 12px;
}

.model-params {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.test-result {
  margin-top: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.test-result h6 {
  margin: 0 0 12px 0;
  color: #303133;
}

.result-content {
  background: #fff;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 8px;
  white-space: pre-wrap;
}

.result-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}
</style>
