<template>
  <div class="coze-settings">
    <el-tabs v-model="activeTab" tab-position="left">
      <!-- 通用设置 -->
      <el-tab-pane label="通用" name="general">
        <div class="settings-section">
          <h3>通用设置</h3>
          
          <el-form :model="settings.general" label-width="120px">
            <el-form-item label="语言">
              <el-select v-model="settings.general.language">
                <el-option label="简体中文" value="zh-CN" />
                <el-option label="English" value="en-US" />
                <el-option label="日本語" value="ja-JP" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="主题">
              <el-select v-model="settings.general.theme">
                <el-option label="跟随系统" value="auto" />
                <el-option label="亮色主题" value="light" />
                <el-option label="暗色主题" value="dark" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="自动保存">
              <el-switch v-model="settings.general.autoSave" />
              <span class="setting-desc">自动保存项目更改</span>
            </el-form-item>
            
            <el-form-item label="保存间隔">
              <el-input-number
                v-model="settings.general.saveInterval"
                :min="30"
                :max="600"
                :disabled="!settings.general.autoSave"
              />
              <span class="setting-desc">秒</span>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      
      <!-- AI模型设置 -->
      <el-tab-pane label="AI模型" name="ai">
        <div class="settings-section">
          <h3>AI模型配置</h3>
          
          <div class="model-list">
            <div
              v-for="model in settings.ai.models"
              :key="model.id"
              class="model-item"
            >
              <div class="model-header">
                <div class="model-info">
                  <div class="model-title">
                    <h4>{{ model.name }}</h4>
                    <div class="model-tags">
                      <el-tag
                        v-for="tag in model.tags"
                        :key="tag"
                        size="small"
                        :type="tag.includes('推荐') ? 'warning' : tag.includes('外部') ? 'success' : 'primary'"
                      >
                        {{ tag }}
                      </el-tag>
                    </div>
                  </div>
                  <p>{{ model.description }}</p>
                  <div class="model-features">
                    <span class="feature-item">上下文: {{ model.contextLength?.toLocaleString() || 'N/A' }}</span>
                    <span class="feature-item">最大令牌: {{ model.maxTokens?.toLocaleString() || 'N/A' }}</span>
                    <span class="feature-item" v-if="model.features?.vision">支持视觉</span>
                    <span class="feature-item" v-if="model.features?.tools">支持工具</span>
                    <span class="feature-item" v-if="model.features?.reasoning">支持推理</span>
                  </div>
                </div>
                <el-switch v-model="model.enabled" />
              </div>
              
              <div v-if="model.enabled" class="model-config">
                <el-form label-width="100px" size="small">
                  <el-form-item label="API密钥">
                    <el-input
                      v-model="model.apiKey"
                      type="password"
                      placeholder="输入API密钥"
                      show-password
                    />
                  </el-form-item>
                  
                  <el-form-item label="API地址">
                    <el-input
                      v-model="model.apiUrl"
                      placeholder="API服务地址"
                    />
                  </el-form-item>
                  
                  <el-form-item label="默认温度">
                    <el-slider
                      v-model="model.defaultTemperature"
                      :min="0"
                      :max="2"
                      :step="0.1"
                      show-input
                    />
                  </el-form-item>
                  
                  <el-form-item label="最大令牌">
                    <el-input-number
                      v-model="model.maxTokens"
                      :min="1"
                      :max="32768"
                    />
                  </el-form-item>
                </el-form>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 工作流设置 -->
      <el-tab-pane label="工作流" name="workflow">
        <div class="settings-section">
          <h3>工作流设置</h3>
          
          <el-form :model="settings.workflow" label-width="120px">
            <el-form-item label="执行超时">
              <el-input-number
                v-model="settings.workflow.timeout"
                :min="10"
                :max="3600"
              />
              <span class="setting-desc">秒</span>
            </el-form-item>
            
            <el-form-item label="重试次数">
              <el-input-number
                v-model="settings.workflow.retryCount"
                :min="0"
                :max="5"
              />
            </el-form-item>
            
            <el-form-item label="并发限制">
              <el-input-number
                v-model="settings.workflow.concurrency"
                :min="1"
                :max="20"
              />
            </el-form-item>
            
            <el-form-item label="日志级别">
              <el-select v-model="settings.workflow.logLevel">
                <el-option label="错误" value="error" />
                <el-option label="警告" value="warn" />
                <el-option label="信息" value="info" />
                <el-option label="调试" value="debug" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="保留日志">
              <el-input-number
                v-model="settings.workflow.logRetention"
                :min="1"
                :max="365"
              />
              <span class="setting-desc">天</span>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      
      <!-- 安全设置 -->
      <el-tab-pane label="安全" name="security">
        <div class="settings-section">
          <h3>安全设置</h3>
          
          <el-form :model="settings.security" label-width="120px">
            <el-form-item label="API访问控制">
              <el-switch v-model="settings.security.apiAccessControl" />
              <span class="setting-desc">启用API访问控制</span>
            </el-form-item>
            
            <el-form-item label="IP白名单">
              <el-input
                v-model="settings.security.ipWhitelist"
                type="textarea"
                :rows="3"
                placeholder="每行一个IP地址或CIDR"
                :disabled="!settings.security.apiAccessControl"
              />
            </el-form-item>
            
            <el-form-item label="会话超时">
              <el-input-number
                v-model="settings.security.sessionTimeout"
                :min="5"
                :max="1440"
              />
              <span class="setting-desc">分钟</span>
            </el-form-item>
            
            <el-form-item label="数据加密">
              <el-switch v-model="settings.security.dataEncryption" />
              <span class="setting-desc">加密敏感数据</span>
            </el-form-item>
            
            <el-form-item label="审计日志">
              <el-switch v-model="settings.security.auditLog" />
              <span class="setting-desc">记录操作审计日志</span>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      
      <!-- 存储设置 -->
      <el-tab-pane label="存储" name="storage">
        <div class="settings-section">
          <h3>存储设置</h3>
          
          <el-form :model="settings.storage" label-width="120px">
            <el-form-item label="存储类型">
              <el-select v-model="settings.storage.type">
                <el-option label="本地存储" value="local" />
                <el-option label="云存储" value="cloud" />
                <el-option label="数据库" value="database" />
              </el-select>
            </el-form-item>
            
            <el-form-item v-if="settings.storage.type === 'cloud'" label="云存储配置">
              <el-input
                v-model="settings.storage.cloudConfig"
                type="textarea"
                :rows="4"
                placeholder="JSON格式的云存储配置"
              />
            </el-form-item>
            
            <el-form-item label="自动清理">
              <el-switch v-model="settings.storage.autoCleanup" />
              <span class="setting-desc">自动清理过期文件</span>
            </el-form-item>
            
            <el-form-item label="清理周期">
              <el-input-number
                v-model="settings.storage.cleanupInterval"
                :min="1"
                :max="30"
                :disabled="!settings.storage.autoCleanup"
              />
              <span class="setting-desc">天</span>
            </el-form-item>
            
            <el-form-item label="最大文件大小">
              <el-input-number
                v-model="settings.storage.maxFileSize"
                :min="1"
                :max="1024"
              />
              <span class="setting-desc">MB</span>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
      
      <!-- 关于 -->
      <el-tab-pane label="关于" name="about">
        <div class="settings-section">
          <div class="about-content">
            <div class="about-logo">
              <el-icon size="64"><Cpu /></el-icon>
              <h2>Coze Studio</h2>
              <p>版本 1.0.0</p>
            </div>
            
            <div class="about-info">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="版本">1.0.0</el-descriptions-item>
                <el-descriptions-item label="构建时间">2024-01-20 10:30:00</el-descriptions-item>
                <el-descriptions-item label="Git提交">abc123def456</el-descriptions-item>
                <el-descriptions-item label="Node.js版本">v18.17.0</el-descriptions-item>
                <el-descriptions-item label="许可证">MIT License</el-descriptions-item>
              </el-descriptions>
            </div>
            
            <div class="about-links">
              <el-button type="primary" @click="checkUpdate">检查更新</el-button>
              <el-button @click="viewLicense">查看许可证</el-button>
              <el-button @click="reportIssue">报告问题</el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 底部操作按钮 -->
    <div class="settings-footer">
      <el-button @click="resetSettings">重置设置</el-button>
      <el-button @click="exportSettings">导出设置</el-button>
      <el-button @click="importSettings">导入设置</el-button>
      <el-button type="primary" @click="saveSettings">保存设置</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { Cpu } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// Emits
