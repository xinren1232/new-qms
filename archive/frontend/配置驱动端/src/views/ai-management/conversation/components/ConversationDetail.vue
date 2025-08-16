<template>
  <el-dialog
    v-model="visible"
    title="对话详情"
    width="800px"
    :before-close="handleClose"
  >
    <div v-if="conversation" class="conversation-detail">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="对话ID">
          {{ conversation.conversationId }}
        </el-descriptions-item>
        <el-descriptions-item label="用户ID">
          {{ conversation.userId }}
        </el-descriptions-item>
        <el-descriptions-item label="对话状态">
          <el-tag :type="getStatusType(conversation.conversationStatus)">
            {{ conversation.conversationStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="满意度评分">
          <el-rate
            :model-value="conversation.satisfactionScore"
            disabled
            show-score
            text-color="#ff9900"
          />
        </el-descriptions-item>
        <el-descriptions-item label="对话时间" :span="2">
          {{ conversation.conversationTime }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="conversation-content">
        <h4>用户问题</h4>
        <div class="question-content">
          {{ conversation.userQuestion }}
        </div>
        
        <h4>AI回答</h4>
        <div class="answer-content">
          {{ conversation.aiResponse }}
        </div>
      </div>
    </div>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="handleAnalyze">分析对话</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  conversation: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const getStatusType = (status) => {
  const typeMap = {
    '进行中': 'warning',
    '已完成': 'success',
    '已中断': 'info'
  }
  return typeMap[status] || 'info'
}

const handleClose = () => {
  visible.value = false
}

const handleAnalyze = () => {
  console.log('分析对话:', props.conversation)
  ElMessage.success('开始分析对话...')
}
</script>

<style lang="scss" scoped>
.conversation-detail {
  .conversation-content {
    margin-top: 24px;
    
    h4 {
      margin: 16px 0 8px 0;
      color: #303133;
    }
    
    .question-content,
    .answer-content {
      padding: 12px;
      background-color: #f5f7fa;
      border-radius: 4px;
      line-height: 1.6;
      color: #606266;
      margin-bottom: 16px;
    }
  }
}
</style>
