<template>
  <div class="message-rating">
    <!-- 评分区域 -->
    <div class="rating-section" v-if="!isRated || showEditRating">
      <div class="rating-header">
        <span class="rating-label">为这个回答评分：</span>
        <el-button 
          v-if="isRated && !showEditRating" 
          type="text" 
          size="small" 
          @click="showEditRating = true"
        >
          修改评分
        </el-button>
      </div>
      
      <div class="rating-stars">
        <el-rate
          v-model="currentRating"
          :max="5"
          :colors="ratingColors"
          :texts="ratingTexts"
          show-text
          @change="handleRatingChange"
        />
      </div>
      
      <!-- 反馈输入框 -->
      <div class="feedback-section" v-if="currentRating > 0">
        <el-input
          v-model="currentFeedback"
          type="textarea"
          :rows="3"
          placeholder="请提供具体的反馈建议（可选）"
          maxlength="500"
          show-word-limit
          class="feedback-input"
        />
        
        <div class="rating-actions">
          <el-button 
            type="primary" 
            size="small" 
            @click="submitRating"
            :loading="submitting"
          >
            {{ isRated ? '更新评分' : '提交评分' }}
          </el-button>
          <el-button 
            v-if="showEditRating" 
            size="small" 
            @click="cancelEdit"
          >
            取消
          </el-button>
        </div>
      </div>
    </div>
    
    <!-- 已评分显示 -->
    <div class="rated-display" v-else-if="isRated">
      <div class="rated-info">
        <span class="rated-label">您的评分：</span>
        <el-rate
          :model-value="rating"
          :max="5"
          :colors="ratingColors"
          disabled
          show-score
          text-color="#ff9900"
        />
        <el-button 
          type="text" 
          size="small" 
          @click="showEditRating = true"
          class="edit-rating-btn"
        >
          <el-icon><Edit /></el-icon>
          修改
        </el-button>
      </div>
      
      <div class="feedback-display" v-if="feedback">
        <div class="feedback-label">您的反馈：</div>
        <div class="feedback-content">{{ feedback }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  messageId: {
    type: String,
    required: true
  },
  rating: {
    type: Number,
    default: null
  },
  feedback: {
    type: String,
    default: ''
  },
  conversationId: {
    type: String,
    required: true
  }
})

// Emits
const emit = defineEmits(['rated', 'rating-updated'])

// 响应式数据
const currentRating = ref(props.rating || 0)
const currentFeedback = ref(props.feedback || '')
const submitting = ref(false)
const showEditRating = ref(false)

// 计算属性
const isRated = computed(() => props.rating !== null && props.rating > 0)

// 评分配置
const ratingColors = ['#F56C6C', '#E6A23C', '#F7BA2A', '#67C23A', '#67C23A']
const ratingTexts = ['很差', '较差', '一般', '满意', '非常满意']

// 监听props变化
watch(() => props.rating, (newRating) => {
  currentRating.value = newRating || 0
})

watch(() => props.feedback, (newFeedback) => {
  currentFeedback.value = newFeedback || ''
})

// 处理评分变化
const handleRatingChange = (value) => {
  console.log('评分变化:', value)
}

// 提交评分
const submitRating = async () => {
  if (currentRating.value === 0) {
    ElMessage.warning('请先选择评分')
    return
  }

  submitting.value = true
  
  try {
    const response = await fetch(`/api/chat/messages/${props.messageId}/rate`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        rating: currentRating.value,
        feedback: currentFeedback.value.trim() || null
      })
    })

    const result = await response.json()

    if (result.success) {
      ElMessage.success(isRated.value ? '评分更新成功' : '评分提交成功')
      showEditRating.value = false
      
      // 触发事件
      if (isRated.value) {
        emit('rating-updated', {
          messageId: props.messageId,
          rating: currentRating.value,
          feedback: currentFeedback.value
        })
      } else {
        emit('rated', {
          messageId: props.messageId,
          rating: currentRating.value,
          feedback: currentFeedback.value
        })
      }
    } else {
      ElMessage.error(result.message || '评分失败')
    }
  } catch (error) {
    console.error('评分失败:', error)
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 取消编辑
const cancelEdit = () => {
  currentRating.value = props.rating || 0
  currentFeedback.value = props.feedback || ''
  showEditRating.value = false
}
</script>

<style lang="scss" scoped>
.message-rating {
  margin-top: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
  
  .rating-section {
    .rating-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      .rating-label {
        font-size: 14px;
        color: #606266;
        font-weight: 500;
      }
    }
    
    .rating-stars {
      margin-bottom: 12px;
      
      :deep(.el-rate) {
        .el-rate__text {
          font-size: 12px;
          color: #909399;
        }
      }
    }
    
    .feedback-section {
      .feedback-input {
        margin-bottom: 12px;
      }
      
      .rating-actions {
        display: flex;
        gap: 8px;
      }
    }
  }
  
  .rated-display {
    .rated-info {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
      
      .rated-label {
        font-size: 14px;
        color: #606266;
        font-weight: 500;
      }
      
      .edit-rating-btn {
        margin-left: auto;
        color: #409eff;
        
        &:hover {
          color: #337ecc;
        }
      }
    }
    
    .feedback-display {
      .feedback-label {
        font-size: 12px;
        color: #909399;
        margin-bottom: 4px;
      }
      
      .feedback-content {
        font-size: 14px;
        color: #606266;
        line-height: 1.5;
        padding: 8px 12px;
        background: white;
        border-radius: 4px;
        border: 1px solid #e4e7ed;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .message-rating {
    padding: 8px;
    
    .rated-info {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;
      
      .edit-rating-btn {
        margin-left: 0;
        align-self: flex-end;
      }
    }
  }
}
</style>