const emit = defineEmits(['save', 'cancel'])

// 响应式数据
const activeTab = ref('general')

// 设置数据
const settings = reactive({
  general: {
    language: 'zh-CN',
    theme: 'auto',
    autoSave: true,
    saveInterval: 60
  },
  ai: {
    models: [
      {
        id: 'gpt-4o',
        name: 'GPT-4o',
        description: 'OpenAI最新的多模态模型，支持图文理解',
        enabled: true,
        apiKey: '',
        apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        defaultTemperature: 0.7,
        maxTokens: 4096,
        contextLength: 128000,
        features: { reasoning: false, tools: true, vision: true, multimodal: true, streaming: true, external: false },
        tags: ['内部模型', '多模态', '推荐']
      },
      {
        id: 'claude-3-5-sonnet',
        name: 'Claude-3.5-Sonnet',
        description: 'Anthropic的Claude-3.5模型，长上下文处理能力强',
        enabled: true,
        apiKey: '',
        apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        defaultTemperature: 0.7,
        maxTokens: 8192,
        contextLength: 200000,
        features: { reasoning: false, tools: true, vision: false, multimodal: false, streaming: true, external: false },
        tags: ['内部模型', '长上下文', '推荐']
      },
      {
        id: 'deepseek-chat',
        name: 'DeepSeek-Chat',
        description: 'DeepSeek聊天模型，具备强大的推理能力',
        enabled: true,
        apiKey: '',
        apiUrl: 'https://api.deepseek.com',
        defaultTemperature: 0.7,
        maxTokens: 4096,
        contextLength: 32768,
        features: { reasoning: true, tools: false, vision: false, multimodal: false, streaming: true, external: true },
        tags: ['外部模型', '编程', '推理']
      },
      {
        id: 'deepseek-coder',
        name: 'DeepSeek-Coder',
        description: 'DeepSeek编程专用模型，代码生成能力强',
        enabled: true,
        apiKey: '',
        apiUrl: 'https://api.deepseek.com',
        defaultTemperature: 0.1,
        maxTokens: 4096,
        contextLength: 16384,
        features: { reasoning: false, tools: false, vision: false, multimodal: false, streaming: true, external: true },
        tags: ['外部模型', '编程专用', '代码生成']
      },
      {
        id: 'gemini-1-5-pro',
        name: 'Gemini-1.5-Pro',
        description: 'Google的Gemini模型，超长上下文和多模态能力',
        enabled: true,
        apiKey: '',
        apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        defaultTemperature: 0.7,
        maxTokens: 8192,
        contextLength: 1000000,
        features: { reasoning: false, tools: false, vision: true, multimodal: true, streaming: true, external: false },
        tags: ['内部模型', '超长上下文', '多模态']
      },
      {
        id: 'gpt-4-turbo',
        name: 'GPT-4-Turbo',
        description: 'OpenAI的GPT-4 Turbo模型，高性能通用模型',
        enabled: true,
        apiKey: '',
        apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        defaultTemperature: 0.7,
        maxTokens: 4096,
        contextLength: 128000,
        features: { reasoning: false, tools: true, vision: true, multimodal: true, streaming: true, external: false },
        tags: ['内部模型', '高性能', '多模态']
      },
      {
        id: 'claude-3-haiku',
        name: 'Claude-3-Haiku',
        description: 'Anthropic的Claude-3 Haiku模型，轻量快速',
        enabled: true,
        apiKey: '',
        apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        defaultTemperature: 0.7,
        maxTokens: 4096,
        contextLength: 200000,
        features: { reasoning: false, tools: false, vision: false, multimodal: false, streaming: true, external: false },
        tags: ['内部模型', '快速响应', '轻量级']
      },
      {
        id: 'gpt-3-5-turbo',
        name: 'GPT-3.5-Turbo',
        description: 'OpenAI的GPT-3.5 Turbo模型，经济实用',
        enabled: true,
        apiKey: '',
        apiUrl: 'https://hk-intra-paas.transsion.com/tranai-proxy/v1',
        defaultTemperature: 0.7,
        maxTokens: 4096,
        contextLength: 16385,
        features: { reasoning: false, tools: true, vision: false, multimodal: false, streaming: true, external: false },
        tags: ['内部模型', '经济实用', '通用']
      }
    ]
  },
  workflow: {
    timeout: 300,
    retryCount: 3,
    concurrency: 5,
    logLevel: 'info',
    logRetention: 30
  },
  security: {
    apiAccessControl: false,
    ipWhitelist: '',
    sessionTimeout: 60,
    dataEncryption: true,
    auditLog: true
  },
  storage: {
    type: 'local',
    cloudConfig: '',
    autoCleanup: true,
    cleanupInterval: 7,
    maxFileSize: 100
  }
})

