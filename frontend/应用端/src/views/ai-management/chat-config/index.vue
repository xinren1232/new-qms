<template>
  <div class="chat-config-container">
    <el-card class="config-card">
      <template #header>
        <div class="card-header">
          <h3>
            <el-icon><Setting /></el-icon>
            AI聊天配置管理
          </h3>
          <el-button type="primary" @click="saveConfig" :loading="saving">
            <el-icon><Check /></el-icon>
            保存配置
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="config-tabs">
        <!-- 模型配置 -->
        <el-tab-pane label="模型配置" name="model">
          <el-form :model="config" :rules="rules" ref="configFormRef" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="API密钥" prop="apiKey">
                  <el-input 
                    v-model="config.apiKey" 
                    type="password" 
                    show-password
                    placeholder="请输入DeepSeek API密钥"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="AI服务商" prop="provider">
                  <el-select v-model="config.provider" placeholder="选择服务商" @change="onProviderChange">
                    <el-option label="DeepSeek" value="deepseek" />
                    <el-option label="Transsion AI" value="transsion" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="模型名称" prop="model">
                  <el-select v-model="config.model" placeholder="选择模型">
                    <el-option-group v-if="config.provider === 'deepseek'" label="DeepSeek模型">
                      <el-option label="DeepSeek Chat (V3-0324) - 外网可用" value="deepseek-chat" />
                      <el-option label="DeepSeek Reasoner (R1-0528) - 外网可用" value="deepseek-reasoner" />
                      <el-option label="deepseek-coder" value="deepseek-coder" />
                    </el-option-group>
                    <el-option-group v-if="config.provider === 'transsion'" label="Transsion AI模型">
                      <el-option label="GPT-4o" value="gpt-4o" />
                      <el-option label="GPT-4o-mini" value="gpt-4o-mini" />
                      <el-option label="Gemini-2.5-Pro-Thinking" value="gemini-2.5-pro-thinking" />
                      <el-option label="Claude-3.5-Sonnet" value="claude-3-5-sonnet-20241022" />
                    </el-option-group>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="API地址" prop="baseURL">
                  <el-input v-model="config.baseURL" placeholder="API基础地址" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="最大令牌数" prop="maxTokens">
                  <el-input-number 
                    v-model="config.maxTokens" 
                    :min="100" 
                    :max="8192" 
                    :step="100"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="温度参数" prop="temperature">
                  <el-slider 
                    v-model="config.temperature" 
                    :min="0" 
                    :max="2" 
                    :step="0.1"
                    show-input
                  />
                  <div class="param-desc">控制回答的随机性，0-2之间</div>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="请求超时" prop="timeout">
                  <el-input-number 
                    v-model="config.timeout" 
                    :min="5000" 
                    :max="60000" 
                    :step="1000"
                    style="width: 100%"
                  />
                  <div class="param-desc">请求超时时间（毫秒）</div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- 系统提示词 -->
        <el-tab-pane label="系统提示词" name="prompt">
          <el-form label-width="120px">
            <el-form-item label="系统角色">
              <el-input 
                v-model="config.systemPrompt" 
                type="textarea" 
                :rows="15"
                placeholder="定义AI助手的角色和行为..."
              />
              <div class="prompt-tips">
                <h4>提示词编写建议：</h4>
                <ul>
                  <li>明确定义AI助手的专业领域和角色</li>
                  <li>说明回答的风格和格式要求</li>
                  <li>设定回答的边界和限制</li>
                  <li>提供具体的示例和指导</li>
                </ul>
              </div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 快速问题 -->
        <el-tab-pane label="快速问题" name="questions">
          <div class="questions-section">
            <div class="section-header">
              <h4>预设问题管理</h4>
              <el-button type="primary" @click="addQuestion">
                <el-icon><Plus /></el-icon>
                添加问题
              </el-button>
            </div>

            <div class="questions-list">
              <div 
                v-for="(question, index) in config.quickQuestions" 
                :key="index"
                class="question-item"
              >
                <el-input 
                  v-model="config.quickQuestions[index]" 
                  placeholder="输入快速问题"
                />
                <el-button 
                  type="danger" 
                  text 
                  @click="removeQuestion(index)"
                  :disabled="config.quickQuestions.length <= 1"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 高级设置 -->
        <el-tab-pane label="高级设置" name="advanced">
          <el-form label-width="120px">
            <el-form-item label="启用流式输出">
              <el-switch v-model="config.stream" />
              <div class="param-desc">启用后将实时显示AI回答过程</div>
            </el-form-item>

            <el-form-item label="历史记录数">
              <el-input-number 
                v-model="config.historyLimit" 
                :min="1" 
                :max="50" 
                style="width: 200px"
              />
              <div class="param-desc">保留的对话历史轮数</div>
            </el-form-item>

            <el-form-item label="自动保存">
              <el-switch v-model="config.autoSave" />
              <div class="param-desc">自动保存对话记录</div>
            </el-form-item>

            <el-form-item label="敏感词过滤">
              <el-switch v-model="config.contentFilter" />
              <div class="param-desc">启用内容安全过滤</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 测试配置 -->
        <el-tab-pane label="测试配置" name="test">
          <div class="test-section">
            <el-form label-width="120px">
              <el-form-item label="测试消息">
                <el-input 
                  v-model="testMessage" 
                  placeholder="输入测试消息..."
                  style="width: 400px"
                />
                <el-button 
                  type="primary" 
                  @click="testConfig" 
                  :loading="testing"
                  style="margin-left: 10px"
                >
                  测试连接
                </el-button>
              </el-form-item>
            </el-form>

            <div v-if="testResult" class="test-result">
              <h4>测试结果：</h4>
              <el-alert 
                :type="testResult.success ? 'success' : 'error'"
                :title="testResult.success ? '连接成功' : '连接失败'"
                :description="testResult.message"
                show-icon
                :closable="false"
              />
              
              <div v-if="testResult.success && testResult.response" class="test-response">
                <h5>AI回答：</h5>
                <div class="response-content">{{ testResult.response }}</div>
                <div class="response-meta">
                  <span>响应时间: {{ testResult.responseTime }}ms</span>
                  <span>使用令牌: {{ testResult.tokensUsed }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 状态监控 -->
    <el-card class="status-card" style="margin-top: 20px">
      <template #header>
        <h3>
          <el-icon><Monitor /></el-icon>
          服务状态监控
        </h3>
      </template>

      <el-row :gutter="20">
        <el-col :span="6">
          <div class="status-item">
            <div class="status-label">API状态</div>
            <div class="status-value" :class="{ 'online': modelStatus.online }">
              {{ modelStatus.online ? '在线' : '离线' }}
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <div class="status-label">当前模型</div>
            <div class="status-value">{{ modelStatus.model || '-' }}</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <div class="status-label">平均响应时间</div>
            <div class="status-value">{{ modelStatus.avgResponseTime || '-' }}ms</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="status-item">
            <div class="status-label">今日对话数</div>
            <div class="status-value">{{ modelStatus.todayChats || 0 }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Setting, 
  Check, 
  Plus, 
  Delete, 
  Monitor 
} from '@element-plus/icons-vue'
import { getModelStatus, switchModel } from '@/api/chat'

// 响应式数据
const activeTab = ref('model')
const saving = ref(false)
const testing = ref(false)
const testMessage = ref('你好，请介绍一下质量管理体系')
const testResult = ref(null)
const configFormRef = ref()

// 配置数据
const config = reactive({
  provider: 'transsion', // 默认使用Transsion AI
  apiKey: 'sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31',
  model: 'gemini-2.5-pro-thinking',
  baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
  maxTokens: 2048,
  temperature: 0.7,
  timeout: 30000,
  systemPrompt: `你是一个专业的质量管理系统(QMS)智能助手。你具备以下专业知识：

1. 质量管理体系标准（ISO 9001、ISO 14001、ISO 45001等）
2. 质量控制方法（SPC、6 Sigma、精益生产等）
3. 质量检测技术和标准
4. 缺陷分析和根因分析方法
5. 质量改进工具和技术
6. 质量数据分析和统计方法
7. 供应商质量管理
8. 产品生命周期质量管理

请用专业、准确、实用的方式回答用户的质量管理相关问题。`,
  quickQuestions: [
    '如何提升产品质量？',
    '质量管理体系如何建立？',
    '缺陷分析方法有哪些？',
    '质量检测标准是什么？',
    '如何进行质量改进？'
  ],
  stream: false,
  historyLimit: 10,
  autoSave: true,
  contentFilter: true
})

// 模型状态
const modelStatus = reactive({
  online: false,
  model: '',
  avgResponseTime: 0,
  todayChats: 0
})

// 服务商配置预设
const providerConfigs = {
  deepseek: {
    apiKey: 'sk-cab797574abf4288bcfaca253191565d',
    baseURL: 'https://api.deepseek.com/v1',
    model: 'deepseek-chat',
    name: 'DeepSeek'
  },
  transsion: {
    apiKey: 'sk_a264854a38dc034077c23f5951285907e6c40d1b4843f46f95e5f31',
    baseURL: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
    model: 'gemini-2.5-pro-thinking',
    name: 'Transsion AI'
  }
}

// 表单验证规则
const rules = {
  provider: [
    { required: true, message: '请选择AI服务商', trigger: 'change' }
  ],
  apiKey: [
    { required: true, message: '请输入API密钥', trigger: 'blur' }
  ],
  model: [
    { required: true, message: '请选择模型', trigger: 'change' }
  ],
  baseURL: [
    { required: true, message: '请输入API地址', trigger: 'blur' }
  ]
}

// 组件挂载
onMounted(() => {
  loadConfig()
  checkModelStatus()
})

// 加载配置
const loadConfig = () => {
  const savedConfig = localStorage.getItem('qms_chat_config')
  if (savedConfig) {
    Object.assign(config, JSON.parse(savedConfig))
  }
}

// 保存配置
const saveConfig = async () => {
  try {
    await configFormRef.value?.validate()
    saving.value = true
    
    // 保存到本地存储
    localStorage.setItem('qms_chat_config', JSON.stringify(config))
    
    // 应用配置到后端
    await switchModel({
      model: config.model,
      config: config
    })
    
    ElMessage.success('配置保存成功')
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

// 测试配置
const testConfig = async () => {
  if (!testMessage.value.trim()) {
    ElMessage.warning('请输入测试消息')
    return
  }
  
  testing.value = true
  testResult.value = null
  
  try {
    const startTime = Date.now()
    
    // 这里应该调用实际的测试API
    const response = await fetch('/api/chat/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        message: testMessage.value,
        model_config: config
      })
    })
    
    const result = await response.json()
    const responseTime = Date.now() - startTime
    
    if (result.success) {
      testResult.value = {
        success: true,
        message: '连接成功，模型响应正常',
        response: result.data.response,
        responseTime: responseTime,
        tokensUsed: result.data.model_info?.tokens_used?.total_tokens || 0
      }
    } else {
      throw new Error(result.message)
    }
    
  } catch (error) {
    testResult.value = {
      success: false,
      message: error.message || '连接失败'
    }
  } finally {
    testing.value = false
  }
}

