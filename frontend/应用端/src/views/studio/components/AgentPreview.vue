<template>
  <div class="agent-preview">
    <div class="preview-header">
      <div class="agent-avatar">
        <img v-if="config.avatar" :src="config.avatar" alt="Agent头像" />
        <el-icon v-else><User /></el-icon>
      </div>
      <div class="agent-info">
        <h3>{{ config.name || '未命名Agent' }}</h3>
        <p>{{ config.description || '暂无描述' }}</p>
      </div>
    </div>
    
    <div class="preview-content">
      <div class="config-summary">
        <h4>配置概览</h4>
        <div class="summary-item">
          <span>模型:</span>
          <span>{{ getModelName(config.model) }}</span>
        </div>
        <div class="summary-item">
          <span>温度:</span>
          <span>{{ config.temperature }}</span>
        </div>
        <div class="summary-item">
          <span>最大令牌:</span>
          <span>{{ config.maxTokens }}</span>
        </div>
      </div>
      
      <!-- 技能配置预览 -->
      <div v-if="hasSkills" class="skills-summary">
        <h4>技能配置</h4>
        <div class="skill-section" v-if="config.plugins?.length">
          <span class="skill-label">插件:</span>
          <div class="skill-tags">
            <el-tag v-for="plugin in config.plugins" :key="plugin.id" size="small">
              {{ plugin.name }}
            </el-tag>
          </div>
        </div>
        <div class="skill-section" v-if="config.workflows?.length">
          <span class="skill-label">工作流:</span>
          <div class="skill-tags">
            <el-tag v-for="workflow in config.workflows" :key="workflow.id" size="small" type="success">
              {{ workflow.name }}
            </el-tag>
          </div>
        </div>
        <div class="skill-section" v-if="config.knowledgeBases?.length">
          <span class="skill-label">知识库:</span>
          <div class="skill-tags">
            <el-tag v-for="kb in config.knowledgeBases" :key="kb.id" size="small" type="warning">
              {{ kb.name }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <!-- 知识配置预览 -->
      <div v-if="hasKnowledge" class="knowledge-summary">
        <h4>知识配置</h4>
        <div class="knowledge-item" v-if="config.knowledge?.variables?.length">
          <span>变量数量:</span>
          <span>{{ config.knowledge.variables.length }}个</span>
        </div>
        <div class="knowledge-item">
          <span>数据库:</span>
          <span>{{ config.knowledge?.enableDatabase ? '已启用' : '未启用' }}</span>
        </div>
        <div class="knowledge-item">
          <span>长期记忆:</span>
          <span>{{ config.knowledge?.longTermMemory ? '已启用' : '未启用' }}</span>
        </div>
      </div>
      
      <!-- 对话体验预览 -->
      <div v-if="hasConversation" class="conversation-summary">
        <h4>对话体验</h4>
        <div class="conversation-item" v-if="config.conversation?.openingStatement">
          <span class="item-label">开场白:</span>
          <div class="item-content">{{ config.conversation.openingStatement }}</div>
        </div>
        <div class="conversation-item" v-if="config.conversation?.suggestions?.length">
          <span class="item-label">建议问题:</span>
          <div class="suggestion-preview">
            <el-tag
              v-for="(suggestion, index) in config.conversation.suggestions.slice(0, 3)"
              :key="index"
              size="small"
              type="info"
            >
              {{ suggestion }}
            </el-tag>
            <span v-if="config.conversation.suggestions.length > 3" class="more-count">
              +{{ config.conversation.suggestions.length - 3 }}
            </span>
          </div>
        </div>
        <div class="conversation-item">
          <span>自动回复:</span>
          <span>{{ config.conversation?.autoReply ? '已启用' : '未启用' }}</span>
        </div>
      </div>
      
      <div class="prompt-preview">
        <h4>系统提示词</h4>
        <div class="prompt-content">
          {{ config.systemPrompt || '暂无系统提示词' }}
        </div>
      </div>
      
      <!-- 模拟对话预览 -->
      <div class="chat-preview">
        <h4>对话预览</h4>
        <div class="mock-chat">
          <div class="chat-message assistant">
            <div class="message-avatar">
              <img v-if="config.avatar" :src="config.avatar" alt="Agent" />
              <el-icon v-else><User /></el-icon>
            </div>
            <div class="message-content">
              {{ config.conversation?.openingStatement || '你好！我是你的AI助手，有什么可以帮助你的吗？' }}
            </div>
          </div>
          <div class="chat-suggestions" v-if="config.conversation?.suggestions?.length">
            <div
              v-for="(suggestion, index) in config.conversation.suggestions.slice(0, 2)"
              :key="index"
              class="suggestion-bubble"
            >
              {{ suggestion }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { User } from '@element-plus/icons-vue'

const props = defineProps({
  config: {
    type: Object,
    default: () => ({})
  }
})

// 计算属性
const hasSkills = computed(() => {
  return props.config.plugins?.length || 
         props.config.workflows?.length || 
         props.config.knowledgeBases?.length
})

const hasKnowledge = computed(() => {
  return props.config.knowledge?.variables?.length ||
         props.config.knowledge?.enableDatabase ||
         props.config.knowledge?.longTermMemory
})

const hasConversation = computed(() => {
  return props.config.conversation?.openingStatement ||
         props.config.conversation?.suggestions?.length ||
         props.config.conversation?.autoReply
})

// 方法
const getModelName = (modelId) => {
  const modelMap = {
    'gpt-4o': 'GPT-4o',
    'gpt-4o-mini': 'GPT-4o Mini',
    'claude-3-5-sonnet': 'Claude 3.5 Sonnet',
    'deepseek-chat': 'DeepSeek Chat',
    'deepseek-coder': 'DeepSeek Coder',
    'qwen-max': 'Qwen Max',
    'glm-4': 'GLM-4',
    'yi-large': 'Yi Large'
  }
  return modelMap[modelId] || modelId || '未选择'
}
</script>

<style scoped>
.agent-preview {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.agent-avatar {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #909399;
}

.agent-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.agent-info h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #303133;
}

.agent-info p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
}

.preview-content {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

.config-summary,
.skills-summary,
.knowledge-summary,
.conversation-summary,
.prompt-preview,
.chat-preview {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.config-summary:last-child,
.skills-summary:last-child,
.knowledge-summary:last-child,
.conversation-summary:last-child,
.prompt-preview:last-child,
.chat-preview:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.summary-item,
.knowledge-item,
.conversation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
}

.summary-item span:first-child,
.knowledge-item span:first-child,
.conversation-item span:first-child {
  color: #606266;
}

.summary-item span:last-child,
.knowledge-item span:last-child,
.conversation-item span:last-child {
  color: #303133;
  font-weight: 500;
}

.skill-section {
  margin-bottom: 12px;
}

.skill-label,
.item-label {
  display: block;
  font-size: 14px;
  color: #606266;
  margin-bottom: 6px;
}

.skill-tags,
.suggestion-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.item-content {
  background: #f8f9fa;
  padding: 8px;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  margin-top: 6px;
}

.more-count {
  font-size: 12px;
  color: #909399;
  margin-left: 4px;
}

.prompt-content {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  max-height: 120px;
  overflow-y: auto;
}

.mock-chat {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
}

.chat-message {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 12px;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  overflow: hidden;
  background: #e6f7ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #409eff;
  flex-shrink: 0;
}

.message-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.message-content {
  background: white;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  max-width: 200px;
}

.chat-suggestions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-left: 40px;
}

.suggestion-bubble {
  background: white;
  border: 1px solid #e4e7ed;
  padding: 6px 10px;
  border-radius: 16px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.3s;
  align-self: flex-start;
}

.suggestion-bubble:hover {
  border-color: #409eff;
  color: #409eff;
}
</style>