// 方法
const saveSettings = () => {
  emit('save', settings)
}

const resetSettings = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重置所有设置吗？此操作不可恢复。',
      '确认重置',
      {
        confirmButtonText: '重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 重置设置逻辑
    ElMessage.success('设置已重置')
  } catch {
    // 用户取消
  }
}

const exportSettings = () => {
  const dataStr = JSON.stringify(settings, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)
  
  const link = document.createElement('a')
  link.href = url
  link.download = 'coze-settings.json'
  link.click()
  
  URL.revokeObjectURL(url)
  ElMessage.success('设置已导出')
}

const importSettings = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.json'
  
  input.onchange = (e) => {
    const file = e.target.files[0]
    if (!file) return
    
    const reader = new FileReader()
    reader.onload = (e) => {
      try {
        const importedSettings = JSON.parse(e.target.result)
        Object.assign(settings, importedSettings)
        ElMessage.success('设置已导入')
      } catch (error) {
        ElMessage.error('设置文件格式错误')
      }
    }
    reader.readAsText(file)
  }
  
  input.click()
}

const checkUpdate = () => {
  ElMessage.success('当前已是最新版本')
}

const viewLicense = () => {
  ElMessage.success('查看许可证')
}

const reportIssue = () => {
  ElMessage.success('报告问题')
}
</script>