// 检查模型状态
const checkModelStatus = async () => {
  try {
    const response = await getModelStatus()
    if (response.success) {
      Object.assign(modelStatus, {
        online: response.data.status === 'online',
        model: response.data.model,
        avgResponseTime: response.data.avg_response_time,
        todayChats: response.data.today_chats
      })
    }
  } catch (error) {
    console.error('获取模型状态失败:', error)
  }
}

// 添加问题
const addQuestion = () => {
  config.quickQuestions.push('')
}

// 删除问题
const removeQuestion = (index) => {
  config.quickQuestions.splice(index, 1)
}

// 服务商切换处理
const onProviderChange = (provider) => {
  const providerConfig = providerConfigs[provider]
  if (providerConfig) {
    config.apiKey = providerConfig.apiKey
    config.baseURL = providerConfig.baseURL
    config.model = providerConfig.model

    // 更新系统提示词
    if (provider === 'transsion') {
      config.systemPrompt = `你是一个专业的质量管理系统(QMS)智能助手，基于Transsion AI平台提供服务。你具备以下专业知识：

1. 质量管理体系标准（ISO 9001、ISO 14001、ISO 45001等）
2. 质量控制方法（SPC、6 Sigma、精益生产等）
3. 质量检测技术和标准
4. 缺陷分析和根因分析方法
5. 质量改进工具和技术
6. 质量数据分析和统计方法
7. 供应商质量管理
8. 产品生命周期质量管理

请用专业、准确、实用的方式回答用户的质量管理相关问题。特别关注移动设备和消费电子产品的质量管理需求。`
    } else {
      config.systemPrompt = `你是一个专业的质量管理系统(QMS)智能助手。你具备以下专业知识：

1. 质量管理体系标准（ISO 9001、ISO 14001、ISO 45001等）
2. 质量控制方法（SPC、6 Sigma、精益生产等）
3. 质量检测技术和标准
4. 缺陷分析和根因分析方法
5. 质量改进工具和技术
6. 质量数据分析和统计方法
7. 供应商质量管理
8. 产品生命周期质量管理

请用专业、准确、实用的方式回答用户的质量管理相关问题。`
    }
  }
}
</script>

<style scoped>
.chat-config-container {
  padding: 20px;
}

.config-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
}

.config-tabs {
  margin-top: 20px;
}

.param-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.prompt-tips {
  margin-top: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}

.prompt-tips h4 {
  margin: 0 0 8px 0;
  color: #606266;
  font-size: 14px;
}

.prompt-tips ul {
  margin: 0;
  padding-left: 20px;
  color: #909399;
  font-size: 12px;
}

.questions-section {
  padding: 20px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h4 {
  margin: 0;
  color: #303133;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.test-section {
  padding: 20px 0;
}

.test-result {
  margin-top: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.test-result h4 {
  margin: 0 0 12px 0;
  color: #303133;
}

.test-response {
  margin-top: 16px;
}

.test-response h5 {
  margin: 0 0 8px 0;
  color: #606266;
}

.response-content {
  padding: 12px;
  background: white;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  line-height: 1.6;
  margin-bottom: 8px;
}

.response-meta {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #909399;
}

.status-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.status-item {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.status-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.status-value {
  font-size: 18px;
  font-weight: 500;
  color: #f56c6c;
}

.status-value.online {
  color: #67c23a;
}
</style>
