<template>
  <div class="coze-agent-builder">
    <!-- 工具栏 -->
    <div class="builder-toolbar">
      <div class="toolbar-left">
        <h3>{{ project?.name || '新建Agent' }}</h3>
        <el-tag v-if="project?.status" :type="getStatusType(project.status)">
          {{ getStatusText(project.status) }}
        </el-tag>
      </div>
      <div class="toolbar-right">
        <el-button :icon="VideoPlay" :loading="testing" @click="testAgent">测试</el-button>
        <el-button :icon="Upload" @click="publishAgent">发布</el-button>
        <el-button :icon="Download" :loading="saving" type="primary" @click="saveAgent">保存</el-button>
      </div>
    </div>
    
    <!-- 主要构建区域 -->
    <div class="builder-main">
      <!-- 左侧配置面板 -->
      <div class="config-panel">
        <el-scrollbar>
          <div class="config-section">
            <!-- 基本信息 -->
            <div class="section-card">
              <h4>基本信息</h4>
              <el-form :model="agentConfig" label-width="80px" size="small">
                <el-form-item label="名称">
                  <el-input v-model="agentConfig.name" placeholder="输入Agent名称" />
                </el-form-item>
                <el-form-item label="描述">
                  <el-input
                    v-model="agentConfig.description"
                    type="textarea"
                    :rows="3"
                    placeholder="描述Agent的功能和用途"
                  />
                </el-form-item>
                <el-form-item label="头像">
                  <el-upload
                    class="avatar-uploader"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    action="/api/upload"
                  >
                    <img v-if="agentConfig.avatar" :src="agentConfig.avatar" class="avatar" />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 模型配置 -->
            <div class="section-card">
              <h4>模型配置</h4>
              <el-form :model="agentConfig" label-width="80px" size="small">
                <el-form-item label="主模型">
                  <el-select v-model="agentConfig.model" placeholder="选择AI模型" filterable>
                    <el-option
                      v-for="model in availableModels"
                      :key="model.id"
                      :label="model.name"
                      :value="model.id"
                    >
                      <div class="model-option">
                        <div class="model-info">
                          <span class="model-name">{{ model.name }}</span>
                          <span class="model-desc">{{ model.description }}</span>
                        </div>
                        <div class="model-tags">
                          <el-tag
                            size="small"
                            :type="model.type === 'premium' ? 'warning' : 'info'"
                            class="model-type-tag"
                          >
                            {{ model.type }}
                          </el-tag>
                          <el-tag
                            v-if="model.external"
                            size="small"
                            type="success"
                            class="external-tag"
                          >
                            外部
                          </el-tag>
                          <el-tag
                            v-for="feature in model.features.slice(0, 2)"
                            :key="feature"
                            size="small"
                            type="primary"
                            class="feature-tag"
                          >
                            {{ feature }}
                          </el-tag>
                        </div>
                      </div>
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="温度">
                  <el-slider
                    v-model="agentConfig.temperature"
                    :min="0"
                    :max="2"
                    :step="0.1"
                    show-input
                    :input-size="'small'"
                  />
                </el-form-item>
                <el-form-item label="最大令牌">
                  <el-input-number
                    v-model="agentConfig.maxTokens"
                    :min="1"
                    :max="8192"
                    size="small"
                  />
                </el-form-item>
              </el-form>
            </div>
            
            <!-- 提示词配置 -->
            <div class="section-card">
              <h4>提示词</h4>
              <div class="prompt-editor">
                <el-tabs v-model="activePromptTab" type="border-card">
                  <el-tab-pane label="系统提示词" name="system">
                    <el-input
                      v-model="agentConfig.systemPrompt"
                      type="textarea"
                      :rows="8"
                      placeholder="定义Agent的角色、能力和行为规范..."
                    />
                  </el-tab-pane>
                  <el-tab-pane label="用户提示词" name="user">
                    <el-input
                      v-model="agentConfig.userPrompt"
                      type="textarea"
                      :rows="8"
                      placeholder="用户交互的提示词模板..."
                    />
                  </el-tab-pane>
                  <el-tab-pane label="示例对话" name="examples">
                    <div class="examples-list">
                      <div
                        v-for="(example, index) in agentConfig.examples"
                        :key="index"
                        class="example-item"
                      >
                        <el-input
                          v-model="example.user"
                          placeholder="用户输入"
                          size="small"
                        />
                        <el-input
                          v-model="example.assistant"
                          placeholder="Assistant回复"
                          size="small"
                        />
                        <el-button
                          :icon="Delete"
                          size="small"
                          type="danger"
                          @click="removeExample(index)"
                        />
                      </div>
                      <el-button :icon="Plus" size="small" @click="addExample">
                        添加示例
                      </el-button>
                    </div>
                  </el-tab-pane>
                </el-tabs>
              </div>
            </div>
            
            <!-- 功能配置 -->
            <div class="section-card">
              <h4>功能配置</h4>
              <div class="feature-list">
                <div class="feature-item">
                  <el-switch v-model="agentConfig.features.memory" />
                  <div class="feature-info">
                    <span class="feature-name">记忆功能</span>
                    <span class="feature-desc">记住对话历史</span>
                  </div>
                </div>
                <div class="feature-item">
                  <el-switch v-model="agentConfig.features.webSearch" />
                  <div class="feature-info">
                    <span class="feature-name">网络搜索</span>
                    <span class="feature-desc">实时搜索信息</span>
                  </div>
                </div>
                <div class="feature-item">
                  <el-switch v-model="agentConfig.features.imageGeneration" />
                  <div class="feature-info">
                    <span class="feature-name">图像生成</span>
                    <span class="feature-desc">生成图片内容</span>
                  </div>
                </div>
                <div class="feature-item">
                  <el-switch v-model="agentConfig.features.codeExecution" />
                  <div class="feature-info">
                    <span class="feature-name">代码执行</span>
                    <span class="feature-desc">执行Python代码</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-scrollbar>
      </div>
      
      <!-- 右侧预览面板 -->
      <div class="preview-panel">
        <div class="preview-header">
          <h4>Agent预览</h4>
          <el-button :icon="Refresh" size="small" @click="refreshPreview">
            刷新
          </el-button>
        </div>
        
        <div class="preview-content">
          <!-- Agent信息卡片 -->
          <div class="agent-card">
            <div class="agent-avatar">
              <img v-if="agentConfig.avatar" :src="agentConfig.avatar" />
              <el-icon v-else><User /></el-icon>
            </div>
            <div class="agent-info">
              <h5>{{ agentConfig.name || '未命名Agent' }}</h5>
              <p>{{ agentConfig.description || '暂无描述' }}</p>
              <div class="agent-tags">
                <el-tag size="small">{{ getModelName(agentConfig.model) }}</el-tag>
                <el-tag v-if="agentConfig.features.memory" size="small" type="success">
                  记忆
                </el-tag>
                <el-tag v-if="agentConfig.features.webSearch" size="small" type="info">
                  搜索
                </el-tag>
              </div>
            </div>
          </div>
          
          <!-- 测试对话区域 -->
          <div class="test-chat">
            <div class="chat-messages">
              <div
                v-for="message in testMessages"
                :key="message.id"
                :class="['message', message.role]"
              >
                <div class="message-content">{{ message.content }}</div>
                <div class="message-time">{{ formatTime(message.timestamp) }}</div>
              </div>
            </div>
            
            <div class="chat-input">
              <el-input
                v-model="testInput"
                placeholder="输入测试消息..."
                @keyup.enter="sendTestMessage"
              >
                <template #append>
                  <el-button :icon="Promotion" @click="sendTestMessage" />
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import {
  VideoPlay,
  Upload,
  Download,
  Plus,
  Delete,
  Refresh,
  User,
  Promotion
} from '@element-plus/icons-vue'
import { ElMessage, ElLoading } from 'element-plus'
import cozeStudioAPI from '@/api/coze-studio'

