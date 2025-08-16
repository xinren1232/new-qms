<template>
  <el-card class="toolbar-card" shadow="never">
    <div class="toolbar-content">
      <div class="toolbar-left">
        <span class="data-count">共 {{ totalCount }} 条数据</span>
        <el-tag v-if="selectedCount > 0" type="info" size="small">
          已选择 {{ selectedCount }} 项
        </el-tag>
      </div>
      
      <div class="toolbar-right">
        <!-- 动态操作按钮 -->
        <el-button
          v-for="action in visibleActions"
          :key="action.key"
          :type="action.type || 'default'"
          :size="action.size || 'small'"
          :icon="action.icon"
          :disabled="action.disabled || (action.needSelection && selectedCount === 0)"
          :loading="action.loading"
          @click="handleAction(action)"
        >
          {{ action.label }}
        </el-button>
        
        <!-- 更多操作下拉菜单 -->
        <el-dropdown v-if="moreActions.length > 0" @command="handleAction">
          <el-button size="small" icon="More">
            更多操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-for="action in moreActions"
                :key="action.key"
                :command="action"
                :disabled="action.disabled || (action.needSelection && selectedCount === 0)"
              >
                <el-icon v-if="action.icon">
                  <component :is="action.icon" />
                </el-icon>
                {{ action.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        
        <!-- 表格设置 -->
        <el-dropdown @command="handleTableSetting">
          <el-button size="small" icon="Setting">
            表格设置<el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="refresh">
                <el-icon><Refresh /></el-icon>
                刷新数据
              </el-dropdown-item>
              <el-dropdown-item command="export">
                <el-icon><Download /></el-icon>
                导出数据
              </el-dropdown-item>
              <el-dropdown-item command="columns">
                <el-icon><Setting /></el-icon>
                列设置
              </el-dropdown-item>
              <el-dropdown-item command="fullscreen">
                <el-icon><FullScreen /></el-icon>
                全屏显示
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  actions: {
    type: Array,
    default: () => []
  },
  selectedCount: {
    type: Number,
    default: 0
  },
  totalCount: {
    type: Number,
    default: 0
  },
  maxVisibleActions: {
    type: Number,
    default: 4
  }
})

const emit = defineEmits(['action', 'table-setting'])

// 可见的操作按钮
const visibleActions = computed(() => {
  return props.actions.slice(0, props.maxVisibleActions)
})

// 更多操作按钮
const moreActions = computed(() => {
  return props.actions.slice(props.maxVisibleActions)
})

// 处理操作
const handleAction = (action) => {
  emit('action', action)
}

// 处理表格设置
const handleTableSetting = (command) => {
  emit('table-setting', command)
}
</script>

<style lang="scss" scoped>
.toolbar-card {
  margin-bottom: 16px;
  
  :deep(.el-card__body) {
    padding: 12px 16px;
  }
}

.toolbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .data-count {
    font-size: 14px;
    color: #606266;
  }
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .toolbar-content {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .toolbar-left {
    justify-content: center;
  }
  
  .toolbar-right {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
