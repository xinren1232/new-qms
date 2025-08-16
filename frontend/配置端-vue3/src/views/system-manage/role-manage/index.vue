<template>
  <div class="role-manage">
    <div class="page-header">
      <h2>角色管理</h2>
      <p class="text-muted">管理系统角色，包括角色权限分配、用户角色关联等</p>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增角色
      </el-button>
      <el-button @click="handleBatchDelete" :disabled="!selectedRows.length">
        <el-icon><Delete /></el-icon>
        批量删除
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
        :checkbox-config="{ checkField: 'checked' }"
        @checkbox-change="handleSelectionChange"
        @checkbox-all="handleSelectionChange"
      >
        <vxe-column type="checkbox" width="60" />
        <vxe-column type="seq" width="60" title="序号" />
        <vxe-column field="roleName" title="角色名称" min-width="150" sortable>
          <template #default="{ row }">
            <el-link type="primary" @click="handleEdit(row)">
              {{ row.roleName }}
            </el-link>
          </template>
        </vxe-column>
        <vxe-column field="roleCode" title="角色编码" width="120" sortable />
        <vxe-column field="roleType" title="角色类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTypeTagType(row.roleType)">
              {{ getRoleTypeText(row.roleType) }}
            </el-tag>
          </template>
        </vxe-column>
        <vxe-column field="userCount" title="用户数量" width="100" />
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
            <el-button type="info" size="small" @click="handlePermission(row)">
              权限
            </el-button>
            <el-button type="warning" size="small" @click="handleUsers(row)">
              用户
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
import { Plus, Delete, Download } from '@element-plus/icons-vue'

// 响应式数据
const tableRef = ref()
const loading = ref(false)
const selectedRows = ref([])

const tableData = ref([
  {
    id: 1,
    roleName: '系统管理员',
    roleCode: 'SYSTEM_ADMIN',
    roleType: 'SYSTEM',
    userCount: 2,
    description: '系统最高权限管理员',
    createTime: '2024-01-01 10:00:00',
    status: true,
    checked: false
  },
  {
    id: 2,
    roleName: '质量管理员',
    roleCode: 'QUALITY_ADMIN',
    roleType: 'BUSINESS',
    userCount: 5,
    description: '质量管理相关权限',
    createTime: '2024-01-02 10:00:00',
    status: true,
    checked: false
  },
  {
    id: 3,
    roleName: '普通用户',
    roleCode: 'NORMAL_USER',
    roleType: 'BUSINESS',
    userCount: 20,
    description: '基础查看权限',
    createTime: '2024-01-03 10:00:00',
    status: false,
    checked: false
  }
])

const pagination = reactive({
  page: 1,
  size: 20,
  total: 3
})

// 方法
const getRoleTypeTagType = (roleType: string) => {
  const typeMap: Record<string, string> = {
    'SYSTEM': 'danger',
    'BUSINESS': 'primary',
    'CUSTOM': 'success'
  }
  return typeMap[roleType] || 'default'
}

const getRoleTypeText = (roleType: string) => {
  const textMap: Record<string, string> = {
    'SYSTEM': '系统角色',
    'BUSINESS': '业务角色',
    'CUSTOM': '自定义角色'
  }
  return textMap[roleType] || roleType
}

const handleAdd = () => {
  ElMessage.info('新增角色功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑角色: ${row.roleName}`)
}

const handlePermission = (row: any) => {
  ElMessage.info(`配置权限: ${row.roleName}`)
}

const handleUsers = (row: any) => {
  ElMessage.info(`管理用户: ${row.roleName}`)
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除角色 "${row.roleName}" 吗？`,
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
    ElMessage.warning('请选择要删除的角色')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除选中的 ${selectedRows.value.length} 个角色吗？`,
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
  ElMessage.success(`${status}角色成功`)
}

const handleExport = () => {
  ElMessage.info('导出功能开发中...')
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
.role-manage {
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