// Props
const props = defineProps({
  project: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['save', 'test'])

// 响应式数据
const activePromptTab = ref('system')
const testInput = ref('')
const testMessages = ref([
  {
    id: 1,
    role: 'assistant',
    content: '你好！我是你的AI助手，有什么可以帮助你的吗？',
    timestamp: new Date()
  }
])
const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const currentAgentId = ref(null)
const currentConversationId = ref(null)

// Agent配置
const agentConfig = reactive({
  name: '',
  description: '',
  avatar: '',
  model: 'gpt-4o',
  temperature: 0.7,
  maxTokens: 2048,
  systemPrompt: '你是一个专业的AI助手，能够帮助用户解决各种问题。请保持友好、专业的态度，提供准确有用的信息。',
  userPrompt: '',
  examples: [
    {
      user: '你好',
      assistant: '你好！我是你的AI助手，有什么可以帮助你的吗？'
    }
  ],
  features: {
    memory: true,
    webSearch: false,
    imageGeneration: false,
    codeExecution: false
  }
})

// 可用模型列表
const availableModels = ref([
  {
    id: 'gpt-4o',
    name: 'GPT-4o',
    type: 'premium',
    description: 'OpenAI最新的多模态模型，支持图文理解',
    features: ['多模态', '推荐'],
    external: false
  },
  {
    id: 'claude-3-5-sonnet',
    name: 'Claude-3.5-Sonnet',
    type: 'premium',
    description: 'Anthropic的Claude-3.5模型，长上下文处理能力强',
    features: ['长上下文', '推荐'],
    external: false
  },
  {
    id: 'deepseek-chat',
    name: 'DeepSeek-Chat',
    type: 'standard',
    description: 'DeepSeek聊天模型，具备强大的推理能力',
    features: ['编程', '推理'],
    external: true
  },
  {
    id: 'deepseek-coder',
    name: 'DeepSeek-Coder',
    type: 'standard',
    description: 'DeepSeek编程专用模型，代码生成能力强',
    features: ['编程专用', '代码生成'],
    external: true
  },
  {
    id: 'gemini-1-5-pro',
    name: 'Gemini-1.5-Pro',
    type: 'premium',
    description: 'Google的Gemini模型，超长上下文和多模态能力',
    features: ['超长上下文', '多模态'],
    external: false
  },
  {
    id: 'gpt-4-turbo',
    name: 'GPT-4-Turbo',
    type: 'premium',
    description: 'OpenAI的GPT-4 Turbo模型，高性能通用模型',
    features: ['高性能', '多模态'],
    external: false
  },
  {
    id: 'claude-3-haiku',
    name: 'Claude-3-Haiku',
    type: 'standard',
    description: 'Anthropic的Claude-3 Haiku模型，轻量快速',
    features: ['快速响应', '轻量级'],
    external: false
  },
  {
    id: 'gpt-3-5-turbo',
    name: 'GPT-3.5-Turbo',
    type: 'standard',
    description: 'OpenAI的GPT-3.5 Turbo模型，经济实用',
    features: ['经济实用', '通用'],
    external: false
  }
])

// 计算属性
const getStatusType = (status) => {
  const typeMap = {
    draft: 'info',
    published: 'success',
    testing: 'warning'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    draft: '草稿',
    published: '已发布',
    testing: '测试中'
  }
  return textMap[status] || '未知'
}

const getModelName = (modelId) => {
  const model = availableModels.value.find(m => m.id === modelId)
  return model ? model.name : '未选择'
}

// 方法
const handleAvatarSuccess = (response) => {
  agentConfig.avatar = response.url
  ElMessage.success('头像上传成功')
}

const addExample = () => {
  agentConfig.examples.push({
    user: '',
    assistant: ''
  })
}

const removeExample = (index) => {
  agentConfig.examples.splice(index, 1)
}

const testAgent = async () => {
  if (!currentAgentId.value) {
    ElMessage.warning('请先保存Agent')
    return
  }

  try {
    testing.value = true

    // 创建测试对话会话
    if (!currentConversationId.value) {
      const response = await cozeStudioAPI.createAgentConversation(currentAgentId.value, {
        title: '测试对话'
      })

      if (response.success) {
        currentConversationId.value = response.data.id
        ElMessage.success('测试会话创建成功')
      }
    }

    emit('test', agentConfig)
  } catch (error) {
    console.error('测试Agent失败:', error)
    ElMessage.error('测试Agent失败')
  } finally {
    testing.value = false
  }
}

const publishAgent = async () => {
  if (!currentAgentId.value) {
    ElMessage.warning('请先保存Agent')
    return
  }

  try {
    await cozeStudioAPI.updateAgent(currentAgentId.value, {
      status: 'published'
    })
    ElMessage.success('Agent发布成功')
  } catch (error) {
    console.error('发布Agent失败:', error)
    ElMessage.error('发布Agent失败')
  }
}

const saveAgent = async () => {
  if (!agentConfig.name || !agentConfig.description) {
    ElMessage.warning('请填写Agent名称和描述')
    return
  }

  try {
    saving.value = true

    if (currentAgentId.value) {
      // 更新现有Agent
      await cozeStudioAPI.updateAgent(currentAgentId.value, {
        name: agentConfig.name,
        description: agentConfig.description,
        avatar: agentConfig.avatar,
        config: {
          model: agentConfig.model,
          temperature: agentConfig.temperature,
          maxTokens: agentConfig.maxTokens,
          systemPrompt: agentConfig.systemPrompt,
          userPrompt: agentConfig.userPrompt,
          examples: agentConfig.examples,
          features: agentConfig.features
        }
      })
      ElMessage.success('Agent更新成功')
    } else {
      // 创建新Agent
      const response = await cozeStudioAPI.createAgent({
        name: agentConfig.name,
        description: agentConfig.description,
        avatar: agentConfig.avatar,
        category: 'general',
        config: {
          model: agentConfig.model,
          temperature: agentConfig.temperature,
          maxTokens: agentConfig.maxTokens,
          systemPrompt: agentConfig.systemPrompt,
          userPrompt: agentConfig.userPrompt,
          examples: agentConfig.examples,
          features: agentConfig.features
        }
      })

      if (response.success) {
        currentAgentId.value = response.data.id
        ElMessage.success('Agent创建成功')
      }
    }

    emit('save', agentConfig)
  } catch (error) {
    console.error('保存Agent失败:', error)
    ElMessage.error('保存Agent失败')
  } finally {
    saving.value = false
  }
}

const refreshPreview = () => {
  ElMessage.success('预览已刷新')
}

const sendTestMessage = async () => {
  if (!testInput.value.trim()) return

  // 检查是否有Agent ID
  if (!currentAgentId.value) {
    ElMessage.warning('请先保存Agent')
    return
  }

  const userMessage = testInput.value.trim()
  testInput.value = ''

  // 添加用户消息
  testMessages.value.push({
    id: Date.now(),
    role: 'user',
    content: userMessage,
    timestamp: new Date()
  })

  try {
    // 如果没有对话会话，先创建一个
    if (!currentConversationId.value) {
      const conversationResponse = await cozeStudioAPI.createAgentConversation(currentAgentId.value, {
        title: '测试对话'
      })

      if (conversationResponse.success) {
        currentConversationId.value = conversationResponse.data.id
        console.log('创建对话会话成功:', currentConversationId.value)
      } else {
        throw new Error('创建对话会话失败')
      }
    }

    // 调用Agent对话API（开启轻量RAG，并使用用户级知识）
    const response = await cozeStudioAPI.chatWithAgent(currentAgentId.value, {
      message: userMessage,
      conversation_id: currentConversationId.value,
      use_rag: true,
      kb_scope: 'user',
      top_k: 3
    })

    if (response.success) {
      // 添加AI回复
      testMessages.value.push({
        id: response.data.message_id || Date.now() + 1,
        role: 'assistant',
        content: response.data.content || response.data.message || '收到您的消息',
        timestamp: new Date(),
        model: response.data.model,
        usage: response.data.usage
      })
    } else {
      throw new Error(response.message || 'Agent对话失败')
    }
  } catch (error) {
    console.error('Agent对话失败:', error)
    // 添加错误消息
    testMessages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: `抱歉，对话出现错误：${error.message}。请检查Agent配置或稍后再试。`,
      timestamp: new Date(),
      error: true
    })
    ElMessage.error('对话失败: ' + error.message)
  }
}

