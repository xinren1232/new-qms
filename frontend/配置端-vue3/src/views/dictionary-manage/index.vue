<template>
  <div class="dictionary-manage">
    <div class="page-header">
      <h2>数据字典</h2>
      <p class="text-muted">管理系统数据字典，包括字典分类、字典项配置等</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增字典
      </el-button>
      <el-button @click="handleBatchDelete" :disabled="!selectedRows.length">
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
      <el-button @click="handleExport">
        <el-icon><Download /></el-icon>
        导出
      </el-button>
      <el-button @click="handleImport">
        <el-icon><Upload /></el-icon>
        导入
      </el-button>
    </div>

    <!-- 搜索区域 -->
    <div class="search-area">
      <el-form :model="searchForm" inline>
        <el-form-item label="字典名称">
          <el-input
            v-model="searchForm.dictName"
            placeholder="请输入字典名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="字典类型">
          <el-select
            v-model="searchForm.dictType"
            placeholder="请选择字典类型"
            clearable
            style="width: 150px"
          >
            <el-option label="系统字典" value="SYSTEM" />
            <el-option label="业务字典" value="BUSINESS" />
            <el-option label="自定义字典" value="CUSTOM" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
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
        :checkbox-config="{ checkField: 'checked' }"
        @checkbox-change="handleSelectionChange"
        @checkbox-all="handleSelectionChange"
      >
        <vxe-column type="checkbox" width="60" />
        <vxe-column type="seq" width="60" title="序号" />
        <vxe-column field="dictName" title="字典名称" min-width="150" sortable>
          <template #default="{ row }">
            <el-link type="primary" @click="handleEdit(row)">
              {{ row.dictName }}
            </el-link>
          </template>
        </vxe-column>
        <vxe-column field="dictCode" title="字典编码" width="150" sortable />
        <vxe-column field="dictType" title="字典类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getDictTypeTagType(row.dictType)">
              {{ getDictTypeText(row.dictType) }}
            </el-tag>
          </template>
        </vxe-column>
        <vxe-column field="itemCount" title="字典项数量" width="120" />
        <vxe-column field="description" title="描述" min-width="200" show-overflow />
        <vxe-column field="createTime" title="创建时间" width="160" />
        <vxe-column field="updateTime" title="更新时间" width="160" />
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
            <el-button type="info" size="small" @click="handleItems(row)">
              字典项
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
import { Plus, Delete, Download, Upload, Search, Refresh } from '@element-plus/icons-vue'

// 响应式数据
const tableRef = ref()
const loading = ref(false)
const selectedRows = ref([])

const searchForm = reactive({
  dictName: '',
  dictType: '',
  status: null
})

const tableData = ref([
  {
    id: 1,
    dictName: '用户状态',
    dictCode: 'USER_STATUS',
    dictType: 'SYSTEM',
    itemCount: 3,
    description: '用户状态字典：正常、禁用、锁定',
    createTime: '2024-01-01 10:00:00',
    updateTime: '2024-01-15 14:30:00',
    status: true,
    checked: false
  },
  {
    id: 2,
    dictName: '产品类型',
    dictCode: 'PRODUCT_TYPE',
    dictType: 'BUSINESS',
    itemCount: 5,
    description: '产品分类字典：硬件、软件、服务等',
    createTime: '2024-01-02 10:00:00',
    updateTime: '2024-01-20 16:45:00',
    status: true,
    checked: false
  },
  {
    id: 3,
    dictName: '质量等级',
    dictCode: 'QUALITY_LEVEL',
    dictType: 'BUSINESS',
    itemCount: 4,
    description: '质量等级分类：优秀、良好、合格、不合格',
    createTime: '2024-01-03 10:00:00',
    updateTime: '2024-01-25 09:15:00',
    status: false,
    checked: false
  },
  {
    id: 4,
    dictName: '审批状态',
    dictCode: 'APPROVAL_STATUS',
    dictType: 'SYSTEM',
    itemCount: 6,
    description: '审批流程状态字典',
    createTime: '2024-01-04 10:00:00',
    updateTime: '2024-01-28 11:20:00',
    status: true,
    checked: false
  },
  {
    id: 5,
    dictName: '文档类型',
    dictCode: 'DOCUMENT_TYPE',
    dictType: 'CUSTOM',
    itemCount: 8,
    description: '文档分类字典：技术文档、管理文档等',
    createTime: '2024-01-05 10:00:00',
    updateTime: '2024-01-30 13:50:00',
    status: true,
    checked: false
  }
])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 5
})

// 方法
const getDictTypeTagType = (dictType: string) => {
  const typeMap: Record<string, string> = {
    'SYSTEM': 'danger',
    'BUSINESS': 'primary',
    'CUSTOM': 'success'
  }
  return typeMap[dictType] || 'default'
}

const getDictTypeText = (dictType: string) => {
  const textMap: Record<string, string> = {
    'SYSTEM': '系统字典',
    'BUSINESS': '业务字典',
    'CUSTOM': '自定义字典'
  }
  return textMap[dictType] || dictType
}

const handleAdd = () => {
  ElMessage.info('新增字典功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑字典: ${row.dictName}`)
}

const handleItems = (row: any) => {
  ElMessage.info(`管理字典项: ${row.dictName}`)
}

const handleCopy = (row: any) => {
  ElMessage.info(`复制字典: ${row.dictName}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除字典 "${row.dictName}" 吗？`,
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

const handleBatchDelete = () => {
  if (!selectedRows.value.length) {
    ElMessage.warning('请选择要删除的字典')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个字典吗？`,
    '确认批量删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    ElMessage.success('批量删除成功')
  })
}

const handleStatusChange = (row: any) => {
  const status = row.status ? '启用' : '禁用'
  ElMessage.success(`${status}字典成功`)
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
}

const handleImport = () => {
  ElMessage.info('导入功能开发中...')
}

const handleSearch = () => {
  ElMessage.info('搜索功能开发中...')
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    dictName: '',
    dictType: '',
    status: null
  })
  loadData()
}

const handleSelectionChange = () => {
  selectedRows.value = tableData.value.filter(row => row.checked)
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
.dictionary-manage {
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

.search-area {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 6px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
}
</style>
