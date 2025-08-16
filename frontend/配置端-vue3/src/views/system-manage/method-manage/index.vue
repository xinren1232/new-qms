<template>
  <div class="method-manage">
    <div class="page-header">
      <h2>方法管理</h2>
      <p class="text-muted">管理系统方法定义，包括业务方法、工作流方法等</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增方法
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
        <vxe-column field="methodName" title="方法名称" min-width="150" sortable>
          <template #default="{ row }">
            <el-link type="primary" @click="handleEdit(row)">
              {{ row.methodName }}
            </el-link>
          </template>
        </vxe-column>
        <vxe-column field="methodCode" title="方法编码" width="120" sortable />
        <vxe-column field="methodType" title="方法类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getMethodTypeTagType(row.methodType)">
              {{ row.methodType }}
            </el-tag>
          </template>
        </vxe-column>
        <vxe-column field="objectName" title="关联对象" width="120" />
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
import { Plus, Download } from '@element-plus/icons-vue'

// 响应式数据
const tableRef = ref()
const loading = ref(false)

const tableData = ref([
  {
    id: 1,
    methodName: '创建产品',
    methodCode: 'CREATE_PRODUCT',
    methodType: 'BUSINESS',
    objectName: '产品',
    description: '创建新产品的业务方法',
    createTime: '2024-01-01 10:00:00',
    status: true
  },
  {
    id: 2,
    methodName: '审批流程',
    methodCode: 'APPROVAL_WORKFLOW',
    methodType: 'WORKFLOW',
    objectName: '文档',
    description: '文档审批工作流方法',
    createTime: '2024-01-02 10:00:00',
    status: true
  }
])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 2
})

// 方法
const getMethodTypeTagType = (methodType: string) => {
  const typeMap: Record<string, string> = {
    'BUSINESS': 'primary',
    'WORKFLOW': 'success',
    'SYSTEM': 'warning'
  }
  return typeMap[methodType] || 'default'
}

const handleAdd = () => {
  ElMessage.info('新增方法功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑方法: ${row.methodName}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除方法 "${row.methodName}" 吗？`,
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
  ElMessage.success(`${status}方法成功`)
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
  setTimeout(() => {
    loading.value = false
  }, 500)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.method-manage {
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