const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString()
}

// 监听项目变化
watch(() => props.project, async (newProject) => {
  if (newProject) {
    // 如果项目有关联的Agent ID，加载Agent数据
    if (newProject.agentId) {
      try {
        const response = await cozeStudioAPI.getAgent(newProject.agentId)
        if (response.success) {
          const agent = response.data
          currentAgentId.value = agent.id

          // 加载Agent配置
          Object.assign(agentConfig, {
            name: agent.name || '',
            description: agent.description || '',
            avatar: agent.avatar || '',
            model: agent.config?.model || 'gpt-4o',
            temperature: agent.config?.temperature || 0.7,
            maxTokens: agent.config?.maxTokens || 2048,
            systemPrompt: agent.config?.systemPrompt || '你是一个专业的AI助手，能够帮助用户解决各种问题。请保持友好、专业的态度，提供准确有用的信息。',
            userPrompt: agent.config?.userPrompt || '',
            examples: agent.config?.examples || [
              {
                user: '你好',
                assistant: '你好！我是你的AI助手，有什么可以帮助你的吗？'
              }
            ],
            features: agent.config?.features || {
              memory: true,
              webSearch: false,
              imageGeneration: false,
              codeExecution: false
            }
          })
        }
      } catch (error) {
        console.error('加载Agent失败:', error)
      }
    } else {
      // 从项目中加载基本配置
      agentConfig.name = newProject.name || ''
      agentConfig.description = newProject.description || ''
    }
  }
}, { immediate: true })
</script>

