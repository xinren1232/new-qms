<template>
  <div class="object-manage">
    <div class="page-header">
      <h2>对象管理</h2>
      <p class="text-muted">管理系统对象定义，包括对象属性、关系、生命周期等</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增对象
      </el-button>
      <el-button @click="handleImport">
        <el-icon><Upload /></el-icon>
        导入
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
        <vxe-column field="objectName" title="对象名称" min-width="150" sortable>
          <template #default="{ row }">
            <el-link type="primary" @click="handleEdit(row)">
              {{ row.objectName }}
            </el-link>
          </template>
        </vxe-column>
        <vxe-column field="objectCode" title="对象编码" width="120" sortable />
        <vxe-column field="objectType" title="对象类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getObjectTypeTagType(row.objectType)">
              {{ row.objectType }}
            </el-tag>
          </template>
        </vxe-column>
        <vxe-column field="attributeCount" title="属性数量" width="100" />
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
            <el-button type="info" size="small" @click="handleAttributes(row)">
              属性
            </el-button>
            <el-button type="warning" size="small" @click="handleRelations(row)">
              关系
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
import { Plus, Upload, Download } from '@element-plus/icons-vue'

// 响应式数据
const tableRef = ref()
const loading = ref(false)

const tableData = ref([
  {
    id: 1,
    objectName: '产品',
    objectCode: 'PRODUCT',
    objectType: 'BUSINESS',
    attributeCount: 15,
    description: '产品基础信息对象',
    createTime: '2024-01-01 10:00:00',
    status: true
  },
  {
    id: 2,
    objectName: '文档',
    objectCode: 'DOCUMENT',
    objectType: 'BUSINESS',
    attributeCount: 12,
    description: '文档管理对象',
    createTime: '2024-01-02 10:00:00',
    status: true
  },
  {
    id: 3,
    objectName: '用户',
    objectCode: 'USER',
    objectType: 'SYSTEM',
    attributeCount: 8,
    description: '系统用户对象',
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
const getObjectTypeTagType = (objectType: string) => {
  const typeMap: Record<string, string> = {
    'BUSINESS': 'primary',
    'SYSTEM': 'warning',
    'CUSTOM': 'success'
  }
  return typeMap[objectType] || 'default'
}

const handleAdd = () => {
  ElMessage.info('新增对象功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑对象: ${row.objectName}`)
}

const handleAttributes = (row: any) => {
  ElMessage.info(`管理属性: ${row.objectName}`)
}

const handleRelations = (row: any) => {
  ElMessage.info(`管理关系: ${row.objectName}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除对象 "${row.objectName}" 吗？`,
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
  ElMessage.success(`${status}对象成功`)
}

const handleImport = () => {
  ElMessage.info('导入功能开发中...')
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
.object-manage {
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
