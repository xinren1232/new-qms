<template>
  <div class="properties-manage">
    <div class="page-header">
      <h2>属性维护</h2>
      <p class="text-muted">管理系统属性配置，包括属性定义、数据类型、验证规则等</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        {{ t('common.add') }}
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon>
        {{ t('common.export') }}
      </el-button>
      <el-button @click="handleImport">
        <el-icon><Upload /></el-icon>
        {{ t('common.import') }}
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
        @cell-click="handleCellClick"
      >
        <vxe-column type="seq" width="60" title="序号" />
        <vxe-column field="name" title="属性名称" min-width="150" sortable>
          <template #default="{ row }">
            <el-link type="primary" @click="handleEdit(row)">
              {{ row.name }}
            </el-link>
          </template>
        </vxe-column>
        <vxe-column field="code" title="属性编码" width="120" sortable />
        <vxe-column field="dataType" title="数据类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getDataTypeTagType(row.dataType)">
              {{ row.dataType }}
            </el-tag>
          </template>
        </vxe-column>
        <vxe-column field="groupName" title="属性分组" width="120" />
        <vxe-column field="description" title="描述" min-width="200" show-overflow />
        <vxe-column field="enableFlag" title="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.enableFlag"
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
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Download, Upload } from '@element-plus/icons-vue'

const { t } = useI18n()

// 响应式数据
const tableRef = ref()
const loading = ref(false)
const tableData = ref([
  {
    id: 1,
    name: '产品名称',
    code: 'PRODUCT_NAME',
    dataType: 'STRING',
    groupName: '基本信息',
    description: '产品的名称标识',
    enableFlag: true
  },
  {
    id: 2,
    name: '创建时间',
    code: 'CREATE_TIME',
    dataType: 'DATETIME',
    groupName: '系统信息',
    description: '记录创建的时间',
    enableFlag: true
  },
  {
    id: 3,
    name: '质量等级',
    code: 'QUALITY_LEVEL',
    dataType: 'ENUM',
    groupName: '质量管理',
    description: '产品质量等级分类',
    enableFlag: false
  }
])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 3
})

// 方法
const getDataTypeTagType = (dataType: string) => {
  const typeMap: Record<string, string> = {
    'STRING': 'primary',
    'NUMBER': 'success',
    'DATETIME': 'warning',
    'ENUM': 'info',
    'BOOLEAN': 'danger'
  }
  return typeMap[dataType] || 'default'
}

const handleAdd = () => {
  ElMessage.info('新增功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑属性: ${row.name}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除属性 "${row.name}" 吗？`,
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
  const status = row.enableFlag ? '启用' : '禁用'
  ElMessage.success(`${status}成功`)
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleImport = () => {
  ElMessage.info('导入功能开发中...')
}

const handleCellClick = (params: any) => {
  console.log('单元格点击:', params)
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
.properties-manage {
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
