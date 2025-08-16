<template>
  <div class="view-manage">
    <div class="page-header">
      <h2>视图管理</h2>
      <p class="text-muted">管理系统视图配置，包括列表视图、详情视图、表单视图等</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增视图
      </el-button>
      <el-button @click="handleCopy">
        <el-icon><CopyDocument /></el-icon>
        复制视图
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon>
        导出
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
        <vxe-column field="viewName" title="视图名称" min-width="150" sortable>
          <template #default="{ row }">
            <el-link type="primary" @click="handleEdit(row)">
              {{ row.viewName }}
            </el-link>
          </template>
        </vxe-column>
        <vxe-column field="viewCode" title="视图编码" width="120" sortable />
        <vxe-column field="viewType" title="视图类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getViewTypeTagType(row.viewType)">
              {{ getViewTypeText(row.viewType) }}
            </el-tag>
          </template>
        </vxe-column>
        <vxe-column field="objectName" title="关联对象" width="120" />
        <vxe-column field="columnCount" title="列数量" width="100" />
        <vxe-column field="description" title="描述" min-width="200" show-overflow />
        <vxe-column field="createTime" title="创建时间" width="160" />
        <vxe-column field="status" title="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              @change="handleStatusChange(row)"
            />
          </template>
        </vxe-column>
        <vxe-column title="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="info" size="small" @click="handlePreview(row)">
              预览
            </el-button>
            <el-button type="warning" size="small" @click="handleCopy(row)">
              复制
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </vxe-column>
      </vxe-table>
    </div>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, CopyDocument, Download } from '@element-plus/icons-vue'

// 响应式数据
const tableRef = ref()
const loading = ref(false)

const tableData = ref([
  {
    id: 1,
    viewName: '产品列表视图',
    viewCode: 'PRODUCT_LIST_VIEW',
    viewType: 'LIST',
    objectName: '产品',
    columnCount: 8,
    description: '产品信息列表展示视图',
    createTime: '2024-01-01 10:00:00',
    status: true
  },
  {
    id: 2,
    viewName: '产品详情视图',
    viewCode: 'PRODUCT_DETAIL_VIEW',
    viewType: 'DETAIL',
    objectName: '产品',
    columnCount: 15,
    description: '产品详细信息展示视图',
    createTime: '2024-01-02 10:00:00',
    status: true
  },
  {
    id: 3,
    viewName: '产品编辑表单',
    viewCode: 'PRODUCT_FORM_VIEW',
    viewType: 'FORM',
    objectName: '产品',
    columnCount: 12,
    description: '产品信息编辑表单视图',
    createTime: '2024-01-03 10:00:00',
    status: false
  }
])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 3
})

// 方法
const getViewTypeTagType = (viewType: string) => {
  const typeMap: Record<string, string> = {
    'LIST': 'primary',
    'DETAIL': 'success',
    'FORM': 'warning',
    'CHART': 'info'
  }
  return typeMap[viewType] || 'default'
}

const getViewTypeText = (viewType: string) => {
  const textMap: Record<string, string> = {
    'LIST': '列表视图',
    'DETAIL': '详情视图',
    'FORM': '表单视图',
    'CHART': '图表视图'
  }
  return textMap[viewType] || viewType
}

const handleAdd = () => {
  ElMessage.info('新增视图功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑视图: ${row.viewName}`)
}

const handlePreview = (row: any) => {
  ElMessage.info(`预览视图: ${row.viewName}`)
}

const handleCopy = (row?: any) => {
  if (row) {
    ElMessage.info(`复制视图: ${row.viewName}`)
  } else {
    ElMessage.info('复制视图功能开发中...')
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除视图 "${row.viewName}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const handleStatusChange = (row: any) => {
  const status = row.status ? '启用' : '禁用'
  ElMessage.success(`${status}视图成功`)
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  loadData()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  loadData()
}

const loadData = () => {
  loading.value = true
  // 模拟API请求
  setTimeout(() => {
    loading.value = false
  }, 500)
}

// 生命周期
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.view-manage {
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

.pagination {
  display: flex;
  justify-content: flex-end;
}
</style>
