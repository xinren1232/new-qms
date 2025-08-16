<template>
  <div class="config-driven-cell">
    <!-- 文本类型 -->
    <span v-if="column.type === 'text'" :title="showTooltip ? value : ''">
      {{ formatText(value) }}
    </span>
    
    <!-- 标签类型 -->
    <el-tag
      v-else-if="column.type === 'tag'"
      :type="getTagType(value)"
      :size="column.size || 'small'"
    >
      {{ value }}
    </el-tag>
    
    <!-- 状态类型 -->
    <div v-else-if="column.type === 'status'" class="status-cell">
      <el-icon :class="getStatusClass(value)">
        <component :is="getStatusIcon(value)" />
      </el-icon>
      <span>{{ value }}</span>
    </div>
    
    <!-- 评分类型 -->
    <el-rate
      v-else-if="column.type === 'rate'"
      :model-value="Number(value)"
      disabled
      :show-score="column.showScore !== false"
      :text-color="column.textColor || '#ff9900'"
      :score-template="column.scoreTemplate || '{value}'"
    />
    
    <!-- 开关类型 -->
    <el-switch
      v-else-if="column.type === 'switch'"
      :model-value="Boolean(value)"
      disabled
      :active-text="column.activeText"
      :inactive-text="column.inactiveText"
    />
    
    <!-- 日期时间类型 -->
    <span v-else-if="column.type === 'datetime'">
      {{ formatDateTime(value) }}
    </span>
    
    <!-- 数字类型 -->
    <span v-else-if="column.type === 'number'">
      {{ formatNumber(value) }}
    </span>
    
    <!-- 链接类型 -->
    <el-link
      v-else-if="column.type === 'link'"
      :href="value"
      :type="column.linkType || 'primary'"
      :target="column.target || '_blank'"
    >
      {{ column.linkText || value }}
    </el-link>
    
    <!-- 图片类型 -->
    <el-image
      v-else-if="column.type === 'image'"
      :src="value"
      :style="{ width: column.imageWidth || '40px', height: column.imageHeight || '40px' }"
      :preview-src-list="[value]"
      fit="cover"
    />
    
    <!-- 进度条类型 -->
    <el-progress
      v-else-if="column.type === 'progress'"
      :percentage="Number(value)"
      :type="column.progressType || 'line'"
      :stroke-width="column.strokeWidth || 6"
      :show-text="column.showText !== false"
    />
    
    <!-- 操作按钮类型 -->
    <div v-else-if="column.type === 'actions'" class="cell-actions">
      <el-button
        v-for="action in column.actions"
        :key="action.key"
        :type="action.type || 'text'"
        :size="action.size || 'small'"
        :icon="action.icon"
        @click="handleAction(action)"
      >
        {{ action.label }}
      </el-button>
    </div>
    
    <!-- 自定义类型 -->
    <component
      v-else-if="column.type === 'custom' && column.component"
      :is="column.component"
      :value="value"
      :row="row"
      :column="column"
      @action="handleAction"
    />
    
    <!-- 默认文本显示 -->
    <span v-else>{{ value }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { formatDate, formatNumber as formatNum } from '@/utils/format'

const props = defineProps({
  value: {
    type: [String, Number, Boolean, Array, Object],
    default: ''
  },
  column: {
    type: Object,
    required: true
  },
  row: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['action'])

// 是否显示tooltip
const showTooltip = computed(() => {
  return props.column.showTooltip !== false && 
         typeof props.value === 'string' && 
         props.value.length > 20
})

// 格式化文本
const formatText = (value) => {
  if (value === null || value === undefined) {
    return '-'
  }
  return String(value)
}

// 格式化日期时间
const formatDateTime = (value) => {
  if (!value) return '-'
  return formatDate(value, props.column.format || 'YYYY-MM-DD HH:mm:ss')
}

// 格式化数字
const formatNumber = (value) => {
  if (value === null || value === undefined) return '-'
  return formatNum(value, props.column.precision || 2)
}

// 获取标签类型
const getTagType = (value) => {
  const typeMap = props.column.tagTypeMap || {
    '成功': 'success',
    '进行中': 'warning',
    '失败': 'danger',
    '已完成': 'success',
    '已中断': 'info'
  }
  return typeMap[value] || 'info'
}

// 获取状态图标
const getStatusIcon = (value) => {
  const iconMap = props.column.statusIconMap || {
    '在线': 'CircleCheckFilled',
    '离线': 'CircleCloseFilled',
    '连接中': 'Loading'
  }
  return iconMap[value] || 'InfoFilled'
}

// 获取状态样式类
const getStatusClass = (value) => {
  const classMap = props.column.statusClassMap || {
    '在线': 'status-success',
    '离线': 'status-danger',
    '连接中': 'status-warning'
  }
  return classMap[value] || 'status-info'
}

// 处理操作
const handleAction = (action) => {
  emit('action', action, props.value, props.row, props.column)
}
</script>

<style lang="scss" scoped>
.config-driven-cell {
  display: flex;
  align-items: center;
  
  .status-cell {
    display: flex;
    align-items: center;
    gap: 4px;
    
    .el-icon {
      font-size: 14px;
      
      &.status-success {
        color: #67c23a;
      }
      
      &.status-warning {
        color: #e6a23c;
      }
      
      &.status-danger {
        color: #f56c6c;
      }
      
      &.status-info {
        color: #909399;
      }
    }
  }
  
  .cell-actions {
    display: flex;
    gap: 4px;
  }
}
</style>