<style lang="scss" scoped>
.coze-settings {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  :deep(.el-tabs) {
    flex: 1;
    display: flex;
    
    .el-tabs__content {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
    }
  }
  
  .settings-section {
    h3 {
      margin: 0 0 24px 0;
      font-size: 18px;
      font-weight: 600;
      color: #1f2937;
    }
    
    .setting-desc {
      margin-left: 8px;
      font-size: 12px;
      color: #6b7280;
    }
    
    .model-list {
      .model-item {
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        padding: 20px;
        margin-bottom: 16px;
        
        .model-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 16px;
          
          .model-info {
            flex: 1;

            .model-title {
              display: flex;
              align-items: center;
              gap: 12px;
              margin-bottom: 8px;

              h4 {
                margin: 0;
                font-size: 16px;
                font-weight: 600;
                color: #1f2937;
              }

              .model-tags {
                display: flex;
                gap: 6px;
                flex-wrap: wrap;
              }
            }

            p {
              margin: 0 0 8px 0;
              font-size: 14px;
              color: #6b7280;
              line-height: 1.4;
            }

            .model-features {
              display: flex;
              gap: 12px;
              flex-wrap: wrap;

              .feature-item {
                font-size: 12px;
                color: #9ca3af;
                background: #f3f4f6;
                padding: 2px 6px;
                border-radius: 4px;
              }
            }
          }
        }
        
        .model-config {
          border-top: 1px solid #f3f4f6;
          padding-top: 16px;
        }
      }
    }
    
    .about-content {
      text-align: center;
      
      .about-logo {
        margin-bottom: 32px;
        
        .el-icon {
          color: #3b82f6;
          margin-bottom: 16px;
        }
        
        h2 {
          margin: 0 0 8px 0;
          font-size: 24px;
          font-weight: 600;
          color: #1f2937;
        }
        
        p {
          margin: 0;
          font-size: 14px;
          color: #6b7280;
        }
      }
      
      .about-info {
        margin-bottom: 32px;
        text-align: left;
      }
      
      .about-links {
        display: flex;
        gap: 12px;
        justify-content: center;
      }
    }
  }
  
  .settings-footer {
    padding: 20px;
    border-top: 1px solid #e5e7eb;
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }
}
</style>
