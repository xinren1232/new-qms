<template>
  <el-card class="table-card" shadow="never">
    <el-table
      :data="data"
      v-loading="loading"
      stripe
      border
      style="width: 100%"
      :height="height"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      @row-click="handleRowClick"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="selection"
        type="selection"
        width="55"
        fixed="left"
      />
      
      <!-- 序号列 -->
      <el-table-column
        v-if="index"
        type="index"
        label="序号"
        width="60"
        fixed="left"
      />
      
      <!-- 动态数据列 -->
      <el-table-column
        v-for="column in columns"
        :key="column.prop"
        :prop="column.prop"
        :label="column.label"
        :width="column.width"
        :min-width="column.minWidth"
        :fixed="column.fixed"
        :sortable="column.sortable"
        :show-overflow-tooltip="column.showTooltip !== false"
      >
        <template #default="{ row }">
          <ConfigDrivenCell
            :value="row[column.prop]"
            :column="column"
            :row="row"
            @action="handleCellAction"
          />
        </template>
      </el-table-column>
      
      <!-- 操作列 -->
      <el-table-column
        v-if="rowActions.length > 0"
        label="操作"
        :width="getActionColumnWidth()"
        fixed="right"
      >
        <template #default="{ row }">
          <div class="row-actions">
            <el-button
              v-for="action in getRowActions(row)"
              :key="action.key"
              :type="action.type || 'text'"
              :size="action.size || 'small'"
              :icon="action.icon"
              :disabled="action.disabled"
              @click="handleRowAction(action, row)"
            >
              {{ action.label }}
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div v-if="pagination" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import ConfigDrivenCell from './Cell.vue'

const props = defineProps({
  columns: {
    type: Array,
    default: () => []
  },
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  pagination: {
    type: Object,
    default: null
  },
  rowActions: {
    type: Array,
    default: () => []
  },
  selection: {
    type: Boolean,
    default: false
  },
  index: {
    type: Boolean,
    default: false
  },
  height: {
    type: [String, Number],
    default: null
  }
})

const emit = defineEmits([
  'selection-change',
  'sort-change',
  'row-action',
  'cell-action',
  'row-click',
  'page-change',
  'size-change'
])

// 获取操作列宽度
const getActionColumnWidth = () => {
  const maxActions = Math.max(...props.data.map(row => getRowActions(row).length))
  return Math.max(maxActions * 70, 120)
}

// 获取行操作按钮
const getRowActions = (row) => {
  return props.rowActions.filter(action => {
    // 可以根据行数据动态判断按钮是否显示
    if (action.condition) {
      return action.condition(row)
    }
    return true
  })
}

// 选择变化
const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

// 排序变化
const handleSortChange = (sortInfo) => {
  emit('sort-change', sortInfo)
}

// 行操作
const handleRowAction = (action, row) => {
  emit('row-action', action, row)
}

// 单元格操作
const handleCellAction = (action, value, row, column) => {
  emit('cell-action', action, value, row, column)
}

// 行点击
const handleRowClick = (row) => {
  emit('row-click', row)
}

// 分页变化
const handlePageChange = (page) => {
  emit('page-change', page)
}

// 页面大小变化
const handleSizeChange = (size) => {
  emit('size-change', size)
}
</script>

<style lang="scss" scoped>
.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.row-actions {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.pagination-wrapper {
  padding: 16px;
  text-align: right;
  border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .pagination-wrapper {
    text-align: center;
    
    :deep(.el-pagination) {
      justify-content: center;
    }
  }
}
</style>