<style lang="scss" scoped>
.coze-agent-builder {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  .builder-toolbar {
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
  
  .builder-main {
    flex: 1;
    display: flex;
    overflow: hidden;
    
    .config-panel {
      width: 400px;
      border-right: 1px solid #e4e7ed;
      background: #f9fafb;
      
      .config-section {
        padding: 20px;
        
        .section-card {
          background: white;
          border-radius: 8px;
          padding: 16px;
          margin-bottom: 16px;
          box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
          
          h4 {
            margin: 0 0 16px 0;
            font-size: 14px;
            font-weight: 600;
            color: #374151;
          }
          
          .model-option {
            display: flex;
            justify-content: space-between;
            align-items: center;
          }
          
          .prompt-editor {
            :deep(.el-tabs__content) {
              padding: 16px 0 0 0;
            }
          }
          
          .examples-list {
            .example-item {
              display: flex;
              gap: 8px;
              margin-bottom: 8px;
              align-items: center;
            }
          }
          
          .feature-list {
            .feature-item {
              display: flex;
              align-items: center;
              gap: 12px;
              margin-bottom: 16px;
              
              .feature-info {
                flex: 1;
                
                .feature-name {
                  display: block;
                  font-size: 14px;
                  color: #374151;
                  margin-bottom: 2px;
                }
                
                .feature-desc {
                  font-size: 12px;
                  color: #6b7280;
                }
              }
            }
          }
        }
      }
    }
    
    .preview-panel {
      flex: 1;
      display: flex;
      flex-direction: column;
      background: white;
      
      .preview-header {
        height: 60px;
        padding: 0 20px;
        border-bottom: 1px solid #e4e7ed;
        display: flex;
        align-items: center;
        justify-content: space-between;
        
        h4 {
          margin: 0;
          font-size: 14px;
          color: #374151;
        }
      }
      
      .preview-content {
        flex: 1;
        padding: 20px;
        overflow-y: auto;
        
        .agent-card {
          display: flex;
          gap: 16px;
          padding: 20px;
          border: 1px solid #e4e7ed;
          border-radius: 8px;
          margin-bottom: 20px;
          
          .agent-avatar {
            width: 60px;
            height: 60px;
            border-radius: 8px;
            background: #f3f4f6;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: hidden;
            
            img {
              width: 100%;
              height: 100%;
              object-fit: cover;
            }
            
            .el-icon {
              font-size: 24px;
              color: #6b7280;
            }
          }
          
          .agent-info {
            flex: 1;
            
            h5 {
              margin: 0 0 8px 0;
              font-size: 16px;
              color: #1f2937;
            }
            
            p {
              margin: 0 0 12px 0;
              font-size: 14px;
              color: #6b7280;
              line-height: 1.4;
            }
            
            .agent-tags {
              display: flex;
              gap: 8px;
              flex-wrap: wrap;
            }
          }
        }
        
        .model-option {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          width: 100%;
          padding: 8px 0;

          .model-info {
            flex: 1;

            .model-name {
              display: block;
              font-weight: 500;
              color: #1f2937;
              margin-bottom: 4px;
            }

            .model-desc {
              display: block;
              font-size: 12px;
              color: #6b7280;
              line-height: 1.3;
            }
          }

          .model-tags {
            display: flex;
            flex-direction: column;
            gap: 4px;
            align-items: flex-end;

            .model-type-tag {
              margin: 0;
            }

            .external-tag {
              margin: 0;
            }

            .feature-tag {
              margin: 0;
              font-size: 10px;
            }
          }
        }

        .test-chat {
          border: 1px solid #e4e7ed;
          border-radius: 8px;
          height: 400px;
          display: flex;
          flex-direction: column;
          
          .chat-messages {
            flex: 1;
            padding: 16px;
            overflow-y: auto;
            
            .message {
              margin-bottom: 16px;
              
              &.user {
                text-align: right;
                
                .message-content {
                  background: #3b82f6;
                  color: white;
                  display: inline-block;
                  padding: 8px 12px;
                  border-radius: 12px;
                  max-width: 70%;
                }
              }
              
              &.assistant {
                text-align: left;
                
                .message-content {
                  background: #f3f4f6;
                  color: #1f2937;
                  display: inline-block;
                  padding: 8px 12px;
                  border-radius: 12px;
                  max-width: 70%;
                }
              }
              
              .message-time {
                font-size: 12px;
                color: #6b7280;
                margin-top: 4px;
              }
            }
          }
          
          .chat-input {
            padding: 16px;
            border-top: 1px solid #e4e7ed;
          }
        }
      }
    }
  }
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;
    
    &:hover {
      border-color: #409eff;
    }
  }
  
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 60px;
    height: 60px;
    text-align: center;
    line-height: 60px;
  }
  
  .avatar {
    width: 60px;
    height: 60px;
    display: block;
  }
}
</style>
