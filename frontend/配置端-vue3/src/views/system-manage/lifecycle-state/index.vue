<template>
  <div class="lifecycle-state-manage">
    <div class="page-header">
      <h2>生命周期状态</h2>
      <p class="text-muted">管理对象生命周期状态定义</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增状态
      </el-button>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <vxe-table
        ref="tableRef"
        :data="tableData"
        :loading="loading"
        border
        stripe
        resizable
        height="600"
      >
        <vxe-column type="seq" width="60" title="序号" />
        <vxe-column field="stateName" title="状态名称" min-width="150" sortable />
        <vxe-column field="stateCode" title="状态编码" width="120" sortable />
        <vxe-column field="stateType" title="状态类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getStateTypeTagType(row.stateType)">
              {{ row.stateType }}
            </el-tag>
          </template>
        </vxe-column>
        <vxe-column field="description" title="描述" min-width="200" show-overflow />
        <vxe-column field="status" title="状态" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.status" />
          </template>
        </vxe-column>
        <vxe-column title="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </vxe-column>
      </vxe-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const tableRef = ref()
const loading = ref(false)

const tableData = ref([
  {
    id: 1,
    stateName: '草稿',
    stateCode: 'DRAFT',
    stateType: 'INITIAL',
    description: '初始草稿状态',
    status: true
  },
  {
    id: 2,
    stateName: '已发布',
    stateCode: 'PUBLISHED',
    stateType: 'ACTIVE',
    description: '已发布状态',
    status: true
  }
])

const getStateTypeTagType = (stateType: string) => {
  const typeMap: Record<string, string> = {
    'INITIAL': 'info',
    'ACTIVE': 'success',
    'INACTIVE': 'warning',
    'FINAL': 'danger'
  }
  return typeMap[stateType] || 'default'
}

const handleAdd = () => {
  ElMessage.info('新增状态功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑状态: ${row.stateName}`)
}

const handleDelete = (row: any) => {
  ElMessage.info(`删除状态: ${row.stateName}`)
}

onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.lifecycle-state-manage {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.text-muted {
  color: #909399;
  margin: 0;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.table-container {
  margin-bottom: 20px;
}
</style>
