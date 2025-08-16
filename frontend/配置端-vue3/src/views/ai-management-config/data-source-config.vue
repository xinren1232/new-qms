<template>
  <div class="data-source-config">
    <div class="page-header">
      <h2>数据源配置</h2>
      <p class="text-muted">配置AI训练和推理所需的数据源</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增数据源
      </el-button>
      <el-button @click="handleTest">
        <el-icon><Connection /></el-icon>
        测试连接
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
        <vxe-column field="sourceName" title="数据源名称" min-width="150" sortable />
        <vxe-column field="sourceType" title="数据源类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getSourceTypeTagType(row.sourceType)">
              {{ getSourceTypeText(row.sourceType) }}
            </el-tag>
          </template>
        </vxe-column>
        <vxe-column field="connectionUrl" title="连接地址" min-width="200" show-overflow />
        <vxe-column field="status" title="连接状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'connected' ? 'success' : 'danger'">
              {{ row.status === 'connected' ? '已连接' : '未连接' }}
            </el-tag>
          </template>
        </vxe-column>
        <vxe-column field="lastSync" title="最后同步" width="160" />
        <vxe-column field="dataCount" title="数据量" width="100" />
        <vxe-column title="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="handleSync(row)">
              同步
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
import { Plus, Connection } from '@element-plus/icons-vue'

const tableRef = ref()
const loading = ref(false)

const tableData = ref([
  {
    id: 1,
    sourceName: 'QMS主数据库',
    sourceType: 'DATABASE',
    connectionUrl: 'mysql://localhost:3306/qms_db',
    status: 'connected',
    lastSync: '2024-01-01 10:00:00',
    dataCount: 50000
  },
  {
    id: 2,
    sourceName: '文档知识库',
    sourceType: 'FILE',
    connectionUrl: '/data/documents/',
    status: 'connected',
    lastSync: '2024-01-01 09:30:00',
    dataCount: 1200
  },
  {
    id: 3,
    sourceName: '外部API',
    sourceType: 'API',
    connectionUrl: 'https://api.example.com/v1',
    status: 'disconnected',
    lastSync: '2024-01-01 08:00:00',
    dataCount: 0
  }
])

const getSourceTypeTagType = (sourceType: string) => {
  const typeMap: Record<string, string> = {
    'DATABASE': 'primary',
    'FILE': 'success',
    'API': 'warning'
  }
  return typeMap[sourceType] || 'default'
}

const getSourceTypeText = (sourceType: string) => {
  const textMap: Record<string, string> = {
    'DATABASE': '数据库',
    'FILE': '文件系统',
    'API': 'API接口'
  }
  return textMap[sourceType] || sourceType
}

const handleAdd = () => {
  ElMessage.info('新增数据源功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑数据源: ${row.sourceName}`)
}

const handleSync = (row: any) => {
  ElMessage.info(`同步数据源: ${row.sourceName}`)
}

const handleDelete = (row: any) => {
  ElMessage.info(`删除数据源: ${row.sourceName}`)
}

const handleTest = () => {
  ElMessage.info('测试连接功能开发中...')
}

onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.data-source-config {
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
